package sturdy.language.pcf.abstractions

import sturdy.effect.failure.Failure
import sturdy.values.relational.EqOps
import sturdy.values.{Join, Topped}
import sturdy.values.integer.{IntSign, given}
import sturdy.language.pcf.Interpreter

object Ints:
  trait Sign extends Interpreter :
    final type VBoolean = Topped[Boolean]
    final type VInt = IntSign

    import Topped._
    override final def asBoolean(v: Value)(using Failure): VBoolean =
      v match
        case Value.Int(i) => i match
          case IntSign.TopSign => Topped.Top
          case IntSign.Neg => Topped.Actual(false)
          case IntSign.NegOrZero => Topped.Actual(false)
          case IntSign.Zero => Topped.Actual(false)
          case IntSign.ZeroOrPos => Topped.Actual(false)
          case IntSign.Pos => Topped.Actual(true)
        case Value.Closure(_) => Topped.Top
        case Value.TopValue => Topped.Top

    override final def boolean(b: VBoolean): Value =
      b match
        case Topped.Top => Value.TopValue
        case Topped.Actual(b) => Value.Int(if b then IntSign.Pos else IntSign.Neg)

