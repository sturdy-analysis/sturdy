package sturdy.language.wasm.wasmbench

import org.json4s.JsonAST.{JInt, JValue}
import sturdy.language.wasm.Interpreter
import sturdy.values.integer.NumericInterval
import sturdy.language.wasm.analyses.{ConstantAnalysis, IntervalAnalysis, ConstantTaintAnalysis, TypeAnalysis}
import sturdy.language.wasm.generic.GenericInterpreter
import sturdy.values.Topped
import sturdy.values.taint.TaintProduct
import sturdy.values.types.BaseType
import org.json4s.{CustomSerializer, JField, JInt, JObject, JString}
import swam.ValType

// (type (;1;) (func (param i32 i64 i32) (result i64)))

// (func (;10;) (type 1)
// (func $xlsx_get_sheets (type 4) (param i32 i32)

// (export "_start" (func $_start))
// (export "resume" (func 22))

enum WASMType:
  case I32
  case I64
  case F32
  case F64

  override def toString: String = this match
    case I32 => "I32"
    case I64 => "I64"
    case F32 => "F32"
    case F64 => "F64"

  def toAnalysisValue[T <: Interpreter](analysis: T) = this match
    case I32 => analysis.Value.Num(analysis.NumValue.Int32(analysis.topI32))
    case I64 => analysis.Value.Num(analysis.NumValue.Int64(analysis.topI64))
    case F32 => analysis.Value.Num(analysis.NumValue.Float32(analysis.topF32))
    case F64 => analysis.Value.Num(analysis.NumValue.Float64(analysis.topF64))



class WASMTypeSerializer extends CustomSerializer[WASMType](format => (
  {
    case JObject(JField("WASMType", JString(v)) :: Nil) =>
      v match
        case "I32" => WASMType.I32
        case "I64" => WASMType.I64
        case "F32" => WASMType.F32
        case "F64" => WASMType.F64
  },
  {
    case x: WASMType =>
      JObject(JField("WASMType", JString(x.toString)) :: Nil)
  }
))

object WASMType:
  def apply(s: String): WASMType = s match {
    case "i32" | "I32" => I32
    case "f32" | "F32" => F32
    case "i64" | "I64" => I64
    case "f64" | "F64" => F64
    case _ => ???
  }

  import WASMType.*

  def toTaintAnalysisValue(wty: WASMType): ConstantTaintAnalysis.Value = wty match
    case I32 => ConstantTaintAnalysis.Value.Num(ConstantTaintAnalysis.NumValue.Int32(ConstantTaintAnalysis.topI32))
    case I64 => ConstantTaintAnalysis.Value.Num(ConstantTaintAnalysis.NumValue.Int64(ConstantTaintAnalysis.topI64))
    case F32 => ConstantTaintAnalysis.Value.Num(ConstantTaintAnalysis.NumValue.Float32(ConstantTaintAnalysis.topF32))
    case F64 => ConstantTaintAnalysis.Value.Num(ConstantTaintAnalysis.NumValue.Float64(ConstantTaintAnalysis.topF64))

  def toTypeAnalysisValue(wty: WASMType): TypeAnalysis.Value = wty match
    case I32 => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Int32(TypeAnalysis.topI32))
    case I64 => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Int64(TypeAnalysis.topI64))
    case F32 => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Float32(TypeAnalysis.topF32))
    case F64 => TypeAnalysis.Value.Num(TypeAnalysis.NumValue.Float64(TypeAnalysis.topF64))

  def toConstantAnalysisValue(wty: WASMType): ConstantAnalysis.Value= wty match
    case I32 => ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Int32(Topped.Top))
    case I64 => ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Int64(Topped.Top))
    case F32 => ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Float32(Topped.Top))
    case F64 => ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Float64(Topped.Top))

  def toIntervalAnalysisValue(wty: WASMType): IntervalAnalysis.Value= wty match
    case I32 => IntervalAnalysis.Value.Num(IntervalAnalysis.NumValue.Int32(NumericInterval(Integer.MIN_VALUE, Integer.MAX_VALUE)))
    case I64 => IntervalAnalysis.Value.Num(IntervalAnalysis.NumValue.Int64(NumericInterval(Long.MinValue, Long.MaxValue)))
    case F32 => IntervalAnalysis.Value.Num(IntervalAnalysis.NumValue.Float32(Topped.Top))
    case F64 => IntervalAnalysis.Value.Num(IntervalAnalysis.NumValue.Float64(Topped.Top))

enum Label:
  case Numeric(i: Int)
  case Symbolic(s: String)

  override def toString: String = this match 
    case Numeric(i) => s"(;$i;)"
    case Symbolic(s) => s"$$$s"
  
  def getString: String = this match
    case Numeric(i) => i.toString
    case Symbolic(s) => s
    
  def toNum: Int = this match 
    case Numeric(i) => i
    case _ => ???
  
object Label:
  def apply(s: String, forceSymbolic: Boolean = true): Label =
    if forceSymbolic then
      return Symbolic(s)
    try
      Numeric(s.toInt)
    catch
      case _ => Symbolic(s)
  def apply(i: Int): Label =
    Numeric(i)

class LabelSerializer extends CustomSerializer[Label](format => (
  {
    case JObject(JField("i", JInt(n)) :: Nil) =>
      Label(n.toInt)
    case JObject(JField("s", JString(s)) :: Nil) =>
      Label(s)
    case x =>
      println(x)
      ???
  },
  {
    case x: Label =>
      x match
        case Label.Numeric(n) =>
          JObject(JField("Label", JObject(JField("i", JInt(n)))))
        case Label.Symbolic(s) =>
          JObject(JField("label", JObject(JField("s", JString(s)))))
  }
))
    
case class TypeDef(label: Label, param: List[WASMType], result: List[WASMType])

case class FuncExport(name: String, label: Label)

case class FuncDef(label: Label, sig: TypeDef, exportedAs: Option[String])