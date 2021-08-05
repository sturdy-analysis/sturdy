package sturdy.language.schemelang

import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.branching.CBoolBranching
import sturdy.effect.environment.CEnvironment
import sturdy.effect.store.CStore
import sturdy.effect.failure.{CFailure, Failure}
import sturdy.fix
import scala.math.{BigInt, log10}
import sturdy.values.numerics.NumericOps
import sturdy.values.numerics.{_, given}
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
  enum Num:
    case IntVal(i: Int)
    case DoubleVal(d: Double)
    def asInt: Int = this match {
      case IntVal(i: Int) => i
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
    }
    def asDouble: Double = this match {
      case DoubleVal(d: Double) => d
      case _ => throw new IllegalArgumentException(s"Expected Double but got $this")
    }


  enum Value:
    import Num._

    case BoolVal(b: Boolean)
    case CharVal(c: Char)
    case StringVal(str: String)
    case SymbolVal(sym: String)
    case QuoteVal(qot: Literal)
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
  import Num._

  def withNumInt1ToNum(v: Value)
                  (opInt: Int => Int): Value = v match
    case NumVal(IntVal(i)) => NumVal(IntVal(opInt(i)))
    case _ => throw new IllegalArgumentException(s"Expected IntVal but got $v")

  def withNum1ToBool(v: Value)
                    (opInt:Int => Boolean)
                    (opDouble:Double => Boolean): Value = v match
    case NumVal(IntVal(i)) => BoolVal(opInt(i))
    case NumVal(DoubleVal(d)) => BoolVal(opDouble(d))
    case _ => throw new IllegalArgumentException(s"Expected NumVal but got $v")

  // TODO
//  def withNum1ToBool_[N](v: Value)(op: Numeric[N] => Boolean): Value = v match
//    case NumVal(IntVal(i)) => BoolVal(op(i))
//    case NumVal(DoubleVal(d)) => BoolVal(op(d))
//    case _ => throw new IllegalArgumentException(s"Expected NumVal but got $v")

  def withNum1ToNum(v: Value)
              (opInt: Int => Int)
              (opDouble:Double => Double): Value = v match
    case NumVal(IntVal(i)) => NumVal(IntVal(opInt(i)))
    case NumVal(DoubleVal(d)) => NumVal(DoubleVal(opDouble(d)))
    case _ => throw new IllegalArgumentException(s"Expected NumVal but got $v")

  def withNum1ToNumDouble(v: Value)
                   (opInt: Int => Double)
                   (opDouble:Double => Double): Value = v match
    case NumVal(IntVal(i)) => NumVal(DoubleVal(opInt(i)))
    case NumVal(DoubleVal(d)) => NumVal(DoubleVal(opDouble(d)))
    case _ => throw new IllegalArgumentException(s"Expected NumVal but got $v")

  inline def withNum2ToNum(v1: Value, v2: Value)
              (opInt: (Int, Int) => Int)
              (opDouble:(Double, Double) => Double): Value = (v1,v2) match
    case (NumVal(IntVal(i1)), NumVal(IntVal(i2))) => NumVal(IntVal(opInt(i1, i2)))
    case (NumVal(DoubleVal(d1)), NumVal(DoubleVal(d2))) => NumVal(DoubleVal(opDouble(d1,d2)))
    case (NumVal(IntVal(i)), NumVal(DoubleVal(d))) => NumVal(DoubleVal(opDouble(i, d)))
    case (NumVal(DoubleVal(d)), NumVal(IntVal(i))) => NumVal(DoubleVal(opDouble(i, d)))
    case _ => throw new IllegalArgumentException(s"Expected NumVal, NumVal but got $v1, $v2")

//  def withNum2ToNum_(v1: Value, v2: Value): Value = (v1,v2) match
//    case (NumVal(IntVal(i1)), NumVal(IntVal(i2))) => summon[NumericOps[Int]].add(i1,i2)
//    case (NumVal(DoubleVal(d1)), NumVal(DoubleVal(d2))) => summon[NumericOps[Double]].add(d1,d2)
//    case (NumVal(IntVal(i1)), NumVal(DoubleVal(d2))) => summon[NumericOps[Double]].add(i1.toDouble,d2)
//    case (NumVal(DoubleVal(d1)), NumVal(IntVal(i2))) => summon[NumericOps[Double]].add(d1,i2.toDouble)
//    case _ => throw new IllegalArgumentException(s"Expected NumVal, NumVal but got $v1, $v2")

  def withNumInt2ToNum(v1: Value, v2: Value)
                      (opInt: (Int, Int) => Int): Value = (v1,v2) match
    case (NumVal(IntVal(i1)), NumVal(IntVal(i2))) => NumVal(IntVal(opInt(i1, i2)))
    case _ => throw new IllegalArgumentException(s"Expected IntVal, IntVal but got $v1, $v2")

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
    given DoubleOps[Value] = new LiftedDoubleOps[Value, Double](_.asNum.asDouble, x => NumVal(DoubleVal(x)))
    given NumericOps[Value] with
      override def isZero(v: Value): Value = withNum1ToBool(v)(_ == 0)(_ == 0)
      override def isPositive(v: Value): Value = withNum1ToBool(v)(_ >= 0)(_ >= 0)
      override def isNegative(v: Value): Value = withNum1ToBool(v)(_ < 0)(_ < 0)
      override def isOdd(v: Value): Value = withNum1ToBool(v)(_ % 2 == 1)(_ % 2 == 1)
      override def isEven(v: Value): Value = withNum1ToBool(v)(_ % 2 == 0)(_ % 2 == 0)
      override def abs(v: Value): Value = withNum1ToNum(v)(_.abs)(x => x.abs)
      override def floor(v: Value): Value = withNum1ToNum(v)(identity)(_.floor)
      override def ceiling(v: Value): Value = withNum1ToNum(v)(identity)(_.ceil)
      override def log(v: Value): Value = withNum1ToNumDouble(v)(x => log10(x))(x => log10(x))
      override def quotient(v1: Value, v2: Value): Value = withNumInt2ToNum(v1, v2)(_ / _)
      override def remainder(v1: Value, v2: Value): Value = withNumInt2ToNum(v1, v2)((x, y) => (x % y).abs)
      override def modulo(v1: Value, v2: Value): Value = withNumInt2ToNum(v1, v2)(_ % _)
      override def max(v1: Value, v2: Value): Value = withNum2ToNum(v1, v2)(_.max(_))(_.max(_))
      override def min(v1: Value, v2: Value): Value = withNum2ToNum(v1, v2)(_.min(_))(_.min(_))
      override def add(v1: Value, v2: Value): Value = withNum2ToNum(v1, v2)(_ + _)(_ + _)
