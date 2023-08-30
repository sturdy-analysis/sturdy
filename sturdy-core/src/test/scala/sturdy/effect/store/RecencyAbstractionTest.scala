package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import sturdy.effect.store.Recency.*
import org.scalatest.matchers.should.Matchers.{be, *}
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.{PhysicalAddress, RecencyStore, VirtualAddress}
import sturdy.values.{Finite, Widen}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}

class RecencyAbstractionTest extends AnyFunSuite:

  type Ctx = String
  given Finite[Ctx] with {}
  given Widen[NumericInterval[Int]] = NumericIntervalWiden[Int](Set(10, 20, 30, 40, 50, 60, 70, 80, 90), 0, 100)

  test("Recency store joins most recent address into old address upon new allocation") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val ctx1 = "ctx1"


    val a1 = store.alloc(ctx1)
    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))

    store.write(a1, NumericInterval(1, 2))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 2)))


    val a2 = store.alloc(ctx1)

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Old))
    a2.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))

    a1 should equal(a1)
    a2 should equal(a2)
    a1 should not equal (a2)

    a1.hashCode() should equal(a1.hashCode())
    a2.hashCode() should equal(a2.hashCode())
    a1.hashCode() should not equal (a2.hashCode())

    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 2)))

    store.write(a2, NumericInterval(5, 6))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 2)))
    store.read(a2) should be(JOptionA.noneSome(NumericInterval(5, 6)))


    val a3 = store.alloc(ctx1)
    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Old))
    a2.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Old))
    a3.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))

    a1 should equal(a1)
    a2 should equal(a2)
    a3 should equal(a3)
    a1 should equal(a2)
    a1 should not equal (a3)
    a2 should not equal (a3)

    a1.hashCode() should equal(a1.hashCode())
    a2.hashCode() should equal(a2.hashCode())
    a3.hashCode() should equal(a3.hashCode())
    a1.hashCode() should equal(a2.hashCode())
    a1.hashCode() should not equal (a3.hashCode())
    a2.hashCode() should not equal (a3.hashCode())

    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 6)))
    store.read(a2) should be(JOptionA.noneSome(NumericInterval(1, 6)))

    store.write(a3, NumericInterval(8, 9))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 6)))
    store.read(a2) should be(JOptionA.noneSome(NumericInterval(1, 6)))
    store.read(a3) should be(JOptionA.noneSome(NumericInterval(8, 9)))
  }

  test("Allocation of the same context in two different branches") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val effectStack: EffectStack = new EffectStack(List(store))

    val ctx1 = "ctx1"
    val a1 = store.alloc(ctx1)
    store.write(a1, NumericInterval(3,4))

    var a2: VirtualAddress[Ctx] = null
    var a3: VirtualAddress[Ctx] = null

    effectStack.joinComputations {
      a2 = store.alloc(ctx1)
      store.write(a2, NumericInterval(1, 2))
    } {
      // a1 should be old, since a3 is a more recent allocation of ctx1
      a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
      store.read(a1) should be(JOptionA.noneSome(NumericInterval(3,4)))

      a3 = store.alloc(ctx1)
      store.write(a3, NumericInterval(5, 6))

      // a1 should be old, since a3 is a more recent allocation of ctx1
      a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Old))
      store.read(a1) should be(JOptionA.noneSome(NumericInterval(3,4)))

      // a2 should not be bound to a physical address, since it was allocated in the other branch.
      an [Exception] should be thrownBy a2.lookupPhysicalAddress

      store.free(a3)

      unit
    }

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Old))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(3, 4)))

    a2.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
    store.read(a2) should be(JOptionA.noneSome(NumericInterval(1, 2)))

    an [Exception] should be thrownBy a3.lookupPhysicalAddress
