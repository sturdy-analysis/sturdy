package sturdy.values.integer

import apron.Interval
import sturdy.data.CombineUnit
import apron.{DoubleScalar, Environment, MpqScalar, Tcons1, Texpr0Node, Texpr1BinNode, Texpr1CstNode, Texpr1Node, Texpr1UnNode, Var}
import gmp.{Mpq, Mpz}
import sturdy.apron.ApronCons
import sturdy.apron.{Apron, ApronExpr, BinOp, JoinApronExpr, UnOp}
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.callframe.ApronCallFrame

import java.util
import math.Numeric.Implicits.infixNumericOps
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.config.{Bits, UnsupportedConfiguration}
import sturdy.values.convert.{&&, LiftedConvert, NilCC, SomeCC, ToppedConvert}
import sturdy.values.floating.FloatOps
import sturdy.values.{Top, Topped, config}
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps, OrderingOps}
import sturdy.values.utils.{ConvertCoeff, ConvertInterval, convertToScalarMpq, given}

import java.nio.ByteOrder
import scala.language.reflectiveCalls


given ApronIntegerOps[B](using Numeric[B])
                        (using ConvertInterval[B])
                        (using ap: Apron, effects: EffectStack, intervalOps: IntervalIntegerOps[B], f: Failure)
      : IntegerOps[B, ApronExpr] with

  import ApronOrderingOps.*

  def unaryIntervalOp(v: ApronExpr, f: NumericInterval[B] => NumericInterval[B])(using convert: ConvertInterval[B]): ApronExpr =
    ApronExpr.Constant(convert(f(convert(ap.getBound(v)))))

  def binaryIntervalOp(v1: ApronExpr, v2: ApronExpr, f: (NumericInterval[B], NumericInterval[B]) => NumericInterval[B])(using convert: ConvertInterval[B]): ApronExpr =
    ApronExpr.Constant(convert(f(convert(ap.getBound(v1)), convert(ap.getBound(v2)))))

  override def integerLit(i: B): ApronExpr = ApronExpr.Constant(new MpqScalar(new Mpz(i.toLong)))

  override def randomInteger(): ApronExpr = ApronExpr.top

  override def add(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Add, v1, v2)
  override def sub(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Sub, v1, v2)
  override def mul(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Mul, v1, v2)
  def neg(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Negate, v)

  override def max(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElseUnit(lt(x1.expr, x2.expr)) {
        ap.assign(r, x2.expr)
      } {
        ap.assign(r, x1.expr)
      }
      r.expr
    }

  override def min(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElseUnit(lt(x1.expr, x2.expr)) {
        ap.assign(r, x1.expr)
      } {
        ap.assign(r, x2.expr)
      }
      r.expr
    }

  override def absolute(v: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariable { x =>
      ap.assign(x, v)
      max(x.expr, neg(x.expr))
    }

  private def safediv(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ApronExpr.Binary(BinOp.Div,
      ApronExpr.Binary(BinOp.Sub,
        v1,
        ApronExpr.Binary(BinOp.Mod,
          v1,
          v2)),
      v2)


  override def div(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElseUnit(ApronCons.eq(x2.expr, ApronExpr.Constant(MpqScalar(0)))) {
        f.fail(IntegerDivisionByZero, s"$v1 / $v2")
      } {
//        ap.assertConstrain(ApronCons.eq(r.expr, safediv(x1.expr, x2.expr)))
        ap.assign(r, safediv(x1.expr, x2.expr))
      }
      r.expr
    }

  override def divUnsigned(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???

  override def remainder(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElseUnit(ApronCons.eq(x2.expr, ApronExpr.Constant(MpqScalar(0)))) {
        f.fail(IntegerDivisionByZero, s"$v1 remainder $v2")
      } {
        ap.assign(r, ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr))
      }
      r.expr
    }

  override def remainderUnsigned(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???

  override def modulo(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      // cumbersome
      ap.ifThenElse(lt(x2.expr, ApronExpr.Constant(MpqScalar(0)))) {
        ap.ifThenElseUnit(lt(x1.expr, ApronExpr.Constant(MpqScalar(0)))) {
          ap.assign(r, sub(ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr), x2.expr))
        } {
          ap.assign(r, ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr))
        }
      } {
        ap.ifThenElse(lt(ApronExpr.Constant(MpqScalar(0)), x2.expr)) {
          ap.ifThenElseUnit(lt(x1.expr, ApronExpr.Constant(MpqScalar(0)))) {
            ap.assign(r, add(ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr), x2.expr))
          } {
            ap.assign(r, ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr))
          }
        } {
          f.fail(IntegerDivisionByZero, s"$v1 % $v2")
        }
      }
      ap.assertConstrain(ApronCons.ge(r.expr, ApronExpr.num(0)))
      r.expr
    }

  override def gcd(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???

  override def bitAnd(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    binaryIntervalOp(v1, v2, intervalOps.bitAnd)
  override def bitOr(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    binaryIntervalOp(v1, v2, intervalOps.bitOr)
  override def bitXor(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    binaryIntervalOp(v1, v2, intervalOps.bitXor)
  override def shiftLeft(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.shiftLeft)
  override def shiftRight(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.shiftRight)
  override def shiftRightUnsigned(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.shiftRightUnsigned)
  override def rotateLeft(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.rotateLeft)
  override def rotateRight(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.rotateRight)
  override def countLeadingZeros(v: ApronExpr): ApronExpr =
    unaryIntervalOp(v, intervalOps.countLeadingZeros)
  override def countTrailingZeros(v: ApronExpr): ApronExpr =
    unaryIntervalOp(v, intervalOps.countTrailingZeros)
  override def nonzeroBitCount(v: ApronExpr): ApronExpr =
    unaryIntervalOp(v, intervalOps.nonzeroBitCount)
  override def invertBits(v: ApronExpr): ApronExpr =
    unaryIntervalOp(v, intervalOps.invertBits)

given ApronConvertIntLong(using Apron, EffectStack, Failure) : ConvertIntLong[ApronExpr,ApronExpr] = new LiftedConvert[Int, Long, ApronExpr, ApronExpr, NumericInterval[Int], NumericInterval[Long], Bits](extract, inject)
given ApronConvertIntFloat(using Apron, EffectStack, Failure) : ConvertIntFloat[ApronExpr,ApronExpr] = new LiftedConvert[Int, Float, ApronExpr, ApronExpr, Topped[Int], Topped[Float], Bits](extract, inject)
given ApronConvertIntDouble(using Apron, EffectStack, Failure) : ConvertIntDouble[ApronExpr,ApronExpr] = new LiftedConvert[Int, Double, ApronExpr, ApronExpr, Topped[Int], Topped[Double], Bits](extract, inject)
given ApronConvertIntBytes(using Apron, EffectStack, Failure) : ConvertIntBytes[ApronExpr,Seq[ApronExpr]] = new LiftedConvert[Int, Seq[Byte], ApronExpr, Seq[ApronExpr], NumericInterval[Int], Seq[NumericInterval[Byte]], config.BytesSize && SomeCC[ByteOrder]](extract, x => x.map(inject))
given ApronConvertBytesInt(using Apron, EffectStack, Failure) : ConvertBytesInt[Seq[ApronExpr],ApronExpr] = new LiftedConvert[Seq[Byte], Int, Seq[ApronExpr], ApronExpr, Seq[NumericInterval[Byte]], NumericInterval[Int], config.BytesSize && SomeCC[ByteOrder] && config.Bits](x => x.map(extract), inject)

given ApronConvertLongInt(using Apron, EffectStack, Failure) : ConvertLongInt[ApronExpr,ApronExpr] = new LiftedConvert[Long, Int, ApronExpr, ApronExpr, NumericInterval[Long], NumericInterval[Int], NilCC.type](extract, inject)
given ApronConvertLongFloat(using Apron, EffectStack, Failure) : ConvertLongFloat[ApronExpr,ApronExpr] = new LiftedConvert[Long, Float, ApronExpr, ApronExpr, Topped[Long], Topped[Float], Bits](extract, inject)
given ApronConvertLongDouble(using Apron, EffectStack, Failure) : ConvertLongDouble[ApronExpr,ApronExpr] = new LiftedConvert[Long, Double, ApronExpr, ApronExpr, Topped[Long], Topped[Double], Bits](extract, inject)
given ApronConvertLongBytes(using Apron, EffectStack, Failure) : ConvertLongBytes[ApronExpr,Seq[ApronExpr]] = new LiftedConvert[Long, Seq[Byte], ApronExpr, Seq[ApronExpr], NumericInterval[Long], Seq[NumericInterval[Byte]], config.BytesSize && SomeCC[ByteOrder]](extract, x => x.map(inject))
given ApronConvertBytesLong(using Apron, EffectStack, Failure) : ConvertBytesLong[Seq[ApronExpr],ApronExpr] = new LiftedConvert[Seq[Byte], Long, Seq[ApronExpr], ApronExpr, Seq[NumericInterval[Byte]], NumericInterval[Long], config.BytesSize && SomeCC[ByteOrder] && config.Bits](x => x.map(extract), inject)

def extract[B: Numeric](from : ApronExpr)(using ap: Apron)(using ConvertCoeff[Mpq, B]) : Topped[B] = convertToScalarMpq[B](ap.getBound(from.toApron))
def extract[B: Numeric](from : ApronExpr)(using ap: Apron, c: ConvertInterval[B]) : NumericInterval[B] = c(ap.getBound(from.toApron))

def inject[B: Numeric](from : Topped[B]): ApronExpr = from match
  case Topped.Top => ApronExpr.top
  case Topped.Actual(i) => ApronExpr.Constant(new MpqScalar(new Mpq(i.toDouble)))
def inject[B: Numeric](from : NumericInterval[B])(using c: ConvertInterval[B]): ApronExpr = ApronExpr.Constant(c(from))