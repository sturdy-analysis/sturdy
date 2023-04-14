package sturdy.language.wasm.analyses

import sturdy.{*,given}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.generic.{FunctionInstance, given}
import TypeAnalysis.*
import sturdy.values.given
import sturdy.values.Abstractly
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*,given}

object TypeAnalysisSoundness {

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(topI32)
      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(topI64)
      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(topF32)
      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(topF64)

  given po: PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x,y) match
      case (_, Value.TopValue) => true
      case (ty1, ty2) => ty1 == ty2

  given [C,A](using aValue: Abstractly[C,A]): Abstractly[List[C], List[A]] with
    override def apply(c: List[C]): List[A] =
      c.map(aValue.apply(_))

  given Soundness[ConcreteInterpreter.Instance, TypeAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: TypeAnalysis.Instance): IsSound =
      // soundness for stack, memory, symbol table, call frame
      a.stack.operandStackIsSound(c.stack) &&
//        a.memory.memoryIsSound(c.memory) &&
        a.globals.tableIsSound(c.globals) &&
        a.funTable.tableIsSound(c.funTable) &&
        a.callFrame.isSound(c.callFrame)
}
