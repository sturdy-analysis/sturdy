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




given CombineStringCharacterInclusion[W <: Widening]: Combine[StringPrefix, W] with
  override def apply(v1: StringPrefix, v2: StringPrefix): MaybeChanged[StringPrefix] =
    if v1 == v2 then Unchanged(v1)
    else if PartialOrder[StringPrefix].lteq(v1, v2) then Changed(v2)
    else if PartialOrder[StringPrefix].lteq(v1, v2) then Unchanged(v1)
    else (v1, v2) match
      case (Prefix(p1), Prefix(p2)) =>
        var joinedPrefix = ""
        for(x <- p1){
          for(y <- p2){
            if (x==y){
              joinedPrefix = joinedPrefix.appended(x)
            }
            else return Changed(Prefix(joinedPrefix))
          }
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
      case Prefix("") => f.fail(StringIndexOutOfBounds, s"charAt of $s with index $i")
      case Prefix(pre) => Prefix(pre.charAt(0).toString)
    case IntSign.NegOrZero => s match
      case Prefix("") => j.joinWithFailure(f.fail(StringIndexOutOfBounds, s"charAt of $s with index $i"))(f.fail(StringNegativeIndex,
        s"charAt of $s with inex $i "))
      case Prefix(pre) => j.joinWithFailure(Prefix(pre.charAt(0).toString))(f.fail(StringNegativeIndex,
        s"charAt of $s with inex $i "))
    case IntSign.Neg => f.fail(StringNegativeIndex, s"charAt of $s with index $i")
    case IntSign.Pos => j.joinWithFailure(Prefix(""))(f.fail(StringIndexOutOfBounds,
      s"charAt of $s with index $i"))
    case IntSign.TopSign => j.joinWithFailure(Prefix(""))(j.joinWithFailure(f.fail(StringNegativeIndex,
      s"charAt of $s with index $i"))(f.fail(StringIndexOutOfBounds,
      s"charAt of $s with index $i")))



  override def compareTo(s1: StringPrefix, s2: StringPrefix): IntSign = (s1, s2) match
    case (StringSets(_, _), StringSets(_, Topped.Top)) | (StringSets(_, Topped.Top), StringSets(_, _)) => IntSign.TopSign
    case (StringSets(c1, Topped.Actual(mc1)), StringSets(c2, Topped.Actual(mc2))) =>
      if(mc1.isEmpty && mc2.isEmpty){
        return IntSign.Zero
      }
      else{
        val mc1Array = mc1.toArray[Char]
        val mc2Array = mc2.toArray[Char]
        var firstIteration = true
        var state = IntSign.TopSign
        for(x <- mc1Array){
          for(y <- mc2Array){
            if(x == y){
              return IntSign.TopSign
            }

            if(firstIteration) {
              firstIteration = false
              if(x < y){
                state = IntSign.Neg
              }
              if(x > y){
                state = IntSign.Pos
              }
            }
            else{
              if(x < y){
                state = CombineIntSign(state, IntSign.Neg).get
              }
              if(x > y){
                state = CombineIntSign(state, IntSign.Pos).get
              }
            }

          }
        }
        state
      }

  // Bei ungültigem Index wird false zurückgegeben (auch negativ)
  override def startsWith(s: StringPrefix, prefix: StringPrefix, offset: IntSign): Topped[Boolean] = offset match
    case IntSign.Zero => (s, prefix) match
      case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
        if(toppedCharSetIsEmpty(mc2)){
          Topped.Actual(true)
        }
        val c2IsSubSetOfMc1 = if mc1.isActual then c2.subsetOf(mc1.get) else true
        if (!c2IsSubSetOfMc1)
        {
          Topped.Actual(false)
        }
        else Topped.Top

    case IntSign.Pos =>
      (s, prefix) match
        case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
          val c2IsSubSetOfMc1 = if mc1.isActual then c2.subsetOf(mc1.get) else true
          if (!c2IsSubSetOfMc1)
          {
            Topped.Actual(false)
          }
          else Topped.Top

    case IntSign.ZeroOrPos | IntSign.NegOrZero => Topped.Top
    case IntSign.Neg => Topped.Actual(false)

  override def indexOf(s: StringPrefix, word: StringPrefix, fromIndex: IntSign): IntSign = (s, word) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
      if (!toppedCharSetSubsetOf(c2, mc1)){
        return IntSign.Neg
      }
      IntSign.TopSign







given CharacterInclusionStringOpsNumericIntervall(using f: Failure, j: EffectStack): CharacterInclusionStringOps[NumericInterval[Int]] with

  override def substring(s: StringCharacterInclusion, begin: NumericInterval[Int], end: NumericInterval[Int]): StringCharacterInclusion =
    s match
      case StringSets(c, mc) =>

        if (isIntervalNegative(begin) || isIntervalNegative(end)){
          return f.fail(StringNegativeIndex, s"substring of $s with indices $begin to $end")
        }

        if (isIntervalGreater(begin, end)){
          return f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end")
        }

        if (mc.isActual){
          if(c.size == 1 && mc.size == 1 && isIntervalZero(begin) && isIntervalOne(end)) {
            return s
          }
        }

        (begin, end) match
          case (NumericInterval.Bounded(bLow, bHigh), NumericInterval.Bounded(endLow, endHigh)) =>
            if (c.size - 1 >= endHigh){
              if (bLow == bHigh && bHigh == endHigh && bLow == endLow){
                return StringSets(Set[Char](),Topped.Actual(Set[Char]()))
              }
              else return StringSets(Set[Char](), mc)
            }
            else return j.joinWithFailure(StringSets(Set[Char](), mc))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end"))
          case _ => return j.joinWithFailure(StringSets(Set[Char](), toppedCharSetUnion(c, mc)))(j.joinWithFailure(f.fail(StringNegativeIndex,
            s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
            s"substring of $s with indices $begin to $end")))



  override def length(s: StringCharacterInclusion): NumericInterval[Int] = s match
    case StringSets(c, mc) => if toppedCharSetIsEmpty(mc) then NumericInterval[Int].tupled(0, 0) else NumericInterval[Int].tupled(c.size, Int.MaxValue)

  override def charAt(s: StringCharacterInclusion, i: NumericInterval[Int]): StringCharacterInclusion =
    i match
      case Bounded(low, high) => s match
        case StringSets(c, mc) =>
          if (low >= 0 && high < c.size && high >= 0){
            StringSets(Set[Char](), mc)
          }
          else CharacterInclusionStringOpsSign.charAt(s, intervalAsSign(i))
      case NumericInterval.Top() => CharacterInclusionStringOpsSign.charAt(s, intervalAsSign(i))

  override def compareTo(s1: StringCharacterInclusion, s2: StringCharacterInclusion): NumericInterval[Int] = (s1, s2) match
    case (StringSets(_, _), StringSets(_, Topped.Top)) | (StringSets(_, Topped.Top), StringSets(_, _)) => NumericInterval.Top()
    case (StringSets(c1, Topped.Actual(mc1)), StringSets(c2, Topped.Actual(mc2))) =>
      if(mc1.isEmpty && mc2.isEmpty){
        return NumericInterval.Bounded(0,0)
      }
      else{
        if(mc1.intersect(mc2).nonEmpty){
          return NumericInterval.Top()
        }

        val mc1Array = mc1.toArray[Char]
        val mc2Array = mc2.toArray[Char]
        val s1Min = mc1Array.min
        val s1Max = mc1Array.max
        val s2Min = mc2Array.min
        val s2Max = mc2Array.max

        val val1 = s1Min.compareTo(s2Max)
        val val2 = s1Max.compareTo(s2Min)
        NumericInterval.Bounded(val1.min(val2), val1.max(val2))
    }

  override def startsWith(s: StringCharacterInclusion, prefix: StringCharacterInclusion, offset: NumericInterval[Int]): Topped[Boolean] =
    CharacterInclusionStringOpsSign.startsWith(s, prefix, intervalAsSign(offset))

  override def indexOf(s: StringCharacterInclusion, word: StringCharacterInclusion, fromIndex: NumericInterval[Int]): NumericInterval[Int] =
    signAsInterval(CharacterInclusionStringOpsSign.indexOf(s, word, intervalAsSign(fromIndex)))







