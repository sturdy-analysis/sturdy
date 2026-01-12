package sturdy.language.wasm.abstractions

import apron.Interval
import sturdy.language.wasm.generic
import swam.binary.custom.dwarf.{CType, Subprogram}

/**
 *
 * @param tableRange
 * @param dataRange
 * @param globalRanges
 * @param stackRange
 * @param stackPointer
 * @param heapRange
 */
case class StaticMemoryLayout(
                               tableRange: Interval,
                               dataRange: Interval, //replace with vector strings
                               globalRanges: Vector[(String, Interval/*, CType*/)],
                               stackRange: Interval, //Vector[(Interval, Subprogram)],
                               stackPointer: generic.GlobalAddr, //maybe find the stackpointer address somewhere
                               heapRange: Interval //TODO: use sturdy NumericInterval
                               //do globals first
                               //also do strings (address should be lower than globalbase and higher than DSO_handle
                               //TODO add functions to staticmemorylayout
                             ):
  def getGlobalRange(name: String): Option[Interval] = globalRanges.find((global, interval/*, cType*/) => name == global).map(_._2)
