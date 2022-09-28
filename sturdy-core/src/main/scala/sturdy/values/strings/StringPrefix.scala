package sturdy.values.strings

import org.eclipse.collections.impl.block.procedure.FastListSelectProcedure
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.booleans.ToppedBooleanOps
import sturdy.values.integer.IntSign.*
import sturdy.values.integer.NumericInterval.*
import sturdy.values.integer.{CombineIntSign, IntSign, NumericInterval}
import sturdy.values.records.ARecord
import sturdy.values.relational.*
import sturdy.values.strings.CharacterInclusionUtil.*

import java.security.KeyStore.TrustedCertificateEntry

enum StringPrefix:
  case Prefix(s: String)

import sturdy.values.strings.StringPrefix.*

given Abstractly[String, StringPrefix] with
  override def apply(s: String): StringPrefix =
    Prefix(s)


given PartialOrder[StringPrefix] with
  override def lteq(x: StringPrefix, y: StringPrefix): Boolean = (x,y) match
    case (Prefix(s), Prefix(t)) =>
      if (t.length <= s.length){
        s.substring(0, t.length) == t
      }
      else false




given CombineStringPrefix[W <: Widening]: Combine[StringPrefix, W] with
  override def apply(v1: StringPrefix, v2: StringPrefix): MaybeChanged[StringPrefix] =
    if v1 == v2 then Unchanged(v1)
    else if PartialOrder[StringPrefix].lteq(v1, v2) then Changed(v2)
    else if PartialOrder[StringPrefix].lteq(v1, v2) then Unchanged(v1)
    else (v1, v2) match
      case (Prefix(p1), Prefix(p2)) =>
        var joinedPrefix = ""
        for((x, y) <- p1.toCharArray.zip(p2.toCharArray)){
          if (x==y){
            joinedPrefix = joinedPrefix.appended(x)
          }
          else return Changed(Prefix(joinedPrefix))

        }
        return Changed(Prefix(joinedPrefix))



abstract class PrefixStringOps[I] extends StringOps[StringPrefix, I, Topped[Boolean]]:
  def stringLit(s: String): StringPrefix = Prefix(s)

  def concat(s1: StringPrefix, s2: StringPrefix): StringPrefix = s1

  override def substring(s: StringPrefix, begin: I, end: I): StringPrefix

  override def contains(s: StringPrefix, w: StringPrefix): Topped[Boolean] = Topped.Top

  override def length(s: StringPrefix): I

  override def isEmpty(s: StringPrefix): Topped[Boolean] = s match
    case Prefix("") => Topped.Top
    case Prefix(_) => Topped.Actual(false)

  override def charAt(s: StringPrefix, i: I): StringPrefix

  override def equals(s1: StringPrefix, s2: StringPrefix): Topped[Boolean] =
    if(PartialOrder[StringPrefix].lteq(s1, s2) || PartialOrder[StringPrefix].lteq(s2, s1)) {
      Topped.Top
    }
    else Topped.Actual(false)

  override def compareTo(s1: StringPrefix, s2: StringPrefix): I

  // Bei ungültigem Index wird false zurückgegeben (auch negativ)
  override def startsWith(s: StringPrefix, prefix: StringPrefix, offset: I): Topped[Boolean]

  override def endsWith(s: StringPrefix, suffix: StringPrefix): Topped[Boolean] = Topped.Top

  override def indexOf(s: StringPrefix, word: StringPrefix, fromIndex: I): I

  override def replace(s: StringPrefix, word: StringPrefix, newWord: StringPrefix): StringPrefix = Prefix("")

  override def toLowerCase(s: StringPrefix): StringPrefix = s match
    case Prefix(p) => Prefix(p.toLowerCase())

  override def toUpperCase(s: StringPrefix): StringPrefix = s match
    case Prefix(p) => Prefix(p.toUpperCase())

  override def trim(s: StringPrefix): StringPrefix = s match
    case Prefix(p) => Prefix(p.stripPrefix(" "))



