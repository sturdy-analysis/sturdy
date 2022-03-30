package sturdy.language.wasm.wasmbench

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

object WASMType:
  def apply(s: String): WASMType = s match {
    case "i32" | "I32" => I32
    case "f32" | "F32" => F32
    case "i64" | "I64" => I64
    case "f64" | "F64" => F64
    case _ => ???
  }

enum Label:
  case Numeric(i: Int)
  case Symbolic(s: String)

  override def toString: String = this match {
    case Numeric(i) => s"(;$i;)"
    case Symbolic(s) => s"$$$s"
  }
  
  def toNum: Int = this match {
    case Numeric(i) => i
    case _ => ???
  }
  
object Label:
  def apply(s: String): Label =
    Symbolic(s)
  def apply(i: Int): Label =
    Numeric(i)
    
case class TypeDef(label: Label, param: Option[List[WASMType]], result: Option[List[WASMType]])

case class FuncExport(name: String, label: Label)

case class FuncDef(label: Label, sig: TypeDef)