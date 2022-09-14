package sturdy.apron

import apron.Coeff
import apron.Interval
import apron.Texpr1BinNode
import apron.Texpr1CstNode
import apron.Texpr1Node
import apron.Texpr1UnNode
import sturdy.values.{Widen, MaybeChanged, Join}

enum ApronExpr:
  case Var(v: ApronVar)
  case Constant(coeff: Coeff)
  case Unary(op: UnOp, e: ApronExpr, roundingType: Int = Texpr1Node.RTYPE_REAL, ronudingDir: Int = Texpr1Node.RDIR_NEAREST)
  case Binary(op: BinOp, l: ApronExpr, r: ApronExpr, roundingType: Int = Texpr1Node.RTYPE_REAL, ronudingDir: Int = Texpr1Node.RDIR_NEAREST)

  def toApron: Texpr1Node = this match
    case Var(v) => v.node
    case Constant(coeff) => new Texpr1CstNode(coeff)
    case Unary(op, e, rtyp, rdir) => new Texpr1UnNode(op.toApron, rtyp, rdir, e.toApron)
    case Binary(op, l, r, rtyp, rdir) => new Texpr1BinNode(op.toApron, rtyp, rdir, l.toApron, r.toApron)

object ApronExpr:
  def top: Constant =
    val topItv = new Interval()
    topItv.setTop()
    Constant(topItv)

enum UnOp:
  case Negate
  case Cast
  case Sqrt

  def toApron: Int = this match
    case Negate => Texpr1UnNode.OP_NEG
    case Cast => Texpr1UnNode.OP_CAST
    case Sqrt => Texpr1UnNode.OP_SQRT

enum BinOp:
  case Add
  case Sub
  case Mul
  case Div
  case Mod
  case Pow

  def toApron: Int = this match
    case Add => Texpr1BinNode.OP_ADD
    case Sub => Texpr1BinNode.OP_SUB
    case Mul => Texpr1BinNode.OP_MUL
    case Div => Texpr1BinNode.OP_DIV
    case Mod => Texpr1BinNode.OP_MOD
    case Pow => Texpr1BinNode.OP_POW

given JoinApronExpr(using ap: Apron): Join[ApronExpr] with
  def apply(v1: ApronExpr, v2: ApronExpr): MaybeChanged[ApronExpr] =
    ap.joinValues(v1, v2, widen = false)

given WidenApronExpr(using ap: Apron): Widen[ApronExpr] with
  def apply(v1: ApronExpr, v2: ApronExpr): MaybeChanged[ApronExpr] =
    ap.joinValues(v1, v2, widen = true)
