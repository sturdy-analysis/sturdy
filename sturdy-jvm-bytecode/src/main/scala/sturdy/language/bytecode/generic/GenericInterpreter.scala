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
import sturdy.values.arrays.ArrayOps
import sturdy.values.arrays.Array
import sturdy.values.objects.{Object, ObjectOps, TypeOps}
import sturdy.values.relational.EqOps

import java.io.{DataInputStream, File, FileInputStream}
import java.net.URL
import scala.collection.immutable.ArraySeq


/*

1. alles was einfach ist
2. invoke static: how to manage operand stack
3. read up on exceptions in Sturdy
4. how do jumps/branching work on the JVM

  i1
  i2
l0:
  i3
  jump l3
  i5
l3:
  jump l0

val jumpTargets: Map[String, InstructionIndex]

 */

enum JvmExcept:
  case Jump(pc: Int)
  case Throw(exception: ObjectType)

enum AllocationSite:
  case classFile(cfs: ClassFile)
  case objField(cfs: ClassFile, field: String)
  case array()
  case arrayVals(idx: Int)
  case default

trait GenericInterpreter[V, Addr, Idx, OID, AID, ObjType, ObjRep, TypeRep, J[_] <: MayJoin[_]]:

  val bytecodeOps: BytecodeOps[Addr, Idx, V, ReferenceType]
  import bytecodeOps.*
  val objectOps: ObjectOps[Addr, Int, String, OID, V, ClassFile, Object[OID, ClassFile, Addr, String], V, AllocationSite, Method, String, MethodDescriptor, V, J]
  val arrayOps: ArrayOps[Addr, AID, V, V, Array[AID, Addr, ArrayType], V, ArrayType, AllocationSite, J]

  implicit val joinUnit: J[Unit]
  implicit val jvV: J[V]

  val stack: DecidableOperandStack[V]
  val failure: Failure
  val except: Except[JvmExcept, JvmExcept, J]
  val alloc: Allocation[Addr, AllocationSite]
  val objAlloc: Allocation[OID, AllocationSite]
  val arrayValAlloc: Allocation[Addr, AllocationSite]
  val arrayAlloc: Allocation[AID, AllocationSite]
  val store: Store[Addr, V, J]
  val arrayValStore: Store[Addr, V, J]
  val staticVarStore: Store[(ObjectType, String), V, J]

  type FrameData = Int
  val frame: DecidableMutableCallFrame[FrameData, Int, V]
  val project: Project[URL]
  val projectSource: String

  val nativeSource = org.opalj.bytecode.RTJar
  val objectCF = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/Object.class").head
  val stringCF = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/String.class").head

  var staticInitialized: Set[ObjectType] = Set()

  def nativeClassFileWrapper(obj: ObjectType): String =
    val source = "classes/" ++ obj.packageName ++ "/" ++ obj.simpleName ++ ".class"
    source

  def nonNativeClassFileWrapper(obj: ObjectType): String =
    val path = projectSource ++ "\\" ++ obj.simpleName ++ ".class"
    path

  private given Failure = failure
  private def fail(k: FailureKind, what: String) = failure.fail(k, s"$what")

  lazy val num = new GenericInterpreterNumerics[Addr, Idx, V, ReferenceType](bytecodeOps)

  def eval(inst: Instruction, pc: Int = 0): Unit = inst.opcode match
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
          ???
        case inst: LoadString =>
          val string = inst.value.toCharArray.map(l => l.toInt).toSeq
          val convString = string.map(l => i32ops.integerLit(l)).zipWithIndex
          val stringArray = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), convString.map(vals => (vals._1, AllocationSite.arrayVals(vals._2))), ArrayType(ObjectType("String")))
          val stringObj = create_native_obj(ObjectType("java/lang/String"))
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
          ???
        case inst: LoadMethodHandle_W =>
          ???
        case inst: LoadMethodType_W =>
          ???

    // LDC2_W
    case x if (x == 20) =>
      stack.push(num.evalNumericOp(inst))

    // load Local variable
    case x if (21 <= x && x <= 45) =>
      stack.push(eval_local_load(inst))

    //load from array
    case x if (46 <= x && x <= 53) =>
      val idx = stack.popOrAbort()
      val array = stack.popOrAbort()
      stack.push(eval_array_load(inst, array, idx, pc))

    // store local variable
    case x if (54 <= x && x <= 78) =>
      val v1 = stack.popOrAbort()
      eval_local_store(inst, v1)

    // store in array
    case x if (79 <= x && x <= 86) =>
      val v = stack.popOrAbort()
      val idx = stack.popOrAbort()
      val array = stack.popOrAbort()
      eval_array_store(inst, array, idx, v, pc)

    // Manip stack
    case x if (87 <= x && x <= 95) =>
      inst match
        case inst: POP.type =>
          stack.popOrAbort()
        case inst: POP2.type =>
          stack.pop2OrAbort()
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
          ???
        case inst: DUP2.type =>
          val dup1 = stack.popOrAbort()
          val dup2 = stack.popOrAbort()
          stack.push(dup2)
          stack.push(dup1)
          stack.push(dup2)
          stack.push(dup1)
        case inst: DUP2_X1.type =>
          ???
        case inst: DUP2_X2.type =>
          ???
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
          ??? //something about return address
          except.throws(JvmExcept.Jump(pc + inst.branchoffset))
        case inst: RET =>
          ???
        case inst: TABLESWITCH =>
          val index = stack.popOrAbort()
          val transformedOffsets = Iterator.from(0).zip(inst.jumpOffsets).toSeq.map(pairs => (i32ops.integerLit(pairs._1), pairs._2)).toMap
          val offset = transformedOffsets.get(index).getOrElse(inst.defaultOffset)
          except.throws(JvmExcept.Jump(pc + offset))
          /*
          val lowAsV = i32ops.integerLit(inst.low)
          val highAsV = i32ops.integerLit(inst.high)
          val ge = compareOps.ge(index, lowAsV)
          val le = compareOps.le(index, highAsV)
          branchOpsUnit.boolBranch(eqOps.equ(ge, le)){
            except.throws(JvmExcept.Jump(pc + transformedOffsets(index)))
          }{
            except.throws(JvmExcept.Jump(pc + inst.defaultOffset))
          }*/
        case inst: LOOKUPSWITCH =>
          val key = stack.popOrAbort()
          val transformedOffsets = inst.npairs.map(pairs => (i32ops.integerLit(pairs.key), pairs.value)).toMap
          val offset = transformedOffsets.get(key).getOrElse(inst.defaultOffset)
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
              val source = nativeClassFileWrapper(objCF)
              val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
              invokeStatic(cfs.staticInitializer.get)
            }
            else{
              staticInitialized += objCF
              val source = nonNativeClassFileWrapper(objCF)
              val cfs: List[ClassFile] =
                process(new DataInputStream(new FileInputStream(source))) { in =>
                  org.opalj.br.reader.Java8Framework.ClassFile(in)
                }
              invokeStatic(cfs.head.staticInitializer.get)
            }
          }

          val v = staticVarStore.readOrElse((objCF, inst.name), fail(UnboundStaticVar, inst.name))
          stack.push(v)

        case inst: PUTSTATIC =>
          val objCF = inst.declaringClass
          if (!staticInitialized.contains(objCF)) {
            if (project.isLibraryType(objCF)) {
              staticInitialized += objCF
              val source = nativeClassFileWrapper(objCF)
              val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
              invokeStatic(cfs.staticInitializer.get)
            }
            else {
              staticInitialized += objCF
              val source = nonNativeClassFileWrapper(objCF)
              println(projectSource)
              println(source)
              val cfs: List[ClassFile] =
                process(new DataInputStream(new FileInputStream(source))) { in =>
                  org.opalj.br.reader.Java8Framework.ClassFile(in)
                }
              invokeStatic(cfs.head.staticInitializer.get)
            }
          }
          val v = stack.popOrAbort()
          staticVarStore.write((objCF, inst.name), v)

    // Load and Store Fields
    case x if (180 <= x && x <= 181) =>
      inst match
        case inst: GETFIELD =>
          val obj = stack.popOrAbort()
          val objCF = project.classFile(inst.declaringClass).get
          val field = objectOps.getField(obj, inst.name).getOrElse(fail(UnboundField, inst.name))
          stack.push(field)
        case inst: PUTFIELD =>
          val value = stack.popOrAbort()
          val obj = stack.popOrAbort()
          val objCF = project.classFile(inst.declaringClass).get
          objectOps.setField(obj, inst.name, value)


    // Invoke Functions
    case x if (182 <= x && x <= 186) =>
      inst match
        case inst: INVOKESTATIC =>
          if (project.isLibraryType(inst.declaringClass)){
            val source = nativeClassFileWrapper(inst.declaringClass)
            val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
            val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
            invokeStatic(mth)
          }
          else{
            val mth = project.classFile(inst.declaringClass).get.findMethod(inst.name, inst.methodDescriptor).get
            invokeStatic(mth)
          }

        case inst: INVOKEVIRTUAL =>
          val objectType = inst.declaringClass.mostPreciseObjectType
          if (project.isLibraryType(objectType))
            val numArgs = inst.methodDescriptor.parametersCount
            val args = stack.popNOrAbort(numArgs)
            val obj = stack.popOrAbort()
            stack.push(obj)
            val mth = objectOps.findFunction(obj, inst.name, inst.methodDescriptor)(findMethodOfObj)
            val ret = objectOps.invokeFunction(obj, mth, args)(invokeMethodOnObjectInline)
            if (!mth.descriptor.returnType.isVoidType){
              stack.push(ret.get)
            }

          else
            val numArgs = inst.methodDescriptor.parametersCount
            val args = stack.popNOrAbort(numArgs)
            val obj = stack.popOrAbort()
            stack.push(obj)
            val mth = objectOps.findFunction(obj, inst.name, inst.methodDescriptor)(findMethodOfObj)
            val ret = objectOps.invokeFunction(obj, mth, args)(invokeMethodOnObjectInline)
            if (!mth.descriptor.returnType.isVoidType) {
              stack.push(ret.get)
            }

        case inst: INVOKESPECIAL =>
          val objectType = inst.declaringClass.mostPreciseObjectType
          if (project.isLibraryType(objectType))
            val source = nativeClassFileWrapper(objectType)
            val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
            val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
            invokeMethodOnObject(mth)
          else
            val cfs = project.classFile(objectType).get
            val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
            invokeMethodOnObject(mth)

        case inst: INVOKEINTERFACE =>
          val numArgs = inst.methodDescriptor.parametersCount
          val args = stack.popNOrAbort(numArgs)
          val obj = stack.popOrAbort()
          val mth = objectOps.findFunction(obj, inst.name, inst.methodDescriptor)(findMethodOfObj)
          invokeMethodOnObject(mth, args, obj)

        case inst: INVOKEDYNAMIC =>
          val test = inst.bootstrapMethod
          val test1 = inst.name
          val test2 = inst.methodDescriptor
          val receiver = inst.bootstrapMethod.handle
          receiver match
            case receiver: InvokeStaticMethodHandle =>
              if (project.isLibraryType(receiver.receiverType.mostPreciseObjectType)) {
                val source = nativeClassFileWrapper(receiver.receiverType.mostPreciseObjectType)
                val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
                val mth = cfs.findMethod(receiver.name, receiver.methodDescriptor).get
                invokeStatic(mth)

              }
              else{
                ???
              }


    // NEW
    case x if (x == 187) =>
      inst match
        case inst: NEW =>
          if (project.isLibraryType(inst.objectType)){
            val obj = create_native_obj(inst.objectType)
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
      ???

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
      ???

    // monitorexit
    case x if (x == 195) =>
      ???

    // WIDE
    case x if (x == 196) =>
      ???

    // multianewarray
    case x if (x == 197) =>
      inst match
        case inst: MULTIANEWARRAY =>
          val dims = stack.popNOrAbort(inst.dimensions)

          stack.push(createNDArray(inst.dimensions, inst.arrayType, dims.reverse))
          /*
          if (inst.dimensions == 2){
            val testSeq = arrayOps.initArray(dims(0)).map(_ => createArray(dims(1), inst.arrayType.elementType))
            val testSeq2 = testSeq.zipWithIndex.map(vals => (vals._1, AllocationSite.arrayVals(vals._2)))
            val array = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), testSeq2)
            stack.push(array)
          }

          if (inst.dimensions == 3){
            val testSeq = arrayOps.initArray(dims(0)).map(_ => arrayOps.initArray(dims(1)).map(_ => createArray(dims(2), inst.arrayType.elementType)))
            val test2Seq = testSeq.map(arrays =>
              val test3Seq = arrays.zipWithIndex.map(vals => (vals._1, AllocationSite.arrayVals(vals._2)))
              arrayOps.makeArray(arrayAlloc(AllocationSite.array()), test3Seq)
            )

            val test2Seq2 = test2Seq.zipWithIndex.map(vals => (vals._1, AllocationSite.arrayVals(vals._2)))

            val array = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), test2Seq2)
            stack.push(array)
          }

          if (inst.dimensions == 4){
            val testSeq = arrayOps.initArray(dims(0)).map(_ => arrayOps.initArray(dims(1)).map(_ => arrayOps.initArray(dims(2)).map(_ => createArray(dims(3), inst.arrayType.elementType))))
            val test2Seq = testSeq.map(arrays =>
              val test3Seq = arrays.map(arrays2 =>
                val test4Seq = arrays2.zipWithIndex.map(vals => (vals._1, AllocationSite.arrayVals(vals._2)))
                arrayOps.makeArray(arrayAlloc(AllocationSite.array()), test4Seq)
                )
              val test3Seq2 = test3Seq.zipWithIndex.map(vals => (vals._1, AllocationSite.arrayVals(vals._2)))
              arrayOps.makeArray(arrayAlloc(AllocationSite.array()), test3Seq2)
              )
            val test2Seq2 = test2Seq.zipWithIndex.map(vals => (vals._1, AllocationSite.arrayVals(vals._2)))
            val array = arrayOps.makeArray(arrayAlloc(AllocationSite.array()), test2Seq2)
            stack.push(array)
          }*/


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
      ???

    // jsr_wt
    case x if (x == 201) =>
      ???

    // breakpoint
    case x if (x == 202) =>
      ???

  def create_native_obj(toLoad: ObjectType): V =
    val source = nativeClassFileWrapper(toLoad)
    val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
    val inheritedFields = project.classHierarchy.allSuperclassesIterator(toLoad, true)(project).map(cfs => cfs.fields).toSeq.distinct
    val fields = inheritedFields.flatMap(fields => fields.map(field => (defaultValue(convertTypes(field.fieldType)), AllocationSite.objField(cfs, field.name), field.name)))
    val obj = objectOps.makeObject(objAlloc(AllocationSite.classFile(cfs)), cfs, fields)
    obj
  def eval_local_load(inst: Instruction): V = inst match
    case inst: LoadLocalVariableInstruction =>
      frame.getLocalOrElse(inst.lvIndex, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

  def eval_local_store(inst: Instruction, v: V): Unit = inst match
    case inst: StoreLocalVariableInstruction =>
      frame.setLocalOrElse(inst.lvIndex, v, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

  def eval_array_load(inst: Instruction, array: V, idx: V, pc: Int): V = inst match
    case inst: ArrayLoadInstruction =>
      //val idx = stack.popOrAbort()
      //val array = stack.popOrAbort()
      arrayOps.getVal(array, idx).getOrElse(except.throws(JvmExcept.Throw(ObjectType("java/lang/IndexOutOfBoundsException"))))

  def eval_array_store(inst: Instruction, array: V, idx: V, v: V, pc: Int): Unit = inst match
    case inst: ArrayStoreInstruction =>
      //val toBeStored = stack.popOrAbort()
      //val idx = stack.popOrAbort()
      //val array = stack.popOrAbort()
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
    if (project.isLibraryType(obj.cls.thisType)) {
      val nextInherit = project.classHierarchy.supertypeInformation(obj.cls.thisType).get.classTypes.last
      obj.cls.findMethod(name, sig)
        .getOrElse(findInheritedMethodOfObj(obj, name, sig, nextInherit))
    }
    else {
      val nextInherit = project.classHierarchy.supertypeInformation(obj.cls.thisType).get.classTypes.last
      obj.cls.findMethod(name, sig)
        .getOrElse(findInheritedMethodOfObj(obj, name, sig, nextInherit))
    }

  def findInheritedMethodOfObj(obj: Object[OID, ClassFile, Addr, String], name: String, sig: MethodDescriptor, inheritedObj: ObjectType): Method =
    if(inheritedObj == ObjectType("java/lang/Object")){
      obj.cls.interfaceTypes.map(interfaces => project.classFile(interfaces)).map(file => file.get.findMethod(name, sig)).head
        .getOrElse(fail(MethodNotFound, s"Method $sig, $name not found"))
    }
    else{
      if (project.isLibraryType(inheritedObj)) {
        val source = nativeClassFileWrapper(inheritedObj)
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

  def checkTypeObj(obj: Object[OID, ClassFile, Addr, String], check: ReferenceType): Boolean =
    obj.cls.thisType.isSubtypeOf(check.mostPreciseObjectType)(project.classHierarchy)

  def checkTypeArray(array: Array[AID, Addr, ArrayType], check: ArrayType): Boolean =
    array.arrayType == check
  def invokeMethodOnObjectInline(obj: Object[OID, ClassFile, Addr, String], mth: Method, args: Seq[V]): JOptionC[V] =
    val newFrameData = 0

    var locals: ArraySeq[ValType] = ArraySeq()
    if (!mth.body.get.localVariableTable.isEmpty) {
      val localstemp = mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
      locals = localstemp
    }
    else {
      val localstemp = ArraySeq.fill(mth.body.get.maxLocals-1)(0).map(_ => ValType.I32)
      locals = localstemp
    }

    val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap

    val objVal = stack.popOrAbort()
    val thisAndArgs = List(objVal) ++ args
    val argsAndLocals = thisAndArgs.view ++ locals.map(defaultValue)

    val startingPC = mth.body.get.iterator.next().pc

    var currInst = instructionMap.get(startingPC)

    stack.withNewFrame(0) {
      frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map(_.swap)) {
        runBlock(0, instructionMap, mth)
      }
    }
    val ret = stack.pop()
    ret

  def invokeMethodOnObject(mth: Method) =
    val newFrameData = 0

    var locals: ArraySeq[ValType] = ArraySeq()
    if (!mth.body.get.localVariableTable.isEmpty) {
      val localstemp = mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
      locals = localstemp
    }
    else {
      val localstemp = ArraySeq.fill(mth.body.get.maxLocals-1)(0).map(_ => ValType.I32)
      locals = localstemp
    }

    val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap

    val numArgs = mth.descriptor.parametersCount
    val args = stack.popNOrAbort(numArgs)
    val obj = stack.popOrAbort()
    val thisAndArgs = List(obj) ++ args
    val argsAndLocals = thisAndArgs.view ++ locals.map(defaultValue)

    val startingPC = mth.body.get.iterator.next().pc

    var currInst = instructionMap.get(startingPC)

    stack.withNewFrame(0) {
      frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map(_.swap)) {
        runBlock(0, instructionMap, mth)
      }
    }

  def invokeMethodOnObject(mth: Method, args: List[V], obj: V) =
    val newFrameData = 0

    var locals: ArraySeq[ValType] = ArraySeq()
    if (!mth.body.get.localVariableTable.isEmpty) {
      val localstemp = mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
      locals = localstemp
    }
    else{
      val localstemp = ArraySeq.fill(mth.body.get.maxLocals-1)(0).map(_ => ValType.I32)
      locals = localstemp
    }

    val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap

    //val numArgs = mth.descriptor.parametersCount
    //val args = stack.popNOrAbort(numArgs)
    //val obj = stack.popOrAbort()
    val thisAndArgs = List(obj) ++ args
    val argsAndLocals = thisAndArgs.view ++ locals.map(defaultValue)

    val startingPC = mth.body.get.iterator.next().pc

    var currInst = instructionMap.get(startingPC)

    stack.withNewFrame(0) {
      frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map(_.swap)) {
        runBlock(0, instructionMap, mth)
      }
    }


  def invokeStatic(mth: Method) =
    val newFrameData = 0

    var locals: ArraySeq[ValType] = ArraySeq()
    if (!mth.body.get.localVariableTable.isEmpty) {
      val localstemp = mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
      locals = localstemp
    }
    else {
      val localstemp = ArraySeq.fill(mth.body.get.maxLocals)(0).map(_ => ValType.I32)
      locals = localstemp
    }

    val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap

    val numArgs = mth.descriptor.parametersCount
    val args = stack.popNOrAbort(numArgs)
    val argsAndLocals = args.view ++ locals.map(defaultValue)

    val startingPC = mth.body.get.iterator.next().pc

    var currInst = instructionMap.get(startingPC)

    stack.withNewFrame(0){
      frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map(_.swap)){
        runBlock(0, instructionMap, mth)
      }
    }

  def initStaticVars(mth: Method) =
    val newFrameData = 0

    val instructionMap = mth.body.get.iterator.map(c => c.pc -> c.instruction).toMap

    val numArgs = mth.descriptor.parametersCount
    val args = stack.popNOrAbort(numArgs)
    val argsAndLocals = args.view

    val startingPC = mth.body.get.iterator.next().pc

    var currInst = instructionMap.get(startingPC)

    stack.withNewFrame(0) {
      frame.withNew(newFrameData, argsAndLocals.view.zipWithIndex.map(_.swap)) {
        runBlock(0, instructionMap, mth)
      }
    }

  def runBlock(pc: Int, instructionMap: Map[Int, Instruction], mth: Method): Unit =
    except.tryCatch {
      var currPC = pc
      var currInst = instructionMap(currPC)
      eval(currInst, currPC)
      while(currInst.nextInstructions(pc)(mth.body.get).nonEmpty){
        currPC = currInst.indexOfNextInstruction(currPC)(mth.body.get)
        frame.setData(currPC)
        currInst = instructionMap(currPC)
          eval(currInst, currPC)
      }
    } {
      case JvmExcept.Jump(targetPC) =>
        runBlock(targetPC, instructionMap, mth)
      case JvmExcept.Throw(exception) =>
        println(exception)
        val currPC = frame.data
        val handler = mth.body.get.exceptionHandlersFor(currPC)
          .find(handlerException => exception.isSubtypeOf(handlerException.catchType.get)(project.classHierarchy))
          .getOrElse(except.throws(JvmExcept.Throw(exception)))
        val exceptionObject = create_native_obj(exception)
        stack.push(exceptionObject)
        runBlock(handler.handlerPC, instructionMap, mth)
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
    case ValType.Obj => objectOps.makeObject(objAlloc(AllocationSite.classFile(objectCF)), objectCF, Seq())
    case ValType.Array => arrayOps.makeArray(arrayAlloc(AllocationSite.default), Seq(), ArrayType.ArrayOfObject)