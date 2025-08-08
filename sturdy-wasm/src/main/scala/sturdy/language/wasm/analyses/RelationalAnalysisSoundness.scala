package sturdy.language.wasm.analyses

import apron.*
import sturdy.apron.{*, given}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.analyses.RelationalAnalysis.*
import sturdy.language.wasm.generic.{FunctionInstance, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.{*, given}
import sturdy.{*, given}

object RelationalAnalysisSoundness {
//  given partialOrderValue(using apronState: ApronState[VirtAddr, Type]): PartialOrder[Value] with
//    override def lteq(x: Value, y: Value): Boolean = (x,y) match
//      case (_, Value.TopValue) => true
//      case (Value.Int32(i1), Value.Int32(i2)) => PartialOrder[Interval].lteq(apronState.getInterval(i1.asApronExpr), apronState.getInterval(i2.asApronExpr))
//      case (Value.Int64(l1), Value.Int64(l2)) => PartialOrder[Interval].lteq(apronState.getInterval(l1),apronState.getInterval(l2))
//      case (Value.Float32(f1), Value.Float32(f2)) => PartialOrder[Interval].lteq(apronState.getInterval(f1),apronState.getInterval(f2))
//      case (Value.Float64(d1), Value.Float64(d2)) => PartialOrder[Interval].lteq(apronState.getInterval(d1),apronState.getInterval(d2))
//      case _ => false
//
//
//  given [C,A](using aValue: Abstractly[C,A]): Abstractly[List[C], List[A]] with
//    override def apply(c: List[C]): List[A] =
//      c.map(aValue.apply)
//
//  given [A](using poValue: PartialOrder[A]): PartialOrder[List[A]] with
//    override def lteq(x: List[A], y: List[A]): Boolean =
//      if (x.length != y.length)
//        false
//      else
//        x.zip(y).forall((a,b) => poValue.lteq(a,b))

  given valuesSound(using apronState: ApronState[VirtAddr, Type]): Soundness[ConcreteInterpreter.Value, RelationalAnalysis.Value] with
    override def isSound(c: ConcreteInterpreter.Value, a: RelationalAnalysis.Value): IsSound =
      (c,a) match
        case (ConcreteInterpreter.Value.Int32(ci32), RelationalAnalysis.Value.Int32(vi32)) => Soundness.isSound(ci32, vi32.asApronExpr)
        case (ConcreteInterpreter.Value.Int64(ci64), RelationalAnalysis.Value.Int64(vi64)) => Soundness.isSound(ci64, vi64)
        case (ConcreteInterpreter.Value.Float32(cf32), RelationalAnalysis.Value.Float32(vf32)) => Soundness.isSound(cf32, vf32)
        case (ConcreteInterpreter.Value.Float64(cf64), RelationalAnalysis.Value.Float64(vf64)) => Soundness.isSound(cf64, vf64)
        case (_, RelationalAnalysis.Value.TopValue) => IsSound.Sound
        case (_, _) => IsSound.NotSound(s"abstract value $a with interval does not overapproximate concrete value $c")

  given listSound[C,A](using soundElem: Soundness[C,A]): Soundness[List[C], List[A]] with
    override def isSound(c: List[C], a: List[A]): IsSound =
      if(c.length != a.length)
        IsSound.NotSound(s"Abstract list $a does not overapproximate list $c, lists have different lengths")
      else if(c.zip(a).forall(soundElem.isSound(_,_) == IsSound.Sound))
          IsSound.Sound
      else
        val (ce,ae) = c.zip(a).find(soundElem.isSound(_,_) != IsSound.Sound).get
        IsSound.NotSound(s"Abstract list $a does not overapproximate list $c: ${soundElem.isSound(ce,ae)}")

  given Soundness[ConcreteInterpreter.FunV, FunV] with
    override def isSound(cFun: ConcreteInterpreter.FunV, aFun: FunV): IsSound =
      powersetContainsOneSound.isSound(cFun, aFun)

  given Soundness[ConcreteInterpreter.Instance, RelationalAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: RelationalAnalysis.Instance): IsSound =
      import a.given

      // soundness for stack, memory, symbol tables, call frame
      a.stack.operandStackIsSound(c.stack) &&
        // a.memory.memoryIsSound(c.memory) &&   (Top-Memory is trivially sound)
        a.globals.tableIsSound(c.globals) &&
        a.funTable.tableIsSound(c.funTable) &&
        a.callFrame.isSound(c.callFrame)
}
