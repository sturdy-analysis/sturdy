package sturdy.language.wasm.generic

import sturdy.effect.noJoin
import sturdy.effect.callframe.CMutableCallFrameInt
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import swam.syntax.*
import swam.{BlockType, GlobalType, LabelIdx, Limits, MemType, OpCode, TableType, ValType}
import sturdy.effect.operandstack.OperandStack
import sturdy.effect.binarymemory.{Memory, Serialize}
import sturdy.effect.branching.BoolBranching
import sturdy.effect.table.Table
import sturdy.values.convert.*
import sturdy.values.doubles.*
import sturdy.values.floats.*
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.CompareOps
import sturdy.values.relational.EqOps
import sturdy.values.unit

object GenericInterpreter:
  case class FrameData[V](returnArity: Int, module: ModuleInstance[V])

  enum WasmException[V]:
    case Jump(labelIndex: LabelIdx, operands: List[V])
    case Return(operands: List[V])

  type GenericEffects[V,Addr,Bytes,Size] =
    OperandStack[V]
      with Memory[Addr,Bytes,Size]
      with Serialize[V,Bytes,MemoryInst,MemoryInst]
      with Table[V,FunctionInstance[V]]
      with CMutableCallFrameInt[FrameData[V], V]
      with BoolBranching[V]
      with Except[WasmException[V]]
      with Failure


import GenericInterpreter.*

