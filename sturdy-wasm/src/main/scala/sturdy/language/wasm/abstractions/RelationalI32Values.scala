package sturdy.language.wasm.abstractions

import apron.*
import sturdy.apron.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.language.wasm.Interpreter
import sturdy.data.{*, given}
import sturdy.fix.DomLogger
import sturdy.language.wasm.generic.{FixIn, FrameData, FuncId}
import sturdy.util.Lazy
import sturdy.values.config.{BitSign, BytesSize, Overflow}
import sturdy.values.convert.{&&, Convert, ConvertConfig, LiftedConvert, NilCC, SomeCC}
import sturdy.values.floating.{ConvertDoubleInt, ConvertFloatInt}
import sturdy.values.integer.{IntegerOpsWithSignInterpretation, RelationalBaseIntegerOps, *, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}

import scala.annotation.tailrec

trait RelationalI32Values extends Interpreter with RelationalAddresses:
  import Type.*
  import Value.*
  import NumValue.*
  
  enum RelI32:
    case NumExpr(expr: ApronExpr[VirtAddr, Type])
    case BoolExpr(expr: Bool)
    case GlobalAddr(nameAndStart: Powerset[(String,Int)], offset: ApronExpr[VirtAddr,Type])
    case StackAddr(function: Powerset[FuncId], frameSize: ApronExpr[VirtAddr,Type], stackPointer: ApronExpr[VirtAddr,Type], initialOffset: Powerset[Int], otherOffset: ApronExpr[VirtAddr,Type])
    case HeapAddr(sites: AbstractReference[PowVirtAddr], size: ApronExpr[VirtAddr, Type], initialOffset: Powerset[Int], otherOffset: ApronExpr[VirtAddr,Type])

    def asNumExpr(using apronState: ApronState[VirtAddr, Type], resolveState: ResolveState): ApronExpr[VirtAddr, Type] =
      this match
        case NumExpr(expr) => expr
        case BoolExpr(condition) => apronState.assert(condition)(using resolveState) match
          case Topped.Actual(true) => ApronExpr.lit(1, I32Type)
          case Topped.Actual(false) => ApronExpr.lit(0, I32Type)
          case Topped.Top => ApronExpr.interval(0, 1, I32Type)
        case GlobalAddr(glob, offset) =>
          val start = glob.set.iterator.map((_, start) => Interval(start, start)).reduce(Join(_, _).get)
          ApronExpr.intAdd(ApronExpr.constant(start, I32Type), offset, I32Type)
        case stackAddr:StackAddr =>
          ApronExpr.intAdd(stackAddr.stackPointer, this.getOffset, I32Type)
        case HeapAddr(_, _, _, _) =>
          ApronExpr.top(I32Type)

    def addOffset(offset: ApronExpr[VirtAddr, Type])(using apronState: ApronState[VirtAddr, Type]): (NumExpr | GlobalAddr | StackAddr | HeapAddr) =
      this match
        case numExpr: NumExpr =>
          NumExpr(ApronExpr.intAdd(numExpr.expr, offset, I32Type))
        case boolExpr: BoolExpr => NumExpr(ApronExpr.intAdd(boolExpr.asNumExpr, offset, I32Type))
        case globalAddress: GlobalAddr =>
          globalAddress.copy(offset = ApronExpr.intAdd(globalAddress.offset, offset, I32Type))
        case stackAddress: StackAddr =>
          if (stackAddress.initialOffset.isEmpty) {
            apronState.getInt(offset) match {
              case Some(o) => stackAddress.copy(initialOffset = Powerset(o))
              case None => stackAddress.copy(otherOffset = apronState.simplify(ApronExpr.intAdd(stackAddress.otherOffset, offset, I32Type)))
            }
          } else {
            stackAddress.copy(otherOffset = apronState.simplify(ApronExpr.intAdd(stackAddress.otherOffset, offset, I32Type)))
          }
        case heapAddress: HeapAddr =>
          if (heapAddress.initialOffset.isEmpty) {
            apronState.getInt(offset) match {
              case Some(o) => heapAddress.copy(initialOffset = Powerset(o))
              case None => heapAddress.copy(otherOffset = apronState.simplify(ApronExpr.intAdd(heapAddress.otherOffset, offset, I32Type)))
            }
          } else {
            heapAddress.copy(otherOffset = apronState.simplify(ApronExpr.intAdd(heapAddress.otherOffset, offset, I32Type)))
          }

    def getOffset: ApronExpr[VirtAddr,Type] =
      this match
        case numExpr: NumExpr => numExpr.expr
        case boolExpr: BoolExpr => ApronExpr.constant(Interval(0d,1d), I32Type)
        case globalAddress: GlobalAddr => globalAddress.offset
        case stackAddress: StackAddr =>
          val initialIv =
            if(stackAddress.initialOffset.isEmpty)
              DoubleScalar(0)
            else
              stackAddress.initialOffset.set.iterator.map(o => Interval(o,o)).reduce(Join(_,_).get)
          ApronExpr.intAdd(ApronExpr.constant(initialIv, I32Type), stackAddress.otherOffset, I32Type)
        case heapAddress: HeapAddr =>
          val initialIv =
            if(heapAddress.initialOffset.isEmpty)
              DoubleScalar(0)
            else
              heapAddress.initialOffset.set.iterator.map(o => Interval(o,o)).reduce(Join(_,_).get)
          ApronExpr.intAdd(ApronExpr.constant(initialIv, I32Type), heapAddress.otherOffset, I32Type)

    def getEffectiveAddress: ApronExpr[VirtAddr,Type] =
      this match
        case numExpr: NumExpr => numExpr.expr
        case boolExpr: BoolExpr => ApronExpr.constant(Interval(0d, 1d), I32Type)
        case globalAddress: GlobalAddr =>
          val startIv = globalAddress.nameAndStart.set.map((_,start) => Interval(start,start)).reduce(Join(_,_).get)
          ApronExpr.intAdd(ApronExpr.constant(startIv, I32Type), globalAddress.offset, I32Type)
        case stackAddress: StackAddr =>
          ApronExpr.intAdd(stackAddress.stackPointer, stackAddress.getOffset, I32Type)
        case heapAddress: HeapAddr =>
          ApronExpr.constant(Interval(0d, Double.PositiveInfinity), I32Type)

    def asNumExprLazy(using lazyApronState: Lazy[ApronState[VirtAddr, Type]], resolveState: ResolveState): ApronExpr[VirtAddr, Type] =
      given ApronState[VirtAddr, Type] = lazyApronState.value
      asNumExpr(using resolveState = resolveState)

    def addNull(): Option[HeapAddr] =
      this match
        case heapAddress: HeapAddr => Some(heapAddress.copy(sites = Join(heapAddress.sites, AbstractReference.Null).get))
        case _ => None

  import RelI32.*

  final type I32 = RelI32
  final type Bool = ApronBool[VirtAddr, Type]

  final override def topI32: I32 = NumExpr(ApronExpr.constant(ApronExpr.topInterval, I32Type))
  final override def booleanToVal(b: Bool): Value = Num(Int32(BoolExpr(b)))

  given CombineI32[W <: Widening](using combineApronExpr: Combine[ApronExpr[VirtAddr,Type], W], combineApronBool: Combine[ApronBool[VirtAddr,Type], W], apronStateLazy: Lazy[ApronRecencyState[AddrCtx,Type,Value]]): Combine[I32, W] with
    override def apply(v1: I32, v2: I32): MaybeChanged[I32] =
     (v1, v2) match
        case _ if(v1 eq v2) => Unchanged(v1)
        case (NumExpr(expr), _) if apronStateLazy.value.isBottom(expr)(using ResolveState.Left) == Topped.Actual(true) =>
          MaybeChanged(v2,
            v2 match
              case NumExpr(e2) => expr.isBottom != Topped.Actual(true)
              case _ => true
          )
        case (_, NumExpr(expr)) if apronStateLazy.value.isBottom(expr)(using ResolveState.Right) == Topped.Actual(true) => Unchanged(v1)
        case (BoolExpr(b1), BoolExpr(b2)) => combineApronBool(b1, b2).map(BoolExpr.apply)
        case (HeapAddrLeft(heapAddr1), HeapAddrRight(heapdAddr2)) => CombineHeapAddr(heapAddr1,heapdAddr2)
        case (g1: GlobalAddr, g2: GlobalAddr) =>
          CombineGlobalAddr(g1, g2)
        case (NumExpr(sp1@ApronExpr.Addr(ApronVar(VirtAddr(AddrCtx.Global(0), _, _)), _, _)), s2: StackAddr) =>
          CombineStackAddr(
            StackAddr(Powerset(), ApronExpr.constant(ApronExpr.bottomInterval, I32Type), sp1, Powerset(), ApronExpr.constant(ApronExpr.bottomInterval, I32Type)),
            s2
          )
        case (s1: StackAddr, NumExpr(sp2@ApronExpr.Addr(ApronVar(VirtAddr(AddrCtx.Global(0), _, _)), _, _))) =>
          CombineStackAddr(
            s1,
            StackAddr(Powerset(), ApronExpr.constant(ApronExpr.bottomInterval, I32Type), sp2, Powerset(), ApronExpr.constant(ApronExpr.bottomInterval, I32Type)),
          )
        case (s1: StackAddr, s2: StackAddr) => CombineStackAddr(s1,s2)
        case (NumExpr(e1), NumExpr(e2)) => combineApronExpr(e1, e2).map(NumExpr.apply)
        case (_,_) =>
          given ApronState[VirtAddr,Type] = apronStateLazy.value
          combineApronExpr(v1.asNumExpr(using resolveState = ResolveState.Left), v2.asNumExpr(using resolveState = ResolveState.Right)).map(NumExpr.apply)

  private object HeapAddrLeft:
    inline def unapply(v: RelI32)(using apronStateLazy: Lazy[ApronRecencyState[AddrCtx,Type,Value]]): Option[HeapAddr] =
      AllocSites.unapply(v)(using resolveState = ResolveState.Left)

  private object HeapAddrRight:
    inline def unapply(v: RelI32)(using apronStateLazy: Lazy[ApronRecencyState[AddrCtx, Type, Value]]): Option[HeapAddr] =
      AllocSites.unapply(v)(using resolveState = ResolveState.Right)

  private object AllocSites:
    def unapply(v: RelI32)(using apronStateLazy: Lazy[ApronRecencyState[AddrCtx,Type,Value]], resolveState: ResolveState): Option[HeapAddr] =
      v match {
        case heapAddr: HeapAddr => Some(heapAddr)
        case NumExpr(ApronExpr.Constant(coeff, _, _)) =>
          if(coeff.isZero)
            Some(HeapAddr(AbstractReference.Null, ApronExpr.constant(ApronExpr.bottomInterval, I32Type), Powerset(), ApronExpr.lit(0, I32Type)))
          else
            None
        case NumExpr(expr@ApronExpr.Addr(x,_,_)) =>
          given apronState: ApronRecencyState[AddrCtx,Type,Value] = apronStateLazy.value
          val iv = apronState.getInterval(expr)(using resolveState)

          apronState.getNonRelationalValue(x)(using resolveState).toOption match {
            case Some(Value.Num(Int32(allocationSites: HeapAddr))) =>
              if(iv.isBottom)
                Some(allocationSites)
              else if(iv.isZero)
                allocationSites.addNull()
              else
                None
            case Some(Value.Num(Int32(NumExpr(expr)))) if expr.isBottom == Topped.Actual(true) =>
              if (iv.isZero)
                Some(HeapAddr(AbstractReference.Null, ApronExpr.constant(ApronExpr.bottomInterval, I32Type), Powerset(), ApronExpr.lit(0, I32Type)))
              else
                None
            case Some(nonRelValue) =>
              None
            case None => throw IllegalArgumentException(s"Address $x is unbound in non-relational store ${apronState.relationalStore}")
          }
        case _ => None
      }

  given CombineHeapAddr[W <: Widening](using combineApronExpr: Combine[ApronExpr[VirtAddr,Type], W]): Combine[HeapAddr, W] with
    override def apply(v1: HeapAddr, v2: HeapAddr): MaybeChanged[HeapAddr] = {
      if(v1 eq v2) {
        Unchanged(v1)
      } else {
        for {
          sites <- Combine(v1.sites, v2.sites)
          size <- combineApronExpr(v1.size, v2.size)
          initialOffset <- Join(v1.initialOffset, v2.initialOffset)
          otherOffset <- combineApronExpr(v1.otherOffset, v2.otherOffset)
        } yield HeapAddr(sites, size, initialOffset, otherOffset)
      }
    }

  given CombineGlobalAddr[W <: Widening](using combineApronExpr: Combine[ApronExpr[VirtAddr, Type], W]): Combine[GlobalAddr, W] with
    override def apply(v1: GlobalAddr, v2: GlobalAddr): MaybeChanged[GlobalAddr] =
      if (v1 eq v2) {
        Unchanged(v1)
      } else {
        for {
          nameAndStart <- Join(v1.nameAndStart, v2.nameAndStart)
          offset <- combineApronExpr(v1.offset, v2.offset)
        } yield GlobalAddr(nameAndStart, offset)
      }

  given CombineStackAddr[W <: Widening](using combineApronExpr: Combine[ApronExpr[VirtAddr,Type], W]): Combine[StackAddr, W] with
    override def apply(v1: StackAddr, v2: StackAddr): MaybeChanged[StackAddr] = {
      if(v1 eq v2) {
        Unchanged(v1)
      } else {
        for {
          function <- Join(v1.function, v2.function)
          frameSize <- combineApronExpr(v1.frameSize, v2.frameSize)
          stackPointer <- combineApronExpr(v1.stackPointer, v2.stackPointer)
          initialOffset <- Join(v1.initialOffset, v2.initialOffset)
          offset <- combineApronExpr(v1.otherOffset, v2.otherOffset)
        } yield StackAddr(function, frameSize, stackPointer, initialOffset, offset)
      }
    }

  final class I32IntegerOps(rootFrameData: FrameData, globals: => Vector[(String,Interval)], stackRange: => Interval)
                           (using intOps: IntegerOpsWithSignInterpretation[Int, ApronExpr[VirtAddr, Type]], apronState: ApronState[VirtAddr, Type], failure: Failure, effectStack: EffectStack, domLogger: DomLogger[FixIn])
    extends LiftedIntegerOpsWithSignInterpretation[Int, I32, ApronExpr[VirtAddr,Type]](extract = _.asNumExpr, inject = NumExpr(_)):
      override def integerLit(i: Int): I32 = {
        globals.find((name, iv) => Interval(i,i).isLeq(iv)) match
          case Some((name,iv)) =>
            val offset = apronState.getInterval(ApronExpr.intSub(ApronExpr.lit(i, I32Type), ApronExpr.constant(iv.inf(), I32Type), I32Type))
            GlobalAddr(Powerset((name,ApronExpr.toInt(iv.inf()).get)), ApronExpr.constant(offset.inf(), I32Type))
          case None => super.integerLit(i)
      }

      override def add(v1: I32, v2: I32): I32 =
        (v1, v2) match

          case (NumExpr(stackPointer@ApronExpr.Addr(ApronVar(VirtualAddress(ctx@AddrCtx.Global(0), _, _)), _, _)), _) =>
            newStackFrame(
              stackPointerExpr = ApronExpr.intAdd(stackPointer, v2.asNumExpr, I32Type),
              frameSize = ApronExpr.intNegate(v2.asNumExpr)
            )

          case (NumExpr(stackPointer@ApronExpr.Constant(const: Scalar, _, _)), _) if const.isEqual(stackRange.sup()) =>
            newStackFrame(
              stackPointerExpr = ApronExpr.intAdd(stackPointer, v2.asNumExpr, I32Type),
              frameSize = ApronExpr.intNegate(v2.asNumExpr)
            )

          case (sp: StackAddr, _) =>
            val v2Iv = apronState.getInterval(v2.asNumExpr)
            if(v2Iv.sup().sgn() < 0) {
              // if v2 < 0
              newStackFrame(
                stackPointerExpr = ApronExpr.intAdd(sp.stackPointer, v2.asNumExpr, I32Type),
                frameSize = ApronExpr.intNegate(v2.asNumExpr)
              )
            } else {
              // if v2 >= 0
              sp.addOffset(v2.asNumExpr)
            }

          case (_, sp: StackAddr) => sp.addOffset(v1.asNumExpr)
          case (glob: GlobalAddr, _) => glob.addOffset(v2.asNumExpr)
          case (_, glob: GlobalAddr) => glob.addOffset(v1.asNumExpr)
          case (heapAddr: HeapAddr, _) => heapAddr.addOffset(v2.asNumExpr)
          case (_, heapAddr: HeapAddr) => heapAddr.addOffset(v1.asNumExpr)

          case (_, _) => NumExpr(intOps.add(v1.asNumExpr, v2.asNumExpr))

      override def sub(v1: I32, v2: I32): I32 =
        v1 match
          case NumExpr(stackPointer@ApronExpr.Addr(ApronVar(VirtualAddress(ctx@AddrCtx.Global(0), _, _)), _, _)) =>
            newStackFrame(
              stackPointerExpr = ApronExpr.intSub(stackPointer, v2.asNumExpr, I32Type),
              frameSize = v2.asNumExpr
            )
          case NumExpr(stackPointer@ApronExpr.Constant(const: Scalar, _, _)) if const.isEqual(stackRange.sup()) =>
            newStackFrame(
              stackPointerExpr = ApronExpr.intSub(stackPointer, v2.asNumExpr, I32Type),
              frameSize = v2.asNumExpr
            )
          case stackAddr: StackAddr =>
            val v2Iv = apronState.getInterval(v2.asNumExpr)
            if(v2Iv.inf().sgn() >= 0) {
              // if v2 >= 0
              newStackFrame(
                stackPointerExpr = ApronExpr.intSub(stackAddr.stackPointer, v2.asNumExpr, I32Type),
                frameSize = v2.asNumExpr
              )
            } else {
              // if v2 < 0 then sp + -v2
              stackAddr.addOffset(ApronExpr.intNegate(v2.asNumExpr, I32Type))
            }

          case heapAddr: HeapAddr => heapAddr.addOffset(ApronExpr.intNegate(v2.asNumExpr, I32Type))
          case _ => NumExpr(intOps.sub(v1.asNumExpr, v2.asNumExpr))


      private def newStackFrame(stackPointerExpr: ApronExpr[VirtAddr, Type], frameSize: ApronExpr[VirtAddr,Type]): StackAddr =
        val newStackPointer = apronState.alloc(AddrCtx.Global(0))
        apronState.assign(newStackPointer, stackPointerExpr)
        val dom = domLogger.currentDom.getOrElse(FixIn.MostGeneralClientLoop(rootFrameData.module))
        StackAddr(
          function = Powerset(dom.funcId.get),
          frameSize = apronState.simplify(frameSize),
          stackPointer = ApronExpr.addr(newStackPointer, I32Type),
          initialOffset = Powerset(),
          otherOffset = ApronExpr.lit(0, I32Type)
        )

      override def bitAnd(v1: I32, v2: I32): I32 =
        (v1, v2) match
          case (BoolExpr(e1), BoolExpr(e2)) => BoolExpr(ApronBool.And(e1, e2))
          case _ => NumExpr(intOps.bitAnd(v1.asNumExpr, v2.asNumExpr))
      override def bitOr(v1: I32, v2: I32): I32 =
        (v1, v2) match
          case (BoolExpr(e1), BoolExpr(e2)) => BoolExpr(ApronBool.Or(e1, e2))
          case _ => NumExpr(intOps.bitOr(v1.asNumExpr, v2.asNumExpr))

      @tailrec
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
        case (HeapAddr(sites, _, _, _), NumExpr(expr)) =>
          if(apronState.getInterval(expr).isZero) {
            sites match {
              case _: AbstractReference.Null.type => ApronBool.Constant(Topped.Actual(true))
              case _: AbstractReference.Addr[?] => ApronBool.Constant(Topped.Actual(false))
              case _: AbstractReference.NullAddr[?] => ApronBool.Constant(Topped.Top)
            }
          } else
            equ(NumExpr(v1.asNumExpr), v2)
        case (_: NumExpr, _: HeapAddr) =>
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

  given I32UnsignedOrderingOps(using UnsignedOrderingOps[ApronExpr[VirtAddr,Type], ApronCons[VirtAddr, Type]], ApronState[VirtAddr, Type], Failure, EffectStack): UnsignedOrderingOps[I32, Bool] =
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

  def i32ToNonRelational(using apronState: ApronState[VirtAddr,Type])(v: I32): I32 =
    v match
      case NumExpr(e) =>
        NumExpr(apronState.toNonRelational(e))
      case BoolExpr(e) =>
        BoolExpr(apronState.toNonRelational(e))
      case GlobalAddr(name, offset) =>
        GlobalAddr(name, apronState.toNonRelational(offset))
      case StackAddr(function, frameSize, stackPointer, initialOffset, offset) =>
        StackAddr(function, apronState.toNonRelational(frameSize), apronState.toNonRelational(stackPointer), initialOffset, apronState.toNonRelational(offset))
      case HeapAddr(sites, size, initialOffset, offset) =>
        HeapAddr(sites, apronState.toNonRelational(size), initialOffset, apronState.toNonRelational(offset))

  final class NonRelationalI32IntegerOps(using relationalIntOps: IntegerOpsWithSignInterpretation[Int, I32], apronState: ApronState[VirtAddr, Type])
    extends LiftedIntegerOpsWithSignInterpretation[Int, I32, I32](extract = i32 => i32, inject = i32ToNonRelational)

  final class NonRelationalI32Convert[From, V, Config <: ConvertConfig[_]](using relationalConvert: Convert[From, Int, V, I32, Config], apronState: ApronState[VirtAddr, Type])
    extends LiftedConvert[From, Int, V, I32, V, I32, Config](
      extract = expr => expr,
      inject = i32ToNonRelational
    )
