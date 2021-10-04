package sturdy.language.wasm.generic

import sturdy.data.unit
import sturdy.effect.callframe.CMutableCallFrameInt
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.fix
import sturdy.effect.operandstack.OperandStack
import sturdy.effect.bytememory.{Memory, Serialize}
import sturdy.effect.branching.BoolBranching
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.symboltable.SymbolTable
import sturdy.values.exceptions.Exceptional
import sturdy.values.convert.*
import sturdy.values.doubles.*
import sturdy.values.floats.*
import sturdy.values.functions.FunctionOps
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.*
import swam.{BlockType, FuncIdx, FuncType, GlobalIdx, GlobalType, LabelIdx, Limits, MemType, OpCode, TableType, ValType}
import swam.syntax.*

import scala.collection.immutable.VectorBuilder
import scala.collection.mutable

case class FrameData[V](returnArity: Int, module: ModuleInstance[V]):
  override def toString: String =
    if (module == null)
      s"module null, rt $returnArity"
    else
      s"module ${Integer.toHexString(module.hashCode())}, rt $returnArity"

object FrameData:
  def empty[V]: FrameData[V] = FrameData[V](0, null)

enum WasmException[V]:
  case Jump(labelIndex: LabelIdx, operands: List[V])
  case Return(operands: List[V])



type GenericEffects[V, Addr, Bytes, Size, ExcV, Symbol, Entry] =
  OperandStack[V]
    with Memory[MemoryAddr, Addr,Bytes,Size]
    with Serialize[V,Bytes,MemoryInst,MemoryInst]
    with SymbolTable[TableAddr, Symbol, Entry]
    //with SymbolTable[TableAddr, GlobalIdx, GlobalInstance[V]]
    with CMutableCallFrameInt[FrameData[V], V]
    with BoolBranching[V]
    with Except[WasmException[V], ExcV]
    with Failure

type Imports[V] = mutable.Map[String, ModuleInstance[V]]

enum FixIn[V]:
  case Eval(inst: Inst)
  case EnterWasmFunction(id: Either[FuncIdx, V], func: swam.syntax.Func, ft: swam.FuncType)

  override def toString: String = this match
    case Eval(i) => s"eval ${i.getClass.getSimpleName} ${Integer.toHexString(i.hashCode())}"
    case EnterWasmFunction(id, _, _) => id match
      case Left(ix) => s"enter direct $ix"
      case Right(v) => s"enter indirect $v"

enum FixOut[V]:
  case Eval()
  case ExitWasmFunction(vals: List[V])