trait GenericInterpreter[V,Addr,Bytes,Size]
  (val effects: GenericEffects[V,Addr,Bytes,Size])
  (using wasmOps: WasmOperations[V, Addr, Size])
  (using effects.BoolBranchJoin[Unit],
   effects.MemoryJoin[Unit], effects.MemoryJoin[V], effects.MemoryJoinComp,
   effects.TableJoin[Unit], effects.TableJoinComp,
   wasmOps.WasmOpsJoin[Unit], wasmOps.WasmOpsJoinComp):

  import effects.*
  implicit val stack: OperandStack[V] = effects.asInstanceOf[OperandStack[V]]
  //val memory = effectOps.asInstanceOf[WasmMemory[Addr,Bytes,Size,V]]

  implicit val intOps: IntOps[V]
  implicit val longOps: LongOps[V]
  implicit val floatOps: FloatOps[V]
  implicit val doubleOps: DoubleOps[V]
  implicit val eqOps: EqOps[V, V]
  implicit val compareOps: CompareOps[V, V]
  implicit val intCompareOps: IntegerCompareOps[V, V]
  implicit val convertIntLong: ConvertIntLong[V, V]
  implicit val convertIntFloat: ConvertIntFloat[V, V]
  implicit val convertIntDouble: ConvertIntDouble[V, V]
  implicit val convertLongInt: ConvertLongInt[V, V]
  implicit val convertLongFloat: ConvertLongFloat[V, V]
  implicit val convertLongDouble: ConvertLongDouble[V, V]
  implicit val convertFloatInt: ConvertFloatInt[V, V]
  implicit val convertFloatLong: ConvertFloatLong[V, V]
  implicit val convertFloatDouble: ConvertFloatDouble[V, V]
  implicit val convertDoubleInt: ConvertDoubleInt[V, V]
  implicit val convertDoubleLong: ConvertDoubleLong[V, V]
  implicit val convertDoubleFloat: ConvertDoubleFloat[V, V]

  val numerics = new InterpretNumerics[V]
  import numerics.*
  import wasmOps.*

  val labelStack = new LabelStack

  inline private def fail(k: FailureKind, what: String) = effects.fail(k, s"$what in $module")

  def module: ModuleInstance[V] = getFrameData.module

  def eval(inst: Inst): Unit =
    val opcode = inst.opcode
    if (opcode >= OpCode.I32Const && opcode <= OpCode.I64Extend32S)
      val v = evalNumeric(inst)
      stack.push(v)
    else if (opcode >= OpCode.I32Load8S && opcode <= OpCode.MemoryGrow)
      evalMemoryInst(inst)
    else if (opcode >= OpCode.Nop && opcode <= OpCode.CallIndirect)
      evalControlInst(inst)
    else inst match
      case i: VarInst => evalVarInst(i)
      case op: Miscop =>
        val v = stack.pop()
        evalMiscop(op, v)
      case Drop => stack.pop()
      case Select =>
        val isZero = evalNumeric(i32.Eqz)
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
      val v = getLocal(ix).orElse(fail(UnboundLocal, ix.toString))
      stack.push(v)
    case LocalSet(ix) =>
      val v = stack.pop()
      setLocal(ix, v).orElse(fail(UnboundLocal, ix.toString))
    case LocalTee(ix) =>
      val v = stack.peek()
      setLocal(ix, v).orElse(fail(UnboundLocal, ix.toString))
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
      val params = stack.popN(paramsArity(bt))
      label(params, returnArity(bt), insts, None)
    case Loop(bt, insts) =>
      val pt = paramsArity(bt)
      val params = stack.popN(pt)
      label(params, pt, insts, Some(inst))
    case If(bt, thnInsts, elsInsts) =>
      val isZero = evalNumeric(i32.Eqz)
      val rt = returnArity(bt)
      val params = stack.popN(paramsArity(bt))
      boolBranch[Unit](isZero) {
        // v == 0: else branch
        label(params, rt, elsInsts, None)
      } {
        label(params, rt, thnInsts, None)
      }
    case Br(labelIndex) => branch(labelIndex)
    case BrIf(labelIndex) =>
      val isZero = evalNumeric(i32.Eqz)
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
      //val table = module.tables(0)
      val ftExpected = module.functionTypes(typeIx)
      val funcIx = stack.pop()
      tableGet(tableIndex, funcIx).orElseAndThen(fail(UnboundFunctionIndex, funcIx.toString)) { func =>
      //indexLookup(funcIx, table.functions).orElseAndThen(fail(UnboundFunctionIndex, funcIx.toString)) { func =>
        if (func == null)
          fail(UninitializedFunction, funcIx.toString)
        val ftActual = func.funcType
        if (ftExpected != ftActual)
          fail(IndirectCallTypeMismatch, s"Expected function of type $ftExpected but $funcIx has type $ftActual")
        invoke(func)
      }
    case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")


  def branch(labelIndex: LabelIdx): Unit =
    val returnArity: Int = labelStack.lookupLabel(labelIndex)
    val operands = stack.popN(returnArity)
    throws(WasmException.Jump(labelIndex, operands))

  def label(params: List[V], returnArity: Int, insts: Iterable[Inst], branchTarget: Option[Inst]): Unit =
    catchFinally {
      labelStack.pushLabel(returnArity)
      stack.restoreAfter {
        stack.pushN(params)
        insts.foreach(eval)
      }
    } { // catch
      case WasmException.Jump(labelIndex, operands) =>
        if (labelIndex == 0) {
          stack.pushN(operands)
          branchTarget.foreach(eval)
        } else {
          throws(WasmException.Jump(labelIndex - 1, operands))
        }
      case ex => throws(ex)
    } { // finally
      labelStack.popLabel()
    }

  def invoke(func: FunctionInstance[V]): Unit =
    catches {
      func match
        case FunctionInstance.Wasm(mod, func, funcType) =>
          val args = stack.popN(funcType.params.size)
          val frameData = FrameData(funcType.t.size, mod)
          val vars = args.view.reverse ++ func.locals.map(defaultValue)
          withFreshOperandStack(labelStack.withFresh(inNewFrameNoIndex(frameData, vars) {
            label(List.empty, funcType.t.size, func.body, None)
          }))
    } {
      case WasmException.Return(operands) =>
        stack.pushN(operands)
      case WasmException.Jump(_,_) => fail(InvalidModule, s"Tried to jump through a function boundary.")
    }

  def invokeWithArguments(args: List[V], rtLength: Int, func: FunctionInstance[V]): List[V] =
    stack.pushN(args.reverse)
    invoke(func)
    val res = stack.popN(rtLength)
    res.reverse

