package sturdy.language.wasm.analyses

import apron.*
import sturdy.apron.{*, given}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.analyses.RelationalAnalysis.*
import sturdy.language.wasm.generic.{ExceptionInstance, FunctionInstance, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.{*, given}
import sturdy.effect.symboltable.{*, given}
import sturdy.util.Profiler
import sturdy.{*, given}

object RelationalAnalysisSoundness {
  given defaultResolveState: ResolveState = ResolveState.Internal

  given valuesSound(using apronState: ApronState[VirtAddr, Type]): Soundness[ConcreteInterpreter.Value, RelationalAnalysis.Value] with
    override def isSound(c: ConcreteInterpreter.Value, a: RelationalAnalysis.Value): IsSound = Profiler.disableMeasurement {
      (c, a) match
        case (ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(ci32)), RelationalAnalysis.Value.Num(RelationalAnalysis.NumValue.Int32(vi32))) => Soundness.isSound(ci32, vi32.asNumExpr)
        case (ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(ci64)), RelationalAnalysis.Value.Num(RelationalAnalysis.NumValue.Int64(vi64))) => Soundness.isSound(ci64, vi64)
        case (ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(cf32)), RelationalAnalysis.Value.Num(RelationalAnalysis.NumValue.Float32(vf32))) => Soundness.isSound(cf32, vf32)
        case (ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(cf64)), RelationalAnalysis.Value.Num(RelationalAnalysis.NumValue.Float64(vf64))) => Soundness.isSound(cf64, vf64)
        case (ConcreteInterpreter.Value.Ref(cr), RelationalAnalysis.Value.Ref(ar)) => Soundness.isSound(cr, ar)
        case (ConcreteInterpreter.Value.Vec(cv), RelationalAnalysis.Value.Vec(av)) => Soundness.isSound(cv, av)
        case (_, RelationalAnalysis.Value.TopValue) => IsSound.Sound
        case (_, _) => IsSound.NotSound(s"abstract value $a with interval does not overapproximate concrete value $c")
    }

  given referencesSound: Soundness[ConcreteInterpreter.Reference, RelationalAnalysis.Reference] with
    override def isSound(c: FunctionInstance | ConcreteInterpreter.ExternReference | ExceptionInstance[ConcreteInterpreter.Value], a: Powerset[FunctionInstance | RelationalAnalysis.ExternReference]): IsSound = Profiler.disableMeasurement {
      c match
        case _: ExceptionInstance[?] => IsSound.Sound // exnref has no abstract counterpart yet
        case other: (FunctionInstance | ConcreteInterpreter.ExternReference) =>
          if (a.set.contains(toRelationalAnalysisExternRef(other)))
            IsSound.Sound
          else
            IsSound.NotSound(s"Abstract reference $a does not contain instance $c")
    }

    private def toRelationalAnalysisExternRef(c: FunctionInstance | ConcreteInterpreter.ExternReference): FunctionInstance | RelationalAnalysis.ExternReference =
      c match
        case ConcreteInterpreter.ExternReference.ExternReference => RelationalAnalysis.ExternReference.ExternReference
        case ConcreteInterpreter.ExternReference.Null => RelationalAnalysis.ExternReference.Null
        case f: FunctionInstance => f

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
    def isSound(c: ConcreteInterpreter.Instance, a: RelationalAnalysis.Instance): IsSound = Profiler.disableMeasurement {
      import a.given
      import a.tables.given

      // soundness for stack, memory, symbol tables, call frame
      a.stack.operandStackIsSound(c.stack) &&
        // a.memory.memoryIsSound(c.memory) &&   (Top-Memory is trivially sound)
        a.globals.tableIsSound(c.globals) // &&
      // a.tables.tableIsSound(c.tables) &&
      // a.callFrame.isSound(c.callFrame)
    }
}
