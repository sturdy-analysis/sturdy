package sturdy.ir

import org.scalacheck.rng.Seed
import sturdy.values.booleans.ConcreteIntBools
import sturdy.ir.IR.{FixResultSmart, mkNot}
import sturdy.values.MaybeChanged.{Changed, Unchanged}
import sturdy.values.booleans.IRBooleanOperator
import sturdy.values.integer.IRIntegerOperator
import sturdy.values.ordering.IROrderingOperator
import sturdy.values.{Abstractly, Join, MaybeChanged, PartialOrder, PathSensitive, Top}

import scala.collection.immutable.SortedMap
import scala.collection.mutable

trait IROperator
trait IRBinaryOperator(val infix: String) extends IROperator
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

trait Target[A]:
  var target: Option[A] = None

enum IR:
  val uid = new IR_UID
  case Bot()
  case Unknown()
  case Undefined()
  case External(name: String)
  case Const[C](c: C)
  case Op(op: IROperator, args: Seq[IR])
  case Select(cond: IR, left: IR, right: IR)
  case Join(left: IR, right: IR)
  case Assert(cond: IR, data: IR)
  case Fix(inits: Map[String, IR], steps: Map[String, IR], loopWhileAny: List[IR])
  case Fixvar(name: String) extends IR, Target[Fix]
  case Fixresult(fix: Fix, name: String)
  case Feedback(inits: List[IR], var cond: Option[IR], var steps: Option[List[IR]])
  case FeedbackAsk(index: Int, feedback: Feedback)
//  case Cast[C](ir: IR, check: IRCheck[C])

