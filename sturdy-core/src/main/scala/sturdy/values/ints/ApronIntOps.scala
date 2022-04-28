package sturdy.values.ints

import sturdy.values.ints.IntOps

import scala.collection.immutable.TreeSet

import apron.Abstract0 // default; for domains without environments
import apron.Box
import apron.*
import scala.compiletime.ops.int

type ApronExpr = Texpr1Node

given ApronIntOps: IntOps[ApronExpr] with
  def intLit(i: Int): ApronExpr = Texpr1CstNode(MpqScalar(i))
  def add(e1: ApronExpr, e2: ApronExpr): ApronExpr = Texpr1BinNode(Texpr1BinNode.OP_ADD, e1, e2)
  // ...
