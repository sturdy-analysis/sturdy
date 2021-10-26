package sturdy.language.wasm.analyses

import sturdy.{*, given}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.generic.{FunctionInstance, given}
import ConstantTaintAnalysis.*
import ConstantAnalysisSoundness.given
import sturdy.values.{*, given}
import sturdy.values.Abstractly
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.toppedPartialOrder
import sturdy.values.concretePO
import sturdy.values.ints.{*, given}
import sturdy.values.longs.{*, given}
import sturdy.values.floats.{*, given}
import sturdy.values.doubles.{*, given}
import sturdy.values.taint.TaintProduct

object ConstantTaintAnalysisSoundness {

  given valueIsSound(using constantSoundness: Soundness[ConcreteInterpreter.Value, ConstantAnalysis.Value]): Soundness[ConcreteInterpreter.Value, Value] with
    override def isSound(c: ConcreteInterpreter.Value, a: Value): IsSound =
      constantSoundness.isSound(c, untaint(a))

  given (using bs: Soundness[Byte,Byte]): Soundness[Byte, AByte] with
    def isSound(cByte: Byte, aByte: AByte): IsSound = aByte match
      case TaintProduct(_,Topped.Top) => IsSound.Sound
      case TaintProduct(_, Topped.Actual(b)) => bs.isSound(cByte,b)


  given Soundness[ConcreteInterpreter.Instance, ConstantTaintAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: ConstantTaintAnalysis.Instance): IsSound =
      // soundness for stack, memory, symbol table, call frame
      a.effects.operandStackIsSound(c.effects) &&
        a.effects.memoryIsSound(c.effects) &&
        a.effects.globalsIsSound(c.effects) &&
        a.effects.tableIsSound(c.effects) &&
        a.effects.joinedCallFrameNumberedIsSound(c.effects)
}
