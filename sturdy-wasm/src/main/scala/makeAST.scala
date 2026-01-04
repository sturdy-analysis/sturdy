import swam.binary.custom.dwarf.llvm.{DWARFUnit, DwarfEncoding, DwarfTag}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional


type llvmDWARFDie = swam.binary.custom.dwarf.llvm.DWARFDie
import swam.binary.custom.dwarf.* //{CType, DWARFDie, GlobalVariable, Subprogram}

case class AST(globals: Seq[GlobalVariable], functions: Seq[Subprogram])

def makeAST(DWARFUnit: DWARFUnit): AST = {
  var globals: Seq[GlobalVariable] = Nil
  var functions: Seq[Subprogram] = Nil

  DWARFUnit.getUnitDIE.children().forEach { die =>
    die.getTag match {
      case DwarfTag.variable =>
        println(die.getNameAttr.get())
        globals = globals.appended(
          GlobalVariable(
            die.getNameAttr.get(),
            makeTypeAST(die.getTypeAttr.get()),
            die.getLocationAttr.get(0)
          )
        )
      case DwarfTag.subprogram => println("subprogram currently ignored")
      case tag@_ => println("ignoring Die with tag: " + tag)
    }
  }
  AST(globals, functions)
}

def makeTypeAST(die: llvmDWARFDie): CType = {
  assert(die.getTag.isTypeTag)
  die.getTag match {
    case DwarfTag.base_type =>
      baseType(
        die.getNameAttr.get(),
        die.getByteSizeAttr.get().toInt,
        DwarfEncoding.fromValue(die.getEncodingAttr.get().toInt)
      )
    case DwarfTag.typedef =>
      typeDef(
        die.getNameAttr.get(),
        makeTypeAST(die.getTypeAttr.get())
      )
    case DwarfTag.array_type =>
      die.children().asScala.toList match {
        case head::Nil =>
          arrayType(
            makeTypeAST(die.getTypeAttr.get()),
            makeTypeAST(head.getTypeAttr.get()),
            Option(head.getCountAttr.get().toInt)
          )
        case head::tail => sys.error("multi-dimensional arrays are not supported yet")
        case Nil => sys.error("array_type without subrange_type")
      }
    case DwarfTag.structure_type =>
      structureType(
        die.getNameAttr.get(),
        die.getByteSizeAttr.get().toInt,
        die.children().asScala.toList.map { memberDie =>
          memberType(
            memberDie.getNameAttr.get(),
            memberDie.getDataMemberLocation.get().toInt,
            makeTypeAST(memberDie.getTypeAttr.get())
          )
        }
      )
    case DwarfTag.enumeration_type =>
      enumType(
        die.getNameAttr.get(),
        die.getByteSizeAttr.get().toInt,
        makeTypeAST(die.getTypeAttr.get()),
        die.children().asScala.toList.map { enumeratorDie =>
          (enumeratorDie.getNameAttr.get(), enumeratorDie.getConstValue.get().toInt)
        }
      )
    case DwarfTag.pointer_type =>
      pointerType(
        makeTypeAST(die.getTypeAttr.get())
      )
    case DwarfTag.const_type =>
      constType(
        makeTypeAST(die.getTypeAttr.get())
      )
    case DwarfTag.volatile_type =>
      volatileType(
        makeTypeAST(die.getTypeAttr.get())
      )
    case DwarfTag.atomic_type =>
      atomicType(
        makeTypeAST(die.getTypeAttr.get())
      )
    case _ => sys.error("type tag " + die.getTag + " is not supported.")
  }
}