package sturdy.effect.allocation

import scala.collection.mutable.ListBuffer

trait CAllocationIntIncrement[Context] extends Allocation[Int, Context]:
  private var next = 0

  private val addressContexts: ListBuffer[(Int, Context)] = ListBuffer()
  def getAddressContexts: List[(Int, Context)] = addressContexts.toList

  override def alloc(ctx: Context): Int =
    val a = next
    next = next + 1
    addressContexts += a -> ctx
    a
