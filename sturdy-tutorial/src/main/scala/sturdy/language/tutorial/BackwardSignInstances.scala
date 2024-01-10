package sturdy.language.tutorial

import Sign.*
import sturdy.effect.EffectStack
import sturdy.values.{CombineMayMust, finitely}


///

import sturdy.{AbstractlySound, IsSound, Soundness}
import sturdy.effect.*
import sturdy.data.{*, given}
import sturdy.values.{*, given}
import sturdy.effect.failure.{AFallible, FailureKind}

object BackwardSignInstances:

  // For the Sign domain, it acts as an identity function
  class SignWidener extends MyWiden[Sign]

  class SignUnifiable extends Unifiable[Sign]:
    override def canUnify(v1: Sign, v2: Sign): Boolean =
      (v1, v2) match
        case (TopSign, _) => true
        case (_, TopSign) => true
        case (x, y) if x == y => true
        case _ => false

  class SignBackJoin extends BackJoin[Sign]:
    override def join(v1: Sign, v2: Sign): Sign =
      (v1, v2) match
        case (x, y) if x == y => x
        case _ => TopSign


  class SignInvertOps(using f: Failure, j: EffectStack) extends InvertOps[Sign]:
    val signNumericOps = new SignNumericOps(using f, j)
    override def trueVal: Sign = Pos
    override def falseVal: Sign = Zero
    override def topVal: Sign = TopSign

    override def invConst(c: Int, a: Sign): Boolean = a match
      case Neg => c < 0
      case Zero => c == 0
      case Pos => c > 0
      case TopSign => true

    override def invAdd(a1: Sign, a2: Sign, a3: Sign): (Sign, Sign, Sign) =
      val prunedRes = if a3 == TopSign then signNumericOps.add(a1, a2) else a3
      (a1, a2, prunedRes) match
        case (TopSign, Zero, Pos) => (Pos, Zero, Pos)
        case (Zero, TopSign, Pos) => (Zero, Pos, Pos)
        case (TopSign, Zero, Neg) => (Neg, Zero, Neg)
        case (Zero, TopSign, Neg) => (Zero, Neg, Neg)
        case (Zero, TopSign, Zero) => (Zero, Zero, Zero)
        case (TopSign, Zero, Zero) => (Zero, Zero, Zero)
        case _ => (a1, a2, prunedRes)

    override def invSub(a1: Sign, a2: Sign, a3: Sign): (Sign, Sign, Sign) =
      val prunedRes = if a3 == TopSign then signNumericOps.sub(a1, a2) else a3
      (a1, a2, prunedRes) match
        case (TopSign, Zero, Pos) => (Pos, Zero, Pos)
        case (Zero, TopSign, Pos) => (Zero, Neg, Pos)
        case (TopSign, Zero, Neg) => (Neg, Zero, Neg)
        case (Zero, TopSign, Neg) => (Neg, Pos, Neg)
        case (Zero, TopSign, Zero) => (Zero, Zero, Zero)
        case (TopSign, Zero, Zero) => (Zero, Zero, Zero)
        case _ => (a1, a2, prunedRes)

    override def invMul(a1: Sign, a2: Sign, a3: Sign): (Sign, Sign, Sign) =
      val prunedRes = if a3 == TopSign then signNumericOps.mul(a1, a2) else a3
      (a1, a2, a3) match
        case (Zero, TopSign, TopSign) => (Zero, TopSign, Zero)
        case (TopSign, Zero, TopSign) => (TopSign, Zero, TopSign)
        case (TopSign, Pos, Pos) => (Pos, Pos, Pos)
        case (Pos, TopSign, Pos) => (Pos, Pos, Pos)
        case (TopSign, Neg, Neg) => (Pos, Neg, Neg)
        case (Neg, TopSign, Neg) => (Neg, Pos, Neg)
        case (TopSign, Pos, Neg) => (Neg, Pos, Neg)
        case (Pos, TopSign, Neg) => (Pos, Neg, Neg)
        case (Neg, Pos, TopSign) => (Neg, Pos, Neg)
        case (Pos, Neg, TopSign) => (Pos, Neg, Neg)
        case (TopSign, Neg, Pos) => (Neg, Neg, Pos)
        case (Neg, TopSign, Pos) => (Neg, Neg, Pos)
        case _ => (TopSign, TopSign, TopSign)



    override def invDiv(a1: Sign, a2: Sign, a3: Sign): (Sign, Sign, Sign) = ???

    override def invLt(a1: Sign, a2: Sign, a3: Sign): (Sign, Sign, Sign) =
      val prunedRes = if a3 == TopSign then signNumericOps.lt(a1, a2) else a3
      (a1, a2, prunedRes) match
        case (Zero, TopSign, Pos) => (Zero, Pos, Pos)
        case (TopSign, Zero, Pos) => (Neg, Zero, Pos)
        case (TopSign, Pos, Zero) => (Pos, Pos, Zero)
        case (Neg, TopSign, Zero) => (Neg, Neg, Zero)
        case _ => (TopSign, TopSign, TopSign)

    override def invGt(a1: Sign, a2: Sign, a3: Sign): (Sign, Sign, Sign) =
      val prunedRes = if a3 == TopSign then signNumericOps.gt(a1, a2) else a3
      (a1, a2, prunedRes) match
        case (TopSign, Zero, Pos) => (Pos, Zero, Pos)
        case (Zero, TopSign, Pos) => (Zero, Neg, Pos)
        case (Pos, TopSign, Zero) => (Pos, Pos, Zero)
        case (TopSign, Neg, Zero) => (Neg, Neg, Zero)
        case _ => (TopSign, TopSign, TopSign)



  // You can add additional methods like invGt, invEq, etc., as required.
