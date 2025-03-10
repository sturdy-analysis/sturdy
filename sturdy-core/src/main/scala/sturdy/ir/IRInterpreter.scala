package sturdy.ir

import sturdy.effect.{Effect, EffectList, EffectStack}
import sturdy.effect.failure.{CollectedFailures, ConcreteFailure, Failure}
import sturdy.values.{Join, MaybeChanged, Structural, Topped, booleans}
import sturdy.values.booleans.{BooleanBranching, BooleanOps, ConcreteBooleanBranching, ConcreteBooleanOps, IRBooleanOperator, ToppedBooleanBranching, ToppedBooleanOps}
import sturdy.values.functions.IRFunctionOperator
import sturdy.values.integer.{ConcreteIntegerOps, IRIntegerOperator, IntegerOps, ToppedIntegerOps}
import sturdy.values.ordering.{ConcreteOrderingOps, EqOps, IREqualityOperator, IROrderingOperator, OrderingOps, StructuralEqOps, ToppedCertainEqOps, ToppedCertainOrderingOps, ToppedUncertainOrderingOps}

import scala.annotation.tailrec
import scala.collection.immutable.ArraySeq


case class IRValue(c: Any)

abstract class IRInterpreter(val externals: Map[String, IRValue], inputValue: () => IRValue) {

  type VInt
  type VBool

  var feedbackStore: Map[IR_UID, List[Option[IRValue]]] = Map()

  val integerOps: IntegerOps[Int, VInt]
  val booleanOps: BooleanOps[VBool]
  val orderingOps: OrderingOps[VInt, VBool]
  val integerEqOps: EqOps[VInt, VBool]
  val booleanBranching: BooleanBranching[VBool, IRValue]

  def interpret(ir: IR): IRValue = ir match
    case IR.Unknown() => inputValue()
    case IR.Undefined() => IRValue(null)
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

      case operator: IRBooleanOperator =>
        val v1 = interpret(args.head).c.asInstanceOf[VBool]
        val v2 = args.lift(1).map(interpret(_).c.asInstanceOf[VBool])
        operator match
          case booleans.IRBooleanOperator.AND => booleanOps.and(v1, v2.getOrElse(throw new Exception("Wrong arity")))
          case booleans.IRBooleanOperator.OR => booleanOps.or(v1, v2.getOrElse(throw new Exception("Wrong arity")))
          case booleans.IRBooleanOperator.NOT => booleanOps.not(v1)
    )

    case IR.Select(cond, thn, els) =>
      booleanBranching.boolBranch(interpret(cond).c.asInstanceOf[VBool]) {interpret(thn)} {interpret(els)}

    case IR.Join(left, right) => ???

    case IR.Feedback(inits, Some(cond), Some(steps)) => feedbackStore.get(ir.uid) match
      case Some(_) => throw new Exception("Should not happen")
      case None =>
        val initsValue = inits.map(init => Some(interpret(init)))
        feedbackStore += ir.uid -> initsValue
        interpretFeedback(ir.uid, cond, steps)
        IRValue(null)

    case IR.FeedbackAsk(index, feedback) =>
      if !feedbackStore.isDefinedAt(feedback.uid) then interpret(feedback)
      feedbackStore(feedback.uid).lift(index) match
        case Some(value) => value.getOrElse(throw new Exception("Variable uninitialized"))
        case None => throw new Exception("Variable not in Callframe")

/*    case IR.Cast(ir, check) =>
      val v = interpret(ir)
      IRValue(check.asInstanceOf[IRCheck[Any]].assert(v.c))*/



// TODO deactivated tailrecursion to trigger stack overvflows during testing
//  @tailrec
  private final def interpretFeedback(uid: IR_UID, cond: IR, steps: List[IR]) : Unit = {
    val v = interpret(cond)
    v match
      case IRValue(false | 0) => println(s"Finished with $uid")
      case _ => val newStore = steps.map(step => Some(interpret(step)))
                feedbackStore += (uid -> newStore)
                interpretFeedback(uid, cond, steps)
  }

}

class IRInterpreterConcrete(externals: Map[String, IRValue], inputValue: () => IRValue) extends IRInterpreter(externals: Map[String, IRValue], inputValue: () => IRValue) {

  override type VInt = Int
  override type VBool = Boolean

  implicit val failure : Failure = new ConcreteFailure

  given Structural[Int] with {}

  override val integerOps: IntegerOps[Int, Int] = new ConcreteIntegerOps
  override val booleanOps: BooleanOps[Boolean] = implicitly
  override val orderingOps: OrderingOps[Int, Boolean] = new ConcreteOrderingOps[Int]
  override val integerEqOps: EqOps[Int, Boolean] = new StructuralEqOps[Int]
  override val booleanBranching: BooleanBranching[Boolean, IRValue] = new ConcreteBooleanBranching[IRValue]
}

class IRInterpreterConstant(externals: Map[String, IRValue], inputValue: () => IRValue) extends IRInterpreter(externals: Map[String, IRValue], inputValue: () => IRValue) {

  override type VInt = Topped[Int]
  override type VBool = Topped[Boolean]

  implicit val failure : Failure = new ConcreteFailure
  implicit val effectStack: EffectStack = EffectStack(EffectList(ArraySeq.empty[Effect]))

  given Structural[Int] with {}
  given Join[IRValue] with {
    override def apply(v1: IRValue, v2: IRValue): MaybeChanged[IRValue] = ???
  }

  override val integerOps: IntegerOps[Int, VInt] = new ToppedIntegerOps[Int, Int]
  override val booleanOps: BooleanOps[Topped[Boolean]] = new ToppedBooleanOps[Boolean]
  override val orderingOps: OrderingOps[Topped[Int], Topped[Boolean]] = new ToppedCertainOrderingOps[Int, Boolean]
  override val integerEqOps: EqOps[Topped[Int], Topped[Boolean]] = new ToppedCertainEqOps[Int, Boolean]
  override val booleanBranching: BooleanBranching[Topped[Boolean], IRValue] = new ToppedBooleanBranching[Boolean, IRValue]
}