//    a3.lookupPhysicalAddress shouldBe PhysicalAddress(ctx1, Recent)
//    store.read(a3) should be(JOptionA.noneSome(NumericInterval(1, 6)))
  }

  test("Allocate addresses for the same context in separate branches") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val effectStack: EffectStack = new EffectStack(List(store))

    val ctx1 = "ctx1"
    val a1 = store.alloc(ctx1)
    store.write(a1, NumericInterval(1,2))
    var a2: VirtualAddress[Ctx] = null
    var a3: VirtualAddress[Ctx] = null
    effectStack.joinComputations {
      a2 = store.alloc(ctx1)
      store.write(a2, NumericInterval(3, 4))
    } {
      a3 = store.alloc(ctx1)
      store.write(a3, NumericInterval(5, 6))
    }
    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Old))
    a2.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
    a3.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 2)))
    store.read(a2) should be(JOptionA.noneSome(NumericInterval(3, 6)))
    store.read(a3) should be(JOptionA.noneSome(NumericInterval(3, 6)))
  }

  test("Strong updates on the same address in separate branches") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val effectStack: EffectStack = new EffectStack(List(store))

    val ctx1 = "ctx1"
    val a1 = store.alloc(ctx1)
    effectStack.joinComputations(
      store.write(a1, NumericInterval(1, 2))
    )(
      store.write(a1, NumericInterval(5, 6))
    )
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 6)))
  }

  test("Recency store should handle reallocation that happens in while loops") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val effectStack: EffectStack = new EffectStack(List(store))

    /**
     * Program:
     * while(...) {
     *   x = ...;
     * }
     */
    val ctx = "x"
    effectStack.joinComputations {
      // First iteration of while body
      val a1 = store.alloc(ctx)
      store.write(a1, NumericInterval(1, 2))
      store.read(a1) should be (JOptionA.noneSome(NumericInterval(1, 2)))

      // Second iteration of while body
      effectStack.joinComputations {
        val a2 = store.alloc(ctx)
        store.write(a2, NumericInterval(5, 6))
        store.read(a1) should be (JOptionA.noneSome(NumericInterval(1, 2)))
        store.read(a2) should be (JOptionA.noneSome(NumericInterval(5, 6)))

        // Third iteration of while body
        effectStack.joinComputations {
          val a3 = store.alloc(ctx)
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

  test("Join with write in second branch, but not in first branch") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val ctx1 = "ctx1"

    val a1 = store.alloc(ctx1)
    val state1 = store.getState
    store.write(a1, NumericInterval(1,2))
    val state2 = store.getState
    val join = store.join(state1.asInstanceOf,state2.asInstanceOf)
    store.setState(join.get.asInstanceOf[store.State])

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1,2)))
    join.hasChanged should be(true)
  }

  test("Join with two allocations of the same context") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val ctx1 = "ctx1"

    val a1 = store.alloc(ctx1)
    val state1 = store.getState
    val a2 = store.alloc(ctx1)
    val state2 = store.getState
    val join = store.join(state1.asInstanceOf, state2.asInstanceOf)
    store.setState(join.get.asInstanceOf[store.State])

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent), PhysicalAddress(ctx1, Old))
    a2.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
    join.hasChanged should be(false)
  }

  test("Join with writes in both branches") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val ctx1 = "ctx1"

    val a1 = store.alloc(ctx1)
    store.write(a1, NumericInterval(1, 2))
    val state1 = store.getState
    store.write(a1, NumericInterval(3, 4))
    val state2 = store.getState
    val join = store.join(state1.asInstanceOf, state2.asInstanceOf)
    store.setState(join.get.asInstanceOf[store.State])

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 4)))
    join.hasChanged should be(true)
  }

  test("Join with free in second branch") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val ctx1 = "ctx1"

    val a1 = store.alloc(ctx1)
    store.write(a1, NumericInterval(1, 2))
    val state1 = store.getState
    store.free(a1)
    val state2 = store.getState
    val join = store.join(state1.asInstanceOf, state2.asInstanceOf)
    store.setState(join.get.asInstanceOf[store.State])

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1,2)))
    join.hasChanged should be(false)
  }

  test("Reaching fixpoint") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val ctx1 = "ctx1"

    val a1 = store.alloc(ctx1)
    val a2 = store.alloc(ctx1)
    store.write(a1, NumericInterval(1, 4))
    store.write(a2, NumericInterval(3, 4))
    val state1 = store.getState
    val a3 = store.alloc(ctx1)
    store.write(a3, NumericInterval(3, 4))
    val state2 = store.getState
    val join = store.join(state1.asInstanceOf, state2.asInstanceOf)
    store.setState(join.get.asInstanceOf[store.State])

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Old))
    a2.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent), PhysicalAddress(ctx1, Old))
    a3.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx1, Recent))
    join.hasChanged should be(false)
  }

  test("Example 1 in \"Revisiting Recency Abstraction for JavaScript\" with Addr = AllocSite x Recency") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val effectStack: EffectStack = new EffectStack(List(store))
    var a1: VirtualAddress[Ctx] = null
    var a2: VirtualAddress[Ctx] = null

    val l0 = "l0"
    val l3 = "l3"
    /* l0: */ a1 = store.alloc(l0);
    store.write(a1, NumericInterval(1, 1))
    /* l1: */ effectStack.joinComputations {
      /* l2: */ store.write(a1, NumericInterval(2, 2))
      /* l3: */ a2 = store.alloc(l3);
      store.write(a2, NumericInterval(1, 1))
    } {
    }

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(l0, Recent))
    a2.lookupPhysicalAddress shouldBe Set(PhysicalAddress(l3, Recent))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(1, 2)))
    store.read(a2) should be(JOptionA.noneSome(NumericInterval(1, 1)))
  }

  test("Example 1 in \"Revisiting Recency Abstraction for JavaScript\" with Addr = Unit x Recency") {
    val store = new RecencyStore[Ctx, NumericInterval[Int]](AStoreSingleAddrThreadded(Map()))
    val effectStack: EffectStack = new EffectStack(List(store))
    var a1: VirtualAddress[Ctx] = null
    var a2: VirtualAddress[Ctx] = null

    val ctx = "Unit"
    /* l0: */ a1 = store.alloc(ctx); store.write(a1, NumericInterval(1, 1))
    /* l1: */ effectStack.joinComputations {
      /* l2: */ store.write(a1, NumericInterval(2, 2))
      /* l3: */ a2 = store.alloc(ctx); store.write(a2, NumericInterval(1, 1))
    } {
    }

    a1.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx, Recent), PhysicalAddress(ctx, Old))
    a2.lookupPhysicalAddress shouldBe Set(PhysicalAddress(ctx, Recent))
    store.read(a1) should be(JOptionA.noneSome(NumericInterval(2, 2)))
    store.read(a2) should be(JOptionA.noneSome(NumericInterval(1, 1)))
  }