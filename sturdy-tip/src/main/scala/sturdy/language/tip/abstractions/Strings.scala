package sturdy.language.tip.abstractions

import sturdy.effect.failure.Failure
import sturdy.language.tip.{Interpreter, TipFailure}
import sturdy.values.Topped
import sturdy.values.integer.NumericInterval.IsZero
import sturdy.values.integer.{IntSign, NumericInterval}
import sturdy.values.relational.EqOps
import sturdy.values.strings.{StringCharacterInclusion, StringPrefix, StringSuffix}

object Strings:
  trait CharacterInclusion extends Interpreter :
    final type VString = StringCharacterInclusion

    override def topString: StringCharacterInclusion = StringCharacterInclusion.StringSets(Set[Char](), Topped.Top)


  trait Prefix extends Interpreter :
    final type VString = StringPrefix

    override def topString: StringPrefix = StringPrefix.Prefix("")


  trait Suffix extends Interpreter :
    final type VString = StringSuffix

    override def topString: StringSuffix = StringSuffix.Suffix("")



