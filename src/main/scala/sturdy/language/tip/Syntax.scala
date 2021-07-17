package sturdy.language.tip

import sturdy.util.Labeled

enum Exp extends Labeled:
  case RandomInt()
  case Var(name: String)
  case NumLit(n: Int)
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

enum Stm extends Labeled:
  case Assign(lhs: Assignable, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  case Block(body: Seq[Stm])

enum Assignable:
  case AVar(name: String)
  case ADeref(e: Exp)
  case AField(rec: String, field: String)
  case ADerefField(rec: Exp, field: String)

case class Function(name: String, params: Seq[String], locals: Seq[String], body: Stm, ret: Exp)

case class Program(funs: Seq[Function])
