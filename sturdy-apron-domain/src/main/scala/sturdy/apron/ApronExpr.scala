package sturdy.apron

import apron.Abstract1
import apron.Coeff
import apron.Environment
import apron.Interval
import apron.Manager
import apron.MpqScalar
import apron.Tcons1
import apron.Texpr1BinNode
import apron.Texpr1CstNode
import apron.Texpr1Node
import apron.Texpr1UnNode
import apron.Texpr1VarNode
import gmp.Mpz
import sturdy.apron.ApronCons.False
import sturdy.values.{Widen, MaybeChanged, Join}

trait ApronVar:
  protected var freed: Boolean = false

  private var bound: Interval = _
  protected val av: apron.Var

  def getOrElse(f: => apron.Var): apron.Var =
    if (freed)
      f
    else
      av
  def free(manager: Manager, state: Abstract1): Unit =
    if (!freed) {
      bound = state.getBound(manager, av)
      freed = true
    }
  def expr: ApronExpr = ApronExpr.Var(this)
  def node: Texpr1Node =
    if (freed)
      new Texpr1CstNode(bound)
    else
      new Texpr1VarNode(av)

  override def toString: String =
    if (freed)
      s"$bound (freed $av)"
    else
      av.toString


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
  def num(i: Int): Constant = Constant(new MpqScalar(new Mpz(i)))
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

enum ApronCons:
  case True
  case False
  case Compare(op: CompareOp, e1: ApronExpr, e2: ApronExpr)

  import CompareOp.*
  
  def toApron(env: Environment): Tcons1 = this match
    case True => new Tcons1(env, Tcons1.EQ, ApronExpr.num(0).toApron)
    case False => new Tcons1(env, Tcons1.EQ, ApronExpr.num(1).toApron)
    case Compare(Eq, e1, e2) => new Tcons1(env, Tcons1.EQ, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron)
    case Compare(Neq, e1, e2) => new Tcons1(env, Tcons1.DISEQ, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron)
    case Compare(Lt, e1, e2) => new Tcons1(env, Tcons1.SUP, ApronExpr.Binary(BinOp.Sub, e2, e1).toApron)
    case Compare(Le, e1, e2) => new Tcons1(env, Tcons1.SUPEQ, ApronExpr.Binary(BinOp.Sub, e2, e1).toApron)
    case Compare(Ge, e1, e2) => new Tcons1(env, Tcons1.SUPEQ, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron)
    case Compare(Gt, e1, e2) => new Tcons1(env, Tcons1.SUP, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron)


  def negated: ApronCons = this match
    case True => False
    case False => True
    case Compare(Eq, e1, e2) => Compare(Neq, e1, e2)
    case Compare(Neq, e1, e2) => Compare(Eq, e1, e2)
    case Compare(Lt, e1, e2) => Compare(Ge, e1, e2)
    case Compare(Le, e1, e2) => Compare(Gt, e1, e2)
    case Compare(Ge, e1, e2) => Compare(Lt, e1, e2)
    case Compare(Gt, e1, e2) => Compare(Le, e1, e2)

  def splitNeq: Option[(ApronCons, ApronCons)] = this match
    case Compare(Neq, e1, e2) => Some((Compare(Lt, e1, e2), Compare(Gt, e1, e2)))
    case _ => None

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
    ap.joinValues(v1, v2, widen = false)

given WidenApronExpr(using ap: Apron): Widen[ApronExpr] with
  def apply(v1: ApronExpr, v2: ApronExpr): MaybeChanged[ApronExpr] =
    ap.joinValues(v1, v2, widen = true)
