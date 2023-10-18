package sturdy.language.bytecode.generic

import sturdy.values.convert.*
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.relational.*
 
trait BytecodeOps[V]:
  //val i8ops:  IntegerOps[Byte, V]
  //val i16ops: IntegerOps[Short, V]
  val i32ops: IntegerOps[Int, V]
  val i64ops: IntegerOps[Long, V]
  val f32ops: FloatOps[Float, V]
  val f64ops: FloatOps[Double, V]
  
  val f32compare: OrderingOps[V, V]
  
  val convert_i32_i64: ConvertIntLong[V, V]
  val convert_i32_f32: ConvertIntFloat[V, V]
  val convert_i32_f64: ConvertIntDouble[V, V]
  val convert_i64_i32: ConvertLongInt[V, V]
  val convert_i64_f32: ConvertLongFloat[V, V]
  val convert_i64_f64: ConvertLongDouble[V, V]
  val convert_f32_i32: ConvertFloatInt[V, V]
  val convert_f32_i64: ConvertFloatLong[V, V]
  val convert_f32_f64: ConvertFloatDouble[V, V]
  val convert_f64_i32: ConvertDoubleInt[V, V]
  val convert_f64_i64: ConvertDoubleLong[V, V]
  val convert_f64_f32: ConvertDoubleFloat[V, V]
  