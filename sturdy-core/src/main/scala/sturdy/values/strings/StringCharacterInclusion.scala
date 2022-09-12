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
  // Top is empty C and all characters in MC
  case TopCharacterInclusion
  case StringSets(C : Set[Char], MC : Set[Char])



import sturdy.values.strings.StringCharacterInclusion.*

given Abstractly[String, StringCharacterInclusion] with
  override def apply(s: String): StringCharacterInclusion =
    StringSets(s.toCharArray.toSet[Char], Set())


given PartialOrder[StringCharacterInclusion] with
  override def lteq(x: StringCharacterInclusion, y: StringCharacterInclusion): Boolean = (x,y) match
    case (_, TopCharacterInclusion) => true
    case (StringSets(c1 :Set[Char], mc1 : Set[Char]), StringSets(c2 :Set[Char], mc2 : Set[Char])) => c2.subsetOf(c1) && mc1.subsetOf(mc2)
    case _ => false
  //override def lteq(x: StringCharacterInclusion, y: StringCharacterInclusion): Boolean = x match
  //  case StringSets(c1 :Set[Char], mc1 : Set[Char]) => y match
  //    case StringSets(c2 : Set[Char], mc2 : Set[Char]) => c2.subsetOf(c1) && mc1.subsetOf(mc2) //&& mc1 != mc2
  //  case _ => false



given CombineStringCharacterInclusion[W <: Widening]: Combine[StringCharacterInclusion, W] with
  override def apply(v1: StringCharacterInclusion, v2: StringCharacterInclusion): MaybeChanged[StringCharacterInclusion] =
    if v1 == v2 then Unchanged(v1)
    else if PartialOrder[StringCharacterInclusion].lteq(v1, v2) then Changed(v2)
    else if PartialOrder[StringCharacterInclusion].lteq(v1, v2) then Unchanged(v1)
    else (v1, v2) match
      case (StringSets(c1, mc1), StringSets(c2, mc2)) => Changed(StringSets(c1.intersect(c2), mc1.union(mc2)))


given CharacterInclusionStringOps[B](using f: Failure, j: EffectStack): StringOps[StringCharacterInclusion, IntSign, Topped[Boolean]] with
  def stringLit(s: String): StringCharacterInclusion = StringSets(s.toString.toCharArray.toSet[Char], Set())
  override def concat(s1: StringCharacterInclusion, s2: StringCharacterInclusion): StringCharacterInclusion = (s1, s2) match
    case (StringSets(c1, mc1), StringSets(c2, mc2)) => StringSets(c1.union(c2), mc1.union(mc2))

  override def substring(s: StringCharacterInclusion, begin: IntSign, end: IntSign): StringCharacterInclusion = (begin, end) match
    case (IntSign.Zero, IntSign.Zero) => StringSets(Set[Char](), Set[Char]())
//    case (IntSign.Neg, _) | (_, IntSign.Neg) => f.fail(StringNegativeIndex, s"substring of $s with indices $begin to $end")
//    case (IntSign.NegOrZero, _) | (_, IntSign.NegOrZero) => s match
//      case StringSets(c, mc) =>
//        j.joinWithFailure(StringSets(Set[Char](), c.union(mc)))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end"))
    case _ => s match
      case StringSets(c, mc) =>
        j.joinWithFailure(StringSets(Set[Char](), c.union(mc)))(f.fail(StringIndexOutOfBounds, s"substring of $s with indices $begin to $end"))

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


