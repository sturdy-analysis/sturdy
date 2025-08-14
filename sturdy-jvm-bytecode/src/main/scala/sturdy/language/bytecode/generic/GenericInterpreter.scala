package sturdy.language.bytecode.generic

import org.opalj.br.instructions.*
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin, noJoin}
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocator
import sturdy.values.booleans.BooleanBranching
import BytecodeFailure.*
import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, BooleanType, ByteType, CharType, ClassFile, DoubleType, FieldType, FloatType, IntegerType, InvokeStaticMethodHandle, LongType, Method, MethodDescriptor, ClassType, ReferenceType, ShortType}
import org.opalj.io.process
import sturdy.effect.{EffectList, EffectStack}
import sturdy.values.arrays.ArrayOps
import sturdy.values.objects.ObjectOps
import sturdy.fix
import sturdy.language.bytecode.abstractions.Site
import sturdy.language.bytecode.generic.FixIn.Eval
import sturdy.values.MaybeChanged.Unchanged
import sturdy.values.{Finite, Join, MaybeChanged}

import java.io.{DataInputStream, File, FileInputStream}
import java.net.URL
import scala.collection.immutable.ArraySeq

enum JvmExcept[V]:
  case Jump(pc: Int)
  case Ret(pc: V)
  case Throw(exception: ClassType)
  case ThrowObject(exception: V)

// deprecated site types
// case class InstructionSite(mth: Method, pc: Int, variant: Int = 0)
// case class ArrayElemInitSite(s: InstructionSite, ix: Int)
// case class FieldInitSite(s: InstructionSite, name: String, cls: ClassType)
// case class StaticInitSite(obj: ClassType, name: String)

type InstructionSite = Site
type ArrayElemInitSite = Site
type FieldInitSite = Site
type StaticInitSite = Site

enum FixIn:
  case Eval(inst: Instruction, mth: Method, pc: Int)
  case Jump(pc: Int, mth: Method)

enum FixOut:
  case Eval()
  case Jump()

given Join[FixOut] with
  override def apply(v1: FixOut, v2: FixOut): MaybeChanged[FixOut] = Unchanged(v1)

given Finite[FixIn] with {}
given Finite[FixOut] with {}

trait GenericInterpreter[V, Addr, Idx, ObjType, ObjRep, TypeRep, ExcV, J[_] <: MayJoin[_]]:

  val fixpoint: fix.Fixpoint[FixIn, FixOut]
  val fixpointSuper: fix.Fixpoint[FixIn, FixOut]
  type Fixed = FixIn => FixOut

  val bytecodeOps: BytecodeOps[Idx, V, ReferenceType]
  import bytecodeOps.*
  val objectOps: ObjectOps[(ClassType, String), Addr, V, ClassFile, V, FieldInitSite, Method, String, MethodDescriptor, V, J]
  val arrayOps: ArrayOps[Addr, V, V, V, ArrayType, ArrayElemInitSite, J]

  implicit val joinUnit: J[Unit]
  implicit val jvV: J[V]

  val stack: DecidableOperandStack[V]
  val failure: Failure
  val except: Except[JvmExcept[V], ExcV, J]
  val objAlloc: Allocator[Addr, InstructionSite]
  val objFieldAlloc: Allocator[Addr, FieldInitSite]
  val arrayAlloc: Allocator[Addr, InstructionSite]
  val arrayValAlloc: Allocator[Addr, ArrayElemInitSite]
  val staticAlloc: Allocator[Addr, StaticInitSite]
  val store: Store[Addr, V, J]
  type FrameData = Int
  val frame: DecidableMutableCallFrame[FrameData, Int, V, Unit]

  val staticAddrMap: scala.collection.mutable.Map[(ClassType, String), Addr]


  val effectStack: EffectStack = new EffectStack(EffectList(stack, failure, except, objFieldAlloc, objAlloc, arrayValAlloc, arrayAlloc, store, frame))
  given EffectStack = effectStack

  val classStack: scala.collection.mutable.Stack[ClassFile] = scala.collection.mutable.Stack[ClassFile]()
  val stringStack: scala.collection.mutable.Stack[String] = scala.collection.mutable.Stack[String]()
  val project: Project[URL]
  val projectSource: String

  val nativeSource: File = org.opalj.bytecode.RTJar
  val objectCF: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/Object.class").head
  // TODO: ununsed
  val classCF: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/Class.class").head
  val stringCF: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/String.class").head

  var staticInitialized: Set[ClassType] = Set()

  def javaLibClassFileWrapper(obj: ClassType): String =
    val source = "classes/" ++ obj.packageName ++ "/" ++ obj.simpleName ++ ".class"
    source

  def nonJavaLibClassFileWrapper(obj: ClassType): String =
    val path = projectSource ++ File.separator ++ obj.simpleName ++ ".class"
    path

  private given Failure = failure
  private def fail(k: FailureKind, what: String) = failure.fail(k, s"$what")

  lazy val num = new GenericInterpreterNumerics[Idx, V, ReferenceType](bytecodeOps)
  lazy val native = new JavaNativeFunctions[V, Addr, Idx, Addr, Addr, ObjRep, TypeRep, J](bytecodeOps, objectOps, arrayOps)

  def eval(inst: Instruction, mth: Method, pc: Int)(using Fixed): Unit =
    val site = Site.Instruction(mth, pc)
    inst match
      // No Op opcode 0
      case inst: NOP.type =>
        ()

      // push NULL on stack opcode 1
      case inst: ACONST_NULL.type =>
        stack.push(objectOps.makeNull())

      // Lit Ops opcode 2 - 17
      case _ if (2 <= inst.opcode && inst.opcode <= 17) =>
        stack.push(num.evalNumericOp(inst))

      // LDC opcode 18
      case inst: LoadInt =>
        stack.push(num.evalNumericOp(inst))
      case inst: LoadFloat =>
        stack.push(num.evalNumericOp(inst))
      case inst: LoadClass =>
        //val obj = createLibraryObj(inst.value.mostPreciseClassType, site)
        //val getClassMth = objectOps.findFunction(obj, "getClass", MethodDescriptor(ArraySeq[FieldType](), ClassType("java/lang/Class")))(findMethodOfObj)
        val cls = createLibraryObj(ClassType("java/lang/Class"), Site.Instruction(mth, pc, variant = 1))
        stack.push(cls)
