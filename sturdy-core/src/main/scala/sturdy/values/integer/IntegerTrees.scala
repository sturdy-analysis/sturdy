package sturdy.values.integer

import sturdy.values.Indent
import sturdy.values.Tree

enum IntegerTree extends Tree:
  case IntegerLit[B](i: B)
  case RandomInteger()

  case Add(v1: Tree, v2: Tree)
  case Sub(v1: Tree, v2: Tree)
  case Mul(v1: Tree, v2: Tree)

  case Max(v1: Tree, v2: Tree)
  case Min(v1: Tree, v2: Tree)
  case Absolute(v: Tree)

  case Div(v1: Tree, v2: Tree)
  case DivUnsigned(v1: Tree, v2: Tree)
  case Remainder(v1: Tree, v2: Tree)
  case RemainderUnsigned(v1: Tree, v2: Tree)
  case Modulo(v1: Tree, v2: Tree)
  case Gcd(v1: Tree, v2: Tree)

  case BitAnd(v1: Tree, v2: Tree)
  case BitOr(v1: Tree, v2: Tree)
  case BitXor(v1: Tree, v2: Tree)
  case ShiftLeft(v1: Tree, v2: Tree)
  case ShiftRight(v1: Tree, v2: Tree)
  case ShiftRightUnsigned(v1: Tree, v2: Tree)
  case RotateLeft(v1: Tree, v2: Tree)
  case RotateRight(v1: Tree, v2: Tree)
  case CountLeadingZeros(v: Tree)
  case CountTrailingZeros(v: Tree)
  case NonzeroBitCount(v: Tree)

  override def prettyPrint(using Indent): String = this match
    case IntegerLit(i) => i.toString
    case RandomInteger() => "random integer"
    case Add(v1, v2) => s"(${v1.prettyPrint} + ${v2.prettyPrint})"
    case Sub(v1, v2) => s"(${v1.prettyPrint} - ${v2.prettyPrint})"
    case Mul(v1, v2) => s"(${v1.prettyPrint} * ${v2.prettyPrint})"
    case Max(v1, v2) => s"max(${v1.prettyPrint}, ${v2.prettyPrint})"
    case Min(v1, v2) => s"min(${v1.prettyPrint}, ${v2.prettyPrint})"
    case Absolute(v) => s"abs(${v.prettyPrint})"
    case Div(v1, v2) => s"(${v1.prettyPrint} / ${v2.prettyPrint})"
    case DivUnsigned(v1, v2) => s"(${v1.prettyPrint} /u ${v2.prettyPrint})"
    case Remainder(v1, v2) => s"(${v1.prettyPrint} % ${v2.prettyPrint})"
    case RemainderUnsigned(v1, v2) => s"(${v1.prettyPrint} %u ${v2.prettyPrint})"
    case Modulo(v1, v2) => s"(${v1.prettyPrint} % ${v2.prettyPrint})"
    case Gcd(v1, v2) => s"gcd(${v1.prettyPrint}, ${v2.prettyPrint})"
    case BitAnd(v1, v2) => s"(${v1.prettyPrint} & ${v2.prettyPrint})"
    case BitOr(v1, v2) => s"(${v1.prettyPrint} | ${v2.prettyPrint})"
    case BitXor(v1, v2) => s"(${v1.prettyPrint} ^ ${v2.prettyPrint})"
    case ShiftLeft(v1, v2) => s"(${v1.prettyPrint} << ${v2.prettyPrint})"
    case ShiftRight(v1, v2) => s"(${v1.prettyPrint} >> ${v2.prettyPrint})"
    case ShiftRightUnsigned(v1, v2) => s"(${v1.prettyPrint} >>u ${v2.prettyPrint})"
    case RotateLeft(v1, v2) => s"(${v1.prettyPrint} <<< ${v2.prettyPrint})"
    case RotateRight(v1, v2) => s"(${v1.prettyPrint} >>> ${v2.prettyPrint})"
    case CountLeadingZeros(v) => s"clz(${v.prettyPrint})"
    case CountTrailingZeros(v) => s"ctz(${v.prettyPrint})"
    case NonzeroBitCount(v) => s"popcount(${v.prettyPrint})"


given IntegerTrees[B]: IntegerOps[B, Tree] with
  def integerLit(i: B): IntegerTree = IntegerTree.IntegerLit(i)
  def randomInteger(): IntegerTree = IntegerTree.RandomInteger()

  def add(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Add(v1, v2)
  def sub(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Sub(v1, v2)
  def mul(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Mul(v1, v2)

  def max(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Max(v1, v2)
  def min(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Min(v1, v2)
  def absolute(v: Tree): IntegerTree = IntegerTree.Absolute(v)

  def div(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Div(v1, v2)
  def divUnsigned(v1: Tree, v2: Tree): IntegerTree = IntegerTree.DivUnsigned(v1, v2)
  def remainder(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Remainder(v1, v2)
  def remainderUnsigned(v1: Tree, v2: Tree): IntegerTree = IntegerTree.RemainderUnsigned(v1, v2)
  def modulo(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Modulo(v1, v2)
  def gcd(v1: Tree, v2: Tree): IntegerTree = IntegerTree.Gcd(v1, v2)

  def bitAnd(v1: Tree, v2: Tree): IntegerTree = IntegerTree.BitAnd(v1, v2)
  def bitOr(v1: Tree, v2: Tree): IntegerTree = IntegerTree.BitOr(v1, v2)
  def bitXor(v1: Tree, v2: Tree): IntegerTree = IntegerTree.BitXor(v1, v2)
  def shiftLeft(v1: Tree, v2: Tree): IntegerTree = IntegerTree.ShiftLeft(v1, v2)
  def shiftRight(v1: Tree, v2: Tree): IntegerTree = IntegerTree.ShiftRight(v1, v2)
  def shiftRightUnsigned(v1: Tree, v2: Tree): IntegerTree = IntegerTree.ShiftRightUnsigned(v1, v2)
  def rotateLeft(v1: Tree, v2: Tree): IntegerTree = IntegerTree.RotateLeft(v1, v2)
  def rotateRight(v1: Tree, v2: Tree): IntegerTree = IntegerTree.RotateRight(v1, v2)
  def countLeadingZeros(v: Tree): IntegerTree = IntegerTree.CountLeadingZeros(v)
  def countTrailingZeros(v: Tree): IntegerTree = IntegerTree.CountTrailingZeros(v)
  def nonzeroBitCount(v: Tree): IntegerTree = IntegerTree.NonzeroBitCount(v)

