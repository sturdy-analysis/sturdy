package sturdy.language.tip_xdai.record.sign

import sturdy.effect.EffectStack
import sturdy.effect.except.Except
import sturdy.effect.failure.Failure
import sturdy.language.tip_xdai.core.Value
import sturdy.language.tip_xdai.core.sign.{SignEqOps as CoreSignEqOps, SignJoin as CoreSignJoin}
import sturdy.language.tip_xdai.core.abstractions.{CoreJoin, TopValue}
import sturdy.language.tip_xdai.record.Field
import sturdy.values.MaybeChanged.{Changed, Unchanged}
import sturdy.values.integer.{IntSign, IntegerDivisionByZero, IntegerOps}
import sturdy.values.ordering.{EqOps, OrderingOps}
import sturdy.values.*


case class RecordValue(value: Map[Field, Value]) extends Value:
  override def toString: String = value.toString

// TODO: Surely we can use something built-in here instead
trait SignEqOps extends CoreSignEqOps:
  val boolFalse = boolToValue(Topped.Actual(false))
  val boolTrue = boolToValue(Topped.Actual(true))

  override def equ(v1: Value, v2: Value): Value = (v1, v2) match
    case (RecordValue(m1), RecordValue(m2)) =>
      if (m1.keySet != m2.keySet)
        boolFalse
      else
        val toppedBools = m1.keySet.map(k => equ(m1(k), m2(k)))
        if (toppedBools.contains(TopValue))
          TopValue
        else if (toppedBools.contains(boolFalse))
          boolFalse
        else
          boolTrue
    case _ => super.equ(v1, v2)

  override def neq(v1: Value, v2: Value): Value = (v1, v2) match
    case (RecordValue(m1), RecordValue(m2)) =>
      if (m1.keySet != m2.keySet)
        boolTrue
      else
        val toppedBools = m1.keySet.map(k => equ(m1(k), m2(k)))
        if (toppedBools.contains(TopValue))
          TopValue
        else if (toppedBools.contains(boolFalse))
          boolTrue
        else
          boolFalse
    case _ => super.neq(v1, v2)

trait SignJoin extends CoreSignJoin:
  override def combine(lhs: Value, rhs: Value): MaybeChanged[Value] = (lhs, rhs) match
    case (RecordValue(m1), RecordValue(m2)) =>
      if (m1.keySet != m2.keySet)
        Changed(TopValue)
      else
        val combined = for ((k, v) <- m1) yield k -> combine(m2(k), v)
        val res = RecordValue(combined.map((k, v) => k -> v.get))
        if (combined.values.exists(_.isInstanceOf[Changed[_]]))
          Changed(res)
        else
          Unchanged(res)
    case _ => super.combine(lhs, rhs)
