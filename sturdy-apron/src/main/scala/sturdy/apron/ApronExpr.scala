package sturdy.apron

import apron.*
import gmp.Mpz
import sturdy.apron.ApronCons.False
import sturdy.apron.ApronExpr.{Binary, Unary}
import sturdy.values.{Join, MaybeChanged, Widen}
import sturdy.effect.store.VirtualAddress

enum ApronExpr[Context]:
  case Var(v: VirtualAddress[Context])
  case Constant(coeff: Coeff)
  case Unary(op: UnOp, e: ApronExpr[Context], roundingType: Int = Texpr1Node.RTYPE_REAL, ronudingDir: Int = Texpr1Node.RDIR_NEAREST)
  case Binary(op: BinOp, l: ApronExpr[Context], r: ApronExpr[Context], roundingType: Int = Texpr1Node.RTYPE_REAL, ronudingDir: Int = Texpr1Node.RDIR_NEAREST)

  override def toString: String = this match
    case Var(v) => v.toString
    case Constant(coeff) => coeff.toString
    case Unary(op, e, _, _) => s"$op $e"
    case Binary(op, l, r, _, _) => s"($l $op $r)"

//  def vars: Set[ApronVar] = this match
//    case Var(v) => Set(v)
//    case Constant(coeff) => Set()
//    case Unary(op, e, rtyp, rdir) => e.vars
//    case Binary(op, l, r, rtyp, rdir) => l.vars ++ r.vars

  def normalize(scope: ApronScope[Context]): ApronExpr[Context] = this match
    case Var(v) => scope.getFreedReference(v) match
      case Some(e) => e.normalize(scope) // TODO: cache e.normalize in scope
      case None => this
    case Constant(coeff) => this
    case Unary(op, e, rtyp, rdir) => Unary(op, e.normalize(scope), rtyp, rdir)
    case Binary(op, l, r, rtyp, rdir) => Binary(op, l.normalize(scope), r.normalize(scope), rtyp, rdir)

  def isEqual(that: ApronExpr[Context], scope: ApronScope[Context]): Boolean =
    this.normalize(scope) == that.normalize(scope)

  def hashCode(scope: ApronScope[Context]): Int =
    normalize(scope).hashCode

  def toApron(scope: ApronScope[Context], allowOpen: Boolean = true): Texpr1Node = this match
    case Var(v) => scope.getFreedReference(v) match
      case Some(e) => e.toApron(scope, allowOpen)
      case None =>
        if (allowOpen || scope.isBound(v))
          v.node
        else
          new Texpr1CstNode(ApronExpr.topInterval)
    case Constant(coeff) => new Texpr1CstNode(coeff)
    case Unary(op, e, rtyp, rdir) => new Texpr1UnNode(op.toApron, rtyp, rdir, e.toApron(scope, allowOpen))
    case Binary(op, l, r, rtyp, rdir) => new Texpr1BinNode(op.toApron, rtyp, rdir, l.toApron(scope, allowOpen), r.toApron(scope, allowOpen))

  def toIntern(scope: ApronScope[Context], allowOpen: Boolean = true): Texpr1Intern =
    val expr = this.toApron(scope, allowOpen)
    try new Texpr1Intern(scope.apronEnv, expr)
    catch {
      case ex: IllegalArgumentException => throw new IllegalArgumentException(s"Cannot close $this in $scope", ex)
    }

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

  override def toString: String = this match
    case Negate => "-"
    case Cast => "cast"
    case Sqrt => "sqrt"

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

  override def toString: String = this match
    case Add => "+"
    case Sub => "-"
    case Mul => "*"
    case Div => "/"
    case Mod => "%"
    case Pow => "^"

  def toApron: Int = this match
    case Add => Texpr1BinNode.OP_ADD
    case Sub => Texpr1BinNode.OP_SUB
    case Mul => Texpr1BinNode.OP_MUL
    case Div => Texpr1BinNode.OP_DIV
    case Mod => Texpr1BinNode.OP_MOD
    case Pow => Texpr1BinNode.OP_POW

enum ApronCons[Context]:
  case True
  case False
  case Compare(op: CompareOp, e1: ApronExpr[Context], e2: ApronExpr[Context])

  import CompareOp.*

  override def toString: String = this match
    case True => "true"
    case False => "false"
    case Compare(op, e1, e2) => s"($e1 $op $e2)"

//  def vars: Set[ApronVar] = this match
//    case True => Set()
//    case False => Set()
//    case Compare(_, e1, e2) => e1.vars ++ e2.vars

  def toApron(scope: ApronScope[Context]): Seq[Tcons1] = this match
    case True => Seq(new Tcons1(scope.apronEnv, Tcons1.EQ, ApronExpr.num(0).toApron(scope)))
    case False => Seq(new Tcons1(scope.apronEnv, Tcons1.EQ, ApronExpr.num(1).toApron(scope)))
    case Compare(Eq, e1, e2) => Seq(new Tcons1(scope.apronEnv, Tcons1.EQ, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron(scope)))
    case Compare(Neq, e1, e2) => Compare(Gt, e1, e2).toApron(scope) ++ Compare(Lt, e1, e2).toApron(scope)
    case Compare(Lt, e1, e2) => Seq(new Tcons1(scope.apronEnv, Tcons1.SUP, ApronExpr.Binary(BinOp.Sub, e2, e1).toApron(scope)))
    case Compare(Le, e1, e2) => Seq(new Tcons1(scope.apronEnv, Tcons1.SUPEQ, ApronExpr.Binary(BinOp.Sub, e2, e1).toApron(scope)))
    case Compare(Ge, e1, e2) => Seq(new Tcons1(scope.apronEnv, Tcons1.SUPEQ, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron(scope)))
    case Compare(Gt, e1, e2) => Seq(new Tcons1(scope.apronEnv, Tcons1.SUP, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron(scope)))


  def negated: ApronCons[Context] = this match
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

  override def toString: String = this match
    case Eq => "=="
    case Neq => "!="
    case Lt => "<"
    case Le => "<="
    case Ge => ">="
    case Gt => ">"

given JoinApronExpr[Context](using state: ApronState): Join[ApronExpr[Context]] with
  def apply(v1: ApronExpr[Context], v2: ApronExpr[Context]): MaybeChanged[ApronExpr[Context]] =
    ApronJoins.combineExprs(v1, v2, state, widen = false)

given WidenApronExpr[Context](using state: ApronState): Widen[ApronExpr[Context]] with
  def apply(v1: ApronExpr[Context], v2: ApronExpr[Context]): MaybeChanged[ApronExpr[Context]] =
    ApronJoins.combineExprs(v1, v2, state, widen = true)
