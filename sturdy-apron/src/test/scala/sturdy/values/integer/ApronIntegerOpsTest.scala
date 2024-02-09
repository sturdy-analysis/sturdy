package sturdy.values.integer

import org.scalatest.matchers.should.Matchers.*

import apron.*

import sturdy.apron.{ApronExpr, ApronRecencyState, ApronState, given}
import sturdy.effect.Stateless
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.{ApronRecencyStore, ApronStore, RecencyStore, given}
import sturdy.values.Finite
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}

enum Ctx:
  case TempVar(tpe: Type, n: Int)
type Type = BaseType[Int]

given Ordering[Ctx] = Ordering.by { case Ctx.TempVar(tpe,n) => (tpe, n) }
given Finite[Ctx] with {}
given tempVariableAllocator: Allocator[Ctx, Type] = new Allocator[Ctx, Type] with Stateless:
  var n = 0

  override def alloc(ctx: Type): Ctx =
    n += 1
    Ctx.TempVar(ctx, n)


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
      iv.inf().toDouble(d, 0)
      val lower = d(0).intValue()
      iv.sup().toDouble(d, 0)
      val upper = d(0).intValue()
      (lower,upper)
  }
}

class ApronIntegerOpsTest extends IntegerOpsTest[Int, ApronExpr[VirtAddr, Type]](
  size = 100,
  makeIntegerOps = makeIntegerOps
)