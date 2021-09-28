//package sturdy.language.wasm.abstractions
//
//import sturdy.language.wasm.Interpreter
//import sturdy.values.Structural
//import sturdy.values.doubles.{ConcreteDoubleOps, DoubleOps, LiftedDoubleOps}
//import sturdy.values.floats.{ConcreteFloatOps, FloatOps, LiftedFloatOps}
//import sturdy.values.ints.{IntOps, LiftedIntOps, ConcreteIntOps}
//import sturdy.values.longs.{LongOps, LiftedLongOps, ConcreteLongOps}
//import sturdy.values.relational.{CompareOps, LiftedCompareOps, given}
//
//import scala.language.implicitConversions
//
//object ConcreteValues:
//
////  implicit def wasmInt(i: Int): WasmInt = new WasmInt(i)
////  implicit def wasmLong(l: Long): WasmLong = new WasmLong(l)
//  implicit def wasmFloat(f: Float): WasmFloat = new WasmFloat(f)
//  implicit def wasmDouble(d: Double): WasmDouble = new WasmDouble(d)
//
////  case class WasmInt(val i: Int) extends AnyVal
////  case class WasmLong(val l: Long) extends AnyVal
//
//  class WasmFloat(val f: Float) extends AnyVal:
//    override def equals(obj: Any): Boolean = obj match
//      case that: WasmFloat =>
//        if (this.f.isNaN || that.f.isNaN) false
//        else if (this.f == 0f && that.f == 0f) true
//        else this.f == that.f
//      case _ => false
//
//    override def hashCode(): Int =
//      if (this.f.isNaN) Float.NaN.hashCode()
//      else if (this.f == 0f) 0f.hashCode()
//      else this.f.hashCode()
//
//  class WasmDouble(val d: Double) extends AnyVal:
//    override def equals(obj: Any): Boolean = obj match
//      case that: WasmDouble =>
//        if (this.d.isNaN || that.d.isNaN) false
//        else if (this.d == 0d && that.d == 0d) true
//        else this.d == that.d
//      case _ => false
//
//    override def hashCode(): Int =
//      if (this.d.isNaN) Double.NaN.hashCode()
//      else if (this.d == 0d) 0d.hashCode()
//      else this.d.hashCode()
//
////  given Structural[WasmInt] with {}
////  given Structural[WasmLong] with {}
//  given Structural[WasmFloat] with {}
//  given Structural[WasmDouble] with {}
//
////  given IntOps[WasmInt] = new LiftedIntOps(_.i, new WasmInt(_))
////  given LongOps[WasmLong] = new LiftedLongOps(_.l, new WasmLong(_))
//  given FloatOps[WasmFloat] = new LiftedFloatOps(_.f, new WasmFloat(_))
//  given DoubleOps[WasmDouble] = new LiftedDoubleOps(_.d, new WasmDouble(_))
//  given CompareOps[WasmFloat, Boolean] = new LiftedCompareOps(_.f, identity[Boolean])
//  given CompareOps[WasmDouble, Boolean] = new LiftedCompareOps(_.d, identity[Boolean])
