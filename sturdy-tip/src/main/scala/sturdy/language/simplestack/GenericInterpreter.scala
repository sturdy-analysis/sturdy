package sturdy.language.simplestack

import sturdy.data.{MayJoin, given}
import sturdy.data.MayJoin.{NoJoin, WithJoin}
import sturdy.effect.{EffectList, EffectStack}
import sturdy.effect.failure.{CollectedFailures, ConcreteFailure, Failure, FailureKind}
import sturdy.effect.except.{ConcreteExcept, Except, JoinedExcept, given}
import sturdy.effect.operandstack.{ConcreteOperandStack, DecidableOperandStack, JoinableDecidableOperandStack, OperandStack, StackUnderflow, given}
import sturdy.{data, fix, values}
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.values.{BoundedPowerset, Finite, Powerset, Topped, given}
import sturdy.values.booleans.{BooleanBranching, LiftedBooleanBranching, given}
import sturdy.values.integer.{IntegerOps, given}
import sturdy.values.exceptions.given
import sturdy.values.ordering.{LiftedOrderingOps, OrderingOps, given}


case class Jump[V](pc: V)

trait GenericInterpreter[V, E, J[_] <: MayJoin[_]]:
  type Jump = sturdy.language.simplestack.Jump[V]
  val intops: IntegerOps[Int, V]
  val compare: OrderingOps[V, V]
  val stack: DecidableOperandStack[V]
  val except: Except[Jump, E, J]
  val branchops: BooleanBranching[V, Unit]
  implicit val failure: Failure

  def jump(to: V)(k: Int => Unit): Unit

  implicit val effectStack: EffectStack = new EffectStack(EffectList(stack, except))

  implicit val joinV: J[V]
  implicit val joinUnit: J[Unit]

  val unknown: V

  val prog: Vector[Inst]

  def step(i: Inst): Unit = i match
    case Inst.Unknown => stack.push(unknown)
    case Inst.Const(i) => stack.push(intops.integerLit(i))
    case Inst.Dup =>
      val v = stack.popOrFail()
      stack.push(v) ; stack.push(v)
    case Inst.Add => stack.push(intops.add.tupled(stack.pop2OrFail()))
    case Inst.Mul => stack.push(intops.mul.tupled(stack.pop2OrFail()))
    case Inst.Gt => stack.push(compare.gt.tupled(stack.pop2OrFail()))
    case Inst.JumpIf =>
      val (c, pc) = stack.pop2OrFail()
      branchops.boolBranch(c){
        except.throws(Jump(pc))
      } {
        // nothing
      }

  val fixpoint: EffectStack ?=> fix.Fixpoint[Int, Unit]

  def run(pc: Int): Unit =
    var current = pc
    while (current < prog.length) {
//      println(s"step ${prog(current)}")
      step(prog(current))
      current = current + 1
    }

  lazy val fixedTrampoline: Int => Unit = fixpoint(trampoline)
  def trampoline(pc: Int): Unit =
    except.tryCatch {
      run(pc)
    } {
      case Jump(to) => jump(to)(fixedTrampoline)
    }

  def runMain(): Unit =
    trampoline(0)


object ConcreteInterpreter:
  class Interpreter(val prog: Vector[Inst]) extends GenericInterpreter[Int, Jump[Int], NoJoin]:
    override val unknown: Int = 0
    override val intops: IntegerOps[Int, Int] = implicitly
    override val compare: OrderingOps[Int, Int] =
      new LiftedOrderingOps[Int, Int, Int, Boolean](identity, b => if (b) 1 else 0)
    override val stack: DecidableOperandStack[Int] = new ConcreteOperandStack[Int]
    override val except: Except[Jump, Jump, NoJoin] = new ConcreteExcept
    override def jump(to: Int)(k: Int => Unit): Unit = k(to)
    override val branchops: BooleanBranching[Int, Unit] = new LiftedBooleanBranching(_ != 0)
    override implicit val failure: Failure = new ConcreteFailure
    override val joinV: NoJoin[Int] = noJoin
    override val joinUnit: NoJoin[Unit] = noJoin
    override val fixpoint: EffectStack ?=> Fixpoint[Int, Unit] = new fix.ConcreteFixpoint