//        val cls = findClassFile(inst.value.mostPreciseClassType)
//        classStack.push(cls)


      case inst: LoadString =>
        val string = inst.value.toCharArray.map(l => l.toInt).toSeq
        val convString = string.map(l => i32ops.integerLit(l)).zipWithIndex
        val stringArray = arrayOps.makeArray(arrayAlloc(site), convString.map(vals => (vals._1, Site.ArrayElementInitialization(site, vals._2))), ArrayType(IntegerType), i32ops.integerLit(inst.value.length))
        val stringObj = createLibraryObj(ClassType("java/lang/String"), site)
        objectOps.setField(stringObj, (ClassType("java/lang/String"),"value"), stringArray)
        stack.push(stringObj)
        stringStack.push(inst.value)

      case inst: LoadMethodHandle =>
        ???
      case inst: LoadMethodType =>
        ???

      // LDC_W opcode 19
      case inst: LoadInt_W =>
        stack.push(num.evalNumericOp(inst))
      case inst: LoadFloat_W =>
        stack.push(num.evalNumericOp(inst))
      case inst: LoadClass_W =>
        val cls = createLibraryObj(ClassType("java/lang/Class"), Site.Instruction(mth, pc, variant = 1))
        stack.push(cls)
      case inst: LoadString_W =>
        val string = inst.value.toCharArray.map(l => l.toInt).toSeq
        val convString = string.map(l => i32ops.integerLit(l)).zipWithIndex
        val stringArray = arrayOps.makeArray(arrayAlloc(site), convString.map(vals => (vals._1, Site.ArrayElementInitialization(site, vals._2))), ArrayType(IntegerType), i32ops.integerLit(inst.value.length))
        val stringObj = createLibraryObj(ClassType("java/lang/String"), site)
        objectOps.setField(stringObj, (ClassType("java/lang/String"),"value"), stringArray)
        stack.push(stringObj)
        stringStack.push(inst.value)
      case inst: LoadMethodHandle_W =>
        ???
      case inst: LoadMethodType_W =>
        ???

      // LDC2_W opcode 20
      case _ if (inst.opcode == 20) =>
        stack.push(num.evalNumericOp(inst))

      // load Local variable opcode 21 - 45
      case inst: LoadLocalVariableInstruction =>
        val v = frame.getLocalOrElse(inst.lvIndex, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))
        stack.push(v)

      //load from array opcode opcode 46 - 53
      case _ if (46 <= inst.opcode && inst.opcode <= 53) =>
        val idx = stack.popOrAbort()
        val array = stack.popOrAbort()
        val v = arrayOps.getVal(array, idx).getOrElse(
          except.throws(JvmExcept.ThrowObject(createLibraryObj(ClassType("java/lang/IndexOutOfBoundsException"), site)))
        )
        stack.push(v)

      // store local variable opcode 54 - 78
      case inst: StoreLocalVariableInstruction =>
        val v1 = stack.popOrAbort()
        frame.setLocalOrElse(inst.lvIndex, v1, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

      // store in array opcode 79 - 86
      case _ if (79 <= inst.opcode && inst.opcode <= 86) =>
        val v = stack.popOrAbort()
        val idx = stack.popOrAbort()
        val array = stack.popOrAbort()
        arrayOps.setVal(array, idx, v).getOrElse(
          except.throws(JvmExcept.ThrowObject(createLibraryObj(ClassType("java/lang/IndexOutOfBoundsException"), site)))
        )

      // Manip stack opcode 87 - 95
      case inst: POP.type =>
        stack.popOrAbort()
      case inst: POP2.type =>
        val v = stack.popOrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(v)){
          stack.popOrAbort()
        }{ }
      case inst: DUP.type =>
        val dup = stack.popOrAbort()
        stack.push(dup)
        stack.push(dup)
      case inst: DUP_X1.type =>
        val dup = stack.popOrAbort()
        val ins = stack.popOrAbort()
        stack.push(dup)
        stack.push(ins)
        stack.push(dup)
      case inst: DUP_X2.type =>
        val dup = stack.popOrAbort()
        val secondElem = stack.popOrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(secondElem)){
          val thirdElem = stack.popOrAbort()
          stack.push(dup)
          stack.push(thirdElem)
          stack.push(secondElem)
          stack.push(dup)
        }{
          stack.push(dup)
          stack.push(secondElem)
          stack.push(dup)
        }
      case inst: DUP2.type =>
        val dup1 = stack.popOrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(dup1)){
          val dup2 = stack.popOrAbort()
          stack.push(dup2)
          stack.push(dup1)
          stack.push(dup2)
          stack.push(dup1)
        }{
          stack.push(dup1)
          stack.push(dup1)
        }

      case inst: DUP2_X1.type =>
        val dup1 = stack.popOrAbort()
        val dup2 = stack.popOrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(dup1)){
          val thirdElem = stack.popOrAbort()
          stack.push(dup2)
          stack.push(dup1)
          stack.push(thirdElem)
          stack.push(dup2)
          stack.push(dup1)
        }{
          stack.push(dup1)
          stack.push(dup2)
          stack.push(dup1)
        }
      case inst: DUP2_X2.type =>
        val dup1 = stack.popOrAbort()
        val dup2 = stack.popOrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(dup1)){
          branchOpsUnit.boolBranch(sizeOps.is32Bit(dup2)){
            //dup1 64bit and dup2 32bit
            val thirdElem = stack.popOrAbort()
            stack.push(dup1)
            stack.push(thirdElem)
            stack.push(dup2)
            stack.push(dup1)
          }{
            //dup1 and dup2 64bit
            stack.push(dup1)
            stack.push(dup2)
            stack.push(dup2)
          }
        }{
          val thirdElem = stack.popOrAbort()
          branchOpsUnit.boolBranch(sizeOps.is32Bit(thirdElem)){
            //all elements 32bit
            val fourthElem = stack.popOrAbort()
            stack.push(dup2)
            stack.push(dup1)
            stack.push(fourthElem)
            stack.push(thirdElem)
            stack.push(dup2)
            stack.push(dup1)
          }{
            //dup1 and dup2 32bit, thirdElem 64bit
            stack.push(dup2)
            stack.push(dup1)
            stack.push(thirdElem)
            stack.push(dup2)
            stack.push(dup1)
          }
        }
      case inst: SWAP.type =>
        val top = stack.popOrAbort()
        val bot = stack.popOrAbort()
        stack.push(top)
        stack.push(bot)

      // Arithmetic Ops opcode 96 - 115
      case _ if (96 <= inst.opcode && inst.opcode <= 115) =>
        val (v1, v2) = stack.pop2OrAbort()
        stack.push(num.evalNumericBinOp(inst, v1, v2))

      // Negation Ops opcode 116 - 119
      case _ if (116 <= inst.opcode && inst.opcode <= 119) =>
        val v1 = stack.popOrAbort()
        stack.push(num.evalNumericUnOp(inst, v1))

      // Bitshift Ops opcode 120 - 131
      case _ if (120 <= inst.opcode && inst.opcode <= 131) =>
        val (v1, v2) = stack.pop2OrAbort()
        stack.push(num.evalNumericBinOp(inst, v1, v2))

      // iinc opcode 132
      case inst: IINC =>
        val toInc = frame.getLocalOrElse(inst.lvIndex, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))
        frame.setLocalOrElse(inst.lvIndex, i32ops.add(toInc, i32ops.integerLit(inst.constValue)), fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

      // Conversions opcode 133 - 147
      case _ if (133 <= inst.opcode && inst.opcode <= 147) =>
        val v1 = stack.popOrAbort()
        stack.push(num.evalConvertOp(inst, v1))

      // Numeric Comparison opcode 148 - 152
      case _ if (148 <= inst.opcode && inst.opcode <= 152) =>
        val (v1, v2) = stack.pop2OrAbort()
        stack.push(num.evalNumericBinOp(inst, v1, v2))

      // Branching opcode 153 - 166
      case inst: IFEQ =>
        handleIfCondInst(eqOps.equ, pc + inst.branchoffset)
      case inst: IFNE =>
        handleIfCondInst(eqOps.neq, pc + inst.branchoffset)
      case inst: IFLT =>
        handleIfCondInst(compareOps.lt, pc + inst.branchoffset)
      case inst: IFGE =>
        handleIfCondInst(compareOps.ge, pc + inst.branchoffset)
      case inst: IFGT =>
        handleIfCondInst(compareOps.gt, pc + inst.branchoffset)
      case inst: IFLE =>
        handleIfCondInst(compareOps.le, pc + inst.branchoffset)
      case inst: IF_ICMPEQ =>
        handleIfCmpInst(eqOps.equ, pc + inst.branchoffset)
      case inst: IF_ICMPNE =>
        handleIfCmpInst(eqOps.neq, pc + inst.branchoffset)
      case inst: IF_ICMPLT =>
        handleIfCmpInst(compareOps.lt, pc + inst.branchoffset)
      case inst: IF_ICMPGE =>
        handleIfCmpInst(compareOps.ge, pc + inst.branchoffset)
      case inst: IF_ICMPGT =>
        handleIfCmpInst(compareOps.gt, pc + inst.branchoffset)
      case inst: IF_ICMPLE =>
        handleIfCmpInst(compareOps.le, pc + inst.branchoffset)
      case inst: IF_ACMPEQ =>
        handleIfCmpInst(eqOps.equ, pc + inst.branchoffset)
      case inst: IF_ACMPNE =>
        handleIfCmpInst(eqOps.neq, pc + inst.branchoffset)
        
      // JUMPS opcode 167 - 171
      case inst: GOTO =>
        except.throws(JvmExcept.Jump(pc + inst.branchoffset))
      case inst: JSR =>
        stack.push(i32ops.integerLit(pc))
        except.throws(JvmExcept.Jump(pc + inst.branchoffset))
      case inst: RET =>
        val index = frame.getLocalOrElse(inst.lvIndex, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))
        except.throws(JvmExcept.Ret(index))
      case inst: TABLESWITCH =>
        val index = stack.popOrAbort()
        val transformedIndices = inst.jumpOffsets.zipWithIndex.map(pairs => (i32ops.integerLit(pairs._2), pairs._1)).toMap
        val offset = transformedIndices.getOrElse(index, inst.defaultOffset)
        except.throws(JvmExcept.Jump(pc + offset))
      case inst: LOOKUPSWITCH =>
        val key = stack.popOrAbort()
        val transformedIndices = inst.npairs.map(pairs => (i32ops.integerLit(pairs.key), pairs.value)).toMap
        val offset = transformedIndices.getOrElse(key, inst.defaultOffset)
        except.throws(JvmExcept.Jump(pc + offset))


      // Return opcode 172 - 177
      case _ if (172 <= inst.opcode && inst.opcode <= 177) =>
        ()

      // Load and Store Statics opcode 178 - 179
      case inst: GETSTATIC =>
        // TODO: why was this here?
        // if(inst.name == "out") {
          // ()
        // } else
          val objCF = inst.declaringClass
          ensureInitialization(objCF)
          val addr = staticAddrMap((objCF, inst.name))
          val v = store.readOrElse(addr, fail(UnboundStaticVar, inst.name))
          stack.push(v)

      case inst: PUTSTATIC =>
        val objCF = inst.declaringClass
        ensureInitialization(objCF)
        val v = stack.popOrAbort()
        val addr = staticAlloc(Site.StaticInitialization(objCF, inst.name))
        staticAddrMap.addOne((objCF, inst.name), addr)
        store.write(addr, v)

      // Load and Store Fields opcode 180 - 181
      case inst: GETFIELD =>
        val obj = stack.popOrAbort()
        val field = objectOps.getField(obj, (inst.declaringClass, inst.name))
        stack.push(field)
      case inst: PUTFIELD =>
        val value = stack.popOrAbort()
        val obj = stack.popOrAbort()
        objectOps.setField(obj, (inst.declaringClass, inst.name), value)


      // Invoke Functions opcode 182 - 186
      case inst: INVOKESTATIC =>
        val cfs = findClassFile(inst.declaringClass)
        val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
        val numArgs = inst.methodDescriptor.parametersCount
        val args = stack.popNOrAbort(numArgs)
        val ret = invoke(mth, args)
        if(!inst.methodDescriptor.returnType.isVoidType){
          stack.push(ret)
        }

      case inst: INVOKEVIRTUAL =>
        val classType = inst.declaringClass.mostPreciseClassType
        val numArgs = inst.methodDescriptor.parametersCount
        if(inst.name == "println" || inst.name == "print")
          val printString = stack.popOrAbort()
          val obj = createLibraryObj(ClassType("java/io/PrintStream"), Site.Instruction(mth, pc, variant = 1))
          val ret = objectOps.invokeFunctionCorrect(obj, inst.name, inst.methodDescriptor, Seq(printString))(invokeWrapper)
          if (!inst.methodDescriptor.returnType.isVoidType) {
            stack.push(ret)
          }
        else
          val args = stack.popNOrAbort(numArgs)
          val obj = stack.popOrAbort()
          val ret = objectOps.invokeFunctionCorrect(obj, inst.name, inst.methodDescriptor, args)(invokeWrapper)
          if (!inst.methodDescriptor.returnType.isVoidType){
            stack.push(ret)
          }

      case inst: INVOKESPECIAL =>
        val cfs = findClassFile(inst.declaringClass)
        val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
        val numArgs = inst.methodDescriptor.parametersCount
        val args = stack.popNOrAbort(numArgs)
        val obj = stack.popOrAbort()
        val ret = invoke(mth, obj +: args)
        if (!inst.methodDescriptor.returnType.isVoidType) {
          stack.push(ret)
        }

      case inst: INVOKEINTERFACE =>
        val numArgs = inst.methodDescriptor.parametersCount
        val args = stack.popNOrAbort(numArgs)
        val obj = stack.popOrAbort()
        val ret = objectOps.invokeFunctionCorrect(obj, inst.name, inst.methodDescriptor, args)(invokeWrapper)
        if(!inst.methodDescriptor.returnType.isVoidType){
          stack.push(ret)
        }

      case inst: INVOKEDYNAMIC =>
        val test = inst.bootstrapMethod
        val test1 = inst.name
        val test2 = inst.methodDescriptor
        val receiver = inst.bootstrapMethod.handle
        receiver match
          case receiver: InvokeStaticMethodHandle =>
            if (inst.name == "makeConcatWithConstants"){
              if(stack.size < 2){
                val test3 = inst.bootstrapMethod.arguments.head.toJava
                val test4 = test3.drop(2).dropRight(1)
                eval(LoadString(test4), mth, pc)
              }

              val source = javaLibClassFileWrapper(receiver.receiverType.mostPreciseClassType)
              val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
              val invokedMth = cfs.findMethod(receiver.name, receiver.methodDescriptor).get
              val args = stack.popNOrAbort(2)
              evalNativeStatic(invokedMth, args)
            }
          case _ => ??? // TODO: not implemented
      /*
      receiver match
        case receiver: InvokeStaticMethodHandle =>
          if (project.isLibraryType(receiver.receiverType.mostPreciseClassType)) {
            val mthTypeSource = javaLibClassFileWrapper(ClassType("java/lang/invoke/MethodType"))
            val mthTypeCFS: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, mthTypeSource).head
            val mthTypeMth = mthTypeCFS.findMethod("methodType", MethodDescriptor(ClassType("java/lang/Class"), ClassType("java/lang/invoke/MethodType")))
            stack.push(createNativeObj(inst.methodDescriptor.returnType.asClassType))
            stack.push(createNativeObj(inst.methodDescriptor.parameterType(0).asClassType))
            val mthTypeObj = invoke(mthTypeMth.get, true)

            val source = javaLibClassFileWrapper(receiver.receiverType.mostPreciseClassType)
            val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
            val mth = cfs.findMethod(receiver.name, receiver.methodDescriptor).get
            invoke(mth, true)

          }
          else{
            ???
          }
      */

      // NEW opcode 187
      case inst: NEW =>
        if (project.isLibraryType(inst.classType)){
          val obj = createLibraryObj(inst.classType, site)
          stack.push(obj)
        } else {
          val cfs = project.classFile(inst.classType).get
          val inheritedFields = project.classHierarchy.allSuperclassesIterator(inst.classType, true)(project).map(cfs => cfs.fields).toSeq
          val fields = inheritedFields.flatMap(fields => fields.map(field => (defaultValue(convertTypes(field.fieldType)), Site.FieldInitialization(site, field.name, field.classFile.thisType), (field.classFile.thisType, field.name))))
          val obj = objectOps.makeObject(objAlloc(site), cfs, fields)
          stack.push(obj)
        }

      // Arrays opcode 188 - 190
      case inst: NEWARRAY =>
        val size = stack.popOrAbort()
        val array = createArray(size, inst.arrayType, site)
        stack.push(array)
      case inst: ANEWARRAY =>
        val size = stack.popOrAbort()
        val array = createArray(size, inst.arrayType, site)
        stack.push(array)
      case inst: ARRAYLENGTH.type =>
        val array = stack.popOrAbort()
        stack.push(arrayOps.arrayLength(array))


      // athrow opcode 191
      case inst: ATHROW.type =>
        val thrown = stack.popOrAbort()
        except.throws(JvmExcept.ThrowObject(thrown))


      // checkcast opcode 192
      case inst: CHECKCAST =>
        val v = stack.popOrAbort()
        val flag = typeOps.instanceOf(v, inst.referenceType)
        branchOpsUnit.boolBranch(flag){
          stack.push(v)
        }{
          stack.push(i32ops.integerLit(0))
        }

      // instanceof opcode 193
      case inst: INSTANCEOF =>
        val v = stack.popOrAbort()
        val flag = typeOps.instanceOf(v, inst.referenceType)
        branchOpsUnit.boolBranch(flag){
          stack.push(i32ops.integerLit(1))
        }{
          stack.push(i32ops.integerLit(0))
        }


      // monitorenter opcode 194
      case _ if (inst.opcode == 194) =>
        stack.popOrAbort()

      // monitorexit opcode 195
      case _ if (inst.opcode == 195) =>
        stack.popOrAbort()

      // WIDE opcode 196
      case inst: WIDE.type =>
        // TODO: implement; ignoring it cause stack to overflow
        failure.fail(AbortEval.UnSupportedInstruction(inst), "wide instruction not implemented")

      // multianewarray opcode 197
      case inst: MULTIANEWARRAY =>
        val dims = stack.popNOrAbort(inst.dimensions)
        stack.push(createNDArray(inst.dimensions, inst.arrayType, dims.reverse, site))
        counter = 0

      // ifnull, ifnonnull opcode 198 - 199
      case inst: IFNULL =>
        handleIfInst(objectOps.isNull, pc + inst.branchoffset)
      case inst: IFNONNULL =>
        val v = stack.popOrAbort()
        val flag = objectOps.isNull(v)
        branchOpsUnit.boolBranch(flag) {} {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        }

      // goto_w opcode 200
      case inst: GOTO_W =>
        except.throws(JvmExcept.Jump(pc + inst.branchoffset))

      // jsr_wt opcode 201
      case inst: JSR_W =>
        stack.push(i32ops.integerLit(pc))
        except.throws(JvmExcept.Jump(pc + inst.branchoffset))

      // breakpoint
      case _ if (inst.opcode == 202) =>
        ()

  def findClassFile(objType: ClassType): ClassFile =
    if (project.isLibraryType(objType)){
      val source = javaLibClassFileWrapper(objType)
      val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
      cfs
    }
    else{
      project.classFile(objType).get
    }

  def ensureInitialization(objType: ClassType)(using Fixed): Unit =
    if(!staticInitialized.contains(objType)) {
      if (!staticInitialized.contains(objType)) {
        if (project.isLibraryType(objType)) {
          staticInitialized += objType
          val source = javaLibClassFileWrapper(objType)
          val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
          val void = invoke(cfs.staticInitializer.get, Seq())
        }
        else {
          staticInitialized += objType
          val source = nonJavaLibClassFileWrapper(objType)
          val cfs: List[ClassFile] =
            process(new DataInputStream(new FileInputStream(source))) { in =>
              org.opalj.br.reader.Java8Framework.ClassFile(in)
            }
          val void = invoke(cfs.head.staticInitializer.get, Seq())
        }
      }
    }

  def createLibraryObj(toLoad: ClassType, site: InstructionSite): V =
    val cfs = findClassFile(toLoad)
    val inheritedFields = project.classHierarchy.allSuperclassesIterator(toLoad, true)(project).map(cfs => cfs.fields).toSeq.distinct
    val fields = inheritedFields.flatMap(fields => fields.map(field => (defaultValue(convertTypes(field.fieldType)), Site.FieldInitialization(site, field.name, field.classFile.thisType), (field.classFile.thisType, field.name))))
    val obj = objectOps.makeObject(objAlloc(site), cfs, fields)
    obj

  def createArray(size: V, compType: ArrayType, site: InstructionSite): V =
    val arrayVals = arrayOps.initArray(size)
    val convertedArrayVals = arrayVals.map(_ => compType.elementType).map(convertTypes).map(defaultValue)
      .zipWithIndex.map(vals => (vals._1, Site.ArrayElementInitialization(site, vals._2)))
    val array = arrayOps.makeArray(arrayAlloc(site), convertedArrayVals, compType, size)
    array

  private var counter = 0
  def createNDArray(numDims: Int, compType: ArrayType, dims: List[V], site: InstructionSite): V =
    val (mth, pc, _) = site.deconstructInstruction()
    if (numDims == 2)
      val counterSnap = counter
      //println("ND IF: " + counterSnap)
      counter += 1
      val temp = arrayOps.initArray(dims(1))
      val temp2 = temp.zipWithIndex.map(vals => (create2DArray(dims.head, compType, Site.Instruction(mth, pc)), Site.ArrayElementInitialization(Site.Instruction(mth, pc, counterSnap), vals._2)))
      val array = arrayOps.makeArray(arrayAlloc(Site.Instruction(mth, pc, counterSnap)), temp2, compType, dims(1))
      array
    else
      val counterSnap = counter
      //println("ND Else: " + counterSnap)
      counter += 1
      val temp = arrayOps.initArray(dims(numDims-1))
      val temp2 = temp.zipWithIndex.map(vals => (createNDArray(numDims-1, compType.componentType.asArrayType, dims, site), Site.ArrayElementInitialization(Site.Instruction(mth, pc, counterSnap), vals._2)))
      val array = arrayOps.makeArray(arrayAlloc(Site.Instruction(mth, pc, counterSnap)), temp2, compType, dims(numDims-1))
      array

  def create2DArray(size: V, compType: ArrayType, site: InstructionSite): V =
    val (mth, pc, _) = site.deconstructInstruction()
    val counterSnap = counter
    //println("2D: " + counterSnap)
    counter += 1
    val arrayVals = arrayOps.initArray(size)
    val convertedArrayVals = arrayVals.map(_ => compType.elementType).map(convertTypes).map(defaultValue)
      .zipWithIndex.map(vals => (vals._1, Site.ArrayElementInitialization(Site.Instruction(mth, pc, counterSnap), vals._2)))
    val array = arrayOps.makeArray(arrayAlloc(Site.Instruction(mth, pc, counterSnap)), convertedArrayVals, compType, size)
    array

  def invokeWrapper(obj: V, mth: Method, args: Seq[V])(using Fixed): V =

    invoke(mth, obj +: args)

  def invoke(mth: Method, args: Seq[V])(using Fixed): V =
    val newFrameData = 0

    if(mth.name == "println" || mth.name == "print")
      val string = arrayOps.getArray(objectOps.getField(args(1), (ClassType("java/lang/String"), "value"))).map(vals => vals.get)
      arrayOps.printString(string)
      i32ops.integerLit(-1)
    // we are currently unable to properly deal with System.exit
    else if mth.classFile.thisType.simpleName == "System" && mth.name == "exit" then
      failure.fail(AbortEval.Exit(args.head), "System.exit")
    else {
      if (native.nativeFunList.contains(mth.name)) {
        val ret = invokeClassMethod(mth, args)
        if (!mth.descriptor.returnType.isVoidType) {
          ret
        }
        else{
          i32ops.integerLit(-1)
        }
      }
      else {
        val locals = if (mth.body.get.localVariableTable.isDefined) {
          mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes)
        }
        else {
          ArraySeq.fill(mth.body.get.maxLocals)(0).map(_ => ValType.I32)
        }

        val argsAndLocals = args.view ++ locals.map(defaultValue)

//        println(s"Stack before call ${stack.getState}")
//        try
        stack.withNewFrame(0) {
          frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map((x,y) => (y, Some(x))), ()) {
            run(0, mth)
            if (!mth.descriptor.returnType.isVoidType) {
//              if (stack.size != 1)
//                throw new IllegalStateException(s"Stack must have exactly one value after non-void method return: ${stack.frameSize}, ${stack.size}, ${stack.getState}.")
              stack.popOrAbort()
            } else {
              i32ops.integerLit(-1)
            }
          }
        }
