package sturdy.language.wasm.analyses

import sturdy.{*,given}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.generic.{FunctionInstance, given}
import ConstantAnalysis.*
import sturdy.values.powersetContainsOneSound
import sturdy.values.Abstractly
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*,given}
import sturdy.values.given

object ConstantAnalysisSoundness {

  given [C,A](using aValue: Abstractly[C,A]): Abstractly[List[C], List[A]] with
    override def apply(c: List[C]): List[A] =
      c.map(aValue.apply)

  given (using bs: Soundness[Byte,Byte]): Soundness[Byte, Topped[Byte]] with
    def isSound(cByte: Byte, aByte: Topped[Byte]): IsSound = aByte match
      case Topped.Top => IsSound.Sound
      case Topped.Actual(b) => bs.isSound(cByte,b)
      
  given Soundness[ConcreteInterpreter.FunV, FunV] with
    override def isSound(cFun: ConcreteInterpreter.FunV, aFun: FunV): IsSound =
      powersetContainsOneSound.isSound(cFun, aFun)

  given Soundness[ConcreteInterpreter.Instance, ConstantAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: ConstantAnalysis.Instance): IsSound =
      // soundness for stack, memory, symbol table, call frame
      a.stack.operandStackIsSound(c.stack) &&
        a.memory.memoryIsSound(c.memory) &&
        a.globals.tableIsSound(c.globals) &&
        a.funTable.tableIsSound(c.funTable) &&
        a.callFrame.isSound(c.callFrame)
}
