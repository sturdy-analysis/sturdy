package sturdy.language.jimple
import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.values.booleans.BooleanBranching
import sturdy.values.convert.*
import sturdy.values.exceptions.Exceptional
import sturdy.values.floating.*
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.*
import sturdy.values.relational.OrderingOps
import sturdy.values.relational.EqOps
import sturdy.values.relational.UnsignedCompareOps
import sturdy.values.types.TypeOfOps


trait JimpleOps[V, Type, J[_] <: MayJoin[_]]:
  val intOps: IntegerOps[Int, V]
  val longOps: IntegerOps[Long, V]
  val floatOps: FloatOps[Float, V]
  val doubleOps: FloatOps[Double, V]
  val classOps: ClassOps[Container, V]
  val typeOfOps: TypeOfOps[V, Type, J]
  val objectOps: ObjectOps[String, V, V, Type]
  val convertIntLong: ConvertIntLong[V, V]
  val convertIntFloat: ConvertIntFloat[V, V]
  val convertIntDouble: ConvertIntDouble[V, V]
  val convertLongFloat: ConvertLongFloat[V, V]
  val convertLongDouble: ConvertLongDouble[V, V]
  val convertFloatDouble: ConvertFloatDouble[V, V]
  val compareLongOps: CompareLongOps[V, V]
  val compareFloatingOps: CompareFloatingOps[V, V]