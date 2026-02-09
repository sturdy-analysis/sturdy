package sturdy.language.wasm.abstractions

import apron.Interval
import sturdy.language.wasm.generic
import sturdy.language.wasm.generic.FuncId
import swam.binary.custom.dwarf.llvm.DWARFLocationExpression
import swam.binary.custom.dwarf.{CType, DwarfOperationSequence, FunctionConcept, Subprogram}

//describes a stackframe of a Function
case class Frame(frame_base: DwarfOperationSequence, frame: Vector[(String, Interval, CType)])

/**
 *
 * @param tableRange
 * @param dataRange
 * @param globalRanges vector describing global variables
 * @param stackRange
 * @param stackPointer
 * @param heapRange
 * @param functionFrames vector describing functions
 */
case class StaticMemoryLayout(
                             //...
                               tableRange: Interval,
                             //...
                               dataRange: Interval, //replace with vector strings (maybe there are not always just strings?)
                             //where global variables are
                               globalRanges: Vector[(String, Interval, CType)],
                             //where the stack is (function frames)
                               stackRange: Interval, //Vector[(Interval, Subprogram)],
                             //address of the stackpointer.
                               stackPointer: generic.GlobalAddr, //maybe find the stackpointer address somewhere
                             //where heap allocated objects are
                               heapRange: Interval, 
                             //also do strings (address should be lower than globalbase and higher than DSO_handle
                               functionFrames: Map[FuncId, Frame]
                             ):
  def getGlobalRange(name: String): Option[Interval] = 
    globalRanges.find((global, interval, cType) => name == global).map(_._2)
    
  def getGlobalCType(name: String): Option[CType] = { 
    globalRanges.find((global, interval, cType) => name == global).map(_._3)
  }
