package sturdy.values.integer


import apron.DoubleScalar
import apron.Texpr1BinNode
import apron.Texpr1CstNode
import apron.Texpr1Node

import math.Numeric.Implicits.infixNumericOps

//given ApronIntegerOps[B](using Numeric[B]): IntegerOps[Int, Texpr1Node] with
//  def integerLit(i: B): Texpr1Node = new Texpr1CstNode(new DoubleScalar(i.toDouble))
//
//  def add(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
//    new Texpr1BinNode(Texpr1BinNode.OP_ADD, v1, v2)
