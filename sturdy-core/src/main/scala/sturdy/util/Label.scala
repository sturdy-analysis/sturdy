package sturdy.util

sealed trait Label
case class IntLabel(i: Int) extends Label:
  override def toString: String = s"L$i"
case class SynLabel(from: Label) extends Label:
  override def toString: String = s"$from$$"

object Labled:
  private var _next = 0
  def next(): Int =
    val n = _next
    _next += 1
    n
  def reset(): Unit = 
    _next = 0

trait Labled:
  private var _label: Label = IntLabel(Labled.next())
  def label: Label = _label
  def @:(l: Label): this.type =
    _label = SynLabel(l)
    this

  override def hashCode(): Int = this.label.hashCode()
  override def equals(obj: Any): Boolean = obj match
    case other: Labled => this.label == other.label
    case _ => false

