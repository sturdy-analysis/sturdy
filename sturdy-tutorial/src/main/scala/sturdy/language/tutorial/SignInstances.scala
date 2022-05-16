package sturdy.language.tutorial

import sturdy.{AbstractlySound, IsSound, Soundness}
import sturdy.effect.{ComputationJoiner, EffectStack, SturdyFailure, TrySturdy}
import sturdy.values.{Abstractly, Changed, Combine, CombineMayMust, Finite, Join, MayMust, MaybeChanged, PartialOrder, Powerset, Structural, Unchanged, Widening, given}
import sturdy.data.{JOption, JOptionA, JOptionC, MakeJoined, MayJoin, WithJoin, joinComputations}
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.data.CombineUnit
import sturdy.data.JOptionA

import scala.collection.mutable.ListBuffer

/*
 * Integer values are abstracted by their sign.
 */
enum Sign:
  case Top
  case Neg
  case Zero
  case Pos
import Sign.*

given finiteSign: Finite[Sign] with {}
given structuralSign: Structural[Sign] with {}
given signPO: PartialOrder[Sign] with
  override def lteq(x: Sign, y: Sign): Boolean = (x,y) match
    case (_,Top) => true
    case (x, y) if (x == y) => true
    case _ => false

/* Joining and widening on sign values */
given CombineSign[W <: Widening]: Combine[Sign,W] with
  override def apply(v1: Sign, v2: Sign): MaybeChanged[Sign] =
    if (v1 == v2) then Unchanged(v1)
    else (v1,v2) match
      case (_,Top) => Unchanged(Top)
      case _ => Changed(Top)

given valuesAbstractly: Abstractly[Int, Sign] with
  override def abstractly(c: Int): Sign =
    if (c < 0) then Neg else if (c == 0) then Zero else Pos

/*
 * The numeric operations of the language on Sign values. Since divisions fail in case of a division by zero and we
 * might not be able to decide if the divisor is zero in the sign domain, we need to join computations in this case.
 * To this end the implementation requires an implicit EffectStack in scope (defined in the generic interpreter) in
 * order to correctly join the state of all effect components.
 */
class SignNumericOps(using f: Failure, j: EffectStack) extends NumericOps[Sign]:
  override def lit(i: Int): Sign =
    if (i < 0)
      Neg
    else if (i == 0)
      Zero
    else
      Pos

  override def add(v1: Sign, v2: Sign): Sign = (v1,v2) match
    case (x, Zero) => x
    case (Zero, x) => x
    case (Pos, Pos) => Pos
    case (Neg, Neg) => Neg
    case _ => Top

  override def sub(v1: Sign, v2: Sign): Sign = (v1,v2) match
    case (x, Zero) => x
    case (Zero, x) => x
    case (Pos, Neg) => Pos
    case (Neg, Pos) => Neg
    case _ => Top

  override def mul(v1: Sign, v2: Sign): Sign = (v1,v2) match
    case (x, Zero) => Zero
    case (Zero, x) => Zero
    case (Pos, Pos) => Pos
    case (Neg, Neg) => Pos
    case (Pos, Neg) => Neg
    case (Neg, Pos) => Neg
    case _ => Top

  override def div(v1: Sign, v2: Sign): Sign = v2 match
    case Zero => f.fail(DivisionByZero, s"$v1 / $v2")
    case Top => j.joinWithFailure(v1)(f.fail(DivisionByZero, s"$v1 / $v2"))
    case _ => mul(v1,v2)

  override def lt(v1: Sign, v2: Sign): Sign = (v1,v2) match
    case (Neg, Zero) => Pos
    case (Neg, Pos) => Pos
    case (Zero, Pos) => Pos
    case (Pos, Zero) => Zero
    case (Pos, Neg) => Zero
    case (Zero, Neg) => Zero
    case _ => Top

/*
 * Branching with a sign value as condition. In case we cannot decide the condition in the abstract domain we
 * need to join both computations of the thn and els continuations (so we need an EffectStack) and additionally
 * join the results of type R (so we need a Join[R]).
 */
