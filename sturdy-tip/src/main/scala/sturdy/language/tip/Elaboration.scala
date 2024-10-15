package sturdy.language.tip

import sturdy.gradual.{Elaboration, ElaborationOps}
import sturdy.language.tip
import sturdy.language.tip.*
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Stm.*
import sturdy.language.tip.abstractions.TipGradualLogger
import sturdy.values.integer
import sturdy.values.integer.{IntSign, NumericInterval}

trait TipElaborationOps[T] extends ElaborationOps[T, Exp]:
  protected def parse(source: String) =
    (whitespaces0 *> Parser.function)
      .parse(source)
      .fold(
        err =>
          throw new RuntimeException(s"Parse error at ${source.slice(err.failedAtOffset, err.failedAtOffset + 10)}: $err"),
        identity)
      ._2

  val abstractionFunction: Function
  val precisionFunction: Function
  final val checkFunction: Function =
    parse("""check(val, abs) {
            | assert(prec(alpha(val), abs));
            | return val;
            |}""".stripMargin)

class TipElaboration[T,V](gl: TipGradualLogger[T,V], eo: TipElaborationOps[T]) extends Elaboration[Program] {
  //var checkFuns = List
  override def elaborate(p: Program): Program = {
    val preamble = Seq(eo.abstractionFunction, eo.precisionFunction, eo.checkFunction)
    Program(preamble ++ p.funs.map(elaborateFunction))
  }
  def elaborateFunction(f: Function): Function = {
    Function(f.name, f.params, f.locals, elaborateStm(f.body), elaborateExp(f.ret))
  }

  def elaborateStm(s: Stm): Stm = s match{
    case Assign(lhs, e) => Assign(lhs, elaborateExp(e))
    case If(cond, thenn, els) => If(elaborateExp(cond), elaborateStm(thenn), els.map(elaborateStm))
    case Block(stms) => Block(stms.map(elaborateStm))
    case While(cond, body) => While(elaborateExp(cond), elaborateStm(body))
    case Output(e) => Output(elaborateExp(e))
    case Assert(e) => Assert(elaborateExp(e))
    case Error(e) => Error(elaborateExp(e))
  }
  def elaborateExp(exp: Exp): Exp = {
    val e = exp match
      case Exp.Add(e1, e2) => Exp.Add(elaborateExp(e1), elaborateExp(e2))
      case Exp.Sub(e1, e2) => Exp.Sub(elaborateExp(e1), elaborateExp(e2))
      case Exp.Mul(e1, e2) => Exp.Mul(elaborateExp(e1), elaborateExp(e2))
      case Exp.Div(e1, e2) => Exp.Div(elaborateExp(e1), elaborateExp(e2))
      case Exp.Gt(e1, e2) => Exp.Gt(elaborateExp(e1), elaborateExp(e2))
      case Exp.Eq(e1, e2) => Exp.Eq(elaborateExp(e1), elaborateExp(e2))
      case Exp.Call(fun, args) => Exp.Call(fun, args.map(elaborateExp))
      case Exp.Alloc(e) => Exp.Alloc(elaborateExp(e))
      case Exp.Deref(e) => Exp.Deref(elaborateExp(e))
      case Exp.Record(fields) => Exp.Record(fields.map{case (k,v) => (k, elaborateExp(v))})
      case Exp.FieldAccess(rec, field) => Exp.FieldAccess(elaborateExp(rec), field)
      case _ => exp
    gl.getCheck(exp.label).map { c =>
      Exp.Call(Exp.Var(eo.checkFunction.name), Seq(e, eo.abstractToExpr(c.unsafe)))
    }.getOrElse{e}
  }
}

given NumericIntervalTipElaborationOps: TipElaborationOps[NumericInterval[Int]] with {
  override def abstractToExpr(v: NumericInterval[Int]): Exp =
    Exp.Record(Seq(("low", Exp.NumLit(v.low)), ("high", Exp.NumLit(v.high))))

  override val abstractionFunction: tip.Function =
    parse(
      """alpha(x) {
        |  return {low: x, high: x};
        |}""".stripMargin)

  override val precisionFunction: tip.Function =
    parse(
      """prec(x, y) {
        |  return (y.low <= x.low + x.high <= y.high) > 1;
        |}""".stripMargin)
}

given SignTipElaborationOps: TipElaborationOps[IntSign] with {
  override def abstractToExpr(abstr: IntSign): Exp =
    abstr match {
      case integer.IntSign.TopSign
      => Exp.NumLit(3)
      case integer.IntSign.Neg
      => Exp.NumLit(-2)
      case integer.IntSign.NegOrZero
      => Exp.NumLit(-1)
      case integer.IntSign.Zero
      => Exp.NumLit(0)
      case integer.IntSign.ZeroOrPos
      => Exp.NumLit(1)
      case integer.IntSign.Pos
      => Exp.NumLit(2)
    }

  override val abstractionFunction: Function =
    parse(
      """alpha(x) {
        |   var r;
        |   if (0 > x)
        |     r = -1;
        |   else {
        |     if (x == 0)
        |       r = 0;
        |     else
        |       r = 1;
        |   }
        |   return r;
        |}""".stripMargin)

  override val precisionFunction: Function =
    parse(
      """prec(x, y) {
        | return ((y == 3) + (x == y) + (x > -1) * (y > 0) * (y > x) + (1 > x) * (0 > y) * (x > y)) > 0;
        |}""".stripMargin)
}

/**


e ::= e e | ..
u ::= n | b | (\x.e)
v ::= (ev u :: T)

ev = <T>

f(<int>1::Int)

def f(x: ?):
    (<Bool>x :: Bool()


def f(x: B => B, v: I):
    (f :: ? => ?) (v :: ?)


 (Bool => Bool :: ? => ?) (Int :: ?)

Top.instanceOf[B] -> Bool // warning
Bool.instanceOf[?] ->  Top // ok!

(Bool => Top) Top -> Top //warning

---------------------

(Bool => Bool :: ? => ?) (Int :: ?)

Top.instanceOf[B] -> Bool // warning
Bool.instanceOf[?] ->  Bool // ok!

(Bool => Top) Int -> Top //runtime error

--------------------------

(Bool => Bool :: ? => ?) Int

Top.instanceOf[B] -> Bool // warning
Bool.instanceOf[?] ->  Bool // ok!

(Bool => Top) Int -> Top //runtime error


def foo(f: ? => ?, x: Int):
    f(x)
foo((x:Bool) => x, 10)
 **/