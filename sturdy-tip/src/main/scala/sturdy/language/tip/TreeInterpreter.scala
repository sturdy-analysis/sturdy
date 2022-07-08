package sturdy.language.tip

import sturdy.data.NoJoin
import sturdy.data.noJoin
import sturdy.data.unit
import sturdy.data.finiteUnit
import sturdy.effect.EffectStack
import sturdy.effect.allocation.AllocationTrees
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.callframe.CallFrameTree
import sturdy.effect.callframe.CallFrameTrees
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.ConcreteFailure
import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureTrees
import sturdy.effect.print.CPrint
import sturdy.effect.print.PrintTrees
import sturdy.effect.store.CStore
import sturdy.effect.store.StoreTrees
import sturdy.effect.userinput.CUserInput
import sturdy.effect.userinput.UserInputTrees
import sturdy.fix
import sturdy.fix.Combinator
import sturdy.fix.Fixpoint
import sturdy.language.tip.abstractions.isFunOrWhile
import sturdy.language.tip.Interpreter
import sturdy.language.tip.Function
import sturdy.language.tip.*
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}

object TreeInterpreter:
  type J[A] = NoJoin[A]

  type Value = Tree
  type Addr = Tree
  type Environment = Map[String, Value]
  type Store = Map[Addr, Value]

  class Instance(initEnvironment: Environment, initStore: Store) extends GenericInterpreter[Value, Addr, J]:
    val treeBuffer = new TreeBuffer
    given TreeBuffer = treeBuffer

    def newInstance: Instance = new Instance(initEnvironment, initStore)
    override def jv: J[Value] = implicitly

    override val failure: FailureTrees = new FailureTrees
    given Failure = failure

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly

    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: CallFrameTrees[Unit, String] = new CallFrameTrees(())
    override val store: StoreTrees = new StoreTrees
    override val alloc: AllocationTrees[AllocationSite] = new AllocationTrees
    override val print: PrintTrees[Value] = new PrintTrees
    override val input: UserInputTrees = new UserInputTrees

//    summon[Finite[Tree]]
//    summon[Join[Tree]]
    given Join[Tree] with {
      override def apply(v1: Tree, v2: Tree): MaybeChanged[Addr] =
        println(s"Join $v1\n with $v2")
        if (v1 == v2)
          Unchanged(v1)
        else
          Changed(TreeAlt(v1, v2))
    }
    given Widen[Tree] with {
      override def apply(v1: Tree, v2: Tree): MaybeChanged[Addr] =
        println(s"Widen $v1\n with $v2")
        ???
    }
//        println(s"Widen $v1\n with $v2")
//        Unchanged(v1)
//    }

    enum IRTrees extends Tree:
      case IRLoop(dom: FixIn)
      case IRBreak(dom: FixIn)
      case IRFunction(dom: FixIn)
      case IRCall(dom: FixIn)

      override def prettyPrint(using Indent): String = this match
        case IRLoop(dom) =>
          s"label($dom)"
        case IRBreak(dom) =>
          s"break($dom)"
        case IRFunction(dom) =>
          s"label($dom)"
        case IRCall(dom) =>
          s"call($dom)"

    def loopTracker(phi: fix.Combinator[FixIn, FixOut[Value]]): fix.Combinator[FixIn, FixOut[Value]] = new Combinator {
      import IRTrees.*
      var stack: Set[FixIn] = Set()

      override def apply(f: FixIn => FixOut[Value]): FixIn => FixOut[Value] = {
        case dom@FixIn.Run(_: Stm.While) =>
          if (stack.contains(dom)) {
            treeBuffer += IRBreak(dom)
            FixOut.Run()
          } else {
            treeBuffer += IRLoop(dom)
            stack += dom
            try phi(f)(dom)
            finally stack -= dom
          }
        case dom: FixIn.EnterFunction =>
          if (stack.contains(dom)) {
            FixOut.ExitFunction(IRCall(dom))
          } else {
            treeBuffer += IRFunction(dom)
            stack += dom
            try phi(f)(dom)
            finally stack -= dom
          }
        case dom => phi(f)(dom)
      }
    }

    override val fixpoint: EffectStack ?=> fix.Fixpoint[FixIn, FixOut[Value]] =
      loopTracker(fix.identity).fixpoint

  def apply(initEnvironment: Environment, initStore: Store): Instance =
    new Instance(initEnvironment, initStore)