given PrefixStringOpsSign(using f: Failure, j: EffectStack): PrefixStringOps[IntSign] with

  override def substring(s: StringPrefix, begin: IntSign, end: IntSign): StringPrefix = (begin, end) match
    case (IntSign.Zero, IntSign.Zero) => Prefix("")
    case (IntSign.Neg, _) | (_, IntSign.Neg) => j.joinWithFailure(f.fail(StringNegativeIndex,
    s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
    s"substring of $s with indices $begin to $end"))

    case (IntSign.Zero, IntSign.Pos) => s match
      case Prefix("") => j.joinWithFailure(Prefix(""))(f.fail(StringIndexOutOfBounds,
        s"substring of $s with indices $begin to $end"))
      case Prefix(pre) => j.joinWithFailure(Prefix(pre.substring(0,1)))(f.fail(StringIndexOutOfBounds,
          s"substring of $s with indices $begin to $end"))

    case (IntSign.NegOrZero, IntSign.Pos) => s match
      case Prefix("") => j.joinWithFailure(Prefix(""))(j.joinWithFailure(f.fail(StringNegativeIndex,
        s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
        s"substring of $s with indices $begin to $end")))
      case Prefix(pre) => j.joinWithFailure(Prefix(pre.substring(0,1)))(j.joinWithFailure(f.fail(StringNegativeIndex,
        s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
        s"substring of $s with indices $begin to $end")))

    case (IntSign.NegOrZero, _) | (_, IntSign.NegOrZero) =>
      j.joinWithFailure(Prefix(""))(j.joinWithFailure(f.fail(StringNegativeIndex,
        s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
        s"substring of $s with indices $begin to $end")))

    case _ => j.joinWithFailure(Prefix(""))(f.fail(StringIndexOutOfBounds,
        s"substring of $s with indices $begin to $end"))


  override def length(s: StringPrefix): IntSign = s match
    case Prefix("") => IntSign.ZeroOrPos
    case _ => IntSign.Pos

  override def charAt(s: StringPrefix, i: IntSign): StringPrefix = i match
    case IntSign.Zero => s match
      case Prefix("") => j.joinWithFailure(Prefix(""))(f.fail(StringIndexOutOfBounds, s"charAt of $s with index $i"))
      case Prefix(pre) => Prefix(pre.charAt(0).toString)
    case IntSign.NegOrZero => s match
      case Prefix("") => j.joinWithFailure(Prefix(""))(j.joinWithFailure(f.fail(StringIndexOutOfBounds, s"charAt of $s with index $i"))(f.fail(StringNegativeIndex,
        s"charAt of $s with inex $i ")))
      case Prefix(pre) => j.joinWithFailure(Prefix(pre.charAt(0).toString))(f.fail(StringNegativeIndex,
        s"charAt of $s with inex $i "))
    case IntSign.Neg => f.fail(StringNegativeIndex, s"charAt of $s with index $i")
    case IntSign.Pos => j.joinWithFailure(Prefix(""))(f.fail(StringIndexOutOfBounds,
      s"charAt of $s with index $i"))
    case IntSign.TopSign => j.joinWithFailure(Prefix(""))(j.joinWithFailure(f.fail(StringNegativeIndex,
      s"charAt of $s with index $i"))(f.fail(StringIndexOutOfBounds,
      s"charAt of $s with index $i")))



  override def compareTo(s1: StringPrefix, s2: StringPrefix): IntSign = (s1, s2) match
    case (Prefix(pre1), Prefix(pre2)) =>
      if (pre1.length == pre2.length && pre1 != pre2){
        if pre1.compareTo(pre2) == 0 then IntSign.Zero
        else if pre1.compareTo(pre2) > 0 then IntSign.Pos
        else IntSign.Neg
      }
      else IntSign.TopSign


  // Bei ungültigem Index wird false zurückgegeben (auch negativ)
  override def startsWith(s: StringPrefix, prefix: StringPrefix, offset: IntSign): Topped[Boolean] = offset match
    case IntSign.Zero => (s, prefix) match
      case (Prefix(pre1), Prefix(pre2)) =>
        if (pre1.startsWith(pre2) || pre2.startsWith(pre1)){
          Topped.Top
        }
        else Topped.Actual(false)
    case IntSign.Pos | IntSign.ZeroOrPos | IntSign.NegOrZero => Topped.Top
    case IntSign.Neg => Topped.Actual(false)

  override def indexOf(s: StringPrefix, word: StringPrefix, fromIndex: IntSign): IntSign = IntSign.TopSign


