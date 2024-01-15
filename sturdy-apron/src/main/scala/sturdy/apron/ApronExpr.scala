package sturdy.apron

import apron.*
import gmp.Mpz
import sturdy.apron.ApronCons.False
import sturdy.apron.ApronExpr.{Binary, Unary}
import sturdy.values.{Join, MaybeChanged, Widen}


enum ApronExpr[Addr <: apron.Var]:
  case Var(v: Addr)
  case Constant[Addr](coeff: Coeff) extends ApronExpr[apron.Var]
  case Unary(op: UnOp, e: ApronExpr[Addr], roundingType: Int = Texpr1Node.RTYPE_REAL, ronudingDir: Int = Texpr1Node.RDIR_NEAREST)
  case Binary(op: BinOp, l: ApronExpr[Addr], r: ApronExpr[Addr], roundingType: Int, roundingDir: Int)

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

  // def normalize(scope: ApronScope[Addr]): ApronExpr[Addr] = this match
  //   case Var(v) => scope.getFreedReference(v) match
  //     case Some(e) => e.normalize(scope) // TODO: cache e.normalize in scope
  //     case None => this
  //   case Constant(coeff) => this
  //   case Unary(op, e, rtyp, rdir) => Unary(op, e.normalize(scope), rtyp, rdir)
  //   case Binary(op, l, r, rtyp, rdir) => Binary(op, l.normalize(scope), r.normalize(scope), rtyp, rdir)

  // def isEqual(that: ApronExpr[Addr], scope: ApronScope[Addr]): Boolean =
  //   this.normalize(scope) == that.normalize(scope)

  // def hashCode(scope: ApronScope[Addr]): Int =
  //   normalize(scope).hashCode

  def toApron(): Texpr1Node = this match
    case Var(v) => new Texpr1VarNode(v) // we have v: Addr, but we want an apron.Var. Extend physical and virtual addresses for that case?
    case Constant(coeff) => new Texpr1CstNode(coeff)
    case Unary(op, e, rtyp, rdir) => new Texpr1UnNode(op.toApron, rtyp, rdir, e.toApron())
    case Binary(op, l, r, rtyp, rdir) => new Texpr1BinNode(op.toApron, rtyp, rdir, l.toApron(), r.toApron())

  def toIntern(env: apron.Environment): Texpr1Intern =
    val expr = this.toApron()
    new Texpr1Intern(env, expr)


object ApronExpr:
  def num(i: Int): Constant[_] = 
    Constant(new MpqScalar(new Mpz(i)))
  def num(iv: Interval): Constant[_] =
    Constant(iv)
  def topInterval: Interval =
    val topItv = new Interval()
    topItv.setTop()
    topItv
  def topConstant: Constant[_] =
    Constant(topInterval)

  def Unary[Addr <: apron.Var](op: UnOp, e: ApronExpr[Addr]): ApronExpr[Addr] =
    Unary(op, e, Texpr1Node.RTYPE_REAL, Texpr1Node.RDIR_NEAREST)
  def Binary[Addr <: apron.Var](op: BinOp, l: ApronExpr[Addr], r: ApronExpr[Addr]): ApronExpr[Addr] =
    Binary(op, l, r, Texpr1Node.RTYPE_REAL, Texpr1Node.RDIR_NEAREST)


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

enum ApronCons[Addr <: apron.Var]:
  case True[Addr]() extends ApronCons[apron.Var]
  case False[Addr]() extends ApronCons[apron.Var]
  case Compare(op: CompareOp, e1: ApronExpr[Addr], e2: ApronExpr[Addr])

  import CompareOp.*

  override def toString: String = this match
    case True() => "true"
    case False() => "false"
    case Compare(op, e1, e2) => s"($e1 $op $e2)"

//  def vars: Set[ApronVar] = this match
//    case True => Set()
//    case False => Set()
//    case Compare(_, e1, e2) => e1.vars ++ e2.vars

  def toApron(env : apron.Environment): Seq[Tcons1] = this match
    case True() => Seq(new Tcons1(env, Tcons1.EQ, ApronExpr.num(0).toApron()))
    case False() => Seq(new Tcons1(env, Tcons1.EQ, ApronExpr.num(1).toApron()))
    case Compare(Eq, e1, e2) =>
      val sub = ApronExpr.Binary(BinOp.Sub, e1, e2)
      Seq(new Tcons1(env, Tcons1.EQ, sub.toApron()))
    case Compare(Neq, e1, e2) => Compare(Gt, e1, e2).toApron(env) ++ Compare(Lt, e1, e2).toApron(env)
    case Compare(Lt, e1, e2) => Seq(new Tcons1(env, Tcons1.SUP, ApronExpr.Binary(BinOp.Sub, e2, e1).toApron()))
    case Compare(Le, e1, e2) => Seq(new Tcons1(env, Tcons1.SUPEQ, ApronExpr.Binary(BinOp.Sub, e2, e1).toApron()))
    case Compare(Ge, e1, e2) => Seq(new Tcons1(env, Tcons1.SUPEQ, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron()))
    case Compare(Gt, e1, e2) => Seq(new Tcons1(env, Tcons1.SUP, ApronExpr.Binary(BinOp.Sub, e1, e2).toApron()))


  def negated: ApronCons[Addr] = this match
    case True() => False()
    case False() => True()
    case Compare(Eq, e1, e2) => Compare(Neq, e1, e2)
    case Compare(Neq, e1, e2) => Compare(Eq, e1, e2)
    case Compare(Lt, e1, e2) => Compare(Ge, e1, e2)
    case Compare(Le, e1, e2) => Compare(Gt, e1, e2)
    case Compare(Ge, e1, e2) => Compare(Lt, e1, e2)
    case Compare(Gt, e1, e2) => Compare(Le, e1, e2)

object ApronCons:
  import CompareOp.*

  // issue below, make CompareOp[Addr]?
  def fromBool(b: Boolean): ApronCons[_] = if (b) True() else False()
  def eq[Addr <: apron.Var](e1: ApronExpr[Addr], e2: ApronExpr[Addr]): Compare[Addr] = Compare(Eq, e1, e2)
  def neq[Addr <: apron.Var](e1: ApronExpr[Addr], e2: ApronExpr[Addr]): Compare[Addr] = Compare(Neq, e1, e2)
  def lt[Addr <: apron.Var](e1: ApronExpr[Addr], e2: ApronExpr[Addr]): Compare[Addr] = Compare(Lt, e1, e2)
  def le[Addr <: apron.Var](e1: ApronExpr[Addr], e2: ApronExpr[Addr]): Compare[Addr] = Compare(Le, e1, e2)
  def ge[Addr <: apron.Var](e1: ApronExpr[Addr], e2: ApronExpr[Addr]): Compare[Addr] = Compare(Ge, e1, e2)
  def gt[Addr <: apron.Var](e1: ApronExpr[Addr], e2: ApronExpr[Addr]): Compare[Addr] = Compare(Gt, e1, e2)

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
