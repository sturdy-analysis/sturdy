package sturdy.values.strings

import sturdy.values.Topped
import sturdy.values.integer.{IntSign, NumericInterval}


object CharacterInclusionUtil {
  def toppedCharSetIsEmpty(toppedCharSet: Topped[Set[Char]]): Boolean ={
    if (toppedCharSet.isActual){
      toppedCharSet.get.isEmpty
    }
    false
  }

  def toppedCharSetUnion(toppedCharSet1: Topped[Set[Char]], toppedCharSet2: Topped[Set[Char]]): Topped[Set[Char]]={
    (toppedCharSet1, toppedCharSet2) match
      case (Topped.Top, _) | (_, Topped.Top) => Topped.Top
      case (Topped.Actual(s1), Topped.Actual(s2)) => Topped.Actual(s1.union(s2))
  }

  def toppedCharSetUnion(toppedCharSet: Topped[Set[Char]], charSet: Set[Char]): Topped[Set[Char]]={
    toppedCharSet match
      case Topped.Top => Topped.Top
      case Topped.Actual(s1) => Topped.Actual(charSet.union(s1))

  }

  def toppedCharSetUnion(charSet: Set[Char], toppedCharSet: Topped[Set[Char]]): Topped[Set[Char]]={
    toppedCharSet match
      case Topped.Top => Topped.Top
      case Topped.Actual(s1) => Topped.Actual(charSet.union(s1))
  }

  def toppedCharSetDifference(charSet: Set[Char], toppedCharSet: Topped[Set[Char]]): Set[Char]={
    toppedCharSet match
      case Topped.Top => Set[Char]()
      case Topped.Actual(s1) => (charSet -- s1)
  }

  def toppedCharSetSubsetOf(charSet: Set[Char], toppedCharSet: Topped[Set[Char]]): Boolean={
    toppedCharSet match
      case Topped.Top => true
      case Topped.Actual(s) => charSet.subsetOf(s)
  }

  def toppedCharSetConatins(toppedCharSet: Topped[Set[Char]], char: Char): Boolean={
    toppedCharSet match
      case Topped.Top => true
      case Topped.Actual(s) => s.contains(char)
  }

  def isIntervalZero(interval: NumericInterval[Int]): Boolean = {
    interval match
      case NumericInterval.Bounded(low, high) => low == 0 && high == 0
      case NumericInterval.Top() => false
  }

  def isIntervalOne(interval: NumericInterval[Int]): Boolean = {
    interval match
      case NumericInterval.Bounded(low, high) => low == 1 && high == 1
      case NumericInterval.Top() => false
  }

  def isIntervalZeroToOne(interval: NumericInterval[Int]): Boolean = {
    interval match
      case NumericInterval.Bounded(low, high) => low == 0 && high == 1
      case NumericInterval.Top() => false
  }

  def isIntervalPositive(interval: NumericInterval[Int]): Boolean = {
    interval match
      case NumericInterval.Bounded(low, high) => low > 0 && high > 0
      case NumericInterval.Top() => false
  }
  def isIntervalZeroOrPositive(interval: NumericInterval[Int]): Boolean = {
    interval match
      case NumericInterval.Bounded(low, high) => low >= 0 && high >= 0
      case NumericInterval.Top() => false
  }

  def isIntervalNegative(interval: NumericInterval[Int]): Boolean = {
    interval match
      case NumericInterval.Bounded(low, high) => low < 0 && high < 0
      case NumericInterval.Top() => false
  }

  def isIntervalNegativeOrZero(interval: NumericInterval[Int]): Boolean = {
    interval match
      case NumericInterval.Bounded(low, high) => low <= 0 && high <= 0
      case NumericInterval.Top() => false
  }

  def isIntervalGreater(x: NumericInterval[Int], y: NumericInterval[Int]): Boolean = {(x, y) match
      case (NumericInterval.Top(), _) => false
      case (NumericInterval.Bounded(l1, h1), NumericInterval.Bounded(l2, h2)) =>
        l1 > l2 && h1 > h2
  }
  def intervalAsSign(x: NumericInterval[Int]): IntSign ={
    if (isIntervalPositive(x)){
      return IntSign.Pos
    }
    if (isIntervalZeroOrPositive(x)){
      return IntSign.ZeroOrPos
    }
    if (isIntervalNegative(x)){
      return IntSign.Neg
    }
    if (isIntervalNegativeOrZero(x)){
      return IntSign.NegOrZero
    }
    IntSign.TopSign
  }

  def signAsInterval(sign: IntSign): NumericInterval[Int] ={
    sign match
      case IntSign.TopSign => NumericInterval.Top()
      case IntSign.Pos => NumericInterval.Bounded(1, Int.MaxValue)
      case IntSign.ZeroOrPos => NumericInterval.Bounded(0, Int.MaxValue)
      case IntSign.Neg => NumericInterval.Bounded(Int.MinValue, -1)
      case IntSign.NegOrZero => NumericInterval.Bounded(Int.MinValue, 0)
      case IntSign.Zero => NumericInterval.Bounded(0,0)
  }
}

def signValue(i: Int): IntSign = {
  if i < 0 then IntSign.Neg
  else if i > 0 then IntSign.Pos
  else IntSign.Zero
}