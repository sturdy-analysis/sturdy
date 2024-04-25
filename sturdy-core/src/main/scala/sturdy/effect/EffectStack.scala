package sturdy.effect

import sturdy.values.{Combine, Join, MaybeChanged, Widen}
import sturdy.fix


class EffectStack(_effects: => Effect,
                  _inEffects: PartialFunction[Any, Effect] = PartialFunction.empty,
                  _outEffects: PartialFunction[Any, Effect] = PartialFunction.empty)
                  extends fix.State, ObservableJoin, Effect:

  private lazy val effects: Effect = _effects

  private def inEffects(dom: Any): Effect = _inEffects.applyOrElse(dom, _ => effects)
  private def outEffects(dom: Any): Effect = _outEffects.applyOrElse(dom, _ => effects)

  final override type All = Any
  final override type In = Any
  final override type Out = Any

  override def getAllState: All = effects.getState
  override def getInState(dom: Any): In = inEffects(dom).getState
  override def getOutState(dom: Any): Out = outEffects(dom).getState
  override def setAllState(st: All): Unit = effects.setState(st.asInstanceOf)
  override def setInState(dom: Any, in: In): Unit = inEffects(dom).setState(in.asInstanceOf)
  override def setOutState(dom: Any, out: Out): Unit = outEffects(dom).setState(out.asInstanceOf)

  override def joinIn(dom: Any): Join[In] = (in1: In, in2: In) => inEffects(dom).join(in1.asInstanceOf, in2.asInstanceOf).asInstanceOf
  override def widenIn(dom: Any): Widen[In] = (in1: In, in2: In) => inEffects(dom).widen(in1.asInstanceOf, in2.asInstanceOf).asInstanceOf
  override def joinOut(dom: Any): Join[Out] = (out1: Out, out2: Out) => outEffects(dom).join(out1.asInstanceOf, out2.asInstanceOf).asInstanceOf
  override def widenOut(dom: Any): Widen[Out] = (out1: Out, out2: Out) => outEffects(dom).widen(out1.asInstanceOf, out2.asInstanceOf).asInstanceOf


  final override type State = All
  override def getState: State = getAllState
  override def setState(st: State): Unit = setAllState(st)
  override def join: Join[State] = (state1: State, state2: State) => effects.join(state1.asInstanceOf, state2.asInstanceOf).asInstanceOf
  override def widen: Widen[State] = (state1: State, state2: State) => effects.widen(state1.asInstanceOf, state2.asInstanceOf).asInstanceOf

  private def baseJoiner[A]: ComputationJoiner[A] = new ComputationJoiner[A] {
    joinStart()
    override def inbetween(): Unit = joinSwitch()
    override def retainNone(): Unit = joinEnd()
    override def retainFirst(fRes: TrySturdy[A]): Unit = joinEnd()
    override def retainSecond(gRes: TrySturdy[A]): Unit = joinEnd()
    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joinEnd()
  }
  
  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] =
    for(joiner <- effects.makeComputationJoiner[A])
      yield(baseJoiner.compose(joiner))

  final def joinComputations[A](f: => A)(g: => A): Join[A] ?=> A = {
    val joiner = makeComputationJoiner[A].get

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
    val joiner = makeComputationJoiner[A].get

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

  final def joinFold[A, B](as: Iterable[A], f: A => B): Join[B] ?=> B = as.size match
    case 0 => throw new IllegalArgumentException
    case 1 => f(as.head)
    case 2 =>
      val Seq(a0, a1) = as.toSeq
      joinComputations(f(a0))(f(a1))
    case 3 =>
      val Seq(a0, a1, a2) = as.toSeq
      joinComputations(joinComputations(f(a0))(f(a1)))(f(a2))
    case 4 =>
      val Seq(a0, a1, a2, a3) = as.toSeq
      joinComputations(joinComputations(joinComputations(f(a0))(f(a1)))(f(a2)))(f(a3))
    case _ =>
      joinFoldIt(as.iterator, f)

  private final def joinFoldIt[A, B](as: Iterator[A], f: A => B): Join[B] ?=> B =
    val a = as.next()
    if (as.isEmpty)
      f(a)
    else {
      joinComputations(f(a))(joinFoldIt(as, f))
    }

object EffectStack:
  def apply(effects: Effect*): EffectStack =
    new EffectStack(EffectList(effects*))

  def localEffect(eff: Effect): EffectStack =
    new EffectStack(eff)

  def pureJoinFold[A, B](as: Iterable[A], f: A => B): Join[B] ?=> B = as.size match
    case 0 => throw new IllegalArgumentException
    case 1 => f(as.head)
    case 2 =>
      val Seq(a0, a1) = as.toSeq
      Join(f(a0), f(a1)).get
    case 3 =>
      val Seq(a0, a1, a2) = as.toSeq
      Join(Join(f(a0), f(a1)).get, f(a2)).get
    case 4 =>
      val Seq(a0, a1, a2, a3) = as.toSeq
      Join(Join(Join(f(a0), f(a1)).get, f(a2)).get, f(a3)).get
    case _ =>
      pureJoinFoldIt(as.iterator, f)

  final def pureJoinFoldIt[A, B](as: Iterator[A], f: A => B): Join[B] ?=> B =
    val a = as.next()
    if (as.isEmpty)
      f(a)
    else {
      Join(f(a), pureJoinFoldIt(as, f)).get
    }