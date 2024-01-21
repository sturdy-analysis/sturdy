package sturdy.values.integer

//starting off with NumericalInterval setup but with ints
import sturdy.data.{JOptionA, JOptionC, JOptionPowerset, NoJoin, SomeJOption, joinComputations, joinWithFailure, noJoin, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.booleans.*
import sturdy.values.config.Bits
import sturdy.values.config.UnsupportedConfiguration
import sturdy.values.convert.*
import sturdy.values.relational.*

import java.nio.{ByteBuffer, ByteOrder}
import java.lang.Math
import scala.collection.immutable.{AbstractSeq, LinearSeq, TreeSet}
import Ordering.Implicits.infixOrderingOps
import Numeric.Implicits.infixNumericOps
import Integral.Implicits.infixIntegralOps
import scala.annotation.tailrec
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.Breaks.{break, breakable}
import scala.math.*
import scala.io.StdIn.readLine
import scala.language.implicitConversions //just for test

//tested
enum AbstractBit:
  case Zer
  case One
  case Bit

  override def toString: String = this match
    case Zer => "0"
    case One => "1"
    case Bit => "*"
  def toBoolean: Topped[Boolean] = this match
    case Zer => Topped.Actual(false)
    case One => Topped.Actual(true)
    case Bit => Topped.Top
  def fromBoolean(t: Topped[Boolean]): AbstractBit = t match
    case Topped.Actual(false) => Zer
    case Topped.Actual(true) => One
    case Topped.Top => Bit
  def and(a: AbstractBit): AbstractBit = (this, a) match
    case (One, _) => a
    case (_, One) => this
    case (Zer, _) | (_, Zer) => Zer
    case _ => Bit
  def or(a: AbstractBit): AbstractBit = (this, a) match
    case (One, _) | (_, One) => One
    case (Zer, _) => a
    case (_, Zer) => this
    case _ => Bit
  def not: AbstractBit = this match
    case One => Zer
    case Zer => One
    case Bit => Bit
  def xor(a: AbstractBit): AbstractBit = (this, a) match
    case (One, One) | (Zer, Zer) => Zer
    case (Zer, One) | (One, Zer) => One
    case _ => Bit

  def add(a: AbstractBit, c: AbstractBit = Zer): (AbstractBit,AbstractBit) = (this,a,c) match //addition with carry
    case (One,One,_) => (One,c)
    case (One,_,One) => (One,a)
    case (_,One,One) => (One,this)
    case (Zer,Zer,_) => (Zer,c)
    case (Zer,_,Zer) => (Zer,a)
    case (_,Zer,Zer) => (Zer,this)
    case _ => (Bit,Bit)

  def subtypeOf(a: AbstractBit): Boolean = (this, a) match
    case (One, One) | (Zer, Zer) | (_, Bit) => true
    case _ => false
  def joinWith(a: AbstractBit): MaybeChanged[AbstractBit] = (this, a) match
    case (One, One) => Unchanged(a)
    case (Zer, Zer) => Unchanged(a)
    case (_, Bit) => Unchanged(a)
    case _ => Changed(Bit)
  def lt(a: AbstractBit): Topped[Boolean] = (this, a) match
    case (Zer, One) => Topped.Actual(true)
    case (_, Zer) | (One, _) => Topped.Actual(false)
    case _ => Topped.Top
  def le(a: AbstractBit): Topped[Boolean] = (this, a) match
    case (Zer, _) | (_, One) => Topped.Actual(true)
    case (One, Zer) | (One, Zer) => Topped.Actual(false)
    case _ => Topped.Top
  def equ(a: AbstractBit): Topped[Boolean] = (this, a) match
    case (One, One) | (Zer, Zer) => Topped.Actual(true)
    case (Zer, One) | (One, Zer) => Topped.Actual(false)
    case _ => Topped.Top
  def neq(a: AbstractBit): Topped[Boolean] = this.xor(a).toBoolean

import AbstractBit.*

//tested
object AbstractBitVector: //I can be Int or Long
  def constant[I](i: I)(using Numeric[I], Ordering[I]): AbstractBitVector[I] =
    val size = if i.isInstanceOf[Int] then 32 else 64
    AbstractBitVector((i.toLong.toBinaryString.reverse.toCharArray.map(x=>if x=='0' then Zer else One)
      ++ Array.fill(size)(Zer)).take(size)) //had to make it long and then do stuff...

//tested
case class AbstractBitVector[I](bits: Array[AbstractBit]):
  import AbstractBitVector.*

  val indices: Array[Int] = (0 to 31).toArray

  override def equals(obj: Any): Boolean = obj match
    case vector: AbstractBitVector[I] => this.bits sameElements vector.bits
    case _ => super.equals(obj)

  override def hashCode(): Int = this.bits.hashCode()

  //print as bitstring with leading sign
  override def toString: String = bits.map(x => x.toString).mkString("").reverse

  //check if abstraction covers specific num
  def containsNum(using num: Numeric[I], o: Ordering[I], ord: AbstractBitVectorOrdering[I])(n: I): Boolean = ord.lteq(constant(n),AbstractBitVector(bits))

  //false if 0, true if non-zero, top if unsure
  def toBoolean: Topped[Boolean] = if bits.contains(One) then Topped.Actual(true)
    else if bits.contains(Bit) then Topped.Top else Topped.Actual(false)

  //how many nums in this abstraction
  def countOfNums(using num: Numeric[I]): BigInt = BigInt(1 << (bits.count(x => x == Bit))) //only int for now

  //decomp into signs
  def getDecomposition: BitDecomposition[I] = BitDecomposition(
    lessZero = if bits.last != Zer then Some(AbstractBitVector(bits.take(bits.length-1):+One)) else None,
    hasZero = !bits.contains(One),
    geqZero = if bits.last != One then Some(AbstractBitVector(bits.take(bits.length-1):+Zer)) else None
  )

  //is a single num if there are no unsure bits
  def isConstant: Boolean = !bits.contains(Bit)

  //is top if only unsure bits
  def isTop: Boolean = bits.count(x => x==Bit) == bits.length //or: !bits.contains(Zer) && !bits.contains(One)

//leqZero can't be shown well, same with greaterZero if there are no Ones, abstraction will always include unwanted values
//solution -> asymmetric abstraction
case class BitDecomposition[I](lessZero: Option[AbstractBitVector[I]], hasZero: Boolean, geqZero: Option[AbstractBitVector[I]])

given StandardAbstractBitVectorIntegerOps[I](using Failure, EffectStack, Numeric[I], Ordering[I]): AbstractBitVectorIntegerOps[I] =
  new AbstractBitVectorIntegerOps

//tested
class AbstractBitVectorIntegerOps[I]
  (using f: Failure, j: EffectStack, num: Numeric[I], ord: Ordering[I],
   ao: AbstractBitVectorOrderingOps[I], ae: AbstractBitVectorEqOps[I], au: AbstractBitVectorUnsignedOrderingOps[I])
  extends IntegerOps[I, AbstractBitVector[I]]:

  import AbstractBitVector.constant

  val top: AbstractBitVector[I] = AbstractBitVector(constant(num.fromInt(0)).bits.map(_ => Bit))
  private val one = num.fromInt(1)
  private val zero = num.fromInt(0)

  def integerLit(i: I): AbstractBitVector[I] = AbstractBitVector.constant(i)
  def randomInteger(): AbstractBitVector[I] = top

  private inline def toNum(x: AbstractBitVector[I]): I =
    num.fromInt((for i <- x.bits.indices yield if x.bits(i) == One then 1 << i else 0).sum) //figure out for long...

  private def changeBitValue(v: AbstractBitVector[I], pos: Int, value: AbstractBit): AbstractBitVector[I] =
    if (pos >= 0 && pos < v.bits.length)
      AbstractBitVector(v.bits.slice(0,pos) ++ Array(value) ++ v.bits.slice(pos +1, v.bits.length))
    else throw ArrayIndexOutOfBoundsException()

  def AbstractBitVectorToList(x: AbstractBitVector[I]): ArrayBuffer[I] =
    var result = ArrayBuffer[I]()
    val n = x.bits.length-1
    if x.isConstant then return result :+ toNum(x)
    if x.bits(n) != Zer then //all negative numbers first
      val xNeg = x.bits.take(n):+One //all negative numbers
      val bitIndices = xNeg.indices.filter(i => x.bits(i) == Bit) //all indices that can be changed
      val nums = (0 to (AbstractBitVector(xNeg).countOfNums-1).toInt)
      for (i <- nums) {
        val iBit = constant(i).bits //bits to replace Bit
        val xNew = for (i <- xNeg.indices) yield (if bitIndices.contains(i) then iBit(bitIndices.indexWhere(_ == i)) else xNeg(i))
        result = result:+toNum(AbstractBitVector(xNew.toArray))
      }
    if x.bits(n) != One then //all positive numbers next
      val xPos = x.bits.take(n):+Zer //all nonnegative numbers
      val bitIndices = xPos.indices.filter(i => x.bits(i) == Bit) //correct indices
      val nums = (0 to (AbstractBitVector(xPos).countOfNums-1).toInt)
      for (i <- nums) {
        val iBit = constant(i).bits //correct bits
        val xNew = for (i <- xPos.indices) yield (if bitIndices.contains(i) then iBit(bitIndices.indexWhere(_ == i)) else xPos(i))
        result = result:+toNum(AbstractBitVector(xNew.toArray))
      }
    result

  def joinMultipleAbstractBits(i: Iterable[AbstractBit]): AbstractBit =
    i.tail.foldLeft(i.head) {
      (jB,cB) => jB.joinWith(cB).get
    }

  def joinMultipleAbstractBitVectors(bitvecs: Iterable[AbstractBitVector[I]]): AbstractBitVector[I] =
    bitvecs.tail.foldLeft(bitvecs.head) {
      (jB,cB) => AbstractBitVectorJoin(jB,cB).get
    }

  //needed for subtraction at least, for signed only
  private inline def complement(v: AbstractBitVector[I]): AbstractBitVector[I] = add(invertBits(v),constant(one)) //2s complement

  private inline def meet(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): Option[AbstractBitVector[I]] = //meet between two abstractions if it exists
    val poss = v1.bits.indices.forall(i => v1.bits(i) == v2.bits(i) || v1.bits(i) == Bit || v2.bits(i) == Bit)
    if !poss then None
    else Some(AbstractBitVector((for (i<-v1.bits.indices) yield
      if v1.bits(i) == Bit then v2.bits(i) else if v2.bits(i) == Bit then v1.bits(i) else v1.bits(i)).toArray))

  def add(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] = addWithCarry(v1,v2)
  private inline def addWithCarry(v1: AbstractBitVector[I], v2: AbstractBitVector[I], c: AbstractBit = Zer): AbstractBitVector[I] =
    var carry = c
    AbstractBitVector((for (i <- v1.indices) yield { //iterate over indices
      val intermediary = v1.bits(i).add(v2.bits(i), carry) //add with current carry to get (newCarry, result)
      carry = intermediary._1 //update carry
      intermediary._2 //return result
    }))

  def sub(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] = addWithCarry(v1,invertBits(v2),One) //more accurate than complement

  def neg(v: AbstractBitVector[I]): AbstractBitVector[I] = ???
  def mul(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] = //accurate but slow/large
    val subResults = Array(constant(zero)) //start with zero; all possible results
    for(i <- v2.bits.indices) { //add bitwise mult result to possible results
      if v2.bits(i) == One then for (x <- subResults.indices) { //one: multiply v1 by (shift by index)
        subResults(x) = add(subResults(x),shiftLeft(v1,constant(num.fromInt(i)))) //add to all
      }
      else if v2.bits(i) == Bit then for (x <- subResults.indices) { //Bit: multiply v1 by 1 or 0; 0 leaves unchanged
        subResults :+ add(subResults(x),shiftLeft(v1,constant(num.fromInt(i)))) //optionally add to all
      }
    }
    joinMultipleAbstractBitVectors(subResults) //join over all option
  private inline def mulByBit(v: AbstractBitVector[I], b: AbstractBit): AbstractBitVector[I] =
    AbstractBitVector((for (x <- v.bits) yield x.and(b)).toArray)
  def div(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] = (v1.bits.last,v2.bits.last) match
    case (Bit,Bit) => //only do divisions with set signs because different techniques
      val n = v1.bits.length-1
      val v1p = v1.bits.take(n):+Zer
      val v2p = v2.bits.take(n):+Zer
      val v1n = v1.bits.take(n):+One
      val v2n = v2.bits.take(n):+One
      joinMultipleAbstractBitVectors(Seq(divWithRem(AbstractBitVector(v1p),AbstractBitVector(v2n),true)._1,
        divWithRem(AbstractBitVector(v1n),AbstractBitVector(v2n),true)._1,
        divWithRem(AbstractBitVector(v1p),AbstractBitVector(v2p),true)._1,
        divWithRem(AbstractBitVector(v1n),AbstractBitVector(v2p),true)._1))
    case (Bit,_) =>
      val n = v1.bits.length-1
      val v1p = v1.bits.take(n):+Zer
      val v1n = v1.bits.take(n):+One
      AbstractBitVectorJoin(divWithRem(AbstractBitVector(v1p),v2,true)._1,
        divWithRem(AbstractBitVector(v1n),v2,true)._1).get
    case (_,Bit) =>
      val n = v1.bits.length-1
      val v2p = v2.bits.take(n):+Zer
      val v2n = v2.bits.take(n):+Zer
      AbstractBitVectorJoin(divWithRem(v1,AbstractBitVector(v2n),true)._1,
        divWithRem(v1,AbstractBitVector(v2p),true)._1).get
    case _ => divWithRem(v1,v2,true)._1

  def max(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] =
    val n = v1.bits.length-1
    val possibleFirstDiff = v1.bits.indices.find(i => Bit.fromBoolean(v1.bits(n-i).equ(v2.bits(n-i))) != One) //first (possible) difference between abstractions
    if possibleFirstDiff.isEmpty then v1 //same elements
    else
      val firstDiff = possibleFirstDiff.get
      if v1.bits(n-firstDiff) == v2.bits(n-firstDiff) && v1.bits(n-firstDiff) == Bit then AbstractBitVectorJoin(v1,v2).get //two unknown bits can go in any direction
      else if v1.bits(n-firstDiff) == One && v2.bits(n-firstDiff) == Zer then if firstDiff == 0 then v2 else v1 //clear, decide if sign bit or not
      else if v1.bits(n-firstDiff) == Zer && v2.bits(n-firstDiff) == One then if firstDiff == 0 then v1 else v2 //clear, decide if sing bit or not
      else
        val rest = max(AbstractBitVector(v1.bits.take(n-firstDiff)++Array.fill(firstDiff+1)(Zer)),
          AbstractBitVector(v2.bits.take(n-firstDiff)++Array.fill(firstDiff+1)(Zer))).bits.take(n-firstDiff) //compare everything after firstDiff, take relevant bits
        if v1.bits(n-firstDiff) == One || v2.bits(n-firstDiff) == Zer then //difference is One,Bit or Bit,Zer => either larger one or max of rest, join
          if firstDiff == 0 then AbstractBitVectorJoin(v2,AbstractBitVector(rest++v2.bits.takeRight(1))).get
          else AbstractBitVectorJoin(v1,AbstractBitVector(rest++v1.bits.takeRight(firstDiff+1))).get //decide if sign bit or not
        else if firstDiff == 0 then AbstractBitVectorJoin(v1,AbstractBitVector(rest++v1.bits.takeRight(1))).get
        else AbstractBitVectorJoin(v2,AbstractBitVector(rest++v2.bits.takeRight(firstDiff+1))).get //decide if sign bit or not
  def min(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] = //could just take 2s complement for both and compute max, but unsure how accurate complement is4
    val n = v1.bits.length-1
    val possibleFirstDiff = v1.bits.indices.find(i => Bit.fromBoolean(v1.bits(n-i).equ(v2.bits(n-i))) != One) //first (possible) difference between abstractions
    if possibleFirstDiff.isEmpty then v1 //same elements
    else //pretty much equivalent to max, but always chose the opposite element to select/join/return
      val firstDiff = possibleFirstDiff.get
      if v1.bits(n-firstDiff) == v2.bits(n-firstDiff) && v1.bits(n-firstDiff) == Bit then AbstractBitVectorJoin(v1,v2).get //two unknown bits can go in any direction
      else if v1.bits(n-firstDiff) == One && v2.bits(n-firstDiff) == Zer then if firstDiff == 0 then v1 else v2 //clear, decide if sign bit or not
      else if v1.bits(n-firstDiff) == Zer && v2.bits(n-firstDiff) == One then if firstDiff == 0 then v2 else v1 //clear, decide if sing bit or not
      else
        val rest = min(AbstractBitVector(v1.bits.take(n-firstDiff)++Array.fill(firstDiff+1)(Zer)),
          AbstractBitVector(v2.bits.take(n-firstDiff)++Array.fill(firstDiff+1)(Zer))).bits.take(n-firstDiff) //compare everything after firstDiff, take relevant bits
        if v1.bits(n-firstDiff) == One || v2.bits(n-firstDiff) == Zer then //difference is One,Bit or Bit,Zer => either smaller one or min of rest, join
          if firstDiff == 0 then AbstractBitVectorJoin(v1,AbstractBitVector(rest++v1.bits.takeRight(1))).get
          else AbstractBitVectorJoin(v2,AbstractBitVector(rest++v2.bits.takeRight(firstDiff+1))).get //decide if sign bit or not
        else if firstDiff == 0 then AbstractBitVectorJoin(v2,AbstractBitVector(rest++v2.bits.takeRight(1))).get
        else AbstractBitVectorJoin(v1,AbstractBitVector(rest++v1.bits.takeRight(firstDiff+1))).get //decide if sign bit or not
  def absolute(v: AbstractBitVector[I]): AbstractBitVector[I] =
    if v.bits.last == Zer then v //positives get returned
    else if v.bits.last == One then complement(v) //negatives
    else
      val n = v.bits.length-1
      AbstractBitVectorJoin(AbstractBitVector(v.bits.take(n):+Zer),complement(AbstractBitVector(v.bits.take(n):+One))).get //unsure: join both possibilities

  //modulo and remainder same except for sign importance
  def modulo(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] = //if negative remainder, make pos mod
    val rem = remainder(v1,v2)
    if rem.bits.last == Zer then rem
      else if rem.bits.last == One then add(rem,v2)
    else
      val withoutSign = rem.bits.take(rem.bits.length-1)
      AbstractBitVectorJoin(AbstractBitVector(withoutSign:+Zer),add(AbstractBitVector(withoutSign:+One),v2)).get
  def remainder(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] =
    val divRem = (v1.bits.last,v2.bits.last) match
      case (Bit, Bit) => //only do divisions with set signs because different techniques
        val n = v1.bits.length - 1
        val v1p = v1.bits.take(n) :+ Zer
        val v2p = v2.bits.take(n) :+ Zer
        val v1n = v1.bits.take(n) :+ One
        val v2n = v2.bits.take(n) :+ One
        (joinMultipleAbstractBitVectors(Seq(divWithRem(AbstractBitVector(v1p), AbstractBitVector(v2n), true)._1,
          divWithRem(AbstractBitVector(v1n), AbstractBitVector(v2n), true)._1,
          divWithRem(AbstractBitVector(v1p), AbstractBitVector(v2p), true)._1,
          divWithRem(AbstractBitVector(v1n), AbstractBitVector(v2p), true)._1)),
        joinMultipleAbstractBitVectors(Seq(divWithRem(AbstractBitVector(v1p), AbstractBitVector(v2n), true)._2,
          divWithRem(AbstractBitVector(v1n), AbstractBitVector(v2n), true)._2,
          divWithRem(AbstractBitVector(v1p), AbstractBitVector(v2p), true)._2,
          divWithRem(AbstractBitVector(v1n), AbstractBitVector(v2p), true)._2)))
      case (Bit, _) =>
        val n = v1.bits.length - 1
        val v1p = v1.bits.take(n) :+ Zer
        val v1n = v1.bits.take(n) :+ One
        (AbstractBitVectorJoin(divWithRem(AbstractBitVector(v1p), v2, true)._1,
          divWithRem(AbstractBitVector(v1n), v2, true)._1).get,
        AbstractBitVectorJoin(divWithRem(AbstractBitVector(v1p), v2, true)._2,
          divWithRem(AbstractBitVector(v1n), v2, true)._2).get)
      case (_, Bit) =>
        val n = v1.bits.length - 1
        val v2p = v2.bits.take(n) :+ Zer
        val v2n = v2.bits.take(n) :+ Zer
        (AbstractBitVectorJoin(divWithRem(v1, AbstractBitVector(v2n), true)._1,
          divWithRem(v1, AbstractBitVector(v2p), true)._1).get,
        AbstractBitVectorJoin(divWithRem(v1, AbstractBitVector(v2n), true)._2,
          divWithRem(v1, AbstractBitVector(v2p), true)._2).get)
      case _ => divWithRem(v1, v2, true)
    val rem1 = divRem._2
    val mult = mul(divRem._1, v2) //trim down so mult <= v1
    val rem2 = sub(v1, mult)
    val rem = meet(rem1, rem2)
    rem.getOrElse(throw new IllegalStateException( s"unable to compute meet between $rem1 % $rem2"))

  def gcd(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] = {
    if v1 == constant(zero) then v2
    if v2 == constant(zero) then v1
    val n = v1.bits.length-1
    var gcdOptions = ArrayBuffer[AbstractBitVector[I]]() //all possible gcd
    var u = absolute(v1) //work only with absolute values
    var v = absolute(v2)
    val k = min(countTrailingZeros(u),countTrailingZeros(v)) //shared trailingZeros
    while(u != constant(zero) && v != constant(zero)) {
      if u.containsNum(zero) then
        gcdOptions.append(shiftLeft(v,k)) //v could be rest of gcd
        u = trimTrailingZeros(u) //trim off trailingzeros (not all) to get odd number
      else
        val s1 = countTrailingZeros(u)
        u = changeBitValue(shiftRight(u,s1),0,One) //shift off trailingZeros and make odd
      if v.containsNum(zero) then
        gcdOptions.append(shiftLeft(u,k)) //u could be rest of bcd
        v = trimTrailingZeros(v) //trim off trailingzeros to get odd number
      else
        val s2 = countTrailingZeros(v)
        v = changeBitValue(shiftRight(v,s2),0,One) //shift off trailingZeros and make odd
      //left with: two odd numbers
      val smaller = min(u,v)
      val larger = max(u,v)
      u = smaller //smaller of the original numbers
      v = shiftRight(trim(changeBitValue(sub(larger,smaller),n,Zer),larger,true),constant(one)) //difference between numbers, trimmed down, even, so halve
    }
    //u,v can't (shouldn't) both be zero, so if one is assume the other isn't
    if v == constant(zero) then //v zero: trim u down
      if u.containsNum(zero) then
        u = trimTrailingZeros(u) //trim off trailingzeros to get odd number
      else
        val s2 = countTrailingZeros(u)
        u = changeBitValue(shiftRight(u,s2),0,One)
      gcdOptions.append(shiftLeft(u,k))
    else //u zero: trim v down
      if v.containsNum(zero) then
        v = trimTrailingZeros(v) //trim off trailingzeros to get odd number
      else
        val s1 = countTrailingZeros(v)
        v = changeBitValue(shiftRight(v,s1),0,One)
      gcdOptions.append(shiftLeft(v,k))
    joinMultipleAbstractBitVectors(gcdOptions)
  }
  private inline def trimTrailingZeros(v: AbstractBitVector[I]): AbstractBitVector[I] = //shifts away trailing zeros for nums containing zero, ignoring zero
    val positions = v.bits.indices.filter(i => v.bits(i) == Bit) //shift until possible 1
    val options = for (s <- positions) yield (changeBitValue(shiftRight(v,constant(num.fromInt(s))),0,One)) //shift and change last bit to one
    if (options.size == 0) v
    else joinMultipleAbstractBitVectors(options) //join yields odd numbers that are not zero

  def bitAnd(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] =
    val newBits = for(i <- v1.indices) yield v1.bits(i).and(v2.bits(i))
    AbstractBitVector(newBits)
  def bitOr(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] =
    val newBits = for(i <- v1.indices) yield v1.bits(i).or(v2.bits(i))
    AbstractBitVector(newBits)
  def bitXor(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] =
    val newBits = for(i <- v1.indices) yield v1.bits(i).xor(v2.bits(i))
    AbstractBitVector(newBits)

  private inline def shiftLeftWithFill(v: AbstractBitVector[I], shift: AbstractBitVector[I], fill: AbstractBit): AbstractBitVector[I] =
    val shift2 = shift.bits.take(if v.bits.length == 32 then 5 else 6) //copy behaviour form ConcreteIntOps, int or long
    val poss = AbstractBitVectorToList(AbstractBitVector(shift2)).map(x => x.toInt)  //possible shifts
    val shifted = for(i <- v.bits.indices) yield //for all indices
      joinMultipleAbstractBits(for(x <- poss) yield if i>=x then v.bits(i-x) else fill) //join possible shifted bits
    AbstractBitVector(shifted.toArray)
  def shiftLeft(v: AbstractBitVector[I], shift: AbstractBitVector[I]): AbstractBitVector[I] = shiftLeftWithFill(v,shift,Zer)
  def shiftRight(v: AbstractBitVector[I], shift: AbstractBitVector[I]): AbstractBitVector[I] =
    val shift2 = shift.bits.take(if v.bits.length == 32 then 5 else 6)
    val poss = AbstractBitVectorToList(AbstractBitVector(shift2)).map(x => x.toInt)
    val sign = v.bits.last //signed shift uses sign bit for filling
    val shifted = for(i <- v.bits.indices) yield
      joinMultipleAbstractBits(for(x <- poss) yield if i+x < v.bits.length then v.bits(i+x) else sign)
    AbstractBitVector(shifted.toArray)
  def rotateLeft(v: AbstractBitVector[I], shift: AbstractBitVector[I]): AbstractBitVector[I] =
    val shift2 = shift.bits.take(if v.bits.length == 32 then 5 else 6)
    val poss = AbstractBitVectorToList(AbstractBitVector(shift2)).map(x => x.toInt)
    val shifted = for(i <- v.bits.indices) yield
      joinMultipleAbstractBits(for(x <- poss) yield v.bits((i+v.bits.length-x)%v.bits.length))
    AbstractBitVector(shifted.toArray)
  def rotateRight(v: AbstractBitVector[I], shift: AbstractBitVector[I]): AbstractBitVector[I] =
    val shift2 = shift.bits.take(if v.bits.length == 32 then 5 else 6)
    val poss = AbstractBitVectorToList(AbstractBitVector(shift2)).map(x => x.toInt)
    val shifted = for(i <- v.bits.indices) yield
      joinMultipleAbstractBits(for(x <- poss) yield v.bits((i+x)%v.bits.length))
    AbstractBitVector(shifted.toArray)

  def countLeadingZeros(v: AbstractBitVector[I]): AbstractBitVector[I] = countTrailingZeros(AbstractBitVector(v.bits.reverse))
  def countTrailingZeros(v: AbstractBitVector[I]): AbstractBitVector[I] =
    val firstOne = v.bits.indexOf(One) //first index in array => last One in bitvector
    if firstOne == 0 then constant(zero)
    else
      val noOnes = if firstOne > 0 then v.bits.take(firstOne):+Bit else v.bits:+Bit //only the relevant ones, one last bit for case all zeros
      val poss = noOnes.zipWithIndex.filter(x => x._1 == Bit).map(x => constant(num.fromInt(x._2)))
      joinMultipleAbstractBitVectors(poss)
  def nonzeroBitCount(v: AbstractBitVector[I]): AbstractBitVector[I] =
    val ones = v.bits.count(x => x==One)
    val tops = v.bits.count(x => x==Bit)
    val poss = (ones to ones+tops).map(x => constant(num.fromInt(x)))
    joinMultipleAbstractBitVectors(poss)

  def invertBits(v: AbstractBitVector[I]): AbstractBitVector[I] = AbstractBitVector(v.bits.map(x => x.not))

  def divUnsigned(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] = divWithRem(v1,v2)._1
  private inline def divWithRem(v1: AbstractBitVector[I], v2: AbstractBitVector[I], signed: Boolean = false): (AbstractBitVector[I],AbstractBitVector[I]) =
    if v2 == constant(zero) then f.fail(IntegerDivisionByZero, msg=s"$v1 / $v2")
    else if v2.containsNum(zero) then
      val bitindices = v2.bits.indices.filter(i => v2.bits(i) == Bit)
      val options = for (j <- bitindices) yield (changeBitValue(v2,j,One))
      val results = for (x <- options) yield (divWithRemWithoutZero(v1,x,signed))
      val result = (joinMultipleAbstractBitVectors(for (x <- results) yield (x._1)),joinMultipleAbstractBitVectors(for (x <- results) yield (x._2)))
      (joinWithFailure(result._1)(f.fail(IntegerDivisionByZero, msg=s"$v1 / $v2")),joinWithFailure(result._2)(f.fail(IntegerDivisionByZero,msg=s"$v1 / $v2")))
    else divWithRemWithoutZero(v1,v2,signed)

  private inline def divWithRemWithoutZero(v1: AbstractBitVector[I], v2: AbstractBitVector[I], signed: Boolean = false): (AbstractBitVector[I],AbstractBitVector[I]) =
    /*
    * ALU: (positive/unsigned)
    * divBy
    * rem | divThis
    * => shift rem | divBy left, fill divBy with 0 if new rem < divBy, 1 if >= divBy, t else
    * if rem >= divBy: rem -= divBy
    * repeat bits.length times
    * trim rem to be smaller than divBy if necessary
    * return (current divThis, current rem)
    *
    * signed: remainder same sign as v1, dividend pos if v1, v2 same sign, neq else
    * rem starts as all 0 or all 1 depending on sign of v1 (remainder same sign as v1)
    * shift left, add or subtract rem by v2 to get closer to zero but not change sign
    * without change, fill with v1.sign xor v2.sign (toward sign of dividend); with change respective opposite
    * if change unsure, trim rem to maintain right sign
    * if dividend result is negative (if signs of v1,v2 don't match) add one at the end
    *
    * */
    var rem = if signed && v1.bits.last == One then constant(-one) else constant(zero) //when signed, starting dividend and rem same sign; else 0
    var dividend = v1 //have to copy to shift stuff
    for (i <- v1.bits) {
      rem = shiftLeftWithFill(rem,constant(one),dividend.bits.last) //shift leftmost bit of divThis into rem
      if signed then
        val modified = if v2.bits.last == v1.bits.last then sub(rem,v2) else add(rem,v2) //edge towards zero
        val changes = if rem == constant(zero) then Zer else Bit.fromBoolean(v1.bits.last.equ(modified.bits.last)) //overshot?
        if changes == One then rem = modified //same sign: do op
        else if changes == Bit then
          rem = AbstractBitVectorJoin(rem,modified).get// join and assure sign
          if v1.bits.last == v2.bits.last then rem = trim(rem,v2,signed) //trim for same signs; differing signs left out for now
        dividend = shiftLeftWithFill(dividend,constant(one),(v1.bits.last.xor(v2.bits.last).xor(changes))) //shift dividend, fill depending on prior computation
      else
        val changes = Bit.fromBoolean(au.ltUnsigned(rem,v2))
        if changes == Zer then
          rem = sub(rem,v2)
        else if changes == Bit then
          rem = AbstractBitVectorJoin(rem,sub(rem,v2)).get// join and assure sign
          if v1.bits.last == v2.bits.last then rem = trim(rem,v2,signed) //trim for same signs; differing signs left out for now
        dividend = shiftLeftWithFill(dividend,constant(one),changes.not)
    }
    if signed && v1.bits.last == One then //negative remainder
      val option = Bit.fromBoolean(ae.equ(constant(zero),(if v2.bits.last == Zer then add (v2,rem) else sub(v2,rem)))) // rem needs to be trimmed to zero?
      if option == Zer then // no trim: add one to dividend if neg
        dividend = if v2.bits.last == Zer then add(dividend,constant(one)) else dividend
      else if option == One then // trim: trim rem to zero (add and sub one from div, cancels out)
        dividend = if v2.bits.last == Zer then dividend else add(dividend,constant(one))
        rem = constant(zero)
      else // unknown trim: join both possibilities
        dividend = AbstractBitVectorJoin(dividend, add(dividend, constant(one))).get
        rem = AbstractBitVectorJoin(rem,constant(zero)).get
    else if signed && v2.bits.last == One then //pos remainder but neg dividend
      dividend = add(dividend,constant(one))
    (dividend, rem)

  private inline def trim(v1: AbstractBitVector[I],v2: AbstractBitVector[I],signed: Boolean): AbstractBitVector[I] = //return part of v1 that is <=/>= v2, if impossible return v1; when signed expect same sign
    var go = true //check if we're still looking
    val n = v1.bits.length-1
    val res = if !signed || v2.bits.last == Zer then for (i <- v1.bits.indices) yield { //trimming down
      if !go then v1.bits(n-i) //not looking: just return value in v1
      else if v1.bits(n-i) == v2.bits(n-i) && v1.bits(n-i) == Bit then //v1 <= v2 can be reached, stop
        go = false
        val test = v1.bits.take(n-i)++Array.fill(i+1)(Zer)
        val against = v2.bits.take(n-i)++Array.fill(i+1)(Zer)
        if Bit.fromBoolean(ao.lt(AbstractBitVector(test),AbstractBitVector(against))) == Zer then Zer else Bit
      else if v1.bits(n-i) == v2.bits(n-i) then v1.bits(n-i) //same static: return static
      else if v1.bits(n-i) != Bit then v1.bits(n-i) // v1 is static: return static
      else if v2.bits(n-i) == One then //(Bit,One) stays unchanged; could improve: check if HAS to be Zer instead
        go = false
        val test = v1.bits.take(n-i)++Array.fill(i+1)(Zer)
        val against = v2.bits.take(n-i)++Array.fill(i+1)(Zer)
        if Bit.fromBoolean(ao.lt(AbstractBitVector(test),AbstractBitVector(against))) == Zer then Zer else Bit
      else Zer //first occurence where v1>v2 possible and before v1<v2 not possible, make sure it can't be, keep going
    }
    else for (i <- v1.bits.indices) yield { //trimming up for signed
      if !go then v1.bits(n-i)
      else if v1.bits(n-i) == v2.bits(n-i) && v1.bits(n-i) == Bit then
        go = false
        val test = v1.bits.take(n - i) ++ Array.fill(i + 1)(Zer)
        val against = v2.bits.take(n-i)++Array.fill(i+1)(Zer)
        if Bit.fromBoolean(ao.gt(AbstractBitVector(test), AbstractBitVector(against))) == Zer then One else Bit
      else if v1.bits(n-i) == v2.bits(n-i) then v1.bits(n-i)
      else if v1.bits(n-i) != Bit then v1.bits(n-i)
      else if v2.bits(n-i) == Zer then
        go = false
        val test = v1.bits.take(n - i) ++ Array.fill(i + 1)(Zer)
        val against = v2.bits.take(n-i)++Array.fill(i+1)(Zer)
        if Bit.fromBoolean(ao.gt(AbstractBitVector(test), AbstractBitVector(against))) == Zer then One else Bit
      else One
    }
    AbstractBitVector(res.toArray.reverse)
  def remainderUnsigned(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] =
    if v2.isConstant && v2.containsNum(zero) then f.fail(IntegerDivisionByZero, msg=s"$v1 % $v2")
    else
      val div = divWithRem(v1,v2)
      val rem1 = div._2
      val mult = mul(div._1,v2) //trim down so mult <= v1
      val rem2 = sub(v1,mult)
      val rem = meet(rem1,rem2)
      val res = if rem.isDefined then rem.get else f.fail(IntegerDivisionByZero, msg=s"unable to compute $v1 % $v2")
      if v2.containsNum(zero) then joinWithFailure(res)(f.fail(IntegerDivisionByZero,msg=s"$v1 % $v2")) else res
  def shiftRightUnsigned(v: AbstractBitVector[I], shift: AbstractBitVector[I]): AbstractBitVector[I] =
    val shift2 = shift.bits.take(if v.bits.length == 32 then 5 else 6)
    val poss = AbstractBitVectorToList(AbstractBitVector(shift2)).map(x => x.toInt)
    val shifted = for(i <- v.bits.indices) yield
      joinMultipleAbstractBits(for(x <- poss) yield if i+x < v.bits.length then v.bits(i+x) else Zer) //filling with Zer instead of sign bit
    AbstractBitVector(shifted.toArray)

  //optional defs, tested
  def getSign(v: AbstractBitVector[I]): AbstractBit = v.bits.last
  def copySign(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): AbstractBitVector[I] =
    val sign1 = getSign(v1) //get signs
    val sign2 = getSign(v2)
    if sign1 == Bit || sign2 == Bit then AbstractBitVectorJoin(v1,complement(v1)).get //if any unclear then anything possible
    else if sign1 == sign2  then v1 //same sure sign: return unchanged
    else complement(v1) //different sure sign: transfer sign by constructing complement

//untested
//top values for int and long
given TopAbstractBitVectorInt: Top[AbstractBitVector[Int]] with
  def top: AbstractBitVector[Int] = AbstractBitVector(Array.fill(32)(Zer))
given TopAbstractBitVectorLong: Top[AbstractBitVector[Long]] with
  def top: AbstractBitVector[Long] = AbstractBitVector(Array.fill(64)(Zer))

//untested
//make number into abstract rep
given AbstractBitVectorAbstractly[I](using Numeric[I], Ordering[I]): Abstractly[I,AbstractBitVector[I]] with
  override def apply(i: I): AbstractBitVector[I] = AbstractBitVector.constant(i)

//tested
//check subtyping between abstract reps
given AbstractBitVectorOrdering[I](using Numeric[I], Ordering[I]): PartialOrder[AbstractBitVector[I]] with
  import AbstractBitVector.*
  override def lteq(x: AbstractBitVector[I], y: AbstractBitVector[I]): Boolean =
    x.bits.indices.forall{i => x.bits(i).subtypeOf(y.bits(i))}

//tested
//joins two abstract reps and return MaybeChanged of them
given AbstractBitVectorJoin[I](using Numeric[I], Ordering[I]): Join[AbstractBitVector[I]] with
  import AbstractBitVector.*
  override def apply(v1: AbstractBitVector[I], v2: AbstractBitVector[I]): MaybeChanged[AbstractBitVector[I]] =
    val joined = for(i <- v1.bits.indices) yield v1.bits(i).joinWith(v2.bits(i)).get
    MaybeChanged(AbstractBitVector(joined.toArray),v1)

//tested
given AbstractBitVectorOrderingOps[I]
  (using uOrd: AbstractBitVectorUnsignedOrderingOps[I], num: Numeric[I], ord: Ordering[I]):
  OrderingOps[AbstractBitVector[I], Topped[Boolean]] with
  def lt(iv1: AbstractBitVector[I], iv2: AbstractBitVector[I]): Topped[Boolean] = (iv1.bits(iv1.bits.length-1),iv2.bits(iv2.bits.length-1)) match
    case (Zer,One) => Topped.Actual(false) //nonneg > nonpos
    case (One,Zer) => if iv1.bits.contains(One) || iv2.bits.contains(One) then Topped.Actual(true) else Topped.Top //npos <= nnep, check for ß
    case (Bit,_) | (_,Bit) => Topped.Top //can be any of the above cases
    case _ => uOrd.ltUnsigned(iv1,iv2)
  def le(iv1: AbstractBitVector[I], iv2: AbstractBitVector[I]): Topped[Boolean] = (iv1.bits(iv1.bits.length-1),iv2.bits(iv2.bits.length-1)) match
    case (Zer,One) => if iv1.bits.contains(One) || iv2.bits.contains(One) then Topped.Actual(false) else Topped.Top //nneg >= npos, check for 0
    case (One,Zer) => Topped.Actual(true) // npos <= nneg
    case (Bit,_) | (_,Bit) => Topped.Top //can be any of the above cases
    case _ => uOrd.leUnsigned(iv1,iv2)

//tested
/*
* surely first < second: one bit surely <, before that (higher order) is guaranteed <=
* surely first >= second: all bits >=; one bit surely >, before that (higher order) is guaranteed >=
*
* surely first <= second: all bits <=; one bit surely <, before that (higher order) is guaranteed <=
* surely first > second: one bit surely <, before that (higher order) is guaranteed >=
*
* idea: find first candidate and first sure occurence of >/< : shows where guaranteed >/< is and guaranteed <=/>= ends
*/
given AbstractBitVectorUnsignedOrderingOps[I](using Numeric[I], Ordering[I]): UnsignedOrderingOps[AbstractBitVector[I], Topped[Boolean]] with
  def ltUnsigned(iv1: AbstractBitVector[I], iv2: AbstractBitVector[I]): Topped[Boolean] =
    val n = iv1.bits.length-1 //last index so we can start with highest order bit
    val firstSureGt = iv1.bits.indices.find(i => iv1.bits(n-i) == One && iv2.bits(n-i) == Zer)
    val firstPossGt = iv1.bits.indices.find(i => iv1.bits(n-i) != Zer && iv2.bits(n-i) != One)
    val firstSureLt = iv1.bits.indices.find(i => iv1.bits(n-i) == Zer && iv2.bits(n-i) == One)
    val firstPossLt = iv1.bits.indices.find(i => iv1.bits(n-i) != One && iv2.bits(n-i) != Zer)
    if firstPossLt.isEmpty then Topped.Actual(false)  //all gteq: can't be lt
    else if firstPossGt.isEmpty && firstSureLt.isDefined then Topped.Actual(true) // all lteq and at least one lt: must be lt
    else if firstSureGt.isDefined && firstSureGt.get < firstPossLt.get then Topped.Actual(false) //one gt with everything before gteq: is gt
    else if firstSureLt.isDefined && firstSureLt.get < firstPossGt.get then Topped.Actual(true) //one lt with everything before lteq: is lt
    else Topped.Top //rest: unsure
  def leUnsigned(iv1: AbstractBitVector[I], iv2: AbstractBitVector[I]): Topped[Boolean] =
    val n = iv1.bits.length-1
    val firstSureGt = iv1.bits.indices.find(i => iv1.bits(n-i) == One && iv2.bits(n-i) == Zer)
    val firstPossGt = iv1.bits.indices.find(i => iv1.bits(n-i) != Zer && iv2.bits(n-i) != One)
    val firstSureLt = iv1.bits.indices.find(i => iv1.bits(n-i) == Zer && iv2.bits(n-i) == One)
    val firstPossLt = iv1.bits.indices.find(i => iv1.bits(n-i) != One && iv2.bits(n-i) != Zer)
    if firstPossLt.isEmpty && firstSureGt.isDefined then Topped.Actual(false) //all gteq and at least one gt: can't be lteq
    else if firstPossGt.isEmpty then Topped.Actual(true) //all lteq: must be lteq
    else if firstSureGt.isDefined && firstSureGt.get < firstPossLt.get then Topped.Actual(false) //one gt with everything before gteq: is gt
    else if firstSureLt.isDefined && firstSureLt.get < firstPossGt.get then Topped.Actual(true) //one lt with everrything before lteq: is lt
    else Topped.Top //rest: unsure

//tested
given AbstractBitVectorEqOps[I](using Numeric[I], Ordering[I]): EqOps[AbstractBitVector[I], Topped[Boolean]] with
  override def equ(iv1: AbstractBitVector[I], iv2: AbstractBitVector[I]): Topped[Boolean] =
    if !iv1.bits.contains(Bit) then
      if iv1.bits.indices.forall{i => iv1.bits(i) == iv2.bits(i)} then Topped.Actual(true) else Topped.Actual(false)
    else if iv1.bits.indices.forall{i => Bit.fromBoolean(iv1.bits(i).equ(iv2.bits(i))) != Zer} then
      Topped.Top
    else Topped.Actual(false)
  override def neq(iv1: AbstractBitVector[I], iv2: AbstractBitVector[I]): Topped[Boolean] =
    if !iv1.bits.contains(Bit) then
      if iv1.bits.indices.forall{i => iv1.bits(i) == iv2.bits(i)} then Topped.Actual(false) else Topped.Actual(true)
    else if iv1.bits.indices.forall{i => Bit.fromBoolean(iv1.bits(i).equ(iv2.bits(i))) != Zer} then
      Topped.Top
    else Topped.Actual(true)

/* TODO MASTERLIST
*
* TODO: implement hashcode for abstract bit vector
* TODO: replace bits.indices and toArray with only indices and implement them
*
* todo review:
* gcd accuracy improvement
* trim when v1 and v2 differing signs (optional!, accuracy improvement)
*
* todo testing:
* bit ops                   x
*
* multipleJoin              x
* countOfNums               x
* abstractBitVectorToList   x
* add                       x
* sub                       x
* mul                       x
* max                       x
* min                       x
* div                       x
* divUnsigned               x
* remainder                 x
* remainderUnsigned         x
* modulo                    x
* gcd                       x
* absolute                  x
* bitAnd                    x
* bitOr                     x
* bitXor                    x
* shiftLeft                 x
* shiftRight                x
* rotateLeft                x
* rotateRight               x
* countLeadingZeros         x
* countTrailingZeros        x
* nonzeroBitCount           x
* invertBits                x
* getSign                   x
* copySign                  x
*
* orderingOps               x
* unsignedOrderingOps       x
* equalOps                  x
*
* (x means passed, o means failed)
*
*
* Testing: Soundness bzgl ConcreteIntOps
*
*/