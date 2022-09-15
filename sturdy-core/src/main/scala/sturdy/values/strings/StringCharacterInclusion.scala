package sturdy.values.strings

import org.eclipse.collections.impl.block.procedure.FastListSelectProcedure
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.booleans.ToppedBooleanOps
import sturdy.values.integer.{IntSign, NumericInterval}
import sturdy.values.integer.IntSign.*
import sturdy.values.integer.NumericInterval.*
import sturdy.values.strings.CharacterInclusionUtil.*
import sturdy.values.relational.*
import sturdy.values.records.ARecord
import sturdy.values.integer.CombineIntSign

import java.security.KeyStore.TrustedCertificateEntry

enum StringCharacterInclusion:
  case StringSets(mustContain: Set[Char], mayContain: Topped[Set[Char]])


import sturdy.values.strings.StringCharacterInclusion.*

given Abstractly[String, StringCharacterInclusion] with
  override def apply(s: String): StringCharacterInclusion =
    StringSets(s.toCharArray.toSet[Char], Topped.Actual(s.toCharArray.toSet[Char]))


given PartialOrder[StringCharacterInclusion] with
  override def lteq(x: StringCharacterInclusion, y: StringCharacterInclusion): Boolean = (x,y) match
    case (StringSets(c1 :Set[Char], mc1 : Topped[Set[Char]]), StringSets(c2 :Set[Char], mc2 : Topped[Set[Char]])) =>
      (mc1, mc2) match
        case (Topped.Actual(amc1), Topped.Actual(amc2)) => c2.subsetOf(c1) && amc1.subsetOf(amc2)
        case (Topped.Top, Topped.Top) | (_, Topped.Top) => c2.subsetOf(c1)
        case _ => false


given CombineStringCharacterInclusion[W <: Widening]: Combine[StringCharacterInclusion, W] with
  override def apply(v1: StringCharacterInclusion, v2: StringCharacterInclusion): MaybeChanged[StringCharacterInclusion] =
    if v1 == v2 then Unchanged(v1)
    else if PartialOrder[StringCharacterInclusion].lteq(v1, v2) then Changed(v2)
    else if PartialOrder[StringCharacterInclusion].lteq(v1, v2) then Unchanged(v1)
    else (v1, v2) match
      case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
        if (mc1.isTop || mc2.isTop){
          Changed(StringSets(c1.intersect(c2), Topped.Top))}
        else{
          Changed(StringSets(c1.intersect(c2), Topped.Actual(mc1.get.union(mc2.get))))
        }




