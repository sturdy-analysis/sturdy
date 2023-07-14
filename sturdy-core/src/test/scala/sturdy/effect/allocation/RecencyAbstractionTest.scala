package sturdy.effect.allocation

import org.scalatest.funsuite.AnyFunSuite
import Recency.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.RecencyStore
import sturdy.values.{Finite, Widen}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}

class RecencyAbstractionTest extends AnyFunSuite:

  type Ctx = String
  given Finite[Ctx] with {}
  given Widen[NumericInterval[Int]] = NumericIntervalWiden[Int](Set(10, 20, 30, 40, 50, 60, 70, 80, 90), 0, 100)

  test("Allocating multiple addresses in the same context should retire the prior allocated address to old") {
    val ctx1 = "ctx1"

    val alloc = new RecencyAllocator[Ctx]
    val a1 = alloc(ctx1)
    a1.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1,Recent)

    val a2 = alloc(ctx1)

    a1.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1, Old)
    a2.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1, Recent)

    a1 should equal (a1)
    a2 should equal (a2)
    a1 should not equal (a2)

    a1.hashCode() should equal (a1.hashCode())
    a2.hashCode() should equal (a2.hashCode())
    a1.hashCode() should not equal(a2.hashCode())


    val a3 = alloc(ctx1)

    a1.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1, Old)
    a2.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1, Old)
    a3.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1, Recent)

    a1 should equal(a1)
    a2 should equal(a2)
    a3 should equal(a3)
    a1 should equal (a2)
    a1 should not equal (a3)
    a2 should not equal (a3)

    a1.hashCode() should equal(a1.hashCode())
    a2.hashCode() should equal(a2.hashCode())
    a3.hashCode() should equal(a3.hashCode())
    a1.hashCode() should equal(a2.hashCode())
    a1.hashCode() should not equal (a3.hashCode())
    a2.hashCode() should not equal (a3.hashCode())
  }
//
//  test("Allocation of the same context in two different branches") {
//    val alloc = new RecencyAllocator[Ctx]
//    val effectStack: EffectStack = new EffectStack(List(alloc))
//
//    val ctx1 = "ctx1"
//    val a1 = alloc(ctx1)
//
//    var a2: VirtualAddress[Ctx] = null
//    var a3: VirtualAddress[Ctx] = null
//
//    effectStack.joinComputations {
//      a2 = alloc(ctx1)
//    } {
//      a3 = alloc(ctx1)
//
//      // a1 should be old, since a3 is a more recent allocation of ctx1
//      a1.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1, Old)
//
//      // a2 should be recent, since it appears in a different branch than a3.
//      a2.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1, Recent)
//
//      unit
//    }
//  }

  test("Recency store joins most recent address into old address upon new allocation") {
    val alloc = new RecencyAllocator[Ctx]
    val store = new RecencyStore[Ctx,NumericInterval[Int]](alloc, Map())
    val ctx1 = "ctx1"


    val a1 = alloc(ctx1)
    store.write(a1, NumericInterval(1,2))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 2)))


    val a2 = alloc(ctx1)
    store.read(a1) should be (JOptionA.noneSome(NumericInterval(1, 2)))

    store.write(a2, NumericInterval(5,6))
    store.read(a1) should be (JOptionA.noneSome(NumericInterval(1, 2)))
    store.read(a2) should be (JOptionA.noneSome(NumericInterval(5, 6)))


    val a3 = alloc(ctx1)
    store.read(a1) should be (JOptionA.noneSome(NumericInterval(1, 6)))
    store.read(a2) should be (JOptionA.noneSome(NumericInterval(1, 6)))

    store.write(a3, NumericInterval(8,9))
    store.read(a1) should be (JOptionA.noneSome(NumericInterval(1, 6)))
    store.read(a2) should be (JOptionA.noneSome(NumericInterval(1, 6)))
    store.read(a3) should be (JOptionA.noneSome(NumericInterval(8, 9)))
  }

  test("Recency store joins strong updates in separate branches") {
    val alloc = new RecencyAllocator[Ctx]
    val store = new RecencyStore[Ctx, NumericInterval[Int]](alloc, Map())
    val effectStack: EffectStack = new EffectStack(List(store, alloc))

    val ctx1 = "ctx1"
    val a1 = alloc(ctx1)
    effectStack.joinComputations(
      store.write(a1, NumericInterval(1, 2))
    )(
      store.write(a1, NumericInterval(5, 6))
    )
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 6)))
  }

  test("Recency store should handle reallocation that happens in while loops") {
    val alloc = new RecencyAllocator[Ctx]
    val store = new RecencyStore[Ctx, NumericInterval[Int]](alloc, Map())
    val effectStack: EffectStack = new EffectStack(List(store, alloc))

    /**
     * Program:
     * while(...) {
     *   x = ...;
     * }
     */
    val ctx = "x"
    effectStack.joinComputations {
      // First iteration of while body
      val a1 = alloc(ctx)
      store.write(a1, NumericInterval(1, 2))
      store.read(a1) should be (JOptionA.noneSome(NumericInterval(1, 2)))

      // Second iteration of while body
      effectStack.joinComputations {
        val a2 = alloc(ctx)
        store.write(a2, NumericInterval(5, 6))
        store.read(a1) should be (JOptionA.noneSome(NumericInterval(1, 2)))
        store.read(a2) should be (JOptionA.noneSome(NumericInterval(5, 6)))

        // Third iteration of while body
        effectStack.joinComputations {
          val a3 = alloc(ctx)
          store.write(a3, NumericInterval(8, 9))
          store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 6)))
          store.read(a2) should be(JOptionA.noneSome(NumericInterval(1, 6)))
          store.read(a3) should be(JOptionA.noneSome(NumericInterval(8, 9)))
          unit
        } {
          // When condition is false, exit loop
        }
      } {
        // When condition is false, exit loop
      }
    }{
      // When condition is false, exit loop
    }
  }
