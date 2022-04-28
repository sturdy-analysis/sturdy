package sturdy.values.ints

import sturdy.values.ints.IntOps

import scala.collection.immutable.TreeSet

import apron.Abstract0 // default; for domains without environments
import apron.Box
import apron.*
import scala.compiletime.ops.int

type ApronExpr = Texpr1Node

class ApronIntOps extends IntOps[ApronExpr]:
  def intLit(i: Int): ApronExpr = Texpr1CstNode(MpqScalar(i))
  def add(e1: ApronExpr, e2: ApronExpr): ApronExpr = Texpr1BinNode(Texpr1BinNode.OP_ADD, e1, e2)
  def absolute(v: ApronExpr): ApronExpr = ???
  def bitAnd(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def bitOr(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def bitXor(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def countLeadingZeros(v: ApronExpr): ApronExpr = ???
  def countTrailinZeros(v: ApronExpr): ApronExpr = ???
  def div(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def divUnsigned(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def gcd(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def max(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def min(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def modulo(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def mul(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def nonzeroBitCount(v: ApronExpr): ApronExpr = ???
  def randomInt(): ApronExpr = ???
  def remainder(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def remainderUnsigned(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???
  def rotateLeft(v: ApronExpr, shift: ApronExpr): ApronExpr = ???
  def rotateRight(v: ApronExpr, shift: ApronExpr): ApronExpr = ???
  def shiftLeft(v: ApronExpr, shift: ApronExpr): ApronExpr = ???
  def shiftRight(v: ApronExpr, shift: ApronExpr): ApronExpr = ???
  def shiftRightUnsigned(v: ApronExpr, shift: ApronExpr): ApronExpr = ???
  def sub(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???