given CharacterInclusionStringOps(using f: Failure, j: EffectStack): StringOps[StringCharacterInclusion, IntSign, Topped[Boolean]] with

  def stringLit(s: String): StringCharacterInclusion = StringSets(s.toCharArray.toSet[Char], Topped.Actual(s.toCharArray.toSet[Char]))

  override def concat(s1: StringCharacterInclusion, s2: StringCharacterInclusion): StringCharacterInclusion = (s1, s2) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) => StringSets(c1.union(c2), toppedCharSetUnion(mc1, mc2))


  override def substring(s: StringCharacterInclusion, begin: IntSign, end: IntSign): StringCharacterInclusion = (begin, end) match
    case (IntSign.Zero, IntSign.Zero) => StringSets(Set[Char](), Topped.Actual(Set[Char]()))
    case (IntSign.Neg, _) | (_, IntSign.Neg) => j.joinWithFailure(f.fail(StringNegativeIndex,
      s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
      s"substring of $s with indices $begin to $end"))
    case (IntSign.NegOrZero, _) | (_, IntSign.NegOrZero) => s match
      case StringSets(c, mc) =>
        j.joinWithFailure(StringSets(Set[Char](), toppedCharSetUnion(c,mc)))(j.joinWithFailure(f.fail(StringNegativeIndex,
          s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds,
          s"substring of $s with indices $begin to $end")))
    case _ => s match
      case StringSets(c, mc) =>
        j.joinWithFailure(StringSets(Set[Char](), toppedCharSetUnion(c, mc)))(f.fail(StringIndexOutOfBounds,
          s"substring of $s with indices $begin to $end"))


  override def contains(s: StringCharacterInclusion, w: StringCharacterInclusion): Topped[Boolean] = (s, w) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
      if (c2.isEmpty && toppedCharSetIsEmpty(mc2)) {
        Topped.Actual(true)
      }  else{
      if(!toppedCharSetSubsetOf(c2,mc1))
      {
        Topped.Actual(false)
      }
      else Topped.Top
    }

  override def length(s: StringCharacterInclusion): IntSign = s match
    case StringSets(c, mc) => if toppedCharSetIsEmpty(mc) then IntSign.Zero else IntSign.Pos



  override def isEmpty(s: StringCharacterInclusion): Topped[Boolean] = s match
    case StringSets(c, mc) =>
      if(toppedCharSetIsEmpty(mc)){
        return Topped.Actual(true)
      }
      if (!c.isEmpty){
        return Topped.Actual(false)
      }
      Topped.Top


  override def charAt(s: StringCharacterInclusion, i: IntSign): StringCharacterInclusion = s match
    case StringSets(c, mc) =>
      if(mc.isActual){
          if (c.size == 1 && mc.get.size == 1){
            i match
              case IntSign.Zero => return s
              case IntSign.Pos | IntSign.ZeroOrPos => j.joinWithFailure(s)(f.fail(StringIndexOutOfBounds,
                s"Char at $i of $s is out of bounds"))
              case IntSign.NegOrZero => j.joinWithFailure(s)(f.fail(StringNegativeIndex,
                s"Char at $i of $s is out of bounds"))
              case IntSign.Neg => f.fail(StringNegativeIndex,
                s"Char at $i of $s is out of bounds")
          }
          if (mc.get.isEmpty){
            i match
              case IntSign.Zero | IntSign.ZeroOrPos | IntSign.Pos => f.fail(StringIndexOutOfBounds,
                s"Char at $i of $s is out of bounds")
              case IntSign.Neg => f.fail(StringNegativeIndex,
                s"Char at $i of $s is out of bounds")
              case IntSign.NegOrZero => j.joinWithFailure(f.fail(StringNegativeIndex,
                s"Char at $i of $s is out of bounds"))(f.fail(StringIndexOutOfBounds,
                s"Char at $i of $s is out of bounds"))
          }
        }
      i match
        case IntSign.Zero => StringCharacterInclusion.StringSets(Set[Char](), mc)
        case IntSign.Pos | ZeroOrPos => j.joinWithFailure(StringCharacterInclusion.StringSets(Set[Char](), mc))(f.fail(StringIndexOutOfBounds,
          s"Char at $i of $s is out of bounds"))
        case IntSign.Neg => f.fail(StringNegativeIndex, s"Char at $i of $s is out of bounds")
        case IntSign.NegOrZero => j.joinWithFailure(StringCharacterInclusion.StringSets(Set[Char](), mc))(f.fail(StringNegativeIndex,
          s"Char at $i of $s is out of bounds"))




  override def equals(s1: StringCharacterInclusion, s2: StringCharacterInclusion): Topped[Boolean] = (s1, s2) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
      if(toppedCharSetIsEmpty(mc1) && toppedCharSetIsEmpty(mc2)){
        return Topped.Actual(true)
      }
      if (!toppedCharSetSubsetOf(c1, mc2) || !toppedCharSetSubsetOf(c2, mc1))
      {
        return Topped.Actual(false)
      }
      Topped.Top



  override def compareTo(s1: StringCharacterInclusion, s2: StringCharacterInclusion): IntSign = (s1, s2) match
    case (StringSets(c1, Topped.Top), StringSets(c2, Topped.Top)) => IntSign.TopSign
    case (StringSets(c1, Topped.Top), StringSets(c2, _)) => IntSign.ZeroOrPos
    case (StringSets(c1, _), StringSets(c2, Topped.Top)) => IntSign.NegOrZero
    case (StringSets(c1, Topped.Actual(mc1)), StringSets(c2, Topped.Actual(mc2))) =>
      if(mc1.isEmpty && mc2.isEmpty){
        IntSign.Zero
      }
      else{
        val mc1Array = mc1.toArray[Char]
        val mc2Array = mc2.toArray[Char]
        var firstIteration = true
        var state = IntSign.TopSign
        for(x <- mc1Array){
          for(y <- mc2Array){
            if(firstIteration) {
              firstIteration = false
              if(x < y){
                state = IntSign.Neg
              }
              if(x > y){
                state = IntSign.Pos
              }
              if(x == y){
                state = IntSign.Zero
              }
            }
            else{
              if(x < y){
                state = CombineIntSign(state, IntSign.Neg).get
              }
              if(x > y){
                state = CombineIntSign(state, IntSign.Pos).get
              }
              if(x == y){
                state = CombineIntSign(state, IntSign.Zero).get
              }
            }

          }
        }
        state
      }

  // Bei ungültigem Index wird false zurückgegeben (auch negativ)
  override def startsWith(s: StringCharacterInclusion, prefix: StringCharacterInclusion, offset: IntSign): Topped[Boolean] = offset match
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

  override def endsWith(s: StringCharacterInclusion, suffix: StringCharacterInclusion): Topped[Boolean] = (s, suffix) match
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


  override def indexOf(s: StringCharacterInclusion, word: StringCharacterInclusion, fromIndex: IntSign): IntSign = (s, word) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
      if (!toppedCharSetSubsetOf(c2, mc1)){
        return IntSign.Neg
      }
      IntSign.TopSign

  override def replace(s: StringCharacterInclusion, word: StringCharacterInclusion, newWord: StringCharacterInclusion): StringCharacterInclusion =
    (s, word, newWord) match
      case (StringSets(sC, sMc), StringSets(wC, wMc), StringSets(nwC, nwMc)) =>
        if(!toppedCharSetSubsetOf(wC,sMc)){
          return s
        }
        if(toppedCharSetIsEmpty(wMc) && toppedCharSetIsEmpty(sMc)){
          return newWord
        }
        StringSets(toppedCharSetDifference(sC, wMc), toppedCharSetUnion(sMc, nwMc))



  override def toLowerCase(s: StringCharacterInclusion): StringCharacterInclusion = s match
    case StringSets(c, mc) => mc match
      // Theoretisch könnte man aus mc noch alle Großbuchstaben entfernen, aber da mc Top und kein konkretes
      // set ist, haben wir uns dagegen entschieden
      case Topped.Top => StringSets(c.map(char => char.toLower), mc)
      case Topped.Actual(amc) => StringSets(c.map(char => char.toLower), Topped.Actual(amc.map(char => char.toLower)))

  override def toUpperCase(s: StringCharacterInclusion): StringCharacterInclusion = s match
    case StringSets(c, mc) => mc match
      case Topped.Top => StringSets(c.map(char => char.toUpper), mc)
      case Topped.Actual(amc) => StringSets(c.map(char => char.toUpper), Topped.Actual(amc.map(char => char.toUpper)))

  override def trim(s: StringCharacterInclusion): StringCharacterInclusion = s match
    case StringSets(c, mc) =>
      if (c == Set(' ')) {
      return StringSets(Set[Char](), Topped.Actual(Set[Char]()))
      }
      if(!toppedCharSetConatins(mc, ' ')){
        s
      }
      else StringSets(c -- Set(' '), mc)

  //override def split(s: StringCharacterInclusion, splitChar: StringCharacterInclusion): ARecord[Int, StringCharacterInclusion] = ???

