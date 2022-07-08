package sturdy.values

import scala.collection.mutable.ListBuffer

class Indent(val spaces: Int) extends AnyVal:
  def spaceString: String = " " * spaces

object Indent:
  def line(s: String)(using ind: Indent): String = ind.spaceString + s
  def increased[A](f: Indent ?=> A)(using ind: Indent): A = f(using new Indent(ind.spaces + 2))
  def increasedLine(s: String)(using Indent): String = increased(line(s))


trait Tree:
  def prettyPrint(using Indent): String
  override def toString: String = prettyPrint(using new Indent(0))

case class TreeList(l: List[Tree]) extends Tree:
  override def prettyPrint(using Indent): String = l.map(_.prettyPrint).mkString("List(", ", ", ")")

case class TreeAlt(l: Tree, r: Tree) extends Tree:
  override def prettyPrint(using Indent): String = s"alt(${l.prettyPrint}, ${r.prettyPrint})"

class TreeBuffer:
  private val buf = new ListBuffer[Tree]
  def +=(x: Tree): x.type = {
    buf += x
    x
  }
  def ++=(xs: IterableOnce[Tree]): Unit = buf ++= xs
  def result: List[Tree] = buf.toList
  def mark: Int = buf.size
  def cut(mark: Int): List[Tree] =
    val after = buf.takeRight(buf.size - mark)
    buf.takeInPlace(mark)
    after.toList
  def clear(): this.type =
    buf.clear()
    this