//        (summon[NumericOps[Int]].add)(summon[NumericOps[Double]].add)
      override def mul(v1: Value, v2: Value): Value = withNum2ToNum(v1, v2)(_ * _)(_ * _)
      override def sub(v1: Value, v2: Value): Value = withNum2ToNum(v1, v2)(_ - _)(_ - _)
      override def div(v1: Value, v2: Value): Value = withNum2ToNum(v1, v2)(_ / _)(_ / _)
      override def gcd(v1: Value, v2: Value): Value = withNumInt2ToNum(v1, v2)((x, y) => BigInt(x).gcd(y).toInt)
      override def lcm(v1: Value, v2: Value): Value = withNumInt2ToNum(v1, v2)((x, y) => (x*y).abs / BigInt(x).gcd(y).toInt)
    given BooleanOps[Value] = new LiftedBooleanOps[Value, Boolean](_.asBoolean, BoolVal.apply)
    given CharOps[Value] = new LiftedCharOps[Value, Char](_.asChar, CharVal.apply)
    given StringOps[Value] = new LiftedStringOps[Value, String](_.asString, StringVal.apply)
    given SymbolOps[Value] = new LiftedSymbolOps[Value, String](_.asSymbol, SymbolVal.apply)
    given QuoteOps[Literal, Value] = new LiftedQuoteOps[Literal, Value, Literal](_.asQuote, QuoteVal.apply)
    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, Int, Boolean](_.asNum.asInt, BoolVal.apply) // Todo: Fix
    given VoidOps[Value] with
      def void():Value = VoidVal
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = (v1, v2) match
        case (BoolVal(b1), BoolVal(b2)) => BoolVal(b1 == b2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      def neq(v1: Value, v2: Value): Value = (v1, v2) match
        case (BoolVal(b1), BoolVal(b2)) => BoolVal(b1 != b2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    given TypeOps[Value] with // TODO: maybe simplify this by using try .. catch on e.g. asInt
      def isNumber(v: Value) : BoolVal = v match
        case NumVal(_) => BoolVal(true)
        case _ => BoolVal(false)
      def isInteger(v: Value): BoolVal = v match
        case NumVal(IntVal(_)) => BoolVal(true)
        case _ => BoolVal(false)
      def isDouble(v:Value ): BoolVal = v match
        case NumVal(DoubleVal(_)) => BoolVal(true)
        case _ => BoolVal(false)
      def isRational(v: Value): BoolVal = ???
      def isNull(v: Value): BoolVal = ???
      def isCons(v: Value): BoolVal = ???
      def isBoolean(v: Value): Value = v match
        case BoolVal(_) => BoolVal(true)
        case _ => BoolVal(false)
    given ClosureOps[String, Value, List[Expr], Environment, Value, Value] =
      new LiftedClosureOps[String, Value, List[Expr], Environment, Value, Value, (List[String], Environment, List[Expr])](_.asClosure, ClosureVal.apply)

    new ConcreteInterpreter(using effects)
  }

import ConcreteInterpreter.*

class ConcreteInterpreter
  (using effectOps: Effects)
  (using intOps: IntOps[Value], doubleOps: DoubleOps[Value],
         numericOps: NumericOps[Value],
         boolOps: BooleanOps[Value], eqOps: EqOps[Value, Value], compareOps: CompareOps[Value, Value],
         charOps: CharOps[Value], stringOps: StringOps[Value],
         symbolOps: SymbolOps[Value], quoteOps: QuoteOps[Literal, Value],
         closureOps: ClosureOps[String, Value, List[Expr], Environment, Value, Value],
         voidOps: VoidOps[Value], typeOps: TypeOps[Value])
  extends GenericInterpreter[Value, Addr, Environment, Effects]:

  val phi = fix.identity[FixIn[Value], FixOut[Value]]