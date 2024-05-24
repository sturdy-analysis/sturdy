package sturdy.language.bytecode.abstractions

import org.opalj.br.{ArrayType, ClassFile, ReferenceType}
import sturdy.effect.store.ManageableAddr
import sturdy.language.bytecode.Interpreter
import sturdy.language.bytecode.generic.InstructionSite
import sturdy.values.{Combine, MaybeChanged, Powerset, Topped, Widening}
import sturdy.values.arrays.Array
import sturdy.values.objects.Object
import sturdy.values.references.AllocationSiteAddr

trait Objects extends Interpreter:

  type ObjType = ClassFile
  case class ObjAddr(site: InstructionSite) extends ManageableAddr(true)
  type FieldName = String
  case class FieldAddr(site: InstructionSite, name: String) extends ManageableAddr(true)
  type ObjRep = Topped[Object[ObjAddr, ObjType, FieldAddr, FieldName]]
  final def topObj: ObjRep = Topped.Top

  final type ArrayRep = Topped[Array[ArrayAddr, FieldAddr, ArrayType]]
  case class ArrayAddr(site: InstructionSite) extends ManageableAddr(true)
  case class ArrayElemAddr(site: InstructionSite, ix: Int) extends ManageableAddr(true)
  type TypeRep = ReferenceType
  type AType = ArrayType
  final def topArray: ArrayRep = Topped.Top

  final type NullVal = Null
  final def topNull: NullVal = null

  given combineNull[W <: Widening]: Combine[Null, W] with
    override def apply(v1: Null, v2: Null): MaybeChanged[Null] = MaybeChanged.Unchanged(null)
  


  

