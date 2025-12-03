package sturdy.language.tip_xdai.arithmetic

import sturdy.data.MayJoin
import sturdy.language.tip_xdai.core.{Exp, Input, Var, CoreGenericInterpreter, FixIn, FixOut }
import sturdy.values.integer.IntegerOps
import sturdy.values.ordering.OrderingOps

trait GenericInterpreter[V, J[_] <: MayJoin[_]] extends CoreGenericInterpreter[V, J]:
  val intOps: IntegerOps[Int, V]
  val intOrderingOps: OrderingOps[V, V]

  override def eval_open(e: Exp)(using Fixed): V = e match
    case NumLit(n) => intOps.integerLit(n)
    case Add(e1, e2) => intOps.add(eval(e1), eval(e2))
    case Sub(e1, e2) => intOps.sub(eval(e1), eval(e2))
    case Mul(e1, e2) => intOps.mul(eval(e1), eval(e2))
    case Div(e1, e2) => intOps.div(eval(e1), eval(e2))
    case Gt(e1, e2) => intOrderingOps.gt(eval(e1), eval(e2))
    case _ => super.eval_open(e)