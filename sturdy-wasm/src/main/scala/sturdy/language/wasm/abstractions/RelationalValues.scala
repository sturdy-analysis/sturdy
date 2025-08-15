package sturdy.language.wasm.abstractions

import sturdy.apron.{*, given}
import sturdy.apron.ApronExpr.*
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.effect.failure.{*, given}
import sturdy.values.{Abstractly, Topped}
import sturdy.values.floating.{FloatSpecials, FloatingLit}
import sturdy.util.{*, given}

trait RelationalValues extends RelationalI32Values:
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
    v match
      case Num(Int32(NumExpr(i))) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](i, lit(0, i._type)))
      case Num(Int32(BoolExpr(cons))) => cons
      case Num(Int64(l)) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](l, lit(0, l._type)))
      case Num(Float32(f)) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](f, lit(0, f._type)))
      case Num(Float64(d)) => ApronBool.Constraint(ApronCons.neq[VirtAddr, Type](d, lit(0, d._type)))
      case TopValue => ApronBool.Constraint(ApronCons.top(I32Type))

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(i)) => Num(Int32(NumExpr(lit(i, I32Type))))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(l)) => Num(Int64(lit(l, I64Type)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => Num(Float32(FloatingLit(f, F32Type)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => Num(Float64(FloatingLit(d, F64Type)))
