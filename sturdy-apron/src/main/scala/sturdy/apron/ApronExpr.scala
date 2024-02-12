package sturdy.apron

import apron.*
import gmp.Mpz
import sturdy.apron.ApronExpr.{Binary, Unary}
import sturdy.values.booleans.BooleanOps
import sturdy.values.integer.IntegerOps
import sturdy.values.ordering.{EqOps, OrderingOps}
import sturdy.values.types.BaseType
import sturdy.values.{Join, MaybeChanged, Widen}

import java.math.BigInteger
import scala.reflect.ClassTag

enum ApronExpr[Addr, Type]:
  case Addr(v: ApronVar[Addr], tpe: Type)
  case Constant(coeff: Coeff, tpe: Type)
  case Unary(op: UnOp,
             e: ApronExpr[Addr, Type],
             tpe: Type,
             roundingType: RoundingType,
             roundingDir: RoundingDir)
  case Binary(op: BinOp,
              l: ApronExpr[Addr, Type],
              r: ApronExpr[Addr, Type],
              tpe: Type,
              roundingType: RoundingType,
              roundingDir: RoundingDir)

  def _type: Type =
    this match
      case Addr(_, t) => t
      case Constant(_, t) => t
      case Unary(_, _, t, _, _) => t
      case Binary(_, _, _, t, _, _) => t

  def mapAddr[OtherAddr : Ordering : ClassTag](f: Addr => OtherAddr): ApronExpr[OtherAddr, Type] =
    this match
      case Addr(ApronVar(addr), _type) => Addr(ApronVar(f(addr)), _type)
      case Constant(coeff, _type) => Constant(coeff, _type)
      case Unary(op, expr, roundingType, roundingDir, _type) =>
        Unary(op, expr.mapAddr(f), roundingType, roundingDir, _type)
      case Binary(op, expr1, expr2, roundingType, roundingDir, _type) =>
        Binary(op, expr1.mapAddr(f), expr2.mapAddr(f), roundingType, roundingDir, _type)

  def addrs: Set[Addr] = this match
    case Addr(v, _) => Set(v.addr)
    case Constant(coeff, _) => Set()
    case Unary(op, e, rtyp, rdir, _) => e.addrs
    case Binary(op, l, r, rtyp, rdir, _) => l.addrs ++ r.addrs

  override def toString: String = this match
    case Addr(v, _) => v.toString
    case Constant(coeff, _) => coeff.toString
    case Unary(op, e, _, _, _) => s"$op $e"
    case Binary(op, l, r, _, _, _) => s"($l $op $r)"

  def toApron: Texpr1Node = this match
    case Addr(v, _) => new Texpr1VarNode(v) // we have v: Addr, but we want an apron.Var. Extend physical and virtual addresses for that case?
    case Constant(coeff, _) => new Texpr1CstNode(coeff)
    case Unary(op, e, _, rtyp, rdir) => new Texpr1UnNode(op.toApron, rtyp.toApron, rdir.toApron, e.toApron)
    case Binary(op, l, r, _, rtyp, rdir) => new Texpr1BinNode(op.toApron, rtyp.toApron, rdir.toApron, l.toApron, r.toApron)

  def toIntern(env: apron.Environment): Texpr1Intern =
    val expr = this.toApron
    new Texpr1Intern(env, expr)


