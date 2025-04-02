package sturdy.ir

import sturdy.values.MaybeChanged.{Changed, Unchanged}
import sturdy.values.booleans.IRBooleanOperator
import sturdy.values.{Abstractly, Join, MaybeChanged, PartialOrder, PathSensitive, Top}

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
  case Undefined()
  case External(name: String)
  case Const[C](c: C)
  case Op(op: IROperator, args: Seq[IR])
  case Select(cond: IR, left: IR, right: IR)
  case Join(left: IR, right: IR)
  case Assert(cond: IR, data: IR)
  case Feedback(inits: List[IR], var cond: Option[IR], var steps: Option[List[IR]])
  case FeedbackAsk(index: Int, feedback: Feedback)
//  case Cast[C](ir: IR, check: IRCheck[C])

  override def hashCode(): Int = uid.hashCode()
  override def equals(obj: Any): Boolean = obj match
    case that: IR => this.uid == that.uid
    case _ => false

  def predecessors: Seq[(IR, String)] = this match
    case IR.Unknown() => Seq.empty
    case IR.Undefined() => Seq.empty
    case IR.External(name) => Seq.empty
    case IR.Const(c) => Seq.empty
    case IR.Op(op, args) => args.zipWithIndex.map(a => a._1 -> a._2.toString)
    case IR.Select(cond, left, right) => Seq(cond -> "?", left -> "⊤", right -> "⊥")
    case IR.Join(left, right) => Seq(left -> "", right -> "")
    case IR.Assert(cond, data) => Seq(cond -> "?", data -> "")
    case IR.Feedback(inits, cond, steps) => inits.zipWithIndex.map((ir, i) => ir ->  s"init_$i") ++ cond.map(_ -> "cond") ++ steps.map(_.zipWithIndex.map((ir, i) => ir ->  s"step_$i")).getOrElse(List.empty)
    case IR.FeedbackAsk(_, feedback) => Seq(feedback -> "feedback")

  override def toString: String = this match
    case IR.Unknown() => s"Unknown@$uid"
    case IR.Undefined() => s"Undefined@$uid"
    case IR.External(name) => s"External($name)@$uid"
    case IR.Const(c) => s"Const($c)@$uid"
    case IR.Op(op, _) => s"Op($op)@$uid"
    case IR.Select(_, _, _) => s"Select@$uid"
    case IR.Join(_, _) => s"Join@$uid"
    case IR.Assert(_, _) => s"Assert@$uid"
    case IR.Feedback(_, _, _) => s"Feedback@$uid"
    case IR.FeedbackAsk(index, _)  => s"FeedbackAsk($index)@$uid"

  def structuralEquality(that: IR): Boolean = (this, that) match
    case (IR.Unknown(), IR.Unknown()) => true
    case (IR.Undefined(), IR.Undefined()) => true
    case (IR.External(name1), IR.External(name2)) if name1 == name2 => true
    case (IR.Const(c1), IR.Const(c2)) if c1 == c2 => true
    case (IR.Op(op1, args1), IR.Op(op2, args2)) if op1 == op2 && args1.length == args2.length =>
      args1.zip(args2).forall(p => p._1.structuralEquality(p._2))
    case (IR.Select(cond1, left1, right1), IR.Select(cond2, left2, right2)) =>
      cond1.structuralEquality(cond2) &&
      left1.structuralEquality(left2) &&
      right1.structuralEquality(right2)
    case (IR.Join(left1, right1), IR.Join(left2, right2)) => left1.structuralEquality(left2) && right1.structuralEquality(right2)
    case (IR.Assert(cond1, data1), IR.Assert(cond2, data2)) => cond1.structuralEquality(cond2) && data1.structuralEquality(data2)
    case (IR.FeedbackAsk(i1, feedback1), IR.FeedbackAsk(i2, feedback2)) if i1 == i2 && feedback1 == feedback2 => true
    // TODO Add Feedback ? Better guard for cycles ? Does this really work (need tests)
    case (IR.Feedback(_, _, _), IR.Feedback(_, _, _)) if this == that => true
    case _ if this == that => true
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
  def select(cond: IR, v1: IR, v2: IR): IR =
    if (v1.structuralEquality(v2))
      v1
    else
      IR.Select(cond, v1, v2)

given sturdy.values.Join[IR] with
  import IR.*
  def apply(left: IR, right: IR): MaybeChanged[IR] =
    val res = (left, right) match
      case (Unknown(), _) => Changed(left)
      case (_, Unknown()) => Unchanged(right)
      case (Assert(cond1, v1), Assert(Op(IRBooleanOperator.NOT, Seq(cond2)), v2)) if cond1 == cond2 =>
          Changed(select(cond1, v1, v2))
      case (Assert(Op(IRBooleanOperator.NOT, Seq(cond1)), v1), Assert(cond2, v2)) if cond1 == cond2 =>
        Changed(select(cond2, v2, v1))
      case (_, _) if left.structuralEquality(right) =>
        Unchanged(left)
      case _ => Changed(IR.Join(left, right))
//    println(s"join $left\n     $right\n   = $res")
//    println(Export.toGraphViz(left))
//    println(Export.toGraphViz(right))
    res

class IR_UID:
  override def toString: String = Integer.toHexString(hashCode)

given Top[IR] with
  override def top: IR = IR.Unknown()

given PathSensitive[IR] = new PSIR

class PSIR extends PathSensitive[IR]:
  override def assert(cond: Any, v: IR): IR = cond match
    case cond: IR => v match
      case IR.FeedbackAsk(ix, IR.Feedback(_, Some(fbCond), _)) =>
        val ir = cond match
          case _ if cond.structuralEquality(fbCond) => v
          case IR.Op(IRBooleanOperator.NOT, Seq(cond)) if cond.structuralEquality(fbCond) => v
          case _ => IR.Assert(cond, v)
        ir
      case _ => IR.Assert(cond, v)
    case _ => throw new IllegalArgumentException(s"Cannot assert condition $cond on value $v")