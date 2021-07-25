package sturdy.language.schemelang

import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.environment.CEnvironmentClosures
import sturdy.effect.store.CStore
import sturdy.effect.failure.{CFailure, Failure}
import sturdy.fix.CFixpoint
import sturdy.values.ints.{_,given}
import sturdy.values.doubles.{_,given}
import sturdy.values.rational.{_,given}
import sturdy.values.booleans.{_,given}
import sturdy.values.chars.{_,given}
import sturdy.values.strings.{_,given}
import sturdy.values.symbols.{_,given}
import sturdy.values.quotes.{_,given}
import sturdy.values.relational.{_,given}
import sturdy.values.void.VoidOps
import sturdy.values.closure.ClosureOps
import sturdy.values.types.TypeOps
import sturdy.util.given


object ConcreteInterpreter:

  enum Value:
    case IntVal(i: Int)
    case BoolVal(b: Boolean)
    case CharVal(c: Char)
    case StringVal(str: String)
    case SymbolVal(sym: String)
    case QuoteVal(qot: Literal)
    case VoidVal
    case ClosureVal(closure: (Expr, Map[String, Addr]))

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


  import Value._

//  def toRational(x: (Int, Int)): Value = RationalVal(x._1, x._2)

  type Addr = Int
  type Environment = Map[String, Addr]
  type Store = Map[Addr, Value]
  type Closure = (Expr, Environment)

  def apply(initEnvironment: Environment, initStore: Store) = {
    val effects =
      new  CBoolBranching[Value]
      with CEnvironmentClosures[String, Addr, Expr](initEnvironment)
      with CStore[Addr, Value](initStore)
      with CAllocationIntIncrement
      with CFailure
    val fixpoint = new CFixpoint[List[Expr], Value]

    given Failure = effects
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

    given ClosureOps[Expr, String, Addr, Value] with
      def closureToVal(cls: (Expr, Map[String, Addr])) = ClosureVal(cls)
      def valToClosure(v: Value): (Expr, Map[String, Addr]) = v match
        case ClosureVal(cls) => cls
        case _ => throw new IllegalArgumentException(s"Expected ClosureVal but got $this")

    new GenericInterpreter(using effects)(using fixpoint) {}
  }

