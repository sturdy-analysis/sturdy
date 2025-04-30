package sturdy.language.wasm.generic

import sturdy.data.{CombineUnit, JOptionC, MayJoin, noJoin}
import sturdy.effect.bytememory.Memory
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.symboltable.{DecidableSymbolTable, SymbolTable}
import sturdy.effect.{EffectList, EffectStack}
import sturdy.language.wasm.generic.WasmFailure.*
import sturdy.values.Finite
import sturdy.values.booleans.BooleanBranching
import sturdy.values.convert.*
import sturdy.{IsSound, Soundness, fix}
import swam.syntax.*
import swam.*

import scala.collection.immutable.VectorBuilder

case class FrameData(returnArity: Int, module: ModuleInstance):
  override def toString: String =
    if (module == null)
      s"null:$returnArity"
    else
      s"$module:$returnArity"

given FiniteFrameData: Finite[FrameData] with {}

given frameDataIsSound: Soundness[FrameData, FrameData] with
  override def isSound(c: FrameData, a: FrameData): IsSound =
    if (c.returnArity != a.returnArity)
      return IsSound.NotSound(s"Return arities do not match: $c $a.")
    if (c.module == null && a.module == null)
      return IsSound.Sound
    if (c.module != null && a.module != null)
      return IsSound.NotSound(s"Concrete module ${c.module} not approximated by ${a.module}")
    summon[Soundness[ModuleInstance, ModuleInstance]].isSound(c.module, a.module)

object FrameData:
  val empty: FrameData = FrameData(0, null)

enum JumpTarget:
  case Jump(labelIndex: LabelIdx)
  case Return

given Finite[JumpTarget] with {}

case class WasmException[V](target: JumpTarget, operands: List[V])

enum WasmReference:
  case Func(fun: FunctionInstance)
  case Extern(any: Any)

type Imports = Map[String, ModuleInstance]

case class FuncId(mod: ModuleInstance, funcIx: Int):
  override def toString: String = s"$mod.$funcIx"

enum InstLoc:
  case InFunction(func: FuncId, pc: Int)
  case InInit(mod: ModuleInstance, pc: Int)
  case InvokeExported(mod: ModuleInstance, funName: String)

  override def hashCode(): Int = this match
    case InFunction(FuncId(mod, funcIx), pc) => mod.hashCode() + 7 * funcIx + 3 * pc
    case InInit(mod, pc) => mod.hashCode() + 101 * pc
    case InvokeExported(mod, funName) => mod.hashCode() + 331 * funName.hashCode

  override def toString: String = this match
    case InFunction(func, pc) => s"$func:$pc"
    case InInit(mod, pc) => s"$mod.INIT:$pc"
    case InvokeExported(mod, funName) => s"$mod.$funName"

  def +(i: Int): InstLoc = this match
    case InFunction(func, pc) => InFunction(func, pc + i)
    case InInit(mod, pc) => InInit(mod, pc + i)
    case InvokeExported(mod, funName) => throw new IllegalStateException

  def -(that: InstLoc): Int = (this, that) match
    case (InFunction(func1, pc1), InFunction(func2, pc2)) if func1 == func2 => pc2 - pc1
    case (InInit(mod1, pc1), InInit(mod2, pc2)) if mod1 == mod2 => pc2 - pc1
    case _ => throw new MatchError((this, that))

enum FixIn:
  case Eval(inst: Inst, loc: InstLoc)
  case EnterWasmFunction(id: FuncId, func: Func, ft: FuncType)
  case EnterHostFunction(id: FuncId, hostFfunc: HostFunction)
  case MostGeneralClientLoop(modInst: ModuleInstance)

  override def toString: String = this match
    case Eval(i, loc) => i match
      case Block(_, _) => s"Block @$loc"
      case Loop(_, _) => s"Loop @$loc"
      case If(_, _, _) => s"If @$loc"
      case _ => s"$i @$loc"
    case EnterWasmFunction(id, _, _) => s"Enter $id"
    case MostGeneralClientLoop(modInst) => s"Most general client for $modInst"

enum FixOut[V]:
  case Eval()
  case ExitWasmFunction(vals: List[V])
  case ExitHostFunction(vals: List[V])
  case MostGeneralClient()

given finiteFixIn: Finite[FixIn] with {}

