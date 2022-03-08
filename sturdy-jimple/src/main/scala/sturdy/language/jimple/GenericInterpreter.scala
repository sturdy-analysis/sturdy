package sturdy.language.jimple

import sturdy.data.MayJoin
import sturdy.effect.callframe.CallFrame
import sturdy.effect.failure.Failure
import sturdy.effect.symboltable.SymbolTable
import sturdy.values.config
import sturdy.values.floating.FloatOps
import sturdy.values.integer.IntegerOps
import sturdy.values.integer.ConvertIntLong
import sturdy.values.types.TypeOfOps

trait ClassOps[C, V]:
  def classValue(c: C): V

trait GenericInterpreter[V, J[_] <: MayJoin[_]]:

  implicit def jvV: J[V]

  /* Value components */

  val intOps: IntegerOps[Int, V]
  val longOps: IntegerOps[Long, V]
  val floatOps: FloatOps[Float, V]
  val doubleOps: FloatOps[Double, V]
  val classOps: ClassOps[Container, V]
  val typeOfOps: TypeOfOps[V, Type, J]

  val convertIntLong: ConvertIntLong[V, V]

  import classOps.*
  import typeOfOps.*

  /* Effect components */

  val failure: Failure

  type CallFrameData = Unit
  val callFrame: CallFrame[CallFrameData, Identifier, V, J]

  val classTable: SymbolTable[Unit, String, Container, J]


  import failure.*

  def constant(c: Constant): V = c match
    case Constant.IntC(v) => longOps.integerLit(v)
    case Constant.LongC(v) => longOps.integerLit(v)
    case Constant.FloatC(v) => floatOps.floatingLit(v)
    case Constant.DoubleC(v) => doubleOps.floatingLit(v)

    case Constant.FloatInfinityC() => floatOps.floatingLit(Float.PositiveInfinity)
    case Constant.FloatNegInfinityC() => floatOps.floatingLit(Float.NegativeInfinity)
    case Constant.FloatNanC() => floatOps.floatingLit(Float.NaN)

    case Constant.InfinityC() => ???
    case Constant.NegInfinityC() => ???
    case Constant.NanC() => ???

    case Constant.StringC(v) => ???
    case Constant.NullC() => ???

  def evalImmediate(i: Immediate): V = i match
    case Immediate.ConstI(c) => constant(c)
    case Immediate.LocalI(l) => callFrame.getLocalByName(l.id).getOrElse(fail(UnboundLocal, l.id))
    case Immediate.ClassI(s) =>
      classTable.get((), s)
        .map(classOps.classValue)
        .getOrElse(fail(UnboundClass, s))

  def evalBinop(v1: V, v2: V, op: BinOp): V = op match {
    case BinOp.Add => typeOf[V](v1, v2) {
      case (Type.IntT(), Type.IntT()) => intOps.add(v1, v2)
      case (Type.IntT(), Type.LongT()) => longOps.add(convertIntLong(v1, config.Bits.Signed), v2)
      // TODO follow the Java language specification https://docs.oracle.com/javase/specs/jls/se17/html/jls-15.html#jls-15.18
    }
    case BinOp.And => ???
    case BinOp.Cmp => ???
    case BinOp.Cmpg => ???
    case BinOp.Cmpl => ???
    case BinOp.Div => ???
    case BinOp.Mul => ???
    case BinOp.Or => ???
    case BinOp.Rem => ???
    case BinOp.Shl => ???
    case BinOp.Shr => ???
    case BinOp.Sub => ???
    case BinOp.Ushr => ???
    case BinOp.Ushl => ???
    case BinOp.Xor => ???
  }

  def evalExp(e: Exp): V = e match
    case Exp.BinopE(i1, i2, op: BinOp) =>
      val v1 = evalImmediate(i1)
      val v2 = evalImmediate(i2)
      evalBinop(v1, v2, op)
    case Exp.ConditionE(i1, i2, op) => ???
    case Exp.CastE(t, i) => ???
    case Exp.InstanceOfE(i, ref) => ???
    case Exp.StaticInvokeE(s, l) => ???
    case Exp.InvokeE(t, i, s, l) => ???
    case Exp.NewArrayE(t, i) => ???
    case Exp.NewE(t) => ???
    case Exp.NewMultArrE(t, dims) => ???
    case Exp.UnopE(i, op) => ???





