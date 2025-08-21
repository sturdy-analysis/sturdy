package sturdy.language.wasm.abstractions

import apron.*
import sturdy.apron.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.language.wasm.Interpreter
import sturdy.data.{*,given}
import sturdy.util.Lazy
import sturdy.values.config.{BitSign, BytesSize, Overflow}
import sturdy.values.convert.{&&, Convert, LiftedConvert, NilCC, SomeCC}
import sturdy.values.floating.{ConvertDoubleInt, ConvertFloatInt}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.{*, given}

import java.nio.ByteOrder

trait RelationalI32Values extends Interpreter with RelationalAddresses:
  import Type.*
  import Value.*
  import NumValue.*

  enum RelI32:
    case NumExpr(expr: ApronExpr[VirtAddr, Type])
    case BoolExpr(expr: Bool)
    case AllocationSites(sites: PowVirtAddr, size: ApronExpr[VirtAddr, Type])
  import RelI32.*

  final type I32 = RelI32
  final type Bool = ApronBool[VirtAddr, Type]

  extension (i32: I32)
    inline def asNumExpr(using apronState: ApronState[VirtAddr, Type]): ApronExpr[VirtAddr, Type] =
      i32 match
        case NumExpr(expr) => expr
        case BoolExpr(condition) => apronState.getBoolean(condition) match
          case Topped.Actual(true) => ApronExpr.lit(1, I32Type)
          case Topped.Actual(false) => ApronExpr.lit(0, I32Type)
          case Topped.Top => ApronExpr.interval(0, 1, I32Type)
        case AllocationSites(sites, _) => ApronExpr.top(I32Type)

    def asNumExprLazy(using lazyApronState: Lazy[ApronState[VirtAddr, Type]]): ApronExpr[VirtAddr, Type] =
      given ApronState[VirtAddr, Type] = lazyApronState.value
      i32.asNumExpr

  final override def topI32: I32 = NumExpr(ApronExpr.constant(ApronExpr.topInterval, I32Type))
  final override def booleanToVal(b: Bool): Value = Num(Int32(BoolExpr(b)))

  var nullAddrVirt: VirtAddr = null
  private def nullAddr(using apronState: ApronState[VirtAddr,Type]): VirtAddr =
    if(nullAddrVirt == null) {
      val nullAddrVirt = apronState.alloc(AddrCtx.Heap(HeapCtx.Static(0)))
      apronState.assign(nullAddrVirt, ApronExpr.lit(0, Type.I32Type))
      nullAddrVirt
    } else {
      nullAddrVirt
    }

  private def containsNull(addrs: PowVirtAddr): Boolean =
    addrs.iterator.exists(virt =>
      virt.ctx == AddrCtx.Heap(HeapCtx.Static(0))
    )

  given CombineI32[W <: Widening](using combineApronExpr: Combine[ApronExpr[VirtAddr,Type], W], apronState: Lazy[ApronState[VirtAddr,Type]]): Combine[I32, W] with
    override def apply(v1: I32, v2: I32): MaybeChanged[I32] =
      (v1, v2) match
        case (BoolExpr(b1), BoolExpr(b2)) if (b1 == b2) => Unchanged(BoolExpr(b1))
        case (AllocationSites(sites1, size1), AllocationSites(sites2, size2)) =>
          val union = sites1.union(sites2)
          val joinedSize = combineApronExpr(size1,size2)
          MaybeChanged(AllocationSites(union,joinedSize.get), joinedSize.hasChanged || sites1.physicalAddresses.size < union.physicalAddresses.size)
        case (NumExpr(expr), _ : (BoolExpr | AllocationSites)) if expr.isBottom == Topped.Actual(true) => Changed(v2)
        case (_ : (BoolExpr | AllocationSites), NumExpr(expr)) if expr.isBottom == Topped.Actual(true) => Unchanged(v1)
        case (NumExpr(expr), AllocationSites(sites, size)) if apronState.value.getInterval(expr).isZero =>
          given ApronState[VirtAddr,Type] = apronState.value
          Changed(AllocationSites(sites.add(nullAddr), size))
        case (AllocationSites(sites, size), NumExpr(expr)) if apronState.value.getInterval(expr).isZero =>
          given ApronState[VirtAddr, Type] = apronState.value
          MaybeChanged(AllocationSites(sites.add(nullAddr), size), containsNull(sites))
        case (_, _) => combineApronExpr(v1.asNumExprLazy, v2.asNumExprLazy).map(NumExpr.apply)

  given overflowHandling: OverflowHandling = OverflowHandling.WrapAround

  given I32IntegerOps(using apronState: ApronState[VirtAddr, Type], failure: Failure, effectStack: EffectStack): IntegerOps[Int, I32] =
    given apronExprIntOps: IntegerOps[Int, ApronExpr[VirtAddr, Type]] = RelationalIntOps[VirtAddr, Type]
    new LiftedIntegerOps[Int, I32, ApronExpr[VirtAddr,Type]](extract = _.asNumExpr, inject = NumExpr(_)):
      override def bitAnd(v1: I32, v2: I32): I32 =
        (v1, v2) match
          case (BoolExpr(e1), BoolExpr(e2)) => BoolExpr(ApronBool.And(e1, e2))
          case _ => NumExpr(apronExprIntOps.bitAnd(v1.asNumExpr, v2.asNumExpr))
      override def bitOr(v1: I32, v2: I32): I32 =
        (v1, v2) match
          case (BoolExpr(e1), BoolExpr(e2)) => BoolExpr(ApronBool.Or(e1, e2))
          case _ => NumExpr(apronExprIntOps.bitOr(v1.asNumExpr, v2.asNumExpr))


  given I32EqOps(using apronState: ApronState[VirtAddr, Type], failure: Failure, effectStack: EffectStack): EqOps[I32, Bool] with
    override def equ(v1: I32, v2: I32): Bool =
      (v1, v2) match
        case (AllocationSites(sites, _), NumExpr(expr)) =>
          if(apronState.getInterval(expr).isZero)
            ApronBool.Constant(Topped.Actual(containsNull(sites)))
          else
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
            EqOps.equ(v1.asNumExpr, i2)
        case (NumExpr(_), BoolExpr(_)) =>
          equ(v2, v1)
        case (BoolExpr(c1), BoolExpr(c2)) =>
          println(s"Created boolean condition ($c1 iff $c2), which may blow up the size of the boolean exponentially")
          ApronBool.Or(ApronBool.And(c1, c2), ApronBool.And(c1.negated, c2.negated))
        case _ => EqOps.equ(v1.asNumExpr, v2.asNumExpr)

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
