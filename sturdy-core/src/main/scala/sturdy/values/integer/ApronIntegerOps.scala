package sturdy.values.integer

import scala.collection.immutable.TreeSet
import apron.Abstract0
import apron.Box
import apron.*
import sturdy.data.MayJoin
import sturdy.values.{ApronValue, Combine, MaybeChanged, TempVar, Unchanged, Widening}
import sturdy.values.integer.IntegerOps
import ApronValue.{join, joinCopy, toDouble}

import scala.compiletime.ops.int

class ApronIntegerOps(using manager: Manager) extends IntegerOps[Int, ApronValue]:
  override def integerLit(i: Int): ApronValue =
    ApronValue.interval(DoubleScalar(i),DoubleScalar(i))
  override def add(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
               expr = Texpr1BinNode(Texpr1BinNode.OP_ADD, v1.expr, v2.expr))
  override def sub(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
      expr = Texpr1BinNode(Texpr1BinNode.OP_SUB, v1.expr, v2.expr))

  override def mul(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
      expr = Texpr1BinNode(Texpr1BinNode.OP_MUL, v1.expr, v2.expr))

  override def div(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
      expr = Texpr1BinNode(Texpr1BinNode.OP_DIV, v1.expr, v2.expr))

  override def divUnsigned(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def modulo(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
      expr = Texpr1BinNode(Texpr1BinNode.OP_MOD, v1.expr, v2.expr))

  override def remainder(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def remainderUnsigned(v1: ApronValue, v2: ApronValue): ApronValue = ???

  /** gcd(x,y) ⊑ [1, min x y] */
  override def gcd(v1: ApronValue, v2: ApronValue): ApronValue = ???

  /** absolute(v) = sqrt(v*v) */
  override def absolute(v: ApronValue): ApronValue =
    ApronValue(domain = v.domain,
      expr =
        Texpr1UnNode(Texpr1UnNode.OP_SQRT,
          Texpr1BinNode(Texpr1BinNode.OP_POW,
            v.expr,
            Texpr1CstNode(ApronValue.scalar(2)))))


  /** max(x,y) = {y | x <= y} ⊔ {x | y <= x} */
  override def max(v1: ApronValue, v2: ApronValue): ApronValue =
    // TODO: Avoid introducing variables x or y if v1.expr or v2.expr are already a variable. This avoids increasing the dimensions of the problem
    val x: Var = TempVar()
    val y: Var = TempVar()
    val join: Var = TempVar()

    val env = v1.domain.getEnvironment.lce(v2.domain.getEnvironment).add(Array(x,y,join), Array.empty[Var])

    val d = v1.domain.changeEnvironmentCopy(manager, env, true)
    d.assign(manager, Array(x,y), Array(Texpr1Intern(env,v1.expr), Texpr1Intern(env,v2.expr)), null)

    // x <= y  iff  0 <= y - x
    val x_leq_y = d.meetCopy(manager, Tcons1(env, Tcons1.SUPEQ, Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1VarNode(y), Texpr1VarNode(x))))
    x_leq_y.assign(manager, join, Texpr1Intern(env,Texpr1VarNode(y)), null)

    // y <= x  iff  0 <= x - y
    val y_leq_x = d
    y_leq_x.meet(manager, Tcons1(env, Tcons1.SUPEQ, Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1VarNode(x), Texpr1VarNode(y))))
    y_leq_x.assign(manager, join, Texpr1Intern(env,Texpr1VarNode(x)), null)

    x_leq_y.join(manager, y_leq_x)
    val result = x_leq_y

    ApronValue(result, Texpr1VarNode(join))


  override def min(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def randomInteger(): ApronValue = ApronValue.interval(ApronValue.negInfinity, ApronValue.posInfinity)

  override def bitAnd(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def bitOr(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def bitXor(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def invertBits(v: ApronValue): ApronValue = ???
  override def countLeadingZeros(v: ApronValue): ApronValue = ???
  override def countTrailingZeros(v: ApronValue): ApronValue = ???

  override def nonzeroBitCount(v: ApronValue): ApronValue = ???
  override def rotateLeft(v: ApronValue, shift: ApronValue): ApronValue = ???
  override def rotateRight(v: ApronValue, shift: ApronValue): ApronValue = ???

  /** shiftLeft(x,y) = x * 2**y */
  override def shiftLeft(v: ApronValue, shift: ApronValue): ApronValue = ???

  /** shiftRight(x,y) = x / 2**y */
  override def shiftRight(v: ApronValue, shift: ApronValue): ApronValue = ???

  override def shiftRightUnsigned(v: ApronValue, shift: ApronValue): ApronValue = ???