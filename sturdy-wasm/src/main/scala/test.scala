import llvm.*
import apron.Interval

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

@main
def main(): Unit = {
  val ABSOLUTEFILEPATH = "/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src/fankuchredux.wasm"
  val RELATIVEFILEPATH = "/src/test/resources/wasm/benchmarksgame/src/fankuchredux.wasm"

  val dwarfContext = new DWARFContext(ABSOLUTEFILEPATH)
  val dwarfUnits = dwarfContext.CompileUnits().asScala.toList
  val unitGlobals = dwarfUnits.map {
    findAllGlobals
  }
}

type Variable = (String, Interval)
def findAllGlobals(unit: DWARFUnit): Vector[Variable] = {
  val root = unit.getUnitDIE

  if !root.hasChildren then return Vector.empty

  val toplevelDIEs = root.children().asScala.toList
  val toplevelVariableDIEs = toplevelDIEs.filter(_.hasTag(DwarfTag.variable))

  toplevelVariableDIEs.map(getNameAndTypeSize)

  Vector.empty
}
def getNameAndTypeSize(die: DWARFDie): Variable = {
  assert(die.hasTag(DwarfTag.variable))

  val name = die.getNameAttr.toScala match {
    case Some(str) => str
    case None => "<unknown variable name>"
  }
  println(name)
  val typeDie = die.getTypeAttr.toScala match {
    case Some(value) => value
    case None => sys.error("encountered variable DIE without a type attribute")
  }

  null
}
def getTypeSize(die: DWARFDie): Int = {
  assert(die.getTag.isTypeTag) //only consider TypeTags:
  die.getTag match {
    case DwarfTag.base_type => die.getByteSizeAttr.toScala match {
      case Some(value) => value.toInt
      case None => sys.error("base_type Die did not have byte_size Attribute")
    }
    case DwarfTag.pointer_type => 8 //Pointer Size in Bytes
    case DwarfTag.array_type => ???
    case DwarfTag.subrange_type => ???
    case DwarfTag.const_type => ???
    case DwarfTag.volatile_type => ???
    case DwarfTag.restrict_type => ???
    case DwarfTag.typedef => ???
    case DwarfTag.subroutine_type => ???
    case DwarfTag.structure_type => ???
    case DwarfTag.union_type => ???
    case DwarfTag.enumeration_type => ???
    case DwarfTag.ptr_to_member_type => ???
    case DwarfTag.unspecified_type => ???
    case _ => sys.error("expected a die containing type information but got '" + die.getTag + "' instead.")
  }
}

def testDie(die: DWARFDie): Unit = {

}

def testUnit(unit: DWARFUnit): Unit = {
  if !unit.getUnitDIE.hasChildren then println("no children on this die.")

  println("Num Children: " + unit.getUnitDIE.children())
}

def testContext(ctx: DWARFContext): Unit = {
  val dwarfUnits = ctx.CompileUnits().asScala.toList
  dwarfUnits.foreach { unit =>
    testUnit(unit)
  }
}
