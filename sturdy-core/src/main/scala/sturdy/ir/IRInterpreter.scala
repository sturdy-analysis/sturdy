package sturdy.ir

import scala.annotation.tailrec


case class IRValue(c: Any)

class IRInterpreter(val externals: Map[String, IRValue]) {

  var feedbackStore: Map[IR_UID, IRValue] = Map()

  def interpret(ir: IR): IRValue = ir match
    case IR.Unknown() => ???
    case IR.External(name) => externals(name)
    case IR.Const(c) => IRValue(c)
    case IR.Op(op, args) => op.eval(args.map(interpret))
    case IR.Select(cond, thn, els) =>
      val c = interpret(cond)
      c match
        case IRValue(false | 0) => interpret(els)
        case _ => interpret(thn)
    case IR.Join(left, right) => ???
    case IR.Feedback(init, Some(cond), Some(loop)) => feedbackStore.get(ir.uid) match
      case Some(value) => value
      case None =>
        feedbackStore += ir.uid -> interpret(init)
        interpretFeedback(ir.uid, cond, loop)

    case IR.Cast(ir, check) =>
      val v = interpret(ir)
      IRValue(check.asInstanceOf[IRCheck[Any]].assert(v.c))

// TODO deactivated tailrecursion to trigger stack overvflows during testing
//  @tailrec
  private final def interpretFeedback(uid: IR_UID, cond: IR, loop: IR): IRValue = {
    val v = interpret(cond)
    v match
      case IRValue(false | 0) => feedbackStore(uid)
      case _ =>
        feedbackStore += uid -> interpret(loop)
        val v = interpretFeedback(uid, cond, loop)
        v
  }

}
