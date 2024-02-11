package sturdy.language.bytecode.generic

import org.opalj.br.instructions.*
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.data.MayJoin
import sturdy.data.noJoin
import sturdy.effect.callframe.{DecidableCallFrame, DecidableMutableCallFrame}
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.Store
import sturdy.effect.allocation.Allocation
import sturdy.values.booleans.BooleanBranching
import BytecodeFailure.*
import org.opalj.br.analyses.Project
import org.opalj.br.{ArrayType, BooleanType, ClassFile, DoubleType, FieldType, FloatType, IntegerType, LongType, Method, ObjectType}
import sturdy.values.objects.ObjectOps
import sturdy.values.relational.EqOps

import java.net.URL
import scala.util.control.Breaks.{break, breakable}


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

enum AllocationSite:
  case classFile(cfs: ClassFile)
  case objField(cfs: ClassFile)

trait GenericInterpreter[V, Addr, Idx, OID, ObjType, ObjRep, J[_] <: MayJoin[_]]:

  val bytecodeOps: BytecodeOps[Addr, Idx, V]
  import bytecodeOps.*
  val objectOps: ObjectOps[Addr, Int, OID, V, ClassFile, V, AllocationSite, Method, J]

  implicit val joinUnit: J[Unit]
  implicit val jvV: J[V]

  val stack: DecidableOperandStack[V]
  val failure: Failure
  val except: Except[JvmExcept, JvmExcept, J]
  val alloc: Allocation[Addr, AllocationSite]
  val objAlloc: Allocation[OID, AllocationSite]
  val store: Store[Addr, V, J]

  type FrameData = Unit
  val frame: DecidableMutableCallFrame[FrameData, Int, V]
  val project: Project[URL]

  val nativeSource = org.opalj.bytecode.RTJar
  val objectCF = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, "classes/java/lang/Object.class").head

  def nativeClassFileWrapper(obj: ObjectType): String =
    val source = "classes/" ++ obj.packageName ++ "/" ++ obj.simpleName ++ ".class"
    source

  private given Failure = failure
  private def fail(k: FailureKind, what: String) = failure.fail(k, s"$what")

  lazy val num = new GenericInterpreterNumerics[Addr, Idx, V](bytecodeOps)

  def eval(inst: Instruction, pc: Int = 0): Unit = inst.opcode match
    // No Op
    case x if (x == 0) =>
      ()

    // push NULL on stack
    case x if (x == 1) =>
      ???

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
          ???
        case inst: LoadMethodHandle =>
          ???
        case inst: LoadMethodType =>
          ???

    // LDC_W
    case x if (x == 19) =>
      inst match
        case inst: LoadInt_W =>
          ???
        case inst: LoadFloat_W =>
          ???
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
      ???

    // store local variable
    case x if (54 <= x && x <= 78) =>
      val v1 = stack.popOrAbort()
      eval_local_store(inst, v1)

    // store in array
    case x if (79 <= x && x <= 86) =>
      val v1 = stack.popOrAbort()
      ???

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
          frame.setLocalOrElse(inst.lvIndex, i32ops.add(toInc, i32ops.integerLit(1)), fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

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
          ???
        case inst: RET =>
          ???
        case inst: TABLESWITCH =>
          val index = stack.popOrAbort()
          val transformedOffsets = Iterator.from(0).zip(inst.jumpOffsets).toSeq.map(pairs => (i32ops.integerLit(pairs._1), pairs._2)).toMap
          val lowAsV = i32ops.integerLit(inst.low)
          val highAsV = i32ops.integerLit(inst.high)
          val ge = compareOps.ge(index, lowAsV)
          val le = compareOps.le(index, highAsV)
          branchOpsUnit.boolBranch(eqOps.equ(ge, le)){
            except.throws(JvmExcept.Jump(pc + transformedOffsets(index)))
          }{
            except.throws(JvmExcept.Jump(pc + inst.defaultOffset))
          }
        case inst: LOOKUPSWITCH =>
          val key = stack.popOrAbort()
          val transformedMap = inst.npairs.map(pairs => (i32ops.integerLit(pairs.key), pairs.value))
          var offset = 0
          breakable {
            for (pair <- transformedMap) {
              branchOpsUnit.boolBranch(eqOps.equ(key, pair._1)) {
                offset = pair._2
                break
              } {
                offset = inst.defaultOffset
              }
            }
          }
          except.throws(JvmExcept.Jump(pc + offset))


    // Return
    case x if (172 <= x && x <= 177) =>
      ()

    // Load and Store Statics
    case x if (178 <= x && x <= 179) =>
      ???

    // Load and Store Fields
    case x if (180 <= x && x <= 181) =>
      inst match
        case inst: GETFIELD =>
          val obj = stack.popOrAbort()
          val objCF = project.classFile(inst.declaringClass).get
          val fieldIndex = objCF.fields.map(_.name).indexOf(inst.name)
          val field = objectOps.getField(obj, fieldIndex).getOrElse(fail(UnboundField, inst.name))
          stack.push(field)
        case inst: PUTFIELD =>
          val value = stack.popOrAbort()
          val obj = stack.popOrAbort()
          val objCF = project.classFile(inst.declaringClass).get
          val fieldIndex = objCF.fields.map(_.name).indexOf(inst.name)
          objectOps.setField(obj, fieldIndex, value)


    // Invoke Functions
    case x if (182 <= x && x <= 186) =>
      inst match
        case inst: INVOKESTATIC =>
          // Overloaded Functions!
          //val mth = cfs.findMethod(inst.name, inst.methodDescriptor).get
          val mth = project.classFile(inst.declaringClass).get.findMethod(inst.name, inst.methodDescriptor).get
          invokeStatic(mth)

        case inst: INVOKEVIRTUAL =>
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

        case _ =>
          val newFrameData = ()
          val args: List[V] = ???
          frame.withNew(newFrameData, args.zipWithIndex.map(_.swap)) {

          }
          ???


    // NEW
    case x if (x == 187) =>
      inst match
        case inst: NEW =>
          val cfs = project.classFile(inst.objectType).get
          val fields = cfs.fields.map(field => (defaultValue(convertTypes(field.fieldType)), AllocationSite.objField(cfs)))
          val obj = objectOps.makeObject(objAlloc(AllocationSite.classFile(cfs)), cfs, fields)
          stack.push(obj)


    // Arrays
    case x if (188 <= x && x <= 190) =>
      ???

    // athrow
    case x if (x == 191) =>
      ???

    // checkcast
    case x if (x == 192) =>
      ???

    // instanceof
    case x if (x == 193) =>
      ???

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
      ???

    // ifnull, ifnonnull
    case x if (198 <= x && x <= 199) =>
      ???

    // goto_w
    case x if (x == 200) =>
      ???

    // jsr_wt
    case x if (x == 201) =>
      ???

    // breakpoint
    case x if (x == 202) =>
      ???

  def eval_local_load(inst: Instruction): V = inst match
    case inst: LoadLocalVariableInstruction =>
      frame.getLocalOrElse(inst.lvIndex, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

  def eval_local_store(inst: Instruction, v: V): Unit = inst match
    case inst: StoreLocalVariableInstruction =>
      frame.setLocalOrElse(inst.lvIndex, v, fail(UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))

  def eval_array_load(inst: Instruction): V = inst match
    case inst: IALOAD.type =>
      ???

  def eval_array_store(inst: Instruction, v: V): Unit = inst match
    case inst: IASTORE.type =>
      ???


  def invokeMethodOnObject(mth: Method) =
    val newFrameData = ()
    val locals = mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
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

  def invokeStatic(locals: List[ValType], instructionList: List[Instruction], args: List[V]) =
      //val cls = ???
      //val mth = ???
      //val params = ???
      val newFrameData = ()
      val localVars = locals.map(defaultValue)

      stack.withNewFrame(0) {
        frame.withNew(newFrameData, localVars.view.zipWithIndex.map(_.swap)) {
          for (inst <- instructionList) {
            eval(inst)
          }
        }
      }
  def invokeStatic(mth: Method) =
    val newFrameData = ()
    val locals = mth.body.get.localVariableTable.get.map(_.fieldType).map(convertTypes(_))
    //println(mth.name)
    //println(locals)
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

  def runBlock(pc: Int, instructionMap: Map[Int, Instruction], mth: Method): Unit =
    except.tryCatch {
      var currPC = pc
      var currInst = instructionMap(currPC)
      //println(currInst.toString ++ " " ++ currPC.toString)
      eval(currInst, currPC)
      while(currInst.nextInstructions(pc)(mth.body.get).nonEmpty){
        currPC = currInst.indexOfNextInstruction(currPC)(mth.body.get)
        currInst = instructionMap(currPC)
        //println(currInst.toString ++ " " ++ currPC.toString)
        eval(currInst, currPC)
      }

    } {
      case JvmExcept.Jump(targetPC) =>
        runBlock(targetPC, instructionMap, mth)
    }


  def convertTypes(opalTypes: FieldType): ValType = opalTypes match
    case opalTypes: IntegerType => ValType.I32
    case opalTypes: FloatType => ValType.F32
    case opalTypes: LongType => ValType.I64
    case opalTypes: DoubleType => ValType.F64
    case opalTypes: BooleanType => ValType.I32
    case opalTypes: ObjectType => ValType.Obj
    case opalTypes: ArrayType => ???
    case _ => ???

  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => num.evalNumericOp(ICONST_0)
    case ValType.I64 => num.evalNumericOp(LCONST_0)
    case ValType.F32 => num.evalNumericOp(FCONST_0)
    case ValType.F64 => num.evalNumericOp(DCONST_0)
    case ValType.Obj => objectOps.makeObject(objAlloc(AllocationSite.classFile(objectCF)), objectCF, Seq())