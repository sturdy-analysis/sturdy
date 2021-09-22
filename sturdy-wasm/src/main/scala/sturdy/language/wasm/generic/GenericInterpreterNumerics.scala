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
  (stack: OperandStack[V],
   ints: IntOps[V], longs: LongOps[V], floats: FloatOps[V], doubles: DoubleOps[V],
   eqOps: EqOps[V, V], compareOps: CompareOps[V, V], unsignedCompareOps: UnsignedCompareOps[V, V],
   intLong: ConvertIntLong[V, V], intFloat: ConvertIntFloat[V, V], intDouble: ConvertIntDouble[V, V],
   longInt: ConvertLongInt[V, V], longFloat: ConvertLongFloat[V, V], longDouble: ConvertLongDouble[V, V],
   floatInt: ConvertFloatInt[V, V], floatLong: ConvertFloatLong[V, V], floatDouble: ConvertFloatDouble[V, V],
   doubleInt: ConvertDoubleInt[V, V], doubleLong: ConvertDoubleLong[V, V], doubleFloat: ConvertDoubleFloat[V, V]
  ):

  import eqOps.*
  import compareOps.*
  import unsignedCompareOps.*
  import intLong.*
  import intDouble.*
  import longDouble.*
  import intFloat.*
  import longFloat.*
  import floatDouble.*
  
  def evalNumeric(inst: Inst): V =
    inst match
      case i32.Const(v) => ints.intLit(v)
      case i64.Const(v) => longs.longLit(v)
      case f32.Const(v) => floats.floatLit(v)
      case f64.Const(v) => doubles.doubleLit(v)
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
    case i32.Eqz => equ(v, ints.intLit(0))
    case i64.Eqz => equ(v, longs.longLit(0))

  inline def evalIUnop(op: IUnop, v: V): V = op match
    case i32.Clz => ints.countLeadingZeros(v)
    case i32.Ctz => ints.countTrailinZeros(v)
    case i32.Popcnt => ints.nonzeroBitCount(v)
    case i32.Extend8S =>
      val shift = ints.intLit(24)
      ints.shiftRight(ints.shiftLeft(v, shift), shift)
    case i32.Extend16S =>
      val shift = ints.intLit(16)
      ints.shiftRight(ints.shiftLeft(v, shift), shift)

    case i64.Clz => longs.countLeadingZeros(v)
    case i64.Ctz => longs.countTrailinZeros(v)
    case i64.Popcnt => longs.nonzeroBitCount(v)
    case i64.Extend8S =>
      val shift = longs.longLit(56)
      longs.shiftRight(longs.shiftLeft(v, shift), shift)
    case i64.Extend16S =>
      val shift = longs.longLit(48)
      longs.shiftRight(longs.shiftLeft(v, shift), shift)
    case i64.Extend32S =>
      val shift = longs.longLit(32)
      longs.shiftRight(longs.shiftLeft(v, shift), shift)

  inline def evalIBinop(op: IBinop, v1: V, v2: V): V = op match
    case i32.Add => ints.add(v1, v2)
    case i32.Sub => ints.sub(v1, v2)
    case i32.Mul => ints.mul(v1, v2)
    case i32.DivS => ints.div(v1, v2)
    case i32.DivU => ints.divUnsigned(v1, v2)
    case i32.RemS => ints.remainder(v1, v2)
    case i32.RemU => ints.remainderUnsigned(v1, v2)
    case i32.And => ints.bitAnd(v1, v2)
    case i32.Or => ints.bitOr(v1, v2)
    case i32.Xor => ints.bitXor(v1, v2)
    case i32.Shl => ints.shiftLeft(v1, ints.remainder(v2, ints.intLit(32)))
    case i32.ShrS => ints.shiftRight(v1, ints.remainder(v2, ints.intLit(32)))
    case i32.ShrU => ints.shiftRightUnsigned(v1, ints.remainder(v2, ints.intLit(32)))
    case i32.Rotl => ints.rotateLeft(v1, ints.remainder(v2, ints.intLit(32)))
    case i32.Rotr => ints.rotateRight(v1, ints.remainder(v2, ints.intLit(32)))

    case i64.Add => longs.add(v1, v2)
    case i64.Sub => longs.sub(v1, v2)
    case i64.Mul => longs.mul(v1, v2)
    case i64.DivS => longs.div(v1, v2)
    case i64.DivU => longs.divUnsigned(v1, v2)
    case i64.RemS => longs.remainder(v1, v2)
    case i64.RemU => longs.remainderUnsigned(v1, v2)
    case i64.And => longs.bitAnd(v1, v2)
    case i64.Or => longs.bitOr(v1, v2)
    case i64.Xor => longs.bitXor(v1, v2)
    case i64.Shl => longs.shiftLeft(v1, longs.remainder(v2, longs.longLit(64)))
    case i64.ShrS => longs.shiftRight(v1, longs.remainder(v2, longs.longLit(64)))
    case i64.ShrU => longs.shiftRightUnsigned(v1, longs.remainder(v2, longs.longLit(64)))
    case i64.Rotl => longs.rotateLeft(v1, longs.remainder(v2, longs.longLit(64)))
    case i64.Rotr => longs.rotateRight(v1, longs.remainder(v2, longs.longLit(64)))

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
    case f32.Abs => floats.absolute(v)
    case f32.Neg => floats.negated(v)
    case f32.Sqrt => floats.sqrt(v)
    case f32.Ceil => floats.ceil(v)
    case f32.Floor => floats.floor(v)
    case f32.Trunc => floats.truncate(v)
    case f32.Nearest => floats.nearest(v)

    case f64.Abs => doubles.absolute(v)
    case f64.Neg => doubles.negated(v)
    case f64.Sqrt => doubles.sqrt(v)
    case f64.Ceil => doubles.ceil(v)
    case f64.Floor => doubles.floor(v)
    case f64.Trunc => doubles.truncate(v)
    case f64.Nearest => doubles.nearest(v)

  inline def evalFBinop(op: FBinop, v1: V, v2: V): V = op match
    case f32.Add => floats.add(v1, v2)
    case f32.Sub => floats.sub(v1, v2)
    case f32.Mul => floats.mul(v1, v2)
    case f32.Div => floats.div(v1, v2)
    case f32.Min => floats.min(v1, v2)
    case f32.Max => floats.max(v1, v2)
    case f32.Copysign => floats.copysign(v1, v2)

    case f64.Add => doubles.add(v1, v2)
    case f64.Sub => doubles.sub(v1, v2)
    case f64.Mul => doubles.mul(v1, v2)
    case f64.Div => doubles.div(v1, v2)
    case f64.Min => doubles.min(v1, v2)
    case f64.Max => doubles.max(v1, v2)
    case f64.Copysign => doubles.copysign(v1, v2)

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
    case i32.WrapI64 => longInt(v, ())
    case i32.TruncSF32 => floatInt(v  , (config.Overflow.Fail, config.Bits.Signed))
    case i32.TruncUF32 => floatInt(v, (config.Overflow.Fail, config.Bits.Unsigned))
    case i32.TruncSF64 => doubleInt(v, (config.Overflow.Fail, config.Bits.Signed))
    case i32.TruncUF64 => doubleInt(v, (config.Overflow.Fail, config.Bits.Unsigned))
    case i32.ReinterpretF32 => floatInt(v, (config.Overflow.Allow, config.Bits.Raw))
    case i64.ExtendSI32 => intLong(v, config.Bits.Signed)
    case i64.ExtendUI32 => intLong(v, config.Bits.Unsigned)
    case i64.TruncSF32 => floatLong(v, (config.Overflow.Fail, config.Bits.Signed))
    case i64.TruncUF32 => floatLong(v, (config.Overflow.Fail, config.Bits.Unsigned))
    case i64.TruncSF64 => doubleLong(v, (config.Overflow.Fail, config.Bits.Signed))
    case i64.TruncUF64 => doubleLong(v, (config.Overflow.Fail, config.Bits.Unsigned))
    case i64.ReinterpretF64 => doubleLong(v, (config.Overflow.Allow, config.Bits.Raw))

    case f32.DemoteF64 => doubleFloat(v, ())
    case f32.ConvertSI32 => intFloat(v, config.Bits.Signed)
    case f32.ConvertUI32 => intFloat(v, config.Bits.Unsigned)
    case f32.ConvertSI64 => longFloat(v, config.Bits.Signed)
    case f32.ConvertUI64 => longFloat(v, config.Bits.Unsigned)
    case f32.ReinterpretI32 => intFloat(v, config.Bits.Raw)
    case f64.PromoteF32 => floatDouble(v, ())
    case f64.ConvertSI32 => intDouble(v, config.Bits.Signed)
    case f64.ConvertUI32 => intDouble(v, config.Bits.Unsigned)
    case f64.ConvertSI64 => longDouble(v, config.Bits.Signed)
    case f64.ConvertUI64 => longDouble(v, config.Bits.Unsigned)
    case f64.ReinterpretI64 => longDouble(v, config.Bits.Raw)

  inline def evalMiscop(op: Miscop, v: V): V = op match
    case i32.TruncSatSF32 => floatInt(v, (config.Overflow.JumpToBounds, config.Bits.Signed))
    case i32.TruncSatUF32 => floatInt(v, (config.Overflow.JumpToBounds, config.Bits.Unsigned))
    case i32.TruncSatSF64 => doubleInt(v, (config.Overflow.JumpToBounds, config.Bits.Signed))
    case i32.TruncSatUF64 => doubleInt(v, (config.Overflow.JumpToBounds, config.Bits.Unsigned))
    case i64.TruncSatSF32 => floatLong(v, (config.Overflow.JumpToBounds, config.Bits.Signed))
    case i64.TruncSatUF32 => floatLong(v, (config.Overflow.JumpToBounds, config.Bits.Unsigned))
    case i64.TruncSatSF64 => doubleLong(v, (config.Overflow.JumpToBounds, config.Bits.Signed))
    case i64.TruncSatUF64 => doubleLong(v, (config.Overflow.JumpToBounds, config.Bits.Unsigned))

  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => evalNumeric(i32.Const(0))
    case ValType.I64 => evalNumeric(i64.Const(0))
    case ValType.F32 => evalNumeric(f32.Const(0))
    case ValType.F64 => evalNumeric(f64.Const(0))