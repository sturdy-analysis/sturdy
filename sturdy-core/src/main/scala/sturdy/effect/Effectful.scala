package sturdy.effect

import sturdy.effect.Effectful.StarvedJoin
import sturdy.values.Join

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait Effectful extends ObservableJoin:
  type Joined[A] = Join[A] ?=> A

  final def joinThrowables(failA: SturdyThrowable, failB: SturdyThrowable): SturdyThrowable =
    if (failA == failB)
      failA
    else
      StarvedJoin(failA, failB)

  private var _fRes: TrySturdy[_] = _
  protected def fRes: TrySturdy[_] = _fRes

  def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoiner {
    joinStart()
    override def inbetween(): Unit = joinSwitch()
    override def retainNone(): Unit = joinEnd()
    override def retainFirst(fRes: TrySturdy[A]): Unit = joinEnd()
    override def retainSecond(gRes: TrySturdy[A]): Unit = joinEnd()
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

    (triedF.isBottom, triedG.isBottom) match
      case (false, false) => joiner.retainBoth(triedF, triedG)
      case (false, true) => joiner.retainFirst(triedF)
      case (true, false) => joiner.retainSecond(triedG)
      case (true, true) => joiner.retainNone()

    Join(triedF, triedG).get.getOrThrow
  }

  def joinWithFailure[A](f: => A)(g: => Nothing): A = {
    val joiner = makeComputationJoiner[A]

    val triedF = TrySturdy(f)
    joiner.inbetween()
    val triedG = TrySturdy[A](g)

    (triedF.isBottom, triedG.isBottom) match
      case (false, false) => joiner.retainBoth(triedF, triedG)
      case (false, true) => joiner.retainFirst(triedF)
      case (true, false) => joiner.retainSecond(triedG)
      case (true, true) => joiner.retainNone()

    implicit val joinA: Join[A] = null.asInstanceOf[Join[A]]
    Join(triedF, triedG).get.getOrThrow
  }

  final def mapJoin[A, B](as: Iterable[A], f: A => B): Joined[B] = as.size match
    case 0 => throw new IllegalArgumentException
    case 1 => f(as.head)
    case 2 =>
      val List(a0, a1) = as.toList
      joinComputations(f(a0))(f(a1))
    case 3 =>
      val List(a0, a1, a2) = as.toList
      joinComputations(joinComputations(f(a0))(f(a1)))(f(a2))
    case 4 =>
      val List(a0, a1, a2, a3) = as.toList
      joinComputations(joinComputations(joinComputations(f(a0))(f(a1)))(f(a2)))(f(a3))
    case _ =>
      mapJoinIt(as.iterator, f)

  private final def mapJoinIt[A, B](as: Iterator[A], f: A => B): Joined[B] =
    val a = as.next()
    if (as.isEmpty)
      f(a)
    else {
      joinComputations(f(a))(mapJoinIt(as, f))
    }


object Effectful:
  case class StarvedJoin(ex1: SturdyThrowable, ex2: SturdyThrowable) extends Exception(s"Starved Join with $ex1 and $ex2") with SturdyThrowable
//    override val isBottom: Boolean = false // ex1.isBottom && ex2.isBottom