given PrefixStringOpsNumericIntervall(using f: Failure, j: EffectStack): PrefixStringOps[NumericInterval[Int]] with

  override def substring(s: StringPrefix, begin: NumericInterval[Int], end: NumericInterval[Int]): StringPrefix = (begin, end) match
    case (_, NumericInterval.Top()) | (NumericInterval.Top(), _) => return PrefixStringOpsSign.substring(s, intervalAsSign(begin), intervalAsSign(end))
    case (NumericInterval.Bounded(0,0), NumericInterval.Bounded(0,0)) => Prefix("")
    case (NumericInterval.Bounded(beginLow, beginHigh), NumericInterval.Bounded(endLow, endHigh)) => s match
      case Prefix(pre) =>
        if (beginLow == beginHigh && beginLow < pre.length) {
          if(endHigh < pre.length){
            return Prefix(pre.substring(beginLow, endLow))
          }
          else return j.joinWithFailure(Prefix(pre.substring(beginLow)))(f.fail(StringIndexOutOfBounds,
            s"substring of $s with indices $begin to $end"))
        }
        if(beginLow < pre.length && beginHigh < pre.length && endLow < pre.length && endHigh < pre.length){
          return Prefix("")
        }
        else return PrefixStringOpsSign.substring(s, intervalAsSign(begin), intervalAsSign(end))


  override def length(s: StringPrefix): NumericInterval[Int] = s match
    case Prefix(pre) =>  NumericInterval[Int].tupled(pre.length, Int.MaxValue)

  override def charAt(s: StringPrefix, i: NumericInterval[Int]): StringPrefix = s match
    case Prefix(pre) => i match
      case NumericInterval.Bounded(low, high) =>
        if (low == high && pre.length > high && pre.length > 0){
          Prefix(pre.charAt(high).toString)
        }
        else PrefixStringOpsSign.charAt(s, intervalAsSign(i))
      case NumericInterval.Top() => PrefixStringOpsSign.charAt(s, IntSign.TopSign)


  override def compareTo(s1: StringPrefix, s2: StringPrefix): NumericInterval[Int] = (s1, s2) match
    case (Prefix(pre1), Prefix(pre2)) =>
      if (pre1.length == pre2.length && pre1 != pre2){
        NumericInterval.Bounded(pre1.compareTo(pre2), pre1.compareTo(pre2))
      }
      else NumericInterval.Top()

  override def startsWith(s: StringPrefix, prefix: StringPrefix, offset: NumericInterval[Int]): Topped[Boolean] = (s, prefix) match
    case (Prefix(pre1), Prefix(pre2)) => offset match
      case NumericInterval.Bounded(low, high) =>
        if (pre1.length < low && pre1.length < high){
          for (x <- low to high) {
            if (pre1.startsWith(pre2, x)){
              return Topped.Top
            }
          }
        return Topped.Actual(false)
        }
        else {
          if (isIntervalNegative(offset)) {
            return Topped.Actual(false)
          }
          else return Topped.Top
        }
      case NumericInterval.Top() => Topped.Top

  override def indexOf(s: StringPrefix, word: StringPrefix, fromIndex: NumericInterval[Int]): NumericInterval[Int] =
    NumericInterval.Bounded(-1, Int.MaxValue)







