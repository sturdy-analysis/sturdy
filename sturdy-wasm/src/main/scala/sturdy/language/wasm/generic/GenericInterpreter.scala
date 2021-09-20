package sturdy.language.wasm.generic

import sturdy.effect.callframe.CMutableCallFrameInt
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import swam.syntax.*
import swam.{ValType, TableType, GlobalType, LabelIdx, MemType, OpCode, BlockType, Limits}
import sturdy.effect.operandstack.OperandStack
import sturdy.effect.bytememory.{Serialize, Memory}
import sturdy.effect.branching.BoolBranching
import sturdy.effect.operandstack.COperandStack
import sturdy.effect.symboltable.SymbolTable
import sturdy.values.exceptions.Exceptional
import sturdy.values.convert.*
import sturdy.values.doubles.*
import sturdy.values.floats.*
import sturdy.values.functions.FunctionOps
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.*
import sturdy.values.unit

object GenericInterpreter:
  case class FrameData[V](returnArity: Int, module: ModuleInstance[V])

  enum WasmException[V]:
    case Jump(labelIndex: LabelIdx, operands: List[V])
    case Return(operands: List[V])

  type GenericEffects[V, Addr, Bytes, Size, ExcV, FuncIx, FunV] =
    COperandStack[V]
      with Memory[Int, Addr,Bytes,Size]
      with Serialize[V,Bytes,MemoryInst,MemoryInst]
      with SymbolTable[Int, FuncIx, FunV]
      with CMutableCallFrameInt[FrameData[V], V]
      with BoolBranching[V]
      with Except[WasmException[V], ExcV]
      with Failure


import GenericInterpreter.*

