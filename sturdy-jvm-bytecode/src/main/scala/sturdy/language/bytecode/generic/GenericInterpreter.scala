package sturdy.language.bytecode.generic

import org.opalj.br.instructions.*
import sturdy.effect.operandstack.{DecidableOperandStack, OperandStack}
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin, noJoin}
import sturdy.effect.callframe.{DecidableCallFrame, DecidableMutableCallFrame}
import sturdy.effect.except.Except
import sturdy.effect.failure.{CFailureException, Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.booleans.BooleanBranching
import BytecodeFailure.*
import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, BooleanType, ByteType, CharType, ClassFile, DoubleType, FieldType, FloatType, IntegerType, InvokeStaticMethodHandle, LongType, Method, MethodDescriptor, ObjectType, ObjectTypes, ReferenceType, ShortType}
import org.opalj.io.process
import sturdy.effect.EffectStack
import sturdy.values.arrays.ArrayOps
import sturdy.values.arrays.Array
import sturdy.values.objects.{Object, ObjectOps, TypeOps}
import sturdy.values.relational.EqOps
import sturdy.fix
import sturdy.values.MaybeChanged.Unchanged
import sturdy.values.{Finite, Join, MaybeChanged, Powerset}

import java.io.{DataInputStream, File, FileInputStream}
import java.net.URL
import scala.collection.View
import scala.collection.immutable.ArraySeq

enum JvmExcept[V]:
  case Jump(pc: Int)
  case Ret(pc: V)
  case Throw(exception: ObjectType)
  case ThrowObject(exception: V)

case class InstructionSite(mth: Method, pc: Int, variant: Int = 0)
case class ArrayElemInitSite(s: InstructionSite, ix: Int)
case class FieldInitSite(s: InstructionSite, name: String, cls: ObjectType)
case class StaticInitSite(obj: ObjectType, name: String)

enum FixIn:
  case Eval(inst: Instruction, mth: Method, pc: Int)

enum FixOut:
  case Eval()

given Join[FixOut] with
  override def apply(v1: FixOut, v2: FixOut): MaybeChanged[FixOut] = Unchanged(v1)

given Finite[FixIn] with {}
given Finite[FixOut] with {}

