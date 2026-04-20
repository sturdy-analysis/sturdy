package sturdy.effect

/**
 * `ComputationJoiner` joins two effectful computations `f: => A` and `g: => A`.
 *
 * `ComputationJoiner` assumes to be called with the following workflow:
 *  1. Upon creation, `ComputationJoiner` snapshots the current state of an effect.
 *  1. Then the first computation `f` is executed.
 *  1. Afterwards `ComputationJoiner.inbetween(failed)` is called, where `failed == true` if `f` failed.
 *     Method `inbetween` saves the state of the effect after `f` and restores the snapshot taken before `f`.
 *  1. Then the second computation `g` is executed.
 *  1. Afterwards `retainNone`, `retainFirst`, `retainSecond`, or `retainBoth` is called, based on if none,
 *     only the first, only the second, or both computations successfully produced a result.
 */
trait ComputationJoiner[A]:
  // put before() code as part of the initializer
  /**
   * Method `inbetween` saves the state of the effect after executing the first computation and restores the initial snapshot.
   *
   * @param fFailed `true` if the first computation failed.
   */
  def inbetween(fFailed: Boolean): Unit

  /** Method `retainNone` is called if both computations failed. */
  def retainNone(): Unit

  /** Method `retainFirst` is called if only the first computation successfully computed a result.
   *
   * @param fRes the result of the first computation.
   */
  def retainFirst(fRes: TrySturdy[A]): Unit

  /** Method `retainSecond` is called if only the second computation successfully computed a result
   *
   * @param gRes the result of the second computation.
   */
  def retainSecond(gRes: TrySturdy[A]): Unit

  /** Method `retainBoth` is called if both computations successfully computed a result.
   *
   * @param fRes the result of the first computation.
   * @param gRes the result of the second computation.
   */
  def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit

  /** Optional method that may be called after `retainBoth`. Used to clean up after join of other effects. */
  def afterJoin(): Unit = {}

  def compose(snd: ComputationJoiner[A]): ComputationJoiner[A] =
    val fst = this
    new ComputationJoiner[A] {
      override def inbetween(fFailed: Boolean): Unit =
        fst.inbetween(fFailed)
        snd.inbetween(fFailed)

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

      override def afterJoin(): Unit =
        fst.afterJoin()
        snd.afterJoin()
    }


class EffectListJoiner[A](effects: Seq[Effect]) extends ComputationJoiner[A]:
  val joiners: Seq[ComputationJoiner[A]] = effects.flatMap(_.makeComputationJoiner[A])
  override def inbetween(fFailed: Boolean): Unit = joiners.foreach(_.inbetween(fFailed))
  override def retainNone(): Unit = joiners.foreach(_.retainNone())
  override def retainFirst(fRes: TrySturdy[A]): Unit = joiners.foreach(_.retainFirst(fRes))
  override def retainSecond(gRes: TrySturdy[A]): Unit = joiners.foreach(_.retainSecond(gRes))
  override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joiners.foreach(_.retainBoth(fRes, gRes))
  override def afterJoin(): Unit = joiners.foreach(_.afterJoin())

final class RetainBoth[A](joiner: ComputationJoiner[A]) extends ComputationJoiner[A]:
  override def inbetween(fFailed: Boolean): Unit = joiner.inbetween(fFailed)
  override def retainNone(): Unit = joiner.retainBoth(null, null)
  override def retainFirst(fRes: TrySturdy[A]): Unit = joiner.retainBoth(fRes, null)
  override def retainSecond(gRes: TrySturdy[A]): Unit = joiner.retainBoth(null, gRes)
  override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joiner.retainBoth(fRes, gRes)
  override def afterJoin(): Unit = joiner.afterJoin()
