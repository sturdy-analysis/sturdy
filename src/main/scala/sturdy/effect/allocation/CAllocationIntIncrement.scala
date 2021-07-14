package sturdy.effect.allocation

trait CAllocationIntIncrement extends Allocation[Int, Any]:
  var next = 0

  override def alloc(ctx: Any): Int =
    val a = next
    next = next + 1
    a
