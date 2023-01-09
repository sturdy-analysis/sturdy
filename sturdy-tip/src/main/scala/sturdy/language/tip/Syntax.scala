package sturdy.language.tip

import sturdy.util.Labeled
import sturdy.values.{Structural, Finite}

import cats.Monoid

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

  def fold[A](using f: Exp => A)(using m: Monoid[A]): A = this match
    case NumLit(_) => f(this)
    case Input() => f(this)
    case Var(_) => f(this)
    case Add(e1, e2) =>
      m.combine(f(this), m.combine(e1.fold, e2.fold))
    case Sub(e1, e2) => m.combine(f(this), m.combine(e1.fold, e2.fold))
    case Mul(e1, e2) => m.combine(f(this), m.combine(e1.fold, e2.fold))
    case Div(e1, e2) => m.combine(f(this), m.combine(e1.fold, e2.fold))
    case Gt(e1, e2) => m.combine(f(this), m.combine(e1.fold, e2.fold))
    case Eq(e1, e2) => m.combine(f(this), m.combine(e1.fold, e2.fold))
    case Call(fun, args) => m.combine(f(this), m.combine(fun.fold, m.combineAll(args.view.map(f))))
    case Alloc(e) => m.combine(f(this), e.fold)
    case VarRef(_) => f(this)
    case Deref(e) => m.combine(f(this), e.fold)
    case NullRef() => f(this)
    case Record(fields) => m.combine(f(this), m.combineAll(fields.view.map(kv => f(kv._2))))
    case FieldAccess(rec, field) => m.combine(f(this), rec.fold)

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

  def intLiterals: Set[Int] =
    fold(using e => e match
      case NumLit(n: Int) => Set(n)
      case _ => Set()
    )

  def assertCount: Int = 0

enum Stm extends Labeled:
  case Assign(lhs: Assignable, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  case Block(body: Seq[Stm])
  case Output(e: Exp)
  case Assert(e : Exp)
  case Error(e: Exp)

  def fold[A](using f: Stm => A, g: Exp => A)(using m: Monoid[A]): A = this match
    case Assign(_, e) => m.combine(f(this), e.fold)
    case If(c, t, e: Option[Stm]) => m.combine(f(this),
      m.combine(c.fold, m.combine(t.fold, if (e.nonEmpty) e.get.fold else m.empty)))
    case While(c, b) =>
      m.combine(f(this), m.combine(c.fold, b.fold))
    case Block(body) =>
      m.combine(f(this), m.combineAll(body.view.map(_.fold)))
    case Output(e) => m.combine(f(this), e.fold)
    case Assert(e) => m.combine(f(this), e.fold)
    case Error(e) => m.combine(f(this), e.fold)

  override def toString: String = this match
    case Assign(x, _) => s"Assign($x)@${this.label}"
    case If(c, t, e) => s"If($c)@${this.label}"
    case While(c, b) => s"While($c)@${this.label}"
    case Block(body) => s"Block@${this.label}"
    case Output(e) => s"Output@${this.label}"
    case Assert(e) => s"Assert@${this.label}"
    case Error(e) => s"Error@${this.label}"

  def intLiterals: Set[Int] =
    fold(using _ => Set(), _.intLiterals)

  def assertCount: Int = this match 
    case Assert(_) => 1
    case _ => 0

enum Assignable:
  case AVar(name: String)
  case ADeref(e: Exp)
  case AField(rec: String, field: String)
  case ADerefField(rec: Exp, field: String)

  def fold[A](using g: Exp => A)(using m: Monoid[A]): A = this match
    case ADeref(e) => g(e)
    case ADerefField(rec, _) => g(rec)
    case _ => m.empty

  def intLiterals: Set[Int] = this match
    case AVar(_) => Set()
    case ADeref(e) => e.intLiterals
    case AField(_, _) => Set()
    case ADerefField(rec, _) => rec.intLiterals
  
  def assertCount: Int = 0

case class Function(name: String, params: Seq[String], locals: Seq[String], body: Stm, ret: Exp):
  override def toString: String = s"function $name"
  def fold[A](using fun: Function => A, f: Stm => A, g: Exp => A)(using m: Monoid[A]): A =
    m.combine(fun(this), m.combine(body.fold, ret.fold))

  def intLiterals: Set[Int] = fold(using _ => Set(), _.intLiterals, _.intLiterals)
  def assertCount: Int = fold(using _ => 0, _.assertCount, _.assertCount)

case class Program(funs: Seq[Function]):
  def fold[A](using fun: Function => A, f: Stm => A, g: Exp => A)(using m: Monoid[A]): A =
    m.combineAll(funs.map(_.fold))

  def intLiterals: Set[Int] = fold(using _ => Set(), _.intLiterals, _.intLiterals)
  def assertCount: Int = fold(using _ => 0, _.assertCount, _.assertCount)

given StructuralFunction: Structural[Function] with {}
given FiniteFunction: Finite[Function] with {}