trait GenericInterpreter[V,Addr,Bytes,Size,ExcV, FuncIx, FunV, Symbol, Entry, Effects <: GenericEffects[V,Addr,Bytes,Size,ExcV, Symbol, Entry]]
  (val effects: Effects)
  (using wasmOps: WasmOperations[V, Addr, Size, FuncIx, FunV, Symbol, Entry],
         exceptOps: Exceptional[WasmException[V], ExcV, effects.ExceptJoin])
  (using effects.BoolBranchJoin[Unit], effects.ExceptJoin[Unit],
   effects.MemoryJoin[Unit], effects.MemoryJoin[V],
   effects.TableJoin[Unit], wasmOps.WasmOpsJoin[Unit]):

  import effects.*
  val stack: OperandStack[V] = effects

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

  val phi: fix.Combinator[FixIn[V], FixOut[V]]

  lazy val num = new GenericInterpreterNumerics[V](
    effects,
    intOps, longOps, floatOps, doubleOps, eqOps, compareOps, unsignedCompareOps,
    convertIntLong, convertIntFloat, convertIntDouble,
    convertLongInt, convertLongFloat, convertLongDouble,
    convertFloatInt, convertFloatLong, convertFloatDouble,
    convertDoubleInt, convertDoubleLong, convertDoubleFloat)

  private var memCount = 0
  private var tabCount = 0
  private var globCount = 0

  // add empty table at addr 0 for global variables
  assert(TableAddr(tabCount) == globalTableIndex)
  addEmptyTable(globalTableIndex)
  tabCount += 1

  import wasmOps.*
  import exceptOps.*

  val labelStack = new LabelStack

  inline private def fail(k: FailureKind, what: String) = effects.fail(k, s"$what in $module")

  protected def module: ModuleInstance[V] = getFrameData.module

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
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      tableGet(globalTableIndex, globIxToSymbol(globalIdx)).orElseAndThen(fail(UnboundGlobal, globalIdx.toString)) { entry =>
        val global = entryToGlobI(entry)
        stack.push(global.value)
      }
    case GlobalSet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val v = stack.pop()
      tableGet(globalTableIndex, globIxToSymbol(globalIdx)).orElseAndThen(fail(UnboundGlobal, globalIdx.toString)) { entry =>
        val global = entryToGlobI(entry)
        val newGlobal = GlobalInstance(global.tpe,v)
        tableSet(globalTableIndex, globIxToSymbol(globalIdx), globIToEntry(newGlobal))
      }

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
    addWithOverflowCheck
    val effectiveAddr = stack.pop()
    val addr = valueToAddr(effectiveAddr)

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
    stack.push(num.evalNumeric(i32.Add))
    val addr = valueToAddr(stack.pop())

    val memIdx = memoryIndex
    memStore(memIdx, addr, bytes).getOrElse(
      fail(MemoryAccessOutOfBounds, s"Cannot write $bytes at address $addr in current memory.")
    )

  def getBytesToRead(inst: MemoryInst): Int = inst match
    case Load(tpe,_,_) => tpe.width / 8
    case LoadN(_,n,_,_) => n / 8
    case _ => throw new IllegalArgumentException(s"Expected load instruction, but got $inst")

  def memoryIndex: MemoryAddr =
    module.memoryAddrs(0)

  def tableIndex: TableAddr =
    module.tableAddrs(0)

  val globalTableIndex: TableAddr = TableAddr(0)

  def getGlobalValue(index: GlobalAddr): V =
    tableGet(globalTableIndex, globIxToSymbol(index)).orElseAndThen(fail(UnboundGlobal, index.toString)) { entry =>
      val global = entryToGlobI(entry)
      stack.push(global.value)
    }
    stack.pop()



  private lazy val fixed = fix.Fixpoint { (rec: FixIn[V] => FixOut[V]) =>
    inline def eval(inst: Inst): FixOut[V] =
      rec(FixIn.Eval(inst))
    inline def enterFunction(id: Either[FuncIdx, V], func: Func, ft: FuncType): FixOut[V] =
      rec(FixIn.EnterWasmFunction(id, func, ft))

    def eval_open(inst: Inst): Unit =
      val opcode = inst.opcode
      if (opcode >= OpCode.I32Const && opcode <= OpCode.I64Extend32S)
        stack.push(num.evalNumeric(inst))
      else if (opcode >= OpCode.I32Load && opcode <= OpCode.MemoryGrow)
        evalMemoryInst(inst)
      else if (opcode >= OpCode.Unreachable && opcode <= OpCode.CallIndirect)
        evalControlInst(inst)
      else inst match
        case i: VarInst => evalVarInst(i)
        case op: Miscop =>
          val v = stack.pop()
          stack.push(num.evalMiscop(op, v))
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
      case Br(labelIndex) =>
        branch(labelIndex)
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
        invoke(func, Left(funcIx))
      case CallIndirect(typeIx) =>
        val ftExpected = module.functionTypes(typeIx)
        val funcIx = stack.pop()
        tableGet(tableIndex, funcIxToSymbol(valueToFuncIx(funcIx))).orElseAndThen(fail(UnboundFunctionIndex, funcIx.toString)) { entry =>
          val func = entryToFuncV(entry)
          if (func == null)
            fail(UninitializedFunction, funcIx.toString)
          invokeIndirect(func, ftExpected, funcIx)
        }
      case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")


    def branch(labelIndex: LabelIdx): Unit =
      val returnArity: Int = labelStack.lookupLabel(labelIndex)
      val operands = stack.popN(returnArity)
      throws(WasmException.Jump(labelIndex, operands))

    /* stack before label-call:  A p0 ... pn (n = params arity)
   * finish without exception: A r0 ... rm (m = return arity) => nothing to do
   * catch Jump(l0, op0, ..., opm)
   *   - this block is the jump target
   *   - stack: A g0 ... gk needs to become A op0 ... opm
   *   - we don't know k, so we need to remember size of stack A
   * catch Jump(li, op0, ..., opl) i != 0 and Return(op0, ..., opl)
   *   - jump target is further out
   *   - stack: A g0 ... gok needs to become A
   *   - we don't know k, so we need to remember size of stack A
   */
    def label(paramsArity: Int, returnArity: Int, insts: Iterable[Inst], branchTarget: Option[Inst]): Unit =
      stack.withFreshOperandFrame {
        tryCatch {
          labelStack.pushLabel(returnArity)
          try insts.foreach(eval(_))
          finally labelStack.popLabel()
        } { ex =>
          stack.clearCurrentOperandFrame()
          ex match {
            case WasmException.Jump(labelIndex, operands) =>
              if (labelIndex == 0) {
                stack.pushN(operands)
                branchTarget.foreach(eval(_))
              } else {
                throws(WasmException.Jump(labelIndex - 1, operands))
              }
            case _: WasmException.Return[V] =>
              throws(ex)
          }
        }
      }

    // stack before invoke: A p0 ... pn (n = params arity)
    // finish without excepction: A r0 ... rm (m = return arity)
    // catch Return(op0, ..., opm)
    //    - stack A g0 ... gk needs to become A op0 ... opm
    //    - we don't know k, so we need to remember size of stack A
    // catch Jump(...) => error
    def invoke(fun: FunctionInstance[V], funcIx: Either[FuncIdx, V]): Unit =
      fun match
        case FunctionInstance.Wasm(mod, func, funcType) =>
          val args = stack.popN(funcType.params.size)
          val frameData = FrameData(funcType.t.size, mod)
          val vars = args.view ++ func.locals.map(num.defaultValue)
          labelStack.withFresh(stack.withFreshOperandFrame(inNewFrameNoIndex(frameData, vars) {
            enterFunction(funcIx, func, funcType)
          }))

    def enterFunction_open(func: Func, funcType: FuncType): List[V] =
      val returnN = funcType.t.size
      tryCatch {
        label(0, returnN, func.body, None)
      } { ex =>
        stack.clearCurrentOperandFrame()
        ex match {
          case WasmException.Return(operands) =>
            stack.pushN(operands)
          case WasmException.Jump(_, _) =>
            fail(InvalidModule, s"Tried to jump through a function boundary.")
        }
      }
      stack.peekN(returnN)

    def invokeIndirect(funV: FunV, ftExpected: swam.FuncType, funcIx: V): Unit =
      functionOps.invokeFun(funV, Seq()) {
        case (func, _) =>
          val ftActual = func.funcType
          if (ftExpected != ftActual)
            fail(IndirectCallTypeMismatch, s"Expected function of type $ftExpected but $funcIx has type $ftActual")
          invoke(func, Right(funcIx))
      }

    phi {
      case FixIn.Eval(inst) =>
        eval_open(inst); FixOut.Eval()
      case FixIn.EnterWasmFunction(id, func, funcType) =>
        FixOut.ExitWasmFunction(enterFunction_open(func, funcType))
    }
  }

  def eval(inst: Inst): FixOut[V] = fixed(FixIn.Eval(inst))
  def enterFunction(id: Either[FuncIdx, V], func: Func, ft: FuncType): FixOut[V] = fixed(FixIn.EnterWasmFunction(id, func, ft))


  def invokeExported[Addr,Bytes,Size](modInst: ModuleInstance[V], funcName: String, args: List[V]): List[V] =
    modInst.exports.find((name, _) => name == funcName) match
      case Some((_, ExternalValue.Function(funcIx))) =>
        val func = modInst.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
        val paramTys = func.funcType.params
        if (paramTys.length != args.length)
          throw new Error(s"Wrong number of arguments in external invocation. Expected ${paramTys.length} but got ${args.length}.")
        // paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
        val rtLength = func.funcType.t.length
        func match
          case FunctionInstance.Wasm(mod, func, funcType) =>
            val frameData = FrameData(funcType.t.size, mod)
            val vars = args.view ++ func.locals.map(num.defaultValue)
            labelStack.withFresh(stack.withFreshOperandFrame(inNewFrameNoIndex(frameData, vars) {
              val res = enterFunction(Left(funcIx), func, funcType)
//              println(s"invoke exported $funcName = $res should have $rtLength values")
//              println(func)
            }))
        stack.popN(rtLength)
      case _ => throw new Error(s"Function with name $funcName was not found in module's exports.")


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

  def evalInstructionSequence(instructions: Expr, mod: ModuleInstance[V]): V =
    val frameData = FrameData(1,mod)
    labelStack.withFresh(withFreshOperandStack(inNewFrameNoIndex(frameData, Vector.empty[V]){
      instructions.foreach(eval)
      stack.pop()
    }))

  def addWithOverflowCheck =
    val v1 = stack.pop()
    val v2 = stack.pop()
    val res = intOps.add(v1,v2)
    val cmp = unsignedCompareOps.ltUnsigned(res,v1)
    boolBranch[Unit](cmp) {
      fail(IntOverflow, s"$v1 + $v2")
    } {
      stack.push(res)
    }
  
  def resolveImports(module: Module, imports: Imports[V]):
    (Vector[FunctionInstance[V]], Vector[GlobalAddr], Vector[TableAddr], Vector[MemoryAddr]) =
    val funcs: VectorBuilder[FunctionInstance[V]] = VectorBuilder()
    val globs: VectorBuilder[GlobalAddr] = VectorBuilder()
    val tabs: VectorBuilder[TableAddr] = VectorBuilder()
    val mems: VectorBuilder[MemoryAddr] = VectorBuilder()

    module.imports.foreach { imp =>
      // get the module to import from
      val from = imports.getOrElse(imp.moduleName, throw new Error(s"No module with name ${imp.moduleName} in imports." ))
      // get the exported field
      val (_,exp) = from.exports.find((name,_) => name == imp.fieldName)
        .getOrElse(throw new Error(s"No export with name ${imp.fieldName} in module."))
      imp match
        case Import.Function(_,_,tpe) =>
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
        case Import.Global(_,_,GlobalType(tpe, mut)) =>
          exp match
            case ExternalValue.Global(addr) =>
              val glob = from.globalAddrs(addr)
              // TODO: check mutability (=> add mut to GlobalInstance)
              globs += glob
            case _ => throw new Error(s"Import mismatch: expected a global but found $exp.")
        case Import.Table(_,_,tpe) =>
          exp match
            case ExternalValue.Table(addr) =>
              val tab = from.tableAddrs(addr)
              // TODO: check table type
              tabs += tab
            case _ => throw new Error(s"Import mismatch: expected a table but found $exp.")
        case Import.Memory(_,_,tpe) =>
          exp match
            case ExternalValue.Memory(addr) =>
              val mem = from.memoryAddrs(addr)
              // TODO: check memory type
              mems += mem
            case _ => throw new Error(s"Import mismatch: expected a memory but found $exp.")
    }

    (funcs.result(), globs.result(), tabs.result(), mems.result())

  // we assume a valid module here
  def initializeModule(module: Module, imports: Imports[V] = mutable.Map.empty): ModuleInstance[V] =
    val modInst = new ModuleInstance[V] {}
    // compute the initilization values for globals
    val (funcImports, globImports, tabImports, memImpors) = resolveImports(module, imports)
    modInst.globalAddrs = globImports
    val globValues = module.globals.map(glob => evalInstructionSequence(glob.init, modInst))
    // in the current swam version reference vectors are already provided via the elem fields of the module
    // -> we don't have to compute anything here for now

    // allocate structures for the new module
    // types
    modInst.functionTypes = module.types
    // functions
    modInst.functions = funcImports ++
      module.funcs.map(func => FunctionInstance.Wasm(modInst,func,module.types(func.tpe)))
    // globals
    modInst.globalAddrs = modInst.globalAddrs :++ module.globals.zip(globValues).map {
      case (Global(GlobalType(tpe,_),_),value) =>
        val globalAddr = GlobalAddr(globCount)
        globCount += 1
        val newGlobal = GlobalInstance(tpe,value)
        tableSet(globalTableIndex, globIxToSymbol(globalAddr), globIToEntry(newGlobal))
        globalAddr
    }
    // tables
    modInst.tableAddrs = tabImports ++ module.tables.map {
      case TableType(_, Limits(min,max)) =>
        val tabAddr = TableAddr(tabCount)
        addEmptyTable(tabAddr)
        tabCount += 1
        tabAddr
    }
    // memory
    modInst.memoryAddrs = memImpors ++ module.mems.map {
      case MemType(Limits(min, max)) =>
        val initSize = valToSize(intOps.intLit(min))
        val sizeLimit = max.map(i => valToSize(intOps.intLit(i)))
        val memAddr = MemoryAddr(memCount)
        addEmptyMemory(memAddr, initSize, sizeLimit)
        memCount += 1
        memAddr
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
            stack.push(num.evalNumeric(i32.Const(byte.toInt)))
            evalMemoryInst(i32.Store8(0, byteIdx))
          }
          // in case we want to use memory.init here:
          //stack.push(num.evalNumeric(i32.Const(0)))
          //stack.push(num.evalNumeric(i32.Const((init.size / 8).toInt))) //is it ok to convert long to int here?
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
            stack.push(num.evalNumeric(i32.Const(i)))
            stack.push(num.evalNumeric(i32.Add)) // adds index to base
            val idx = stack.pop() // stack is empty
            val funV = functionOps.funValue(modInst.functions(funcIx)) // funcIx is valid due to validation
            tableSet(TableAddr(modInst.tableAddrs(tableIdx).addr), funcIxToSymbol(valueToFuncIx(idx)), funVToEntry(funV))
            // TODO add failure conditions for table writing
          }
     }
    }

    // invoke the start function
    module.start.foreach {
      funcIdx =>
        val func = modInst.functions(funcIdx)
        func match
          case FunctionInstance.Wasm(mod, func, funcType) =>
            val frameData = FrameData(funcType.t.size, mod)
            val vars = func.locals.map(num.defaultValue)
            labelStack.withFresh(stack.withFreshOperandFrame(inNewFrameNoIndex(frameData, vars) {
              val res = enterFunction(Left(funcIdx), func, funcType)
//              println(s"invoke exported $funcName = $res should have $rtLength values")
//              println(func)
            }))
    }

    modInst

