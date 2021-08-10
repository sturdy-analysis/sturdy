package sturdy.language.wasm.generic

import sturdy.effect.operandstack.OperandStack
import swam.syntax.*

class InterpreterNumerics[V]
  (using stack: OperandStack[V]):
  
  def evalNumeric(inst: Inst): V =
    inst match
      case i32.Const(v) => ???
      case i64.Const(v) => ???
      case f32.Const(v) => ???
      case f64.Const(v) => ???
      case op: IUnop => ???
        val v = stack.pop()
        evalIUnop(op, v)
      case op: FUnop => ???
        val v = stack.pop()
        evalFUnop(op, v)
      case op: IBinop => ???
        val (v1, v2) = stack.pop2()
        evalIBinop(op, v1, v2)
      case op: FBinop => ???
        val (v1, v2) = stack.pop2()
        evalFBinop(op, v1, v2)
      case op: Testop => ???
        val v = stack.pop()
        evalTestop(op, v)
      case op: IRelop => ???
        val (v1, v2) = stack.pop2()
        evalIRelop(op, v1, v2)
      case op: FRelop => ???
        val (v1, v2) = stack.pop2()
        evalFRelop(op, v1, v2)
      case op: Convertop => ???
        val v = stack.pop()
        evalConvertop(op, v)
      case _ => throw new IllegalArgumentException(s"Expected numeric instruction, but got $inst")


  def evalTestop(op: Testop, v: V): V = op match
    case i32.Eqz => ???
    case i64.Eqz => ???

  def evalIUnop(op: IUnop, v: V): V = op match
    case i32.Clz => ???
    case i32.Ctz => ???
    case i32.Popcnt => ???
    case i32.Extend8S => ???
    case i32.Extend16S => ???
    case i64.Clz => ???
    case i64.Ctz => ???
    case i64.Popcnt => ???
    case i64.Extend8S => ???
    case i64.Extend16S => ???
    case i64.Extend32S => ???

  def evalFUnop(op: FUnop, v: V): V = op match
    case f32.Abs => ???
    case f32.Neg => ???
    case f32.Sqrt => ???
    case f32.Ceil => ???
    case f32.Floor => ???
    case f32.Trunc => ???
    case f32.Nearest => ???
    case f64.Abs => ???
    case f64.Neg => ???
    case f64.Sqrt => ???
    case f64.Ceil => ???
    case f64.Floor => ???
    case f64.Trunc => ???
    case f64.Nearest => ???

  def evalIBinop(op: IBinop, v1: V, v2: V): V = op match
    case i32.Add => ???
    case i32.Sub => ???
    case i32.Mul => ???
    case i32.DivS => ???
    case i32.DivU => ???
    case i32.RemS => ???
    case i32.RemU => ???
    case i32.And => ???
    case i32.Or => ???
    case i32.Xor => ???
    case i32.Shl => ???
    case i32.ShrS => ???
    case i32.ShrU => ???
    case i32.Rotl => ???
    case i32.Rotr => ???
    case i64.Add => ???
    case i64.Sub => ???
    case i64.Mul => ???
    case i64.DivS => ???
    case i64.DivU => ???
    case i64.RemS => ???
    case i64.RemU => ???
    case i64.And => ???
    case i64.Or => ???
    case i64.Xor => ???
    case i64.Shl => ???
    case i64.ShrS => ???
    case i64.ShrU => ???
    case i64.Rotl => ???
    case i64.Rotr => ???

  def evalFBinop(op: FBinop, v1: V, v2: V): V = op match
    case f32.Add => ???
    case f32.Sub => ???
    case f32.Mul => ???
    case f32.Div => ???
    case f32.Min => ???
    case f32.Max => ???
    case f32.Copysign => ???
    case f64.Add => ???
    case f64.Sub => ???
    case f64.Mul => ???
    case f64.Div => ???
    case f64.Min => ???
    case f64.Max => ???
    case f64.Copysign => ???

  def evalIRelop(op: IRelop, v1: V, v2: V): V = op match
    case i32.Eq => ???
    case i32.Ne => ???
    case i32.LtS => ???
    case i32.LtU => ???
    case i32.GtS => ???
    case i32.GtU => ???
    case i32.LeS => ???
    case i32.LeU => ???
    case i32.GeS => ???
    case i32.GeU => ???
    case i64.Eq => ???
    case i64.Ne => ???
    case i64.LtS => ???
    case i64.LtU => ???
    case i64.GtS => ???
    case i64.GtU => ???
    case i64.LeS => ???
    case i64.LeU => ???
    case i64.GeS => ???
    case i64.GeU => ???

  def evalFRelop(op: FRelop, v1: V, v2: V): V = op match
    case f32.Eq => ???
    case f32.Ne => ???
    case f32.Lt => ???
    case f32.Gt => ???
    case f32.Le => ???
    case f32.Ge => ???
    case f64.Eq => ???
    case f64.Ne => ???
    case f64.Lt => ???
    case f64.Gt => ???
    case f64.Le => ???
    case f64.Ge => ???

  def evalConvertop(op: Convertop, v: V): V = op match
    case i32.WrapI64 => ???
    case i32.TruncSF32 => ???
    case i32.TruncUF32 => ???
    case i32.TruncSF64 => ???
    case i32.TruncUF64 => ???
    case i32.ReinterpretF32 => ???
    case i64.ExtendSI32 => ???
    case i64.ExtendUI32 => ???
    case i64.TruncSF32 => ???
    case i64.TruncUF32 => ???
    case i64.TruncSF64 => ???
    case i64.TruncUF64 => ???
    case i64.ReinterpretF64 => ???
    case f32.DemoteF64 => ???
    case f32.ConvertSI32 => ???
    case f32.ConvertUI32 => ???
    case f32.ConvertSI64 => ???
    case f32.ConvertUI64 => ???
    case f32.ReinterpretI32 => ???
    case f64.PromoteF32 => ???
    case f64.ConvertSI32 => ???
    case f64.ConvertUI32 => ???
    case f64.ConvertSI64 => ???
    case f64.ConvertUI64 => ???
    case f64.ReinterpretI64 => ???

  def evalMiscop(op: Miscop, v: V): V = op match
    case i32.TruncSatSF32 => ???
    case i32.TruncSatUF32 => ???
    case i32.TruncSatSF64 => ???
    case i32.TruncSatUF64 => ???
    case i64.TruncSatSF32 => ???
    case i64.TruncSatUF32 => ???
    case i64.TruncSatSF64 => ???
    case i64.TruncSatUF64 => ???

