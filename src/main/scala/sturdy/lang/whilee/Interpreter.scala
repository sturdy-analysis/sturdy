package sturdy.lang.whilee

import sturdy.lang.whilee.Syntax._

trait Interpreter[V] {
  def eval(a: Eval[V]): Expr => V = a.fixEval(rec => {
    case e@Var(x) => a.lookup(x, e.label)
    case e@BoolLit(b) => a.boolLit(b, e.label)
    case e@And(e1, e2) => a.and(rec(e1), rec(e2), e.label)
    case e@Or(e1, e2) => rec(
      Not(
        And(
          Not(e1) <@@ e.label,
          Not(e2) <@@ e.label)
          <@@ e.label)
        <@@ e.label)
    case Not(e) => a.not(rec(e), e.label)
    case e@NumLit(n) => a.numLit(n, e.label)
    case e@RandomNum() => a.randomNum(e.label)
    case e@Add(e1, e2) => a.add(rec(e1), rec(e2), e.label)
    case e@Sub(e1, e2) => a.sub(rec(e1), rec(e2), e.label)
    case e@Mul(e1, e2) => a.mul(rec(e1), rec(e2), e.label)
    case e@Div(e1, e2) => a.div(rec(e1), rec(e2), e.label)
    case e@Eq(e1, e2) => a.eq(rec(e1), rec(e2), e.label)
  })

  def run(a: Run[V] with RunFix[Statement, Unit] with Eval[V]): Statement => Unit =
    a.fix(rec => {
      case s@Assign(x, e) => a.assign(x, eval(a)(e), s.label)
      case s@If(cond, thn, els) => a.if_(eval(a)(cond), rec(thn), rec(els), s.label)
      case s@While(cond, body) => rec(
        If(cond,
          Block(List(body, s)) <@@ s.label,
          Block(Nil) <@@ s.label)
        <@@ s.label)
      case Block(body) => body.foldLeft(())((_,s) => rec(s))
    })
}
