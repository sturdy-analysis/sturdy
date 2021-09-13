package sturdy.language.tip.abstractions

import sturdy.language.tip.Interpreter
import sturdy.values.records.ARecord
import sturdy.values.records.ARecordJoin
import sturdy.values.JoinValue
import sturdy.util.Lazy

object Records:
  trait PreciseFieldsOrTop extends Interpreter :
    final type VRecord = ARecord[String, Value]

    final def topRecord(using Interpreter): ARecord[String, Value] = ARecord.Top()

    given recordJoin(using Lazy[JoinValue[Value]]): JoinValue[VRecord] = new ARecordJoin