//  override def hashCode(): Int = uid.hashCode()
//  override def equals(obj: Any): Boolean = obj match
//    case that: IR => this.uid == that.uid
//    case _ => false

  def resolveFix(): Unit = {
    var fixvars: Map[String, IR.Fix] = Map()
    def resolve(ir: IR): Unit = ir match
      case fix@IR.Fix(inits, steps, loopWhileAny) =>
        inits.foreach((k, v) => resolve(v))
        val old = fixvars
        fixvars = fixvars ++ inits.keys.map(k => k -> fix)
        steps.foreach((k, v) => resolve(v))
        loopWhileAny.foreach(resolve)
        fixvars = old
      case v@IR.Fixvar(name) =>
        v.target = fixvars.get(name)
      case IR.Fixresult(fix, _) =>
        resolve(fix)
      case IR.Select(cond, left, right) =>
        resolve(cond)
        resolve(left)
        resolve(right)
      case IR.Join(left, right) =>
        resolve(left)
        resolve(right)
      case IR.Assert(cond, data) =>
        resolve(cond)
        resolve(data)
      case IR.Feedback(inits, Some(cond), Some(steps)) => ()
      case IR.FeedbackAsk(_, feedback) =>
        resolve(feedback)
      case IR.Op(_, args) =>
        args.foreach(resolve)
      case IR.Unknown() | IR.Undefined() | IR.External(_) | IR.Const(_) | IR.Bot() => ()
    resolve(this)
  }

  def predecessors: Seq[(IR, String)] = this match
    case IR.Unknown() => Seq.empty
    case IR.Undefined() => Seq.empty
    case IR.External(name) => Seq.empty
    case IR.Const(c) => Seq.empty
    case IR.Bot() => Seq.empty
    case IR.Op(op, args) => args.zipWithIndex.map(a => a._1 -> a._2.toString)
    case IR.Select(cond, left, right) => Seq(cond -> "?", left -> "⊤", right -> "⊥")
    case IR.Join(left, right) => Seq(left -> "", right -> "")
    case IR.Assert(cond, data) => Seq(cond -> "?", data -> "")
    case IR.Feedback(inits, cond, steps) => inits.zipWithIndex.map((ir, i) => ir ->  s"init_$i") ++ cond.map(_ -> "cond") ++ steps.map(_.zipWithIndex.map((ir, i) => ir ->  s"step_$i")).getOrElse(List.empty)
    case IR.FeedbackAsk(_, feedback) => Seq(feedback -> "feedback")
    case fix@IR.Fix(inits, steps, loopWhileAny) =>
      val initPreds = inits.toSeq.map((k, v) => v -> s"init_$k")
      val stepPreds = steps.toSeq.map((k, v) => v -> s"step_$k")
      val condPreds = if (loopWhileAny.size != 1) {
        println(s"Warning: Fix should be normalized before export.")
        loopWhileAny.zipWithIndex.map((c, i) => c -> s"cond_$i")
      } else {
        loopWhileAny.headOption.map(c => c -> "cond").toSeq
      }
      initPreds ++ stepPreds ++ condPreds
    case v@IR.Fixvar(name) =>
      val target = v.target.getOrElse(throw new UnsupportedOperationException(s"Cannot get predecessors of unbound fixvar $name"))
      Seq(target -> "")
    case IR.Fixresult(fix, _) => Seq(fix -> "")

  def toString(bound: Int): String =
    if (bound <= 0)
      nodeString
    else this match
      case IR.Unknown() => s"Unknown"
      case IR.Undefined() => s"Undefined"
      case IR.External(name) => s"Ext_$name"
      case IR.Const(c) => s"$c"
      case IR.Op(bop: IRBinaryOperator, Seq(a1, a2)) => s"(${a1.toString(bound - 1)} ${bop.infix} ${a2.toString(bound - 1)})"
      case IR.Op(op, args) => s"Op($op, [${args.map(_.toString(bound - 1)).mkString(", ")}])"
      case IR.Select(cond, left, right) => s"Select(${cond.toString(bound - 1)}, ${left.toString(bound - 1)}, ${right.toString(bound - 1)})"
      case IR.Join(left, right) => s"Join(${left.toString(bound - 1)}, ${right.toString(bound - 1)})"
      case IR.Assert(cond, data) => s"Assert(${cond.toString(bound - 1)}, ${data.toString(bound - 1)})"
      case IR.Feedback(inits, cond, steps) =>
        s"Feedback@$uid([${inits.map(_.toString(bound - 1)).mkString(", ")}], [${steps.map(_.map(_.toString(bound - 1)).mkString(", ")).getOrElse("")}], ${cond.map(_.toString(bound - 1)).getOrElse("None")})"
      case IR.FeedbackAsk(index, feedback) => s"Feedback@${feedback.uid}_$index"
      case IR.Bot() => s"Bot"
      case IR.Fix(inits, steps, cond) =>
        s"Fix@$uid({${inits.map((k, v) => s"$k -> ${v.toString(bound - 1)}").mkString(", ")}}," +
          s" {${steps.map((k, v) => s"$k -> ${v.toString(bound - 1)}").mkString(", ")}}, " +
          s"${cond.map(_.toString(bound - 1)).mkString(" || ")})"
      case IR.Fixvar(name) => s"Fix_$name"
      case IR.Fixresult(fix, name) => s"Fixed_$name@${fix.uid}"

  override def toString: String = toString(10)

  def nodeString: String = this match
    case IR.Unknown() => s"Unknown@$uid"
    case IR.Undefined() => s"Undefined@$uid"
    case IR.External(name) => s"Ext_$name@$uid"
    case IR.Const(c) => s"Const($c)@$uid"
    case IR.Op(op, _) => s"Op($op)@$uid"
    case IR.Select(_, _, _) => s"Select@$uid"
    case IR.Join(_, _) => s"Join@$uid"
    case IR.Assert(_, _) => s"Assert@$uid"
    case IR.Feedback(_, _, _) => s"Feedback@$uid"
    case IR.FeedbackAsk(index, _)  => s"FeedbackAsk($index)@$uid"
    case IR.Bot() => s"Bot@$uid"
    case IR.Fix(_, _, _) => s"Fix@$uid"
    case v@IR.Fixvar(name) => s"Fix_$name@${v.target.map(_.uid).getOrElse("?")}"
    case IR.Fixresult(fix, name) => s"Fixed_$name@${fix.uid}"

  def structuralEquality(that: IR): Boolean = (this, that) match
    case _ if this == that => true
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
    case (IR.Fixvar(name1), IR.Fixvar(name2)) if name1 == name2 => true
    case (IR.Fix(inits1, steps1, cond1), IR.Fix(inits2, steps2, cond2)) if inits1.keySet == inits2.keySet && steps1.keySet == steps2.keySet && cond1.size == cond2.size =>
      inits1.forall((k, v) => v.structuralEquality(inits2(k))) &&
      steps1.forall((k, v) => v.structuralEquality(steps2(k))) &&
      cond1.zip(cond2).forall(p => p._1.structuralEquality(p._2))
    case (IR.Fixresult(fix1, name1), IR.Fixresult(fix2, name2)) if name1 == name2 && fix1.structuralEquality(fix2) => true
    case _ => false

  def map(f: IR => IR): IR = this match {
    case IR.Bot() => f(this)
    case IR.Unknown() => f(this)
    case IR.Undefined() => f(this)
    case IR.External(name) => f(this)
    case IR.Const(c) => f(this)
    case IR.Op(op, args) => f(IR.Op(op, args.map(_.map(f))))
    case IR.Select(cond, left, right) => f(IR.Select(cond.map(f), left.map(f), right.map(f)))
    case IR.Join(left, right) => f(IR.Join(left.map(f), right.map(f)))
    case IR.Assert(cond, data) => f(IR.Assert(cond.map(f), data.map(f)))
    case IR.Fix(inits, steps, cond) => f(IR.Fix(inits.map((k, v) => k -> v.map(f)), steps.map((k, v) => k -> v.map(f)), cond.map(f)))
    case IR.Fixvar(name) => f(this)
    case IR.Fixresult(fix, name) => f(IR.Fixresult(fix.map(f).asInstanceOf[IR.Fix], name))
    case IR.Feedback(inits, cond, steps) => throw new UnsupportedOperationException()
    case IR.FeedbackAsk(index, feedback) => throw new UnsupportedOperationException()
  }

  protected def norm(path: Set[IR], f: (Set[IR], IR) => IR): IR = this match {
    case IR.Bot() => f(path, this)
    case IR.Unknown() => f(path, this)
    case IR.Undefined() => f(path, this)
    case IR.External(name) => f(path, this)
    case IR.Const(c) => f(path, this)
    case IR.Op(op, args) => f(path, IR.Op(op, args.map(_.norm(path, f))))
    case IR.Select(cond, left, right) =>
      val condN = cond.norm(path, f)
      val oldPath = path
      val leftN = left.norm(oldPath + condN, f)
      val rightN = right.norm(oldPath + mkNot(condN), f)
      f(path, IR.Select(condN, leftN, rightN))
    case IR.Join(left, right) => f(path, IR.Join(left.norm(path, f), right.norm(path, f)))
    case IR.Assert(cond, data) => f(path, IR.Assert(cond.norm(path, f), data.norm(path, f)))
    case IR.Fix(inits, steps, cond) =>
      val condN = cond.map(_.norm(path, f))
      f(path, IR.Fix(inits.map((k, v) => k -> v.norm(path, f)), steps.map((k, v) => k -> v.norm(path ++ condN, f)), condN))
    case IR.Fixvar(name) => f(path, this)
    case IR.Fixresult(fix, name) => f(path, IR.Fixresult(fix.norm(path, f).asInstanceOf[IR.Fix], name))
    case IR.Feedback(inits, cond, steps) => throw new UnsupportedOperationException()
    case IR.FeedbackAsk(index, feedback) => throw new UnsupportedOperationException()
  }

  def normalize: IR = normalizeLoop(Set.empty)

  def isRecursive: Boolean =
    var isRecursive = false
    this.foreachTree { case IR.Fixvar(_) => isRecursive = true; case _ => () }
    isRecursive


  protected def normalizeLoop(path: Set[IR]): IR =
    norm (path, { (path, ir) =>
      ir match {

        case IR.Fix(inits, steps, loopWhileAny) if loopWhileAny.exists(c => !c.isRecursive && c.tryDecide(path).contains(true)) =>
          IR.Bot() // loop will always diverge
        case IR.Fix(inits, steps, loopWhileAny) if loopWhileAny.exists(c => !c.isRecursive && c.tryDecide(path).contains(false)) =>
          val newconds = loopWhileAny.filter(c => c.isRecursive || !c.tryDecide(path).contains(false))
          IR.Fix(inits, steps, newconds).normalizeLoop(path)

        case ir@IR.Fixresult(IR.Fix(inits, steps, List()), name) =>
          inits(name).normalizeLoop(path)
        case ir@IR.Fixresult(IR.Fix(inits, steps, loopWhileAny), name) if loopWhileAny.forall(!_.isRecursive) =>
          IR.Select(IR.mkOr(loopWhileAny), IR.Bot(), inits(name)).normalizeLoop(path)
        case IR.Select(cond, left, right) if left == right => left
        case IR.Select(cond, left, right) =>
          cond.tryDecide(path) match
            case Some(true) =>
              left
            case Some(false) =>
              right
            case None =>
              println(s"Select $cond\n  path $path")
              ir
        case ir => ir
      }
    })

  def tryDecide(path: Set[IR]): Option[Boolean] = this match {
    case Const(0 | false) => Some(false)
    case Const(_) => Some(true)
    case IR.Op(IRBooleanOperator.NOT, Seq(inner)) =>
      inner.tryDecide(path).map(b => !b)
    case cond if path.contains(cond) => Some(true)
    case cond if path.contains(mkNot(cond)) => Some(false)
    case IR.Op(IROrderingOperator.LE, Seq(IR.Op(IRIntegerOperator.SUB, Seq(a, IR.Const(1))), b)) =>
      IR.Op(IROrderingOperator.LT, a, b).tryDecide(path)
    case IR.Op(IROrderingOperator.LT, Seq(a, b)) if path.contains(IR.Op(IROrderingOperator.LT, Seq(b, a))) => Some(false)
    case IR.Op(IROrderingOperator.LE, Seq(IR.Fixresult(fix, name), b)) =>
      val subir = IR.Op(IROrderingOperator.LE, Seq(IR.Fixvar(name), b))
      val extra = fix.loopWhileAny.map(mkNot)
      subir.tryDecide(path ++ fix.loopWhileAny.map(mkNot))
    case _ => None
  }

  def dedup: IR =
    var seen: Map[IR, IR] = Map()
    this map { ir =>
      seen.get(ir) match
        case Some(existing) =>
//          println(s"Deduping $ir")
          existing
        case None =>
          seen += ir -> ir
          ir
    }

  def foreachTree(f: IR => Unit): Unit = this match {
    case IR.Bot() => f(this)
    case IR.Unknown() => f(this)
    case IR.Undefined() => f(this)
    case IR.External(name) => f(this)
    case IR.Const(c) => f(this)
    case IR.Op(op, args) => args.foreach(_.foreachTree(f)); f(this)
    case IR.Select(cond, left, right) => cond.foreachTree(f); left.foreachTree(f); right.foreachTree(f); f(this)
    case IR.Join(left, right) => left.foreachTree(f); right.foreachTree(f); f(this)
    case IR.Assert(cond, data) => cond.foreachTree(f); data.foreachTree(f); f(this)
    case IR.Fix(inits, steps, loopWhileAny) =>
      inits.values.foreach(_.foreachTree(f))
      steps.values.foreach(_.foreachTree(f))
      loopWhileAny.foreach(_.foreachTree(f))
      f(this)
    case IR.Fixvar(name) => f(this)
    case IR.Fixresult(fix, name) => fix.foreachTree(f); f(this)
    case IR.Feedback(inits, cond, steps) => throw new UnsupportedOperationException()
    case IR.FeedbackAsk(index, feedback) => throw new UnsupportedOperationException()
  }

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
    foreachTree {
      case IR.External(name) => names += name
      case _ => ()
    }
    names

  def testIsLeq(other: IR, cond: IR): Unit =
    import org.scalacheck.Gen.Choose
    import org.scalacheck.{Arbitrary, Gen, Shrink, Prop, Test}

    val extVariables = this.externals ++ other.externals ++ cond.externals
    val genVals = Gen.containerOfN[List, IRValue](extVariables.size, Arbitrary.arbitrary[Int].map(IRValue.apply))
    val genOkVals = genVals.filter { vals =>
      val extValues = extVariables.zip(vals).toMap
      val interp = new IRInterpreterConcrete[Int](extValues, () => throw new IllegalStateException("Should not happen"))
      interp.run(cond) match
        case Some(IRValue(false | 0)) => false
        case _ => true
    }

    val p = Prop.forAll(genOkVals) { vals =>
      val extValues = extVariables.zip(vals).toMap
      isLeq(other)(extValues)
    }

    val res = Test.check(Test.Parameters.default, p)
    res.status match {
      case Test.Passed => // ok
      case Test.Proved(args) => // ok
      case Test.Failed(args, labels) =>

        val vals = args.head.arg.asInstanceOf[List[IRValue]]
        val extValues = extVariables.zip(vals).toMap
        val interp = new IRInterpreterConcrete[Int](extValues, () => throw new IllegalStateException("Should not happen"))
        val thisValue = interp.run(this)
        val otherValue = interp.run(other)
        throw new AssertionError(s"IsLeq Failed for $this <= $other.\n  arguments $extValues\n  this evaluates to $thisValue\n  other evaluates to $otherValue")
      case Test.Exhausted =>
        // throw new AssertionError(s"IsLeq Failed for $this <= $other. Failed to generate sufficient test inputs.")
      case Test.PropException(args, e, labels) =>
        throw new AssertionError(s"IsLeq Failed for $this <= $other. Exception during test generation: ${e.getMessage}", e)
    }


  def isLeq(other: IR)(extValues: Map[String, IRValue]): Boolean =
    val extVariables = this.externals ++ other.externals
    if (!extVariables.subsetOf(extValues.keySet))
      throw new IllegalArgumentException(s"Missing values for externals: ${extVariables.diff(extValues.keySet)}")
    val interp = new IRInterpreterConcrete[Int](extValues, () => throw new IllegalStateException("Should not happen"))
    val thisValue = interp.run(this)
    val otherValue = interp.run(other)
    (thisValue, otherValue) match
      case (None, _) => true
      case (Some(_), None) => false
      case (Some(v1), Some(v2)) => v1 == v2


