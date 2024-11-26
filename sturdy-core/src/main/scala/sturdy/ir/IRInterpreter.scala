package sturdy.ir

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Join, MaybeChanged, Structural, Topped}
import sturdy.values.booleans.{BooleanBranching, ConcreteBooleanBranching, ToppedBooleanBranching}
import sturdy.values.functions.IRFunctionOperator
import sturdy.values.integer.{ConcreteIntegerOps, IRIntegerOperator, IntegerOps, ToppedIntegerOps}
import sturdy.values.ordering.{ConcreteOrderingOps, EqOps, IREqualityOperator, IROrderingOperator, OrderingOps, StructuralEqOps, ToppedCertainEqOps, ToppedCertainOrderingOps, ToppedUncertainOrderingOps}

import scala.annotation.tailrec


case class IRValue(c: Any)

abstract class IRInterpreter(val externals: Map[String, IRValue]) {

  type VInt
  type VBool

  var feedbackStore: Map[IR_UID, IRValue] = Map()

  val integerOps: IntegerOps[Int, VInt]
  val orderingOps: OrderingOps[VInt, VBool]
  val integerEqOps: EqOps[VInt, VBool]
  val booleanBranching: BooleanBranching[VBool, IRValue]

  def interpret(ir: IR): IRValue = ir match
    case IR.Unknown() => ???
    case IR.External(name) => externals(name)
    case IR.Const(c) => c match
      case i: Integer => IRValue(integerOps.integerLit(i))
      case b: Boolean => IRValue(b)
      case _ => ???

    case IR.Op(op, args) => IRValue(op match
      case operator: IREqualityOperator =>
        val v1 = interpret(args.head).c.asInstanceOf[VInt]
        val v2 = interpret(args(1)).c.asInstanceOf[VInt]
        operator match
          case IREqualityOperator.EQ => integerEqOps.equ(v1, v2)
          case IREqualityOperator.NEQ => integerEqOps.neq(v1, v2)

      case operator: IRFunctionOperator => operator match
        case IRFunctionOperator.CALL(invoke) => ???

      case operator: IRIntegerOperator =>
        val v1 = interpret(args.head).c.asInstanceOf[VInt]
        val v2 = interpret(args(1)).c.asInstanceOf[VInt]
        operator match
          case IRIntegerOperator.ADD => integerOps.add(v1, v2)
          case IRIntegerOperator.SUB => integerOps.sub(v1, v2)
          case IRIntegerOperator.MUL => integerOps.mul(v1, v2)
          case IRIntegerOperator.DIV => integerOps.div(v1, v2)


      case operator: IROrderingOperator =>
        val v1 = interpret(args.head).c.asInstanceOf[VInt]
        val v2 = interpret(args(1)).c.asInstanceOf[VInt]
        operator match
          case IROrderingOperator.LT => orderingOps.lt(v1, v2)
          case IROrderingOperator.LE => orderingOps.le(v1, v2)
      case _ => ???
    )

    case IR.Select(cond, thn, els) =>
      booleanBranching.boolBranch(interpret(cond).c.asInstanceOf[VBool]) {interpret(thn)} {interpret(els)}

    case IR.Join(left, right) => ???
    case IR.Feedback(init, Some(cond), Some(loop)) => feedbackStore.get(ir.uid) match
      case Some(value) => value
      case None =>
        feedbackStore += ir.uid -> interpret(init)
        interpretFeedback(ir.uid, cond, loop)

    case IR.Cast(ir, check) =>
      val v = interpret(ir)
      IRValue(check.asInstanceOf[IRCheck[Any]].assert(v.c))

// TODO deactivated tailrecursion to trigger stack overvflows during testing
//  @tailrec
  private final def interpretFeedback(uid: IR_UID, cond: IR, loop: IR): IRValue = {
    val v = interpret(cond)
    v match
      case IRValue(false | 0) => feedbackStore(uid)
      case _ =>
        feedbackStore += uid -> interpret(loop)
        val v = interpretFeedback(uid, cond, loop)
        v
  }

}

class IRInterpreterConcrete(externals: Map[String, IRValue]) extends IRInterpreter(externals: Map[String, IRValue]) {

  override type VInt = Int
  override type VBool = Boolean

  implicit val failure : Failure = ???

  given Structural[Int] with {}

  override val integerOps: IntegerOps[Int, Int] = new ConcreteIntegerOps
  override val orderingOps: OrderingOps[Int, Boolean] = new ConcreteOrderingOps[Int]
  override val integerEqOps: EqOps[Int, Boolean] = new StructuralEqOps[Int]
  override val booleanBranching: BooleanBranching[Boolean, IRValue] = new ConcreteBooleanBranching[IRValue]
}

class IRInterpreterConstant(externals: Map[String, IRValue]) extends IRInterpreter(externals: Map[String, IRValue]) {

  override type VInt = Topped[Int]
  override type VBool = Topped[Boolean]

  implicit val failure : Failure = ???
  implicit val effectStack: EffectStack = ???

  given Structural[Int] with {}
  given Join[IRValue] with {
    override def apply(v1: IRValue, v2: IRValue): MaybeChanged[IRValue] = ???
  }

  override val integerOps: IntegerOps[Int, VInt] = new ToppedIntegerOps[Int, Int]
  override val orderingOps: OrderingOps[Topped[Int], Topped[Boolean]] = new ToppedCertainOrderingOps[Int, Boolean]
  override val integerEqOps: EqOps[Topped[Int], Topped[Boolean]] = new ToppedCertainEqOps[Int, Boolean]
  override val booleanBranching: BooleanBranching[Topped[Boolean], IRValue] = new ToppedBooleanBranching[Boolean, IRValue]
}