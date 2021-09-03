package sturdy.language.minijava

import sturdy.effect.noJoin
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.callframe.CCallFrame
import sturdy.effect.failure.{Failure, CFailure}
import sturdy.effect.print.CPrint
import sturdy.effect.store.CStore
import sturdy.effect.userinput.CUserInput
import sturdy.fix
import sturdy.values.booleans.{_, given}
import sturdy.values.ints.{_, given}
import sturdy.values.functions.{_, given}
import sturdy.values.records.{_, given}
import sturdy.values.references.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.given

import GenericInterpreter.{AllocationSite, GenericPhi, FixIn, FixOut}

object ConcreteInterpreter:
  enum Value:
    case IntValue(i: Int)
    // Do we have Fun values?
    case FunValue(fun: Function)
    case BoolValue(b: Boolean)
    // Array Value? case ArrayValue(arr: Array[Int])
    // Identifiers? case IdValue(name: String)
    
    //case RefValue(addr: Option[Addr])
    //case RecValue(rec: Map[String, Value])

    def asBoolean: Boolean = this match
      case BoolValue(b) => b
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asInt: Int = this match
      case IntValue(i) => i
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asFunction: Function = this match
      case FunValue(f) => f
      case _ => throw new IllegalArgumentException(s"Expected Function but got $this")
    
    /*def asReference: Option[Addr] = this match
      case RefValue(a) => a
      case _ => throw new IllegalArgumentException(s"Expected Reference but got $this")
    def asRecord: Map[String, Value] = this match
      case RecValue(rec) => rec
      case _ => throw new IllegalArgumentException(s"Expected Record but got $this")
    */
  import Value._

  //def boolValue(b: Boolean): Value = IntValue(if (b) 1 else 0)

  type Addr = Int
  type Environment = Map[String, Int]
  type Store = Map[Int, Value]
  class Effects(initEnvironment: Environment, initStore: Store, nextInput: () => Value)
    extends CBoolBranching[Value]
      with CCallFrame[Unit, String, Int]((), initEnvironment)
      with CStore[Int, Value](initStore)
      with CAllocationIntIncrement[AllocationSite]
      with CPrint[Value]
      with CUserInput[Value](nextInput)
      with CFailure

  def apply(initEnvironment: Environment, initStore: Store, nextInput: () => Value): ConcreteInterpreter = {
    val effects = new Effects(initEnvironment, initStore, nextInput)

    // added BoolValues and adjusted neq Function for use of actual bools
    given Failure = effects
    given IntOps[Value] = new LiftedIntOps(_.asInt, IntValue.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps(_.asInt, boolValue)
    given BooleanOps[Value] = new LiftedBooleanOps(_.asBoolean, BoolValue.apply)
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (IntValue(i1), IntValue(i2)) => BoolValue(i1 == i2)
        case (FunValue(f1), FunValue(f2)) => BoolValue(f1 == f2)
        case (BoolValue(b1), BoolValue(b2)) => BoolValue(b1 == b2)
        //case (RefValue(a1), RefValue(a2)) => boolValue(a1 == a2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = BoolValue(!equ(v1, v2).asBoolean)
    given FunctionOps[Function, Value, Value, Value] = new LiftedFunctionOps(_.asFunction, FunValue.apply)
    //given RecordOps[String, Value, Value] = new LiftedRecordOps(_.asRecord, identity, RecValue.apply, identity)
    //given ReferenceOps[Addr, Value] = new LiftedReferenceOps(_.asReference, RefValue.apply)

    given TypeOps[Value] with
      def isInteger(v: Value): BoolValue = v match
        case NumVal(IntVal(_)) => BoolValue(true)
        case _ => BoolValue(false)
      def isBoolean(v: Value): Value = v match
        case BoolValue(_) => BoolValue(true)
        case _ => BoolValue(false)

      //def isArray(v: Value): Value = v match
      //def isIdentifier(v: Value): BoolValue = v match
    new ConcreteInterpreter(using effects)
  }

import ConcreteInterpreter.*

class ConcreteInterpreter
(using effectOps: Effects)
(using intOps: IntOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value],
 functionOps: FunctionOps[Function, Value, Value, Value], recOps: RecordOps[String, Value, Value], refOps: ReferenceOps[Addr, Value])
  extends GenericInterpreter[Value, Addr, Effects]:

  val phi = fix.identity[FixIn, FixOut[Value]]