object ApronExpr:
  def addr[Addr : Ordering : ClassTag, Type](addr: Addr, _type: Type): ApronExpr[Addr, Type] = ApronExpr.Addr(ApronVar(addr), _type)
  def intLit[Addr, Type](using intOps: IntegerOps[Int,Type])(i: Int): Constant[Addr, Type] =
    Constant(new MpqScalar(new Mpz(i)), intOps.integerLit(0))
  def longLit[Addr, Type](using intOps: IntegerOps[Int, Type])(i: Long): Constant[Addr, Type] =
    Constant(new MpqScalar(new Mpz(BigInteger.valueOf(i))), intOps.integerLit(0))
  def intInterval[Addr, Type](using intOps: IntegerOps[Int,Type])(lower: Int, upper: Int): Constant[Addr, Type] =
    Constant(Interval(lower, upper), intOps.integerLit(0))
  def intTop[Addr, Type](using intOps: IntegerOps[Int, Type]): Constant[Addr, Type] =
    Constant(topInterval, intOps.integerLit(0))

  def constant[Addr, Type](iv: Interval, _type: Type): Constant[Addr, Type] =
    Constant(iv, _type)

  def unary[Addr, Type: ApronType](op: UnOp, e1: ApronExpr[Addr, Type], resultType: Type): ApronExpr[Addr, Type] =
    ApronExpr.Unary(op, e1, resultType, resultType.roundingType, resultType.roundingDir)

  def binary[Addr, Type: ApronType](op: BinOp, e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type], resultType: Type): ApronExpr[Addr, Type] =
    ApronExpr.Binary(op, e1, e2, resultType, resultType.roundingType, resultType.roundingDir)

  def intNegate[Addr, Type: ApronType](using intOps: IntegerOps[Int, Type])(e1: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    unary(UnOp.Negate, e1, intOps.sub(intOps.integerLit(0), e1._type))

  def intAdd[Addr,Type: ApronType](using intOps: IntegerOps[Int,Type])(e1: ApronExpr[Addr,Type], e2: ApronExpr[Addr,Type]): ApronExpr[Addr,Type] =
    binary(BinOp.Add, e1, e2, intOps.add(e1._type, e2._type))

  def intSub[Addr, Type: ApronType](using intOps: IntegerOps[Int, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Sub, e1, e2, intOps.sub(e1._type, e2._type))

  def intMul[Addr, Type: ApronType](using intOps: IntegerOps[Int, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Mul, e1, e2, intOps.mul(e1._type, e2._type))

  def intDiv[Addr, Type: ApronType](using intOps: IntegerOps[Int, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Div, e1, e2, intOps.div(e1._type, e2._type))

  def intMod[Addr, Type: ApronType](using intOps: IntegerOps[Int, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Mod, e1, e2, intOps.remainder(e1._type, e2._type))

  def intPow[Addr, Type: ApronType](using intOps: IntegerOps[Int, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Pow, e1, e2, intOps.mul(e1._type, e2._type))

  def topInterval: Interval =
    val topItv = new Interval()
    topItv.setTop()
    topItv
  def topConstant[Type](_type: Type): Constant[_, Type] =
    Constant(topInterval, _type)

  def bottomInterval: Interval =
    val itv = new Interval()
    itv.setBottom()
    itv
  def bottomConstant[Type](_type: Type): Constant[_, Type] =
    Constant(bottomInterval, _type)


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

enum ApronCons[Addr, Type]:
  case Compare(op: CompareOp, e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type], tpe: Type)

  import CompareOp.*

  override def toString: String = this match
    case Compare(op, e1, e2, _) => s"($e1 $op $e2)"

  def mapAddr[OtherAddr : Ordering : ClassTag](f: Addr => OtherAddr): ApronCons[OtherAddr, Type] = this match
    case Compare(op, e1, e2, tpe) => Compare(op, e1.mapAddr(f), e2.mapAddr(f), tpe)

  def addrs: Set[Addr] = this match
    case Compare(_, e1, e2, _) => e1.addrs ++ e2.addrs

  def toApron(env : apron.Environment)(using ApronType[Type]): Seq[Tcons1] = this match
    case Compare(Eq, e1, e2, tpe) =>
      Seq(new Tcons1(env, Tcons1.EQ, ApronExpr.binary(BinOp.Sub, e1, e2, tpe).toApron))
    case Compare(Neq, e1, e2, tpe) => Compare(Gt, e1, e2, tpe).toApron(env) ++ Compare(Lt, e1, e2, tpe).toApron(env)
    case Compare(Lt, e1, e2, tpe) => Seq(new Tcons1(env, Tcons1.SUP, ApronExpr.binary(BinOp.Sub, e2, e1, tpe).toApron))
    case Compare(Le, e1, e2, tpe) => Seq(new Tcons1(env, Tcons1.SUPEQ, ApronExpr.binary(BinOp.Sub, e2, e1, tpe).toApron))
    case Compare(Ge, e1, e2, tpe) => Seq(new Tcons1(env, Tcons1.SUPEQ, ApronExpr.binary(BinOp.Sub, e1, e2, tpe).toApron))
    case Compare(Gt, e1, e2, tpe) => Seq(new Tcons1(env, Tcons1.SUP, ApronExpr.binary(BinOp.Sub, e1, e2, tpe).toApron))


  def negated: ApronCons[Addr, Type] = this match
    case Compare(Eq, e1, e2, tpe) => Compare(Neq, e1, e2, tpe)
    case Compare(Neq, e1, e2, tpe) => Compare(Eq, e1, e2, tpe)
    case Compare(Lt, e1, e2, tpe) => Compare(Ge, e1, e2, tpe)
    case Compare(Le, e1, e2, tpe) => Compare(Gt, e1, e2, tpe)
    case Compare(Ge, e1, e2, tpe) => Compare(Lt, e1, e2, tpe)
    case Compare(Gt, e1, e2, tpe) => Compare(Le, e1, e2, tpe)

object ApronCons:
  import CompareOp.*

  // issue below, make CompareOp[Addr]?
  def intEq[Addr, Type](using eqOps: EqOps[Type, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): Compare[Addr, Type] =
    Compare(Eq, e1, e2, eqOps.equ(e1._type, e2._type))
  def intNeq[Addr, Type](using eqOps: EqOps[Type, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): Compare[Addr, Type] =
    Compare(Neq, e1, e2, eqOps.neq(e1._type, e2._type))
  def intLt[Addr, Type](using orderingOps: OrderingOps[Type, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): Compare[Addr, Type] =
    Compare(Lt, e1, e2, orderingOps.lt(e1._type, e2._type))
  def intLe[Addr, Type](using orderingOps: OrderingOps[Type, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): Compare[Addr, Type] =
    Compare(Le, e1, e2, orderingOps.le(e2._type, e1._type))
  def intGe[Addr, Type](using orderingOps: OrderingOps[Type, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): Compare[Addr, Type] =
    Compare(Ge, e1, e2, orderingOps.ge(e1._type, e2._type))
  def intGt[Addr, Type](using orderingOps: OrderingOps[Type, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): Compare[Addr, Type] =
    Compare(Gt, e1, e2, orderingOps.gt(e1._type, e2._type))

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

given JoinApronExpr[Var, Type]: Join[ApronExpr[Var, Type]] with
  def apply(v1: ApronExpr[Var, Type], v2: ApronExpr[Var, Type]): MaybeChanged[ApronExpr[Var, Type]] =
    throw NotImplementedError()

given WidenApronExpr[Var, Type]: Widen[ApronExpr[Var, Type]] with
  def apply(v1: ApronExpr[Var, Type], v2: ApronExpr[Var, Type]): MaybeChanged[ApronExpr[Var, Type]] =
    throw NotImplementedError()