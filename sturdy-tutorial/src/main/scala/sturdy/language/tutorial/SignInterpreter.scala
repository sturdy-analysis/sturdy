package sturdy.language.tutorial

import sturdy.effect.EffectStack
import sturdy.values.{Changed, Combine, Join, MaybeChanged, Unchanged, Widening}
import sturdy.data.{JOption, JOptionA, JOptionC, MakeJoined, WithJoin, joinComputations}

/*
 * We now implement a sign analysis. Integer values are abstracted by their sign.
 */
enum Sign:
  case Top
  case Neg
  case Zero
  case Pos
import Sign.*

/* Joining and widening on sign values */
given CombineSign[W <: Widening]: Combine[Sign,W] with
  override def apply(v1: Sign, v2: Sign): MaybeChanged[Sign] =
    if (v1 == v2) then Unchanged(v1)
    else (v1,v2) match
      case (_,Top) => Unchanged(Top)
      case _ => Changed(Top)

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
    case Top => j.joinComputations(v1)(f.fail(DivisionByZero, s"$v1 / $v2"))
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
    
class SignStore extends Store[Sign, WithJoin]:
  protected var store: Map[String, Sign] = Map()
  override def read(name: String): JOptionA[Sign] = ???
  override def write(name: String, v: Sign): Unit = ???
  
  override type State = Map[String, Sign]
  override def getState: Map[String, Sign] = ???
  override def setState(s: Map[String, Sign]): Unit = ???