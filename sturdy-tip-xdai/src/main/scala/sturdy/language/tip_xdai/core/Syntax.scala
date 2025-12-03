package sturdy.language.tip_xdai.core

import cats.Monoid
import org.eclipse.collections.impl.factory.Multimaps
import sturdy.language.tip_xdai.*
import sturdy.util.Labeled
import sturdy.values.{Finite, Structural}

import scala.annotation.tailrec

trait Exp extends Labeled

case class Input() extends Exp:
  override def toString: String = s"Input@${this.label}"
case class Var(name: String) extends Exp:
  override def toString: String = s"$name@${this.label}"

case class Eq(e1: Exp, e2: Exp) extends Exp:
  override def toString: String = s"Eq@${this.label}"
case class Call(fun: Exp, args: Seq[Exp]) extends Exp:
  override def toString: String = fun match
    case Var(fun) => s"Call($fun)@${this.label}"
    case _=> s"Call@${this.label}"



trait Stm extends Labeled

case class Assign(lhs: Assignable, e: Exp) extends Stm:
  override def toString: String = s"Assign($lhs@${this.label}"
case class If(cond: Exp, thn: Stm, els: Option[Stm]) extends Stm:
  override def toString: String = s"If($cond)@${this.label}"
case class While(cond: Exp, body: Stm) extends Stm:
  override def toString: String = s"While($cond)@${this.label}"
case class Block(body: Seq[Stm]) extends Stm:
  override def toString: String = s"Block@${this.label}"
case class Output(e: Exp) extends Stm:
  override def toString: String = s"Output@${this.label}"
case class Assert(e : Exp) extends Stm:
  override def toString: String = s"Assert@${this.label}"
case class Error(e: Exp) extends Stm:
  override def toString: String = s"Error@${this.label}"

trait Assignable
case class AVar(name: String) extends Assignable


case class Function(name: String, params: Seq[String], locals: Seq[String], body: Stm, ret: Exp):
  override def toString: String = s"function $name"

given Ordering[Function] = (f1: Function, f2: Function) => f1.name.compareTo(f2.name)

case class Program(funs: Seq[Function]):
  lazy val functions: Map[String, Function] = funs.map(f => f.name -> f).toMap


given StructuralFunction: Structural[Function] with {}
given FiniteFunction: Finite[Function] with {}