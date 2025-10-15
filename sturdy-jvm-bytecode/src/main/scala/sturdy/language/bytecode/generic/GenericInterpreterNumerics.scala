package sturdy.language.bytecode.generic

import sturdy.values.booleans.BooleanBranching
import sturdy.values.config
import sturdy.values.convert.*
import org.opalj.br.instructions.*

class GenericInterpreterNumerics[V, TypeRep](bytecodeOps: BytecodeOps[V, TypeRep]):

  import bytecodeOps.*

  def evalNumericOp(inst: Instruction): V = inst match
    case IConstInstruction(value) =>
      i32ops.integerLit(value)
    case LConstInstruction(value) =>
      i64ops.integerLit(value)
    case FConstInstruction(value) =>
      f32ops.floatingLit(value)
    case DConstInstruction(value) =>
      f64ops.floatingLit(value)
    case BIPUSH(value) =>
      i32ops.integerLit(value)
    case SIPUSH(value) =>
      i32ops.integerLit(value)
    case LoadInt(value) =>
      i32ops.integerLit(value)
    case LoadInt_W(value) =>
      i32ops.integerLit(value)
    case LoadLong(value) =>
      i64ops.integerLit(value)
    case LoadFloat(value) =>
      f32ops.floatingLit(value)
    case LoadFloat_W(value) =>
      f32ops.floatingLit(value)
    case LoadDouble(value) =>
      f64ops.floatingLit(value)

  def evalNumericUnOp(inst: Instruction, v1: V): V = inst match
    case INEG =>
      i32ops.sub(i32ops.integerLit(0), v1)
    case LNEG =>
      i64ops.sub(i64ops.integerLit(0), v1)
    case FNEG =>
      f32ops.negated(v1)
    case DNEG =>
      f64ops.negated(v1)

  // throws an arithmetic exception through the provided function if the 2nd operand is 0, performs the computation otherwise
  private def div0Checked(throwArithmeticException: () => Nothing)(mk0: 0 => V, op: (V, V) => V)(v1: V, v2: V): V =
    branchOpsV.boolBranch(eqOps.equ(v2, mk0(0))) {
      throwArithmeticException()
    } {
      op(v1, v2)
    }

  def evalNumericBinOp(throwArithmeticException: () => Nothing)(inst: Instruction, v1: V, v2: V): V = inst match
    case IADD =>
      i32ops.add(v1, v2)
    case LADD =>
      i64ops.add(v1, v2)
    case FADD =>
      f32ops.add(v1, v2)
    case DADD =>
      f64ops.add(v1, v2)
    case ISUB =>
      i32ops.sub(v1, v2)
    case LSUB =>
      i64ops.sub(v1, v2)
    case FSUB =>
      f32ops.sub(v1, v2)
    case DSUB =>
      f64ops.sub(v1, v2)
    case IMUL =>
      i32ops.mul(v1, v2)
    case LMUL =>
      i64ops.mul(v1, v2)
    case FMUL =>
      f32ops.mul(v1, v2)
    case DMUL =>
      f64ops.mul(v1, v2)
    case IDIV =>
      div0Checked(throwArithmeticException)(i32ops.integerLit, i32ops.div)(v1, v2)
    case LDIV =>
      div0Checked(throwArithmeticException)(i64ops.integerLit, i64ops.div)(v1, v2)
    case FDIV =>
      f32ops.div(v1, v2)
    case DDIV =>
      f64ops.div(v1, v2)
    case IREM =>
      div0Checked(throwArithmeticException)(i32ops.integerLit, i32ops.remainder)(v1, v2)
    case LREM =>
      div0Checked(throwArithmeticException)(i64ops.integerLit, i64ops.remainder)(v1, v2)
    case FREM =>
      f32ops.remainder(v1, v2)
    case DREM =>
      f64ops.remainder(v1, v2)
    case ISHL =>
      i32ops.shiftLeft(v1, v2)
    case LSHL =>
      i64ops.shiftLeft(v1, convert_i32_i64(v2, config.Bits.Signed))
    case ISHR =>
      i32ops.shiftRight(v1, v2)
    case LSHR =>
      i64ops.shiftRight(v1, convert_i32_i64(v2, config.Bits.Signed))
    case IUSHR =>
      i32ops.shiftRightUnsigned(v1, v2)
    case LUSHR =>
      i64ops.shiftRightUnsigned(v1, convert_i32_i64(v2, config.Bits.Signed))
    case IAND =>
      i32ops.bitAnd(v1, v2)
    case LAND =>
      i64ops.bitAnd(v1, v2)
    case IOR =>
      i32ops.bitOr(v1, v2)
    case LOR =>
      i64ops.bitOr(v1, v2)
    case IXOR =>
      i32ops.bitXor(v1, v2)
    case LXOR =>
      i64ops.bitXor(v1, v2)
    case inst@(FCMPL | FCMPG | DCMPL | DCMPG) =>
      val isGt = compareOps.gt(v1, v2)
      branchOpsV.boolBranch(isGt) {
        i32ops.integerLit(1)
      } {
        val isEq = eqOps.equ(v1, v2)
        branchOpsV.boolBranch(isEq) {
          i32ops.integerLit(0)
        } {
          val isLt = compareOps.lt(v1, v2)
          branchOpsV.boolBranch(isLt) {
            i32ops.integerLit(-1)
          } {
            // v1 or v2 must be NaN if this point is reached
            i32ops.integerLit:
              inst match
                case FCMPL | DCMPL => -1
                case FCMPG | DCMPG => 1
          }
        }
      }
    case LCMP =>
      val isLt = compareOps.lt(v1, v2)
      branchOpsV.boolBranch(isLt) {
        i32ops.integerLit(-1)
      } {
        val isGt = compareOps.gt(v1, v2)
        branchOpsV.boolBranch(isGt) {
          i32ops.integerLit(1)
        } {
          // must be equal if v1 is not less or greater than v2
          i32ops.integerLit(0)
        }
      }

  def evalConvertOp(inst: Instruction, v: V): V = inst match
    case I2L =>
      convert_i32_i64(v, config.Bits.Signed)
    case I2F =>
      convert_i32_f32(v, config.Bits.Signed)
    case I2D =>
      convert_i32_f64(v, config.Bits.Signed)
    case L2I =>
      convert_i64_i32(v, NilCC)
    case L2F =>
      convert_i64_f32(v, config.Bits.Signed)
    case L2D =>
      convert_i64_f64(v, config.Bits.Signed)
    case F2I =>
      convert_f32_i32(v, config.Overflow.Fail && config.Bits.Signed)
    case F2L =>
      convert_f32_i64(v, config.Overflow.Fail && config.Bits.Signed)
    case F2D =>
      convert_f32_f64(v, NilCC)
    case D2I =>
      convert_f64_i32(v, config.Overflow.Fail && config.Bits.Signed)
    case D2L =>
      convert_f64_i64(v, config.Overflow.Fail && config.Bits.Signed)
    case D2F =>
      convert_f64_f32(v, NilCC)
    case I2B =>
      convert_i32_i8(v, NilCC)
    case I2C =>
      convert_i32_u16(v, NilCC)
    case I2S =>
      convert_i32_i16(v, NilCC)
