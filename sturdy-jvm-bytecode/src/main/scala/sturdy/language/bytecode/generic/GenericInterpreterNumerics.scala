package sturdy.language.bytecode.generic

import sturdy.data.MayJoin
import sturdy.data.noJoin
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.values.config
import sturdy.values.convert.*
import sturdy.values.floating.*
import sturdy.values.integer.*
import org.opalj.br.instructions.*


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
  def evalNumericUnOP(inst: Instruction, v1: V): V = inst match
      case inst: INEG.type =>
        ???
      case inst: LNEG.type =>
        ???
      case inst: FNEG.type =>
        f32ops.negated(v1)
      case inst: DNEG.type =>
        f64ops.negated(v1)
  def evalNumericBinOP(inst: Instruction, v1: V, v2: V): V = inst match
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
        ???
      case DREM =>
        ???
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