given CharacterInclusionStringOpsNumericIntervall(using f: Failure, j: EffectStack): StringOps[StringCharacterInclusion, NumericInterval[Int], Topped[Boolean]] with
  def stringLit(s: String): StringCharacterInclusion = StringSets(s.toCharArray.toSet[Char], Topped.Actual(s.toCharArray.toSet[Char]))

  override def concat(s1: StringCharacterInclusion, s2: StringCharacterInclusion): StringCharacterInclusion = ???

  override def substring(s: StringCharacterInclusion, begin: NumericInterval[Int], end: NumericInterval[Int]): StringCharacterInclusion =
    if (isIntervalZero(begin) && isIntervalZero(end)){
      return StringSets(Set[Char](),Topped.Actual(Set[Char]()))
    }
    s match
      case StringSets(c, mc) =>
        if (mc.isActual){
          if(c.size == 1 && mc.size == 1){
            if(isIntervalOne(begin) && isIntervalOne(end)){
              return StringSets(Set[Char](),Topped.Actual(Set[Char]()))
            }
            if (isIntervalZero(begin) && isIntervalOne(end)){
              return s
            }
          }
        }
        if (isIntervalNegative(begin) || isIntervalNegative(end)){
          return f.fail(StringNegativeIndex, s"substring of $s with indices $begin to $end")
        }
        if (isIntervalLTEQ(begin, end)){
          return j.joinWithFailure(StringSets(Set[Char](), toppedCharSetUnion(c, mc)))(f.fail(StringIndexOutOfBounds,
            s"substring of $s with indices $begin to $end"))
        }
        f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end")

  override def contains(s: StringCharacterInclusion, w: StringCharacterInclusion): Topped[Boolean] = ???

  override def length(s: StringCharacterInclusion): NumericInterval[Int] = s match
    case StringSets(c, mc) => if toppedCharSetIsEmpty(mc) then NumericInterval[Int].tupled(0, 0) else NumericInterval[Int].tupled(c.size, Int.MaxValue)

  override def charAt(s: StringCharacterInclusion, i: NumericInterval[Int]): StringCharacterInclusion =
    i match
      case Bounded(low, high) => s match
        case StringSets(c, mc) =>
          if (low >= 0 && high < c.size && high >= 0){
            StringSets(Set[Char](), mc)
          }
          else CharacterInclusionStringOps.charAt(s, intervalAsSign(i))
      case NumericInterval.Top() => CharacterInclusionStringOps.charAt(s, intervalAsSign(i))


  override def equals(s1: StringCharacterInclusion, s2: StringCharacterInclusion): Topped[Boolean] = ???

  override def compareTo(s1: StringCharacterInclusion, s2: StringCharacterInclusion): NumericInterval[Int] = ???

  override def startsWith(s: StringCharacterInclusion, prefix: StringCharacterInclusion, offset: NumericInterval[Int]): Topped[Boolean] =
    CharacterInclusionStringOps.startsWith(s, prefix, intervalAsSign(offset))

  override def endsWith(s: StringCharacterInclusion, suffix: StringCharacterInclusion): Topped[Boolean] = ???

  override def indexOf(s: StringCharacterInclusion, word: StringCharacterInclusion, fromIndex: NumericInterval[Int]): NumericInterval[Int] =
    signAsInterval(CharacterInclusionStringOps.indexOf(s, word, intervalAsSign(fromIndex)))

  override def replace(s: StringCharacterInclusion, word: StringCharacterInclusion, newWord: StringCharacterInclusion): StringCharacterInclusion = ???

  override def toLowerCase(s: StringCharacterInclusion): StringCharacterInclusion = ???

  override def toUpperCase(s: StringCharacterInclusion): StringCharacterInclusion = ???

  override def trim(s: StringCharacterInclusion): StringCharacterInclusion = ???

  override def isEmpty(s: StringCharacterInclusion): Topped[Boolean] = ???



