package sturdy.language.wasm.abstractions

import apron.*
import sturdy.apron.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.language.wasm.Interpreter
import sturdy.data.{*, given}
import sturdy.util.Lazy
import sturdy.values.config.{BitSign, BytesSize, Overflow}
import sturdy.values.convert.{&&, Convert, ConvertConfig, LiftedConvert, NilCC, SomeCC}
import sturdy.values.floating.{ConvertDoubleInt, ConvertFloatInt}
import sturdy.values.integer.{IntegerOpsWithSignInterpretation, RelationalBaseIntegerOps, *, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}

import java.nio.ByteOrder

trait RelationalI32Values extends Interpreter with RelationalAddresses:
  import Type.*
  import Value.*
  import NumValue.*

  enum RelI32:
    case NumExpr(expr: ApronExpr[VirtAddr, Type])
    case BoolExpr(expr: Bool)
    case AllocationSites(sites: AbstractReference[PowVirtAddr], size: ApronExpr[VirtAddr, Type])
  import RelI32.*

  final type I32 = RelI32
  final type Bool = ApronBool[VirtAddr, Type]

  extension (i32: I32)
    inline def asNumExpr(using apronState: ApronState[VirtAddr, Type], resolveState: ResolveState): ApronExpr[VirtAddr, Type] =
      i32 match
        case NumExpr(expr) => expr
        case BoolExpr(condition) => apronState.assert(condition)(using resolveState) match
          case Topped.Actual(true) => ApronExpr.lit(1, I32Type)
          case Topped.Actual(false) => ApronExpr.lit(0, I32Type)
          case Topped.Top => ApronExpr.interval(0, 1, I32Type)
        case AllocationSites(sites, _) => ApronExpr.top(I32Type)

    def asNumExprLazy(using lazyApronState: Lazy[ApronState[VirtAddr, Type]], resolveState: ResolveState): ApronExpr[VirtAddr, Type] =
      given ApronState[VirtAddr, Type] = lazyApronState.value
      i32.asNumExpr(using resolveState = resolveState)

  final override def topI32: I32 = NumExpr(ApronExpr.constant(ApronExpr.topInterval, I32Type))
  final override def booleanToVal(b: Bool): Value = Num(Int32(BoolExpr(b)))

  given CombineI32[W <: Widening](using combineApronExpr: Combine[ApronExpr[VirtAddr,Type], W], apronStateLazy: Lazy[ApronRecencyState[AddrCtx,Type,Value]]): Combine[I32, W] with
    override def apply(v1: I32, v2: I32): MaybeChanged[I32] =
      (v1, v2) match
        case _ if(v1 eq v2) => Unchanged(v1)
        case (NumExpr(expr), _) if expr.isBottom == Topped.Actual(true) => Changed(v2)
        case (_, NumExpr(expr)) if expr.isBottom == Topped.Actual(true) => Unchanged(v1)
        case (BoolExpr(b1), BoolExpr(b2)) if (b1 == b2) => Unchanged(BoolExpr(b1))
        case (alloc1:AllocationSites, alloc2:AllocationSites) => CombineAllocationSites(alloc1,alloc2)
        case (NumExpr(expr), alloc2@AllocationSites(sites, size)) =>
          given apronState: ApronRecencyState[AddrCtx,Type,Value] = apronStateLazy.value
          if(apronState.getInterval(expr)(using ResolveState.Left).isZero)
            Changed(AllocationSites(Join(sites, AbstractReference.Null).get, size))
          else
            expr match
              case ApronExpr.Addr(x,_,_) =>
                apronState.getValue(x)(using ResolveState.Left).toOption match
                  case Some(Value.Num(Int32(alloc1: AllocationSites))) => CombineAllocationSites(alloc1, alloc2)
                  case _ => combineApronExpr(v1.asNumExpr(using resolveState = ResolveState.Left), v2.asNumExpr(using resolveState = ResolveState.Right)).map(NumExpr.apply)
        case (alloc1@AllocationSites(sites, size), NumExpr(expr)) =>
          given apronState: ApronRecencyState[AddrCtx,Type,Value] = apronStateLazy.value
          if(apronState.getInterval(expr)(using ResolveState.Right).isZero)
            MaybeChanged(AllocationSites(Join(sites, AbstractReference.Null).get, size), !sites.containsNull)
          else
            expr match
              case ApronExpr.Addr(x,_,_) =>
                apronState.getValue(x)(using ResolveState.Right).toOption match
                  case Some(Value.Num(Int32(alloc2: AllocationSites))) => CombineAllocationSites(alloc1, alloc2)
                  case _ => combineApronExpr(v1.asNumExpr(using resolveState = ResolveState.Left), v2.asNumExpr(using resolveState = ResolveState.Right)).map(NumExpr.apply)
        case (_,_) =>
          given ApronState[VirtAddr,Type] = apronStateLazy.value
          combineApronExpr(v1.asNumExpr(using resolveState = ResolveState.Left), v2.asNumExpr(using resolveState = ResolveState.Right)).map(NumExpr.apply)

  given CombineAllocationSites[W <: Widening](using combineApronExpr: Combine[ApronExpr[VirtAddr,Type], W]): Combine[AllocationSites, W] with
    override def apply(v1: AllocationSites, v2: AllocationSites): MaybeChanged[AllocationSites] =
      for {
        sites <- Combine(v1.sites, v2.sites);
        size <- combineApronExpr(v1.size, v2.size)
      } yield (AllocationSites(sites, size))

  given overflowHandling: OverflowHandling = OverflowHandling.WrapAround

  given I32IntegerOps(using intOps: IntegerOps[Int, ApronExpr[VirtAddr, Type]], apronState: ApronState[VirtAddr, Type], failure: Failure, effectStack: EffectStack): IntegerOpsWithSignInterpretation[Int, I32] =
    new LiftedIntegerOpsWithSignInterpretation[Int, I32, ApronExpr[VirtAddr,Type]](extract = _.asNumExpr, inject = NumExpr(_)):
      override def bitAnd(v1: I32, v2: I32): I32 =
        (v1, v2) match
          case (BoolExpr(e1), BoolExpr(e2)) => BoolExpr(ApronBool.And(e1, e2))
          case _ => NumExpr(intOps.bitAnd(v1.asNumExpr, v2.asNumExpr))
      override def bitOr(v1: I32, v2: I32): I32 =
        (v1, v2) match
          case (BoolExpr(e1), BoolExpr(e2)) => BoolExpr(ApronBool.Or(e1, e2))
          case _ => NumExpr(intOps.bitOr(v1.asNumExpr, v2.asNumExpr))

      override def bitXor(v1: I32, v2: I32): I32 =
        (v1, v2) match
          case (BoolExpr(e1), NumExpr(e2)) =>
            val iv = apronState.getInterval(e2)
            if(iv.isEqual(1)) {
              // b xor 1 == !b
              BoolExpr(e1.negated)
            } else {
              bitXor(NumExpr(v1.asNumExpr), v2)
            }
          case (NumExpr(e1), BoolExpr(e2)) => bitXor(v2, v1)
          case _ => NumExpr(intOps.bitXor(v1.asNumExpr, v2.asNumExpr))

  given I32EqOps(using eqOps: EqOps[ApronExpr[VirtAddr,Type], Bool], apronState: ApronState[VirtAddr, Type], failure: Failure, effectStack: EffectStack): EqOps[I32, Bool] with
    override def equ(v1: I32, v2: I32): Bool =
      (v1, v2) match
        case (AllocationSites(sites, _), NumExpr(expr)) =>
          if(apronState.getInterval(expr).isZero) {
            sites match {
              case _: AbstractReference.Null.type => ApronBool.Constant(Topped.Actual(true))
              case _: AbstractReference.Addr[?] => ApronBool.Constant(Topped.Actual(false))
              case _: AbstractReference.NullAddr[?] => ApronBool.Constant(Topped.Top)
            }
          } else
            equ(NumExpr(v1.asNumExpr), v2)
        case (_: NumExpr, _: AllocationSites) =>
          equ(v2, v1)
        case (BoolExpr(c1), NumExpr(i2@ApronExpr.Constant(coeff, _, tpe))) =>
          val c1ContainsNaN = c1.constraint.exists { case ApronCons(_, e1, e2) => e1.floatSpecials.nan || e2.floatSpecials.nan }
          if (coeff.isEqual(0) && !c1ContainsNaN)
            c1.negated
          else if (coeff.isScalar && !c1ContainsNaN /* && ! coeff.isEqual(0) */ )
            c1
          else
            eqOps.equ(v1.asNumExpr, i2)
        case (NumExpr(_), BoolExpr(_)) =>
          equ(v2, v1)
        case (BoolExpr(c1), BoolExpr(c2)) =>
          println(s"Created boolean condition ($c1 iff $c2), which may blow up the size of the boolean exponentially")
          ApronBool.Or(ApronBool.And(c1, c2), ApronBool.And(c1.negated, c2.negated))
        case _ => eqOps.equ(v1.asNumExpr, v2.asNumExpr)

    override def neq(v1: I32, v2: I32): Bool = equ(v1, v2).negated

  given I32OrderingOps(using ApronState[VirtAddr, Type], Failure, EffectStack): OrderingOps[I32, Bool] =
    LiftedOrderingOps[I32, Bool, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](extract = _.asNumExpr, inject = ApronBool.Constraint(_))

  given I32UnsignedOrderingOps(using ApronState[VirtAddr, Type], Failure, EffectStack): UnsignedOrderingOps[I32, Bool] =
    LiftedUnsignedOrderingOps[I32, Bool, ApronExpr[VirtAddr, Type], ApronCons[VirtAddr, Type]](extract = _.asNumExpr, inject = ApronBool.Constraint(_))

  given I32ConvertIntLong(using ApronState[VirtAddr, Type], ConvertIntLong[ApronExpr[VirtAddr, Type], I64]): ConvertIntLong[I32, I64] =
    LiftedConvert[Int, Long, I32, I64, ApronExpr[VirtAddr, Type], I64, BitSign](extract = _.asNumExpr, inject = x => x)

  given I32ConvertIntFloat(using ApronState[VirtAddr, Type], ConvertIntFloat[ApronExpr[VirtAddr, Type], F32]): ConvertIntFloat[I32, F32] =
    LiftedConvert[Int, Float, I32, F32, ApronExpr[VirtAddr, Type], F32, BitSign](extract = _.asNumExpr, inject = x => x)

  given I32ConvertIntDouble(using ApronState[VirtAddr, Type], ConvertIntDouble[ApronExpr[VirtAddr, Type], F64]): ConvertIntDouble[I32, F64] =
    LiftedConvert[Int, Double, I32, F64, ApronExpr[VirtAddr, Type], F64, BitSign](extract = _.asNumExpr, inject = x => x)

  given I32ConvertLongInt(using ApronState[VirtAddr, Type], ConvertLongInt[I64, ApronExpr[VirtAddr, Type]]): ConvertLongInt[I64, I32] =
    LiftedConvert[Long, Int, I64, I32, I64, ApronExpr[VirtAddr, Type], NilCC.type](extract = x => x, inject = NumExpr(_))

  given I32ConvertFloatInt(using ApronState[VirtAddr, Type], ConvertFloatInt[F32, ApronExpr[VirtAddr, Type]]): ConvertFloatInt[F32, I32] =
    LiftedConvert[Float, Int, F32, I32, F32, ApronExpr[VirtAddr, Type], Overflow && BitSign](extract = x => x, inject = NumExpr(_))

  given I32ConvertDoubleInt(using ApronState[VirtAddr, Type], ConvertDoubleInt[F64, ApronExpr[VirtAddr, Type]]): ConvertDoubleInt[F64, I32] =
    LiftedConvert[Double, Int, F64, I32, F64, ApronExpr[VirtAddr, Type], Overflow && BitSign](extract = x => x, inject = NumExpr(_))

  private inline def toNonRelational(using apronState: ApronState[VirtAddr,Type])(v: I32): I32 =
    v match
      case NumExpr(e) => NumExpr(apronState.toNonRelational(e))
      case BoolExpr(e) => BoolExpr(apronState.toNonRelational(e))
      case _: AllocationSites => v

  final class NonRelationalI32IntegerOps(using relationalIntOps: IntegerOpsWithSignInterpretation[Int, I32], apronState: ApronState[VirtAddr, Type])
    extends LiftedIntegerOpsWithSignInterpretation[Int, I32, I32](extract = i32 => i32, inject = toNonRelational)

  final class NonRelationalI32Convert[From, V, Config <: ConvertConfig[_]](using relationalConvert: Convert[From, Int, V, I32, Config], apronState: ApronState[VirtAddr, Type])
    extends LiftedConvert[From, Int, V, I32, V, I32, Config](
      extract = expr => expr,
      inject = toNonRelational
    )
