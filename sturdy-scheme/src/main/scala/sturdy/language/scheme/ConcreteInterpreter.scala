//package sturdy.language.scheme
//
//import org.apache.commons.math3.fraction.Fraction
//
//import sturdy.data.unit
//import sturdy.effect.allocation.CAllocationIntIncrement
//import sturdy.effect.branching.CBoolBranching
//import sturdy.effect.environment.CEnvironment
//import sturdy.effect.store.CStore
//import sturdy.effect.failure.{CFailure, Failure}
//import sturdy.fix
//import sturdy.values.convert.LiftedConvert
//import sturdy.values.ints.{_, given}
//import sturdy.values.rationals.{_, given}
//import sturdy.values.booleans.{_, given}
//import sturdy.values.relational.{_, given}
//import sturdy.values.closures.{_, given}
//import sturdy.values.given
//import sturdy.util
//import GenericInterpreter.*
//
//
//object ConcreteInterpreter:
//  enum Num:
//    case IntVal(i: Int)
//    case RationalVal(r: Rational)
//    case DoubleVal(d: Double)
//
//    def asInt(using f: Failure): Int = this match
//      case IntVal(i: Int) => i
//      case RationalVal(r) if r.f.getDenominator == 1 => r.f.intValue()
//      case DoubleVal(d) if Math.rint(d) == d => d.toInt
//      case _ => f.fail(TypeError, s"Expected Int but got $this")
//    def asRational: Rational = this match
//      case IntVal(i1) => Rational(new Fraction(i1))
//      case RationalVal(r) => r
//      case DoubleVal(d) => Rational(d)
//    def asDouble: Double = this match
//      case IntVal(i: Int) => i.toDouble
//      case RationalVal(r: Rational) => r.f.doubleValue()
//      case DoubleVal(d: Double) => d
//
//  enum Value:
//    import Num._
//
//    case BoolVal(b: Boolean)
//    case CharVal(c: Char)
//    case StringVal(str: String)
//    case NilVal
//    case ConsVal(car: Value, cdr: Value)
//    case SymbolVal(sym: String)
//    case QuoteVal(qot: Value)
//    case VoidVal
//    case ClosureVal(cl: Closure[String, Body, Environment])
//    case NumVal(num: Num)
//
//    def asNum(using f: Failure): Num = this match
//      case NumVal(num) => num
//      case _ => f.fail(TypeError, s"Expected Num but got $this")
//    def asBoolean(using f: Failure): Boolean = this match
//      case BoolVal(b) => b
//      case _ => true
//    def asChar(using f: Failure): Char = this match
//      case CharVal(c) => c
//      case _ => f.fail(TypeError, s"Expected Char but got $this")
//    def asString(using f: Failure): String = this match
//      case StringVal(str) => str
//      case _ => f.fail(TypeError, s"Expected String but got $this")
//    def asSymbol(using f: Failure): String = this match
//      case SymbolVal(sym) => sym
//      case _ => f.fail(TypeError, s"Expected Symbol but got $this")
//    def asQuote(using f: Failure): Value = this match
//      case QuoteVal(qot) => qot
//      case _ => f.fail(TypeError, s"Expected Quote but got $this")
//    def asClosure(using f: Failure) : Closure[String, Body, Environment] = this match
//      case ClosureVal(cl) => cl
//      case _ => f.fail(TypeError, s"Expected Closure but got $this")
//
//  import Value._
//  import Num._
//
//  type Addr = Int
//  type Environment = Map[String, Addr]
//  type Store = Map[Addr, Value]
//
//  class Effects(initEnvironment: Environment, initStore: Store)
//    extends CBoolBranching[Value](using _.asBoolean)
//      with CEnvironment[String, Int](initEnvironment)
//      with CStore[Addr, Value](initStore)
//      with CAllocationIntIncrement[AllocationSite]
//      with CFailure
//
//  def apply(initEnvironment: Environment, initStore: Store): ConcreteInterpreter = {
//    val effects = new Effects(initEnvironment, initStore)
//
//    given Failure = effects
//
//    given IntOps[Value] = new LiftedIntOps(_.asNum.asInt, x => NumVal(IntVal(x)))
//    given RationalOps[Value] = new LiftedRationalOps(_.asNum.asRational, r => NumVal(RationalVal(r)))
//    given DoubleOps[Value] = new LiftedDoubleOps(_.asNum.asDouble, x => NumVal(DoubleVal(x)))
//    given BooleanOps[Value] = new LiftedBooleanOps(_.asBoolean, BoolVal.apply)
//    given CompareOps[Value, Value] = new LiftedCompareOps(_.asNum.asDouble, BoolVal.apply)
//    given ClosureOps[String, Value, Body, Environment, Value, Value] = new LiftedClosureOps(_.asClosure, ClosureVal.apply)
//
//    given concreteListOps(using f: Failure): ListOps[Value] with
//      override def cons(v1: Value, v2: Value): Value = ConsVal(v1, v2)
//      override def nil: Value = NilVal
//      override def car(v: Value): Value = v match
//        case ConsVal(car, _) => car
//        case _ => f.fail(TypeError, s"Expected ConsVal but got $v")
//      override def cdr(v: Value): Value = v match
//        case ConsVal(_, cdr) => cdr
//        case _ => f.fail(TypeError, s"Expected ConsVal but got $v")
//
//    given EqOps[Value, Value] with
//      def equ(v1: Value, v2: Value): Value = (v1, v2) match
//        case (BoolVal(b1), BoolVal(b2)) => BoolVal(b1 == b2)
//        case (NumVal(IntVal(i1)), NumVal(IntVal(i2))) => BoolVal(i1 == i2)
//        case (NumVal(RationalVal(r1)), NumVal(RationalVal(r2))) => BoolVal(r1 == r2)
//        case (NumVal(DoubleVal(d1)), NumVal(DoubleVal(d2))) => BoolVal(d1 == d2)
//        case (CharVal(c1), CharVal(c2)) => BoolVal(c1 == c2)
//        case (StringVal(s1), StringVal(s2)) => BoolVal(s1 == s2)
//        case (QuoteVal(q1), QuoteVal(q2)) => equ(q1, q2)
//        case (NilVal, NilVal) => BoolVal(true)
//        case (ConsVal(car1, cdr1), ConsVal(car2, cdr2)) => BoolVal(equ(car1, car2).asBoolean && equ(cdr1, cdr2).asBoolean)
//        case (_:ClosureVal, _) | (_, _:ClosureVal) => effects.fail(ClosureComparison, s"Cannot compute (= $v1 $v2)")
//        case _ => BoolVal(false)
//      def neq(v1: Value, v2: Value): Value = BoolVal(!equ(v1, v2).asBoolean)
//
//    given QuoteOps[Value] with
//      override def quoteLit(l: Value): Value = QuoteVal(l)
//
//    given SymbolOps[Value] with
//      override def symbolLit(s: String): Value = SymbolVal(s)
//
//    given VoidOps[Value] with
//      def void:Value = VoidVal
//
//    given CharOps[Value] with
//      override def charLit(c: Char): Value = CharVal(c)
//
//    given strings(using f: Failure): StringOps[Value] with
//      override def stringLit(s: String): Value = StringVal(s)
//      override def numberToString(v: Value): Value = v match
//        case NumVal(num) => StringVal(num.toString)
//        case _ => f.fail(TypeError, s"Expected NumVal but got $v")
//      override def stringToSymbol(v: Value): Value = v match
//        case StringVal(s) => QuoteVal(SymbolVal(s))
//        case _ => f.fail(TypeError, s"Expected StringVal but got $v")
//      override def symbolToString(v: Value): Value = v match
//        case QuoteVal(SymbolVal(s)) => StringVal(s)
//        case _ => f.fail(TypeError, s"Expected QuoteVal but got $v")
//      override def stringRef(v1: Value, v2: Value): Value = (v1, v2) match
//        case (StringVal(s), NumVal(IntVal(i))) => CharVal(s.charAt(i))
//        case _ => f.fail(TypeError, s"Expected StringVal and IntVal but got $v1 and $v2")
//      override def stringAppend(v1: Value, v2: Value): Value = (v1, v2) match
//        case (StringVal(s1), StringVal(s2)) => StringVal(s1+s2)
//        case _ => f.fail(TypeError, s"Expected StringVal and IntVal but got $v1 and $v2")
//
//    given TypeOps[Value] with
//      def isNumber(v: Value) : BoolVal = v match
//        case NumVal(_) => BoolVal(true)
//        case _ => BoolVal(false)
//      def isInteger(v: Value): BoolVal = v match
//        case NumVal(IntVal(_)) => BoolVal(true)
//        case _ => BoolVal(false)
//      def isDouble(v:Value ): BoolVal = v match
//        case NumVal(DoubleVal(_)) => BoolVal(true) // all numbers can be used as doubles
//        case _ => BoolVal(false)
//      def isRational(v: Value): BoolVal = v match
//        case NumVal(RationalVal(_)) => BoolVal(true) // all numbers can be used as rationals
//        case _ => BoolVal(false)
//      def isNull(v: Value): BoolVal = BoolVal(v == NilVal)
//      def isCons(v: Value): BoolVal = v match
//        case _: ConsVal => BoolVal(true)
//        case _ => BoolVal(false)
//      def isBoolean(v: Value): Value = v match
//        case BoolVal(_) => BoolVal(true)
//        case _ => BoolVal(false)
//
//    given ConvertIntDouble[Value, Value] = new LiftedConvert(_.asNum.asInt, NumVal.apply compose DoubleVal.apply)
//    given ConvertRationalDouble[Value, Value] = new LiftedConvert(_.asNum.asRational, NumVal.apply compose DoubleVal.apply)
//
//    new ConcreteInterpreter(using effects)
//  }
//
//import ConcreteInterpreter.*
//
//class ConcreteInterpreter
//  (using effectOps: Effects)
//  (using IntOps[Value], ConvertIntDouble[Value, Value],
//   RationalOps[Value], ConvertRationalDouble[Value, Value],
//   DoubleOps[Value],
//   BooleanOps[Value], CharOps[Value], StringOps[Value],
//   ListOps[Value], SymbolOps[Value], QuoteOps[Value], VoidOps[Value],
//   TypeOps[Value], EqOps[Value, Value], CompareOps[Value, Value],
//   ClosureOps[String, Value, Body, effectOps.Env, Value, Value])
//  extends GenericInterpreter[Value, Addr, Effects]:
//
//  val phi = fix.identity[Exp, Value]
