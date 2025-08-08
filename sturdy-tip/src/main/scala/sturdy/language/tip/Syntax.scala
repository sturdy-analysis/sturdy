package sturdy.language.tip

import sturdy.util.Labeled
import sturdy.values.{Finite, Structural}
import cats.Monoid
import org.eclipse.collections.impl.factory.Multimaps

import scala.annotation.tailrec

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

enum Assignable:
  case AVar(name: String)
  case ADeref(e: Exp)
  case AField(rec: String, field: String)
  case ADerefField(rec: Exp, field: String)

  def fold[A](using g: Exp => A)(using m: Monoid[A]): A = this match
    case ADeref(e) => g(e)
    case ADerefField(rec, _) => g(rec)
    case _ => m.empty

case class Function(name: String, params: Seq[String], locals: Seq[String], body: Stm, ret: Exp):
  override def toString: String = s"function $name"
  def fold[A](using fun: Function => A, f: Stm => A, g: Exp => A)(using m: Monoid[A]): A =
    m.combine(fun(this), m.combine(body.fold, ret.fold))

  def loopVars: Set[String] =
    val loops = body.fold(using {case w: Stm.While => Set(w); case _ => Set()}, _ => Set())
    val loopAssigns = loops.flatMap(_.fold(using {case a: Stm.Assign => Set(a); case _ => Set()}, _ => Set()))
    val vars = loopAssigns.collect {
      case Stm.Assign(Assignable.AVar(v), _) => v
    }
    vars

given Ordering[Function] = (f1: Function, f2: Function) => f1.name.compareTo(f2.name)

case class Program(funs: Seq[Function]):
  def fold[A](using fun: Function => A, f: Stm => A, g: Exp => A)(using m: Monoid[A]): A =
    m.combineAll(funs.map(_.fold))

  def intLiterals: Set[Int] = fold(using _ => Set(), _ => Set(), {case Exp.NumLit(n) => Set(n); case _ => Set()})
  def assertions: Set[Stm.Assert] = fold(using _ => Set(), {case a: Stm.Assert => Set(a); case _ => Set()}, _ => Set())

  lazy val functions: Map[String, Function] = funs.map(f => f.name -> f).toMap
  /** The direct function dependencies. Functions that invoke first-class functions are assumed to depend on all functions in the program. */
  lazy val functionDeps: Map[String, Set[String]] =
    functions.view.mapValues { f =>
      val locals = f.params ++ f.locals
      val calls: Set[Exp.Call] = f.fold(using _ => Set(), _ => Set(), {case c: Exp.Call => Set(c); case _ => Set()})
      calls.flatMap {
        case Exp.Call(Exp.Var(g), _) if !locals.contains(g) => functions.get(g).map(_.name).toSeq
        case Exp.Call(_, _) => funs.map(_.name)
      }
    }.toMap

  private def addTransitive[A, B](s: Set[(A, B)]) =
    s ++ (for ((x1, y1) <- s; (x2, y2) <- s if y1 == x2) yield (x1, y2))
  @tailrec
  private def transitiveClosure[A, B](s: Set[(A, B)]): Set[(A, B)] = {
    val t = addTransitive(s)
    if (t.size == s.size) s else transitiveClosure(t)
  }
  lazy val functionDepsTransitive: Map[String, Set[String]] =
    val tuples = functionDeps.flatMap(kv => kv._2.map(to => kv._1 -> to)).toSet
    val closure = transitiveClosure(tuples).groupMap(_._1)(_._2)
//    val recs = funs.foreach(f => if (closure.get(f.name).exists(_.contains(f.name))) println(s"$f is recursive") else println(s"$f is NOT recursive"))
    closure
  def isRecursive(f: String): Boolean =
    functionDepsTransitive.get(f).exists(_.contains(f))



given StructuralFunction: Structural[Function] with {}
given FiniteFunction: Finite[Function] with {}