object IR:
  def Op(op: IROperator, arg: IR, args: IR*): IR.Op =
    IR.Op(op, arg +: args)
  def SelectSmart(cond: IR, v1: IR, v2: IR): IR =
    if (v1.structuralEquality(v2))
      v1
    else
      IR.Select(cond, v1, v2)
  def FixResultSmart(fix: Fix, name: String): IR =
    if (fix.loopWhileAny.isEmpty)
      fix.inits(name)
    else
      Fixresult(fix, name)

  def mkNot(ir: IR): IR = ir match {
    case IR.Const(b: Boolean) => IR.Const(!b)
    case IR.Op(IRBooleanOperator.NOT, Seq(inner)) => inner
    case IR.Op(IROrderingOperator.LE, Seq(e1, e2)) => IR.Op(IROrderingOperator.LT, e2, e1)
    case IR.Op(IROrderingOperator.LT, Seq(e1, e2)) => IR.Op(IROrderingOperator.LE, e2, e1)
    case _ => IR.Op(IRBooleanOperator.NOT, ir)
  }

  def reduce(irs : List[IR])(default: IR, f: (IR, IR) => IR): IR =
    irs.size match
      case 0 => default
      case 1 => irs.head
      case _ => irs.tail.foldLeft(irs.head)(f)

  def mkAnd(irs: List[IR]): IR = reduce(irs)(IR.Const(false), (a, b) => IR.Op(IRBooleanOperator.AND, a, b))
  def mkOr(irs: List[IR]): IR = reduce(irs)(IR.Const(true), (a, b) => IR.Op(IRBooleanOperator.OR, a, b))


given sturdy.values.Join[IR] with
  import IR.*
  def apply(left: IR, right: IR): MaybeChanged[IR] =
    val res = (left, right) match
      case (Unknown(), _) => Changed(left)
      case (_, Unknown()) => Unchanged(right)
      case (Assert(cond1, v1), Assert(Op(IRBooleanOperator.NOT, Seq(cond2)), v2)) if cond1 == cond2 =>
          Changed(SelectSmart(cond1, v1, v2))
      case (Assert(Op(IRBooleanOperator.NOT, Seq(cond1)), v1), Assert(cond2, v2)) if cond1 == cond2 =>
        Changed(SelectSmart(cond2, v2, v1))
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