//        finally println(s"Stack after call ${stack.getState}")


      }
    }

  def evalNativeStatic(mth: Method, args: Seq[V]): Unit =
    mth.name match
      case "makeConcatWithConstants" =>
        //val testBase = objectOps.getField(args(0), (ClassType("java/lang/String"),"value")).get
        val baseString = arrayOps.getArray(objectOps.getField(args.head, (ClassType("java/lang/String"),"value"))).map(vals => vals.get)
        val constantString = arrayOps.getArray(objectOps.getField(args(1), (ClassType("java/lang/String"),"value"))).map(vals => vals.get)
        val concattedString = (baseString ++ constantString).zipWithIndex
        val site = Site.Instruction(mth, 0)
        val stringArray = arrayOps.makeArray(arrayAlloc(site),
          concattedString.map(vals => (vals._1, Site.ArrayElementInitialization(site, vals._2))), ArrayType(IntegerType), i32ops.integerLit(concattedString.size))
        val stringObj = createLibraryObj(ClassType("java/lang/String"), Site.Instruction(mth, 0))
        objectOps.setField(stringObj, (ClassType("java/lang/String"),"value"), stringArray)
        stack.push(stringObj)
      case _ =>
        native.evalNative(mth, args)

  def invokeExternal(mth: Method, isStatic: Boolean): V = external {
    val args = stack.popNOrAbort(stack.size)
    invoke(mth, args)
  }
  def evalExternal(inst: Instruction): Unit = external {
    eval(inst, null, 0)
  }
  inline def evalFix(inst: Instruction, mth: Method, pc: Int)(using rec: Fixed): FixOut =
    rec(FixIn.Eval(inst, mth, pc))

  private def fixed: Fixed = fixpointSuper {
    case FixIn.Eval(inst, mth, pc) =>
      eval(inst, mth, pc)
      FixOut.Eval()
    case FixIn.Jump(pc, mth) =>
      run_open(pc, mth)
      FixOut.Jump()
  }
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  def run(pc: Int, mth: Method)(using fixed: Fixed): Unit =
    fixed(FixIn.Jump(pc, mth)) match
      case FixOut.Jump() => ()
      case out => throw new MatchError(out)

  def run_open(pc: Int, mth: Method)(using Fixed): Unit =
    except.tryCatch {
      runBody(pc, mth)
    } {
      case JvmExcept.Jump(targetPC) =>
        run(targetPC, mth)
      case JvmExcept.Ret(currPC) =>
        //TODO
      case JvmExcept.Throw(exception) =>
        val currPC = frame.data
        val handler = mth.body.get.exceptionHandlersFor(currPC)
          .find(handlerException => exception.isSubtypeOf(handlerException.catchType.get)(project.classHierarchy))
          .getOrElse(except.throws(JvmExcept.Throw(exception)))
        val exceptionObject = createLibraryObj(exception, Site.Instruction(mth, pc))
        stack.push(exceptionObject)
        run(handler.handlerPC, mth)
      case JvmExcept.ThrowObject(exception) =>
        val currPC = frame.data
        val handler = mth.body.get.exceptionHandlersFor(currPC)
          .find(handlerException => typeOps.instanceOf(exception, handlerException.catchType.get) == i32ops.integerLit(1))
          .getOrElse(except.throws(JvmExcept.ThrowObject(exception)))
        stack.push(exception)
        run(handler.handlerPC, mth)
    }

  def runBody(pc: Int, mth: Method)(using Fixed): Unit =
    val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap
    val currInst = instructionMap(pc)
    frame.setData(pc)
    //println(currInst)
