package sturdy.language.schemelang

import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.environment.CEnvironment
import sturdy.effect.store.CStore
import sturdy.effect.failure.{CFailure, Failure}
import sturdy.fix
import sturdy.values.ints.{_, given}
import sturdy.values.doubles.{_, given}
import sturdy.values.rationals.{_, given}
import sturdy.values.booleans.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.closures.{_, given}
import sturdy.values.given
import sturdy.util
import GenericInterpreter.{AllocationSite, FixIn, FixOut}

object ConcreteInterpreter:
  enum Num:
    case IntVal(i: Int)
    case RatioVal(i1: Int, i2: Int)
    case DoubleVal(d: Double)

    def asInt: Int = this match
      case IntVal(i: Int) => i
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    def asRatio: (Int, Int) = this match
      case IntVal(i1: Int) => (i1, 1)
      case RatioVal(i1: Int, i2: Int) => (i1, i2)
      case _ => throw new IllegalArgumentException(s"Expected IntVal or RatioVal but got $this")
    def asDouble: Double = this match
      case IntVal(i: Int) => i.toDouble
      case RatioVal(i1: Int, i2: Int) => (i1 / i2).toDouble
      case DoubleVal(d: Double) => d

  enum Cons:
    import Value._

    case ConsVal(head: Value, tail: Value.ListVal)
    case NilVal

  enum Value:
    import Num._

    case BoolVal(b: Boolean)
    case CharVal(c: Char)
    case StringVal(str: String)
    case ListVal(cons: Cons)
    case SymbolVal(sym: String)
    case QuoteVal(qot: Value)
    case VoidVal
    case ClosureVal(closure: (List[String], Environment, List[Expr]))
    case NumVal(num: Num)

    def asNum: Num = this match
      case NumVal(num) => num
      case _ => throw new IllegalArgumentException(s"Expected Num but got $this")
    def asBoolean: Boolean = this match
      case BoolVal(b) => b
      case _ => throw new IllegalArgumentException(s"Expected Boolean but got $this")
    def asChar: Char = this match
      case CharVal(c) => c
      case _ => throw new IllegalArgumentException(s"Expected Char but got $this")
    def asString: String = this match
      case StringVal(str) => str
      case _ => throw new IllegalArgumentException(s"Expected String but got $this")
    def asList: Cons = this match
      case ListVal(cons) => cons
      case _ => throw new IllegalArgumentException(s"Expected List but got $this")
    def asSymbol: String = this match
      case SymbolVal(sym) => sym
      case _ => throw new IllegalArgumentException(s"Expected Symbol but got $this")
    def asQuote : Value = this match
      case QuoteVal(qot) => qot
      case _ => throw new IllegalArgumentException(s"Expected Quote but got $this")
    def asClosure : (List[String], Environment, List[Expr]) = this match
      case ClosureVal(cls) => cls
      case _ => throw new IllegalArgumentException(s"Expected Closure but got $this")


  import Value._
  import Num._
  import Cons._

  type Addr = Int
  type Environment = Map[String, Addr]
  type Store = Map[Addr, Value]

  class Effects(initEnvironment: Environment, initStore: Store)
    extends CBoolBranching[Value]
      with CEnvironment[String, Int](initEnvironment)
      with CStore[Addr, Value](initStore)
      with CAllocationIntIncrement[AllocationSite]
      with CFailure

  def apply(initEnvironment: Environment, initStore: Store): ConcreteInterpreter = {
    val effects = new Effects(initEnvironment, initStore)

    given Failure = effects

    given IntOps[Value] = new LiftedIntOps[Value, Int](_.asNum.asInt, x => NumVal(IntVal(x)))
    given IntDoubleOps[Value, Value] = new LiftedIntDoubleOps[Value, Value, Int, Double](_.asNum.asInt, x => NumVal(DoubleVal(x)))
    given IntBoolOps[Value, Value] = new LiftedIntBoolOps[Value, Value, Int, Boolean](_.asNum.asInt, BoolVal.apply)
    given RationalOps[Value] = new LiftedRationalOps[Value, (Int,Int)](_.asNum.asRatio, (x1,x2) => NumVal(RatioVal(x1,x2)))
    given RationalIntOps[Value, Value] = new LiftedRationalIntOps[Value, Value, (Int,Int), Int](_.asNum.asRatio, x => NumVal(IntVal(x)))
    given RationalDoubleOps[Value, Value] = new LiftedRationalDoubleOps[Value, Value, (Int,Int), Double](_.asNum.asRatio, x => NumVal(DoubleVal(x)))
    given RationalBoolOps[Value, Value] = new LiftedRationalBoolOps[Value, Value, (Int,Int), Boolean](_.asNum.asRatio, BoolVal.apply)
    given DoubleOps[Value] = new LiftedDoubleOps[Value, Double](_.asNum.asDouble, x => NumVal(DoubleVal(x)))
    given DoubleIntOps[Value, Value] = new LiftedDoubleIntOps[Value, Value, Double, Int](_.asNum.asDouble, x => NumVal(IntVal(x)))
    given DoubleBoolOps[Value, Value] = new LiftedDoubleBoolOps[Value, Value, Double, Boolean](_.asNum.asDouble, BoolVal.apply)
    given BooleanOps[Value] = new LiftedBooleanOps[Value, Boolean](_.asBoolean, BoolVal.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, Double, Boolean](_.asNum.asDouble, BoolVal.apply)
    given ClosureOps[String, Value, List[Expr], Environment, Value, Value] = new LiftedClosureOps[String, Value, List[Expr], Environment, Value, Value, (List[String], Environment, List[Expr])](_.asClosure, ClosureVal.apply)

    given ListOps[Value] with
      override def cons(v1: Value, v2: Value): Value = v2 match
        case lv@ListVal(x) => ListVal(ConsVal(v1, lv))
        case _ => throw new IllegalArgumentException(s"Expected ListVal but got $v2")
      override def nil: Value = ListVal(NilVal)
      override def car(v: Value): Value = v match
        case ListVal(ConsVal(head,_)) => head
        case _ => throw new IllegalArgumentException(s"Expected ListVal but got $v")
      override def cdr(v: Value): Value = v match
        case ListVal(ConsVal(_,tail)) => tail
        case _ => throw new IllegalArgumentException(s"Expected ListVal but got $v")

    given IfIsTypeOps[Value] with
      def ifIsInt[A](v: Value, thn: => A, els: => A): A = v match
        case NumVal(IntVal(_)) => thn
        case _ => els
      def ifIsRatio[A](v: Value, thn: => A, els: => A): A = v match
        case NumVal(RatioVal(_,_)) => thn
        case _ => els
      def ifIsDouble[A](v: Value, thn: => A, els: => A): A = v match
        case NumVal(DoubleVal(_)) => thn
        case _ => els

    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (BoolVal(b1), BoolVal(b2)) => BoolVal(b1 == b2)
        case (NumVal(IntVal(i1)), NumVal(IntVal(i2))) => BoolVal(i1 == i2)
        case (NumVal(RatioVal(i11, i12)), NumVal(RatioVal(i21,i22))) => BoolVal(i11 == i21 && i12 == i22)
        case (NumVal(DoubleVal(d1)), NumVal(DoubleVal(d2))) => BoolVal(d1 == d2)
        case (CharVal(c1), CharVal(c2)) => BoolVal(c1 == c2)
        case (StringVal(s1), StringVal(s2)) => BoolVal(s1 == s2)
        case (QuoteVal(SymbolVal(sym1)), QuoteVal(SymbolVal(sym2))) => BoolVal(sym1 == sym2)
        case (ListVal(NilVal), ListVal(NilVal)) => BoolVal(true)
        case _ => BoolVal(false)
      def neq(v1: Value, v2: Value): Value = throw new IllegalArgumentException("neq does not exist")

    given QuoteOps[Value] with
      override def quoteLit(l: Value): Value = QuoteVal(l)

    given SymbolOps[Value] with
      override def symbolLit(s: String): Value = SymbolVal(s)

    given VoidOps[Value] with
      def void:Value = VoidVal

    given CharOps[Value] with
      override def charLit(c: Char): Value = CharVal(c)

    given StringOps[Value] with
      override def stringLit(s: String): Value = StringVal(s)
      override def numberToString(v: Value): Value = v match
        case NumVal(num) => StringVal(num.toString)
        case _ => throw new IllegalArgumentException(s"Expected NumVal but got $v")
      override def stringToSymbol(v: Value): Value = v match
        case StringVal(s) => QuoteVal(SymbolVal(s))
        case _ => throw new IllegalArgumentException(s"Expected StringVal but got $v")
      override def symbolToString(v: Value): Value = v match
        case QuoteVal(SymbolVal(s)) => StringVal(s)
        case _ => throw new IllegalArgumentException(s"Expected QuoteVal but got $v")
      override def stringRef(v1: Value, v2: Value): Value = (v1, v2) match
        case (StringVal(s), NumVal(IntVal(i))) => CharVal(s.charAt(i))
        case _ => throw new IllegalArgumentException(s"Expected StringVal and IntVal but got $v1 and $v2")
      override def stringAppend(v1: Value, v2: Value): Value = (v1, v2) match
        case (StringVal(s1), StringVal(s2)) => StringVal(s1+s2)
        case _ => throw new IllegalArgumentException(s"Expected StringVal and IntVal but got $v1 and $v2")

    given TypeOps[Value] with
      def isNumber(v: Value) : BoolVal = v match
        case NumVal(_) => BoolVal(true)
        case _ => BoolVal(false)
      def isInteger(v: Value): BoolVal = v match
        case NumVal(IntVal(_)) => BoolVal(true)
        case _ => BoolVal(false)
      def isDouble(v:Value ): BoolVal = v match
        case NumVal(DoubleVal(_)) => BoolVal(true)
        case _ => BoolVal(false)
      def isRational(v: Value): BoolVal = v match
        case NumVal(RatioVal(_,_)) => BoolVal(true)
        case _ => BoolVal(false)
      def isNull(v: Value): BoolVal = v match
        case ListVal(NilVal) => BoolVal(true)
        case _ => BoolVal(false)
      def isCons(v: Value): BoolVal = v match
        case ListVal(ConsVal(_,_)) => BoolVal(true)
        case _ => BoolVal(false)
      def isBoolean(v: Value): Value = v match
        case BoolVal(_) => BoolVal(true)
        case _ => BoolVal(false)

    new ConcreteInterpreter(using effects)
  }

