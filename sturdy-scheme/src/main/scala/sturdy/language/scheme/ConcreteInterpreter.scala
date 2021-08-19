//package sturdy.language.scheme
//
//import org.apache.commons.math3.fraction.Fraction
//
//import sturdy.effect.noJoin
//import sturdy.effect.allocation.CAllocationIntIncrement
//import sturdy.effect.branching.CBoolBranching
//import sturdy.effect.environment.CEnvironment
//import sturdy.effect.store.CStore
//import sturdy.effect.failure.{CFailure, Failure}
//import sturdy.fix
//import sturdy.values.conversion.ConvertIntDoubleOps
//import sturdy.values.ints.{_, given}
//import sturdy.values.doubles.{_, given}
//import sturdy.values.rationals.{_, given}
//import sturdy.values.booleans.{_, given}
//import sturdy.values.relational.{_, given}
//import sturdy.values.closures.{_, given}
//import sturdy.values.given
//import sturdy.util
//import GenericInterpreter.*
//
//object ConcreteInterpreter:
//  enum Num:
//    case IntVal(i: Int)
//    case RationalVal(r: Fraction)
//    case DoubleVal(d: Double)
//
//    def asInt: Int = this match
//      case IntVal(i: Int) => i
//      case RationalVal(r) if r.getDenominator == 1 => r.intValue()
//      case DoubleVal(d) if Math.rint(d) == d => d.toInt
//      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")
//    def asRational: Fraction = this match
//      case IntVal(i1) => new Fraction(i1)
//      case RationalVal(r) => r
//      case DoubleVal(d) => ???
//      case _ => throw new IllegalArgumentException(s"Expected IntVal or RationalVal but got $this")
//    def asDouble: Double = this match
//      case IntVal(i: Int) => i.toDouble
//      case RationalVal(i1: Int, i2: Int) => (i1 / i2).toDouble
//      case DoubleVal(d: Double) => d
//
//  enum Value:
//    import Num._
//
//    case BoolVal(b: Boolean)
//    case CharVal(c: Char)
//    case StringVal(str: String)
//    case ListVal(li: List[Value])
//    case SymbolVal(sym: String)
//    case QuoteVal(qot: Value)
//    case VoidVal
//    case ClosureVal(closure: (List[String], Environment, List[Expr]))
//    case NumVal(num: Num)
//
//    def asNum: Num = this match
//      case NumVal(num) => num
//      case _ => throw new IllegalArgumentException(s"Expected Num but got $this")
//    def asBoolean: Boolean = this match
//      case BoolVal(b) => b
//      case _ => throw new IllegalArgumentException(s"Expected Boolean but got $this")
//    def asChar: Char = this match
//      case CharVal(c) => c
//      case _ => throw new IllegalArgumentException(s"Expected Char but got $this")
//    def asString: String = this match
//      case StringVal(str) => str
//      case _ => throw new IllegalArgumentException(s"Expected String but got $this")
//    def asList: List[Value] = this match
//      case ListVal(cons) => cons
//      case _ => throw new IllegalArgumentException(s"Expected List but got $this")
//    def asSymbol: String = this match
//      case SymbolVal(sym) => sym
//      case _ => throw new IllegalArgumentException(s"Expected Symbol but got $this")
//    def asQuote : Value = this match
//      case QuoteVal(qot) => qot
//      case _ => throw new IllegalArgumentException(s"Expected Quote but got $this")
//    def asClosure : (List[String], Environment, List[Expr]) = this match
//      case ClosureVal(cls) => cls
//      case _ => throw new IllegalArgumentException(s"Expected Closure but got $this")
//
//
//  import Value._
//  import Num._
//
//  type Addr = Int
//  type Environment = Map[String, Addr]
//  type Store = Map[Addr, Value]
//
//  class Effects(initEnvironment: Environment, initStore: Store)
//    extends CBoolBranching[Value]
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
//    given IntOps[Value] = new LiftedIntOps[Value, Int](_.asNum.asInt, x => NumVal(IntVal(x)))
//    given RationalOps[Value] = new LiftedRationalOps[Value, Fraction](_.asNum.asRational, r => NumVal(RationalVal(r)))
//    given DoubleOps[Value] = new LiftedDoubleOps[Value, Double](_.asNum.asDouble, x => NumVal(DoubleVal(x)))
//    given BooleanOps[Value] = new LiftedBooleanOps[Value, Boolean](_.asBoolean, BoolVal.apply)
//    given CompareOps[Value, Value] = new LiftedCompareOps[Value, Value, Double, Boolean](_.asNum.asDouble, BoolVal.apply)
//    given ClosureOps[String, Value, List[Expr], Environment, Value, Value] = new LiftedClosureOps[String, Value, List[Expr], Environment, Value, Value, (List[String], Environment, List[Expr])](_.asClosure, ClosureVal.apply)
//
//    given concreteListOps(using f: Failure): ListOps[Value] with
//      override def cons(v1: Value, v2: Value): Value = v2 match
//        case ListVal(vs) => ListVal(v1 :: vs)
//        case _ => throw new IllegalArgumentException(s"Expected ListVal but got $v2")
//      override def nil: Value = ListVal(Nil)
//      override def car(v: Value): Value = v match
//        case ListVal(vs) => vs match
//          case Nil => f.fail(NullDeconstruct, s"(car $v)")
//          case head::_ => head
//        case _ => throw new IllegalArgumentException(s"Expected ListVal but got $v")
//      override def cdr(v: Value): Value = v match
//        case ListVal(vs) => vs match
//          case Nil => f.fail(NullDeconstruct, s"(cdr $v)")
//          case _::tail => ListVal(tail)
//        case _ => throw new IllegalArgumentException(s"Expected ListVal but got $v")
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
//        case (ListVal(vs1), ListVal(vs2)) if vs1.size == vs2.size =>
//          BoolVal(vs1.zip(vs2).forall((v1, v2) => equ(v1, v2).asBoolean))
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
//    given StringOps[Value] with
//      override def stringLit(s: String): Value = StringVal(s)
//      override def numberToString(v: Value): Value = v match
//        case NumVal(num) => StringVal(num.toString)
//        case _ => throw new IllegalArgumentException(s"Expected NumVal but got $v")
//      override def stringToSymbol(v: Value): Value = v match
//        case StringVal(s) => QuoteVal(SymbolVal(s))
//        case _ => throw new IllegalArgumentException(s"Expected StringVal but got $v")
//      override def symbolToString(v: Value): Value = v match
//        case QuoteVal(SymbolVal(s)) => StringVal(s)
//        case _ => throw new IllegalArgumentException(s"Expected QuoteVal but got $v")
//      override def stringRef(v1: Value, v2: Value): Value = (v1, v2) match
//        case (StringVal(s), NumVal(IntVal(i))) => CharVal(s.charAt(i))
//        case _ => throw new IllegalArgumentException(s"Expected StringVal and IntVal but got $v1 and $v2")
//      override def stringAppend(v1: Value, v2: Value): Value = (v1, v2) match
//        case (StringVal(s1), StringVal(s2)) => StringVal(s1+s2)
//        case _ => throw new IllegalArgumentException(s"Expected StringVal and IntVal but got $v1 and $v2")
//
//    given TypeOps[Value] with
//      def isNumber(v: Value) : BoolVal = v match
//        case NumVal(_) => BoolVal(true)
//        case _ => BoolVal(false)
//      def isInteger(v: Value): BoolVal = v match
//        case NumVal(IntVal(_)) => BoolVal(true)
//        case _ => BoolVal(false)
//      def isDouble(v:Value ): BoolVal = v match
//        case NumVal(DoubleVal(_)) => BoolVal(true)
//        case _ => BoolVal(false)
//      def isRational(v: Value): BoolVal = v match
//        case NumVal(RationalVal(_,_)) => BoolVal(true)
//        case _ => BoolVal(false)
//      def isNull(v: Value): BoolVal = v match
//        case ListVal(Nil) => BoolVal(true)
//        case _ => BoolVal(false)
//      def isCons(v: Value): BoolVal = v match
//        case ListVal(_::_) => BoolVal(true)
//        case _ => BoolVal(false)
//      def isBoolean(v: Value): Value = v match
//        case BoolVal(_) => BoolVal(true)
//        case _ => BoolVal(false)
//
//    new ConcreteInterpreter(using effects)
//  }
//
//import ConcreteInterpreter.*
//
//class ConcreteInterpreter
//  (using effectOps: Effects)
//  (using val intOps: IntOps[Value], intDoubleOps: ConvertIntDoubleOps[Value, Value],
//             rationalOps: RationalOps[Value], doubleRationalOps: ConvertDoubleRationalOps[Value, Value],
//             doubleOps: DoubleOps[Value],
//             boolOps: BooleanOps[Value], charOps: CharOps[Value], stringOps: StringOps[Value],
//             listOps: ListOps[Value], symbolOps: SymbolOps[Value], quoteOps: QuoteOps[Value], voidOps: VoidOps[Value],
//             typeOps: TypeOps[Value],
//             eqOps: EqOps[Value, Value], compareOps: CompareOps[Value, Value],
//             closureOps: ClosureOps[String, Value, List[Expr], effectOps.Env, Value, Value])
//  extends GenericInterpreter[Value, Addr, Effects]:
//
//  val phi = fix.identity[FixIn[Value], FixOut[Value]]