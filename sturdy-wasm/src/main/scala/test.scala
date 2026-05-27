import swam.binary.custom.dwarf.{DwarfLogging, DwarfTreeBuilder}
import swam.binary.custom.dwarf.llvm.{DWARFContext, DWARFDie, DWARFUnit, DwarfTag}

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional


def tryPrintDieName(die: DWARFDie): Unit = {
  die.getNameAttr.toScala match {
    case Some(value) => println(value)
    case None => println(s"<unknown die name at 0x${die.getOffset.toHexString}>")
  }
}

@main
def main(): Unit = {
  val ABSOLUTEFILEPATH = "/home/flo/programming/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src/test-arrays.wasm"

  val dwarfContext = new DWARFContext(ABSOLUTEFILEPATH)
  dwarfContext.devTest()
  return
}