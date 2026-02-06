package sturdy.language.wasm.abstractions

import apron.Interval
import sturdy.language.wasm.generic
import swam.binary.custom.dwarf.{CType, FunctionConcept, Subprogram}

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
                             //...
                               tableRange: Interval,
                             //...
                               dataRange: Interval, //replace with vector strings
                             //where global variables are
                               globalRanges: Vector[(String, Interval, CType)],
                             //where the stack is (function frames)
                               stackRange: Interval, //Vector[(Interval, Subprogram)],
                             //address of the stackpointer.
                               stackPointer: generic.GlobalAddr, //maybe find the stackpointer address somewhere
                             //where heap allocated objects are
                               heapRange: Interval, 
                             //also do strings (address should be lower than globalbase and higher than DSO_handle
                             //TODO add functions to StaticMemoryLayout 
                             functions: Vector[FunctionConcept]
                             ):
  def getGlobalRange(name: String): Option[Interval] = 
    globalRanges.find((global, interval, cType) => name == global).map(_._2)
    
  def getGlobalCType(name: String): Option[CType] = 
    globalRanges.find((global, interval, cType) => name == global).map(_._3)
