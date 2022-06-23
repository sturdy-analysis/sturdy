package sturdy.effect.allocation

import sturdy.effect.Concrete

import scala.collection.mutable.ListBuffer

class CAllocationIntIncrement[Context] extends Allocation[Int, Context], Concrete:
  private var next = 0

  private val addressContexts: ListBuffer[(Int, Context)] = ListBuffer()
  def getAddressContexts: List[(Int, Context)] = addressContexts.toList

  override def apply(ctx: Context): Int =
    val a = next
    next = next + 1
    addressContexts += a -> ctx
    a
