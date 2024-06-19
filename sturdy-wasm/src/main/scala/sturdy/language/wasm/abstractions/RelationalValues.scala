package sturdy.language.wasm.abstractions

import sturdy.language.wasm.generic.given
import sturdy.apron.ApronExpr.{intLit, topInterval}
import sturdy.apron.{*, given}
import sturdy.effect.failure.Failure
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.{FixIn, FixOut, FuncId, InstLoc, MemoryAddr}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.util.Lazy
import sturdy.values.{Combine, Finite, Join, MaybeChanged, Topped, Unchanged, Widen, Widening, given}
import sturdy.values.booleans.given
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{*, given}
import swam.OpCode
import swam.syntax
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import scala.collection.MapView

trait RelationalValues extends Interpreter:
  enum RelAddr:
    case CallFrameAddr(funcId: FuncId, addr: Int)
    case HeapAddr(memoryAddr: MemoryAddr, addr: Int)


  import RelAddr.*

  given Ordering[RelAddr] = {
    case (CallFrameAddr(f1,addr1), CallFrameAddr(f2, addr2)) =>
      Ordering[(FuncId,Int)].compare((f1,addr1),(f2,addr2))
    case (HeapAddr(m1,addr1), HeapAddr(m2,addr2)) =>
      Ordering[(MemoryAddr, Int)].compare((m1,addr1),(m2,addr2))
  }

  type VAddr = VirtualAddress[RelAddr]

  given Ordering[VAddr] = VirtualAddressOrdering

  enum Type:
    case I32Type
    case I64Type
    case F32Type
    case F64Type

    override def toString: String =
      this match
        case I32Type => "i32"
        case I64Type => "i64"
        case F32Type => "f32"
        case F64Type => "f64"

  import Type.*

  given CombineType[W <: Widening]: Combine[Type, W] = {
    case (I32Type, I32Type) => Unchanged(I32Type)
    case (I64Type, I64Type) => Unchanged(I64Type)
    case (F32Type, F32Type) => Unchanged(F32Type)
    case (F64Type, F64Type) => Unchanged(F64Type)
    case (t1,      t2)      => throw new IllegalArgumentException(s"Incompatible types $t1 and $t2")
  }

  given ApronType[Type] with
    extension(t: Type)
      def apronRepresentation: ApronRepresentation =
        t match
          case I32Type => ApronRepresentation.Int
          case I64Type => ApronRepresentation.Int
          case F32Type => ApronRepresentation.Real
          case F64Type => ApronRepresentation.Real
      def roundingDir: RoundingDir =
        t match
          case I32Type => RoundingDir.Zero
          case I64Type => RoundingDir.Zero
          case F32Type => RoundingDir.Rnd
          case F64Type => RoundingDir.Rnd
      def roundingType: RoundingType =
        t match
          case I32Type => RoundingType.Int
          case I64Type => RoundingType.Int
          case F32Type => RoundingType.Single
          case F64Type => RoundingType.Double
      def byteSize: Int =
        t match
          case I32Type => 4
          case I64Type => 8
          case F32Type => 4
          case F64Type => 8

  given IntegerOps[Int, Type] = new TypeIntegerOps[Int,Type](I32Type, msg => throw new IllegalArgumentException(msg)) {}
  given IntegerOps[Long, Type] = new TypeIntegerOps[Long,Type](I64Type, msg => throw new IllegalArgumentException(msg)) {}
  given FloatOps[Float, Type] = new TypeFloatOps[Float,Type](F32Type, msg => throw new IllegalArgumentException(msg)) {}
  given FloatOps[Double, Type] = new TypeFloatOps[Double,Type](F64Type, msg => throw new IllegalArgumentException(msg)) {}

  final type I32 = ApronExpr[VAddr, Type]
  final type I64 = ApronExpr[VAddr, Type]
  final type F32 = ApronExpr[VAddr, Type]
  final type F64 = ApronExpr[VAddr, Type]
  final type Bool = ApronCons[VAddr, Type]

  def apronState: ApronState[VAddr, Type]

  given Lazy[ApronState[VAddr, Type]] = Lazy(apronState)

  given Join[ApronExpr[VAddr, Type]] = JoinApronExpr[VAddr, Type]

  given Widen[ApronExpr[VAddr, Type]] = WidenApronExpr[VAddr, Type]


  import Value.*
  final def topI32: I32 = ApronExpr.top(I32Type)
  final def topI64: I64 = ApronExpr.top(I64Type)
  final def topF32: F32 = ApronExpr.top(F32Type)
  final def topF64: F64 = ApronExpr.top(F64Type)

  final def asBoolean(v: Value): Bool =
    v match
      case Int32(i) => ApronCons.neq[VAddr,Type](i, intLit(0, i._type))
      case Int64(l) => ApronCons.neq[VAddr,Type](l, intLit(0, l._type))
      case Float32(f) => ApronCons.neq[VAddr,Type](f, intLit(0, f._type))
      case Float64(d) => ApronCons.neq[VAddr,Type](d, intLit(0, d._type))
      case TopValue => ApronCons.top(I32Type)

  final def boolean(b: Bool): Value = ???