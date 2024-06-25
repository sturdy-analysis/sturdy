package sturdy.utils

import org.scalactic.Equality
import org.scalatest.enablers.Containing
import org.scalatest.matchers.should.Matchers.{fail, should}
import org.scalatest.matchers.{MatchResult, Matcher}

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait TestIntervalOps[L, IV]:
  def constant(i: L): IV

  def interval(low: L, high: L): IV

  def contains(iv: IV, number: L): Boolean

  def equals(iv: IV, l: L, u: L): Boolean

given IntervalContaining[L, IV](using ivOps: TestIntervalOps[L, IV]): Containing[IV] with
  override def contains(interval: IV, element: Any): Boolean =
    element match
      case number: L => ivOps.contains(interval, number)
      case _ => false

  override def containsNoneOf(interval: IV, elements: collection.Seq[Any]): Boolean =
    elements.forall(! contains(interval, _))

  override def containsOneOf(interval: IV, elements: collection.Seq[Any]): Boolean =
    elements.exists(contains(interval, _))

given TryContaining[A](using containing: Containing[A]): Containing[Try[A]] with
  override def contains(container: Try[A], element: Any): Boolean =
    (container, element) match
      case (Success(cont), Success(elem)) =>
        containing.contains(cont, elem)
      case (Failure(ex1), Failure(ex2)) =>
        ex1.getClass == ex2.getClass
      case (_,_) =>
        false

  override def containsNoneOf(container: Try[A], elements: collection.Seq[Any]): Boolean =
    elements.forall(!contains(container, _))

  override def containsOneOf(container: Try[A], elements: collection.Seq[Any]): Boolean =
    elements.exists(contains(container, _))

given IntervalEquality[L, IV](using ivOps: TestIntervalOps[L, IV]): Equality[IV] with
  override def areEqual(iv: IV, other: Any): Boolean =
    other match
      case (l: L, u: L) => ivOps.equals(iv, l, u)
      case _ => false