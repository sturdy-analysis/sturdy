package sturdy.language.jimple

import sturdy.data.MayJoin
import sturdy.effect.callframe.CallFrame
import sturdy.effect.failure.Failure
import sturdy.effect.symboltable.SymbolTable
import sturdy.values.floating.FloatOps
import sturdy.values.integer.IntegerOps

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

  def evalExp(e: Exp): V = e match
    case Exp.BinopE(i1, i2, op) => ???
    case Exp.ConditionE(i1, i2, op) => ???
    case Exp.CastE(t, i) => ???
    case Exp.InstanceOfE(i, ref) => ???
    case Exp.StaticInvokeE(s, l) => ???
    case Exp.InvokeE(t, i, s, l) => ???
    case Exp.NewArrayE(t, i) => ???
    case Exp.NewE(t) => ???
    case Exp.NewMultArrE(t, dims) => ???
    case Exp.UnopE(i, op) => ???