class SignBranching[R](using EffectStack, Join[R]) extends Branching[Sign, R]:
  override def branch(v: Sign, thn: => R, els: => R): R =
    if (v == Zero) then els
    else if (v == Pos || v == Neg) then thn
    else joinComputations(thn)(els)

/*
 * An abstract store storing sign values. We treat strings a finite here, because in our toy language they encode
 * program variables, from which there may only be finitely many.
 */
class SignStore(using j: Join[MayMust[Sign]]) extends Store[Sign, WithJoin]:
  import MayMust.*
  protected var store: Map[String, MayMust[Sign]] = Map()
  override def read(name: String): JOptionA[Sign] =
    store.get(name) match
      case None => JOptionA.none
      case Some(Must(v)) => JOptionA.some(v)
      case Some(May(v)) => JOptionA.noneSome(v)
  override def write(name: String, v: Sign): Unit =
    weekUpdate(name, Must(v))

  private def weekUpdate(name: String, v: MayMust[Sign]): Unit =
    store.get(name) match
      case None => store += name -> v
      case Some(old) => j(old,v).ifChanged(store += name -> _)

  override type State = Map[String, MayMust[Sign]]
  override def getState: State = store
  override def setState(s: State): Unit = store = s

  private class SignStoreJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = store
    private var fStore: Map[String, MayMust[Sign]] = _

    override def inbetween(): Unit =
      fStore = store
      store = snapshot

    override def retainNone(): Unit =
      store = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      store = fStore

    override def retainSecond(gRes: TrySturdy[A]): Unit = ()

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      for ((name,v) <- fStore)
        weekUpdate(name,v)

  }

  def storeIsSound(cs: CStore): IsSound =
    val cMap = cs.getState
    // all keys in concrete store must be present in abstract store
    if (!cMap.keySet.subsetOf(store.keySet)) {
      val missing = cMap.flatMap { (k,v) =>
        if (store.contains(k))
          None
        else
          Some(k)
      }
      IsSound.NotSound(s"${classOf[SignStore].getName}: Expected all concrete keys to be contained, but $missing are missing in $store.")
    }
    // all concrete entries must be approximated by the corresponding abstract entries
    cMap.foreachEntry { (k,v) =>
      val subSound = Soundness.isSound(v, store(k))
      if (subSound.isNotSound)
        return subSound
    }
    // all "must" entries in the abstract store must be present in the concrete store
    val musts = store.filter {
      case (_,Must(_)) => true
      case _ => false
    }
    if (!musts.keySet.subsetOf(cMap.keySet)) {
      val missing = musts.flatMap { (k,v) =>
        if (cMap.contains(k))
          None
        else
          Some(k)
      }
      IsSound.NotSound(s"${classOf[SignStore].getName}: Expected all must entries to be contained in concrete store, but $missing are missing.")
    }
    IsSound.Sound

  given mustAbstractly[A,B](using a: Abstractly[A,B]): Abstractly[A, MayMust[B]] with
    override def abstractly(c: A): MayMust[B] = Must(a.abstractly(c))

/*
 * Abstract failures. We simply collect all possible failures in a list.
 */
case object AFailureCollectException extends SturdyFailure
class AFailure extends Failure:
  protected val failures: ListBuffer[(FailureKind,String)] = ListBuffer()
  override def fail(kind: FailureKind, msg: String): Nothing =
    failures += kind -> msg
    throw AFailureCollectException

  def fallible[A](f: => A): AFallible[A] =
    try {
      val res = f
      if (failures.isEmpty)
        AFallible.Unfailing(res)
      else
        AFallible.MaybeFailing(res, Powerset(failures.toSet))
    } catch {
      case AFailureCollectException => AFallible.Failing(Powerset(failures.toSet))
      case ex => throw ex
    }

  override type State = List[(FailureKind,String)]
  override def getState: List[(FailureKind,String)] = failures.toList
  override def setState(s: List[(FailureKind, String)]): Unit =
    failures.clear()
    failures ++= s