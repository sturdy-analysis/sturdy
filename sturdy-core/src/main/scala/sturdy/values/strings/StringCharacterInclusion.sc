package sturdy.values.strings

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.relational.*

enum StringCharacterInclusion:
  case TopCharacterInclusion
  case StringSets(C : Set[Char], MC : Set[Char])



import sturdy.values.strings.StringCharacterInclusion.*

given Abstractly[String, StringCharacterInclusion] with
  override def apply(s: String): StringCharacterInclusion =
    StringSets(s.chars(), Set())


given PartialOrder[StringCharacterInclusion] with
  override def lteq(x: StringCharacterInclusion, y: StringCharacterInclusion): Boolean = x match
    case StringSets(C1 :Set[Char], MC1 : Set[Char]) => y match
      case StringSets(C2 : Set[Char], MC2 : Set[Char]) => C2.subsetOf(C1) && MC1.subsetOf(MC2) && !MC1.equals(MC2)
      case TopCharacterInclusion => True
    case _ => False
    //y == TopCharacterInclusion || x == StringSets(Set(),Set()) ||
//x == y || x < y


given CombineIntSign[W <: Widening]: Combine[StringCharacterInclusion, W] with
  override def apply(v1: StringCharacterInclusion, v2: StringCharacterInclusion): MaybeChanged[StringCharacterInclusion] =
    if v1 == v2  then Unchanged(v1)
    else if v1 <= v2 then Changed(v2)
    else if v2 <= v1 then Unchanged(v1)
    else _ => Changed(TopCharacterInclusion)


given CharacterInclusionStringOps[B](using f: Failure, j: EffectStack): StringOps[B, StringCharacterInclusion] with
  def stringLit(s: B): StringCharacterInclusion = StringSets(s.chars, Set())

