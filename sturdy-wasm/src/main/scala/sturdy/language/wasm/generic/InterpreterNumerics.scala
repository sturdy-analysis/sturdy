package sturdy.language.wasm.generic

import sturdy.effect.operandstack.OperandStack
import sturdy.values.conversion.ConvertFloatDoubleOps
import sturdy.values.conversion.ConvertIntDoubleOps
import sturdy.values.conversion.ConvertIntFloatOps
import sturdy.values.conversion.ConvertIntLongOps
import sturdy.values.conversion.ConvertLongDoubleOps
import sturdy.values.conversion.ConvertLongFloatOps
import sturdy.values.doubles.DoubleOps
import sturdy.values.floats.FloatOps
import sturdy.values.ints.IntCompareOps
import sturdy.values.ints.IntOps
import sturdy.values.longs.LongOps
import sturdy.values.relational.CompareOps
import sturdy.values.relational.EqOps
import swam.syntax.*

class InterpreterNumerics[V]
  (using stack: OperandStack[V],
   ints: IntOps[V], longs: LongOps[V], floats: FloatOps[V], doubles: DoubleOps[V],
   eqOps: EqOps[V, V], compareOps: CompareOps[V, V], intCompareOps: IntCompareOps[V, V],
   intLong: ConvertIntLongOps[V, V], intDouble: ConvertIntDoubleOps[V, V], longDouble: ConvertLongDoubleOps[V, V],
   intFloat: ConvertIntFloatOps[V, V], longFloat: ConvertLongFloatOps[V, V], floatDouble: ConvertFloatDoubleOps[V, V]
  ):

  import eqOps.*
  import compareOps.*
  import intCompareOps.*
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
    case i32.Shl => ints.shiftLeft(v1, v2)
    case i32.ShrS => ints.shiftRight(v1, v2)
    case i32.ShrU => ints.shiftRightUnsigned(v1, v2)
    case i32.Rotl => ints.rotateLeft(v1, v2)
    case i32.Rotr => ints.rotateRight(v1, v2)

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
    case i64.Shl => longs.shiftLeft(v1, v2)
    case i64.ShrS => longs.shiftRight(v1, v2)
    case i64.ShrU => longs.shiftRightUnsigned(v1, v2)
    case i64.Rotl => longs.rotateLeft(v1, v2)
    case i64.Rotr => longs.rotateRight(v1, v2)

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
    case i32.WrapI64 => longToInt(v)
    case i32.TruncSF32 => floatToInt(v)
    case i32.TruncUF32 => floatToIntUnsigned(v)
    case i32.TruncSF64 => doubleToInt(v)
    case i32.TruncUF64 => doubleToIntUnsigned(v)
    case i32.ReinterpretF32 => floatToRawInt(v)
    case i64.ExtendSI32 => intToLong(v)
    case i64.ExtendUI32 => intToLongUnsigned(v)
    case i64.TruncSF32 => floatToLong(v)
    case i64.TruncUF32 => floatToLongUnsigned(v)
    case i64.TruncSF64 => doubleToLong(v)
    case i64.TruncUF64 => doubleToLongUnsigned(v)
    case i64.ReinterpretF64 => doubleToRawLong(v)

    case f32.DemoteF64 => doubleToFloat(v)
    case f32.ConvertSI32 => intToFloat(v)
    case f32.ConvertUI32 => intToFloatUnsigned(v)
    case f32.ConvertSI64 => longToFloat(v)
    case f32.ConvertUI64 => longToFloatUnsigned(v)
    case f32.ReinterpretI32 => intToRawFloat(v)
    case f64.PromoteF32 => floatToDouble(v)
    case f64.ConvertSI32 => intToDouble(v)
    case f64.ConvertUI32 => intToDoubleUnsigned(v)
    case f64.ConvertSI64 => longToDouble(v)
    case f64.ConvertUI64 => longToDoubleUnsigned(v)
    case f64.ReinterpretI64 => longToRawDoulbe(v)

  inline def evalMiscop(op: Miscop, v: V): V = op match
    case i32.TruncSatSF32 => floatToIntSaturating(v)
    case i32.TruncSatUF32 => floatToIntSaturatingUnsigned(v)
    case i32.TruncSatSF64 => doubleToIntSaturating(v)
    case i32.TruncSatUF64 => doubleToIntSaturatingUnsigned(v)
    case i64.TruncSatSF32 => floatToLongSaturating(v)
    case i64.TruncSatUF32 => floatToLongSaturatingUnsigned(v)
    case i64.TruncSatSF64 => doubleToLongSaturating(v)
    case i64.TruncSatUF64 => doubleToLongSaturatingUnsigned(v)

