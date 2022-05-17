package sturdy.effect

import sturdy.{Executable, Executor}
import sturdy.values.Join

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class EffectStack(_effects: => List[Effectful]) extends ObservableJoin:
  private lazy val effects = _effects
  private def baseJoiner[A]: ComputationJoiner[A] = new ComputationJoiner {
    joinStart()
    override def inbetween(): Unit = joinSwitch()
    override def retainNone(): Unit = joinEnd()
    override def retainFirst(fRes: TrySturdy[A]): Unit = joinEnd()
    override def retainSecond(gRes: TrySturdy[A]): Unit = joinEnd()
    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joinEnd()
  }
  
  def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoiner {
    val joiners: Seq[ComputationJoiner[A]] = baseJoiner +: effects.flatMap(_.getComputationJoiner[A])
    override def inbetween(): Unit = joiners.foreach(_.inbetween())
    override def retainNone(): Unit = joiners.foreach(_.retainNone())
    override def retainFirst(fRes: TrySturdy[A]): Unit = joiners.foreach(_.retainFirst(fRes))
    override def retainSecond(gRes: TrySturdy[A]): Unit = joiners.foreach(_.retainSecond(gRes))
    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joiners.foreach(_.retainBoth(fRes, gRes))
  }

  final def joinComputations[A](f: Executable[A])(g: Executable[A]): (Executor, Join[A]) ?=> A = {
    val joiner = makeComputationJoiner[A]

    val gExecutor = if (f.executor eq g.executor) f.executor.fork else g.executor

    import concurrent.ExecutionContext.Implicits.global
    val futureF = Future(f.run)
    val futureG = Future(g.runWith(gExecutor))

    val triedF = TrySturdy(Await.result(futureF, Duration.Inf))
    val triedG = TrySturdy(Await.result(futureG, Duration.Inf))

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
      case (false, true) => joiner.retainFirst(triedF)
      case (true, true) => joiner.retainNone()
      case (_, false) => throw new MatchError(s"joinWithFailure: g must yield bottom but was $triedG")

    implicit val joinA: Join[A] = null.asInstanceOf[Join[A]]
    Join(triedF, triedG).get.getOrThrow
  }

  final def mapJoin[A, B](as: Iterable[A], f: A => B): (Executor, Join[B]) ?=> B = as.size match
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

  private final def mapJoinIt[A, B](as: Iterator[A], f: A => B): (Executor, Join[B]) ?=> B =
    val a = as.next()
    if (as.isEmpty)
      f(a)
    else {
      joinComputations(f(a))(mapJoinIt(as, f))
    }

  def copyState(other: EffectStack): Unit =
    this.effects.zip(other.effects).foreach {
      case (to, from) =>
        to.setState(from.getState.asInstanceOf[to.State])
    }

