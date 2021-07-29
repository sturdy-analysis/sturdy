package sturdy.language.schemelang

import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.environment.CEnvironment
import sturdy.effect.environment.CClosableEnvironment
import sturdy.effect.environment.ClosableEnvironment
import sturdy.effect.store.CStore
import sturdy.effect.failure.{CFailure, Failure}
import sturdy.fix
import sturdy.values.ints.{_, given}
import sturdy.values.doubles.{_, given}
import sturdy.values.rational.{_, given}
import sturdy.values.booleans.{_, given}
import sturdy.values.chars.{_, given}
import sturdy.values.strings.{_, given}
import sturdy.values.symbols.{_, given}
import sturdy.values.quotes.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.void.VoidOps
import sturdy.values.closures.{_, given}
import sturdy.values.types.TypeOps
import sturdy.values.given
import sturdy.util
import GenericInterpreter.{AllocationSite, FixIn, FixOut}

object ConcreteInterpreter:
  enum Value:
    case IntVal(i: Int)
    case BoolVal(b: Boolean)
    case CharVal(c: Char)
    case StringVal(str: String)
    case SymbolVal(sym: String)
    case QuoteVal(qot: Literal)
    case VoidVal
    case ClosureVal(closure: (List[String], Environment, List[Expr]))

    def asInt: Int = this match
      case IntVal(i) => i
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asBoolean: Boolean = this match
      case BoolVal(b) => b
      case _ => throw new IllegalArgumentException(s"Expected Boolean but got $this")
    def asChar: Char = this match
      case CharVal(c) => c
      case _ => throw new IllegalArgumentException(s"Expected Char but got $this")
    def asString: String = this match
      case StringVal(str) => str
      case _ => throw new IllegalArgumentException(s"Expected String but got $this")
    def asSymbol: String = this match
      case SymbolVal(sym) => sym
      case _ => throw new IllegalArgumentException(s"Expected Symbol but got $this")
    def asQuote : Literal = this match
      case QuoteVal(qot) => qot
      case _ => throw new IllegalArgumentException(s"Expected Quote but got $this")

    def asClosure : (List[String], Environment, List[Expr]) = this match
      case ClosureVal(cls) => cls
      case _ => throw new IllegalArgumentException(s"Expected Closure but got $this")

  import Value._

//  def toRational(x: (Int, Int)): Value = RationalVal(x._1, x._2)

  type Addr = Int
  type Environment = Map[String, Addr]
  type Store = Map[Addr, Value]
//  type Closure = (String[String], List[Expr], Environment)

  class Effects(initEnvironment: Environment, initStore: Store)
    extends CBoolBranching[Value]
      with CEnvironment[String, Int](initEnvironment)
      with CClosableEnvironment[String, Int]
      with CStore[Addr, Value](initStore)
      with CAllocationIntIncrement[AllocationSite]
      with CFailure


  def apply(initEnvironment: Environment, initStore: Store): ConcreteInterpreter = {
    val effects = new Effects(initEnvironment, initStore)

    given Failure = effects
    given ClosableEnvironment[String, Addr, Environment] = effects
    given IntOps[Value] = new LiftedIntOps[Value, Int](_.asInt, IntVal.apply)
    given BooleanOps[Value] = new LiftedBooleanOps[Value, Boolean](_.asBoolean, BoolVal.apply)
    given CharOps[Value] = new LiftedCharOps[Value, Char](_.asChar, CharVal.apply)
    given StringOps[Value] = new LiftedStringOps[Value, String](_.asString, StringVal.apply)
    given SymbolOps[Value] = new LiftedSymbolOps[Value, String](_.asSymbol, SymbolVal.apply)
    given QuoteOps[Literal, Value] = new LiftedQuoteOps[Literal, Value, Literal](_.asQuote, QuoteVal.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, Int, Boolean](_.asInt, BoolVal.apply)
    given VoidOps[Value] with
      def void():Value = VoidVal
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (BoolVal(b1), BoolVal(b2)) => BoolVal(b1 == b2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = (v1, v2) match
        case (BoolVal(b1), BoolVal(b2)) => BoolVal(b1 != b2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")


    // TODO: maybe simplify this by using try .. catch on e.g. asInt
    given TypeOps[Value] with
      def isNumber(v: Value) : BoolVal = v match
//        case IntVal(_) | DoubleVal(_) | RationalVal(_,_) => BoolVal(true)
        case IntVal(_) => BoolVal(true)
        case _ => BoolVal(false)
      def isInteger(v: Value): BoolVal = v match
        case IntVal(_) => BoolVal(true)
        case _ => BoolVal(false)
      def isDouble(v:Value ): BoolVal = v match
//        case DoubleVal(_) => BoolVal(true)
        case _ => BoolVal(false)
      def isRational(v: Value): BoolVal = v match
//        case RationalVal(_, _) => BoolVal(true)
        case _ => BoolVal(false)
      def isNull(v: Value): BoolVal = ???
      def isCons(v: Value): BoolVal = ???
      def isBoolean(v: Value): Value = v match
        case BoolVal(_) => BoolVal(true)
        case _ => BoolVal(false)

    given ClosureOps[
      String, Addr, Environment, List[Expr], Value, Value, Value] =
      new LiftedClosureOps[String, Addr, Environment, (List[String], Environment, List[Expr]), List[Expr], Value, Value](_.asClosure, ClosureVal.apply)


    new ConcreteInterpreter(using effects)
  }

import ConcreteInterpreter.*

class ConcreteInterpreter
  (using effectOps: Effects)
  (using intOps: IntOps[Value],
             boolOps: BooleanOps[Value], eqOps: EqOps[Value, Value], compareOps: CompareOps[Value, Value],
             charOps: CharOps[Value], stringOps: StringOps[Value],
             symbolOps: SymbolOps[Value], quoteOps: QuoteOps[Literal, Value],
             closureOps: ClosureOps[String, Addr, Environment, List[Expr], Value, Value, Value],
             voidOps: VoidOps[Value], typeOps: TypeOps[Value]) //, closureOps: ClosureOps[Expr, String, Addr, V])
  extends GenericInterpreter[Value, Addr, Effects, Environment]:

  val phi = fix.identity[FixIn[Value], FixOut[Value]]