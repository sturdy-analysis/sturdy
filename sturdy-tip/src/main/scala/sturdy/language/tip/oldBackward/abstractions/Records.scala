package sturdy.language.tip.oldBackward.abstractions

import sturdy.language.tip.oldBackward.BackwardsInterpreter
import sturdy.language.tip.{Field, Interpreter}
import sturdy.util.Lazy
import sturdy.values.{Join, Widening}
import sturdy.values.records.ARecord

object Records:
  trait PreciseFieldsOrTop extends BackwardsInterpreter:
    final type VRecord = ARecord[Field, Value]
    final def topRecord: ARecord[Field, Value] = ARecord.Top()
