package stateful.whilelang

trait Alloc[Addr] {
  def alloc: Addr
}

trait AllocImpl extends Alloc[Int] {
  var next = 0

  override def alloc: Int = {
    val a = next
    next = next + 1
    a
  }
}
