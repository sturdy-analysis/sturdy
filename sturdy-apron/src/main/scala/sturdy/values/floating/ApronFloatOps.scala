package sturdy.values.floating

import apron.Interval
import apron.Texpr1VarNode
import sturdy.data.CombineUnit
import apron.{MpfrScalar, Tcons1, Texpr1BinNode, Texpr1CstNode, Texpr1Node, Texpr1UnNode}
import sturdy.values.integer.{IntegerDivisionByZero, IntegerOps}

import math.Numeric.Implicits.infixNumericOps
import gmp.Mpfr
import sturdy.apron.{Apron, ApronCons, ApronExpr, ApronState, BinOp, JoinApronExpr, UnOp}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Topped, config}
import sturdy.values.config.{Bits, Overflow}
import sturdy.values.convert.{&&, LiftedConvert, NilCC, SomeCC, ToppedConvert, given}
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}
import sturdy.values.utils.{ConvertCoeff, convertToScalarMpfr, given}

import java.nio.ByteOrder
import scala.language.reflectiveCalls


given ApronFloatOps[B](using Fractional[B])
                      (using ap: Apron, effects: EffectStack, f: Failure)
                      : FloatOps[B, ApronExpr] with

  import ApronOrderingOps.*

  implicit def state: ApronState = ap.getState

  override def floatingLit(f: B): ApronExpr = ApronExpr.Constant(new MpfrScalar(f.toDouble, 2))

  override def randomFloat(): ApronExpr = ApronExpr.topConstant

  override def add(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Add, v1, v2)
  override def sub(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Sub, v1, v2)
  override def mul(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Mul, v1, v2)
  override def div(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Div, v1, v2)
  
  override def min(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElseUnit(lt(x1.expr, x2.expr)) {
        ap.assign(r, x1.expr)
      } {
        ap.assign(r, x2.expr)
      }
      r.expr
    }

  override def max(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElseUnit(lt(x1.expr, x2.expr)) {
        ap.assign(r, x2.expr)
      } {
        ap.assign(r, x1.expr)
      }
      r.expr
    }

  override def absolute(v: ApronExpr): ApronExpr =
    ap.withTemporaryDoubleVariable { x =>
      ap.assign(x, v)
      max(x.expr, negated(x.expr))
    }

  override def negated(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Negate, v)

  override def sqrt(v: ApronExpr): ApronExpr =
    ap.ifThenElse(ge(v, ApronExpr.Constant(MpfrScalar(0, 0)))) {
      ApronExpr.Unary(UnOp.Sqrt, v)
    } {
      f.fail(IntegerDivisionByZero, s"sqrt($v)")
    }

  override def ceil(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_UP)
  override def floor(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_DOWN)
  override def truncate(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_ZERO)
  override def nearest(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_NEAREST)

  override def copysign(v: ApronExpr, sign: ApronExpr): ApronExpr =
    ap.withTemporaryDoubleVariables(3) { case List(x, xsign, r) =>
      ap.assign(x, v)
      ap.assign(xsign, sign)
      ap.ifThenElse(lt(x.expr, ApronExpr.Constant(MpfrScalar(0, 0)))) {
        ap.ifThenElseUnit(lt(xsign.expr, ApronExpr.Constant(MpfrScalar(0, 0)))) {
          ap.assign(r, x.expr)
        } {
          ap.assign(r, negated(x.expr))
        }
      } {
        ap.ifThenElseUnit(lt(xsign.expr, ApronExpr.Constant(MpfrScalar(0, 0)))) {
          ap.assign(r, negated(x.expr))
        } {
          ap.assign(r, x.expr)
        }
      }
      r.expr
    }

given ApronConvertFloatInt(using Apron, EffectStack, Failure) : ConvertFloatInt[ApronExpr,ApronExpr] = new LiftedConvert[Float, Int, ApronExpr, ApronExpr, Topped[Float], Topped[Int], Overflow && Bits](extract, inject)
given ApronConvertFloatLong(using Apron, EffectStack, Failure) : ConvertFloatLong[ApronExpr,ApronExpr] = new LiftedConvert[Float, Long, ApronExpr, ApronExpr, Topped[Float], Topped[Long], Overflow && Bits](extract, inject)
given ApronConvertFloatDouble(using Apron, EffectStack, Failure) : ConvertFloatDouble[ApronExpr,ApronExpr] = new LiftedConvert[Float, Double, ApronExpr, ApronExpr, Topped[Float], Topped[Double], NilCC.type](extract, inject)
given ApronConvertFloatBytes(using Apron, EffectStack, Failure) : ConvertFloatBytes[ApronExpr,Seq[ApronExpr]] = new LiftedConvert[Float, Seq[Byte], ApronExpr, Seq[ApronExpr], Topped[Float], Seq[Topped[Byte]], config.BytesSize && SomeCC[ByteOrder]](extract, x => x.map(inject))
given ApronConvertBytesFloat(using Apron, EffectStack, Failure) : ConvertBytesFloat[Seq[ApronExpr],ApronExpr] = new LiftedConvert[Seq[Byte], Float, Seq[ApronExpr], ApronExpr, Seq[Topped[Byte]], Topped[Float], SomeCC[ByteOrder]](x => x.map(extract), inject)

given ApronConvertDoubleInt(using Apron, EffectStack, Failure) : ConvertDoubleInt[ApronExpr,ApronExpr] = new LiftedConvert[Double, Int, ApronExpr, ApronExpr, Topped[Double], Topped[Int], Overflow && Bits](extract, inject)
given ApronConvertDoubleLong(using Apron, EffectStack, Failure) : ConvertDoubleLong[ApronExpr,ApronExpr] = new LiftedConvert[Double, Long, ApronExpr, ApronExpr, Topped[Double], Topped[Long], Overflow && Bits](extract, inject)
given ApronConvertDoubleFloat(using Apron, EffectStack, Failure) : ConvertDoubleFloat[ApronExpr,ApronExpr] = new LiftedConvert[Double, Float, ApronExpr, ApronExpr, Topped[Double], Topped[Float], NilCC.type](extract, inject)
given ApronConvertDoubleBytes(using Apron, EffectStack, Failure) : ConvertDoubleBytes[ApronExpr,Seq[ApronExpr]] = new LiftedConvert[Double, Seq[Byte], ApronExpr, Seq[ApronExpr], Topped[Double], Seq[Topped[Byte]], config.BytesSize && SomeCC[ByteOrder]](extract, x => x.map(inject))
given ApronConvertBytesDouble(using Apron, EffectStack, Failure) : ConvertBytesDouble[Seq[ApronExpr],ApronExpr] = new LiftedConvert[Seq[Byte], Double, Seq[ApronExpr], ApronExpr, Seq[Topped[Byte]], Topped[Double], SomeCC[ByteOrder]](x => x.map(extract), inject)

def extract[B: Numeric](expr : ApronExpr)(using ap: Apron, conv: ConvertCoeff[Mpfr, B]) : Topped[B] = convertToScalarMpfr[B](ap.currentScope.getBound(expr))

def inject[B](cst : Topped[B])(using Numeric[B]): ApronExpr = cst match
  case Topped.Top => ApronExpr.topConstant
  case Topped.Actual(i) => ApronExpr.Constant(new MpfrScalar(i.toDouble, 0))
