package sturdy.effect

trait ComputationJoiner[A]:
  // put before() code as part of the initializer
  def inbetween(): Unit
  def retainNone(): Unit
  def retainFirst(fRes: TrySturdy[A]): Unit
  def retainSecond(gRes: TrySturdy[A]): Unit
  def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit


abstract class ComputationJoinerWithSuper[A](sup: ComputationJoiner[A]) extends ComputationJoiner[A]:
  def inbetween_(): Unit
  final def inbetween(): Unit = {
    inbetween_()
    sup.inbetween()
  }
  def retainNone_(): Unit
  final def retainNone(): Unit = {
    retainNone_()
    sup.retainNone()
  }
  def retainFirst_(fRes: TrySturdy[A]): Unit
  final def retainFirst(fRes: TrySturdy[A]): Unit = {
    retainFirst_(fRes)
    sup.retainFirst(fRes)
  }
  def retainSecond_(gRes: TrySturdy[A]): Unit
  final def retainSecond(gRes: TrySturdy[A]): Unit = {
    retainSecond_(gRes)
    sup.retainSecond(gRes)
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
  override def retainNone_(): Unit = joiner.retainNone()
  def retainFirst_(fRes: TrySturdy[A]): Unit = joiner.retainFirst(fRes)
  def retainSecond_(gRes: TrySturdy[A]): Unit = joiner.retainSecond(gRes)
  def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joiner.retainBoth(fRes, gRes)
