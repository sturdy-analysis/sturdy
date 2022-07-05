package sturdy.effect

import sturdy.values.{Combine, Join, MaybeChanged, Widening, Widen}
import sturdy.fix

import scala.collection.mutable.ListBuffer


class EffectStack(_effects: => List[Effect], _inEffects: PartialFunction[Any, List[Effect]] = PartialFunction.empty, _outEffects: PartialFunction[Any, List[Effect]] = PartialFunction.empty) extends fix.State, ObservableJoin:

  private lazy val effects = _effects
  private def inEffects(dom: Any): List[Effect] = _inEffects.applyOrElse(dom, _ => effects)
  private def outEffects(dom: Any): List[Effect] = _outEffects.applyOrElse(dom, _ => effects)

  final override type All = List[Any]
  final override type In = List[Any]
  final override type Out = List[Any]

  private inline def getEffectState(eff: List[Effect]): List[Any] =
    eff.map(_.getState)
  private def setEffectState(eff: List[Effect], st: List[Any]): Unit = {
    var effs = eff
    var s = st
    while (effs.nonEmpty) {
      val e = effs.head
      e.setState(s.head.asInstanceOf[e.State])
      effs = effs.tail
      s = s.tail
    }
  }
  private def joinEffectulState[W <: Widening](eff: List[Effect], comb: Effect => (Any, Any) => MaybeChanged[Any]): Combine[List[Any], W] = new Combine {
    override def apply(st1: List[Any], st2: List[Any]): MaybeChanged[List[Any]] =
      var effs = eff
      var s1 = st1
      var s2 = st2
      val res = ListBuffer[Any]()
      var changed = false
      while (effs.nonEmpty) {
        val e = effs.head
        comb(e)(s1.head, s2.head) match
          case MaybeChanged.Changed(a) =>
            res += a
            changed |= true
          case MaybeChanged.Unchanged(a) =>
            res += a
        effs = effs.tail
        s1 = s1.tail
        s2 = s2.tail
      }
      MaybeChanged(res.toList, changed)
  }

  override def getAllState: All = getEffectState(effects)
  override def getInState(dom: Any): In = getEffectState(inEffects(dom))
  override def getOutState(dom: Any): Out = getEffectState(outEffects(dom))
  override def setAllState(st: All): Unit = setEffectState(effects, st)
  override def setInState(dom: Any, in: In): Unit = setEffectState(inEffects(dom), in)
  override def setOutState(dom: Any, out: Out): Unit = setEffectState(outEffects(dom), out)

  override def joinIn(dom: Any): Join[In] = joinEffectulState(inEffects(dom), e => (a1, a2) => e.join(a1.asInstanceOf[e.State], a2.asInstanceOf[e.State]))
  override def widenIn(dom: Any): Widen[In] = joinEffectulState(inEffects(dom), e => (a1, a2) => e.widen(a1.asInstanceOf[e.State], a2.asInstanceOf[e.State]))
  override def joinOut(dom: Any): Join[Out] = joinEffectulState(outEffects(dom), e => (a1, a2) => e.join(a1.asInstanceOf[e.State], a2.asInstanceOf[e.State]))
  override def widenOut(dom: Any): Widen[Out] = joinEffectulState(outEffects(dom), e => (a1, a2) => e.widen(a1.asInstanceOf[e.State], a2.asInstanceOf[e.State]))

  private def baseJoiner[A]: ComputationJoiner[A] = new ComputationJoiner {
    joinStart()
    override def inbetween(): Unit = joinSwitch()
    override def retainNone(): Unit = joinEnd()
    override def retainFirst(fRes: TrySturdy[A]): Unit = joinEnd()
    override def retainSecond(gRes: TrySturdy[A]): Unit = joinEnd()
    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joinEnd()
  }
  
  def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoiner {
    val joiners: Seq[ComputationJoiner[A]] = baseJoiner +: effects.flatMap(_.makeComputationJoiner[A])
    override def inbetween(): Unit = joiners.foreach(_.inbetween())
    override def retainNone(): Unit = joiners.foreach(_.retainNone())
    override def retainFirst(fRes: TrySturdy[A]): Unit = joiners.foreach(_.retainFirst(fRes))
    override def retainSecond(gRes: TrySturdy[A]): Unit = joiners.foreach(_.retainSecond(gRes))
    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joiners.foreach(_.retainBoth(fRes, gRes))
  }

  final def joinComputations[A](f: => A)(g: => A): Join[A] ?=> A = {
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
  def localEffect(eff: Effect): EffectStack =
    new EffectStack(List(eff))

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