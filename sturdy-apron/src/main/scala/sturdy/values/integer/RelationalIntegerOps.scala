package sturdy.values.integer

import apron.Interval
import sturdy.data.given
import sturdy.apron.{*, given}
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

import scala.reflect.ClassTag
import ApronExpr.*
import ApronCons.*
trait RelationalBaseIntegerOps
    [
      L,
      Addr: Ordering: ClassTag,
      Type : ApronType : Join
    ]
    (using
       apronState: ApronState[Addr,Type],
       f: Failure,
       typeIntOps: IntegerOps[L,Type]
    ) extends IntegerOps[L, ApronExpr[Addr,Type]]:
  override def add(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intAdd(v1, v2)

  override def sub(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intSub(v1, v2)

  override def mul(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intMul(v1, v2)


  override def max(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.max(v1._type, v2._type)
    apronState.withTempVars(resultType, v1, v2) { case (result, List(x, y)) =>
      apronState.ifThenElse(lt(x, y)) {
        apronState.assign(result, y)
      } {
        apronState.assign(result, x)
      }
      addr(result, resultType)
    }

  override def min(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.min(v1._type, v2._type)
    apronState.withTempVars(resultType, v1, v2) { case (result, List(x, y)) =>
      apronState.ifThenElse(lt(x, y)) {
        apronState.assign(result, x)
      } {
        apronState.assign(result, y)
      }
      addr(result, resultType)
    }

  override def absolute(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.absolute(v._type)
    apronState.withTempVars(resultType, v) { case (result, List(x)) =>
      apronState.ifThenElse(le(intLit(0, x._type), x)) {
        apronState.assign(result, x)
      } {
        apronState.assign(result, intNegate(x))
      }
      addr(result, resultType)
    }

  override def div(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.div(v1._type, v2._type)
    apronState.withTempVars(resultType, v1, v2) { case (result, List(x, y)) =>
      apronState.join {
        apronState.addConstraint(lt(intLit(0, y._type), y))
        apronState.assign(result, intDiv(x, y))
      } {
        apronState.addConstraint(lt(y, intLit(0, y._type)))
        apronState.assign(result, intDiv(x, y))
      }
      addr(result, resultType)
    }


  override def divUnsigned(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    fromUnsigned(div(toUnsigned(v1), toUnsigned(v2)))

  override def remainder(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.remainder(v1._type, v2._type)
    apronState.withTempVars(resultType, v1, v2) { case (result, List(x, y)) =>
      apronState.join {
        apronState.addConstraint(lt(intLit(0, y._type), y))
        apronState.assign(result, intMod(x, y))
      } {
        apronState.addConstraint(lt(y, intLit(0, y._type)))
        apronState.assign(result, intMod(x, y))
      }
      addr(result, resultType)
    }

  override def remainderUnsigned(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    fromUnsigned(remainder(toUnsigned(v1), toUnsigned(v2)))

  def toUnsigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.divUnsigned(v._type, v._type)
    val unsignedMaxValue = math.pow(2, v._type.byteSize * 8).longValue()
    apronState.withTempVars(resultType, v) { case (result, List(x)) =>
      apronState.ifThenElse(lt(x, intLit(0, x._type))) {
        apronState.assign(result, intAdd(x, longLit(unsignedMaxValue, x._type)))
      } {
        apronState.assign(result, x)
      }
      addr(result, resultType)
    }

  def fromUnsigned(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.divUnsigned(v._type, v._type)
    val unsignedMaxValue = math.pow(2, v._type.byteSize * 8).longValue()
    val signedMaxValue = math.pow(2, v._type.byteSize * 8 - 1).longValue() - 1
    apronState.withTempVars(resultType, v) { case (result, List(x)) =>
      apronState.ifThenElse(lt(longLit(signedMaxValue, x._type), x)) {
        apronState.assign(result, intSub(x, longLit(unsignedMaxValue, x._type)))
      } {
        apronState.assign(result, x)
      }
      addr(result, resultType)
    }

  override def modulo(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.modulo(v1._type, v2._type)
    apronState.withTempVars(resultType, remainder(v1, v2)) { case (result, List(x)) =>
      apronState.ifThenElse(lt(x, intLit(0, x._type))) {
        apronState.assign(result, intAdd(x, v2))
      } {
        apronState.assign(result, x)
      }
      addr(result, resultType)
    }

  override def shiftLeft(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    fromUnsigned(intMul(v, intPow(intLit(2, v._type), modulo(shift, intLit(32, shift._type)))))

  override def shiftRight(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intDiv(v, intPow(intLit(2, v._type), modulo(shift, intLit(32, shift._type))))

  override def shiftRightUnsigned(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    fromUnsigned(shiftRight(toUnsigned(v), shift))

  override def rotateLeft(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def rotateRight(v: ApronExpr[Addr, Type], shift: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def gcd(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def bitAnd(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def bitOr(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def bitXor(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def countLeadingZeros(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.countLeadingZeros(v._type)
    apronState.withTempVars(resultType, v) { case (result, List(x)) =>
      apronState.ifThenElse(lt(intLit(0, x._type), x)) {
        apronState.assign(result, intSub(intLit(v._type.byteSize * 8, resultType), mostSignificantBit(x)))
      } {
        apronState.assign(result, intLit(0, resultType))
      }
      addr(result, resultType)
    }

  def mostSignificantBit(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    intAdd(log(2, v), intLit(1, v._type))

  def log(n: Int, v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeIntOps.countLeadingZeros(v._type)
    apronState.withTempVars(resultType, v) { case (result, List(x)) =>
      val resultExpr = addr(result, resultType)
      apronState.assign(result, ApronExpr.top(resultType))
      apronState.addConstraint(ApronCons.eq(intPow(intLit(n, resultExpr._type), resultExpr), x))
      resultExpr
    }

  override def countTrailingZeros(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def nonzeroBitCount(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

  override def invertBits(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???

given RelationalIntOps
  [
    Addr : Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
    apronState: ApronState[Addr,Type],
    f: Failure,
    typeIntOps: IntegerOps[Int,Type]
  ): RelationalBaseIntegerOps[Int, Addr, Type] with

  override def integerLit(i: Int): ApronExpr[Addr, Type] =
    intLit(i, typeIntOps.integerLit(i))

  override def randomInteger(): ApronExpr[Addr, Type] =
    ApronExpr.top(typeIntOps.integerLit(0))


given RelationalLongOps
  [
    Addr : Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   f: Failure,
   typeIntOps: IntegerOps[Long,Type]
  ): RelationalBaseIntegerOps[Long, Addr, Type] with

  override def integerLit(i: Long): ApronExpr[Addr, Type] =
    longLit(i, typeIntOps.integerLit(i))

  override def randomInteger(): ApronExpr[Addr, Type] =
    ApronExpr.top(typeIntOps.integerLit(0))