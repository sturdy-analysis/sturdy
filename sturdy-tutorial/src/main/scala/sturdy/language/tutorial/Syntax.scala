package sturdy.language.tutorial

import sturdy.util.Labeled

enum Exp extends Labeled:
  case NumLit(n: Int)
  case Var(name: String)
  case Add(e1: Exp, e2: Exp)
  case Sub(e1: Exp, e2: Exp)
  case Mul(e1: Exp, e2: Exp)
  case Div(e1: Exp, e2: Exp)
  case Lt(e1: Exp, e2: Exp)
  case Gt(e1: Exp, e2: Exp)

  override def toString: String = this match
    case NumLit(n) => n.toString
    case Var(name) => name
    case Add(e1, e2) => s"${e1.toString} + ${e2.toString}"
    case Sub(e1, e2) => s"${e1.toString} - ${e2.toString}"
    case Mul(e1, e2) => s"${e1.toString} * ${e2.toString}"
    case Div(e1, e2) => s"${e1.toString} / ${e2.toString}"
    case Lt(e1, e2) => s"${e1.toString} < ${e2.toString}"
    case Gt(e1, e2) => s"${e1.toString} > ${e2.toString}"

  def isPresent(varName: String): Boolean = this match
    case NumLit(_) => false
    case Var(name) => varName == name
    case Add(e1, e2) => isPresentInEither(varName, e1, e2)
    case Sub(e1, e2) => isPresentInEither(varName, e1, e2)
    case Mul(e1, e2) => isPresentInEither(varName, e1, e2)
    case Div(e1, e2) => isPresentInEither(varName, e1, e2)
    case Gt(e1, e2) => isPresentInEither(varName, e1, e2)
    case Lt(e1, e2) => isPresentInEither(varName, e1, e2)

  private def isPresentInEither(varName: String, expr1: Exp, expr2: Exp): Boolean =
    expr1.isPresent(varName) || expr2.isPresent(varName)

  private def binOp(e1: Exp, e2: Exp)(op: (Exp, Exp) => Exp)(ov: String, nv: String): Exp =
    op(e1.transformVar(ov, nv), e2.transformVar(ov, nv))

  def transformVar(ov: String, nv: String): Exp = this match
    case Var(name) if name == ov => Var(nv)
    case Add(e1, e2) => binOp(e1, e2)(Add)(ov, nv)
    case Sub(e1, e2) => binOp(e1, e2)(Sub)(ov, nv)
    case Mul(e1, e2) => binOp(e1, e2)(Mul)(ov, nv)
    case Div(e1, e2) => binOp(e1, e2)(Div)(ov, nv)
    case Lt(e1, e2) => binOp(e1, e2)(Lt)(ov, nv)
    case Gt(e1, e2) => binOp(e1, e2)(Gt)(ov, nv)
    case _ => this


enum Stm extends Labeled:
  import Exp.*
  case Assign(name: String, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  case Block(body: Seq[Stm])

  override def toString: String = this match {
    case Assign(name, e) => s"$name = $e"
    case If(cond, thn, els) => els.fold(s"if ($cond) $thn")(e => s"if ($cond) $thn else $e")
    case While(cond, body) => s"while ($cond) \n$body"
    case Block(body) => body.mkString("{", ";\n ", "}")
  }

  def transform: Stm = this match
    case Assign(n, exp) if exp.isPresent(n) =>
      val tVar = s"tmp_$n"
      Block(Seq(
        Assign(tVar, Var(n)),
        Assign(n, exp.transformVar(n, tVar))
      ))
    case If(cond, thn, els) => If(cond, thn.transform, els.map(_.transform))
    case While(cond, body) => While(cond, body.transform)
    case Block(b) => Block(b.map(_.transform))
    case _ => this