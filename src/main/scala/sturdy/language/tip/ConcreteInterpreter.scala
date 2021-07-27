package sturdy.language.tip

import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.environment.CEnvironment
import sturdy.effect.failure.{Failure, CFailure}
import sturdy.effect.print.CPrint
import sturdy.effect.store.CStore
import sturdy.effect.userinput.CUserInput
import sturdy.fix
import sturdy.values.booleans.{_, given}
import sturdy.values.ints.{_, given}
import sturdy.values.functions.{_, given}
import sturdy.values.references.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.given

import GenericInterpreter.{AllocationSite, GenericPhi, FixIn, FixOut}

object ConcreteInterpreter:
  enum Value:
    case IntValue(i: Int)
    case RefValue(addr: Option[Addr])
    case FunValue(fun: Function)

    def asBoolean: Boolean = this match
      case IntValue(i) => i != 0
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asInt: Int = this match
      case IntValue(i) => i
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asFunction: Function = this match
      case FunValue(f) => f
      case _ => throw new IllegalArgumentException(s"Expected Function but got $this")
    def asReference: Option[Addr] = this match
      case RefValue(a) => a
      case _ => throw new IllegalArgumentException(s"Expected Reference but got $this")

  import Value._

  def boolValue(b: Boolean): Value = IntValue(if (b) 1 else 0)

  type Addr = Int
  type Environment = Map[String, Int]
  type Store = Map[Int, Value]
  class Effects(initEnvironment: Environment, initStore: Store, nextInput: () => Value)
    extends CBoolBranching[Value]
      with CEnvironment[String, Int](initEnvironment)
      with CStore[Int, Value](initStore)
      with CAllocationIntIncrement[AllocationSite]
      with CPrint[Value]
      with CUserInput[Value](nextInput)
      with CFailure

  def apply(initEnvironment: Environment, initStore: Store, nextInput: () => Value): ConcreteInterpreter = {
    val effects = new Effects(initEnvironment, initStore, nextInput)

    given Failure = effects
    given IntOps[Value] = new LiftedIntOps[Value, Int](_.asInt, IntValue.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, Int, Boolean](_.asInt, boolValue)
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (IntValue(i1), IntValue(i2)) => boolValue(i1 == i2)
        case (RefValue(a1), RefValue(a2)) => boolValue(a1 == a2)
        case (FunValue(f1), FunValue(f2)) => boolValue(f1 == f2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = IntValue(1 - equ(v1, v2).asInt)
    given FunctionOps[Function, Value, Value, Value] = new LiftedFunctionOps[Function, Value, Value, Value, Function](_.asFunction, FunValue.apply)
    given ReferenceOps[Addr, Value] = new LiftedReferenceOps[Value, Addr, Option[Addr]](_.asReference, RefValue.apply)

    new ConcreteInterpreter(using effects)
  }

import ConcreteInterpreter.*

class ConcreteInterpreter
  (using effectOps: Effects)
  (using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value],
         functionOps: FunctionOps[Function, Value, Value, Value], refOps: ReferenceOps[Addr, Value])
  extends GenericInterpreter[Value, Addr, Effects]:

  val phi = fix.identity[FixIn[Value], FixOut[Value]]

