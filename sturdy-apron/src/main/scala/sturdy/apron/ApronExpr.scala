package sturdy.apron

import apron.*
import gmp.Mpz
import sturdy.apron.ApronCons.False
import sturdy.apron.ApronExpr.{Binary, Unary}
import sturdy.values.{Join, MaybeChanged, Widen}

enum ApronExpr:
  case Var(v: ApronVar)
  case Constant(coeff: Coeff)
  case Unary(op: UnOp, e: ApronExpr, roundingType: Int = Texpr1Node.RTYPE_REAL, ronudingDir: Int = Texpr1Node.RDIR_NEAREST)
  case Binary(op: BinOp, l: ApronExpr, r: ApronExpr, roundingType: Int = Texpr1Node.RTYPE_REAL, ronudingDir: Int = Texpr1Node.RDIR_NEAREST)

//  def vars: Set[ApronVar] = this match
//    case Var(v) => Set(v)
//    case Constant(coeff) => Set()
//    case Unary(op, e, rtyp, rdir) => e.vars
//    case Binary(op, l, r, rtyp, rdir) => l.vars ++ r.vars

  def toApron(apron: Apron): Texpr1Node = this match
    case Var(v) => apron.getFreedReference(v) match
      case Some(e) => e.toApron(apron)
      case None => apron.initializeVar(v).node
    case Constant(coeff) => new Texpr1CstNode(coeff)
    case Unary(op, e, rtyp, rdir) => new Texpr1UnNode(op.toApron, rtyp, rdir, e.toApron(apron))
    case Binary(op, l, r, rtyp, rdir) => new Texpr1BinNode(op.toApron, rtyp, rdir, l.toApron(apron), r.toApron(apron))

  def toIntern(apron: Apron): Texpr1Intern =
    val expr = this.toApron(apron)
    new Texpr1Intern(apron.env, expr)

object ApronExpr:
  def num(i: Int): Constant = 
    Constant(new MpqScalar(new Mpz(i)))
  def num(iv: Interval): Constant =
    Constant(iv)
  def topInterval: Interval =
    val topItv = new Interval()
    topItv.setTop()
    topItv
  def topConstant: Constant =
    Constant(topInterval)

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

enum ApronCons:
  case True
  case False
  case Compare(op: CompareOp, e1: ApronExpr, e2: ApronExpr)

  import CompareOp.*

//  def vars: Set[ApronVar] = this match
//    case True => Set()
//    case False => Set()
//    case Compare(_, e1, e2) => e1.vars ++ e2.vars

  def toApron(apron: Apron): Seq[Tcons1] = this match
    case True => Seq(new Tcons1(apron.env, Tcons1.EQ, ApronExpr.num(0).toApron(apron)))
    case False => Seq(new Tcons1(apron.env, Tcons1.EQ, ApronExpr.num(1).toApron(apron)))
    case Compare(Eq, e1, e2) => Seq(new Tcons1(apron.env, Tcons1.EQ, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron(apron)))
    case Compare(Neq, e1, e2) => Compare(Gt, e1, e2).toApron(apron) ++ Compare(Lt, e1, e2).toApron(apron)
    case Compare(Lt, e1, e2) => Seq(new Tcons1(apron.env, Tcons1.SUP, ApronExpr.Binary(BinOp.Sub, e2, e1).toApron(apron)))
    case Compare(Le, e1, e2) => Seq(new Tcons1(apron.env, Tcons1.SUPEQ, ApronExpr.Binary(BinOp.Sub, e2, e1).toApron(apron)))
    case Compare(Ge, e1, e2) => Seq(new Tcons1(apron.env, Tcons1.SUPEQ, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron(apron)))
    case Compare(Gt, e1, e2) => Seq(new Tcons1(apron.env, Tcons1.SUP, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron(apron)))


  def negated: ApronCons = this match
    case True => False
    case False => True
    case Compare(Eq, e1, e2) => Compare(Neq, e1, e2)
    case Compare(Neq, e1, e2) => Compare(Eq, e1, e2)
    case Compare(Lt, e1, e2) => Compare(Ge, e1, e2)
    case Compare(Le, e1, e2) => Compare(Gt, e1, e2)
    case Compare(Ge, e1, e2) => Compare(Lt, e1, e2)
    case Compare(Gt, e1, e2) => Compare(Le, e1, e2)

object ApronCons:
  import CompareOp.*

  def fromBool(b: Boolean): ApronCons = if (b) True else False
  def eq(e1: ApronExpr, e2: ApronExpr): Compare = Compare(Eq, e1, e2)
  def neq(e1: ApronExpr, e2: ApronExpr): Compare = Compare(Neq, e1, e2)
  def lt(e1: ApronExpr, e2: ApronExpr): Compare = Compare(Lt, e1, e2)
  def le(e1: ApronExpr, e2: ApronExpr): Compare = Compare(Le, e1, e2)
  def ge(e1: ApronExpr, e2: ApronExpr): Compare = Compare(Ge, e1, e2)
  def gt(e1: ApronExpr, e2: ApronExpr): Compare = Compare(Gt, e1, e2)


enum CompareOp:
  case Eq
  case Neq
  case Lt
  case Le
  case Ge
  case Gt


given JoinApronExpr(using ap: Apron): Join[ApronExpr] with
  def apply(v1: ApronExpr, v2: ApronExpr): MaybeChanged[ApronExpr] =
    ap.joins.combineExprs(v1, v2, ap.getState, widen = false)

given WidenApronExpr(using ap: Apron): Widen[ApronExpr] with
  def apply(v1: ApronExpr, v2: ApronExpr): MaybeChanged[ApronExpr] =
    ap.joins.combineExprs(v1, v2, ap.getState, widen = true)
