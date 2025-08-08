package sturdy.effect.array

import sturdy.data
import sturdy.data.{JOption, MayJoin}
import sturdy.effect.failure.FailureKind

/**
 * Array operations for a managed memory model, i.e.,
 * allocated arrays are initialized to null and are deallocated by a garbage collector.
 */
trait ManagedArrayOps[Array, Size, Index, Value, J[_] <: MayJoin[_]]:
  def alloc(size: Size): Array
  def read(array: Array, index: Index): JOption[J, Value]
  def write(array: Array, index: Index, value: Value): Unit

/** Occurs when array is accessed outside its bounds. */
case object ArrayIndexOutOfBounds extends FailureKind

/** Occurs when a new array is to be allocated, but the program runs out of memory. */
case object OutOfMemory extends FailureKind