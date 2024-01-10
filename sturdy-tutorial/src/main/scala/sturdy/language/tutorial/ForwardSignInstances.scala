package sturdy.language.tutorial

import sturdy.{AbstractlySound, IsSound, Soundness}
import sturdy.effect.*
import sturdy.data.{*, given}
import sturdy.values.{*, given}
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.effect.store.CStore

import scala.collection.mutable.ListBuffer

/*
 * Integer values are abstracted by their sign.
 */
enum Sign:
  case TopSign
  case Neg
  case Zero
  case Pos

  override def toString: String = this match
    case TopSign => "⊤"
    case Neg => "-"
    case Zero => "0"
    case Pos => "+"

import Sign.*

given finiteSign: Finite[Sign] with {}
given structuralSign: Structural[Sign] with {}
given signPO: PartialOrder[Sign] with
  override def lteq(x: Sign, y: Sign): Boolean = (x,y) match
    case (_,TopSign) => true
    case (x, y) if (x == y) => true
    case _ => false

/* Joining and widening on sign values */
given CombineSign[W <: Widening]: Combine[Sign,W] with
  override def apply(v1: Sign, v2: Sign): MaybeChanged[Sign] =
    if (v1 == v2) then Unchanged(v1)
    else (v1, v2) match
      case (TopSign, _) => Unchanged(TopSign)
      case _ => Changed(TopSign)

given valuesAbstractly: Abstractly[Int, Sign] with
  override def apply(c: Int): Sign =
    if (c < 0) then Neg else if (c == 0) then Zero else Pos

/*
 * The numeric operations of the language on Sign values. Since divisions fail in case of a division by zero and we
 * might not be able to decide if the divisor is zero in the sign domain, we need to join computations in this case.
 * To this end the implementation requires an implicit EffectStack in scope (defined in the generic interpreter) in
 * order to correctly join the state of all effect components.
 */
class SignNumericOps(using f: Failure, j: EffectStack) extends NumericOps[Sign]:
  override def lit(i: Int): Sign =
    if (i < 0)
      Neg
    else if (i == 0)
      Zero
    else
      Pos

  override def add(v1: Sign, v2: Sign): Sign = (v1,v2) match
    case (x, Zero) => x
    case (Zero, x) => x
    case (Pos, Pos) => Pos
    case (Neg, Neg) => Neg
    case _ => TopSign

  override def sub(v1: Sign, v2: Sign): Sign = (v1,v2) match
    case (x, Zero) => x
    case (Zero, x) => x
    case (Pos, Neg) => Pos
    case (Neg, Pos) => Neg
    case _ => TopSign

  override def mul(v1: Sign, v2: Sign): Sign = (v1,v2) match
    case (x, Zero) => Zero
    case (Zero, x) => Zero
    case (Pos, Pos) => Pos
    case (Neg, Neg) => Pos
    case (Pos, Neg) => Neg
    case (Neg, Pos) => Neg
    case _ => TopSign

  override def div(v1: Sign, v2: Sign): Sign = v2 match
    case Zero => f.fail(Failures.DivisionByZero, s"$v1 / $v2")
    case TopSign => j.joinWithFailure(v1)(f.fail(Failures.DivisionByZero, s"$v1 / $v2"))
    case _ => mul(v1,v2)

  override def lt(v1: Sign, v2: Sign): Sign = (v1,v2) match
    case (Neg, Zero) => Pos
    case (Neg, Pos) => Pos
    case (Zero, Pos) => Pos
    case (Pos, Zero) => Zero
    case (Pos, Neg) => Zero
    case (Zero, Neg) => Zero
    case _ => TopSign

  override def gt(v1: Sign, v2: Sign): Sign = (v1, v2) match {
    case (TopSign, _) | (_, TopSign) => TopSign
    case (Zero, Neg) => Pos
    case (Neg, Neg) => TopSign
    case (Pos, Neg) => Pos
    case (Zero, Pos) => Zero
    case (Neg, Pos) => Zero
    case (Pos, Pos) => TopSign
    case (Pos, Zero) => Pos
    case (Zero, Zero) => Zero
    case (Neg, Zero) => Zero
  }




/*
 * Branching with a sign value as condition. In case we cannot decide the condition in the abstract domain we
 * need to join both computations of the thn and els continuations (so we need an EffectStack) and additionally
 * join the results of type R (so we need a Join[R]).
 */
class SignBranching[R](using EffectStack, Join[R]) extends Branching[Sign, R]:
  override def branch(v: Sign, thn: => R, els: => R): R =
    if (v == Zero) then els
    else if (v == Pos || v == Neg) then thn
    else joinComputations(thn)(els)


/*
 * Abstract failures. We simply collect all possible failures in a list.
 */
case object AFailureCollectException extends SturdyFailure
class CollectedFailures[K <: FailureKind](using Finite[K]) extends Failure, Monotone:
  protected var failureKinds: Set[FailureKind] = Set()
  protected val failures: ListBuffer[(FailureKind,String)] = ListBuffer()

  override def fail(kind: FailureKind, msg: String): Nothing =
    failureKinds += kind
    failures += ((kind, msg))
    throw AFailureCollectException

  def fallible[A](f: => A): AFallible[A] =
    try {
      val res = f
      if (failures.isEmpty)
        AFallible.Unfailing(res)
      else
        AFallible.MaybeFailing(res, Powerset(failures.toSet))
    } catch {
      case AFailureCollectException => AFallible.Failing(Powerset(failures.toSet))
      case recur: RecurrentCall => AFallible.Diverging(recur)
      case ex => throw ex
    }

  