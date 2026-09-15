package sturdy.values.booleans

import sturdy.data.{*, given}
import sturdy.values.{Join, Topped, Widen}
import sturdy.effect.failure.{AssertionFailure, Failure}
import sturdy.effect.EffectStack

given ToppedBooleanSelection[B, R](using ops: BooleanSelection[B, R])(using Join[R]): BooleanSelection[Topped[B], R] with
  override def boolSelect(v: Topped[B], ifTrue: R, ifFalse: R): R = v match
    case Topped.Top => Join(ifTrue, ifFalse).get
    case Topped.Actual(b) => ops.boolSelect(b, ifTrue, ifFalse)

given ToppedBooleanBranching[B, R](using ops: BooleanBranching[B, R])(using EffectStack, Join[R]): BooleanBranching[Topped[B], R] with
  override def boolBranch(v: Topped[B], thn: => R, els: => R): R = v match
    case Topped.Top => joinComputations(thn)(els)
    case Topped.Actual(b) => ops.boolBranch(b, thn, els)

given ToppedBreakIf[B](using effectStack: EffectStack): BreakIf[Topped[Boolean]] with
  type State = Unit

  override def break(br: Unit => Unit): Unit = br(())

  override def breakIf(cond: Topped[Boolean])(break: State => Unit): Unit =
    cond match
      case Topped.Actual(true) => break(())
      case Topped.Actual(false) =>
      case Topped.Top => joinComputations { break(()) } { }

  override def assertCondition(cond: Topped[Boolean], state: State): Unit = {}

  override def joinClosingOver[Body](using Join[Body]): Join[(Body, State)] = (v1,v2) => Join(v1._1,v2._1).map((_,()))
  override def widenClosingOver[Body](using Widen[Body]): Widen[(Body, State)] = (v1,v2) => Widen(v1._1,v2._1).map((_,()))