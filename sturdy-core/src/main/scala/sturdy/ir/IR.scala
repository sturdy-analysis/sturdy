package sturdy.ir

import sturdy.values.{Abstractly, Join, MaybeChanged, PartialOrder}

import scala.collection.mutable

trait IROperator
//trait IRCheck[C]:
//  def assert(c: C): C
//
//case class SoundnessCheck[C,A](abs: Abstractly[C,A], unsafe: A, po: PartialOrder[A]) extends IRCheck[C]:
//  override def assert(c: C): C =
//    val a = abs(c)
//    if (po.lteq(a, unsafe)) {
//      c
//    } else {
//      throw new AssertionError(s"Unsound assumption $unsafe for run-time value $c, which abstracts to $a")
//    }

enum IR:
  val uid = new IR_UID
  case Unknown()
  case External(name: String)
  case Const[C](c: C)
  case Op(op: IROperator, args: Seq[IR])
  case Select(cond: IR, left: IR, right: IR)
  case Join(left: IR, right: IR)
  case Feedback(inits: List[Option[IR]], var cond: Option[IR], var steps: List[Option[IR]])
  case FeedbackAsk(index: Int, feedback: Feedback)
//  case Cast[C](ir: IR, check: IRCheck[C])

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
    case IR.Feedback(inits, cond, steps) => inits.zipWithIndex.flatMap((ir, i) => ir.map(_ -> s"init_$i")) ++ cond.map(_ -> "cond") ++ steps.zipWithIndex.flatMap((ir, i) => ir.map(_ -> s"step_$i"))
    case IR.FeedbackAsk(_, feedback) => Seq(feedback -> "feedback")

  override def toString: String = this match
    case IR.Unknown() => s"Unknown@$uid"
    case IR.External(name) => s"External($name)@$uid"
    case IR.Const(c) => s"Const($c)@$uid"
    case IR.Op(op, _) => s"Op($op)@$uid"
    case IR.Select(_, _, _) => s"Select@$uid"
    case IR.Join(_, _) => s"Join@$uid"
    case IR.Feedback(_, _, _) => s"Feedback@$uid"
    case IR.FeedbackAsk(index, _)  => s"FeedbackAsk($index)@$uid"

  def structuralEquality(that: IR): Boolean = (this, that) match
    case (IR.Unknown(), _) => false
    case (_, IR.Unknown()) => false
    case (IR.External(name1), IR.External(name2)) if name1 == name2 => true
    case (IR.Const(c1), IR.Const(c2)) if c1 == c2 => true
    case (IR.Op(op1, args1), IR.Op(op2, args2)) if op1 == op2 && args1.length == args2.length =>
      args1.zip(args2).forall( p => p._1.structuralEquality(p._2))
    case (IR.Select(cond1, left1, right1), IR.Select(cond2, left2, right2)) =>
      cond1.structuralEquality(cond2) &&
      left1.structuralEquality(left2) &&
      right1.structuralEquality(right2)
    case (IR.Join(left1, right1), IR.Join(left2, right2)) => left1.structuralEquality(left2) && right1.structuralEquality(right2)
    case (IR.FeedbackAsk(i1, feedback1), IR.FeedbackAsk(i2, feedback2)) if i1 == i2 && feedback1 == feedback2 => true
    // TODO Add Feedback ? Better guard for cycles ?
    case (IR.Feedback(_, _, _), IR.Feedback(_, _, _)) if this == that => true
    case _ => false

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
  override def apply(v1: IR, v2: IR): MaybeChanged[IR] = MaybeChanged.Changed(IR.Select(cond, v1, v2))

class IR_UID:
  override def toString: String = Integer.toHexString(hashCode)