package sturdy.apron

import apron.*
import gmp.Mpz
import sturdy.apron.ApronExpr.{Binary, Unary}
import sturdy.values.booleans.BooleanOps
import sturdy.values.floating.FloatOps
import sturdy.values.integer.IntegerOps
import sturdy.values.types.BaseType
import sturdy.values.{Join, MaybeChanged, Topped, Widen}

import java.math.BigInteger
import scala.reflect.ClassTag

enum ApronExpr[Addr, +Type]:
  case Addr(v: ApronVar[Addr], tpe: Type)
  case Constant(coeff: Coeff, tpe: Type)
  case Unary(op: UnOp,
             e: ApronExpr[Addr, Type],
             roundingType: RoundingType,
             roundingDir: RoundingDir,
             tpe: Type)
  case Binary(op: BinOp,
              l: ApronExpr[Addr, Type],
              r: ApronExpr[Addr, Type],
              roundingType: RoundingType,
              roundingDir: RoundingDir,
              tpe: Type)

  def _type: Type =
    this match
      case Addr(_, t) => t
      case Constant(_, t) => t
      case Unary(_, _, _, _, t) => t
      case Binary(_, _, _, _, _, t) => t

  def mapAddr[OtherAddr : Ordering : ClassTag](f: Addr => OtherAddr): ApronExpr[OtherAddr, Type] =
    this match
      case Addr(ApronVar(addr), _type) => Addr(ApronVar(f(addr)), _type)
      case Constant(coeff, _type) => Constant(coeff, _type)
      case Unary(op, expr, roundingType, roundingDir, _type) =>
        Unary(op, expr.mapAddr(f), roundingType, roundingDir, _type)
      case Binary(op, expr1, expr2, roundingType, roundingDir, _type) =>
        Binary(op, expr1.mapAddr(f), expr2.mapAddr(f), roundingType, roundingDir, _type)

  def mapAddrSame(f: Addr => Addr): ApronExpr[Addr,Type] =
    this match
      case Addr(_var, _type) => Addr(_var.mapAddr(f), _type)
      case Constant(coeff, _type) => Constant(coeff, _type)
      case Unary(op, expr, roundingType, roundingDir, _type) =>
        Unary(op, expr.mapAddrSame(f), roundingType, roundingDir, _type)
      case Binary(op, expr1, expr2, roundingType, roundingDir, _type) =>
        Binary(op, expr1.mapAddrSame(f), expr2.mapAddrSame(f), roundingType, roundingDir, _type)
        
  def isConstant: Boolean = addrs.isEmpty

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
    case Unary(op, e, rtyp, rdir, _) => new Texpr1UnNode(op.toApron, rtyp.toApron, rdir.toApron, e.toApron)
    case Binary(op, l, r, rtyp, rdir, _) => new Texpr1BinNode(op.toApron, rtyp.toApron, rdir.toApron, l.toApron, r.toApron)

  def toIntern(env: apron.Environment): Texpr1Intern =
    val expr = this.toApron
    try {
      new Texpr1Intern(env, expr)
    } catch {
      case exc: Exception =>
        throw new IllegalArgumentException(s"Exception while converting ApronExpr $expr with environment $env", exc)
    }


