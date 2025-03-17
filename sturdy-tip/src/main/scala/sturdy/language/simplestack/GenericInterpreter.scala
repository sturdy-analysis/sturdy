package sturdy.language.simplestack

import sturdy.data.{MayJoin, given}
import sturdy.data.MayJoin.{NoJoin, WithJoin}
import sturdy.effect.{EffectList, EffectStack}
import sturdy.effect.failure.{CollectedFailures, ConcreteFailure, Failure, FailureKind}
import sturdy.effect.except.{ConcreteExcept, Except, JoinedExcept, given}
import sturdy.effect.operandstack.{ConcreteOperandStack, DecidableOperandStack, JoinableDecidableOperandStack, OperandStack, StackUnderflow, given}
import sturdy.{data, fix}
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.values.{Finite, Powerset, Topped, given}
import sturdy.values.booleans.{BooleanBranching, LiftedBooleanBranching, given}
import sturdy.values.integer.{IntegerOps, given}
import sturdy.values.exceptions.given
import sturdy.values.ordering.{LiftedOrderingOps, OrderingOps, given}

case class Jump(pc: Int)

trait GenericInterpreter[V, E, J[_] <: MayJoin[_]]:

  val intops: IntegerOps[Int, V]
  val compare: OrderingOps[V, V]
  val stack: DecidableOperandStack[V]
  val except: Except[Jump, E, J]
  val branchops: BooleanBranching[V, Unit]
  implicit val failure: Failure

  implicit val effectStack: EffectStack = new EffectStack(EffectList(stack, except))

  implicit val joinV: J[V]
  implicit val joinUnit: J[Unit]

  val unknown: V

  val prog: Vector[Inst]

  def step(i: Inst): Unit = i match
    case Inst.Unknown => stack.push(unknown)
    case Inst.Const(i) => stack.push(intops.integerLit(i))
    case Inst.Add => stack.push(intops.add.tupled(stack.pop2OrFail()))
    case Inst.Mul => stack.push(intops.mul.tupled(stack.pop2OrFail()))
    case Inst.Gt => stack.push(compare.gt.tupled(stack.pop2OrFail()))
    case Inst.JumpIf(pc) =>
      val c = stack.popOrFail()
      branchops.boolBranch(c){
        except.throws(Jump(pc))
      } {
        // nothing
      }

  val fixpoint: EffectStack ?=> fix.Fixpoint[Int, Unit]

  def run(pc: Int): Unit =
    var current = pc
    while (current < prog.length) {
      step(prog(current))
      current = current + 1
    }

  def trampoline(pc: Int): Unit =
    except.tryCatch {
      run(pc)
    } {
      case Jump(to) => fixpoint(trampoline)(to)
    }

  def runMain(): Unit =
    trampoline(0)


object ConcreteInterpreter:
  class Interpreter(val prog: Vector[Inst]) extends GenericInterpreter[Int, Jump, NoJoin]:
    override val unknown: Int = 0
    override val intops: IntegerOps[Int, Int] = implicitly
    override val compare: OrderingOps[Int, Int] =
      new LiftedOrderingOps[Int, Int, Int, Boolean](identity, b => if (b) 1 else 0)
    override val stack: DecidableOperandStack[Int] = new ConcreteOperandStack[Int]
    override val except: Except[Jump, Jump, NoJoin] = new ConcreteExcept
    override val branchops: BooleanBranching[Int, Unit] = new LiftedBooleanBranching(_ != 0)
    override implicit val failure: Failure = new ConcreteFailure
    override val joinV: NoJoin[Int] = noJoin
    override val joinUnit: NoJoin[Unit] = noJoin
    override val fixpoint: EffectStack ?=> Fixpoint[Int, Unit] = new fix.ConcreteFixpoint

object ConstantInterpreter:
  given Finite[Jump] with {}
  class Interpreter(val prog: Vector[Inst]) extends GenericInterpreter[Topped[Int], Powerset[Jump], WithJoin]:
    override val unknown: Topped[Int] = Topped.Top
    override val intops: IntegerOps[Int, Topped[Int]] = implicitly
    override val compare: OrderingOps[Topped[Int], Topped[Int]] =
      new LiftedOrderingOps[Topped[Int], Topped[Int], Topped[Int], Topped[Boolean]](identity, _.map(b => if (b) 1 else 0))
    override val stack: DecidableOperandStack[Topped[Int]] = new JoinableDecidableOperandStack
    override val except: Except[Jump, Powerset[Jump], WithJoin] = new JoinedExcept
    override val branchops: BooleanBranching[Topped[Int], Unit] = new LiftedBooleanBranching(_.map(_ != 0))
    override implicit val failure: Failure = new CollectedFailures[StackUnderflow.type]
    override val joinV: WithJoin[Topped[Int]] = implicitly
    override val joinUnit: data.MayJoin.WithJoin[Unit] = implicitly
    given Finite[Int] with {}
    override val fixpoint: EffectStack ?=> Fixpoint[Int, Unit] =
      fix.notContextSensitive(
        fix.iter.innermost[Int, Unit, Unit](StackConfig.StackedStates())
      ).fixpoint


def test(prog: Vector[Inst]): Unit =
  val cinterp = new ConcreteInterpreter.Interpreter(prog)
  cinterp.runMain()
  println(s"CStack: ${cinterp.stack}")

  val ainterp = new ConstantInterpreter.Interpreter(prog)
  ainterp.runMain()
  println(s"AStack: ${ainterp.stack}")

import Inst.*
object Test1 extends App:
  def run = test(Vector(
    Const(1),
    Const(2),
    Add,
    Const(3),
    Mul
  ))

object Test2 extends App:
  test(Vector(
    Unknown,
    Const(1),
    Add
  ))

object Test3 extends App:
  test(Vector(
    Const(3),
    Unknown,
    JumpIf(0)
  ))

object Test4 extends App:
  test(Vector(
    Const(3),
    JumpIf(3),
    Const(1),
    Const(2),
  ))
