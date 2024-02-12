package sturdy.values.integer

import org.scalatest.matchers.should.Matchers.*
import apron.*
import sturdy.apron.{ApronExpr, ApronRecencyState, ApronRepresentation, ApronState, ApronType, RoundingDir, RoundingType, given}
import sturdy.effect.Stateless
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.store.{ApronRecencyStore, ApronStore, RecencyStore, given}
import sturdy.values.{Combine, Finite, Join, MaybeChanged, Widening}
import sturdy.values.ordering.{EqOps, LiftedEqOps, LiftedOrderingOps, OrderingOps}
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

enum Error extends FailureKind:
  case TypeError

import Error.*

enum Type:
  case IntType(baseType: BaseType[Int])
  case BoolType(baseType: BaseType[Boolean])

  def asInt(using f: Failure): BaseType[Int] =
    this match
      case IntType(tpe) => tpe
      case _ => f.fail(TypeError, s"Expected int, but got $this")

  def asBool(using f: Failure): BaseType[Boolean] =
    this match
      case BoolType(tpe) => tpe
      case _ => f.fail(TypeError, s"Expected bool, but got $this")

  override def toString: String =
    this match
      case IntType(_) => "int"
      case BoolType(_) => "boolean"

given Ordering[Type] = {
  case (Type.IntType(_), Type.IntType(_)) | (Type.BoolType(_), Type.BoolType(_)) => 0
  case (_, _) => -1
}
given IntegerOps[Int,Type] = LiftedIntegerOps[Int,Type,BaseType[Int]](extract = _.asInt, inject = Type.IntType(_))
given OrderingOps[Type,Type] = LiftedOrderingOps[Type,Type,BaseType[Int], BaseType[Boolean]](extract = _.asInt, inject = Type.BoolType(_))
given EqOps[Type,Type] = LiftedEqOps[Type,Type,BaseType[Int], BaseType[Boolean]](extract = _.asInt, inject = Type.BoolType(_))
given ApronType[Type] with
  extension(tpe: Type)
    override def apronRepresentation: ApronRepresentation =
      tpe match
        case Type.IntType(baseType) => baseType.apronRepresentation
        case Type.BoolType(baseType) => baseType.apronRepresentation

    override def roundingDir: RoundingDir =
      tpe match
        case Type.IntType(baseType) => baseType.roundingDir
        case Type.BoolType(baseType) => baseType.roundingDir

    override def roundingType: RoundingType =
      tpe match
        case Type.IntType(baseType) => baseType.roundingType
        case Type.BoolType(baseType) => baseType.roundingType

    override def byteSize: Int =
      tpe match
        case Type.IntType(baseType) => baseType.byteSize
        case Type.BoolType(baseType) => baseType.byteSize

given [W <: Widening]: Combine[Type, W] = {
  case (t@Type.IntType(_), Type.IntType(_)) => MaybeChanged.Unchanged(t)
  case (t@Type.BoolType(_), Type.BoolType(_)) => MaybeChanged.Unchanged(t)
  case (t1,t2) => throw new IllegalStateException(s"Cannot join type $t1 with type $t2")
}

enum Ctx:
  case TempVar(tpe: Type, n: Int)

  override def toString: String =
    this match
      case TempVar(tpe,n) => s"x${n}:$tpe"

given Ordering[Ctx] = Ordering.by { case Ctx.TempVar(tpe,n) => (tpe, n) }
given Finite[Ctx] with {}

given tempVariableAllocator: Allocator[Ctx, Type] = new Allocator[Ctx, Type] with Stateless:
  var n = 0

  override def alloc(tpe: Type): Ctx =
    n += 1
    Ctx.TempVar(tpe, n)


type VirtAddr = VirtualAddress[Ctx]
type PhysAddr = PhysicalAddress[Ctx]
type PowPhysAddr = PowersetAddr[PhysicalAddress[Ctx], PhysicalAddress[Ctx]]
type PowVirtAddr = PowVirtualAddress[Ctx]
type ApronExprVirtAddr = ApronExpr[VirtualAddress[Ctx], Type]
type ApronExprPhysAddr = ApronExpr[PhysicalAddress[Ctx], Type]

val apronManager: Manager = new apron.Polka(true)

def makeIntegerOps: IntervalIntegerOps[Int, ApronExpr[VirtAddr, Type]] = {
  val (recencyStore, apronStore) = ApronRecencyStore[Ctx,Type](apronManager)
  given ApronState[VirtAddr, Type] = new ApronRecencyState(tempVariableAllocator, recencyStore, apronStore) {}
  new ApronIntegerOps[VirtAddr, Type] with IntervalIntegerOps[Int, ApronExpr[VirtAddr, Type]] {
    override def integerLit(i: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intLit(i)

    override def interval(low: Int, high: Int): ApronExpr[VirtAddr, Type] = ApronExpr.intInterval(low,high)

    override def getBounds(n: ApronExpr[VirtAddr, Type]): (Int, Int) =
      val iv = apronState.getBound(n)
      val d = Array[Double](0)
      val lower =
        if(iv.inf().isInfty() != 0)
          Integer.MIN_VALUE
        else
          iv.inf().toDouble(d, 0)
          d(0).intValue()

      val upper =
        if(iv.sup().isInfty() != 0)
          Integer.MAX_VALUE
        else
          iv.sup().toDouble(d, 0)
          d(0).intValue()

      (lower,upper)
  }
}

class ApronIntegerOpsTest extends IntegerOpsTest[Int, ApronExpr[VirtAddr, Type]](
  size = 100,
  makeIntegerOps = makeIntegerOps
)