object ConstantInterpreter:
  class Interpreter(val prog: Vector[Inst]) extends GenericInterpreter[Topped[Int], Powerset[Jump[Topped[Int]]], WithJoin]:
    given Finite[Jump] with {}
    override val unknown: Topped[Int] = Topped.Top
    override val intops: IntegerOps[Int, Topped[Int]] = implicitly
    override val compare: OrderingOps[Topped[Int], Topped[Int]] =
      new LiftedOrderingOps[Topped[Int], Topped[Int], Topped[Int], Topped[Boolean]](identity, _.map(b => if (b) 1 else 0))
    override val stack: DecidableOperandStack[Topped[Int]] = new JoinableDecidableOperandStack
    override val except: Except[Jump, Powerset[Jump], WithJoin] = new JoinedExcept
    override def jump(to: Topped[Int])(k: Int => Unit): Unit = to match
      case Topped.Actual(v) => k(v)
      case Topped.Top => sturdy.data.mapJoin(prog.indices, k)

    override val branchops: BooleanBranching[Topped[Int], Unit] = new LiftedBooleanBranching(_.map(_ != 0))
    override implicit val failure: Failure = new CollectedFailures[StackUnderflow.type]
    override val joinV: WithJoin[Topped[Int]] = implicitly
    override val joinUnit: data.MayJoin.WithJoin[Unit] = implicitly
    given Finite[Int] with {}
    override val fixpoint =
      fix.notContextSensitive(
        fix.iter.innermost[Int, Unit, Unit](StackConfig.StackedStates())
      ).fixpoint

object PowersetInterpreter:
  class Interpreter(val prog: Vector[Inst], val bound: Int) extends GenericInterpreter[BoundedPowerset[Int], Powerset[Jump[BoundedPowerset[Int]]], WithJoin]:
    given Finite[Jump] with {}

    override val unknown: BoundedPowerset[Int] = BoundedPowerset.Unbound()
    override val intops: IntegerOps[Int, BoundedPowerset[Int]] = implicitly
    override val compare: OrderingOps[BoundedPowerset[Int], BoundedPowerset[Int]] = ???
    override val stack: DecidableOperandStack[BoundedPowerset[Int]] = new JoinableDecidableOperandStack
    override val except: Except[Jump, Powerset[Jump], WithJoin] = new JoinedExcept

    override def jump(to: BoundedPowerset[Int])(k: Int => Unit): Unit = to match
      case BoundedPowerset.Inbounds(set, _) => sturdy.data.mapJoin(set, k)
      case BoundedPowerset.Unbound() => sturdy.data.mapJoin(prog.indices, k)

    override val branchops: BooleanBranching[BoundedPowerset[Int], Unit] = ???
    override implicit val failure: Failure = new CollectedFailures[StackUnderflow.type]
    override val joinV: WithJoin[BoundedPowerset[Int]] = implicitly
    override val joinUnit: data.MayJoin.WithJoin[Unit] = implicitly

    given Finite[Int] with {}

    override val fixpoint =
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
  test(Vector(
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
    Const(1),
    JumpIf // if ? jump to 1
  ))

object Test4 extends App:
  test(Vector(
    Unknown,
    Unknown,
    JumpIf, // if ? jump to ?
    Const(1),
    Const(2),
  ))

object Test5 extends App:
  // lam x. min(x,10)
  test(Vector(
    Unknown,
    Dup,
    Const(9),
    Gt,
    Const(Int.MaxValue),
    JumpIf,
    Const(1),
    Add,
    Const(1),
    Const(1),
    JumpIf
  ))