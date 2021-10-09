package sturdy.language.wasm.generic

import sturdy.effect.operandstack.OperandStack
import sturdy.values.config
import sturdy.values.doubles.*
import sturdy.values.floats.*
import sturdy.values.ints.*
import sturdy.values.longs.*
import sturdy.values.relational.*
import swam.ValType
import swam.syntax.*

class GenericInterpreterNumerics[V]
  (stack: OperandStack[V], wasmOps: WasmOps[V, _]):

  import wasmOps.*
  import eqOps.*
  import compareOps.*
  import unsignedCompareOps.*
  import convertIntLong.*
  import convertIntDouble.*
  import convertLongDouble.*
  import convertIntFloat.*
  import convertLongFloat.*
  import convertFloatDouble.*
  
  def evalNumeric(inst: Inst): V =
    inst match
      case i32.Const(v) => intOps.intLit(v)
      case i64.Const(v) => longOps.longLit(v)
      case f32.Const(v) => floatOps.floatLit(v)
      case f64.Const(v) => doubleOps.doubleLit(v)
      case op: IUnop =>
        val v = stack.pop()
        evalIUnop(op, v)
      case op: FUnop =>
        val v = stack.pop()
        evalFUnop(op, v)
      case op: IBinop =>
        val (v1, v2) = stack.pop2()
        evalIBinop(op, v1, v2)
      case op: FBinop =>
        val (v1, v2) = stack.pop2()
        evalFBinop(op, v1, v2)
      case op: Testop =>
        val v = stack.pop()
        evalTestop(op, v)
      case op: IRelop =>
        val (v1, v2) = stack.pop2()
        evalIRelop(op, v1, v2)
      case op: FRelop =>
        val (v1, v2) = stack.pop2()
        evalFRelop(op, v1, v2)
      case op: Convertop =>
        val v = stack.pop()
        evalConvertop(op, v)
      case _ => throw new IllegalArgumentException(s"Expected numeric instruction, but got $inst")


  inline def evalTestop(op: Testop, v: V): V = op match
    case i32.Eqz => equ(v, intOps.intLit(0))
    case i64.Eqz => equ(v, longOps.longLit(0))

  inline def evalIUnop(op: IUnop, v: V): V = op match
    case i32.Clz => intOps.countLeadingZeros(v)
    case i32.Ctz => intOps.countTrailinZeros(v)
    case i32.Popcnt => intOps.nonzeroBitCount(v)
    case i32.Extend8S =>
      val shift = intOps.intLit(24)
      intOps.shiftRight(intOps.shiftLeft(v, shift), shift)
    case i32.Extend16S =>
      val shift = intOps.intLit(16)
      intOps.shiftRight(intOps.shiftLeft(v, shift), shift)

    case i64.Clz => longOps.countLeadingZeros(v)
    case i64.Ctz => longOps.countTrailinZeros(v)
    case i64.Popcnt => longOps.nonzeroBitCount(v)
    case i64.Extend8S =>
      val shift = longOps.longLit(56)
      longOps.shiftRight(longOps.shiftLeft(v, shift), shift)
    case i64.Extend16S =>
      val shift = longOps.longLit(48)
      longOps.shiftRight(longOps.shiftLeft(v, shift), shift)
    case i64.Extend32S =>
      val shift = longOps.longLit(32)
      longOps.shiftRight(longOps.shiftLeft(v, shift), shift)

  inline def evalIBinop(op: IBinop, v1: V, v2: V): V = op match
    case i32.Add => intOps.add(v1, v2)
    case i32.Sub => intOps.sub(v1, v2)
    case i32.Mul => intOps.mul(v1, v2)
    case i32.DivS => intOps.div(v1, v2)
    case i32.DivU => intOps.divUnsigned(v1, v2)
    case i32.RemS => intOps.remainder(v1, v2)
    case i32.RemU => intOps.remainderUnsigned(v1, v2)
    case i32.And => intOps.bitAnd(v1, v2)
    case i32.Or => intOps.bitOr(v1, v2)
    case i32.Xor => intOps.bitXor(v1, v2)
    case i32.Shl => intOps.shiftLeft(v1, intOps.remainder(v2, intOps.intLit(32)))
    case i32.ShrS => intOps.shiftRight(v1, intOps.remainder(v2, intOps.intLit(32)))
    case i32.ShrU => intOps.shiftRightUnsigned(v1, intOps.remainder(v2, intOps.intLit(32)))
    case i32.Rotl => intOps.rotateLeft(v1, intOps.remainder(v2, intOps.intLit(32)))
    case i32.Rotr => intOps.rotateRight(v1, intOps.remainder(v2, intOps.intLit(32)))

    case i64.Add => longOps.add(v1, v2)
    case i64.Sub => longOps.sub(v1, v2)
    case i64.Mul => longOps.mul(v1, v2)
    case i64.DivS => longOps.div(v1, v2)
    case i64.DivU => longOps.divUnsigned(v1, v2)
    case i64.RemS => longOps.remainder(v1, v2)
    case i64.RemU => longOps.remainderUnsigned(v1, v2)
    case i64.And => longOps.bitAnd(v1, v2)
    case i64.Or => longOps.bitOr(v1, v2)
    case i64.Xor => longOps.bitXor(v1, v2)
    case i64.Shl => longOps.shiftLeft(v1, longOps.remainder(v2, longOps.longLit(64)))
    case i64.ShrS => longOps.shiftRight(v1, longOps.remainder(v2, longOps.longLit(64)))
    case i64.ShrU => longOps.shiftRightUnsigned(v1, longOps.remainder(v2, longOps.longLit(64)))
    case i64.Rotl => longOps.rotateLeft(v1, longOps.remainder(v2, longOps.longLit(64)))
    case i64.Rotr => longOps.rotateRight(v1, longOps.remainder(v2, longOps.longLit(64)))

  inline def evalIRelop(op: IRelop, v1: V, v2: V): V = op match
    case i32.Eq => equ(v1, v2)
    case i32.Ne => neq(v1, v2)
    case i32.LtS => lt(v1, v2)
    case i32.LtU => ltUnsigned(v1, v2)
    case i32.GtS => gt(v1, v2)
    case i32.GtU => gtUnsigned(v1, v2)
    case i32.LeS => le(v1, v2)
    case i32.LeU => leUnsigned(v1, v2)
    case i32.GeS => ge(v1, v2)
    case i32.GeU => geUnsigned(v1, v2)

    case i64.Eq => equ(v1, v2)
    case i64.Ne => neq(v1, v2)
    case i64.LtS => lt(v1, v2)
    case i64.LtU => ltUnsigned(v1, v2)
    case i64.GtS => gt(v1, v2)
    case i64.GtU => gtUnsigned(v1, v2)
    case i64.LeS => le(v1, v2)
    case i64.LeU => leUnsigned(v1, v2)
    case i64.GeS => ge(v1, v2)
    case i64.GeU => geUnsigned(v1, v2)


  inline def evalFUnop(op: FUnop, v: V): V = op match
    case f32.Abs => floatOps.absolute(v)
    case f32.Neg => floatOps.negated(v)
    case f32.Sqrt => floatOps.sqrt(v)
    case f32.Ceil => floatOps.ceil(v)
    case f32.Floor => floatOps.floor(v)
    case f32.Trunc => floatOps.truncate(v)
    case f32.Nearest => floatOps.nearest(v)

    case f64.Abs => doubleOps.absolute(v)
    case f64.Neg => doubleOps.negated(v)
    case f64.Sqrt => doubleOps.sqrt(v)
    case f64.Ceil => doubleOps.ceil(v)
    case f64.Floor => doubleOps.floor(v)
    case f64.Trunc => doubleOps.truncate(v)
    case f64.Nearest => doubleOps.nearest(v)

  inline def evalFBinop(op: FBinop, v1: V, v2: V): V = op match
    case f32.Add => floatOps.add(v1, v2)
    case f32.Sub => floatOps.sub(v1, v2)
    case f32.Mul => floatOps.mul(v1, v2)
    case f32.Div => floatOps.div(v1, v2)
    case f32.Min => floatOps.min(v1, v2)
    case f32.Max => floatOps.max(v1, v2)
    case f32.Copysign => floatOps.copysign(v1, v2)

    case f64.Add => doubleOps.add(v1, v2)
    case f64.Sub => doubleOps.sub(v1, v2)
    case f64.Mul => doubleOps.mul(v1, v2)
    case f64.Div => doubleOps.div(v1, v2)
    case f64.Min => doubleOps.min(v1, v2)
    case f64.Max => doubleOps.max(v1, v2)
    case f64.Copysign => doubleOps.copysign(v1, v2)

  inline def evalFRelop(op: FRelop, v1: V, v2: V): V = op match
    case f32.Eq => equ(v1, v2)
    case f32.Ne => neq(v1, v2)
    case f32.Lt => lt(v1, v2)
    case f32.Gt => gt(v1, v2)
    case f32.Le => le(v1, v2)
    case f32.Ge => ge(v1, v2)

    case f64.Eq => equ(v1, v2)
    case f64.Ne => neq(v1, v2)
    case f64.Lt => lt(v1, v2)
    case f64.Gt => gt(v1, v2)
    case f64.Le => le(v1, v2)
    case f64.Ge => ge(v1, v2)

  inline def evalConvertop(op: Convertop, v: V): V = op match
    case i32.WrapI64 => convertLongInt(v, ())
    case i32.TruncSF32 => convertFloatInt(v  , (config.Overflow.Fail, config.Bits.Signed))
    case i32.TruncUF32 => convertFloatInt(v, (config.Overflow.Fail, config.Bits.Unsigned))
    case i32.TruncSF64 => convertDoubleInt(v, (config.Overflow.Fail, config.Bits.Signed))
    case i32.TruncUF64 => convertDoubleInt(v, (config.Overflow.Fail, config.Bits.Unsigned))
    case i32.ReinterpretF32 => convertFloatInt(v, (config.Overflow.Allow, config.Bits.Raw))
    case i64.ExtendSI32 => convertIntLong(v, config.Bits.Signed)
    case i64.ExtendUI32 => convertIntLong(v, config.Bits.Unsigned)
    case i64.TruncSF32 => convertFloatLong(v, (config.Overflow.Fail, config.Bits.Signed))
    case i64.TruncUF32 => convertFloatLong(v, (config.Overflow.Fail, config.Bits.Unsigned))
    case i64.TruncSF64 => convertDoubleLong(v, (config.Overflow.Fail, config.Bits.Signed))
    case i64.TruncUF64 => convertDoubleLong(v, (config.Overflow.Fail, config.Bits.Unsigned))
    case i64.ReinterpretF64 => convertDoubleLong(v, (config.Overflow.Allow, config.Bits.Raw))

    case f32.DemoteF64 => convertDoubleFloat(v, ())
    case f32.ConvertSI32 => convertIntFloat(v, config.Bits.Signed)
    case f32.ConvertUI32 => convertIntFloat(v, config.Bits.Unsigned)
    case f32.ConvertSI64 => convertLongFloat(v, config.Bits.Signed)
    case f32.ConvertUI64 => convertLongFloat(v, config.Bits.Unsigned)
    case f32.ReinterpretI32 => convertIntFloat(v, config.Bits.Raw)
    case f64.PromoteF32 => convertFloatDouble(v, ())
    case f64.ConvertSI32 => convertIntDouble(v, config.Bits.Signed)
    case f64.ConvertUI32 => convertIntDouble(v, config.Bits.Unsigned)
    case f64.ConvertSI64 => convertLongDouble(v, config.Bits.Signed)
    case f64.ConvertUI64 => convertLongDouble(v, config.Bits.Unsigned)
    case f64.ReinterpretI64 => convertLongDouble(v, config.Bits.Raw)

  inline def evalMiscop(op: Miscop, v: V): V = op match
    case i32.TruncSatSF32 => convertFloatInt(v, (config.Overflow.JumpToBounds, config.Bits.Signed))
    case i32.TruncSatUF32 => convertFloatInt(v, (config.Overflow.JumpToBounds, config.Bits.Unsigned))
    case i32.TruncSatSF64 => convertDoubleInt(v, (config.Overflow.JumpToBounds, config.Bits.Signed))
    case i32.TruncSatUF64 => convertDoubleInt(v, (config.Overflow.JumpToBounds, config.Bits.Unsigned))
    case i64.TruncSatSF32 => convertFloatLong(v, (config.Overflow.JumpToBounds, config.Bits.Signed))
    case i64.TruncSatUF32 => convertFloatLong(v, (config.Overflow.JumpToBounds, config.Bits.Unsigned))
    case i64.TruncSatSF64 => convertDoubleLong(v, (config.Overflow.JumpToBounds, config.Bits.Signed))
    case i64.TruncSatUF64 => convertDoubleLong(v, (config.Overflow.JumpToBounds, config.Bits.Unsigned))

  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => evalNumeric(i32.Const(0))
    case ValType.I64 => evalNumeric(i64.Const(0))
    case ValType.F32 => evalNumeric(f32.Const(0))
    case ValType.F64 => evalNumeric(f64.Const(0))