//  def invokeExported(funcName: String, args: List[V]): List[V] =
//    // lookup funcName in module's exports
//    module.exports.find(((name,_) => name == funcName)) match
//      case Some((_,ExternalValue.Function(funcIx))) =>
//        val func = module.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
//        val paramTys = func.funcType.params
//        if (paramTys.length != args.length)
//          fail(InvocationError,
//            s"Wrong number of arguments in external invocation. Expected ${paramTys.length} but got ${args.length}")
//        paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
//        val rtLength = func.funcType.t.length
//        stack.pushN(args.reverse)
//        invoke(func)
//        val res = stack.popN(rtLength)
//        res.reverse
//      case _ => fail(InvocationError,s"Function with name $funcName was not found in module's exports.")

  def invokeExported[Addr,Bytes,Size](funcName: String, args: List[V]): List[V] =
    module.exports.find((name,_) => name == funcName) match
      case Some((_,ExternalValue.Function(funcIx))) =>
        val func = module.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
        val paramTys = func.funcType.params
        if (paramTys.length != args.length)
          throw new Error(s"Wrong number of arguments in external invocation.")
        // paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
        val rtLength = func.funcType.t.length
        invokeWithArguments(args, rtLength, func)
      case _ => throw new Error(s"Function with name $funcName was not found in module's exports.")


  private def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => evalNumeric(i32.Const(0))
    case ValType.I64 => evalNumeric(i64.Const(0))
    case ValType.F32 => evalNumeric(f32.Const(0))
    case ValType.F64 => evalNumeric(f64.Const(0))

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
      val res = memGrow(memIdx, delta).withDefault
        (evalNumeric(i32.Const(0xFFFFFFFF))) // 0xFFFFFFFF ~= -1
        {sizeToVal(_)}
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def load(inst: MemoryInst): Unit =
    // add offset to base address (which is already on the stack)
    stack.push(summon[IntOps[V]].intLit(inst.offset))
    evalNumeric(i32.Add)
    val addr = valueToAddr(stack.pop())

    val memIdx = memoryIndex
    val byteSize = getBytesToRead(inst)
    val bytes = memRead(memIdx,addr,byteSize).withDefault
      (fail(MemoryAccessOutOfBounds, s"Cannot read $byteSize bytes at address $addr in current memory."))
      {(b: Bytes) =>
        val v = decode(b, inst)
        stack.push(v)}

  def store(inst: MemoryInst): Unit =
    val v = stack.pop()
    val bytes = encode(v, inst)

    // add offset to base address (which is already on the stack)
    stack.push(summon[IntOps[V]].intLit(inst.offset))
    evalNumeric(i32.Add)
    val addr = valueToAddr(stack.pop())

    val memIdx = memoryIndex
    memStore(memIdx, addr, bytes).orElse(
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
      tab =>
        tab match
          case TableType(_, Limits(min,max)) => addEmptyTable(min,max)
    }
    // memory
    modInst.memoryAddrs = module.mems.map {
      mem =>
        mem match
          case MemType(Limits(min, max)) => addEmptyMemory(min,max)
    }
    // globals
    modInst.globals = module.globals.zip(globValues).map {
      glob =>
        glob match
          case (Global(GlobalType(tpe,_),_),value) => GlobalInstance(tpe,value)
    }
    // data
    modInst.data = module.data.map {
      data =>
        data match
          case Data(_,_,init) => DataInstance(init.toByteVector)
    }
    // we don't need elems currently
    // exports
    modInst.exports = module.exports.map {
      exp =>
        exp match
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
        data =>
          data match
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
        elem =>
          elem match
            case (Elem(tableIdx, off, init), i) =>
              off.foreach(eval)
              val base = stack.pop()
              init.zipWithIndex.foreach { (funcIx,i) =>
                stack.push(base)
                eval(i32.Const(i))
                eval(i32.Add) // adds index to base
                val idx = stack.pop() // stack is empty
                tableSet(tableIdx, idx, modInst.functions(funcIx)).orElse(
                  fail(TableAccessOutOfBounds, s"Cannot write at index $idx in current table.")
                ) // funcIx is valid due to validation
              }
     }
    }

    // invoke the start function
    module.start.foreach {
      funcIdx => eval(Call(funcIdx))
    }

    modInst