trait GenericInterpreter[V,Addr,Bytes,Size,ExcV, FuncIx, FunV, Effects <: GenericEffects[V,Addr,Bytes,Size,ExcV, FuncIx, FunV]]
  (val effects: Effects)
  (using wasmOps: WasmOperations[V, Addr, Size, FuncIx],
         exceptOps: Exceptional[WasmException[V], ExcV, effects.ExceptJoin])
  (using effects.BoolBranchJoin[Unit], effects.ExceptJoin[Unit],
   effects.MemoryJoin[Unit], effects.MemoryJoin[V],
   effects.TableJoin[Unit], wasmOps.WasmOpsJoin[Unit]):

  import effects.*
  val stack: OperandStack[V] = effects
  val memory: Memory[Int, Addr,Bytes,Size] = effects

  val intOps: IntOps[V]
  val longOps: LongOps[V]
  val floatOps: FloatOps[V]
  val doubleOps: DoubleOps[V]
  val eqOps: EqOps[V, V]
  val compareOps: CompareOps[V, V]
  val unsignedCompareOps: UnsignedCompareOps[V, V]
  val convertIntLong: ConvertIntLong[V, V]
  val convertIntFloat: ConvertIntFloat[V, V]
  val convertIntDouble: ConvertIntDouble[V, V]
  val convertLongInt: ConvertLongInt[V, V]
  val convertLongFloat: ConvertLongFloat[V, V]
  val convertLongDouble: ConvertLongDouble[V, V]
  val convertFloatInt: ConvertFloatInt[V, V]
  val convertFloatLong: ConvertFloatLong[V, V]
  val convertFloatDouble: ConvertFloatDouble[V, V]
  val convertDoubleInt: ConvertDoubleInt[V, V]
  val convertDoubleLong: ConvertDoubleLong[V, V]
  val convertDoubleFloat: ConvertDoubleFloat[V, V]
  val functionOps: FunctionOps[FunctionInstance[V], Nothing, Unit, FunV]

  lazy val num = new GenericInterpreterNumerics[V](
    effects,
    intOps, longOps, floatOps, doubleOps, eqOps, compareOps, unsignedCompareOps,
    convertIntLong, convertIntFloat, convertIntDouble,
    convertLongInt, convertLongFloat, convertLongDouble,
    convertFloatInt, convertFloatLong, convertFloatDouble,
    convertDoubleInt, convertDoubleLong, convertDoubleFloat)

  import wasmOps.*
  import exceptOps.*

  val labelStack = new LabelStack

  inline private def fail(k: FailureKind, what: String) = effects.fail(k, s"$what in $module")

  protected def module: ModuleInstance[V] = getFrameData.module

  def eval(inst: Inst): Unit =
    val opcode = inst.opcode
    if (opcode >= OpCode.I32Const && opcode <= OpCode.I64Extend32S)
      val v = num.evalNumeric(inst)
      stack.push(v)
    else if (opcode >= OpCode.I32Load && opcode <= OpCode.MemoryGrow)
      evalMemoryInst(inst)
    else if (opcode >= OpCode.Unreachable && opcode <= OpCode.CallIndirect)
      evalControlInst(inst)
    else inst match
      case i: VarInst => evalVarInst(i)
      case op: Miscop =>
        val v = stack.pop()
        num.evalMiscop(op, v)
      case Drop => stack.pop()
      case Select =>
        val isZero = num.evalNumeric(i32.Eqz)
        boolBranch[Unit](isZero) {
          // v == 0: else branch
          val (_, v2) = stack.pop2()
          stack.push(v2)
        } {
          stack.pop()
        }
      case _ => throw new IllegalArgumentException(s"Unexpected instruction $inst")

  def evalVarInst(inst: VarInst): Unit = inst match
    case LocalGet(ix) =>
      val v = getLocal(ix).getOrElse(fail(UnboundLocal, ix.toString))
      stack.push(v)
    case LocalSet(ix) =>
      val v = stack.pop()
      setLocal(ix, v).getOrElse(fail(UnboundLocal, ix.toString))
    case LocalTee(ix) =>
      val v = stack.peek()
      setLocal(ix, v).getOrElse(fail(UnboundLocal, ix.toString))
    case GlobalGet(globalIx) =>
      val global = module.globals.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      stack.push(global.value)
    case GlobalSet(globalIx) =>
      val global = module.globals.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val v = stack.pop()
      global.value = v


  def evalControlInst(inst: Inst): Unit = inst match
    case Nop => // nothing
    case Unreachable => fail(UnreachableInstruction, inst.toString)
    case Block(bt, insts) =>
      label(paramsArity(bt), returnArity(bt), insts, None)
    case l@Loop(bt, insts) =>
      val pt = paramsArity(bt)
      label(pt, pt, insts, Some(l))
    case If(bt, thnInsts, elsInsts) =>
      val isZero = num.evalNumeric(i32.Eqz)
      val rt = returnArity(bt)
      boolBranch[Unit](isZero) {
        // v == 0: else branch
        label(paramsArity(bt), rt, elsInsts, None)
      } {
        label(paramsArity(bt), rt, thnInsts, None)
      }
    case Br(labelIndex) => branch(labelIndex)
    case BrIf(labelIndex) =>
      val isZero = num.evalNumeric(i32.Eqz)
      boolBranch[Unit](isZero) {
        // v == 0: else branch
        // do nothing
      } {
        branch(labelIndex)
      }
    case BrTable(labels, defaultLabel) =>
      val ix = stack.pop()
      indexLookup(ix, labels).orElseAndThen(defaultLabel)(branch)
    case Return =>
      val operands = stack.popN(getFrameData.returnArity)
      throws(WasmException.Return(operands))
    case Call(funcIx) =>
      val func = module.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
      invoke(func)
    case CallIndirect(typeIx) =>
      val ftExpected = module.functionTypes(typeIx)
      val funcIx = stack.pop()
      tableGet(tableIndex, valueToFuncIx(funcIx)).orElseAndThen(fail(UnboundFunctionIndex, funcIx.toString)) { func =>
        if (func == null)
          fail(UninitializedFunction, funcIx.toString)
        invokeIndirect(func, ftExpected, funcIx)
      }
    case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")


  def branch(labelIndex: LabelIdx): Unit =
    val returnArity: Int = labelStack.lookupLabel(labelIndex)
    val operands = stack.popN(returnArity)
    throws(WasmException.Jump(labelIndex, operands))

  // stack before label-call:  A p0 ... pn (n = params arity)
  // finish without exception: A r0 ... rm (m = return arity) => nothing to do
  // catch Jump(l0, op0, ..., opm)
  //   - this block is the jump target
  //   - stack: A g0 ... gk needs to become A op0 ... opm
  //   - we don't know k, so we need to remember size of stack A
  // catch Jump(li, op0, ..., opl) i != 0 and Return(op0, ..., opl)
  //   - jump target is further out
  //   - stack: A g0 ... gok needs to become A
  //   - we don't know k, so we need to remember size of stack A
  def label(paramsArity: Int, returnArity: Int, insts: Iterable[Inst], branchTarget: Option[Inst]): Unit =
    val restoreStackSize = stack.size() - paramsArity
    tryCatchFinally {
      labelStack.pushLabel(returnArity)
      insts.foreach(eval)
    } { ex =>
      stack.popN(stack.size() - restoreStackSize)
      ex match {
        case WasmException.Jump(labelIndex, operands) =>
          if (labelIndex == 0) {
            stack.pushN(operands)
            branchTarget.foreach(eval)
          } else {
            throws(WasmException.Jump(labelIndex - 1, operands))
          }
        case _: WasmException.Return[V] => throws(ex)
      }
    } { // finally
      labelStack.popLabel()
    }

  // stack before invoke: A p0 ... pn (n = params arity)
  // finish without excepction: A r0 ... rm (m = return arity)
  // catch Return(op0, ..., opm)
  //    - stack A g0 ... gk needs to become A op0 ... opm
  //    - we don't know k, so we need to remember size of stack A
  // catch Jump(...) => error
  def invoke(func: FunctionInstance[V]): Unit =
    func match
      case FunctionInstance.Wasm(mod,func, funcType) =>
        val args = stack.popN(funcType.params.size)
        val frameData = FrameData(funcType.t.size, mod)
        val vars = args.view ++ func.locals.map(defaultValue)
        val restoreStackSize = stack.size()
        tryCatch {
          labelStack.withFresh(inNewFrameNoIndex(frameData, vars) {
            label(0, funcType.t.size, func.body, None)
          })
        } { ex =>
          stack.popN(stack.size() - restoreStackSize)
          ex match {
            case WasmException.Return(operands) =>
              stack.pushN(operands)
            case WasmException.Jump(_, _) =>
              fail(InvalidModule, s"Tried to jump through a function boundary.")
          }
        }

  def invokeIndirect(funV: FunV, ftExpected: swam.FuncType, funcIx: V): Unit =
    functionOps.invokeFun(funV, Seq()) {
      case (func, _) =>
        val ftActual = func.funcType
        if (ftExpected != ftActual)
          fail(IndirectCallTypeMismatch, s"Expected function of type $ftExpected but $funcIx has type $ftActual")
        invoke(func)
    }

  def invokeExported[Addr,Bytes,Size](modInst: ModuleInstance[V], funcName: String, args: List[V]): List[V] =
    inNewFrameNoIndex(FrameData(0, modInst), Iterable.empty) {
      module.exports.find((name, _) => name == funcName) match
        case Some((_, ExternalValue.Function(funcIx))) =>
          val func = module.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
          val paramTys = func.funcType.params
          if (paramTys.length != args.length)
            throw new Error(s"Wrong number of arguments in external invocation. Expected ${paramTys.length} but got ${args.length}.")
          // paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
          val rtLength = func.funcType.t.length
          stack.pushN(args)
          invoke(func)
          stack.popN(rtLength)
        case _ => throw new Error(s"Function with name $funcName was not found in module's exports.")
    }


  private def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => num.evalNumeric(i32.Const(0))
    case ValType.I64 => num.evalNumeric(i64.Const(0))
    case ValType.F32 => num.evalNumeric(f32.Const(0))
    case ValType.F64 => num.evalNumeric(f64.Const(0))

  private def returnArity(bt: BlockType): Int =
    val returnArity = bt.arity(module.functionTypes)
    if (returnArity < 0)
      fail(UnboundFunctionType, bt.toString)
    else
      returnArity

  private def paramsArity(bt: BlockType): Int =
    bt.params(module.functionTypes) match
      case Some(params) => params.size
      case None => fail(UnboundFunctionType, bt.toString)


  def evalMemoryInst(inst: Inst): Unit = inst match
    case i: LoadInst => load(i)
    case i: LoadNInst => load(i)

    case i: StoreInst => store(i)
    case i: StoreNInst => store(i)

    case MemorySize =>
      val memIdx = memoryIndex
      stack.push(sizeToVal(memSize(memIdx)))
    case MemoryGrow =>
      val delta = valToSize(stack.pop())
      val memIdx = memoryIndex
      val res = memGrow(memIdx, delta).option
        (num.evalNumeric(i32.Const(0xFFFFFFFF))) // 0xFFFFFFFF ~= -1
        {sizeToVal(_)}
      stack.push(res)
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def load(inst: MemoryInst): Unit =
    // add offset to base address (which is already on the stack)
    stack.push(intOps.intLit(inst.offset))
    eval(i32.Add)
    val addr = valueToAddr(stack.pop())

    val memIdx = memoryIndex
    val byteSize = getBytesToRead(inst)
    memRead(memIdx,addr,byteSize).option
      (fail(MemoryAccessOutOfBounds, s"Cannot read $byteSize bytes at address $addr in current memory."))
      {(b: Bytes) =>
        val v = decode(b, inst)
        stack.push(v)}

  def store(inst: MemoryInst): Unit =
    val v = stack.pop()
    val bytes = encode(v, inst)

    // add offset to base address (which is already on the stack)
    stack.push(intOps.intLit(inst.offset))
    eval(i32.Add)
    val addr = valueToAddr(stack.pop())

    val memIdx = memoryIndex
    memStore(memIdx, addr, bytes).getOrElse(
      fail(MemoryAccessOutOfBounds, s"Cannot write $bytes at address $addr in current memory.")
    )

  def getBytesToRead(inst: MemoryInst): Int = inst match
    case Load(tpe,_,_) => tpe.width / 4
    case LoadN(_,n,_,_) => n / 4
    case _ => throw new IllegalArgumentException(s"Expected load instruction, but got $inst")

  def memoryIndex: Int =
    module.memoryAddrs(0)

  def tableIndex: Int =
    module.tableAddrs(0)

  // placeholder for the (not yet present in swam) memory.init instruction
  def memoryInit(dataIdx: Int): Unit =
    val dataInstance = module.data(dataIdx)
    val cnt = stack.pop() // i32
    val src = stack.pop() // i32
    val dst = stack.pop() // i32
    // check ranges TODO
    //if (src + cnt > dataInstance.data.size)
    // TODO WIP
    ???

  def evalInstructionSequence(instructions: Expr, mod: ModuleInstance[V]): V =
    val frameData = FrameData(1,mod)
    withFreshOperandStack(labelStack.withFresh(inNewFrameNoIndex(frameData, Vector.empty[V]){
      instructions.foreach(eval)
      stack.pop()
    }))

  // we assume a valid module here
  def initializeModule(module: Module): ModuleInstance[V] =
    var memCount = 0
    var tabCount = 0
    // we ignore imports an imports checking for now -> start with the empty module instance
    val modInst = new ModuleInstance[V] {}
    // compute the initilization values for globals
    val globValues = module.globals.map(glob => evalInstructionSequence(glob.init, modInst))
    // in the current swam version reference vectors are already provided via the elem fields of the module
    // -> we don't have to compute anything here for now

    // allocate structures for the new module
    // types
    modInst.functionTypes = module.types
    // functions
    modInst.functions = module.funcs.map(func => FunctionInstance.Wasm(modInst,func,module.types(func.tpe)))
    // tables
    modInst.tableAddrs = module.tables.map {
      case TableType(_, Limits(min,max)) =>
        addEmptyTable(tabCount)
        val tabAddr = tabCount
        tabCount += 1
        tabAddr
    }
    // memory
    modInst.memoryAddrs = module.mems.map {
      case MemType(Limits(min, max)) =>
        val initSize = valToSize(intOps.intLit(min))
        val sizeLimit = max.map(i => valToSize(intOps.intLit(i)))
        addEmptyMemory(memCount, initSize, sizeLimit)
        val memAddr = memCount
        memCount += 1
        memAddr
    }
    // globals
    modInst.globals = module.globals.zip(globValues).map {
      case (Global(GlobalType(tpe,_),_),value) => GlobalInstance(tpe,value)
    }
    // data
    modInst.data = module.data.map {
      case Data(_,_,init) => DataInstance(init.toByteVector)
    }
    // we don't need elems currently
    // exports
    modInst.exports = module.exports.map {
      case Export(fieldName, kind, index) =>
        kind match {
          case ExternalKind.Function => (fieldName,ExternalValue.Function(index))
          case ExternalKind.Global => (fieldName,ExternalValue.Global(index))
          case ExternalKind.Memory => (fieldName,ExternalValue.Memory(index))
          case ExternalKind.Table => (fieldName,ExternalValue.Table(index))
        }
    }

    // initialize tables and memories
    val frameData = FrameData(1,modInst)
    // TODO: do we need a fresh stack and label stack here?
    inNewFrameNoIndex(frameData, Vector.empty[V]){
      // memory
      module.data.zipWithIndex.foreach {
        case (Data(memIdx, off, init),i) =>
          assert(memIdx == 0)
          off.foreach(eval)
          val base = stack.pop()
          val bytes = init.toByteVector.toIterable
          bytes.zipWithIndex.foreach { (byte,byteIdx) =>
            stack.push(base)
            eval(i32.Const(byte.toInt))
            eval(i32.Store8(0, byteIdx))
          }
          // in case we want to use memory.init here:
          //eval(i32.Const(0))
          //eval(i32.Const((init.size / 8).toInt)) //is it ok to convert long to int here?
          //memoryInit(i)
          // memoryDrop(i) for the current wasm version
      }
      // tables
      module.elem.zipWithIndex.foreach {
        case (Elem(tableIdx, off, init), i) =>
          off.foreach(eval)
          val base = stack.pop()
          init.zipWithIndex.foreach { (funcIx,i) =>
            stack.push(base)
            eval(i32.Const(i))
            eval(i32.Add) // adds index to base
            val idx = stack.pop() // stack is empty
            val funV = functionOps.funValue(modInst.functions(funcIx)) // funcIx is valid due to validation
            tableSet(tableIdx, valueToFuncIx(idx), funV)
            // TODO add failure conditions for table writing
          }
     }
    }

    // invoke the start function
    module.start.foreach {
      funcIdx => eval(Call(funcIdx))
    }

    modInst

