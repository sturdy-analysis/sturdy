package sturdy.effect

import sturdy.values.{Combine, Join, MaybeChanged, StackWidening, Widen, Widening}

import scala.collection.immutable.ArraySeq
import scala.reflect.ClassTag

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
    
  override def join: Join[State] = combine((effect) => (s1, s2) => effect.join(s1.asInstanceOf, s2.asInstanceOf).asInstanceOf)
  override def widen: Widen[State] = combine((effect) => (s1, s2) => effect.widen(s1.asInstanceOf, s2.asInstanceOf).asInstanceOf)
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

  override def stackWiden: StackWidening[State] =
    (stack: List[State], call: State) =>
      var changed = false
      val joinedCall = effects.view.zipWithIndex.map {
        (effect, idx) =>
          val joined = effect.stackWiden(stack.map(_.apply(idx)).asInstanceOf, call(idx).asInstanceOf)
          changed ||= joined.hasChanged
          joined.get
      }

      MaybeChanged(ArraySeq.from(joinedCall), changed)

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(EffectListJoiner[A](effects))

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    for(effect <- effects.iterator;
        addr <- effect.addressIterator[Addr](valueIterator))
      yield(addr)