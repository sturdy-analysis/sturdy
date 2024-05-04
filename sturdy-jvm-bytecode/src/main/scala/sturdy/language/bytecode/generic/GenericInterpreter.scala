package sturdy.language.bytecode.generic

import org.opalj.br.instructions.*
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.data.{JOptionC, MayJoin, noJoin}
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
import sturdy.values.Finite

import java.io.{DataInputStream, File, FileInputStream}
import java.net.URL
import scala.collection.View
import scala.collection.immutable.ArraySeq


enum JvmExcept[V]:
  case Jump(pc: Int)
  case Ret(pc: V)
  case Throw(exception: ObjectType)
  case ThrowObject(exception: V)

enum AllocationSite:
  case classFile(cfs: ClassFile)
  case objField(cfs: ClassFile, field: String)
  case array()
  case arrayVals(idx: Int)
  case default

enum FixIn:
  case Eval(inst: Instruction, pc: Int)

enum FixOut:
  case Eval()

given finiteFixIn: Finite[FixIn] with {}

trait GenericInterpreter[V, Addr, Idx, OID, AID, ObjType, ObjRep, TypeRep, J[_] <: MayJoin[_]]:

  val fixpoint: fix.ContextualFixpoint[FixIn, FixOut]
  val fixpointSuper: fix.Fixpoint[FixIn, FixOut]
  type Fixed = FixIn => FixOut

  val bytecodeOps: BytecodeOps[Addr, Idx, V, ReferenceType]
  import bytecodeOps.*
  val objectOps: ObjectOps[Addr, String, OID, V, ClassFile, Object[OID, ClassFile, Addr, String], V, AllocationSite, Method, String, MethodDescriptor, V, J]
  val arrayOps: ArrayOps[Addr, AID, V, V, Array[AID, Addr, ArrayType], V, ArrayType, AllocationSite, J]

  implicit val joinUnit: J[Unit]
  implicit val jvV: J[V]

  val stack: DecidableOperandStack[V]
  val failure: Failure
  val except: Except[JvmExcept[V], JvmExcept[V], J]
  val objFieldAlloc: Allocation[Addr, AllocationSite]
  val objAlloc: Allocation[OID, AllocationSite]
  val arrayValAlloc: Allocation[Addr, AllocationSite]
  val arrayAlloc: Allocation[AID, AllocationSite]
  val objFieldStore: Store[Addr, V, J]
  val arrayValStore: Store[Addr, V, J]
  val staticVarStore: Store[(ObjectType, String), V, J]
  type FrameData = Int
  val frame: DecidableMutableCallFrame[FrameData, Int, V]

  /*val effectStack: EffectStack = new EffectStack(List(stack, failure, except, objFieldAlloc, objAlloc, arrayValAlloc, arrayAlloc, objFieldStore, arrayValStore, staticVarStore, frame), {
    case _: FixIn.Eval => List(stack, failure, except, objFieldAlloc, objAlloc, arrayValAlloc, arrayAlloc, objFieldStore, arrayValStore, staticVarStore, frame)
  }, {
    case _: FixIn.Eval => List(stack, failure, except, objFieldAlloc, objAlloc, arrayValAlloc, arrayAlloc, objFieldStore, arrayValStore, staticVarStore, frame)
  })
  given EffectStack = effectStack*/

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
    val path = projectSource ++ "\\" ++ obj.simpleName ++ ".class"
    path

  private given Failure = failure
  private def fail(k: FailureKind, what: String) = failure.fail(k, s"$what")

  lazy val num = new GenericInterpreterNumerics[Addr, Idx, V, ReferenceType](bytecodeOps)
  lazy val native = new JavaNativeFunctions[V, Addr, Idx, OID, AID, ObjRep, TypeRep, AllocationSite, J](bytecodeOps, objectOps, arrayOps)

  def eval(inst: Instruction, pc: Int = 0)(using Fixed): Unit = inst.opcode match
    // No Op
    case x if (x == 0) =>
      ()

    // push NULL on stack
    case x if (x == 1) =>
      inst match
        case inst: ACONST_NULL.type =>
          stack.push(objectOps.makeNull())

    // Lit Ops
    case x if (2 <= x && x <= 17) =>
      stack.push(num.evalNumericOp(inst))

    // LDC
    case x if (x == 18) =>
      inst match
        case inst: LoadInt =>
          stack.push(num.evalNumericOp(inst))
        case inst: LoadFloat =>
          stack.push(num.evalNumericOp(inst))
        case inst: LoadClass =>
          val obj = createLibraryObj(inst.value.mostPreciseObjectType)
          val mth = objectOps.findFunction(obj, "getClass", MethodDescriptor(ArraySeq[FieldType](), ObjectType("java/lang/Class")))(findMethodOfObj)
          val test = createLibraryObj(ObjectType("java/lang/Class"))
          stack.push(test)

        case inst: LoadString =>
          val string = inst.value.toCharArray.map(l => l.toInt).toSeq
          val convString = string.map(l => i32ops.integerLit(l)).zipWithIndex
          val stringArray = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), convString.map(vals => (vals._1, AllocationSite.arrayVals(vals._2))), ArrayType(ObjectType("String")))
          val stringObj = createLibraryObj(ObjectType("java/lang/String"))
          objectOps.setField(stringObj, "value", stringArray)
          stack.push(stringObj)

        case inst: LoadMethodHandle =>
          ???
        case inst: LoadMethodType =>
          ???

    // LDC_W
    case x if (x == 19) =>
      inst match
        case inst: LoadInt_W =>
          stack.push(num.evalNumericOp(inst))
        case inst: LoadFloat_W =>
          stack.push(num.evalNumericOp(inst))
        case inst: LoadClass_W =>
          ???
        case inst: LoadString_W =>
          val string = inst.value.toCharArray.map(l => l.toInt).toSeq
          val convString = string.map(l => i32ops.integerLit(l)).zipWithIndex
          val stringArray = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), convString.map(vals => (vals._1, AllocationSite.arrayVals(vals._2))), ArrayType(ObjectType("String")))
          val stringObj = createLibraryObj(ObjectType("java/lang/String"))
          objectOps.setField(stringObj, "value", stringArray)
          stack.push(stringObj)
        case inst: LoadMethodHandle_W =>
          ???
        case inst: LoadMethodType_W =>
          ???

    // LDC2_W
    case x if (x == 20) =>
      stack.push(num.evalNumericOp(inst))

    // load Local variable
    case x if (21 <= x && x <= 45) =>
      stack.push(evalLocalLoad(inst))

    //load from array
    case x if (46 <= x && x <= 53) =>
      val idx = stack.popOrAbort()
      val array = stack.popOrAbort()
      stack.push(evalArrayLoad(inst, array, idx))

    // store local variable
    case x if (54 <= x && x <= 78) =>
      val v1 = stack.popOrAbort()
      evalLocalStore(inst, v1)

    // store in array
    case x if (79 <= x && x <= 86) =>
      val v = stack.popOrAbort()
      val idx = stack.popOrAbort()
      val array = stack.popOrAbort()
      evalArrayStore(inst, array, idx, v)

    // Manip stack
    case x if (87 <= x && x <= 95) =>
      inst match
        case inst: POP.type =>
          stack.popOrAbort()
        case inst: POP2.type =>
          val v = stack.popOrAbort()
          branchOpsUnit.boolBranch(sizeOps.is32Bit(v)){
            stack.popOrAbort()
          }{

          }
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

    // Arithmetic Ops
    case x if (96 <= x && x <= 115) =>
      val (v1, v2) = stack.pop2OrAbort()
      stack.push(num.evalNumericBinOp(inst, v1, v2))

    // Negation Ops
    case x if (116 <= x && x <= 119) =>
      val v1 = stack.popOrAbort()
      stack.push(num.evalNumericUnOp(inst, v1))

    // Bitshift Ops
    case x if (120 <= x && x <= 131) =>
      val (v1, v2) = stack.pop2OrAbort()
      stack.push(num.evalNumericBinOp(inst, v1, v2))

    // iinc
    case x if (x == 132) =>
      inst match
        case inst: IINC =>
          val toInc = frame.getLocalOrElse(inst.lvIndex, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))
          frame.setLocalOrElse(inst.lvIndex, i32ops.add(toInc, i32ops.integerLit(inst.constValue)), fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

    // Conversions
    case x if (133 <= x && x <= 147) =>
      val v1 = stack.popOrAbort()
      stack.push(num.evalConvertOp(inst, v1))

    // Numeric Comparison
    case x if (148 <= x && x <= 152) =>
      val (v1, v2) = stack.pop2OrAbort()
      stack.push(num.evalNumericBinOp(inst, v1, v2))

    // Branching
    case x if (153 <= x && x <= 166) =>
      inst match
        case inst: IFEQ =>
          val v = stack.popOrAbort()
          val isEq = eqOps.equ(v, i32ops.integerLit(0))
          branchOpsUnit.boolBranch(isEq) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IFNE =>
          val v = stack.popOrAbort()
          val isNe = eqOps.neq(v, i32ops.integerLit(0))
          branchOpsUnit.boolBranch(isNe) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          }{

          }
        case inst: IFLT =>
          val v = stack.popOrAbort()
          val isLt = compareOps.lt(v, i32ops.integerLit(0))
          branchOpsUnit.boolBranch(isLt) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IFGE =>
          val v = stack.popOrAbort()
          val isGe = compareOps.ge(v, i32ops.integerLit(0))
          branchOpsUnit.boolBranch(isGe) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IFGT =>
          val v = stack.popOrAbort()
          val isGt = compareOps.gt(v, i32ops.integerLit(0))
          branchOpsUnit.boolBranch(isGt) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IFLE =>
          val v = stack.popOrAbort()
          val isLe = compareOps.le(v, i32ops.integerLit(0))
          branchOpsUnit.boolBranch(isLe) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IF_ICMPEQ =>
          val (v1, v2) = stack.pop2OrAbort()
          val isEq = eqOps.equ(v1, v2)
          branchOpsUnit.boolBranch(isEq) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IF_ICMPNE =>
          val (v1, v2) = stack.pop2OrAbort()
          val isNe = eqOps.neq(v1, v2)
          branchOpsUnit.boolBranch(isNe) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IF_ICMPLT =>
          val (v1, v2) = stack.pop2OrAbort()
          val isLt = compareOps.lt(v1, v2)
          branchOpsUnit.boolBranch(isLt) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IF_ICMPGE =>
          val (v1, v2) = stack.pop2OrAbort()
          val isGe = compareOps.ge(v1, v2)
          branchOpsUnit.boolBranch(isGe) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IF_ICMPGT =>
          val (v1, v2) = stack.pop2OrAbort()
          val isGt = compareOps.gt(v1, v2)
          branchOpsUnit.boolBranch(isGt) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IF_ICMPLE =>
          val (v1, v2) = stack.pop2OrAbort()
          val isLe = compareOps.le(v1, v2)
          branchOpsUnit.boolBranch(isLe) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }
        case inst: IF_ACMPEQ =>
          val(v1, v2) = stack.pop2OrAbort()
          val isEq = eqOps.equ(v1, v2)
          branchOpsUnit.boolBranch(isEq){
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          }{

          }
        case inst: IF_ACMPNE =>
          val (v1, v2) = stack.pop2OrAbort()
          val isNe = eqOps.neq(v1, v2)
          branchOpsUnit.boolBranch(isNe) {
            except.throws(JvmExcept.Jump(pc + inst.branchoffset))
          } {

          }


    // JUMPS
    case x if (167 <= x && x <= 171) =>
      inst match
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
          val transformedIndices = Iterator.from(0).zip(inst.jumpOffsets).map(pairs => (i32ops.integerLit(pairs._1), pairs._2)).toMap
          val offset = transformedIndices.getOrElse(index, inst.defaultOffset)
          except.throws(JvmExcept.Jump(pc + offset))
        case inst: LOOKUPSWITCH =>
          val key = stack.popOrAbort()
          val transformedIndices = inst.npairs.map(pairs => (i32ops.integerLit(pairs.key), pairs.value)).toMap
          val offset = transformedIndices.getOrElse(key, inst.defaultOffset)
          except.throws(JvmExcept.Jump(pc + offset))


    // Return
    case x if (172 <= x && x <= 177) =>
      ()

    // Load and Store Statics
    case x if (178 <= x && x <= 179) =>
      inst match
        case inst: GETSTATIC =>
          val objCF = inst.declaringClass
          if(!staticInitialized.contains(objCF)){
            if (project.isLibraryType(objCF)){
              staticInitialized += objCF
              val source = javaLibClassFileWrapper(objCF)
              val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
              invoke(cfs.staticInitializer.get, true)
            }
            else{
              staticInitialized += objCF
              val source = nonJavaLibClassFileWrapper(objCF)
              val cfs: List[ClassFile] =
                process(new DataInputStream(new FileInputStream(source))) { in =>
                  org.opalj.br.reader.Java8Framework.ClassFile(in)
                }
              invoke(cfs.head.staticInitializer.get, true)
            }
          }

          val v = staticVarStore.readOrElse((objCF, inst.name), fail(UnboundStaticVar, inst.name))
          stack.push(v)

        case inst: PUTSTATIC =>
          val objCF = inst.declaringClass
          if (!staticInitialized.contains(objCF)) {
            if (project.isLibraryType(objCF)) {
              staticInitialized += objCF
              val source = javaLibClassFileWrapper(objCF)
              val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
              invoke(cfs.staticInitializer.get, true)
            }
            else {
              staticInitialized += objCF
              val source = nonJavaLibClassFileWrapper(objCF)
              println(projectSource)
              println(source)
              val cfs: List[ClassFile] =
                process(new DataInputStream(new FileInputStream(source))) { in =>
                  org.opalj.br.reader.Java8Framework.ClassFile(in)
                }
              invoke(cfs.head.staticInitializer.get, true)
            }
          }
          val v = stack.popOrAbort()
          staticVarStore.write((objCF, inst.name), v)

    // Load and Store Fields
    case x if (180 <= x && x <= 181) =>
      inst match
        case inst: GETFIELD =>
          val obj = stack.popOrAbort()
          val field = objectOps.getField(obj, inst.name).getOrElse(fail(UnboundField, inst.name))
          stack.push(field)
        case inst: PUTFIELD =>
          val value = stack.popOrAbort()
          val obj = stack.popOrAbort()
          objectOps.setField(obj, inst.name, value)


    // Invoke Functions
    case x if (182 <= x && x <= 186) =>
      inst match
        case inst: INVOKESTATIC =>
          if (project.isLibraryType(inst.declaringClass)){
            val source = javaLibClassFileWrapper(inst.declaringClass)
            val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
            val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
            invoke(mth, true)
          }
          else{
            val cfs: ClassFile = project.classFile(inst.declaringClass).get
            val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
            invoke(mth, true)
          }

        case inst: INVOKEVIRTUAL =>
          val objectType = inst.declaringClass.mostPreciseObjectType
          val numArgs = inst.methodDescriptor.parametersCount
          val args = stack.popNOrAbort(numArgs)
          val obj = stack.popOrAbort()
          stack.push(obj)
          val mth = objectOps.findFunction(obj, inst.name, inst.methodDescriptor)(findMethodOfObj)
          val ret = objectOps.invokeFunction(obj, mth, args)(invokeMethodOnObject)
          if (!mth.descriptor.returnType.isVoidType){
            stack.push(ret.get)
          }

        case inst: INVOKESPECIAL =>
          val objectType = inst.declaringClass.mostPreciseObjectType
          if (project.isLibraryType(objectType))
            val source = javaLibClassFileWrapper(objectType)
            val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
            val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
            invoke(mth, false)
          else
            val cfs = project.classFile(objectType).get
            val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
            invoke(mth, false)

        case inst: INVOKEINTERFACE =>
          val numArgs = inst.methodDescriptor.parametersCount
          val args = stack.popNOrAbort(numArgs)
          val obj = stack.popOrAbort()
          val mth = objectOps.findFunction(obj, inst.name, inst.methodDescriptor)(findMethodOfObj)
          stack.push(obj)
          stack.pushN(args)
          invoke(mth, false)

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
                  eval(LoadString(test4))
                }

                val source = javaLibClassFileWrapper(receiver.receiverType.mostPreciseObjectType)
                val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
                val mth = cfs.findMethod(receiver.name, receiver.methodDescriptor).get
                val args = stack.popNOrAbort(2)
                evalNativeStatic(mth, args)
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

    // NEW
    case x if (x == 187) =>
      inst match
        case inst: NEW =>
          if (project.isLibraryType(inst.objectType)){
            val obj = createLibraryObj(inst.objectType)
            stack.push(obj)
          }
          else{
            val cfs = project.classFile(inst.objectType).get
            val inheritedFields = project.classHierarchy.allSuperclassesIterator(inst.objectType, true)(project).map(cfs => cfs.fields).toSeq.distinct
            val fields = inheritedFields.flatMap(fields => fields.map(field => (defaultValue(convertTypes(field.fieldType)), AllocationSite.objField(cfs, field.name), field.name)))
            val obj = objectOps.makeObject(objAlloc(AllocationSite.classFile(cfs)), cfs, fields)
            stack.push(obj)
          }



    // Arrays
    case x if (188 <= x && x <= 190) =>
      inst match
        case inst: NEWARRAY =>
          val size = stack.popOrAbort()
          val array = createArray(size, inst.arrayType)
          stack.push(array)
        case inst: ANEWARRAY =>
          val size = stack.popOrAbort()
          val array = createArray(size, inst.arrayType)
          stack.push(array)
        case inst: ARRAYLENGTH.type =>
          val array = stack.popOrAbort()
          val length = arrayOps.arrayLength(array)
          stack.push(i32ops.integerLit(length))


    // athrow
    case x if (x == 191) =>
      inst match
        case inst: ATHROW.type =>
          val thrown = stack.popOrAbort()
          except.throws(JvmExcept.ThrowObject(thrown))


    // checkcast
    case x if (x == 192) =>
      inst match
        case inst: CHECKCAST =>
          val v = stack.popOrAbort()
          val flag = typeOps.instanceOf(v, inst.referenceType)
          branchOpsUnit.boolBranch(flag){
            stack.push(v)
          }{
            stack.push(i32ops.integerLit(0))
          }

    // instanceof
    case x if (x == 193) =>
      inst match
        case inst: INSTANCEOF =>
          val v = stack.popOrAbort()
          val flag = typeOps.instanceOf(v, inst.referenceType)
          branchOpsUnit.boolBranch(flag){
            stack.push(i32ops.integerLit(1))
          }{
            stack.push(i32ops.integerLit(0))
          }


    // monitorenter
    case x if (x == 194) =>
      stack.popOrAbort()

    // monitorexit
    case x if (x == 195) =>
      stack.popOrAbort()

    // WIDE
    case x if (x == 196) =>
      inst match
        case inst: WIDE.type =>
          ()

    // multianewarray
    case x if (x == 197) =>
      inst match
        case inst: MULTIANEWARRAY =>
          val dims = stack.popNOrAbort(inst.dimensions)
          stack.push(createNDArray(inst.dimensions, inst.arrayType, dims.reverse))

    // ifnull, ifnonnull
    case x if (198 <= x && x <= 199) =>
      inst match
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

    // goto_w
    case x if (x == 200) =>
      inst match
        case inst: GOTO_W =>
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))

    // jsr_wt
    case x if (x == 201) =>
      inst match
        case inst: JSR_W =>
          stack.push(i32ops.integerLit(pc))
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))

    // breakpoint
    case x if (x == 202) =>
      ()

  def createLibraryObj(toLoad: ObjectType): V =
    val source = javaLibClassFileWrapper(toLoad)
    val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
    val inheritedFields = project.classHierarchy.allSuperclassesIterator(toLoad, true)(project).map(cfs => cfs.fields).toSeq.distinct
    val fields = inheritedFields.flatMap(fields => fields.map(field => (defaultValue(convertTypes(field.fieldType)), AllocationSite.objField(cfs, field.name), field.name)))
    val obj = objectOps.makeObject(objAlloc(AllocationSite.classFile(cfs)), cfs, fields)
    obj
  def evalLocalLoad(inst: Instruction): V = inst match
    case inst: LoadLocalVariableInstruction =>
      frame.getLocalOrElse(inst.lvIndex, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

  def evalLocalStore(inst: Instruction, v: V): Unit = inst match
    case inst: StoreLocalVariableInstruction =>
      frame.setLocalOrElse(inst.lvIndex, v, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

  def evalArrayLoad(inst: Instruction, array: V, idx: V): V = inst match
    case inst: ArrayLoadInstruction =>
      arrayOps.getVal(array, idx).getOrElse(except.throws(JvmExcept.Throw(ObjectType("java/lang/IndexOutOfBoundsException"))))

  def evalArrayStore(inst: Instruction, array: V, idx: V, v: V): Unit = inst match
    case inst: ArrayStoreInstruction =>
      arrayOps.setVal(array, idx, v).getOrElse(except.throws(JvmExcept.Throw(ObjectType("java/lang/IndexOutOfBoundsException"))))

  def createArray(size: V, compType: ArrayType): V =
    val arrayVals = arrayOps.initArray(size)
    val convertedArrayVals = arrayVals.map(_ => compType.elementType).map(convertTypes).map(defaultValue)
      .zipWithIndex.map(vals => (vals._1, AllocationSite.arrayVals(vals._2)))
    val array = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), convertedArrayVals, compType)
    array

  def createNDArray(numDims: Int, compType: ArrayType, dims: List[V]): V =
    if (numDims == 2) {
      val temp = arrayOps.initArray(dims(1))
      val temp2 = temp.zipWithIndex.map(vals => (createArray(dims(0), compType), AllocationSite.arrayVals(vals._2)))
      val array = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), temp2, compType)
      array
    }
    else{
      val temp = arrayOps.initArray(dims(numDims-1))
      val temp2 = temp.zipWithIndex.map(vals => (createNDArray(numDims-1, compType.componentType.asArrayType, dims), AllocationSite.arrayVals(vals._2)))
      val array = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), temp2, compType)
      array
    }

  def findMethodOfObj(obj: Object[OID, ClassFile, Addr, String], name: String, sig: MethodDescriptor): Method =
    if (obj.cls.thisType != ObjectType("java/lang/Object")) {
      val nextInherit = project.classHierarchy.supertypeInformation(obj.cls.thisType).get.classTypes.last
      obj.cls.findMethod(name, sig)
        .getOrElse(findInheritedMethodOfObj(obj, name, sig, nextInherit))
    }
    else {
      obj.cls.findMethod(name, sig).getOrElse(fail(MethodNotFound, s"Method $name, $sig not found"))
    }

  def findInheritedMethodOfObj(obj: Object[OID, ClassFile, Addr, String], name: String, sig: MethodDescriptor, inheritedObj: ObjectType): Method =
    if(inheritedObj == ObjectType("java/lang/Object")){
      objectCF.findMethod(name, sig).getOrElse(
          obj.cls.interfaceTypes.map(interfaces => project.classFile(interfaces)).map(file => file.get.findMethod(name, sig)).head
            .getOrElse(fail(MethodNotFound, s"Method $name, $sig not found"))
      )
    }
    else{
      if (project.isLibraryType(inheritedObj)) {
        val source = javaLibClassFileWrapper(inheritedObj)
        val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
        val nextInherit = project.classHierarchy.supertypeInformation(inheritedObj).get.classTypes.last
        cfs.findMethod(name, sig)
          .getOrElse(findInheritedMethodOfObj(obj, name, sig, nextInherit))
      }
      else {
        val cfs = project.classFile(inheritedObj).get
        val nextInherit = project.classHierarchy.supertypeInformation(inheritedObj).get.classTypes.last
        cfs.findMethod(name, sig)
          .getOrElse(findInheritedMethodOfObj(obj, name, sig, nextInherit))
      }
    }

  def invokeMethodOnObject(obj: Object[OID, ClassFile, Addr, String], mth: Method, args: Seq[V])(using Fixed): JOptionC[V] =
    val newFrameData = 0
    val objVal = stack.popOrAbort()

    if (native.nativeFunList.contains(mth.name)) {
      native.evalNative(objVal, mth, args)
    }
    else{
      var locals: ArraySeq[ValType] = ArraySeq()
      if (!mth.body.get.localVariableTable.isEmpty) {
        val localstemp = mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
        locals = localstemp
      }
      else {
        val localstemp = ArraySeq.fill(mth.body.get.maxLocals - 1)(0).map(_ => ValType.I32)
        locals = localstemp
      }

      val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap

      val thisAndArgs = List(objVal) ++ args
      val argsAndLocals = thisAndArgs.view ++ locals.map(defaultValue)

      val remainingOperands = stack.popNOrAbort(stack.size)

      stack.withNewFrame(0) {
        frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map(_.swap)) {
          run(0, instructionMap, mth)
        }
      }
      val ret = stack.pop()
      stack.pushN(remainingOperands)
      ret
    }

  def invoke(mth: Method, isStatic: Boolean)(using Fixed) =
    val newFrameData = 0
    val numArgs = mth.descriptor.parametersCount
    val args = stack.popNOrAbort(numArgs)

    if (native.nativeFunList.contains(mth.name) && !isStatic) {
      val obj = stack.popOrAbort()
      val ret = native.evalNative(obj, mth, args)
      if (!mth.descriptor.returnType.isVoidType) {
        stack.push(ret.get)
      }
    }
    else if (native.nativeFunList.contains(mth.name) && isStatic) {
      val ret = native.evalNativeStatic(mth, args)
      if (!mth.descriptor.returnType.isVoidType) {
        stack.push(ret.get)
      }
    }
    else{
      var locals: ArraySeq[ValType] = ArraySeq()
      if (!mth.body.get.localVariableTable.isEmpty) {
        val localstemp = mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
        locals = localstemp
      }
      else {
        if (isStatic) {
          val localstemp = ArraySeq.fill(mth.body.get.maxLocals)(0).map(_ => ValType.I32)
          locals = localstemp
        }
        else {
          val localstemp = ArraySeq.fill(mth.body.get.maxLocals - 1)(0).map(_ => ValType.I32)
          locals = localstemp
        }
      }

      val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap

      var argsAndLocals: View[V] = View()

      if (isStatic) {
        argsAndLocals = args.view ++ locals.map(defaultValue)
      }
      else {
        val obj = stack.popOrAbort()
        val thisAndArgs = List(obj) ++ args
        argsAndLocals = thisAndArgs.view ++ locals.map(defaultValue)
      }

      val remainingOperands = stack.popNOrAbort(stack.size)

      stack.withNewFrame(0) {
        frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map(_.swap)) {
          run(0, instructionMap, mth)
        }
      }
      if(!mth.descriptor.returnType.isVoidType){
        val ret = stack.popOrAbort()
        stack.pushN(remainingOperands)
        stack.push(ret)
      }
      else{
        stack.pushN(remainingOperands)
      }

    }

  def evalNativeStatic(mth: Method, args: Seq[V]) =
    mth.name match
      case "makeConcatWithConstants" =>
        val baseString = arrayOps.getArray(objectOps.getField(args(0), "value").get).map(vals => vals.get)
        val constantString = arrayOps.getArray(objectOps.getField(args(1), "value").get).map(vals => vals.get)
        val concattedString = (baseString ++ constantString).zipWithIndex
        val stringArray = arrayOps.makeArray(arrayAlloc(AllocationSite.array()),
          concattedString.map(vals => (vals._1, AllocationSite.arrayVals(vals._2))), ArrayType(ObjectType("String")))
        val stringObj = createLibraryObj(ObjectType("java/lang/String"))
        objectOps.setField(stringObj, "value", stringArray)
        stack.push(stringObj)
      case _ =>
        native.evalNativeStatic(mth, args)

  def invokeExternal(mth: Method, isStatic: Boolean) = external {
    invoke(mth, isStatic)
  }
  def evalExternal(inst: Instruction) = external {
    eval(inst)
  }
  inline def evalFix(inst: Instruction, pc: Int)(using rec: Fixed): FixOut =
    rec(FixIn.Eval(inst, pc))

  private def fixed: Fixed = fixpointSuper{
    case FixIn.Eval(inst, pc) =>
      eval(inst, pc)
      FixOut.Eval()
  }
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)
  def run(pc: Int, instructionMap: Map[Int, Instruction], mth: Method)(using Fixed): Unit =
    except.tryCatch {
      val currInst = instructionMap(pc)
      frame.setData(pc)
      evalFix(currInst, pc)
      if (currInst.nextInstructions(pc)(mth.body.get).nonEmpty) {
        val nextPC = currInst.indexOfNextInstruction(pc)(mth.body.get)
        run(nextPC, instructionMap, mth)
      }
    } {
      case JvmExcept.Jump(targetPC) =>
        run(targetPC, instructionMap, mth)
      case JvmExcept.Ret(currPC) =>
        //TODO transform V to Int
        //val currInst = instructionMap(currPC)
        //val nextPC = currInst.indexOfNextInstruction(currPC)(mth.body.get)
        //run(nextPC, instructionMap, mth)
      case JvmExcept.Throw(exception) =>
        println(exception)
        val currPC = frame.data
        val handler = mth.body.get.exceptionHandlersFor(currPC)
          .find(handlerException => exception.isSubtypeOf(handlerException.catchType.get)(project.classHierarchy))
          .getOrElse(except.throws(JvmExcept.Throw(exception)))
        val exceptionObject = createLibraryObj(exception)
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
    case _ => ???

  //val defaultObj = objectOps.makeObject(objAlloc(AllocationSite.classFile(objectCF)), objectCF, Seq())
  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => num.evalNumericOp(ICONST_0)
    case ValType.I64 => num.evalNumericOp(LCONST_0)
    case ValType.F32 => num.evalNumericOp(FCONST_0)
    case ValType.F64 => num.evalNumericOp(DCONST_0)
    case ValType.Obj => objectOps.makeNull()
    case ValType.Array => objectOps.makeNull()