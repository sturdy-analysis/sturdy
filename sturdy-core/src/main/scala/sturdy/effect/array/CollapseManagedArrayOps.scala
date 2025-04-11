package sturdy.effect.array

import sturdy.data.{JOption, JOptionA, MayJoin, WithJoin}
import sturdy.effect.allocation.Allocator
import sturdy.values.{Finite, Join, Topped, Widen}
import sturdy.data.given
import sturdy.effect.{Effect, EffectStack}
import sturdy.effect.failure.Failure
import sturdy.values.ordering.OrderingOps

import scala.collection.immutable

case class AbstractArrayPointer[Site, Size](allocationSite: Site, size: Size)

// Abstract array operations that collapse the entire content of the array by joining it into a single value
given CollapseManagedArrayOps[Site: Finite, Index, Size <: Index, Value: Join: Widen]
                      (using
                       allocator: Allocator[AbstractArrayPointer[Site, Size], Size],
                       orderingOps: OrderingOps[Index, Topped[Boolean]],
                       effectStack: EffectStack,
                       failure: Failure
                      ): ManagedArrayOps[AbstractArrayPointer[Site, Size], Size, Index, Value, WithJoin] with Effect with
  var memory: immutable.Map[Site, Value] = immutable.Map.empty

  override def alloc(size: Size): AbstractArrayPointer[Site, Size] =
    effectStack.joinWithFailure {
      val array = allocator(size)
      array
    } {
      failure.fail(OutOfMemory, s"Cannot allocate array of size $size")
    }

  override def read(array: AbstractArrayPointer[Site, Size], index: Index): JOptionA[Value] =
    ifTrueElseFail(orderingOps.lt(index, array.size)) {
      if memory(array.allocationSite) == null then
        JOptionA.None()
      else
        JOptionA.NoneSome(memory(array.allocationSite))
    } {
      failure.fail(ArrayIndexOutOfBounds, s"Index $index not in bounds of $array")
    }

  override def write(array: AbstractArrayPointer[Site, Size], index: Index, value: Value): Unit =
    val joinedVal = Join(memory.get(array.allocationSite), Some(value)).get.get
    ifTrueElseFail(orderingOps.lt(index, array.size)) {
      memory += array.allocationSite -> joinedVal
    } {
      failure.fail(ArrayIndexOutOfBounds, s"Index $index not in bounds of $array")
    }

  inline private def ifTrueElseFail[A:Join](toppedBool: Topped[Boolean])(f: => A)(g: => Nothing): A =
    toppedBool match
      case Topped.Actual(true) =>
        f
      case Topped.Actual(false) =>
        g
      case Topped.Top =>
        effectStack.joinWithFailure {
          f
        } {
          g
        }

  type State = immutable.Map[Site, Value]

  override def getState: State = memory
  override def setState(state: State): Unit = memory = state
  override def join: Join[State] = JoinMap
  override def widen: Widen[State] = WidenFiniteKeyMap