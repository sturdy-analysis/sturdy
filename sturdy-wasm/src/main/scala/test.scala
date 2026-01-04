import swam.binary.custom.dwarf.llvm.{DWARFContext, DWARFDie, DWARFUnit, DwarfTag}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional


def tryPrintDieName(die: DWARFDie): Unit = {
  die.getNameAttr.toScala match {
    case Some(value) => println(value)
    case None => println("<unknown name>")
  }
}

@main
def main(): Unit = {
  val ABSOLUTEFILEPATH = "/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src/fankuchredux.wasm"
  
  val dwarfContext = new DWARFContext(ABSOLUTEFILEPATH)
  //dwarfContext.devTest()
  val dwarfUnits = dwarfContext.CompileUnits().asScala.toList
  println(makeAST(dwarfUnits.head))
  val unitGlobals = dwarfUnits.map {
    findAllGlobals
  }
  unitGlobals.foreach { thisCUList =>
    println("====NEW CU====")
    println("with " + thisCUList.size + " global variables")
    thisCUList.foreach { case (str, size) =>
      println(s"$str has size: $size")
    }
  }
}

def findAllGlobals(unit: DWARFUnit): List[(String, (Int, Int))] = {
  val root = unit.getUnitDIE

  if !root.hasChildren then return Nil

  val toplevelDIEs = root.children().asScala.toList
  val toplevelVariableDIEs = toplevelDIEs.filter(_.hasTag(DwarfTag.variable))

  toplevelVariableDIEs.map(getNameAndTypeSize)
}
def getNameAndTypeSize(die: DWARFDie): (String, (Int, Int)) = {
  assert(die.hasTag(DwarfTag.variable))
  //println("Die has Tag: " + die.getTag)
  val name = die.getNameAttr.toScala match {
    case Some(str) => str
    case None => "<unknown variable name>"
    //TODO: current understanding: some values do not get a name since they are never assigned to a variable with a name
    // examples encountered: printf() calls containing directly written const char* strings
  }
  //println("Die has offset: " + die.getOffset.toHexString)
  //println("Die has Name: " + name)
  die.devTest()
  val typeDie = die.getTypeAttr.toScala match {
    case Some(value) => value
    case None => sys.error("encountered variable DIE without a type attribute")
  }
  var baseLocation = 0
  //baseLocation = typeDie.getLocationAttr.toScala match
  //  case Some(value) => value.toInt
  //  case None => sys.error("encountered top level variable DIE without a location attribute")
  val typeSize = getTypeSize(typeDie)
  //println(typeSize)

  (name, (baseLocation, baseLocation + typeSize))
}
def getTypeSize(die: DWARFDie): Int = {
  assert(die.getTag.isTypeTag) //only consider TypeTags:
  die.getTag match {
    case DwarfTag.base_type => die.getByteSizeAttr.toScala match
      case Some(value) =>
        println(die.getEncodingAttr.toScala)
        value.toInt
      case None => sys.error("base_type die did not have byte_size attribute")

    case DwarfTag.pointer_type => die.getAddrSize //for webassembly this should always be 4

    case DwarfTag.array_type => die.getTypeAttr.toScala match
      case None => sys.error("array_type did not have type attribute")
      case Some(typeDie) =>
        die.children().asScala.toList match
        case Nil => sys.error("array_type has unknown size")
        //multidimensional array types are allowed to have multiple child Dies (one for each dimension)
        case subrangeDies@head::tail => subrangeDies.foldRight(1)((subrangeDie, acc) => getTypeSize(subrangeDie) * acc)

    case DwarfTag.subrange_type => die.getCountAttr.toScala match
      case Some(value) => value.toInt
      case None => sys.error("subrange_type did not have count attribute")

    case DwarfTag.const_type => die.getTypeAttr.toScala match
      case Some(typeDie) => getTypeSize(typeDie)
      case None => sys.error("const_type did not have type attribute")

    case DwarfTag.volatile_type => die.getTypeAttr.toScala match
      case Some(typeDie) => getTypeSize(typeDie)
      case None => sys.error("volatile_type did not have type attribute")

    case DwarfTag.restrict_type => die.getTypeAttr.toScala match
      case Some(typeDie) => getTypeSize(typeDie)
      case None => sys.error("restrict_type did not have type attribute")

    case DwarfTag.typedef => die.getTypeAttr.toScala match
      case Some(typeDie) => getTypeSize(typeDie)
      case None => sys.error("typedef did not have type attribute")

    case DwarfTag.structure_type => die.getByteSizeAttr.toScala match
      case Some(value) => value.toInt
      case None => sys.error("structure_type did not have byte_size attribute")

    case DwarfTag.union_type => die.getByteSizeAttr.toScala match
      case Some(value) => value.toInt
      case None => sys.error("union_type did not have byte_size attribute")


    case DwarfTag.enumeration_type => die.getByteSizeAttr.toScala match
      case Some(value) => value.toInt
      case None => sys.error("enumeration_type did not have byte_size attribute")

    case DwarfTag.ptr_to_member_type => sys.error("ptr_to_member_type is not expected when compiling C to WASM")


    case DwarfTag.unspecified_type => ???
    case DwarfTag.subroutine_type => ???
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
