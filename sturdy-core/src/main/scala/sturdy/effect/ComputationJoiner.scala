package sturdy.effect

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

class DelegatingComputationJoinerWithSuper[A](other: Effectful, sup: ComputationJoiner[A])
  extends ComputationJoinerWithSuper[A](sup):

  val joiner = other.makeComputationJoiner[A]
  override def inbetween_(): Unit = joiner.inbetween()
  def retainOnlyFirst_(fRes: TrySturdy[A]): Unit = joiner.retainOnlyFirst(fRes)
  def retainOnlySecond_(gRes: TrySturdy[A]): Unit = joiner.retainOnlySecond(gRes)
  def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joiner.retainBoth(fRes, gRes)
