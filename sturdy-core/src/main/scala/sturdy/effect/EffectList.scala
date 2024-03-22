package sturdy.effect

import sturdy.values.{Combine, Join, MaybeChanged, Widen, Widening}

import scala.collection.immutable.ArraySeq

object EffectList:
  def apply(effects: Effect*): EffectList = new EffectList(ArraySeq.from(effects))

case class EffectList(effects: ArraySeq[Effect]) extends Effect:
  override type State = ArraySeq[Any]

  override def getState: State =
    effects.map[Any](_.getState)

  override def setState(st: State): Unit =
    effects.view.zip(st).foreach((effect,state) =>
      effect.setState(state.asInstanceOf)
    )

  override def mapState(st: State, f: [A] => A => A): State =
    val mappedStates = effects.view.zip(st).map((effect,state) => effect.mapState(state.asInstanceOf, f))
    ArraySeq(mappedStates)

  override def join: Join[ArraySeq[Any]] = combine((effect) => (s1, s2) => effect.join(s1.asInstanceOf, s2.asInstanceOf).asInstanceOf)
  override def widen: Widen[ArraySeq[Any]] = combine((effect) => (s1, s2) => effect.widen(s1.asInstanceOf, s2.asInstanceOf).asInstanceOf)
  def combine[W <: Widening](combineEffectState: Effect => (Any, Any) => MaybeChanged[Any]): Combine[State, W] =
    (states1: State, states2: State) =>
      var changed = false
      val joinedStates = effects.view.zip(states1.view.zip(states2)).map {
        case (effect, (state1, state2)) =>
          val joinedState = combineEffectState(effect)(state1, state2)
          changed ||= joinedState.hasChanged
          joinedState.get
      }

      MaybeChanged(ArraySeq.from(joinedStates), changed)

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A] {
    val joiners: Seq[ComputationJoiner[A]] = effects.flatMap(_.makeComputationJoiner[A])
    override def inbetween(): Unit = joiners.foreach(_.inbetween())
    override def retainNone(): Unit = joiners.foreach(_.retainNone())
    override def retainFirst(fRes: TrySturdy[A]): Unit = joiners.foreach(_.retainFirst(fRes))
    override def retainSecond(gRes: TrySturdy[A]): Unit = joiners.foreach(_.retainSecond(gRes))
    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joiners.foreach(_.retainBoth(fRes, gRes))
  })
