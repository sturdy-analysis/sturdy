package sturdy.values.integer

import sturdy.data.{NoJoin, joinComputations, noJoin, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.relational.*
import Numeric.Implicits.infixNumericOps

case class Congruence(c: Int, m: Int)

object Congruence:
  val top = Congruence(0,1)

  def normalize(v : Congruence) : Congruence =
    if v.m != 0 then
      val newM = v.m.abs
      val newC = v.c%newM
      if newC < 0 then
        Congruence(newC+newM, newM)
      else
        Congruence(newC, newM)
    else
      v

  def gcdInt(a : Int, b : Int) : Int = BigInt(a).gcd(BigInt(b)).toInt
  def gcdInt(a : Int, b : Int, c : Int) : Int = gcdInt(a,gcdInt(b,c))

  def join(v1 : Congruence, v2 : Congruence) : Congruence =
    val newM = gcdInt(v1.m, v2.m, v1.c - v2.c)
    normalize(Congruence(v1.c, newM))

// use of (using ops: IntegerOps[I, I], num: Numeric[I]) for generics if needed
given CongruenceIntegerOps(using f: Failure, j: EffectStack): IntegerOps[Int, Congruence] with
  import Congruence.*

  def integerLit(i: Int): Congruence = Congruence(i, 0)
  def randomInteger() : Congruence = Congruence(0,1)

  def add(v1: Congruence, v2: Congruence): Congruence =
    val newM = gcdInt(v1.m, v2.m)
    normalize(Congruence(v1.c+v2.c, newM))

  def sub(v1: Congruence, v2: Congruence): Congruence =
    val newM = gcdInt(v1.m, v2.m)
    normalize(Congruence((v1.c-v2.c), newM))

  def mul(v1: Congruence, v2: Congruence): Congruence =
    val newM = gcdInt(v1.m*v2.m, v1.c*v2.m, v2.c*v1.m)
    normalize(Congruence(v1.c*v2.c, newM))

  def max(v1: Congruence, v2: Congruence) : Congruence = (v1, v2) match
    case (Congruence(c1,0), Congruence(c2,0)) => if c1 > c2 then v1 else v2
    case _ => join(v1,v2)

  def min(v1: Congruence, v2: Congruence) : Congruence = (v1, v2) match
    case (Congruence(c1,0), Congruence(c2,0)) => if c1 < c2 then v1 else v2
    case _ => join(v1, v2)

  def absolute(v: Congruence): Congruence = v match
    case Congruence(c, 0) => Congruence(c.abs, 0)
    case Congruence(0, _) => v // TODO : CHECK
    case _ => join(v, normalize(Congruence(-v.c, v.m)))

  private inline def divByZero(v1: Congruence, v2: Congruence) = f.fail(IntegerDivisionByZero, s"$v1 / $v2")

  def div(v1: Congruence, v2: Congruence): Congruence =
    def divBy(nonzeroDenom: Congruence): Congruence = (v1, nonzeroDenom) match
      case (Congruence(a, 0), Congruence(d, 0)) => Congruence(a/d, 0)
      case (_, Congruence(d,0)) => if d%v1.m == 0 then normalize(Congruence(v1.c/d, v1.m/d)) else top
      case (Congruence(a,0), _) =>
        val N = nonzeroDenom.m * ((a-nonzeroDenom.c)/nonzeroDenom.m) + nonzeroDenom.c
        if N <= 0 then Congruence(0,0) else Congruence(0,a/N)
      case _ => top

    v2 match
      case Congruence(0, 0) => divByZero(v1, v2)
      case Congruence(0, _) => joinComputations(divBy(v2))(divByZero(v1, v2))
      case _ => divBy(v2)

  def divUnsigned(v1: Congruence, v2: Congruence): Congruence = ???

  def remainder(v1: Congruence, v2: Congruence): Congruence =
    def remainderBy(nonzeroDenom: Congruence): Congruence = (v1, nonzeroDenom) match
      case (_, Congruence(d,0)) => if d%v1.m == 0 then Congruence(v1.c%d, 0) else normalize(Congruence(v1.c, gcdInt(v1.m, d)))
      case (Congruence(a,0), _) =>
        val N = nonzeroDenom.m * ((a-nonzeroDenom.c)/nonzeroDenom.m) + nonzeroDenom.c
        if N <= 0 then Congruence(a,0)
        else if a/N>=2 then normalize(Congruence(a, N*(a/N)))
        else normalize(Congruence(a, gcdInt(nonzeroDenom.c,nonzeroDenom.m)))
      case _ => normalize(Congruence(v1.c, gcdInt(v1.m,v2.m,v2.c)))

    v2 match
      case Congruence(0, 0) => divByZero(v1, v2)
      case Congruence(0, _) => joinComputations(remainderBy(v2))(divByZero(v1, v2))
      case _ => remainderBy(v2)

  def remainderUnsigned(v1: Congruence, v2: Congruence): Congruence = ???

  def modulo(v1: Congruence, v2: Congruence): Congruence = remainder(v1, v2) // TODO : Check specification

  def gcd(v1: Congruence, v2: Congruence): Congruence = (v1,v2) match
    case (Congruence(_,0),Congruence(_,0)) => Congruence(gcdInt(v1.c, v2.c), 0)
    case _ => top

  // TODO : Bit operations could be more precise, in particular with congruence classes modulo 2^n

  def bitAnd(v1: Congruence, v2: Congruence): Congruence = (v1,v2) match
    case (Congruence(c1, 0), Congruence(c2, 0)) => normalize(Congruence(c1 & c2, 0))
    case _ => top

  def bitOr(v1: Congruence, v2: Congruence): Congruence = (v1, v2) match
    case (Congruence(c1, 0), Congruence(c2, 0)) => normalize(Congruence(c1 | c2, 0))
    case _ => top

  def bitXor(v1: Congruence, v2: Congruence): Congruence = (v1, v2) match
    case (Congruence(c1, 0), Congruence(c2, 0)) => normalize(Congruence(c1 ^ c2, 0))
    case _ => top

  def shiftLeft(v: Congruence, shift: Congruence): Congruence = (v, shift) match
    case (Congruence(c1, 0), Congruence(c2, 0)) => normalize(Congruence(c1 << c2, 0))
    case _ => top

  def shiftRight(v: Congruence, shift: Congruence): Congruence = (v, shift) match
    case (Congruence(c1, 0), Congruence(c2, 0)) => normalize(Congruence(c1 >> c2, 0))
    case _ => top

  def shiftRightUnsigned(v: Congruence, shift: Congruence): Congruence = (v, shift) match
    case (Congruence(c1, 0), Congruence(c2, 0)) => normalize(Congruence(c1 >>> c2, 0))
    case _ => top

  def rotateLeft(v: Congruence, shift: Congruence): Congruence = (v, shift) match
    case (Congruence(c1, 0), Congruence(c2, 0)) => normalize(Congruence(Integer.rotateLeft(c1, c2), 0))
    case _ => top

  def rotateRight(v: Congruence, shift: Congruence): Congruence = (v, shift) match
    case (Congruence(c1, 0), Congruence(c2, 0)) => normalize(Congruence(Integer.rotateRight(c1,c2), 0))
    case _ => top

  def countLeadingZeros(v: Congruence): Congruence = v match
    case Congruence(c1, 0) => normalize(Congruence(Integer.numberOfLeadingZeros(c1), 0))
    case _ => top

  def countTrailingZeros(v: Congruence): Congruence = v match
    case Congruence(c1, 0) => normalize(Congruence(Integer.numberOfTrailingZeros(c1), 0))
    case _ => top

  def nonzeroBitCount(v: Congruence): Congruence = v match
    case Congruence(c1, 0) => normalize(Congruence(Integer.bitCount(c1), 0))
    case _ => top

given TopCongruence: Top[Congruence] with
  override def top: Congruence = Congruence.top

given CongruenceOrdering(using Ordering[Int]): PartialOrder[Congruence] with
  import Congruence.*
  override def lteq(x: Congruence, y: Congruence): Boolean = (x, y) match
    case (Congruence(0,1), _) => false
    case (_, Congruence(0,1)) => true
    case (Congruence(m1, c1), Congruence(m2, 0)) => c1 == 0 && m1 == m2
    case (Congruence(c1, m1), Congruence(c2, m2)) =>
      gcdInt(c1-c2, m1)%m2 == 0

given CongruenceJoin(using Ordering[Int]): Join[Congruence] with
  import Congruence.*
  override def apply(v1: Congruence, v2: Congruence): MaybeChanged[Congruence] = (v1, v2) match
    case (Congruence(0,1), _) => MaybeChanged.Unchanged(v1)
    case (_, Congruence(0,1)) => MaybeChanged.Changed(v2)
    case (Congruence(c1, m1), Congruence(c2, m2)) =>
      MaybeChanged(normalize(Congruence(c1, gcdInt(m1, gcdInt(m2, c1-c2)))), v1)

given CongruenceWiden(using Numeric[Int]): Widen[Congruence] with
  override def apply(v1: Congruence, v2: Congruence): MaybeChanged[Congruence] = CongruenceJoin.apply(v1,v2)

given CongruenceOrderingOps(using Ordering[Int]): OrderingOps[Congruence, Topped[Boolean]] with
  import Congruence.*
  def lt(v1: Congruence, v2: Congruence): Topped[Boolean] = (v1, v2) match
    case (Congruence(a, 0), Congruence(b, 0)) => Topped.Actual(a < b)
    case _ => Topped.Top
  def le(v1: Congruence, v2: Congruence): Topped[Boolean] = (v1, v2) match
    case (Congruence(a, 0), Congruence(b, 0)) => Topped.Actual(a <= b)
    case _ => Topped.Top

given CongruenceUnsignedOrderingOps(using ops: UnsignedOrderingOps[Int, Boolean]): UnsignedOrderingOps[Congruence, Topped[Boolean]] with
  def ltUnsigned(v1: Congruence, v2: Congruence): Topped[Boolean] = CongruenceOrderingOps.lt(v1, v2)
  def leUnsigned(v1: Congruence, v2: Congruence): Topped[Boolean] = CongruenceOrderingOps.le(v1, v2)

given CongruenceEqOps(using Ordering[Int]): EqOps[Congruence, Topped[Boolean]] with
  import Congruence.*

  override def equ(v1: Congruence, v2: Congruence): Topped[Boolean] = (v1, v2) match
    case (Congruence(0,1), _) => Topped.Top
    case (_, Congruence(0,1)) => Topped.Top
    case (Congruence(c1, 0), Congruence(c2, 0)) => Topped.Actual(c1 == c2)
    case _ => if (v1.c - v2.c)%gcdInt(v1.m, v2.m) == 0 then Topped.Top else Topped.Actual(false)

  override def neq(v1: Congruence, v2: Congruence): Topped[Boolean] = (v1, v2) match
    case (Congruence(0,1), _) => Topped.Top
    case (_, Congruence(0,1)) => Topped.Top
    case (Congruence(c1, 0), Congruence(c2, 0)) => Topped.Actual(c1 != c2)
    case _ => if (v1.c - v2.c)%gcdInt(v1.m, v2.m) == 0 then Topped.Top else Topped.Actual(true)

given CongruenceAbstractly: Abstractly[Int, Congruence] with
  override def apply(i: Int): Congruence =
    Congruence(i, 0)
