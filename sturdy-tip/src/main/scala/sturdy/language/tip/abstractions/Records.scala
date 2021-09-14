package sturdy.language.tip.abstractions

import sturdy.fix.Widening
import sturdy.language.tip.Interpreter
import sturdy.values.records.ARecord
import sturdy.values.records.ARecordJoin
import sturdy.values.JoinValue
import sturdy.util.Lazy
import sturdy.values.records.ARecordWidening

object Records:
  trait PreciseFieldsOrTop extends Interpreter :
    final type VRecord = ARecord[String, Value]

    final def topRecord(using Instance): ARecord[String, Value] = ARecord.Top()

    given recordJoin(using Lazy[JoinValue[Value]]): JoinValue[VRecord] = new ARecordJoin
    given recordWidening(using Lazy[Widening[Value]]): Widening[VRecord] = new ARecordWidening