import ConcreteInterpreter.*

class ConcreteInterpreter
  (using effectOps: Effects)
  (using intOps: IntOps[Value], intDoubleOps: IntDoubleOps[Value, Value], intBoolOps: IntBoolOps[Value, Value],
         doubleOps: DoubleOps[Value], doubleIntOps: DoubleIntOps[Value, Value], doubleBoolOps: DoubleBoolOps[Value, Value],
         rationalOps: RationalOps[Value], rationalIntOps: RationalIntOps[Value,Value], rationalDoubleOps: RationalDoubleOps[Value,Value], rationalBoolOps: RationalBoolOps[Value, Value],
         boolOps: BooleanOps[Value], listOps: ListOps[Value], charOps: CharOps[Value], stringOps: StringOps[Value],
         symbolOps: SymbolOps[Value], quoteOps: QuoteOps[Value],voidOps: VoidOps[Value],
         eqOps: EqOps[Value, Value], compareOps: CompareOps[Value, Value],
         closureOps: ClosureOps[String, Value, List[Expr], Environment, Value, Value],
         typeOps: TypeOps[Value], isTypeOps: IfIsTypeOps[Value])
  extends GenericInterpreter[Value, Addr, Environment, Effects]:

  val phi = fix.identity[FixIn[Value], FixOut[Value]]