trait GenericInterpreter[V, Addr, Bytes, Size, ExcV, Index, FunV, J[_] <: MayJoin[_]]:

  // fixpoint
  val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[V]]
  type Fixed = FixIn => FixOut[V]

  // joins
  implicit def jvUnit: J[Unit]

  implicit def jvV: J[V]

  // implicit def jvFunV: J[V]
  implicit def jvFunV: J[FunV]

  // value components
  val wasmOps: WasmOps[V, Addr, Bytes, Size, ExcV, Index, FunV, J]

  import wasmOps.*
  import specialOps.*

  // effect components
  val stack: DecidableOperandStack[V]
  val memory: Memory[MemoryAddr, Addr, Bytes, Size, J]
  val globals: DecidableSymbolTable[Unit, GlobalAddr, V]
  val tables: SymbolTable[TableAddr, Index, V, J]
  val callFrame: DecidableMutableCallFrame[FrameData, Int, V, InstLoc]
  val except: Except[WasmException[V], ExcV, J]
  val failure: Failure

  // table limits and types
  var tableLimits: List[(Int, Option[Int])] = List.empty
  var tableTypes: List[ReferenceType] = List.empty

  import except.*

  // effect stack
  val effectStack: EffectStack = new EffectStack(EffectList(stack, memory, globals, tables, callFrame, except, failure), {
    case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => EffectList(memory, globals, callFrame)
    case _: FixIn.Eval => EffectList(stack, memory, globals, callFrame)
  }, {
    case _: FixIn.EnterWasmFunction | _: FixIn.MostGeneralClientLoop => EffectList(stack, memory, globals, failure)
    case _: FixIn.Eval => EffectList(stack, memory, globals, callFrame, except)
  })

  given EffectStack = effectStack

  private given Failure = failure

  lazy val num = new GenericInterpreterNumerics[V, J](stack, wasmOps)

  private val labelStack = new LabelStack
  private var memCount = 0
  private var tabCount = 0
  private var globCount = 0

  private def fail(k: FailureKind, what: String) = failure.fail(k, s"$what in $module")

  def module: ModuleInstance = callFrame.data.module

  def evalVarInst(inst: VarInst): Unit = inst match
    case LocalGet(ix) =>
      val v = callFrame.getLocalOrElse(ix, fail(UnboundLocal, ix.toString))
      stack.push(v)
    case LocalSet(ix) =>
      val v = stack.popOrAbort()
      callFrame.setLocalOrElse(ix, v, fail(UnboundLocal, ix.toString))
    case LocalTee(ix) =>
      val v = stack.peekOrAbort()
      callFrame.setLocalOrElse(ix, v, fail(UnboundLocal, ix.toString))
    case GlobalGet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val global = getGlobalValue(globalIdx)
      stack.push(global)
    case GlobalSet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val v = stack.popOrAbort()
      val _ = getGlobalValue(globalIdx)
      writeGlobalValue(globalIdx, v)

  def validateTableAccess(tableIdx: TableIdx, elemIdx: V): Unit = {
    val t = module.tableAddrs.lift(tableIdx).getOrElse(fail(TableAccessOutOfBounds, tableIdx.toString))
    val tabSz = tables.size(t)
    val e = valToInt(elemIdx)
    if (e < 0 | e >= tabSz) {
      fail(TableAccessOutOfBounds, "Element Index out of bounds")
    }
  }

  def growtable(t: Int, n: V, ref: V): Boolean = {
    var success = true
    val prevSize = tables.size(TableAddr(t))
    val len = valToInt(n) + prevSize
    if (len >= math.pow(2, 32) | valToInt(n) < 0) {
      success = false
    } else {
      var i = prevSize
      tableLimits(t)._2 match {
        case Some(maxLimit) => if (len > maxLimit) {
          success = false
        } else {
          while (i < len) {
            tables.set(TableAddr(t), valToIdx(num.evalNumeric(i32.Const(i))), ref)
            i += 1
          }
        }
        case _ =>
          while (i < len) {
            tables.set(TableAddr(t), valToIdx(num.evalNumeric(i32.Const(i))), ref)
            i += 1
          }
      }
    }
    success
  }

  def getElemLeft(in: Seq[FuncIdx], s: V): V =
    numToRef(num.evalNumeric(i32.Const(in(valToInt(s)))))

  def getElemRight(in: Seq[Inst], s: V): V =
    instToVal(in(valToInt(s)))

  def evalTableInst(inst: Inst): Unit =
    inst match {
      case TableGet(ix) =>
        val elemIdx = stack.popOrAbort()
        validateTableAccess(ix, elemIdx)
        val tpe = tableTypes(ix)
        val ref = tables.getOrElse(TableAddr(ix), valToIdx(elemIdx), makeNullRef(tpe))
        stack.push(ref)
      case TableSet(ix) =>
        val v = stack.popOrAbort()
        val elemIdx = stack.popOrAbort()
        validateTableAccess(ix, elemIdx)
        tables.set(TableAddr(ix), valToIdx(elemIdx), v)
      case TableSize(ix) =>
        val sz = num.evalNumeric(i32.Const(tables.size(TableAddr(ix))))
        stack.push(sz)
      case TableGrow(ix) =>
        val n = stack.popOrAbort()
        val ref = stack.popOrAbort()
        val sz = tables.size(TableAddr(ix))
        val err = -1
        if (growtable(ix, n, ref)) {
          stack.push(num.evalNumeric(i32.Const(sz)))
        } else {
          stack.push(num.evalNumeric(i32.Const(err)))
        }
      case TableFill(ix) =>
        val n = stack.popOrAbort()
        val fillEntry = stack.popOrAbort()
        val i = stack.popOrAbort()
        stack.push(n)
        stack.push(num.evalNumeric(i32.Const(1)))
        stack.push(num.evalNumeric(i32.Sub))
        stack.push(i)
        validateTableAccess(ix, num.evalNumeric(i32.Add))
        if (valToInt(n) == 0) return
        stack.push(i)
        stack.push(fillEntry)
        evalTableInst(TableSet(ix))
        stack.push(i)
        stack.push(num.evalNumeric(i32.Const(1)))
        stack.push(num.evalNumeric(i32.Add))
        stack.push(fillEntry)
        stack.push(n)
        stack.push(num.evalNumeric(i32.Const(1)))
        stack.push(num.evalNumeric(i32.Sub))
        evalTableInst(TableFill(ix))
      case TableCopy(x, y) =>
        val n = stack.popOrAbort()
        val s = stack.popOrAbort()
        val d = stack.popOrAbort()
        stack.push(n)
        stack.push(num.evalNumeric(i32.Const(1)))
        stack.push(num.evalNumeric(i32.Sub))
        stack.push(d)
        validateTableAccess(x, num.evalNumeric(i32.Add))
        stack.push(n)
        stack.push(num.evalNumeric(i32.Const(1)))
        stack.push(num.evalNumeric(i32.Sub))
        stack.push(s)
        validateTableAccess(y, num.evalNumeric(i32.Add))
        if (valToInt(n) == 0) {
          return
        }
        if (valToInt(d) <= valToInt(s)) {
          if (valToInt(d) + 1 >= math.pow(2, 32) || valToInt(s) + 1 >= math.pow(2, 32)) {
            return
          }
          stack.push(d)
          stack.push(s)
          evalTableInst(TableGet(y))
          evalTableInst(TableSet(x))
          stack.push(d)
          stack.push(num.evalNumeric(i32.Const(1)))
          stack.push(num.evalNumeric(i32.Add))
          stack.push(s)
          stack.push(num.evalNumeric(i32.Const(1)))
          stack.push(num.evalNumeric(i32.Add))
        } else {
          if (valToInt(d) + valToInt(n) - 1 >= math.pow(2, 32) || valToInt(s) + valToInt(n) - 1 >= math.pow(2, 32)) {
            return
          }
          stack.push(n)
          stack.push(num.evalNumeric(i32.Const(1)))
          stack.push(num.evalNumeric(i32.Sub))
          stack.push(d)
          stack.push(num.evalNumeric(i32.Add))
          stack.push(n)
          stack.push(num.evalNumeric(i32.Const(1)))
          stack.push(num.evalNumeric(i32.Sub))
          stack.push(s)
          stack.push(num.evalNumeric(i32.Add))
          evalTableInst(TableGet(y))
          evalTableInst(TableSet(x))
          stack.push(d)
          stack.push(s)
        }
        stack.push(n)
        stack.push(num.evalNumeric(i32.Const(1)))
        stack.push(num.evalNumeric(i32.Sub))
        evalTableInst(TableCopy(x, y))
      case TableInit(ix, el) =>
        val elem = module.elements.lift(el).getOrElse(fail(TableAccessOutOfBounds, el.toString))
        val n = stack.popOrAbort()
        val s = stack.popOrAbort()
        val d = stack.popOrAbort()
        validateTableAccess(ix, n)
        var elemLen = 0
        elem.init match {
          case Left(in) => elemLen = in.length
          case Right(in) => elemLen = in.length
        }
        if (valToInt(s) + valToInt(n) > elemLen) {
          fail(TableAccessOutOfBounds, "Index > Elem List")
        }
        stack.push(d)
        stack.push(n)
        validateTableAccess(ix, num.evalNumeric(i32.Add))
        stack.push(n)
        if (valToInt(n) == 0) {
          return
        }
        stack.push(d)
        elem.init match {
          case Left(in) => stack.push(getElemLeft(in, s))
          case Right(in) => stack.push(getElemRight(in, s))
            evalTableInst(TableSet(ix))
            stack.push(d)
            stack.push(num.evalNumeric(i32.Const(1)))
            stack.push(num.evalNumeric(i32.Add))
            stack.push(s)
            stack.push(num.evalNumeric(i32.Const(1)))
            stack.push(num.evalNumeric(i32.Add))
            stack.push(n)
            stack.push(num.evalNumeric(i32.Const(1)))
            stack.push(num.evalNumeric(i32.Sub))
            evalTableInst(TableInit(ix, el))
        }

      case ElemDrop(el) =>
        val elem = module.elements.lift(el).getOrElse(fail(TableAccessOutOfBounds, el.toString))
        val elemSz = module.elements.size
        var newElems = Vector.empty[Elem]
        var i = 0
        while (i < elemSz) {
          val currElem = module.elements.lift(i).getOrElse(fail(TableAccessOutOfBounds, el.toString))
          if (currElem == elem) {
            newElems = newElems.appended(Elem(ReferenceType.FuncRef, Left(Seq.empty), ElemMode.Passive(elem.reftype, elem.init)))
          } else {
            newElems = newElems.appended(currElem)
          }
          i += 1
        }
        module.elements = newElems
    }

  def evalRefInst(inst: Inst): Unit = inst match {
    case RefNull(t) =>
      stack.push(makeNullRef(t))
    case RefIsNull() =>
      val ref = stack.popOrAbort()
      stack.push(isNull(ref))
    case RefFunc(funcIdx) =>
      val func = module.functions.lift(funcIdx).getOrElse(fail(UnboundFunctionIndex, funcIdx.toString))
      stack.push(makeRef(func))
    case RefExtern(funcIdx) =>
      stack.push(makeExternRef(funcIdx))

  }

  def evalMemoryInst(inst: Inst): Unit = inst match
    case i: LoadInst => load(i)
    case i: LoadNInst => load(i)

    case i: StoreInst => store(i)
    case i: StoreNInst => store(i)

    case MemorySize =>
      val sz = memory.size(memoryIndex)
      stack.push(sizeToVal(sz))
    case MemoryGrow =>
      val delta = valToSize(stack.popOrAbort())
      val res = memory.grow(memoryIndex, delta).option
        (num.evalNumeric(i32.Const(0xFFFFFFFF))) // 0xFFFFFFFF ~= -1
        (sizeToVal)
      stack.push(res)
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def load(inst: LoadInst | LoadNInst): Unit =
    val addr = effectiveAddr(inst.offset)
    val memIdx = memoryIndex
    val length = getBytesToRead(inst)
    memory.read(memIdx, addr, length).orElseAndThen(fail(MemoryAccessOutOfBounds, s"Cannot read $length bytes at address $addr in current memory.")) {
      bytes =>
        val v = decode(bytes, SomeCC(inst, false))
        stack.push(v)
    }

  def store(inst: StoreInst | StoreNInst): Unit =
    val v = stack.popOrAbort()
    val bytes = encode(v, SomeCC(inst, false))

    // add offset to base address (which is already on the stack)
    stack.push(i32ops.integerLit(inst.offset))
    val addr = valToAddr(num.evalNumeric(i32.Add))

    val memIdx = memoryIndex
    memory.write(memIdx, addr, bytes).getOrElse(
      fail(MemoryAccessOutOfBounds, s"Cannot write $bytes at address $addr in current memory.")
    )

  def getBytesToRead(inst: MemoryInst): Int = inst match
    case Load(tpe, _, _) => tpe.width / 8
    case LoadN(_, n, _, _) => n / 8
    case _ => throw new IllegalArgumentException(s"Expected load instruction, but got $inst")

  def memoryIndex: MemoryAddr =
    module.memoryAddrs(0)

  def tableIndex(i: Int): TableAddr =
    module.tableAddrs(i)

  def globalTableIndex: TableAddr =
    TableAddr(0)

  def getGlobalValue(ga: GlobalAddr): V =
    globals.getOrElse((), ga, fail(UnboundGlobal, ga.toString))

  def writeGlobalValue(ga: GlobalAddr, v: V): Unit =
    globals.set((), ga, v)

  //def getTableValue(addr: TableAddr): V =
  //val x = funTable.getOrElse(addr,valueToFuncIx(intToVal(0)), fail(UnboundGlobal, addr.toString))
  //stack.pu

  def writeTableValue(addr: TableAddr, v: V): Unit = ???
  //funTable.set(addr, valueToFuncIx(intToVal(0)), v)

  def eval_open(inst: Inst, loc: InstLoc)(using Fixed): Unit =
    val opcode = inst.opcode
    if (opcode >= OpCode.I32Const && opcode <= OpCode.I64Extend32S)
      stack.push(num.evalNumeric(inst))
    else if (opcode >= OpCode.I32Load && opcode <= OpCode.MemoryGrow)
      evalMemoryInst(inst)
    else if (opcode >= OpCode.Unreachable && opcode <= OpCode.CallIndirect)
      evalControlInst(inst, loc)
    else if (opcode >= OpCode.TableGet && opcode <= OpCode.TableSet)
      evalTableInst(inst)
    else if (opcode >= OpCode.RefNull && opcode <= OpCode.RefFunc)
      evalRefInst(inst)
    else inst match
      case i: VarInst => evalVarInst(i)
      case i: TableInst => evalTableInst(i)
      case i: ReferenceInst => evalRefInst(i)
      case op: Miscop =>
        val v = stack.popOrAbort()
        stack.push(num.evalMiscop(op, v))
      case Drop => stack.popOrAbort()
      case Select =>
        val isZero = num.evalNumeric(i32.Eqz)
        branchOpsUnit.boolBranch(isZero) {
          // v == 0: else branch
          val (_, v2) = stack.pop2OrAbort()
          stack.push(v2)
        } {
          stack.popOrAbort()
        }
      case _ => throw new IllegalArgumentException(s"Unexpected instruction $inst")

  def evalControlInst(inst: Inst, loc: InstLoc)(using Fixed): Unit = inst match
    case Nop => // nothing
    case Unreachable => fail(UnreachableInstruction, inst.toString)
    case b@Block(bt, insts) =>
      label(BlockId(b), labelArities(bt, isLoop = false), insts, None)
    case l@Loop(bt, insts) =>
      label(BlockId(l), labelArities(bt, isLoop = true), insts, Some((l, loc)))
    case ifInst@If(bt, thnInsts, elsInsts) =>
      val isZero = num.evalNumeric(i32.Eqz)
      val ars = labelArities(bt, isLoop = false)
      branchOpsUnit.boolBranch(isZero) {
        // v == 0: else branch
        label(BlockId(ifInst -> false), ars, elsInsts, None)
      } {
        label(BlockId(ifInst -> true), ars, thnInsts, None)
      }
    case Br(labelIndex) =>
      branch(labelIndex)
    case BrIf(labelIndex) =>
      val isZero = num.evalNumeric(i32.Eqz)
      branchOpsUnit.boolBranch(isZero) {
        // v == 0: else branch
        // do nothing
      } {
        branch(labelIndex)
      }
    case BrTable(labels, defaultLabel) =>
      val ix = stack.popOrAbort()
      indexLookup(ix, labels).orElseAndThen(defaultLabel)(branch)
    case Return =>
      val operands = stack.popNOrAbort(callFrame.data.returnArity)
      throws(WasmException(JumpTarget.Return, operands))
    case Call(funcIdx) =>
      val func = module.functions.lift(funcIdx).getOrElse(fail(UnboundFunctionIndex, funcIdx.toString))
      invoke(func, loc)
    case CallIndirect(tableIdx, typeIdx) =>
      val ftExpected = module.functionTypes(typeIdx)
      val funcIx = stack.popOrAbort()
      val fRef = tables.getOrElse(TableAddr(tableIdx), valToIdx(funcIx), fail(UnboundFunctionIndex, funcIx.toString))
      val func = module.functions.lift(funcRefToInt(fRef)).getOrElse(fail(UnboundFunctionIndex, fRef.toString))
      val funV = funcInstToFunV(func)
      invokeIndirect(funV, ftExpected, funcIx, loc)
    case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")

  def branch(labelIndex: LabelIdx): Unit =
    val returnArity = labelStack.lookupReturnArity(labelIndex)
    val operands = stack.popNOrAbort(returnArity)
    throws(WasmException(JumpTarget.Jump(labelIndex), operands))

  /** Arities used by a label. Results equals jumpOperands if branchTarget is None. */
  case class LabelArities(params: Int, results: Int, jumpOperands: Int)

  private inline def assertFrameSize(size: Int): Unit =
    if (Debug.DEBUG_GENERIC_WASM_STACK && stack.frameSize != size)
      throw new AssertionError(s"Expected stack frame of size $size, but current stack frame has size ${stack.frameSize}")

  def label(block: BlockId, arities: LabelArities, insts: Iterable[Inst], branchTarget: Option[(Inst, InstLoc)])(using Fixed): Unit =
    stack.withNewFrame(arities.params) {
      tryCatch {
        labelStack.pushLabel(arities.jumpOperands)
        try {
          val modInst = module
          for ((inst, ix) <- insts.zipWithIndex) {
            val loc = modInst.blockInstLocs((block, ix))
            eval(inst, loc)
          }
          assertFrameSize(arities.results)
        } finally labelStack.popLabel()
      } { ex =>
        stack.clearCurrentOperandFrame()
        ex match {
          case WasmException(JumpTarget.Jump(labelIndex), operands) =>
            if (labelIndex == 0) {
              stack.pushN(operands)
              assertFrameSize(arities.jumpOperands)
              for ((i, loc) <- branchTarget)
                eval(i, loc)
              assertFrameSize(arities.results)
            } else {
              assertFrameSize(0)
              throws(WasmException(JumpTarget.Jump(labelIndex - 1), operands))
            }
          case WasmException(JumpTarget.Return, _) =>
            assertFrameSize(0)
            throws(ex)
        }
      }
    }

  def invoke(fun: FunctionInstance, loc: InstLoc)(using Fixed): Unit =
    val funcType = fun.funcType
    val frameData = FrameData(funcType.t.size, fun.module)
    val args = stack.popNOrAbort(funcType.params.size)

    fun match
      case FunctionInstance.Wasm(mod, ix, func, funcType) =>
        val vars = args.view.map(Some.apply) ++ func.locals.map(ty => Some(num.defaultValue(ty)))
        labelStack.withNew(stack.withNewFrame(0)(callFrame.withNew(frameData, vars.zipWithIndex.map(_.swap), loc) {
          enterFunction(FuncId(mod, ix), func, funcType)
        }))
      case FunctionInstance.Host(mod, ix, hostFunc) =>
        val vars = args.view.map(Some.apply)
        labelStack.withNew(stack.withNewFrame(0)(callFrame.withNew(frameData, vars.zipWithIndex.map(_.swap), loc) {
          enterHostFunction(FuncId(mod, ix), hostFunc)
        }))

  private def enterFunction_open(id: FuncId, func: Func, funcType: FuncType)(using Fixed): List[V] =
    val returnN = funcType.t.size
    tryCatch {
      label(BlockId(id), LabelArities(0, returnN, returnN), func.body, None)
    } { ex =>
      stack.clearCurrentOperandFrame()
      ex match {
        case WasmException(JumpTarget.Return, operands) =>
          stack.pushN(operands)
        case WasmException(JumpTarget.Jump(_), _) =>
          fail(InvalidModule, s"Tried to jump through a function boundary.")
      }
    }
    stack.peekNOrAbort(returnN)

  def invokeIndirect(funV: FunV, ftExpected: swam.FuncType, funcIx: V, loc: InstLoc)(using Fixed): Unit =
    functionOps.invokeFun(funV, ftExpected) {
      case (func, _) =>
        val ftActual = func.funcType
        if (ftExpected != ftActual)
          fail(IndirectCallTypeMismatch, s"Expected function of type $ftExpected but $funcIx has type $ftActual")
        invoke(func, loc)
    }


  inline def eval(inst: Inst, loc: InstLoc)(using rec: Fixed): FixOut[V] =
    rec(FixIn.Eval(inst, loc))

  inline def enterFunction(id: FuncId, func: Func, ft: FuncType)(using rec: Fixed): FixOut[V] =
    rec(FixIn.EnterWasmFunction(id, func, ft))

  inline def enterHostFunction(id: FuncId, hostFunc: HostFunction)(using rec: Fixed): FixOut[V] =
    rec(FixIn.EnterHostFunction(id, hostFunc))


  private def fixed: Fixed = fixpoint {
    case FixIn.Eval(inst, loc) =>
      eval_open(inst, loc)
      FixOut.Eval()
    case FixIn.EnterWasmFunction(id, func, funcType) =>
      FixOut.ExitWasmFunction(enterFunction_open(id, func, funcType))
    case FixIn.EnterHostFunction(id, hostFunc) =>
      FixOut.ExitHostFunction({
        val args = hostFunc.funcType.params.zipWithIndex.map(p => callFrame.getLocal(p._2).getOrElse(fail(UnboundLocal, s"Host arg $p")))
        val res = invokeHostFunction(hostFunc, args.toList)
        stack.pushN(res)
        res
      })
    case FixIn.MostGeneralClientLoop(modInst) =>
      runMostGeneralClient_open(modInst)
      FixOut.Eval()
  }

  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  private var typedTop: ValType => V = _

  def runMostGeneralClient(modInst: ModuleInstance, typedTop: ValType => V): Unit = external { rec ?=>
    this.typedTop = typedTop
    rec(FixIn.MostGeneralClientLoop(modInst))
  }

  private def runMostGeneralClient_open(modInst: ModuleInstance)(using rec: Fixed): Unit = {
    if (modInst.exportedFunctions.nonEmpty)
      effectStack.joinFold(modInst.exportedFunctions, { case (funcName, ExternalValue.Function(funcIx)) =>
        val fun = modInst.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
        val paramTys = fun.funcType.params
        val args = paramTys.map(typedTop).toList
        invokeExported_open(modInst, funcName, args)
        ()
      })
    rec(FixIn.MostGeneralClientLoop(modInst))
  }

  def invokeExported(modInst: ModuleInstance, funcName: String, args: List[V]): List[V] = external {
    invokeExported_open(modInst, funcName, args)
  }

  private def invokeExported_open(modInst: ModuleInstance, funcName: String, args: List[V])(using Fixed): List[V] = {
    stack.withNewStack {
      modInst.exportedFunctions.get(funcName) match
        case Some(ExternalValue.Function(funcIx)) =>
          val fun = modInst.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
          val paramTys = fun.funcType.params
          if (paramTys.length != args.length)
            throw new Error(s"Wrong number of arguments in external invocation. Expected ${paramTys.length} but got ${args.length}.")
          // paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
          val rtLength = fun.funcType.t.length
          stack.pushN(args)
          val loc = InstLoc.InvokeExported(modInst, funcName)
          callFrame.withNew(FrameData(0, modInst), Iterable.empty, loc) {
            eval(Call(funcIx), loc)
          }
          stack.popNOrAbort(rtLength)
        case _ => throw new Error(s"Function with name $funcName was not found in module's exports.")
    }
  }


  private def labelArities(bt: BlockType, isLoop: Boolean): LabelArities = bt match
    case swam.BlockType.NoType => LabelArities(0, 0, 0)
    case _: swam.BlockType.ValueType =>
      if (isLoop)
        LabelArities(0, 1, 0)
      else
        LabelArities(0, 1, 1)
    case swam.BlockType.FunctionType(tpe) =>
      val ft = module.functionTypes(tpe)
      val params = ft.params.size
      val results = ft.t.size
      if (isLoop)
        LabelArities(params, results, params)
      else
        LabelArities(params, results, results)


  //  // placeholder for the (not yet present in swam) memory.init instruction
  //  def memoryInit(dataIdx: Int): Unit =
  //    val dataInstance = module.data(dataIdx)
  //    val cnt = stack.pop() // i32
  //    val src = stack.pop() // i32
  //    val dst = stack.pop() // i32
  //    // check ranges TODO
  //    //if (src + cnt > dataInstance.data.size)
  //    // TODO WIP
  //    ???

  def evalInstructionSequence(block: BlockId, insts: Vector[Inst], mod: ModuleInstance, loc: InstLoc)(using Fixed): V =
    val frameData = FrameData(1, mod)
    labelStack.withNew(stack.withNewStack(callFrame.withNew(frameData, Iterable.empty, loc) {
      for ((inst, ix) <- insts.zipWithIndex) {
        val loc = mod.blockInstLocs((block, ix))
        eval(inst, loc)
      }
      stack.popOrAbort()
    }))

  /** add offset to base address (which is already on the stack) */
  def effectiveAddr(offset: Int): Addr =
    val v1 = i32ops.integerLit(offset)
    val v2 = stack.popOrAbort()
    val res = i32ops.add(v1, v2)
    val cmp = unsignedCompareOps.ltUnsigned(res, v1)
    val v = branchOpsV.boolBranch(cmp, fail(MemoryAccessOutOfBounds, s"$v1 + $v2"), res)
    valToAddr(v)

  def resolveImports(module: Module, imports: Imports):
  (Vector[FunctionInstance], Vector[GlobalAddr], Vector[TableAddr], Vector[MemoryAddr]) =
    val funcs: VectorBuilder[FunctionInstance] = VectorBuilder()
    val globs: VectorBuilder[GlobalAddr] = VectorBuilder()
    val tabs: VectorBuilder[TableAddr] = VectorBuilder()
    val mems: VectorBuilder[MemoryAddr] = VectorBuilder()

    module.imports.foreach { imp =>
      // handle host functions
      if (imp.moduleName == "wasi_snapshot_preview1" || imp.moduleName == "wasi_unstable") {
        imp match
          case Import.Function(_, funcName, funcType) =>
            val (ix, hf) = wasi.get(funcName)
            if (hf.funcType != module.types(funcType))
              throw new Error(s"Importing host function $funcName with wrong type: expected ${hf.funcType}, but imported with ${module.types(funcType)}")
            funcs += FunctionInstance.Host(wasi.module, ix, hf)
          case _ => throw new Error(s"Import from runtime: expected a function, but got $imp.")
      } else {
        // get the module to import from
        val from = imports.getOrElse(imp.moduleName, throw new Error(s"No module with name ${imp.moduleName} in imports."))
        // get the exported field
        val (_, exp) = from.exports.find((name, _) => name == imp.fieldName)
          .getOrElse(throw new Error(s"No export with name ${imp.fieldName} in module."))
        imp match
          case Import.Function(_, _, tpe) =>
            exp match
              case ExternalValue.Function(addr) =>
                val expectedType = module.types(tpe)
                val func = from.functions(addr)
                if (expectedType == func.funcType) {
                  funcs += func
                } else {
                  throw new Error(s"Type mismatch: expected $expectedType but found ${func.funcType}.")
                }
              case _ => throw new Error(s"Import mismatch: expected a function but found $exp.")
          case Import.Global(_, _, GlobalType(tpe, mut)) =>
            exp match
              case ExternalValue.Global(addr) =>
                val glob = from.globalAddrs(addr)
                // TODO: check mutability (=> add mut to GlobalInstance)
                globs += glob
              case _ => throw new Error(s"Import mismatch: expected a global but found $exp.")
          /*case Import.Reference(_, _, ReferenceType.FuncRef()) =>
            exp match
              case ExternalValue.Global(addr) =>
                val glob = from.globalAddrs(addr)
                // TODO: check mutability (=> add mut to GlobalInstance)
                globs += glob
              case _ => throw new Error(s"Import mismatch: expected a global but found $exp.")*/
          case Import.Table(_, _, tpe) =>
            exp match
              case ExternalValue.Table(addr) =>
                val tab = from.tableAddrs(addr)
                // TODO: check table type
                tabs += tab
              case _ => throw new Error(s"Import mismatch: expected a table but found $exp.")
          case Import.Memory(_, _, tpe) =>
            exp match
              case ExternalValue.Memory(addr) =>
                val mem = from.memoryAddrs(addr)
                // TODO: check memory type
                mems += mem
              case _ => throw new Error(s"Import mismatch: expected a memory but found $exp.")
      }
    }

    (funcs.result(), globs.result(), tabs.result(), mems.result())

  private var initialized: Boolean = false

  def initializeThis(): Unit =
    if (!initialized) {
      globals.putNew(globalTableIndex)
      initialized = true
    }

  // we assume a valid module here
  def initializeModule(module: Module, imports: Imports = Map.empty, moduleId: Option[Any] = None): ModuleInstance = external {
    initializeThis()

    val modInst = new ModuleInstance(moduleId)
    var loc = InstLoc.InInit(modInst, 0)
    // compute the initialization values for globals
    val (funcImports, globImports, tabImports, memImpors) = resolveImports(module, imports)
    // modInst.globalAddrs = globImports

    tabCount = 0
    tableLimits = List.empty
    tableTypes = List.empty
    // in the current swam version reference vectors are already provided via the elem fields of the module
    // -> we don't have to compute anything here for now

    // allocate structures for the new module
    // types
    modInst.functionTypes = module.types

    // functions
    val funcImportsSize = funcImports.size
    funcImports.foreach(modInst.addFunction)
    module.funcs.view.zipWithIndex.map { (func, ix) =>
      FunctionInstance.Wasm(modInst, funcImportsSize + ix, func, module.types(func.tpe))
    }.foreach(modInst.addFunction)

    // globals
    val globValues = module.globals.map { glob =>
      val id = BlockId(glob)
      loc = modInst.registerBlockSizes(id, loc, glob.init)
      evalInstructionSequence(id, glob.init, modInst, loc)
    }

    modInst.globalAddrs = modInst.globalAddrs :++ module.globals.zip(globValues).map {
      case (Global(GlobalType(tpe, _), _), value) =>
        val globalAddr = GlobalAddr(globCount)
        globCount += 1
        writeGlobalValue(globalAddr, value)
        globalAddr
    }
    // tables
    modInst.tableAddrs = tabImports ++ module.tables.map {
      case TableType(ty, Limits(min, max)) =>
        val tabAddr = TableAddr(tabCount)
        tables.putNew(tabAddr)
        tableTypes = tableTypes ::: List(ty)
        tableLimits = tableLimits ::: List((min, max))
        tabCount += 1
        tabAddr
    }
    var i = 0
    while (i < tabCount) {
      var j = 0
      val minlimit = tableLimits(i)._1
      while (j < minlimit) {
        tables.set(TableAddr(modInst.tableAddrs(i).addr), valToIdx(num.evalNumeric(i32.Const(j))), makeNullRef(tableTypes(i)))
        j += 1
      }
      i += 1
    }

    // memory
    modInst.memoryAddrs = memImpors ++ module.mems.map {
      case MemType(Limits(min, max)) =>
        val initSize = valToSize(i32ops.integerLit(min))
        val sizeLimit = max.map(i => valToSize(i32ops.integerLit(i)))
        val memAddr = MemoryAddr(memCount)
        memory.putNew(memAddr, initSize, sizeLimit)
        memCount += 1
        memAddr
    }
    // data
    modInst.data = module.data.map {
      case Data(_, _, init) => DataInstance(init.toByteVector)
    }
    // we don't need elems currently
    // exports
    modInst.exports = module.exports.map {
      case Export(fieldName, kind, index) =>
        kind match {
          case ExternalKind.Function => (fieldName, ExternalValue.Function(index))
          case ExternalKind.Global => (fieldName, ExternalValue.Global(index))
          case ExternalKind.Memory => (fieldName, ExternalValue.Memory(index))
          case ExternalKind.Table => (fieldName, ExternalValue.Table(index))
        }
    }

    // initialize tables and memories
    // memory
    module.data.zipWithIndex.foreach {
      case (data@Data(memIdx, off, init), i) =>
        assert(memIdx == 0)
        val id = BlockId(data)
        loc = modInst.registerBlockSizes(id, loc, off)
        val base = evalInstructionSequence(id, off, modInst, loc)
        val bytes = init.toByteVector.toIterable
        callFrame.withNew(FrameData(1, modInst), Iterable.empty, loc) {
          bytes.zipWithIndex.foreach { (byte, byteIdx) =>
            stack.push(base)
            stack.push(num.evalNumeric(i32.Const(byte.toInt)))
            store(i32.Store8(0, byteIdx))
          }
        }
      // in case we want to use memory.init here:
      //stack.push(num.evalNumeric(i32.Const(0)))
      //stack.push(num.evalNumeric(i32.Const((init.size / 8).toInt))) //is it ok to convert long to int here?
      //memoryInit(i)
      // memoryDrop(i) for the current wasm version
    }

    // tables
    modInst.elements = module.elem
    module.elem.zipWithIndex.foreach {
      case (elem@Elem(reftype, Left(init), mode), i) =>
        mode match {
          case ElemMode.Passive(_, _) => None
          case ElemMode.Declarative(_, _) => None
          case ElemMode.Active(_, _, tableIdx, off) =>
            val id = BlockId(elem)
            loc = modInst.registerBlockSizes(id, loc, off)
            val base = evalInstructionSequence(id, off, modInst, loc)
            init.zipWithIndex.foreach { (funcIx, i) => //i = index in element
              tableLimits(tableIdx)._2 match {
                case Some(maxLimit) => if (i >= maxLimit) {
                  fail(TableAccessOutOfBounds, s"FuncRef $funcIx in Table $tableIdx is larger than max limit $maxLimit")
                }
                case _ => None
              }
              stack.push(base)
              stack.push(num.evalNumeric(i32.Const(i)))
              stack.push(num.evalNumeric(i32.Add)) // adds index to base
              val idx = stack.popOrAbort() // stack is empty
              val funV = functionOps.funValue(modInst.functions(funcIx)) // funcIx is valid due to validation
              tables.set(TableAddr(modInst.tableAddrs(tableIdx).addr), valToIdx(idx), makeRef(funV))
              // TODO add failure conditions for table writing
            }

        }
      case _ => None
    }

    // invoke the start function
    module.start.foreach {
      funcIdx =>
        val func = modInst.functions(funcIdx)
        func match
          case FunctionInstance.Wasm(mod, ix, func, funcType) =>
            val frameData = FrameData(funcType.t.size, mod)
            val vars = func.locals.map(ty => Some(num.defaultValue(ty)))
            val loc = InstLoc.InvokeExported(mod, "$start")
            labelStack.withNew(stack.withNewFrame(0)(callFrame.withNew(frameData, vars.view.zipWithIndex.map(_.swap), loc) {
              enterFunction(FuncId(mod, ix), func, funcType)
            }))
          case _: FunctionInstance.Host => ??? // TODO: is it allowed to use host functions as start function?
    }
    //stack.ifEmpty({}, {throw IllegalStateException("Stack is not empty after module initialization.")})
    modInst

  }

