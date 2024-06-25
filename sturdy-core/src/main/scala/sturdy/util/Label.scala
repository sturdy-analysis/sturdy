package sturdy.util

sealed trait Label
case class IntLabel(i: Int) extends Label:
  override def toString: String = s"L$i"
case class SynLabel(from: Label) extends Label:
  override def toString: String = s"$from$$"

given Ordering[Label] = {
  case (IntLabel(i1), IntLabel(i2)) => Ordering[Int].compare(i1, i2)
  case (SynLabel(s1), SynLabel(s2)) => Ordering[Label].compare(s1, s2)
  case (IntLabel(_), SynLabel(_)) => 1
  case (SynLabel(_), IntLabel(_)) => -1
}

object Labeled:
  private var _next = 0
  def next(): Int =
    val n = _next
    _next += 1
    n
  def reset(): Unit =
    _next = 0

trait Labeled:
  private var _label: Label = IntLabel(Labeled.next())
  def label: Label = _label
  def @:(l: Label): this.type =
    _label = SynLabel(l)
    this

  override def hashCode(): Int = this.label.hashCode()
  override def equals(obj: Any): Boolean = obj match
    case other: Labeled => this.label == other.label
    case _ => false

