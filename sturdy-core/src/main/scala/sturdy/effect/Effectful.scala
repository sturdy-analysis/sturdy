package sturdy.effect

import sturdy.effect.Effectful.StarvedJoin
import sturdy.effect.except.ExceptException
import sturdy.effect.failure.FailureException
import sturdy.fix.RecurrentCall
import sturdy.values.Join

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait SturdyException extends Exception:
  def isBottom: Boolean

enum TrySturdy[A]:
  case Success(a: A)
  case Failure(ex: SturdyException)
  def isSuccess: Boolean = this match
    case Success(_) => true
    case Failure(_) => false
  def isBottom: Boolean = this match
    case Success(_) => false
    case Failure(ex) => ex.isBottom
  def get: A = this match
    case Success(a) => a
    case Failure(ex) => throw ex
  def exception: SturdyException = this match
    case Success(_) => throw new MatchError(this)
    case Failure(ex) => ex
object TrySturdy:
  inline def apply[A](f: => A) =
    try Success(f) catch {
      case ex: SturdyException => Failure(ex)
      case ex => throw ex
    }

trait Effectful extends ObservableJoin:
//  type JoinExceptions
  type Joined[A] = Join[A] ?=> A

  final def joinThrowables(failA: SturdyException, failB: SturdyException): SturdyException =
    if (failA == failB)
      failA
    else
      StarvedJoin(failA, failB)

  private var _fRes: TrySturdy[_] = _
  protected def fRes: TrySturdy[_] = _fRes

  def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoiner {
    joinStart()
    override def inbetween(): Unit = joinSwitch()
    override def retainOnlyFirst(fRes: TrySturdy[A]): Unit = joinEnd()
    override def retainOnlySecond(gRes: TrySturdy[A]): Unit = joinEnd()
    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joinEnd()
  }

  /* This is the default join for pure computations f and g.
   * Subclasses must override join to join effects and call super.join
   */
  final def joinComputations[A](f: => A)(g: => A): Joined[A] = {
    val joiner = makeComputationJoiner[A]

    val triedF = TrySturdy(f)
    joiner.inbetween()
    val triedG = TrySturdy(g)

    if (triedF.isBottom) {
      if (!triedG.isBottom) {
        joiner.retainOnlySecond(triedG)
        triedG.get
      } else {
        triedF.get
      }
    } else if (triedG.isBottom) {
      joiner.retainOnlyFirst(triedF)
      triedF.get
    } else {
      joiner.retainBoth(triedF, triedG)
      (triedF, triedG) match
        case (TrySturdy.Failure(failA), TrySturdy.Failure(failB)) => throw joinThrowables(failA, failB)
        case (TrySturdy.Success(aF), TrySturdy.Success(aG)) => Join(aF, aG).get
        case (TrySturdy.Success(aF), _) => aF
        case (_, TrySturdy.Success(aG)) => aG
    }
  }

  def joinWithFailure[A](f: => A)(g: => Nothing): A = {
    val joiner = makeComputationJoiner[A]

    val triedF = TrySturdy(f)
    joiner.inbetween()
    val triedG = TrySturdy(g).asInstanceOf[TrySturdy[A]]

    if (triedF.isBottom) {
      if (!triedG.isBottom) {
        joiner.retainOnlySecond(triedG)
        triedG.get
      } else {
        triedF.get
      }
    } else if (triedG.isBottom) {
      joiner.retainOnlyFirst(triedF)
      triedF.get
    } else {
      joiner.retainBoth(triedF, triedG)
      val failB = triedG.exception
      triedF match
        case TrySturdy.Failure(failA) => throw joinThrowables(failA, failB)
        case TrySturdy.Success(aF) => aF
    }
  }

  final def joinComputationsIterable[A](as: IterableOnce[() => A]): Joined[A] =
    joinComputationsIt(as.iterator)

  private final def joinComputationsIt[A](as: Iterator[() => A]): Joined[A] =
    val next = as.next()
    if (as.isEmpty)
      next()
    else {
      joinComputations(next())(joinComputationsIt(as))
    }

object Effectful:
  def join[A](f: => A)(g: => A)(using j: Effectful): Join[A] ?=> A =
    j.joinComputations(f)(g)
  case class StarvedJoin(ex1: SturdyException, ex2: SturdyException) extends Exception(s"Starved Join with $ex1 and $ex2") with SturdyException:
    override val isBottom: Boolean = ex1.isBottom && ex2.isBottom


trait ComputationJoiner[A]:
  // put before() code as part of the initializer
  def inbetween(): Unit
  def retainOnlyFirst(fRes: TrySturdy[A]): Unit
  def retainOnlySecond(gRes: TrySturdy[A]): Unit
  def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit

abstract class ComputationJoinerWithSuper[A](sup: ComputationJoiner[A]) extends ComputationJoiner[A]:
  def inbetween_(): Unit
  final def inbetween(): Unit = {
    inbetween_()
    sup.inbetween()
  }
  def retainOnlyFirst_(fRes: TrySturdy[A]): Unit
  final def retainOnlyFirst(fRes: TrySturdy[A]): Unit = {
    retainOnlyFirst_(fRes)
    sup.retainOnlyFirst(fRes)
  }
  def retainOnlySecond_(gRes: TrySturdy[A]): Unit
  final def retainOnlySecond(gRes: TrySturdy[A]): Unit = {
    retainOnlySecond_(gRes)
    sup.retainOnlySecond(gRes)
  }
  def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit
  final def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = {
    retainBoth_(fRes, gRes)
    sup.retainBoth(fRes, gRes)
  }