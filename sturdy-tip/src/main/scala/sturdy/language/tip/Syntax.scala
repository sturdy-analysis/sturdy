package sturdy.language.tip

import sturdy.util.Labeled
import sturdy.values.Structural

enum Exp extends Labeled:
  case NumLit(n: Int)
  case Input()
  case Var(name: String)
  case Add(e1: Exp, e2: Exp)
  case Sub(e1: Exp, e2: Exp)
  case Mul(e1: Exp, e2: Exp)
  case Div(e1: Exp, e2: Exp)
  case Gt(e1: Exp, e2: Exp)
  case Eq(e1: Exp, e2: Exp)
  case Call(fun: Exp, args: Seq[Exp])
  case Alloc(e: Exp)
  case VarRef(name: String)
  case Deref(e: Exp)
  case NullRef()
  case Record(fields: Seq[(String, Exp)])
  case FieldAccess(rec: Exp, field: String)

  def intLiterals: Set[Int] = this match
    case NumLit(n: Int) => Set(n)
    case Add(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Sub(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Mul(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Div(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Gt(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Eq(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Call(fun: Exp, args: Seq[Exp]) => fun.intLiterals ++ args.flatMap(_.intLiterals)
    case Alloc(e: Exp) => e.intLiterals
    case Deref(e: Exp) => e.intLiterals
    case Record(fields: Seq[(String, Exp)]) => fields.flatMap(f => f._2.intLiterals).toSet
    case FieldAccess(rec: Exp, field: String) => rec.intLiterals
    case _ => Set()

  override def toString: String = this match
    case NumLit(n) => s"$n@${this.label}"
    case Input() => s"Input@${this.label}"
    case Var(name) => s"$name@${this.label}"
    case Add(e1, e2) => s"Add@${this.label}"
    case Sub(e1, e2) => s"Sub@${this.label}"
    case Mul(e1, e2) => s"Mul@${this.label}"
    case Div(e1, e2) => s"Div@${this.label}"
    case Gt(e1, e2) => s"Gt@${this.label}"
    case Eq(e1, e2) => s"Eq@${this.label}"
    case Call(Var(fun), args) => s"Call($fun)@${this.label}"
    case Call(fun, args) => s"Call@${this.label}"
    case Alloc(e) => s"Alloc@${this.label}"
    case VarRef(name: String) => s"&$name@${this.label}"
    case Deref(e) => s"Deref@${this.label}"
    case NullRef() => s"Null@${this.label}"
    case Record(fields: Seq[(String, Exp)]) => s"Record@${this.label}"
    case FieldAccess(rec: Exp, field: String) => s"FieldAccess@${this.label}"


enum Stm extends Labeled:
  case Assign(lhs: Assignable, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  case Block(body: Seq[Stm])
  case Output(e: Exp)
  case Error(e: Exp)

  def intLiterals: Set[Int] = this match
    case Assign(_, e) => e.intLiterals
    case If(c, t, e) => c.intLiterals ++ t.intLiterals ++ e.map(_.intLiterals).getOrElse(Set())
    case While(c, b) => c.intLiterals ++ b.intLiterals
    case Block(body) => body.flatMap(_.intLiterals).toSet
    case Output(e) => e.intLiterals
    case Error(e) => e.intLiterals

  override def toString: String = this match
    case Assign(x, _) => s"Assign(x)@${this.label}"
    case If(c, t, e) => s"If(c)@${this.label}"
    case While(c, b) => s"While(c)@${this.label}"
    case Block(body) => s"Block@${this.label}"
    case Output(e) => s"Output@${this.label}"
    case Error(e) => s"Error@${this.label}"

enum Assignable:
  case AVar(name: String)
  case ADeref(e: Exp)
  case AField(rec: String, field: String)
  case ADerefField(rec: Exp, field: String)

case class Function(name: String, params: Seq[String], locals: Seq[String], body: Stm, ret: Exp):
  override def toString: String = s"function $name"
  def intLiterals: Set[Int] = body.intLiterals ++ ret.intLiterals

case class Program(funs: Seq[Function]):
  def intLiterals: Set[Int] = funs.flatMap(_.intLiterals).toSet


given Structural[Function] with {}