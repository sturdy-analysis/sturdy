package sturdy.values.floating

import sturdy.apron.{ApronExpr, ApronState, ApronType}
import sturdy.effect.failure.Failure
import sturdy.values.Join
import scala.reflect.ClassTag
import ApronExpr.*

trait RelationalBaseFloatOps
  [
    L,
    Addr: Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   f: Failure,
   typeFloatOps: FloatOps[L,Type]
  ) extends FloatOps[L, ApronExpr[Addr,Type]]:
  override def add(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatAdd(v1, v2)

  override def sub(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatSub(v1, v2)

  override def mul(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatMul(v1, v2)

  override def div(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatDiv(v1, v2)

  override def max(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def min(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???


  override def absolute(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def negated(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def sqrt(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    floatSqrt(v)

  override def ceil(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def floor(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def truncate(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def nearest(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def copysign(v: ApronExpr[Addr, Type], sign: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???
  


// given ApronFloatOps[B](using Fractional[B])
//                       (using ap: Apron, effects: EffectStack, f: Failure)
//                       : FloatOps[B, ApronExpr[Addr]] with
// 
//   import ApronOrderingOps.*
// 
//   implicit def state: ApronState = ap.getState
// 
//   override def floatingLit(f: B): ApronExpr[Addr] = ApronExpr[Addr].Constant(new MpfrScalar(f.toDouble, 2))
// 
//   override def randomFloat(): ApronExpr[Addr] = ApronExpr[Addr].topConstant
// 
//   override def add(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Binary(BinOp.Add, v1, v2)
//   override def sub(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Binary(BinOp.Sub, v1, v2)
//   override def mul(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Binary(BinOp.Mul, v1, v2)
//   override def div(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Binary(BinOp.Div, v1, v2)
//   
//   override def min(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
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
//   override def max(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
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
//   override def absolute(v: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryDoubleVariable { x =>
//       ap.assign(x, v)
//       max(x.expr, negated(x.expr))
//     }
// 
//   override def negated(v: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Unary(UnOp.Negate, v)
// 
//   override def sqrt(v: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.ifThenElse(ge(v, ApronExpr[Addr].Constant(MpfrScalar(0, 0)))) {
//       ApronExpr[Addr].Unary(UnOp.Sqrt, v)
//     } {
//       f.fail(IntegerDivisionByZero, s"sqrt($v)")
//     }
// 
//   override def ceil(v: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_UP)
//   override def floor(v: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_DOWN)
//   override def truncate(v: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_ZERO)
//   override def nearest(v: ApronExpr[Addr]): ApronExpr[Addr] = ApronExpr[Addr].Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_NEAREST)
// 
//   override def copysign(v: ApronExpr[Addr], sign: ApronExpr[Addr]): ApronExpr[Addr] =
//     ap.withTemporaryDoubleVariables(3) { case List(x, xsign, r) =>
//       ap.assign(x, v)
//       ap.assign(xsign, sign)
//       ap.ifThenElse(lt(x.expr, ApronExpr[Addr].Constant(MpfrScalar(0, 0)))) {
//         ap.ifThenElseUnit(lt(xsign.expr, ApronExpr[Addr].Constant(MpfrScalar(0, 0)))) {
//           ap.assign(r, x.expr)
//         } {
//           ap.assign(r, negated(x.expr))
//         }
//       } {
//         ap.ifThenElseUnit(lt(xsign.expr, ApronExpr[Addr].Constant(MpfrScalar(0, 0)))) {
//           ap.assign(r, negated(x.expr))
//         } {
//           ap.assign(r, x.expr)
//         }
//       }
//       r.expr
//     }
// 
// given ApronConvertFloatInt(using Apron, EffectStack, Failure) : ConvertFloatInt[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Float, Int, ApronExpr[Addr], ApronExpr[Addr], Topped[Float], Topped[Int], Overflow && Bits](extract, inject)
// given ApronConvertFloatLong(using Apron, EffectStack, Failure) : ConvertFloatLong[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Float, Long, ApronExpr[Addr], ApronExpr[Addr], Topped[Float], Topped[Long], Overflow && Bits](extract, inject)
// given ApronConvertFloatDouble(using Apron, EffectStack, Failure) : ConvertFloatDouble[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Float, Double, ApronExpr[Addr], ApronExpr[Addr], Topped[Float], Topped[Double], NilCC.type](extract, inject)
// given ApronConvertFloatBytes(using Apron, EffectStack, Failure) : ConvertFloatBytes[ApronExpr[Addr],Seq[ApronExpr[Addr]]] = new LiftedConvert[Float, Seq[Byte], ApronExpr[Addr], Seq[ApronExpr[Addr]], Topped[Float], Seq[Topped[Byte]], config.BytesSize && SomeCC[ByteOrder]](extract, x => x.map(inject))
// given ApronConvertBytesFloat(using Apron, EffectStack, Failure) : ConvertBytesFloat[Seq[ApronExpr[Addr]],ApronExpr[Addr]] = new LiftedConvert[Seq[Byte], Float, Seq[ApronExpr[Addr]], ApronExpr[Addr], Seq[Topped[Byte]], Topped[Float], SomeCC[ByteOrder]](x => x.map(extract), inject)
// 
// given ApronConvertDoubleInt(using Apron, EffectStack, Failure) : ConvertDoubleInt[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Double, Int, ApronExpr[Addr], ApronExpr[Addr], Topped[Double], Topped[Int], Overflow && Bits](extract, inject)
// given ApronConvertDoubleLong(using Apron, EffectStack, Failure) : ConvertDoubleLong[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Double, Long, ApronExpr[Addr], ApronExpr[Addr], Topped[Double], Topped[Long], Overflow && Bits](extract, inject)
// given ApronConvertDoubleFloat(using Apron, EffectStack, Failure) : ConvertDoubleFloat[ApronExpr[Addr],ApronExpr[Addr]] = new LiftedConvert[Double, Float, ApronExpr[Addr], ApronExpr[Addr], Topped[Double], Topped[Float], NilCC.type](extract, inject)
// given ApronConvertDoubleBytes(using Apron, EffectStack, Failure) : ConvertDoubleBytes[ApronExpr[Addr],Seq[ApronExpr[Addr]]] = new LiftedConvert[Double, Seq[Byte], ApronExpr[Addr], Seq[ApronExpr[Addr]], Topped[Double], Seq[Topped[Byte]], config.BytesSize && SomeCC[ByteOrder]](extract, x => x.map(inject))
// given ApronConvertBytesDouble(using Apron, EffectStack, Failure) : ConvertBytesDouble[Seq[ApronExpr[Addr]],ApronExpr[Addr]] = new LiftedConvert[Seq[Byte], Double, Seq[ApronExpr[Addr]], ApronExpr[Addr], Seq[Topped[Byte]], Topped[Double], SomeCC[ByteOrder]](x => x.map(extract), inject)
// 
// def extract[B: Numeric](expr : ApronExpr[Addr])(using ap: Apron, conv: ConvertCoeff[Mpfr, B]) : Topped[B] = convertToScalarMpfr[B](ap.currentScope.getBound(expr))
// 
// def inject[B](cst : Topped[B])(using Numeric[B]): ApronExpr[Addr] = cst match
//   case Topped.Top => ApronExpr[Addr].topConstant
//   case Topped.Actual(i) => ApronExpr[Addr].Constant(new MpfrScalar(i.toDouble, 0))
// 