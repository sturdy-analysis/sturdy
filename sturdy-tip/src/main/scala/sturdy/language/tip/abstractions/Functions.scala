package sturdy.language.tip.abstractions

import sturdy.ir.IR
import sturdy.language.tip.Interpreter
import sturdy.language.tip.Function
import sturdy.util.Lazy
import sturdy.values.functions.FunctionOps
import sturdy.values.{Combine, MaybeChanged, Widening, Powerset as PSet}

object Functions:
  trait Powerset extends Interpreter:
    final type VFun = PSet[Function]

    final def topFun(using self: Instance): VFun = PSet(self.getFunctions.toSet)

  trait IRFun extends Interpreter:
    override final type VFun = IR
    override def topFun(using Instance): IR = IR.Unknown()

  trait Types extends Interpreter:
    case class VFun(from: Value, to: Value)
    override def topFun(using Instance): VFun = VFun(Value.TopValue, Value.TopValue)
    
    given CombineFunType[W <: Widening](using c: Lazy[Combine[Value, W]]): Combine[VFun, W] with
      override def apply(v1: VFun, v2: VFun): MaybeChanged[VFun] =
        val MaybeChanged(from, c1) = c.force(v1.from, v2.from)
        val MaybeChanged(to, c2) = c.force(v1.to, v2.to)
        MaybeChanged(VFun(from, to), c1 || c2)
    
    given FunctionOps[Function, Seq[Value], Value, VFun] with
      override def funValue(fun: Function): VFun = VFun(Value.TopValue, Value.TopValue)
      override def invokeFun(fun: VFun, a: Seq[Value])(invoke: (Function, Seq[Value]) => Value): Value =
        ???
      
    