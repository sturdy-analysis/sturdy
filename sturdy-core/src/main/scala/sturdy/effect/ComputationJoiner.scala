package sturdy.effect

trait ComputationJoiner[A]:
  // put before() code as part of the initializer
  def inbetween(): Unit
  def retainNone(): Unit
  def retainFirst(fRes: TrySturdy[A]): Unit
  def retainSecond(gRes: TrySturdy[A]): Unit
  def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit
  
  def compose(snd: ComputationJoiner[A]): ComputationJoiner[A] =
    val fst = this
    new ComputationJoiner[A] {
      override def inbetween(): Unit = 
        fst.inbetween()
        snd.inbetween()
      override def retainNone(): Unit = 
        fst.retainNone()
        snd.retainNone()
      override def retainFirst(fRes: TrySturdy[A]): Unit = 
        fst.retainFirst(fRes)
        snd.retainFirst(fRes)
      override def retainSecond(gRes: TrySturdy[A]): Unit =
        fst.retainSecond(gRes)
        snd.retainSecond(gRes)
      override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
        fst.retainBoth(fRes, gRes)
        snd.retainBoth(fRes, gRes)
    }

//
//abstract class ComputationJoinerWithSuper[A](sup: ComputationJoiner[A]) extends ComputationJoiner[A]:
//  def inbetween(): Unit
//  final def inbetween(): Unit = {
//    inbetween()
//    sup.inbetween()
//  }
//  def retainNone(): Unit
//  final def retainNone(): Unit = {
//    retainNone()
//    sup.retainNone()
//  }
//  def retainFirst(fRes: TrySturdy[A]): Unit
//  final def retainFirst(fRes: TrySturdy[A]): Unit = {
//    retainFirst(fRes)
//    sup.retainFirst(fRes)
//  }
//  def retainSecond(gRes: TrySturdy[A]): Unit
//  final def retainSecond(gRes: TrySturdy[A]): Unit = {
//    retainSecond(gRes)
//    sup.retainSecond(gRes)
//  }
//  def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit
//  final def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = {
//    retainBoth(fRes, gRes)
//    sup.retainBoth(fRes, gRes)
//  }
//
//class DelegatingComputationJoinerWithSuper[A](other: Effect, sup: ComputationJoiner[A])
//  extends ComputationJoinerWithSuper[A](sup):
//
//  val joiner = other.makeComputationJoiner[A]
//  override def inbetween(): Unit = joiner.inbetween()
//  override def retainNone(): Unit = joiner.retainNone()
//  def retainFirst(fRes: TrySturdy[A]): Unit = joiner.retainFirst(fRes)
//  def retainSecond(gRes: TrySturdy[A]): Unit = joiner.retainSecond(gRes)
//  def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joiner.retainBoth(fRes, gRes)
