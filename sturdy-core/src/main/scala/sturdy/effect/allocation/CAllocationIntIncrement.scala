package sturdy.effect.allocation

import sturdy.effect.Concrete

import scala.collection.mutable.ListBuffer

class CAllocationIntIncrement[Site] extends Allocation[Int, Site], Concrete:
  private var next = 0

  private val addressContexts: ListBuffer[(Int, Site)] = ListBuffer()
  def getAddressContexts: List[(Int, Site)] = addressContexts.toList

  override def apply(site: Site): Int =
    val a = next
    next = next + 1
    addressContexts += a -> site
    a
