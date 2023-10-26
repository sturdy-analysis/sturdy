package sturdy.language.bytecode.generic

import org.opalj.br.BaseType
import sturdy.data.MayJoin
import sturdy.data.noJoin
import sturdy.effect.failure.Failure
import sturdy.values.booleans.BooleanBranching
import sturdy.values.config
import sturdy.values.convert.*
import sturdy.values.floating.*
import sturdy.values.integer.*
import org.opalj.br.instructions.*


import scala.Float.NaN


class GenericInterpreterNumerics[V]
  (bytecodeOps: BytecodeOps[V]):


  import bytecodeOps.*


  def evalNumericOp(inst: Instruction): V = inst match
    case inst: ICONST_M1.type =>
      i32ops.integerLit(inst.value)
    case inst: ICONST_0.type =>
      i32ops.integerLit(inst.value)
    case inst: ICONST_1.type =>
      i32ops.integerLit(inst.value)
    case inst: ICONST_2.type =>
      i32ops.integerLit(inst.value)
    case inst: ICONST_3.type =>
      i32ops.integerLit(inst.value)
    case inst: ICONST_4.type =>
      i32ops.integerLit(inst.value)
    case inst: ICONST_5.type =>
      i32ops.integerLit(inst.value)
    case inst: LCONST_0.type =>
      i64ops.integerLit(inst.value)
    case inst: LCONST_1.type =>
      i64ops.integerLit(inst.value)
    case inst: FCONST_0.type =>
      f32ops.floatingLit(inst.value)
    case inst: FCONST_1.type =>
      f32ops.floatingLit(inst.value)
    case inst: FCONST_2.type =>
      f32ops.floatingLit(inst.value)
    case inst: DCONST_0.type =>
      f64ops.floatingLit(inst.value)
    case inst: DCONST_1.type =>
      f64ops.floatingLit(inst.value)
    case inst: BIPUSH =>
      i32ops.integerLit(inst.value)
    case inst: SIPUSH =>
      i32ops.integerLit(inst.value)
    case inst: LoadInt =>
      i32ops.integerLit(inst.value)
    case inst: LoadLong =>
      i64ops.integerLit(inst.value)
    case inst: LoadFloat =>
      f32ops.floatingLit(inst.value)
    case inst: LoadDouble =>
      f64ops.floatingLit(inst.value)
  def evalNumericUnOp(inst: Instruction, v1: V): V = inst match
      case inst: INEG.type =>
        i32ops.sub(i32ops.integerLit(0), v1)
      case inst: LNEG.type =>
        i64ops.sub(i64ops.integerLit(0), v1)
      case inst: FNEG.type =>
        f32ops.negated(v1)
      case inst: DNEG.type =>
        f64ops.negated(v1)
  def evalNumericBinOp(inst: Instruction, v1: V, v2: V): V = inst match
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
        i32ops.div(v1, v2)
      case LDIV =>
        i64ops.div(v1, v2)
      case FDIV =>
        f32ops.div(v1, v2)
      case DDIV =>
        f64ops.div(v1, v2)
      case IREM =>
        i32ops.remainder(v1, v2)
      case LREM =>
        i64ops.remainder(v1, v2)
      case FREM =>
        val convertv1 = convert_f32_i32(v1, (config.Overflow.Fail && config.Bits.Signed))
        val convertv2 = convert_f32_i32(v2, (config.Overflow.Fail && config.Bits.Signed))
        val result = i32ops.remainder(convertv1, convertv2)
        convert_i32_f32(result, config.Bits.Signed)
      case DREM =>
        val convertv1 = convert_f64_i64(v1, (config.Overflow.Fail && config.Bits.Signed))
        val convertv2 = convert_f64_i64(v2, (config.Overflow.Fail && config.Bits.Signed))
        val result = i64ops.remainder(convertv1, convertv2)
        convert_i64_f64(result, config.Bits.Signed)
      case ISHL =>
        i32ops.shiftLeft(v1, v2)
      case LSHL =>
        i64ops.shiftLeft(v1, v2)
      case ISHR =>
        i32ops.shiftRight(v1, v2)
      case LSHR =>
        i64ops.shiftRight(v1, v2)
      case IUSHR =>
        i32ops.shiftRightUnsigned(v1, v2)
      case LUSHR =>
        i64ops.shiftRightUnsigned(v1, v2)
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
      case FCMPL =>
        val isLt = compareOps.lt(v1, v2)
        branchOpsV.boolBranch(isLt) {
          f32ops.floatingLit(-1)
        } {
          val isGt = compareOps.gt(v1, v2)
          branchOpsV.boolBranch(isGt) {
            f32ops.floatingLit(1)
          } {
            f32ops.floatingLit(0)
          }
        }
      case FCMPG =>
        val isLt = compareOps.lt(v1, v2)
        branchOpsV.boolBranch(isLt) {
          f32ops.floatingLit(-1)
        } {
          val isGt = compareOps.gt(v1, v2)
          branchOpsV.boolBranch(isGt) {
            f32ops.floatingLit(1)
          } {
            f32ops.floatingLit(0)
          }
        }
      case DCMPL =>
        val isLt = compareOps.lt(v1, v2)
        branchOpsV.boolBranch(isLt) {
          f64ops.floatingLit(-1)
        } {
          val isGt = compareOps.gt(v1, v2)
          branchOpsV.boolBranch(isGt) {
            f64ops.floatingLit(1)
          } {
            f64ops.floatingLit(0)
          }
        }
      case DCMPG =>
        val isLt = compareOps.lt(v1, v2)
        branchOpsV.boolBranch(isLt) {
          f64ops.floatingLit(-1)
        } {
          val isGt = compareOps.gt(v1, v2)
          branchOpsV.boolBranch(isGt) {
            f64ops.floatingLit(1)
          } {
            f64ops.floatingLit(0)
          }
        }
      case LCMP =>
        val isLt = compareOps.lt(v1, v2)
        branchOpsV.boolBranch(isLt) {
          i64ops.integerLit(-1)
        } {
          val isGt = compareOps.gt(v1, v2)
          branchOpsV.boolBranch(isGt) {
            i64ops.integerLit(1)
          } {
            i64ops.integerLit(0)
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
      convert_f32_i32(v, (config.Overflow.Fail && config.Bits.Signed))
    case F2L =>
      convert_f32_i64(v, (config.Overflow.Fail && config.Bits.Signed))
    case F2D =>
      convert_f32_f64(v, NilCC)
    case D2I =>
      convert_f64_i32(v, (config.Overflow.Fail && config.Bits.Signed))
    case D2L =>
      convert_f64_i64(v, (config.Overflow.Fail && config.Bits.Signed))
    case D2F =>
      convert_f64_f32(v, NilCC)
    case I2B =>
      ???
    case I2C =>
      ???
    case I2S =>
      ???

  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => evalNumericOp(ICONST_0)
    case ValType.I64 => evalNumericOp(LCONST_0)
    case ValType.F32 => evalNumericOp(FCONST_0)
    case ValType.F64 => evalNumericOp(DCONST_0)
    
  

