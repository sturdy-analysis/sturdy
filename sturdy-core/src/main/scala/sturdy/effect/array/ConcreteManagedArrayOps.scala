package sturdy.effect.array

import sturdy.data.JOptionC
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.Concrete
import sturdy.effect.failure.{Failure}

import scala.collection.mutable
import scala.reflect.ClassTag

case class ArrayPointer(start: Int, size: Int):
  def range: Range = Range.Exclusive(start, start + size, step = 1)

final class ConcreteManagedArrayOps[Value >: Null: ClassTag](memorySize: Int)(using failure: Failure) extends ManagedArrayOps[ArrayPointer, Int, Int, Value, NoJoin], Concrete:
  val allocatedArrays: mutable.Map[Range, ArrayPointer] = mutable.Map[Range, ArrayPointer](Range.Exclusive(start = 0, end = memorySize, step = 1) -> null)
  val memory: mutable.ArraySeq[Value] = mutable.ArraySeq.fill[Value](memorySize)(null)

  override def alloc(size: Int): ArrayPointer =
    allocatedArrays.collectFirst{ case (range, array) if array == null && range.size >= size => range } match
      case Some(freeRange) =>
        val allocatedRange = Range.Exclusive(start = freeRange.start, end = freeRange.start + size, step = 1)
        val residualRange  = Range.Exclusive(start = freeRange.start + size + 1, end = freeRange.end, step = 1)
        allocatedArrays -= freeRange
        allocatedArrays += allocatedRange -> ArrayPointer(start = freeRange.start, size = size)
        for(i <- allocatedRange)
          memory(i) = null
        if(residualRange.nonEmpty) allocatedArrays += residualRange -> null
        allocatedArrays(allocatedRange)

      case None =>
        failure.fail(OutOfMemory, s"Cannot allocate array of size $size")

  override def read(array: ArrayPointer, index: Int): JOptionC[Value] =
    if(index < array.size)
      val result = memory(array.start + index)
      if result == null then
        JOptionC.None()
      else
        JOptionC.Some(result)
    else
      failure.fail(ArrayIndexOutOfBounds, s"Index $index not in bounds of $array")

  override def write(array: ArrayPointer, index: Int, value: Value): Unit =
    if(index > array.size)
      failure.fail(ArrayIndexOutOfBounds, s"Index $index not in bounds of $array")
    else
      memory(array.start + index) = value