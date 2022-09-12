package sturdy.values.strings

import org.eclipse.collections.impl.block.procedure.FastListSelectProcedure
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.booleans.ToppedBooleanOps
import sturdy.values.integer.{IntSign, NumericInterval}
import sturdy.values.relational.*

import java.security.KeyStore.TrustedCertificateEntry

enum StringCharacterInclusion:
  case StringSets(mustContain: Set[Char], mayContain: Topped[Set[Char]])


import sturdy.values.strings.StringCharacterInclusion.*

given Abstractly[String, StringCharacterInclusion] with
  override def apply(s: String): StringCharacterInclusion =
    StringSets(s.toCharArray.toSet[Char], Topped.Actual(Set[Char]()))


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



given CharacterInclusionStringOps[B](using f: Failure, j: EffectStack): StringOps[StringCharacterInclusion, IntSign, Topped[Boolean]] with
  def stringLit(s: String): StringCharacterInclusion = StringSets(s.toString.toCharArray.toSet[Char], Topped.Actual(Set[Char]()))

  override def concat(s1: StringCharacterInclusion, s2: StringCharacterInclusion): StringCharacterInclusion = (s1, s2) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
      var mcUnion = if mc1.isTop || mc2.isTop then Topped.Top else Topped.Actual(mc1.get.union(mc2.get))
      StringSets(c1.union(c2), mcUnion)

    //case (StringSets(c1, mc1), StringSets(c2, mc2)) =>
    //      if (mc1.isTop || mc2.isTop){
    //        StringSets(c1.union(c2), Topped.Top)}
    //      else{
    //        StringSets(c1.union(c2), Topped.Actual(mc1.get.union(mc2.get)))
    //      }


  //TODO: Error messages schöner machen, Code formatieren
  override def substring(s: StringCharacterInclusion, begin: IntSign, end: IntSign): StringCharacterInclusion = (begin, end) match
    case (IntSign.Zero, IntSign.Zero) => StringSets(Set[Char](), Topped.Actual(Set[Char]()))
    case (IntSign.Neg, _) | (_, IntSign.Neg) => j.joinWithFailure(f.fail(StringNegativeIndex, s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end"))
    case (IntSign.NegOrZero, _) | (_, IntSign.NegOrZero) => s match
      case StringSets(c, mc) =>
        var cUnionMc = if mc.isTop then Topped.Top else Topped.Actual(mc.get.union(c))
        j.joinWithFailure(StringSets(Set[Char](), cUnionMc))(j.joinWithFailure(f.fail(StringNegativeIndex, s"substring of $s with indices $begin to $end"))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end")))
    case _ => s match
      case StringSets(c, mc) =>
        var cUnionMc = if mc.isTop then Topped.Top else Topped.Actual(mc.get.union(c))
        j.joinWithFailure(StringSets(Set[Char](), cUnionMc))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end"))

  override def contains(s: StringCharacterInclusion, w: StringCharacterInclusion): Topped[Boolean] = (s, w) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) => if (c2.isEmpty && mc2.isEmpty) {
      Topped.Actual(true)
    }  else{
      if(!c2.subsetOf(c1) && c1 != c2)
      {
        Topped.Actual(false)
      }
      else Topped.Top
    }

  override def length(s: StringCharacterInclusion): IntSign = s match
    case (StringSets(c, mc)) => if (mc.isActual){
      if (c.isEmpty && mc.get.isEmpty) then IntSign.Zero else IntSign.Pos
    } else IntSign.Pos
    case _ => IntSign.Pos

  override def isEmpty(s: StringCharacterInclusion): Topped[Boolean] = s match
    case StringSets(_, Topped.Top) => Topped.Top
    case StringSets(c, mc) => if (c.isEmpty && mc.get.isEmpty) {
      Topped.Actual(true)
    }
      else {
      if (!c.isEmpty){
        Topped.Actual(false)
      }
      else Topped.Top
    }

