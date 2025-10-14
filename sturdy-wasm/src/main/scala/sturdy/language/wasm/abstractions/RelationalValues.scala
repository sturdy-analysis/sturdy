package sturdy.language.wasm.abstractions

import sturdy.apron.{*, given}
import sturdy.apron.ApronExpr.*
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.effect.failure.{*, given}
import sturdy.language.wasm.generic.FunctionInstance
import sturdy.values.{Abstractly, Powerset, Topped}
import sturdy.values.floating.{FloatSpecials, FloatingLit}
import sturdy.util.{*, given}

trait RelationalValues extends RelationalI32Values with PowersetReference:
  override final type I64 = ApronExpr[VirtAddr, Type]
  override final type F32 = ApronExpr[VirtAddr, Type]
  override final type F64 = ApronExpr[VirtAddr, Type]
  override final type V128 = Topped[Array[Byte]]

  import Type.*
  import Value.*
  import NumValue.*
  import RelI32.*

  final override def topI64: I64 = constant(ApronExpr.topInterval, I64Type)
  final override def topF32: F32 = ApronExpr.floatConstant(ApronExpr.topInterval, FloatSpecials.Top, F32Type)
  final override def topF64: F64 = ApronExpr.floatConstant(ApronExpr.topInterval, FloatSpecials.Top, F64Type)

  final override def asBoolean(v: Value)(using failure: Failure): Bool =
    v.asInt32 match
      case NumExpr(i) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](i, lit(0, i._type)))
      case BoolExpr(cons) => cons
      case HeapAddr(_,_) => ApronBool.Constant(Topped.Top)

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(i)) => Num(Int32(NumExpr(lit(i, I32Type))))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(l)) => Num(Int64(lit(l, I64Type)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => Num(Float32(FloatingLit(f, F32Type)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => Num(Float64(FloatingLit(d, F64Type)))
      case ConcreteInterpreter.Value.Ref(func : FunctionInstance) => Ref(Powerset(func))
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.ExternReference.ExternReference) => Ref(Powerset(ExternReference.ExternReference))
      // case ConcreteInterpreter.Value.Ref(ExternReference.Null) => Ref(ExternReference.Null)
