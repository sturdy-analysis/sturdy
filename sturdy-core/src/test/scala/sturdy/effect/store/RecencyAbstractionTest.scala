package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.*

import sturdy.values.references.Recency.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.RecencyStore
import sturdy.effect.store.given
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}


type Ctx = String
given Finite[Ctx] with {}

type VAddr = VirtualAddress[Ctx]
given Widen[NumericInterval[Int]] = NumericIntervalWiden[Int](Set(10, 20, 30, 40, 50, 60, 70, 80, 90), 0, 100)

class RecencyAbstractionAStoreThreadedTest extends RecencyAbstractionTest(RecencyStore(AStoreThreaded(Map())))

class RecencyAbstractionTest(emptyStore: => RecencyStore[Ctx, VAddr, NumericInterval[Int]]) extends AnyFunSuite:

  test("Recency store joins most recent address into old address upon new allocation") {
    val store = emptyStore
    val ctx1 = "ctx1"


    val a1 = store.alloc(ctx1)
    a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))

    store.write(a1, NumericInterval(1, 2))
    store.read(a1) should be(JOptionA.Some(NumericInterval(1, 2)))


    val a2 = store.alloc(ctx1)

    a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Old))
    a2.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))

    a1 should equal(a1)
    a2 should equal(a2)
    a1 should not equal (a2)

    a1.hashCode() should equal(a1.hashCode())
    a2.hashCode() should equal(a2.hashCode())
    a1.hashCode() should not equal (a2.hashCode())

    store.read(a1) should be(JOptionA.Some(NumericInterval(1, 2)))

    store.write(a2, NumericInterval(5, 6))
    store.read(a1) should be(JOptionA.Some(NumericInterval(1, 2)))
    store.read(a2) should be(JOptionA.Some(NumericInterval(5, 6)))


    val a3 = store.alloc(ctx1)
    a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Old))
    a2.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Old))
    a3.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))

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

    store.read(a1) should be(JOptionA.Some(NumericInterval(1, 6)))
    store.read(a2) should be(JOptionA.Some(NumericInterval(1, 6)))

    store.write(a3, NumericInterval(8, 9))
    store.read(a1) should be(JOptionA.Some(NumericInterval(1, 6)))
    store.read(a2) should be(JOptionA.Some(NumericInterval(1, 6)))
    store.read(a3) should be(JOptionA.Some(NumericInterval(8, 9)))
  }

  test("Join of powersets of virtual addresses") {
    val store = emptyStore
    val ctx1 = "ctx1"

    def joinIsReflexive(virts: PowVirtualAddress[Ctx]) =
      Join(virts, virts).get shouldBe virts
      Join(virts, virts).hasChanged shouldBe false

    def joinIsCommutative(virt1: PowVirtualAddress[Ctx], virt2: PowVirtualAddress[Ctx], virt3: PowVirtualAddress[Ctx], changed: Boolean) =
      Join(virt1, virt2).get shouldBe virt3
      Join(virt1, virt2).hasChanged shouldBe changed
      Join(virt2, virt1).get shouldBe virt3
      Join(virt2, virt1).hasChanged shouldBe changed

    PowVirtualAddress.empty[Ctx].isEmpty shouldBe true
    PowVirtualAddress.empty[Ctx].isStrong shouldBe true

    joinIsReflexive(PowVirtualAddress.empty[Ctx])
    joinIsCommutative(PowVirtualAddress.empty[Ctx], PowVirtualAddress.empty[Ctx], PowVirtualAddress.empty[Ctx], false)

    val a1 = store.alloc(ctx1)
    val virt1 = PowVirtualAddress(a1)

    virt1.isEmpty shouldBe false
    virt1.isStrong shouldBe true

    joinIsReflexive(virt1)
    joinIsCommutative(virt1, virt1, virt1, false)
    joinIsCommutative(PowVirtualAddress.empty[Ctx], virt1, virt1, true)

    val a2 = store.alloc(ctx1)
    val virt2 = PowVirtualAddress(a2)
    val virt3 = PowVirtualAddress(a1, a2)

    virt2.isEmpty shouldBe false
    virt3.isEmpty shouldBe false
    virt1.isStrong shouldBe false
    virt2.isStrong shouldBe true
    virt3.isStrong shouldBe false

    joinIsReflexive(virt1)
    joinIsReflexive(virt2)
    joinIsReflexive(virt3)

    joinIsCommutative(virt1, virt2, virt3, true)
    joinIsCommutative(virt1, virt3, virt3, true)
    joinIsCommutative(virt2, virt3, virt3, true)
  }

  test("Allocation of the same context in two different branches") {
    val store = emptyStore
    val effectStack: EffectStack = new EffectStack(List(store, store.addressTranslation))

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
      a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))
      store.read(a1) should be(JOptionA.Some(NumericInterval(3,4)))

      a3 = store.alloc(ctx1)
      store.write(a3, NumericInterval(5, 6))

      // a1 should be old, since a3 is a more recent allocation of ctx1
      a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Old))
      store.read(a1) should be(JOptionA.Some(NumericInterval(3,4)))

      // a2 should not be bound to a physical address, since it was allocated in the other branch.
      an [Exception] should be thrownBy a2.physical

      store.free(a3)

      unit
    }

    a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Old))
    store.read(a1) should be(JOptionA.Some(NumericInterval(3, 4)))

    a2.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))
    store.read(a2) should be(JOptionA.Some(NumericInterval(1, 6)))

    a3.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))
    store.read(a3) should be(JOptionA.Some(NumericInterval(1, 6)))
  }

  test("Allocate addresses for the same context in separate branches") {
    val store = emptyStore
    val effectStack: EffectStack = new EffectStack(List(store, store.addressTranslation))

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
    a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Old))
    a2.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))
    a3.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))
    store.read(a1) should be(JOptionA.Some(NumericInterval(1, 2)))
    store.read(a2) should be(JOptionA.Some(NumericInterval(3, 6)))
    store.read(a3) should be(JOptionA.Some(NumericInterval(3, 6)))
  }

  test("Strong updates on the same address in separate branches") {
    val store = emptyStore
    val effectStack: EffectStack = new EffectStack(List(store))

    val ctx1 = "ctx1"
    val a1 = store.alloc(ctx1)
    effectStack.joinComputations(
      store.write(a1, NumericInterval(1, 2))
    )(
      store.write(a1, NumericInterval(5, 6))
    )
    store.read(a1) should be(JOptionA.Some(NumericInterval(1, 6)))
  }

  test("Recency store should handle reallocation that happens in while loops") {
    val store = emptyStore
    val effectStack: EffectStack = new EffectStack(List(store, store.addressTranslation))

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
      store.read(a1) should be (JOptionA.Some(NumericInterval(1, 2)))

      // Second iteration of while body
      effectStack.joinComputations {
        val a2 = store.alloc(ctx)
        store.write(a2, NumericInterval(5, 6))
        store.read(a1) should be (JOptionA.Some(NumericInterval(1, 2)))
        store.read(a2) should be (JOptionA.Some(NumericInterval(5, 6)))

        // Third iteration of while body
        effectStack.joinComputations {
          val a3 = store.alloc(ctx)
          store.write(a3, NumericInterval(8, 9))
          store.read(a1) should be(JOptionA.Some(NumericInterval(1, 6)))
          store.read(a2) should be(JOptionA.Some(NumericInterval(1, 6)))
          store.read(a3) should be(JOptionA.Some(NumericInterval(8, 9)))
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

  test("Reaching fixpoint") {
    val store = emptyStore
    val ctx1 = "ctx1"

    val a1 = store.alloc(ctx1)
    val a2 = store.alloc(ctx1)
    store.write(a1, NumericInterval(1, 4))
    store.write(a2, NumericInterval(3, 4))
    val state1 = (store.getState, store.getAddressTranslation.getState)
    val a3 = store.alloc(ctx1)
    store.write(a3, NumericInterval(3, 4))
    val state2 = (store.getState, store.getAddressTranslation.getState)
    val joinStore = store.join(state1._1, state2._1)
    val joinAddrTrans = store.getAddressTranslation.join(state1._2, state2._2)
    store.setState(joinStore.get)
    store.getAddressTranslation.setState(joinAddrTrans.get)

    a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Old))
    a2.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent), PhysicalAddress(ctx1, Old))
    a3.physical shouldBe PowersetAddr(PhysicalAddress(ctx1, Recent))
    joinStore.hasChanged should be(false)
    joinAddrTrans.hasChanged should be(false)
  }

  test("Example 1 in \"Revisiting Recency Abstraction for JavaScript\" with Addr = AllocSite x Recency") {
    val store = emptyStore
    val effectStack: EffectStack = new EffectStack(List(store, store.addressTranslation))
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

    a1.physical shouldBe PowersetAddr(PhysicalAddress(l0, Recent))
    a2.physical shouldBe PowersetAddr(PhysicalAddress(l3, Recent))
    store.read(a1) should beGreaterThan(JOptionA.Some(NumericInterval(2, 2)))
    store.read(a2) should be(JOptionA.Some(NumericInterval(1, 1)))
  }

  test("Example 1 in \"Revisiting Recency Abstraction for JavaScript\" with Addr = Unit x Recency") {
    val store = emptyStore
    val effectStack: EffectStack = new EffectStack(List(store, store.getAddressTranslation))
    var a1: VirtualAddress[Ctx] = null
    var a2: VirtualAddress[Ctx] = null

    val ctx = "Unit"
    /* l0: */ a1 = store.alloc(ctx); store.write(a1, NumericInterval(1, 1))
    /* l1: */ effectStack.joinComputations {
      /* l2: */ store.write(a1, NumericInterval(2, 2))
      /* l3: */ a2 = store.alloc(ctx); store.write(a2, NumericInterval(1, 1))
    } {
    }

    a1.physical shouldBe PowersetAddr(PhysicalAddress(ctx, Recent), PhysicalAddress(ctx, Old))
    a2.physical shouldBe PowersetAddr(PhysicalAddress(ctx, Recent))
    store.read(a1) should be(JOptionA.Some(NumericInterval(1, 2)))
    store.read(a2) should be(JOptionA.Some(NumericInterval(1, 1)))
  }

  def beGreaterThan[A: Join](right: JOptionA[A]) = new Matcher[JOption[WithJoin,A]] {
    def apply(left: JOption[WithJoin,A]): MatchResult =
      left match
        case l: JOptionA[A] =>
          MatchResult(
            // l >= r   iff   l join r  has not grown
            ! Join(l, right).hasChanged,
            s"${left} not greater than ${right}",
            s"${left} greater than ${right}"
          )
        case _ =>
          MatchResult(false, s"${left} not of type JOptionA", s"${left} of type JOptionA")
  }