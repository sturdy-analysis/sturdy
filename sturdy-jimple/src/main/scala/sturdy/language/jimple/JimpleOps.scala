package sturdy.language.jimple

import sturdy.data.MayJoin
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.relational.CompareOps
import sturdy.values.types.TypeOfOps

// all value operations used in jimple
trait JimpleOps[V, T, J[_] <: MayJoin[_]]:
  val intOps: IntegerOps[Int, V]
  val longOps: IntegerOps[Long, V]
  val floatOps: FloatOps[Float, V]
  val doubleOps: FloatOps[Double, V]
  val classOps: ClassOps[Container, V]
  val typeOfOps: TypeOfOps[V, T, J]
  val objectOps: ObjectOps[String, V, V, T]
  val convertIntLong: ConvertIntLong[V, V]
  val convertIntFloat: ConvertIntFloat[V, V]
  val convertIntDouble: ConvertIntDouble[V, V]
  val convertLongFloat: ConvertLongFloat[V, V]
  val convertLongDouble: ConvertLongDouble[V, V]
  val convertFloatDouble: ConvertFloatDouble[V, V]
  val compareLongOps: CompareOps[V, V]
  val compareDoubleOps: CompareOps[V, V]
  val stringOps: StringOps[String, V]