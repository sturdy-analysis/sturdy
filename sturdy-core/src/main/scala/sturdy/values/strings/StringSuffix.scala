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

enum StringSuffix:
  case Suffix(s: String)

import sturdy.values.strings.StringSuffix.*

given Abstractly[String, StringSuffix] with
  override def apply(s: String): StringSuffix =
    Suffix(s)


given PartialOrder[StringSuffix] with
  override def lteq(x: StringSuffix, y: StringSuffix): Boolean = (x,y) match
    case (Suffix(s), Suffix(t)) =>
      if (t.length <= s.length){
        s.substring(s.length - t.length, s.length) == t
      }
      else false




given CombineStringSuffix[W <: Widening]: Combine[StringSuffix, W] with
  override def apply(v1: StringSuffix, v2: StringSuffix): MaybeChanged[StringSuffix] =
    if v1 == v2 then Unchanged(v1)
    else if PartialOrder[StringSuffix].lteq(v1, v2) then Changed(v2)
    else if PartialOrder[StringSuffix].lteq(v1, v2) then Unchanged(v1)
    else (v1, v2) match
      case (Suffix(p1), Suffix(p2)) =>
        var joinedSuffix = ""
        for((x, y) <- p1.reverse.toCharArray.zip(p2.reverse.toCharArray)){
          if (x==y){
            joinedSuffix = joinedSuffix.appended(x)
          }
          else return Changed(Suffix(joinedSuffix.reverse))

        }
        return Changed(Suffix(joinedSuffix))



abstract class SuffixStringOps[I] extends StringOps[StringSuffix, I, Topped[Boolean]]:
  def stringLit(s: String): StringSuffix = Suffix(s)

  def concat(s1: StringSuffix, s2: StringSuffix): StringSuffix = s2

  override def substring(s: StringSuffix, begin: I, end: I): StringSuffix

  override def contains(s: StringSuffix, w: StringSuffix): Topped[Boolean] = Topped.Top

  override def length(s: StringSuffix): I

  override def isEmpty(s: StringSuffix): Topped[Boolean] = s match
    case Suffix("") => Topped.Top
    case Suffix(_) => Topped.Actual(false)

  override def charAt(s: StringSuffix, i: I): StringSuffix

  override def equals(s1: StringSuffix, s2: StringSuffix): Topped[Boolean] =
    if(PartialOrder[StringSuffix].lteq(s1, s2) || PartialOrder[StringSuffix].lteq(s2, s1)) {
      Topped.Top
    }
    else Topped.Actual(false)

  override def compareTo(s1: StringSuffix, s2: StringSuffix): I

  // Bei ungültigem Index wird false zurückgegeben (auch negativ)
  override def startsWith(s: StringSuffix, Suffix: StringSuffix, offset: I): Topped[Boolean]

  override def endsWith(s: StringSuffix, suffix: StringSuffix): Topped[Boolean] =
    if PartialOrder[StringSuffix].lteq(suffix, s) then Topped.Top else Topped.Actual(false)

  override def indexOf(s: StringSuffix, word: StringSuffix, fromIndex: I): I

  override def replace(s: StringSuffix, word: StringSuffix, newWord: StringSuffix): StringSuffix = Suffix("")

  override def toLowerCase(s: StringSuffix): StringSuffix = s match
    case Suffix(p) => Suffix(p.toLowerCase())

  override def toUpperCase(s: StringSuffix): StringSuffix = s match
    case Suffix(p) => Suffix(p.toUpperCase())

  override def trim(s: StringSuffix): StringSuffix = s match
    case Suffix(p) => Suffix(p.stripSuffix(" "))



given SuffixStringOpsSign(using f: Failure, j: EffectStack): SuffixStringOps[IntSign] with

  override def substring(s: StringSuffix, begin: IntSign, end: IntSign): StringSuffix = (begin, end) match
    case (IntSign.Zero, IntSign.Zero) => Suffix("")
    case (IntSign.Neg, _) | (_, IntSign.Neg) => j.joinWithFailure(f.fail(StringNegativeIndex,
    s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
    s"substring of $s with indices $begin to $end"))

    case (IntSign.NegOrZero, _) | (_, IntSign.NegOrZero) =>
      j.joinWithFailure(Suffix(""))(j.joinWithFailure(f.fail(StringNegativeIndex,
        s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
        s"substring of $s with indices $begin to $end")))

    case _ => j.joinWithFailure(Suffix(""))(f.fail(StringIndexOutOfBounds,
        s"substring of $s with indices $begin to $end"))


  override def length(s: StringSuffix): IntSign = s match
    case Suffix("") => IntSign.ZeroOrPos
    case _ => IntSign.Pos

  override def charAt(s: StringSuffix, i: IntSign): StringSuffix = i match
    case IntSign.Zero => s match
      case Suffix("") => j.joinWithFailure(Suffix(""))(f.fail(StringIndexOutOfBounds, s"charAt of $s with index $i"))
      case Suffix(_) => Suffix("")
    case IntSign.NegOrZero => s match
      case Suffix("") => j.joinWithFailure(Suffix(""))(j.joinWithFailure(f.fail(StringIndexOutOfBounds,
        s"charAt of $s with index $i"))(f.fail(StringNegativeIndex,
        s"charAt of $s with inex $i ")))
      case Suffix(_) => j.joinWithFailure(Suffix(""))(f.fail(StringNegativeIndex,
        s"charAt of $s with inex $i "))
    case IntSign.Neg => f.fail(StringNegativeIndex, s"charAt of $s with index $i")
    case IntSign.Pos => j.joinWithFailure(Suffix(""))(f.fail(StringIndexOutOfBounds,
      s"charAt of $s with index $i"))
    case IntSign.TopSign => j.joinWithFailure(Suffix(""))(j.joinWithFailure(f.fail(StringNegativeIndex,
      s"charAt of $s with index $i"))(f.fail(StringIndexOutOfBounds,
      s"charAt of $s with index $i")))



  override def compareTo(s1: StringSuffix, s2: StringSuffix): IntSign = IntSign.TopSign


  // Bei ungültigem Index wird false zurückgegeben (auch negativ)
  override def startsWith(s: StringSuffix, Suffix: StringSuffix, offset: IntSign): Topped[Boolean] = offset match
    case IntSign.Neg => Topped.Actual(false)
    case _ => Topped.Top

  override def indexOf(s: StringSuffix, word: StringSuffix, fromIndex: IntSign): IntSign = IntSign.TopSign


given SuffixStringOpsNumericIntervall(using f: Failure, j: EffectStack): SuffixStringOps[NumericInterval[Int]] with

  override def substring(s: StringSuffix, begin: NumericInterval[Int], end: NumericInterval[Int]): StringSuffix =
    SuffixStringOpsSign.substring(s, intervalAsSign(begin), intervalAsSign(end))



  override def length(s: StringSuffix): NumericInterval[Int] = s match
    case Suffix(su) =>  NumericInterval[Int].tupled(su.length, Int.MaxValue)

  override def charAt(s: StringSuffix, i: NumericInterval[Int]): StringSuffix = Suffix("")


  override def compareTo(s1: StringSuffix, s2: StringSuffix): NumericInterval[Int] = NumericInterval.Top()

  override def startsWith(s: StringSuffix, Suffix: StringSuffix, offset: NumericInterval[Int]): Topped[Boolean] = Topped.Top

  override def indexOf(s: StringSuffix, word: StringSuffix, fromIndex: NumericInterval[Int]): NumericInterval[Int] =
    NumericInterval.Bounded(-1, Int.MaxValue)