//    if(currInst.isSimpleConditionalBranchInstruction)
//      println("test")
    evalFix(currInst, mth, pc)
    
    if (currInst.nextInstructions(pc)(mth.body.get).nonEmpty) {
      val nextPC = currInst.indexOfNextInstruction(pc)(mth.body.get)
      runBody(nextPC, mth)
    }

  def convertTypes(opalTypes: FieldType): ValType = opalTypes match
    case opalTypes: ByteType => ValType.I32
    case opalTypes: ShortType => ValType.I32
    case opalTypes: IntegerType => ValType.I32
    case opalTypes: FloatType => ValType.F32
    case opalTypes: LongType => ValType.I64
    case opalTypes: DoubleType => ValType.F64
    case opalTypes: BooleanType => ValType.I32
    case opalTypes: CharType => ValType.I32
    case opalTypes: ClassType => ValType.Obj
    case opalTypes: ArrayType => ValType.Array

  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => i32ops.integerLit(0)
    case ValType.I64 => i64ops.integerLit(0)
    case ValType.F32 => f32ops.floatingLit(0)
    case ValType.F64 => f64ops.floatingLit(0)
    case ValType.Obj => objectOps.makeNull()
    case ValType.Array => objectOps.makeNull()

  // helper function for all if instructions
  private def handleIfInst(predicate: V => V, target: Int): Unit =
    val v = stack.popOrAbort()
    val flag = predicate(v)
    branchOpsUnit.boolBranch(flag) {
      except.throws(JvmExcept.Jump(target))
    } {}

  // helper function for if<cond> instructions
  // issues a jump to target iff the comparison is successful
  private def handleIfCondInst(cmpOp: (V, V) => V, target: Int): Unit =
    handleIfInst(cmpOp(_, i32ops.integerLit(0)), target)

  // helper function for if_icmp<cond> and if_acmp<cond> instructions
  // issues a jump to target iff the comparison is successful
  private def handleIfCmpInst(cmpOp: (V, V) => V, target: Int): Unit =
    val v2 = stack.popOrAbort()
    handleIfInst(cmpOp(_, v2), target)

  def invokeClassMethod(mth: Method, args: Seq[V]): V =
    mth.name match
      case "desiredAssertionStatus0" =>
        i32ops.integerLit(1)
      case "forName0" =>
        ???
      case "getConstantPool" =>
        // not in docs
        ???
      case "getDeclaredClasses0" =>
        // returns array of all declared classes in this class
        ???
      case "getDeclaredConstructors0" =>
        // returns array of all constructors declared by the class
        ???
      case "getDeclaredFields0" =>
        // creates a field object of a given string name
        ???
      case "getDeclaredMethods0" =>
        // returns an array of method objects of all declared methods
        ???
      case "getDeclaringClass0" =>
        // if this class is member of another class return class object of that class
        ???
      case "getEnclosingMethod0" =>
        // if this class is local or anonymous within a method, return method object of that method
        ???
      case "getGenericSignature0" =>
        // not in docs
        ???
      case "getInterfaces0" =>
        // array of all implemented classes for objects, of all extended interfaces for interfaces
        ???
      case "getModifiers" =>
        // returns java class modifiers encoed as an integer
        ???
      case "getNestHost0" =>
        ???
      case "getNestMembers0" =>
        ???
      case "getPermittedSubclasses0" =>
        ???
      case "getPrimitiveClass" =>
        // not in docs
        val clsObj = createLibraryObj(ClassType("java/lang/Class"), Site.Instruction(mth, 0))
        clsObj
      case "getProtectionDomain" =>
        // returns protectionDomain of this class
        ???
      case "getRawAnnotations" =>
        // not in docs
        ???
      case "getRawTypeAnnotations" =>
        // not in docs
        ???
      case "getRecordComponents0" =>
        ???
      case "getSigners" =>
        // returns signers of this class
        ???
      case "getSimpleBinaryName0" =>
        ???
      case "getSuperclass" =>
        // returns class of the superclass of the encapsulated object
        ???
      case "initClassName" =>
        ???
      case "isArray" =>
        // true if this class represents an array
        ???
      case "isAssignableFrom" =>
        // true if this class is the same, or super of that class
        ???
      case "isHidden" =>
        ???
      case "isInstance" =>
        // true if that object is assignment compatable with the object represented by this class
        ???
      case "isInterface" =>
        val cls = classStack.pop()
        if (cls.isInterfaceDeclaration)
          i32ops.integerLit(1)
        else
          i32ops.integerLit(0)
      case "isPrimitive" =>
        val string = stringStack.pop()
        if(string == "int" || string ==  "float" || string == "boolean" || string == "byte" || string == "char" || string == "short" || string == "long" || string == "double")
          i32ops.integerLit(1)
        else
          i32ops.integerLit(0)
      case "isRecord0" =>
        ???
      case "registerNatives" =>
        i32ops.integerLit(-1)
      case "setSigners" =>
        // not in docs
        ???

      /*
      case "getComponentType" =>
        // returns class representing the component type of an array, this class must represent an array class
        ???
      case "getName" =>
        // returns the name of the object encapsulated by this class
        ???
      case "getName0" =>
        // returns the name of the object encapsulated by this class
        ???
      */
      case "desiredAssertionStatus" =>
        ???
      case "fillInStackTrace" =>
        //temporary
        bytecodeOps.i32ops.integerLit(-1)
      case "arraycopy" =>
        val src = args.head
        val srcPos = args(1)
        val dest = args(2)
        val destPos = args(3)
        val length = args(4)
        arrayOps.arraycopy(src, srcPos, dest, destPos, length)
        //temporary
        bytecodeOps.i32ops.integerLit(-1)

  enum AbortEval extends FailureKind:
    // abort eval due to System.exit
    case Exit(v: V)
    case UnSupportedInstruction(i: Instruction)
