package sturdy.language.tip.abstractions

import sturdy.language.tip.Interpreter
import sturdy.values.records.ARecord
import sturdy.values.Join
import sturdy.util.Lazy
import sturdy.values.Widening

object Records:
  trait PreciseFieldsOrTop extends Interpreter :
    final type VRecord = ARecord[String, Value]
    final def topRecord(using Instance): ARecord[String, Value] = ARecord.Top()
