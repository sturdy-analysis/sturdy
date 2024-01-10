package sturdy.language.tutorial

import sturdy.effect.EffectStack
import sturdy.effect.failure.FailureKind

object ConstantInstances:

  enum Const:
    case C   // Constant
    case NC  // Not Constant

  import Const.*

  type TripleC = (Const,Const,Const)

  // Utility function to determine the result of a binary operation
  private def opResult(op1: Const, op2: Const): Const =
    (op1, op2) match
      case (C, C) => C
      case _      => NC

  private def invopResult(a1: Const, a2: Const, res:Const): TripleC
    = (a1, a2, res) match
      case (_, _, C) => (C, C, C)
      case (C, C, NC) => (C, C, C)
      case (_, _, NC) => (a1, a2, res)


  class ConstantUnifiable extends Unifiable[Const]:
      override def canUnify(v1: Const, v2: Const): Boolean = (v1, v2) match
        case (C, C) => true
        case _      => false

  class ConstantBackJoin extends BackJoin[Const]:
    override def join(v1: Const, v2: Const): Const = opResult(v1, v2)

  class ConstantInvertOps(using f: Failure, j: EffectStack) extends InvertOps[Const]:
    override def trueVal: Const = C
    override def falseVal: Const = NC
    override def topVal: Const = NC

    override def invConst(c: Int, a: Const): Boolean = a match
      case C => true
      case NC => false

    def invAdd(a1: Const, a2: Const, res: Const): TripleC = invopResult(a1,a2,res)
    def invSub(a1: Const, a2: Const, res: Const): TripleC = invopResult(a1, a2, res)
    def invMul(a1: Const, a2: Const, res: Const): TripleC = invopResult(a1, a2, res)
    def invDiv(a1: Const, a2: Const, res: Const): TripleC = invopResult(a1, a2, res)
    def invLt(a1: Const, a2: Const, res: Const): TripleC = invopResult(a1, a2, res)
    def invGt(a1: Const, a2: Const, res: Const): TripleC = invopResult(a1, a2, res)

  class ConstantNumericOps(using f: Failure, j: EffectStack) extends NumericOps[Const]:
    override def lit(i: Int): Const = C
    override def add(v1: Const, v2: Const): Const = opResult(v1, v2)
    override def sub(v1: Const, v2: Const): Const = opResult(v1, v2)
    override def mul(v1: Const, v2: Const): Const = opResult(v1, v2)
    override def div(v1: Const, v2: Const): Const = opResult(v1, v2)
    override def lt(v1: Const, v2: Const): Const = opResult(v1, v2)
    override def gt(v1: Const, v2: Const): Const = opResult(v1, v2)


  class ConstantWidener extends MyWiden[Const]