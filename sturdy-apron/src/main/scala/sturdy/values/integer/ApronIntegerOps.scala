package sturdy.values.integer

import apron.Interval
import sturdy.data.CombineUnit
import apron.{DoubleScalar, Environment, MpqScalar, Tcons1, Texpr0Node, Texpr1BinNode, Texpr1CstNode, Texpr1Node, Texpr1UnNode, Var}
import gmp.{Mpq, Mpz}
import sturdy.apron.{Apron, ApronCons, ApronExpr, BinOp, JoinApronExpr, UnOp}
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


// given ApronIntegerOps[B](using Numeric[B])
//                         (using ConvertInterval[B])
//                         (using ap: Apron, effects: EffectStack, intervalOps: IntervalIntegerOps[B], f: Failure)
//       : IntegerOps[B, ApronExpr[Addr]] with
// 
//   import ApronOrderingOps.*
// 
//   def unaryIntervalOp(v: ApronExpr[Addr], f: NumericInterval[B] => NumericInterval[B])(using convert: ConvertInterval[B]): ApronExpr[Addr] =
//     ApronExpr[Addr].Constant(convert(f(convert(ap.currentScope.getBound(v)))))
// 
//   def binaryIntervalOp(v1: ApronExpr[Addr], v2: ApronExpr[Addr], f: (NumericInterval[B], NumericInterval[B]) => NumericInterval[B])(using convert: ConvertInterval[B]): ApronExpr[Addr] =
//     ApronExpr[Addr].Constant(convert(f(convert(ap.currentScope.getBound(v1)), convert(ap.currentScope.getBound(v2)))))
// 
//   override def integerLit(i: B): ApronExpr[Addr] = ApronExpr[Addr].Constant(new MpqScalar(new Mpz(i.toLong)))
// 
//   override def randomInteger(): ApronExpr[Addr] = ApronExpr[Addr].topConstant
// 
//   override def add(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Binary(BinOp.Add, v1, v2)
//   override def sub(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Binary(BinOp.Sub, v1, v2)
//   override def mul(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Binary(BinOp.Mul, v1, v2)
//   def neg(v: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Unary(UnOp.Negate, v)
// 
//   override def max(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
//       ap.assign(x1, v1)
//       ap.assign(x2, v2)
//       ap.ifThenElseUnit(lt(x1.expr, x2.expr)) {
//         ap.assign(r, x2.expr)
//       } {
//         ap.assign(r, x1.expr)
//       }
//       r.expr
//     }
// 
//   override def min(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
//       ap.assign(x1, v1)
//       ap.assign(x2, v2)
//       ap.ifThenElseUnit(lt(x1.expr, x2.expr)) {
//         ap.assign(r, x1.expr)
//       } {
//         ap.assign(r, x2.expr)
//       }
//       r.expr
//     }
// 
//   override def absolute(v: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryIntVariable { x =>
//       ap.assign(x, v)
//       max(x.expr, neg(x.expr))
//     }
// 
//   /** The default div of apron produces rationals rather than integers. */
//   private def integerDiv(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     ApronExpr[Addr].Binary(BinOp.Div,
//       ApronExpr[Addr].Binary(BinOp.Sub,
//         v1,
//         ApronExpr[Addr].Binary(BinOp.Mod,
//           v1,
//           v2)),
//       v2)
// 
// 
//   override def div(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
//       ap.assign(x1, v1)
//       ap.assign(x2, v2)
//       ap.ifThenElseUnit(ApronCons.eq(x2.expr, ApronExpr[Addr].num(0))) {
//         // x2 == 0
//         f.fail(IntegerDivisionByZero, s"$v1 / $v2")
//       } {
//         ap.ifThenElseUnit(ApronCons.gt(x2.expr, ApronExpr[Addr].num(0))) {
//           // x2 > 0
//           ap.assign(r, integerDiv(x1.expr, x2.expr))
//         } {
//           // x2 < 0
//           ap.assign(r,
//             ApronExpr[Addr].Unary(UnOp.Negate,
//               integerDiv(
//                 x1.expr,
//                 ApronExpr[Addr].Unary(UnOp.Negate, x2.expr))))
//         }
//       }
//       r.expr
//     }
// 
//   override def divUnsigned(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ???
// 
//   override def remainder(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
//       ap.assign(x1, v1)
//       ap.assign(x2, v2)
//       ap.ifThenElseUnit(ApronCons.eq(x2.expr, ApronExpr[Addr].Constant(MpqScalar(0)))) {
//         f.fail(IntegerDivisionByZero, s"$v1 remainder $v2")
//       } {
//         ap.assign(r, ApronExpr[Addr].Binary(BinOp.Mod, x1.expr, x2.expr))
//       }
//       r.expr
//     }
// 
//   override def remainderUnsigned(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ???
// 
//   override def modulo(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
//       ap.assign(x1, v1)
//       ap.assign(x2, v2)
//       // cumbersome
//       ap.ifThenElse(lt(x2.expr, ApronExpr[Addr].Constant(MpqScalar(0)))) {
//         ap.ifThenElseUnit(lt(x1.expr, ApronExpr[Addr].Constant(MpqScalar(0)))) {
//           ap.assign(r, sub(ApronExpr[Addr].Binary(BinOp.Mod, x1.expr, x2.expr), x2.expr))
//         } {
//           ap.assign(r, ApronExpr[Addr].Binary(BinOp.Mod, x1.expr, x2.expr))
//         }
//       } {
//         ap.ifThenElse(lt(ApronExpr[Addr].Constant(MpqScalar(0)), x2.expr)) {
//           ap.ifThenElseUnit(lt(x1.expr, ApronExpr[Addr].Constant(MpqScalar(0)))) {
//             ap.assign(r, add(ApronExpr[Addr].Binary(BinOp.Mod, x1.expr, x2.expr), x2.expr))
//           } {
//             ap.assign(r, ApronExpr[Addr].Binary(BinOp.Mod, x1.expr, x2.expr))
//           }
//         } {
//           f.fail(IntegerDivisionByZero, s"$v1 % $v2")
//         }
//       }
//       ap.assertConstrain(ApronCons.ge(r.expr, ApronExpr[Addr].num(0)))
//       r.expr
//     }
// 
//   override def gcd(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ???
// 
//   override def bitAnd(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     binaryIntervalOp(v1, v2, intervalOps.bitAnd)
//   override def bitOr(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     binaryIntervalOp(v1, v2, intervalOps.bitOr)
//   override def bitXor(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     binaryIntervalOp(v1, v2, intervalOps.bitXor)
//   override def shiftLeft(v: ApronExpr[Addr], shift: ApronExpr[Addr]): ApronExpr[Addr] =
//     binaryIntervalOp(v, shift, intervalOps.shiftLeft)
//   override def shiftRight(v: ApronExpr[Addr], shift: ApronExpr[Addr]): ApronExpr[Addr] =
//     binaryIntervalOp(v, shift, intervalOps.shiftRight)
//   override def shiftRightUnsigned(v: ApronExpr[Addr], shift: ApronExpr[Addr]): ApronExpr[Addr] =
//     binaryIntervalOp(v, shift, intervalOps.shiftRightUnsigned)
//   override def rotateLeft(v: ApronExpr[Addr], shift: ApronExpr[Addr]): ApronExpr[Addr] =
//     binaryIntervalOp(v, shift, intervalOps.rotateLeft)
//   override def rotateRight(v: ApronExpr[Addr], shift: ApronExpr[Addr]): ApronExpr[Addr] =
//     binaryIntervalOp(v, shift, intervalOps.rotateRight)
//   override def countLeadingZeros(v: ApronExpr[Addr]): ApronExpr[Addr] =
//     unaryIntervalOp(v, intervalOps.countLeadingZeros)
//   override def countTrailingZeros(v: ApronExpr[Addr]): ApronExpr[Addr] =
//     unaryIntervalOp(v, intervalOps.countTrailingZeros)
//   override def nonzeroBitCount(v: ApronExpr[Addr]): ApronExpr[Addr] =
//     unaryIntervalOp(v, intervalOps.nonzeroBitCount)
//   override def invertBits(v: ApronExpr[Addr]): ApronExpr[Addr] =
//     unaryIntervalOp(v, intervalOps.invertBits)
// 
// given ApronConvertIntLong(using Apron, EffectStack, Failure) : ConvertIntLong[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Int, Long, ApronExpr[Addr], ApronExpr[Addr], NumericInterval[Int], NumericInterval[Long], Bits](extract, inject)
// given ApronConvertIntFloat(using Apron, EffectStack, Failure) : ConvertIntFloat[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Int, Float, ApronExpr[Addr], ApronExpr[Addr], Topped[Int], Topped[Float], Bits](extract, inject)
// given ApronConvertIntDouble(using Apron, EffectStack, Failure) : ConvertIntDouble[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Int, Double, ApronExpr[Addr], ApronExpr[Addr], Topped[Int], Topped[Double], Bits](extract, inject)
// given ApronConvertIntBytes(using Apron, EffectStack, Failure) : ConvertIntBytes[ApronExpr[Addr],Seq[ApronExpr[Addr]]] = new LiftedConvert[Int, Seq[Byte], ApronExpr[Addr], Seq[ApronExpr[Addr]], NumericInterval[Int], Seq[NumericInterval[Byte]], config.BytesSize && SomeCC[ByteOrder]](extract, x => x.map(inject))
// given ApronConvertBytesInt(using Apron, EffectStack, Failure) : ConvertBytesInt[Seq[ApronExpr[Addr]],ApronExpr[Addr]] = new LiftedConvert[Seq[Byte], Int, Seq[ApronExpr[Addr]], ApronExpr[Addr], Seq[NumericInterval[Byte]], NumericInterval[Int], config.BytesSize && SomeCC[ByteOrder] && config.Bits](x => x.map(extract), inject)
// 
// given ApronConvertLongInt(using Apron, EffectStack, Failure) : ConvertLongInt[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Long, Int, ApronExpr[Addr], ApronExpr[Addr], NumericInterval[Long], NumericInterval[Int], NilCC.type](extract, inject)
// given ApronConvertLongFloat(using Apron, EffectStack, Failure) : ConvertLongFloat[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Long, Float, ApronExpr[Addr], ApronExpr[Addr], Topped[Long], Topped[Float], Bits](extract, inject)
// given ApronConvertLongDouble(using Apron, EffectStack, Failure) : ConvertLongDouble[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Long, Double, ApronExpr[Addr], ApronExpr[Addr], Topped[Long], Topped[Double], Bits](extract, inject)
// given ApronConvertLongBytes(using Apron, EffectStack, Failure) : ConvertLongBytes[ApronExpr[Addr],Seq[ApronExpr[Addr]]] = new LiftedConvert[Long, Seq[Byte], ApronExpr[Addr], Seq[ApronExpr[Addr]], NumericInterval[Long], Seq[NumericInterval[Byte]], config.BytesSize && SomeCC[ByteOrder]](extract, x => x.map(inject))
// given ApronConvertBytesLong(using Apron, EffectStack, Failure) : ConvertBytesLong[Seq[ApronExpr[Addr]],ApronExpr[Addr]] = new LiftedConvert[Seq[Byte], Long, Seq[ApronExpr[Addr]], ApronExpr[Addr], Seq[NumericInterval[Byte]], NumericInterval[Long], config.BytesSize && SomeCC[ByteOrder] && config.Bits](x => x.map(extract), inject)
// 
// def extract[B: Numeric](from : ApronExpr[Addr])(using ap: Apron)(using ConvertCoeff[Mpq, B]) : Topped[B] = convertToScalarMpq[B](ap.currentScope.getBound(from))
// def extract[B: Numeric](from : ApronExpr[Addr])(using ap: Apron, c: ConvertInterval[B]) : NumericInterval[B] = c(ap.currentScope.getBound(from))
// 
// def inject[B: Numeric](from : Topped[B]): ApronExpr[Addr] = from match
//   case Topped.Top => ApronExpr[Addr].topConstant
//   case Topped.Actual(i) => ApronExpr[Addr].Constant(new MpqScalar(new Mpq(i.toDouble)))
// def inject[B: Numeric](from : NumericInterval[B])(using c: ConvertInterval[B]): ApronExpr[Addr] = ApronExpr[Addr].Constant(c(from))