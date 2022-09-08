package sturdy.values.strings

import org.eclipse.collections.impl.block.procedure.FastListSelectProcedure
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.*
import sturdy.values.relational.*

import java.security.KeyStore.TrustedCertificateEntry

enum StringCharacterInclusion:
  case TopCharacterInclusion
  case StringSets(C : Set[Char], MC : Set[Char])



import sturdy.values.strings.StringCharacterInclusion.*

given Abstractly[String, StringCharacterInclusion] with
  override def apply(s: String): StringCharacterInclusion =
    StringSets(s.toCharArray.toSet[Char], Set())


given PartialOrder[StringCharacterInclusion] with
  override def lteq(x: StringCharacterInclusion, y: StringCharacterInclusion): Boolean =
    x match
    case StringSets(c1 :Set[Char], mc1 : Set[Char]) => y match
      case StringSets(c2 : Set[Char], mc2 : Set[Char]) => c2.subsetOf(c1) && mc1.subsetOf(mc2) && !mc1.equals(mc2)
      case TopCharacterInclusion => true
    case _ => false



//given CombineIntSign[W <: Widening]: Combine[StringCharacterInclusion, W] with
//  override def apply(v1: StringCharacterInclusion, v2: StringCharacterInclusion): MaybeChanged[StringCharacterInclusion] =
//    if v1 == v2  then Unchanged(v1)
//    else if v1 <= v2 then Changed(v2)
//    else if v2 <= v1 then Unchanged(v1)
//    else _ => Changed(TopCharacterInclusion)
//

given CharacterInclusionStringOps[B](using f: Failure, j: EffectStack): StringOps[B, StringCharacterInclusion] with
  def stringLit(s: B): StringCharacterInclusion = StringSets(s.toString.toCharArray.toSet[Char], Set())

