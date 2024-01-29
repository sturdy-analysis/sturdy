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
  case Neg(e: Exp)
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
    case Neg(e) => m.combine(f(this),e.fold)
    case Call(fun, args) => m.combine(f(this), m.combine(fun.fold, m.combineAll(args.view.map(f))))
    case Alloc(e) => m.combine(f(this), e.fold)
    case VarRef(_) => f(this)
    case Deref(e) => m.combine(f(this), e.fold)
    case NullRef() => f(this)
    case Record(fields) => m.combine(f(this), m.combineAll(fields.view.map(kv => f(kv._2))))
    case FieldAccess(rec, field) => m.combine(f(this), rec.fold)

  override def toString: String = this match
    case NumLit(n) => s"$n"
    case Input() => "input()"
    case Var(name) => name
    case Add(e1, e2) => s"(${e1.toString} + ${e2.toString})"
    case Sub(e1, e2) => s"(${e1.toString} - ${e2.toString})"
    case Mul(e1, e2) => s"(${e1.toString} * ${e2.toString})"
    case Div(e1, e2) => s"(${e1.toString} / ${e2.toString})"
    case Gt(e1, e2) => s"(${e1.toString} > ${e2.toString})"
    case Eq(e1, e2) => s"(${e1.toString} == ${e2.toString})"
    case Neg(e)     => s"-(${e.toString})"
    case Call(Var(fun), args) =>
      val argsStr = args.map(_.toString).mkString(", ")
      s"$fun($argsStr)"
    case Call(fun, args) =>
      val argsStr = args.map(_.toString).mkString(", ")
      s"${fun.toString}($argsStr)"
    case Alloc(e) => s"alloc(${e.toString})"
    case VarRef(name) => s"&$name"
    case Deref(e) => s"*${e.toString}"
    case NullRef() => "null"
    case Record(fields) =>
      val fieldsStr = fields.map { case (name, exp) => s"$name: ${exp.toString}" }.mkString(", ")
      s"{ $fieldsStr }"
    case FieldAccess(rec, field) => s"${rec.toString}.$field"


  def intLiterals: Set[Int] =
    fold(using e => e match
      case NumLit(n: Int) => Set(n)
      case _ => Set()
    )

enum Stm extends Labeled:
  case Assign(lhs: Assignable, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  case Block(body: Seq[Stm])
  case Output(e: Exp)
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
    case Error(e) => m.combine(f(this), e.fold)

  override def toString: String = this match
    case Assign(x, e) => s"$x := $e"
    case If(c, t, None) => s"if ($c) {...}"
    case If(c, t, Some(e)) => s"if ($c) {...}"
    case While(c, b) => s"while ($c) {...}"
    case Block(body) =>
      val bodyStr = body.map(stm => s"  ${stm.toString}").mkString("\n")
      s"{\n$bodyStr\n}"
    case Output(e) => s"output($e)"
    case Error(e) => s"error($e)"


  def intLiterals: Set[Int] =
    fold(using _ => Set(), _.intLiterals)

enum Assignable:
  case AVar(name: String)
  case ADeref(e: Exp)
  case AField(rec: String, field: String)
  case ADerefField(rec: Exp, field: String)

  override def toString: String = this match
    case AVar(name) => name
    case ADeref(e) => s"*($e)"
    case AField(rec, field) => s"$rec.$field"
    case ADerefField(rec, field) => s"(*$rec).$field"


  def fold[A](using g: Exp => A)(using m: Monoid[A]): A = this match
    case ADeref(e) => g(e)
    case ADerefField(rec, _) => g(rec)
    case _ => m.empty

  def intLiterals: Set[Int] = this match
    case AVar(_) => Set()
    case ADeref(e) => e.intLiterals
    case AField(_, _) => Set()
    case ADerefField(rec, _) => rec.intLiterals

case class Function(name: String, params: Seq[String], locals: Seq[String], body: Stm, ret: Exp):
  override def toString: String = s"function $name"
  def fold[A](using fun: Function => A, f: Stm => A, g: Exp => A)(using m: Monoid[A]): A =
    m.combine(fun(this), m.combine(body.fold, ret.fold))

  def intLiterals: Set[Int] = fold(using _ => Set(), _.intLiterals, _.intLiterals)

case class Program(funs: Seq[Function]):
  def fold[A](using fun: Function => A, f: Stm => A, g: Exp => A)(using m: Monoid[A]): A =
    m.combineAll(funs.map(_.fold))

  def intLiterals: Set[Int] = fold(using _ => Set(), _.intLiterals, _.intLiterals)


given StructuralFunction: Structural[Function] with {}
given FiniteFunction: Finite[Function] with {}