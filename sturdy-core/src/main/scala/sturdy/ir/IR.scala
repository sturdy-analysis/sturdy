package sturdy.ir

import sturdy.values.{Abstractly, Join, MaybeChanged, PartialOrder}

import scala.collection.mutable

trait IROperator
trait IRCheck[C]:
  def assert(c: C): C

case class SoundnessCheck[C,A](abs: Abstractly[C,A], unsafe: A, po: PartialOrder[A]) extends IRCheck[C]:
  override def assert(c: C): C =
    val a = abs(c)
    if (po.lteq(a, unsafe)) {
      c
    } else {
      throw new AssertionError(s"Unsound assumption $unsafe for run-time value $c, which abstracts to $a")
    }

enum IR:
  val uid = new IR_UID
  case Unknown()
  case External(name: String)
  case Const[C](c: C)
  case Op(op: IROperator, args: Seq[IR])
  case Select(cond: IR, left: IR, right: IR)
  case Join(left: IR, right: IR)
  case Feedback(init: IR, var cond: Option[IR], var loop: Option[IR])
  case Cast[C](ir: IR, check: IRCheck[C])

  override def hashCode(): Int = uid.hashCode()
  override def equals(obj: Any): Boolean = obj match
    case that: IR => this.uid == that.uid
    case _ => false

  def predecessors: Seq[(IR, String)] = this match
    case IR.Unknown() => Seq.empty
    case IR.External(name) => Seq.empty
    case IR.Const(c) => Seq.empty
    case IR.Op(op, args) => args.zipWithIndex.map(a => a._1 -> a._2.toString)
    case IR.Select(cond, left, right) => Seq(cond -> "?", left -> "⊤", right -> "⊥")
    case IR.Join(left, right) => Seq(left -> "", right -> "")
    case IR.Feedback(init, cond, loop) => Seq(init -> "init") ++ cond.map(_ -> "cond") ++ loop.map(_ -> "loop")

  override def toString: String = this match
    case IR.Unknown() => s"Unknown@$uid"
    case IR.External(name) => s"External($name)@$uid"
    case IR.Const(c) => s"Const($c)@$uid"
    case IR.Op(op, args) => s"Op($op)@$uid"
    case IR.Select(cond, left, right) => s"Select@$uid"
    case IR.Join(left, right) => s"Join@$uid"
    case IR.Feedback(init, cond, loop) => s"Feedback@$uid"

  def foreach(f: IR => Unit): Unit =
    val visited = mutable.Set[IR]()
    val stack = mutable.Stack[IR](this)

    while (stack.nonEmpty) {
      val node = stack.pop()
      f(node)
      visited += node
      for ((p,l) <- node.predecessors) {
        if (!visited(p))
          stack.push(p)
      }
    }

  def externals: Set[String] =
    var names = Set.empty[String]
    foreach {
      case IR.External(name) => names += name
      case _ => ()
    }
    names

object IR:
  def Op(op: IROperator, arg: IR, args: IR*): IR.Op =
    IR.Op(op, arg +: args)


class IRJoin extends Join[IR]:
  override def apply(v1: IR, v2: IR): MaybeChanged[IR] = MaybeChanged.Changed(IR.Join(v1, v2))

class SelectJoin(cond: IR) extends Join[IR]:
  def apply(v1: IR, v2: IR): MaybeChanged[IR] = MaybeChanged.Changed(IR.Select(cond, v1, v2))

class IR_UID:
  override def toString: String = Integer.toHexString(hashCode)