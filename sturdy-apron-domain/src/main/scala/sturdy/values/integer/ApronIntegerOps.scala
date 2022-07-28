package sturdy.values.integer


import apron.{DoubleScalar, Environment, Texpr0Node, Texpr1BinNode, Texpr1CstNode, Texpr1Node, Texpr1UnNode, Var}
import sturdy.effect.callframe.ApronCallFrame

import java.util
import math.Numeric.Implicits.infixNumericOps
import sturdy.effect.EffectStack

given ApronIntegerOps[B, Data, Var](using Numeric[B])(using callframe: ApronCallFrame[Data, Var], effects : EffectStack) : IntegerOps[Int, Texpr1Node] with

  def integerLit(i: B): Texpr1Node = new Texpr1CstNode(new DoubleScalar(i.toDouble))
  override def integerLit(i: Int): Texpr1Node = new Texpr1CstNode(new DoubleScalar(i.toDouble))
  override def randomInteger(): Texpr1Node = ???

  override def add(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_ADD, v1, v2)

  override def sub(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_SUB, v1, v2)

  override def mul(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_MUL, v1, v2)

  override def max(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def min(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???

  override def div(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    // Use join computations for call frame test

    new Texpr1BinNode(Texpr1BinNode.OP_DIV, v1, v2)

  override def divUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def remainder(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def remainderUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def modulo(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)
  override def gcd(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???

  override def absolute(v: Texpr1Node): Texpr1Node = ???
  override def bitAnd(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def bitOr(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def bitXor(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def shiftLeft(v: Texpr1Node, shift: Texpr1Node): Texpr1Node = ???
  override def shiftRight(v: Texpr1Node, shift: Texpr1Node): Texpr1Node = ???
  override def shiftRightUnsigned(v: Texpr1Node, shift: Texpr1Node): Texpr1Node = ???
  override def rotateLeft(v: Texpr1Node, shift: Texpr1Node): Texpr1Node = ???
  override def rotateRight(v: Texpr1Node, shift: Texpr1Node): Texpr1Node = ???
  override def countLeadingZeros(v: Texpr1Node): Texpr1Node = ???
  override def countTrailingZeros(v: Texpr1Node): Texpr1Node = ???
  override def nonzeroBitCount(v: Texpr1Node): Texpr1Node = ???
  override def invertBits(v: Texpr1Node): Texpr1Node = ???