trait GenericInterpreter[V, FieldAddr, ArrayElemAddr, StaticAddr, Idx, ObjAddr, ArrayAddr, ObjType, ObjRep, TypeRep, ExcV, J[_] <: MayJoin[_]]:

  val fixpoint: fix.Fixpoint[FixIn, FixOut]
  val fixpointSuper: fix.Fixpoint[FixIn, FixOut]
  type Fixed = FixIn => FixOut

  val bytecodeOps: BytecodeOps[Idx, V, ReferenceType]
  import bytecodeOps.*
  val objectOps: ObjectOps[(ObjectType, String), ObjAddr, V, ClassFile, V, FieldInitSite, Method, String, MethodDescriptor, V, J]
  val arrayOps: ArrayOps[ArrayAddr, V, V, V, ArrayType, ArrayElemInitSite, J]

  implicit val joinUnit: J[Unit]
  implicit val jvV: J[V]

  val stack: DecidableOperandStack[V]
  val failure: Failure
  val except: Except[JvmExcept[V], ExcV, J]
  val objAlloc: Allocation[ObjAddr, InstructionSite]
  val objFieldAlloc: Allocation[FieldAddr, FieldInitSite]
  val arrayAlloc: Allocation[ArrayAddr, InstructionSite]
  val arrayValAlloc: Allocation[ArrayElemAddr, ArrayElemInitSite]
  val objFieldStore: Store[FieldAddr, V, J]
  val arrayValStore: Store[ArrayElemAddr, V, J]
  val staticVarStore: Store[StaticAddr, V, J]
  val staticAlloc: Allocation[StaticAddr, StaticInitSite]
  type FrameData = Int
  val frame: DecidableMutableCallFrame[FrameData, Int, V]

  val staticAddrMap: scala.collection.mutable.Map[(ObjectType, String), StaticAddr]


  val effectStack: EffectStack = new EffectStack(List(stack, failure, except, objFieldAlloc, objAlloc, arrayValAlloc, arrayAlloc, objFieldStore, arrayValStore, staticVarStore, frame))
  given EffectStack = effectStack

  val classStack: scala.collection.mutable.Stack[ClassFile] = scala.collection.mutable.Stack[ClassFile]()
  val stringStack: scala.collection.mutable.Stack[String] = scala.collection.mutable.Stack[String]()
  val project: Project[URL]
  val projectSource: String

  val nativeSource = org.opalj.bytecode.RTJar
  val objectCF = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/Object.class").head
  val classCF = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/Class.class").head
  val stringCF = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/String.class").head

  var staticInitialized: Set[ObjectType] = Set()

  def javaLibClassFileWrapper(obj: ObjectType): String =
    val source = "classes/" ++ obj.packageName ++ "/" ++ obj.simpleName ++ ".class"
    source

  def nonJavaLibClassFileWrapper(obj: ObjectType): String =
    val path = projectSource ++ File.separator ++ obj.simpleName ++ ".class"
    path

  private given Failure = failure
  private def fail(k: FailureKind, what: String) = failure.fail(k, s"$what")

  lazy val num = new GenericInterpreterNumerics[Idx, V, ReferenceType](bytecodeOps)
  lazy val native = new JavaNativeFunctions[V, FieldAddr, ArrayElemAddr, Idx, ObjAddr, ArrayAddr, ObjRep, TypeRep, J](bytecodeOps, objectOps, arrayOps)

  def eval(inst: Instruction, mth: Method, pc: Int)(using Fixed): Unit = {
    val site = InstructionSite(mth, pc)
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
        //val obj = createLibraryObj(inst.value.mostPreciseObjectType, site)
        //val getClassMth = objectOps.findFunction(obj, "getClass", MethodDescriptor(ArraySeq[FieldType](), ObjectType("java/lang/Class")))(findMethodOfObj)
        val cls = createLibraryObj(ObjectType("java/lang/Class"), site.copy(variant = 1))
        stack.push(cls)
//        val cls = findClassFile(inst.value.mostPreciseObjectType)
//        classStack.push(cls)


      case inst: LoadString =>
        val string = inst.value.toCharArray.map(l => l.toInt).toSeq
        val convString = string.map(l => i32ops.integerLit(l)).zipWithIndex
        val stringArray = arrayOps.makeArray(arrayAlloc(site), convString.map(vals => (vals._1, ArrayElemInitSite(site, vals._2))), ArrayType(IntegerType), i32ops.integerLit(inst.value.size))
        val stringObj = createLibraryObj(ObjectType("java/lang/String"), site)
        objectOps.setField(stringObj, (ObjectType("java/lang/String"),"value"), stringArray)
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
        val cls = createLibraryObj(ObjectType("java/lang/Class"), site.copy(variant = 1))
        stack.push(cls)
      case inst: LoadString_W =>
        val string = inst.value.toCharArray.map(l => l.toInt).toSeq
        val convString = string.map(l => i32ops.integerLit(l)).zipWithIndex
        val stringArray = arrayOps.makeArray(arrayAlloc(site), convString.map(vals => (vals._1, ArrayElemInitSite(site, vals._2))), ArrayType(IntegerType), i32ops.integerLit(inst.value.size))
        val stringObj = createLibraryObj(ObjectType("java/lang/String"), site)
        objectOps.setField(stringObj, (ObjectType("java/lang/String"),"value"), stringArray)
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
          except.throws(JvmExcept.ThrowObject(createLibraryObj(ObjectType("java/lang/IndexOutOfBoundsException"), site)))
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
          except.throws(JvmExcept.ThrowObject(createLibraryObj(ObjectType("java/lang/IndexOutOfBoundsException"), site)))
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
        val v = stack.popOrAbort()
        val isEq = eqOps.equ(v, i32ops.integerLit(0))
        branchOpsUnit.boolBranch(isEq) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IFNE =>
        val v = stack.popOrAbort()
        val isNe = eqOps.neq(v, i32ops.integerLit(0))
        branchOpsUnit.boolBranch(isNe) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IFLT =>
        val v = stack.popOrAbort()
        val isLt = compareOps.lt(v, i32ops.integerLit(0))
        branchOpsUnit.boolBranch(isLt) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IFGE =>
        val v = stack.popOrAbort()
        val isGe = compareOps.ge(v, i32ops.integerLit(0))
        branchOpsUnit.boolBranch(isGe) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IFGT =>
        val v = stack.popOrAbort()
        val isGt = compareOps.gt(v, i32ops.integerLit(0))
        branchOpsUnit.boolBranch(isGt) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IFLE =>
        val v = stack.popOrAbort()
        val isLe = compareOps.le(v, i32ops.integerLit(0))
        branchOpsUnit.boolBranch(isLe) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IF_ICMPEQ =>
        val (v1, v2) = stack.pop2OrAbort()
        val isEq = eqOps.equ(v1, v2)
        branchOpsUnit.boolBranch(isEq) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IF_ICMPNE =>
        val (v1, v2) = stack.pop2OrAbort()
        val isNe = eqOps.neq(v1, v2)
        branchOpsUnit.boolBranch(isNe) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IF_ICMPLT =>
        val (v1, v2) = stack.pop2OrAbort()
        val isLt = compareOps.lt(v1, v2)
        branchOpsUnit.boolBranch(isLt) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IF_ICMPGE =>
        val (v1, v2) = stack.pop2OrAbort()
        val isGe = compareOps.ge(v1, v2)
        branchOpsUnit.boolBranch(isGe) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IF_ICMPGT =>
        val (v1, v2) = stack.pop2OrAbort()
        val isGt = compareOps.gt(v1, v2)
        branchOpsUnit.boolBranch(isGt) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IF_ICMPLE =>
        val (v1, v2) = stack.pop2OrAbort()
        val isLe = compareOps.le(v1, v2)
        branchOpsUnit.boolBranch(isLe) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IF_ACMPEQ =>
        val(v1, v2) = stack.pop2OrAbort()
        val isEq = eqOps.equ(v1, v2)
        branchOpsUnit.boolBranch(isEq){
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
      case inst: IF_ACMPNE =>
        val (v1, v2) = stack.pop2OrAbort()
        val isNe = eqOps.neq(v1, v2)
        branchOpsUnit.boolBranch(isNe) {
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        } { }
        
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
        if(inst.name == "out")
          ()
        else
          val objCF = inst.declaringClass
          ensureInitialization(objCF)
          val addr = staticAddrMap((objCF, inst.name))
          val v = staticVarStore.readOrElse(addr, fail(UnboundStaticVar, inst.name))
          stack.push(v)

      case inst: PUTSTATIC =>
        val objCF = inst.declaringClass
        ensureInitialization(objCF)
        val v = stack.popOrAbort()
        val addr = staticAlloc(StaticInitSite(objCF, inst.name))
        staticAddrMap.addOne((objCF, inst.name), addr)
        staticVarStore.write(addr, v)

      // Load and Store Fields opcode 180 - 181
      case inst: GETFIELD =>
        val obj = stack.popOrAbort()
        val field = objectOps.getField(obj, (inst.declaringClass, inst.name)).getOrElse(fail(UnboundField, inst.name))
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
        val objectType = inst.declaringClass.mostPreciseObjectType
        val numArgs = inst.methodDescriptor.parametersCount
        if(inst.name == "println")
          val printString = stack.popOrAbort()
          val obj = createLibraryObj(ObjectType("java/io/PrintStream"), InstructionSite(mth, pc, variant = 1))
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

              val source = javaLibClassFileWrapper(receiver.receiverType.mostPreciseObjectType)
              val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
              val invokedMth = cfs.findMethod(receiver.name, receiver.methodDescriptor).get
              val args = stack.popNOrAbort(2)
              evalNativeStatic(invokedMth, args)
            }
      /*
      receiver match
        case receiver: InvokeStaticMethodHandle =>
          if (project.isLibraryType(receiver.receiverType.mostPreciseObjectType)) {
            val mthTypeSource = javaLibClassFileWrapper(ObjectType("java/lang/invoke/MethodType"))
            val mthTypeCFS: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, mthTypeSource).head
            val mthTypeMth = mthTypeCFS.findMethod("methodType", MethodDescriptor(ObjectType("java/lang/Class"), ObjectType("java/lang/invoke/MethodType")))
            stack.push(createNativeObj(inst.methodDescriptor.returnType.asObjectType))
            stack.push(createNativeObj(inst.methodDescriptor.parameterType(0).asObjectType))
            val mthTypeObj = invoke(mthTypeMth.get, true)

            val source = javaLibClassFileWrapper(receiver.receiverType.mostPreciseObjectType)
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
        if (project.isLibraryType(inst.objectType)){
          val obj = createLibraryObj(inst.objectType, site)
          stack.push(obj)
        } else {
          val cfs = project.classFile(inst.objectType).get
          val inheritedFields = project.classHierarchy.allSuperclassesIterator(inst.objectType, true)(project).map(cfs => cfs.fields).toSeq
          val fields = inheritedFields.flatMap(fields => fields.map(field => (defaultValue(convertTypes(field.fieldType)), FieldInitSite(site, field.name, field.classFile.thisType), (field.classFile.thisType, field.name))))
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
        ()

      // multianewarray opcode 197
      case inst: MULTIANEWARRAY =>
        val dims = stack.popNOrAbort(inst.dimensions)
        stack.push(createNDArray(inst.dimensions, inst.arrayType, dims.reverse, site))
        counter = 0

      // ifnull, ifnonnull opcode 198 - 199
      case inst: IFNULL =>
        val v = stack.popOrAbort()
        val flag = typeOps.instanceOf(v, null)

        branchOpsUnit.boolBranch(flag){
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        }{

        }
      case inst: IFNONNULL =>
        val v = stack.popOrAbort()
        val flag = typeOps.instanceOf(v, null)

        branchOpsUnit.boolBranch(flag){

        }{
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
  }

  def findClassFile(objType: ObjectType): ClassFile =
    if (project.isLibraryType(objType)){
      val source = javaLibClassFileWrapper(objType)
      val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
      cfs
    }
    else{
      project.classFile(objType).get
    }

  def ensureInitialization(objType: ObjectType)(using Fixed): Unit =
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

  def createLibraryObj(toLoad: ObjectType, site: InstructionSite): V =
    val cfs = findClassFile(toLoad)
    val inheritedFields = project.classHierarchy.allSuperclassesIterator(toLoad, true)(project).map(cfs => cfs.fields).toSeq.distinct
    val fields = inheritedFields.flatMap(fields => fields.map(field => (defaultValue(convertTypes(field.fieldType)), FieldInitSite(site, field.name, field.classFile.thisType), (field.classFile.thisType, field.name))))
    val obj = objectOps.makeObject(objAlloc(site), cfs, fields)
    obj

  def createArray(size: V, compType: ArrayType, site: InstructionSite): V =
    val arrayVals = arrayOps.initArray(size)
    val convertedArrayVals = arrayVals.map(_ => compType.elementType).map(convertTypes).map(defaultValue)
      .zipWithIndex.map(vals => (vals._1, ArrayElemInitSite(site, vals._2)))
    val array = arrayOps.makeArray(arrayAlloc(site), convertedArrayVals, compType, size)
    array

  var counter = 0
  def createNDArray(numDims: Int, compType: ArrayType, dims: List[V], site: InstructionSite): V =
    if (numDims == 2) {
      val counterSnap = counter
      //println("ND IF: " + counterSnap)
      counter += 1
      val temp = arrayOps.initArray(dims(1))
      val temp2 = temp.zipWithIndex.map(vals => (create2DArray(dims.head, compType, InstructionSite(site.mth, site.pc)), ArrayElemInitSite(InstructionSite(site.mth, site.pc, counterSnap), vals._2)))
      val array = arrayOps.makeArray(arrayAlloc(InstructionSite(site.mth, site.pc, counterSnap)), temp2, compType, dims(1))
      array
    }
    else{
      val counterSnap = counter
      //println("ND Else: " + counterSnap)
      counter += 1
      val temp = arrayOps.initArray(dims(numDims-1))
      val temp2 = temp.zipWithIndex.map(vals => (createNDArray(numDims-1, compType.componentType.asArrayType, dims, site), ArrayElemInitSite(InstructionSite(site.mth, site.pc, counterSnap), vals._2)))
      val array = arrayOps.makeArray(arrayAlloc(InstructionSite(site.mth, site.pc, counterSnap)), temp2, compType, dims(numDims-1))
      array
    }

  def create2DArray(size: V, compType: ArrayType, site: InstructionSite): V =
    val counterSnap = counter
    //println("2D: " + counterSnap)
    counter += 1
    val arrayVals = arrayOps.initArray(size)
    val convertedArrayVals = arrayVals.map(_ => compType.elementType).map(convertTypes).map(defaultValue)
      .zipWithIndex.map(vals => (vals._1, ArrayElemInitSite(InstructionSite(site.mth, site.pc, counterSnap), vals._2)))
    val array = arrayOps.makeArray(arrayAlloc(InstructionSite(site.mth, site.pc, counterSnap)), convertedArrayVals, compType, size)
    array

  def invokeWrapper(obj: V, mth: Method, args: Seq[V])(using Fixed): V =

    invoke(mth, obj +: args)

  def invoke(mth: Method, args: Seq[V])(using Fixed): V =
    val newFrameData = 0

    if(mth.name == "println")
      val string = arrayOps.getArray(objectOps.getField(args(1), (ObjectType("java/lang/String"), "value")).get).map(vals => vals.get)
      println(string)
      i32ops.integerLit(-1)
    else
      if (native.nativeFunList.contains(mth.name)) {
        val ret = invokeClassMethod(mth, args)
        if (!mth.descriptor.returnType.isVoidType) {
          ret
        }
        else{
          i32ops.integerLit(-1)
        }
      }
      else{
        val locals = if (!mth.body.get.localVariableTable.isEmpty) {
          mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
        }
        else {
          ArraySeq.fill(mth.body.get.maxLocals)(0).map(_ => ValType.I32)
        }

        val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap

        val argsAndLocals = args.view ++ locals.map(defaultValue)

        val remainingOperands = stack.popNOrAbort(stack.size)

        stack.withNewFrame(0) {
          frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map(_.swap)) {
            run(0, instructionMap, mth)
          }
        }
        if(!mth.descriptor.returnType.isVoidType){
          val ret = stack.popOrAbort()
          stack.pushN(remainingOperands)
          ret
        }
        else{
          stack.pushN(remainingOperands)
          i32ops.integerLit(-1)
        }
      }

  def evalNativeStatic(mth: Method, args: Seq[V]) =
    mth.name match
      case "makeConcatWithConstants" =>
        //val testBase = objectOps.getField(args(0), (ObjectType("java/lang/String"),"value")).get
        val baseString = arrayOps.getArray(objectOps.getField(args(0), (ObjectType("java/lang/String"),"value")).get).map(vals => vals.get)
        val constantString = arrayOps.getArray(objectOps.getField(args(1), (ObjectType("java/lang/String"),"value")).get).map(vals => vals.get)
        val concattedString = (baseString ++ constantString).zipWithIndex
        val site = InstructionSite(mth, 0)
        val stringArray = arrayOps.makeArray(arrayAlloc(site),
          concattedString.map(vals => (vals._1, ArrayElemInitSite(site, vals._2))), ArrayType(IntegerType), i32ops.integerLit(concattedString.size))
        val stringObj = createLibraryObj(ObjectType("java/lang/String"), InstructionSite(mth, 0))
        objectOps.setField(stringObj, (ObjectType("java/lang/String"),"value"), stringArray)
        stack.push(stringObj)
      case _ =>
        native.evalNative(mth, args)

  def invokeExternal(mth: Method, isStatic: Boolean) = external {
    val args = stack.popNOrAbort(stack.size)
    invoke(mth, args)
  }
  def evalExternal(inst: Instruction) = external {
    eval(inst, null, 0)
  }
  inline def evalFix(inst: Instruction, mth: Method, pc: Int)(using rec: Fixed): FixOut =
    rec(FixIn.Eval(inst, mth, pc))

  private def fixed: Fixed = fixpointSuper {
    case FixIn.Eval(inst, mth, pc) =>
      eval(inst, mth, pc)
      FixOut.Eval()
  }
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  def run(pc: Int, instructionMap: Map[Int, Instruction], mth: Method)(using Fixed): Unit =
    except.tryCatch {
      runBody(pc, instructionMap, mth)
    } {
      case JvmExcept.Jump(targetPC) =>
        run(targetPC, instructionMap, mth)
      case JvmExcept.Ret(currPC) =>
        //TODO transform V to Int
        //val currInst = instructionMap(currPC)
        //val nextPC = currInst.indexOfNextInstruction(currPC)(mth.body.get)
        //run(nextPC, instructionMap, mth)
      case JvmExcept.Throw(exception) =>
        val currPC = frame.data
        val handler = mth.body.get.exceptionHandlersFor(currPC)
          .find(handlerException => exception.isSubtypeOf(handlerException.catchType.get)(project.classHierarchy))
          .getOrElse(except.throws(JvmExcept.Throw(exception)))
        val exceptionObject = createLibraryObj(exception, InstructionSite(mth, pc))
        stack.push(exceptionObject)
        run(handler.handlerPC, instructionMap, mth)
      case JvmExcept.ThrowObject(exception) =>
        val currPC = frame.data
        val handler = mth.body.get.exceptionHandlersFor(currPC)
          .find(handlerException => typeOps.instanceOf(exception, handlerException.catchType.get) == i32ops.integerLit(1))
          .getOrElse(except.throws(JvmExcept.ThrowObject(exception)))
        stack.push(exception)
        run(handler.handlerPC, instructionMap, mth)
    }

  def runBody(pc: Int, instructionMap: Map[Int, Instruction], mth: Method)(using Fixed): Unit =
    val currInst = instructionMap(pc)
    frame.setData(pc)
    //println(currInst)
    evalFix(currInst, mth, pc)
    
    if (currInst.nextInstructions(pc)(mth.body.get).nonEmpty) {
      val nextPC = currInst.indexOfNextInstruction(pc)(mth.body.get)
      runBody(nextPC, instructionMap, mth)
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
    case opalTypes: ObjectType => ValType.Obj
    case opalTypes: ArrayType => ValType.Array

  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => i32ops.integerLit(0)
    case ValType.I64 => i64ops.integerLit(0)
    case ValType.F32 => f32ops.floatingLit(0)
    case ValType.F64 => f64ops.floatingLit(0)
    case ValType.Obj => objectOps.makeNull()
    case ValType.Array => objectOps.makeNull()

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
        val clsObj = createLibraryObj(ObjectType("java/lang/Class"), InstructionSite(mth, 0))
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
        val src = args(0)
        val srcPos = args(1)
        val dest = args(2)
        val destPos = args(3)
        val length = args(4)
        arrayOps.arraycopy(src, srcPos, dest, destPos, length)
        //temporary
        bytecodeOps.i32ops.integerLit(-1)





