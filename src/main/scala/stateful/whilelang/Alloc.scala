package stateful.whilelang

import sturdy.common.Label

trait Alloc[Addr] {
  def alloc(l: Label): Addr
}

trait AllocImpl extends Alloc[Int] {
  var next = 0

  override def alloc(l: Label): Int = {
    val a = next
    next = next + 1
    a
  }
}

trait AllocAbs extends Alloc[Label] {
  override def alloc(l: Label): Label = l
}