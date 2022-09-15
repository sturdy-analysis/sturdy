package sturdy.values.strings

import sturdy.values.Topped

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
}
