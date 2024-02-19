package sturdy.language.bytecode.generic

sealed trait Type {
  def <:<(that: Type): Boolean
}

sealed abstract class ValType(val width: Int) extends Type

object ValType {
  case object I32 extends ValType(32) {
    def <:<(that: Type): Boolean =
      that == I32
  }
  
  case object I64 extends ValType(64){
    def <:<(that: Type): Boolean =
      that == I64
  }
  
  case object F32 extends ValType(32){
    def <:<(that: Type): Boolean =
      that == F32
  }
  
  case object F64 extends ValType(64){
    def <:<(that: Type): Boolean =
      that == F64
  }
 
  case object Obj extends ValType(32){
    def <:<(that: Type): Boolean =
      that == Obj
  }

  case object Array extends ValType(32){
    def <:<(that: Type): Boolean =
      that == Array
  }
}