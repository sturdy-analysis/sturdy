package sturdy.common

sealed trait Label
case class IntLabel(i: Int) extends Label
case class SynLabel(from: Label) extends Label

object Labeled {
  private var _next = 0
  def next(): Int = {
    val n = _next
    _next += 1
    n
  }
}

trait Labeled {
  private var _label: Label = IntLabel(Labeled.next)
  def label: Label = _label
  def <@@(l: Label): this.type = {
    _label = SynLabel(l)
    this
  }
}