object ApronExpr:
  inline def addr[Addr : Ordering : ClassTag, Type](addr: Addr, _type: Type): ApronExpr[Addr, Type] = ApronExpr.Addr(ApronVar(addr), _type)

  inline def constant[Addr, Type](iv: Coeff, _type: Type): Constant[Addr, Type] =
    Constant(iv, _type)

  inline def intLit[Addr, Type](i: Int, tpe: Type): Constant[Addr, Type] =
    Constant(new MpqScalar(new Mpz(i)), tpe)
  inline def longLit[Addr, Type](l: Long, tpe: Type): Constant[Addr, Type] =
    Constant(new MpqScalar(new Mpz(BigInt(l).bigInteger)), tpe)
  inline def bigIntLit[Addr, Type](i: BigInt, tpe: Type): Constant[Addr, Type] =
    bigIntLit(i.bigInteger, tpe)
  inline def bigIntLit[Addr, Type](i: BigInteger, tpe: Type): Constant[Addr, Type] =
    Constant(new MpqScalar(new Mpz(i)), tpe)
  inline def doubleLit[Addr,Type](d: Double, tpe: Type): Constant[Addr, Type] =
    Constant(new DoubleScalar(d), tpe)
  inline def intInterval[Addr, Type](lower: Int, upper: Int, tpe: Type): Constant[Addr, Type] =
    Constant(Interval(lower, upper), tpe)
  inline def longInterval[Addr, Type](lower: Long, upper: Long, tpe: Type): Constant[Addr, Type] =
    Constant(Interval(new Mpz(BigInt(lower).bigInteger), new Mpz(BigInt(upper).bigInteger)), tpe)
  inline def doubleInterval[Addr, Type](lower: Double, upper: Double, tpe: Type): Constant[Addr, Type] =
    Constant(Interval(new DoubleScalar(lower), new DoubleScalar(upper)), tpe)

  inline def top[Addr,Type](tpe: Type): Constant[Addr,Type] =
    Constant(topInterval, tpe)

  inline def booleanLit[Addr, Type](using booleanOps: BooleanOps[Type])(b: Boolean): Constant[Addr, Type] =
    val n = if (b) 1 else 0
    Constant(new MpqScalar(new Mpz(n)), booleanOps.boolLit(b))

  inline def unary[Addr, Type: ApronType](op: UnOp, e1: ApronExpr[Addr, Type], resultType: Type): ApronExpr[Addr, Type] =
    ApronExpr.Unary(op, e1, resultType.roundingType, resultType.roundingDir, resultType)

  inline def binary[Addr, Type: ApronType](op: BinOp, e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type], resultType: Type): ApronExpr[Addr, Type] =
    ApronExpr.Binary(op, e1, e2, resultType.roundingType, resultType.roundingDir, resultType)

  inline def intNegate[L, Addr, Type: ApronType](using intOps: IntegerOps[L, Type])(e1: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    unary(UnOp.Negate, e1, e1._type)

  inline def floatNegate[L, Addr, Type: ApronType](using floatOps: FloatOps[L, Type])(e1: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    unary(UnOp.Negate, e1, floatOps.negated(e1._type))

  inline def floatSqrt[L, Addr, Type: ApronType](using floatOps: FloatOps[L, Type])(e1: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    unary(UnOp.Sqrt, e1, floatOps.sqrt(e1._type))

  inline def intAdd[L,Addr,Type: ApronType](using intOps: IntegerOps[L,Type])(e1: ApronExpr[Addr,Type], e2: ApronExpr[Addr,Type]): ApronExpr[Addr,Type] =
    intAdd(e1, e2, intOps.add(e1._type, e2._type))

  inline def intAdd[L,Addr,Type: ApronType](e1: ApronExpr[Addr,Type], e2: ApronExpr[Addr,Type], tpe: Type): ApronExpr[Addr,Type] =
    binary(BinOp.Add, e1, e2, tpe)

  inline def floatAdd[L,Addr,Type:ApronType](using floatOps: FloatOps[L,Type])(e1: ApronExpr[Addr,Type], e2: ApronExpr[Addr,Type]): ApronExpr[Addr,Type] =
    binary(BinOp.Add, e1, e2, floatOps.add(e1._type, e2._type))

  inline def intSub[L, Addr, Type: ApronType](using intOps: IntegerOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intSub(e1, e2, intOps.sub(e1._type, e2._type))

  inline def intSub[L, Addr, Type: ApronType](e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type], tpe: Type): ApronExpr[Addr, Type] =
    binary(BinOp.Sub, e1, e2, tpe)

  inline def floatSub[L, Addr, Type: ApronType](using floatOps: FloatOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Sub, e1, e2, floatOps.sub(e1._type, e2._type))

  inline def intMul[L, Addr, Type: ApronType](using intOps: IntegerOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Mul, e1, e2, intOps.mul(e1._type, e2._type))

  inline def floatMul[L, Addr, Type: ApronType](using floatOps: FloatOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Mul, e1, e2, floatOps.mul(e1._type, e2._type))

  inline def intDiv[L, Addr, Type: ApronType](using intOps: IntegerOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Div, e1, e2, intOps.div(e1._type, e2._type))

  inline def floatDiv[L, Addr, Type: ApronType](using floatOps: FloatOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Div, e1, e2, floatOps.div(e1._type, e2._type))

  inline def intMod[L, Addr, Type: ApronType](using intOps: IntegerOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Mod, e1, e2, intOps.remainder(e1._type, e2._type))

  inline def intPow[L, Addr, Type: ApronType](using intOps: IntegerOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Pow, e1, e2, intOps.mul(e1._type, e2._type))

  inline def floatPow[L, Addr, Type: ApronType](using floatOps: FloatOps[L, Type])(e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Pow, e1, e2, floatOps.mul(e1._type, e2._type))

  inline def cast[Addr, Type: ApronType](e: ApronExpr[Addr, Type], roundingType: RoundingType, roundingDir: RoundingDir, tpe: Type): ApronExpr[Addr, Type] =
    Unary(UnOp.Cast, e, roundingType, roundingDir, tpe)

  inline def topInterval: Interval =
    val topItv = new Interval()
    topItv.setTop()
    topItv

  inline def topConstant[Type](_type: Type): Constant[_, Type] =
    Constant(topInterval, _type)

  inline def bottomInterval: Interval =
    val itv = new Interval()
    itv.setBottom()
    itv
  inline def bottomConstant[Type](_type: Type): Constant[_, Type] =
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

case class ApronCons[Addr, Type](op: CompareOp, e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]):
  import CompareOp.*

  override def toString: String = s"($e1 $op $e2)"

  def mapAddr[OtherAddr : Ordering : ClassTag](f: Addr => OtherAddr): ApronCons[OtherAddr, Type] =
    ApronCons(op, e1.mapAddr(f), e2.mapAddr(f))

  def addrs: Set[Addr] = e1.addrs ++ e2.addrs

  def toApron(env : apron.Environment)(using apronType: ApronType[Type]): Tcons1 = op match
    case Eq  => Tcons1(env, Tcons1.EQ, ApronExpr.binary(BinOp.Sub, e1, e2, e1._type).toApron)
    case Neq => Tcons1(env, Tcons1.DISEQ, ApronExpr.binary(BinOp.Sub, e2, e1, e1._type).toApron)
    case Lt  => Tcons1(env, Tcons1.SUP, ApronExpr.binary(BinOp.Sub, e2, e1, e1._type).toApron)
    case Le  => Tcons1(env, Tcons1.SUPEQ, ApronExpr.binary(BinOp.Sub, e2, e1, e1._type).toApron)
    case Ge  => Tcons1(env, Tcons1.SUPEQ, ApronExpr.binary(BinOp.Sub, e1, e2, e1._type).toApron)
    case Gt  => Tcons1(env, Tcons1.SUP, ApronExpr.binary(BinOp.Sub, e1, e2, e1._type).toApron)

  def negated: ApronCons[Addr, Type] = op match
    case Eq  => ApronCons(Neq, e1, e2)
    case Neq => ApronCons(Eq, e1, e2)
    case Lt  => ApronCons(Ge, e1, e2)
    case Gt  => ApronCons(Le, e1, e2)
    case Le  => ApronCons(Gt, e1, e2)
    case Ge  => ApronCons(Lt, e1, e2)

object ApronCons:
  import CompareOp.*

  // issue below, make CompareOp[Addr]?
  def eq[Addr, Type](e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] =
    assert(e1._type == e2._type)
    ApronCons(Eq, e1, e2)
  def neq[Addr, Type](e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] =
    assert(e1._type == e2._type)
    ApronCons(Neq, e1, e2)
  def lt[Addr, Type](e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] =
    assert(e1._type == e2._type)
    ApronCons(Lt, e1, e2)
  def le[Addr, Type](e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] =
    assert(e1._type == e2._type)
    ApronCons(Le, e1, e2)
  def ge[Addr, Type](e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] =
    assert(e1._type == e2._type)
    ApronCons(Ge, e1, e2)
  def gt[Addr, Type](e1: ApronExpr[Addr, Type], e2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] =
    assert(e1._type == e2._type)
    ApronCons(Gt, e1, e2)
  def top[Addr, Type](tpe: Type): ApronCons[Addr, Type] =
    val itop = ApronExpr.constant[Addr,Type](ApronExpr.topInterval, tpe)
    ApronCons(Eq, itop, itop)

  def from[Addr,Type](tpe: Type)(boolean: Topped[Boolean]): ApronCons[Addr,Type] =
    boolean match
      case Topped.Top           => ApronCons.top(tpe)
      case Topped.Actual(true)  => ApronCons(Eq, ApronExpr.intLit(0, tpe), ApronExpr.intLit(0, tpe))
      case Topped.Actual(false) => ApronCons(Eq, ApronExpr.intLit(0, tpe), ApronExpr.intLit(1, tpe))

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

