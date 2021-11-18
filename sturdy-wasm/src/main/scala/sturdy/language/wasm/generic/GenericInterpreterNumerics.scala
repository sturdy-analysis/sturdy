package sturdy.language.wasm.generic

import sturdy.data.unit
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.config
import sturdy.values.convert.*
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.relational.*
import swam.ValType
import swam.syntax.*

class GenericInterpreterNumerics[V, MayJoin[_]]
  (stack: DecidableOperandStack[V], wasmOps: WasmOps[V, _, _, _, _, _, _, MayJoin])
  (using Failure):

  import wasmOps.*
  import eqOps.*
  import compareOps.*
  import unsignedCompareOps.*
  import convert_i32_i64.*
  import convert_i32_f64.*
  import convert_i64_f64.*
  import convert_i32_f32.*
  import convert_i64_f32.*
  import convert_f32_f64.*

  def evalNumeric(inst: Inst): V =
    inst match
      case i32.Const(v) => i32ops.integerLit(v)
      case i64.Const(v) => i64ops.integerLit(v)
      case f32.Const(v) => f32ops.floatingLit(v)
      case f64.Const(v) => f64ops.floatingLit(v)
      case op: IUnop =>
        val v = stack.popOrFail()
        evalIUnop(op, v)
      case op: FUnop =>
        val v = stack.popOrFail()
        evalFUnop(op, v)
      case op: IBinop =>
        val (v1, v2) = stack.pop2OrFail()
        evalIBinop(op, v1, v2)
      case op: FBinop =>
        val (v1, v2) = stack.pop2OrFail()
        evalFBinop(op, v1, v2)
      case op: Testop =>
        val v = stack.popOrFail()
        evalTestop(op, v)
      case op: IRelop =>
        val (v1, v2) = stack.pop2OrFail()
        evalIRelop(op, v1, v2)
      case op: FRelop =>
        val (v1, v2) = stack.pop2OrFail()
        evalFRelop(op, v1, v2)
      case op: Convertop =>
        val v = stack.popOrFail()
        evalConvertop(op, v)
      case _ => throw new IllegalArgumentException(s"Expected numeric instruction, but got $inst")


  inline def evalTestop(op: Testop, v: V): V = op match
    case i32.Eqz => equ(v, i32ops.integerLit(0))
    case i64.Eqz => equ(v, i64ops.integerLit(0))

  inline def evalIUnop(op: IUnop, v: V): V = op match
    case i32.Clz => i32ops.countLeadingZeros(v)
    case i32.Ctz => i32ops.countTrailinZeros(v)
    case i32.Popcnt => i32ops.nonzeroBitCount(v)
    case i32.Extend8S =>
      val shift = i32ops.integerLit(24)
      i32ops.shiftRight(i32ops.shiftLeft(v, shift), shift)
    case i32.Extend16S =>
      val shift = i32ops.integerLit(16)
      i32ops.shiftRight(i32ops.shiftLeft(v, shift), shift)

    case i64.Clz => i64ops.countLeadingZeros(v)
    case i64.Ctz => i64ops.countTrailinZeros(v)
    case i64.Popcnt => i64ops.nonzeroBitCount(v)
    case i64.Extend8S =>
      val shift = i64ops.integerLit(56)
      i64ops.shiftRight(i64ops.shiftLeft(v, shift), shift)
    case i64.Extend16S =>
      val shift = i64ops.integerLit(48)
      i64ops.shiftRight(i64ops.shiftLeft(v, shift), shift)
    case i64.Extend32S =>
      val shift = i64ops.integerLit(32)
      i64ops.shiftRight(i64ops.shiftLeft(v, shift), shift)

  inline def evalIBinop(op: IBinop, v1: V, v2: V): V = op match
    case i32.Add => i32ops.add(v1, v2)
    case i32.Sub => i32ops.sub(v1, v2)
    case i32.Mul => i32ops.mul(v1, v2)
    case i32.DivS =>
      val v1IsMinValue = eqOps.equ(v1, i32ops.integerLit(Int.MinValue))
      val v2IsMinusOne = eqOps.equ(v2, i32ops.integerLit(-1))
      val isOverflow = i32ops.bitAnd(v1IsMinValue, v2IsMinusOne)
      wasmOps.branchOpsV.boolBranch(isOverflow) {
        Failure(IntegerOverflow, s"$v1 / $v2")
      } {
        i32ops.div(v1, v2)
      }

    case i32.DivU => i32ops.divUnsigned(v1, v2)
    case i32.RemS => i32ops.remainder(v1, v2)
    case i32.RemU => i32ops.remainderUnsigned(v1, v2)
    case i32.And => i32ops.bitAnd(v1, v2)
    case i32.Or => i32ops.bitOr(v1, v2)
    case i32.Xor => i32ops.bitXor(v1, v2)
    case i32.Shl => i32ops.shiftLeft(v1, i32ops.remainder(v2, i32ops.integerLit(32)))
    case i32.ShrS => i32ops.shiftRight(v1, i32ops.remainder(v2, i32ops.integerLit(32)))
    case i32.ShrU => i32ops.shiftRightUnsigned(v1, i32ops.remainder(v2, i32ops.integerLit(32)))
    case i32.Rotl => i32ops.rotateLeft(v1, i32ops.remainder(v2, i32ops.integerLit(32)))
    case i32.Rotr => i32ops.rotateRight(v1, i32ops.remainder(v2, i32ops.integerLit(32)))

    case i64.Add => i64ops.add(v1, v2)
    case i64.Sub => i64ops.sub(v1, v2)
    case i64.Mul => i64ops.mul(v1, v2)
    case i64.DivS =>
      val v1IsMinValue = eqOps.equ(v1, i64ops.integerLit(Long.MinValue))
      val v2IsMinusOne = eqOps.equ(v2, i64ops.integerLit(-1))
      val isOverflow = i32ops.bitAnd(v1IsMinValue, v2IsMinusOne)
      wasmOps.branchOpsV.boolBranch(isOverflow) {
        Failure(IntegerOverflow, s"$v1 / $v2")
      } {
        i64ops.div(v1, v2)
      }
    case i64.DivU => i64ops.divUnsigned(v1, v2)
    case i64.RemS => i64ops.remainder(v1, v2)
    case i64.RemU => i64ops.remainderUnsigned(v1, v2)
    case i64.And => i64ops.bitAnd(v1, v2)
    case i64.Or => i64ops.bitOr(v1, v2)
    case i64.Xor => i64ops.bitXor(v1, v2)
    case i64.Shl => i64ops.shiftLeft(v1, i64ops.remainder(v2, i64ops.integerLit(64)))
    case i64.ShrS => i64ops.shiftRight(v1, i64ops.remainder(v2, i64ops.integerLit(64)))
    case i64.ShrU => i64ops.shiftRightUnsigned(v1, i64ops.remainder(v2, i64ops.integerLit(64)))
    case i64.Rotl => i64ops.rotateLeft(v1, i64ops.remainder(v2, i64ops.integerLit(64)))
    case i64.Rotr => i64ops.rotateRight(v1, i64ops.remainder(v2, i64ops.integerLit(64)))

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
    case f32.Abs => f32ops.absolute(v)
    case f32.Neg => f32ops.negated(v)
    case f32.Sqrt => f32ops.sqrt(v)
    case f32.Ceil => f32ops.ceil(v)
    case f32.Floor => f32ops.floor(v)
    case f32.Trunc => f32ops.truncate(v)
    case f32.Nearest => f32ops.nearest(v)

    case f64.Abs => f64ops.absolute(v)
    case f64.Neg => f64ops.negated(v)
    case f64.Sqrt => f64ops.sqrt(v)
    case f64.Ceil => f64ops.ceil(v)
    case f64.Floor => f64ops.floor(v)
    case f64.Trunc => f64ops.truncate(v)
    case f64.Nearest => f64ops.nearest(v)

  inline def evalFBinop(op: FBinop, v1: V, v2: V): V = op match
    case f32.Add => f32ops.add(v1, v2)
    case f32.Sub => f32ops.sub(v1, v2)
    case f32.Mul => f32ops.mul(v1, v2)
    case f32.Div => f32ops.div(v1, v2)
    case f32.Min => f32ops.min(v1, v2)
    case f32.Max => f32ops.max(v1, v2)
    case f32.Copysign => f32ops.copysign(v1, v2)

    case f64.Add => f64ops.add(v1, v2)
    case f64.Sub => f64ops.sub(v1, v2)
    case f64.Mul => f64ops.mul(v1, v2)
    case f64.Div => f64ops.div(v1, v2)
    case f64.Min => f64ops.min(v1, v2)
    case f64.Max => f64ops.max(v1, v2)
    case f64.Copysign => f64ops.copysign(v1, v2)

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
    case i32.WrapI64 => convert_i64_i32(v, NilCC)
    case i32.TruncSF32 => convert_f32_i32(v, (config.Overflow.Fail && config.Bits.Signed))
    case i32.TruncUF32 => convert_f32_i32(v, (config.Overflow.Fail && config.Bits.Unsigned))
    case i32.TruncSF64 => convert_f64_i32(v, (config.Overflow.Fail && config.Bits.Signed))
    case i32.TruncUF64 => convert_f64_i32(v, (config.Overflow.Fail && config.Bits.Unsigned))
    case i32.ReinterpretF32 => convert_f32_i32(v, (config.Overflow.Allow && config.Bits.Raw))
    case i64.ExtendSI32 => convert_i32_i64(v, config.Bits.Signed)
    case i64.ExtendUI32 => convert_i32_i64(v, config.Bits.Unsigned)
    case i64.TruncSF32 => convert_f32_i64(v, (config.Overflow.Fail && config.Bits.Signed))
    case i64.TruncUF32 => convert_f32_i64(v, (config.Overflow.Fail && config.Bits.Unsigned))
    case i64.TruncSF64 => convert_f64_i64(v, (config.Overflow.Fail && config.Bits.Signed))
    case i64.TruncUF64 => convert_f64_i64(v, (config.Overflow.Fail && config.Bits.Unsigned))
    case i64.ReinterpretF64 => convert_f64_i64(v, (config.Overflow.Allow && config.Bits.Raw))

    case f32.DemoteF64 => convert_f64_f32(v, NilCC)
    case f32.ConvertSI32 => convert_i32_f32(v, config.Bits.Signed)
    case f32.ConvertUI32 => convert_i32_f32(v, config.Bits.Unsigned)
    case f32.ConvertSI64 => convert_i64_f32(v, config.Bits.Signed)
    case f32.ConvertUI64 => convert_i64_f32(v, config.Bits.Unsigned)
    case f32.ReinterpretI32 => convert_i32_f32(v, config.Bits.Raw)
    case f64.PromoteF32 => convert_f32_f64(v, NilCC)
    case f64.ConvertSI32 => convert_i32_f64(v, config.Bits.Signed)
    case f64.ConvertUI32 => convert_i32_f64(v, config.Bits.Unsigned)
    case f64.ConvertSI64 => convert_i64_f64(v, config.Bits.Signed)
    case f64.ConvertUI64 => convert_i64_f64(v, config.Bits.Unsigned)
    case f64.ReinterpretI64 => convert_i64_f64(v, config.Bits.Raw)

  inline def evalMiscop(op: Miscop, v: V): V = op match
    case i32.TruncSatSF32 => convert_f32_i32(v, (config.Overflow.JumpToBounds && config.Bits.Signed))
    case i32.TruncSatUF32 => convert_f32_i32(v, (config.Overflow.JumpToBounds && config.Bits.Unsigned))
    case i32.TruncSatSF64 => convert_f64_i32(v, (config.Overflow.JumpToBounds && config.Bits.Signed))
    case i32.TruncSatUF64 => convert_f64_i32(v, (config.Overflow.JumpToBounds && config.Bits.Unsigned))
    case i64.TruncSatSF32 => convert_f32_i64(v, (config.Overflow.JumpToBounds && config.Bits.Signed))
    case i64.TruncSatUF32 => convert_f32_i64(v, (config.Overflow.JumpToBounds && config.Bits.Unsigned))
    case i64.TruncSatSF64 => convert_f64_i64(v, (config.Overflow.JumpToBounds && config.Bits.Signed))
    case i64.TruncSatUF64 => convert_f64_i64(v, (config.Overflow.JumpToBounds && config.Bits.Unsigned))

  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => evalNumeric(i32.Const(0))
    case ValType.I64 => evalNumeric(i64.Const(0))
    case ValType.F32 => evalNumeric(f32.Const(0))
    case ValType.F64 => evalNumeric(f64.Const(0))