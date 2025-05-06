package sturdy.values.floating

import sturdy.data.joinWithFailure
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Topped, *}
import sturdy.values.config.{Bits, Overflow}
import sturdy.values.{Abstractly, Join, MaybeChanged, PartialOrder, Topped}
import sturdy.values.convert.{&&, ConversionFailure, NilCC, SomeCC}
import sturdy.values.integer.{ConvertIntDouble, ConvertIntFloat, ConvertLongDouble, ConvertLongFloat, NumericInterval}
import sturdy.values.floating.{ConvertBytesDouble, ConvertBytesFloat, ConvertDoubleBytes, ConvertFloatBytes}
import sturdy.values.ordering.{EqOps, OrderingOps}
import sturdy.values.config.BytesSize

import java.nio.ByteOrder
import java.lang.Float.{NaN, floatToIntBits, intBitsToFloat}
import java.lang.Double.{doubleToLongBits, longBitsToDouble}
import Ordering.Implicits.infixOrderingOps
import Numeric.Implicits.infixNumericOps
import scala.collection.immutable.TreeSet
import scala.reflect.ClassTag




case class FloatingInterval[F](l: F, h: F, NaN: Boolean)(using ord: Ordering[F]):
  if (l > h) throw new IllegalArgumentException(s"Empty intervals are illegal $this")
  override def toString: String =
    s"[$l, $h, containsNaN:$NaN]"
  def containsNum(f: F) = f >= l && f <= h
  def containsSubIntervall(other: FloatingInterval[F]) = l <= other.l && other.h <= h
  def isConstant: Boolean = l == h
  def isNaN(using ct: ClassTag[F]): Boolean =
    if ct.runtimeClass == classOf[Float] then l.asInstanceOf[Float].isNaN || h.asInstanceOf[Float].isNaN
    else if ct.runtimeClass == classOf[Double] then l.asInstanceOf[Double].isNaN || h.asInstanceOf[Double].isNaN
    else throw new Exception("type nots supported")


object FloatingInterval:
  def constant[F](f: F)(using Ordering[F]): FloatingInterval[F] =

    if f.isInstanceOf[Float] then
      if f.asInstanceOf[Float].isNaN then
        FloatingInterval(f, f, true)
      else
        FloatingInterval(f, f, false)
    else if f.isInstanceOf[Double] then
      if f.asInstanceOf[Double].isNaN then
        FloatingInterval(f, f, true)
      else
        FloatingInterval(f, f, false)
    else
      throw new Exception("Type not supported")

class FloatingIntervalOps[F](using ord: Ordering[F], ops: FloatOps[F,F], num: Numeric[F], ct: ClassTag[F])
                            (using f: Failure, j: EffectStack)
  extends FloatOps[F, FloatingInterval[F]]:

  private val zero = num.fromInt(0)
  private val one = num.fromInt(1)
  val minNonInfinity = (if ct.runtimeClass == classOf[Float] then -Float.MaxValue else -Double.MaxValue).asInstanceOf[F]
  val maxNonInfinity = (if ct.runtimeClass == classOf[Float] then Float.MaxValue else Double.MaxValue).asInstanceOf[F]

  private def FIsNaN(f: F): Boolean =
    if f.isInstanceOf[Float] then f.asInstanceOf[Float].isNaN
    else if f.isInstanceOf[Double] then f.asInstanceOf[Double].isNaN
    else throw new Exception("Type not supported")

  private def isNaN(v: FloatingInterval[F]): Boolean =
    if ct.runtimeClass == classOf[Float] then v.l.asInstanceOf[Float].isNaN && v.h.asInstanceOf[Float].isNaN
    else if ct.runtimeClass == classOf[Double] then v.l.asInstanceOf[Double].isNaN && v.h.asInstanceOf[Double].isNaN
    else throw new Exception("Type not supported")

  private def valueNegative(v: F) =
    if ct.runtimeClass == classOf[Float] then
      if v.asInstanceOf[Float] == -0.0f && 1.0f / v.asInstanceOf[Float] == Float.NegativeInfinity then
        true
      else
        v < zero
    else
      if v.asInstanceOf[Double] == -0.0d && 1.0d / v.asInstanceOf[Double] == Double.NegativeInfinity then
        true
      else
        v < zero
  override def floatingLit(f: F): FloatingInterval[F] = FloatingInterval.constant(f)

  override def randomFloat(): FloatingInterval[F] = FloatingInterval(zero,one, false)

  override def add(v1: FloatingInterval[F], v2: FloatingInterval[F]): FloatingInterval[F] =
    //TODO if v1.l v1.h contains other numbers than infinity => v1.l != v1.h or v2.l != v2.h then try to retain these values
    // or try to catch the values that lead to NaN
    val newL = ops.add(v1.l,v2.l)
    val newH = ops.add(v1.h,v2.h)

    val newNaN =
      if ct.runtimeClass == classOf[Float] then
        newL.asInstanceOf[Float].isNaN || newH.asInstanceOf[Float].isNaN
      else if ct.runtimeClass == classOf[Double] then
        newL.asInstanceOf[Double].isNaN || newH.asInstanceOf[Double].isNaN
      else throw new Exception("Type not supported")
    FloatingInterval(newL, newH, newNaN || v1.NaN || v2.NaN)

  override def sub(v1: FloatingInterval[F], v2: FloatingInterval[F]): FloatingInterval[F] =
    val newL = ops.sub(v1.l,v2.h)
    val newH = ops.sub(v1.h,v2.l)
    val newNaN = if ct.runtimeClass == classOf[Float] then
      newL.asInstanceOf[Float].isNaN || newH.asInstanceOf[Float].isNaN
    else if ct.runtimeClass == classOf[Double] then
      newL.asInstanceOf[Double].isNaN || newH.asInstanceOf[Double].isNaN
    else
      throw new Exception("Type not supported")
    FloatingInterval(newL, newH, newNaN || v1.NaN || v2.NaN)


  def removeInfinities(v: FloatingInterval[F]): FloatingInterval[F] =
    // what to do for NaN
    if v.isConstant then return v
    FloatingInterval(ops.max(v.l, minNonInfinity), ops.min(v.h, maxNonInfinity), v.NaN)

  def funcWithNaN(f1: F, f2: F, func: (F,F) => F): F =
    if ct.runtimeClass == classOf[Float] then
      if f1.asInstanceOf[Float].isNaN then
        if f2.asInstanceOf[Float].isNaN then f1
        else f2
      else if f2.asInstanceOf[Float].isNaN then f1
      else func(f1,f2)
    else
      if f1.asInstanceOf[Double].isNaN then
        if f2.asInstanceOf[Double].isNaN then f1
        else f2
      else if f2.asInstanceOf[Double].isNaN then f1
      else func(f1,f2)

  override def mul(v1: FloatingInterval[F], v2: FloatingInterval[F]): FloatingInterval[F] =
    // cases where intervall mult with constant => creat non-infinity interval
    // cases where bounds only results in NaN but other values are also contined
    def multResultsNan(f1: FloatingInterval[F], f2: FloatingInterval[F]): Boolean =
      if ct.runtimeClass == classOf[Float] then
        (f1.containsNum(Float.NegativeInfinity.asInstanceOf[F]) || f1.containsNum(Float.PositiveInfinity.asInstanceOf[F]))
          && (f2.containsNum(-0.0f.asInstanceOf[F]) || f2.containsNum(0.0f.asInstanceOf[F]))
      else if ct.runtimeClass == classOf[Double] then
        (f1.containsNum(Double.NegativeInfinity.asInstanceOf[F]) || f1.containsNum(Double.PositiveInfinity.asInstanceOf[F]))
          && (f2.containsNum(-0.0d.asInstanceOf[F]) || f2.containsNum(0.0d.asInstanceOf[F]))
      else throw new Exception("Type not supported ")



    val new1 = ops.mul(v1.l, v2.l)
    val new2 = ops.mul(v1.l, v2.h)
    val new3 = ops.mul(v1.h, v2.l)
    val new4 = ops.mul(v1.h, v2.h)

    val v1NoInf = removeInfinities(v1)
    val v2NoInf = removeInfinities(v2)

    val new1NoInf = ops.mul(v1NoInf.l, v2.l)
    val new1NoInf2 = ops.mul(v1.l, v2NoInf.l)
    val new2NoInf = ops.mul(v1NoInf.l, v2.h)
    val new2NoInf2 = ops.mul(v1.l, v2NoInf.h)
    val new3NoInf = ops.mul(v1NoInf.h, v2.l)
    val new3NoInf2 = ops.mul(v1.h, v2NoInf.l)
    val new4NoInf = ops.mul(v1NoInf.h, v2.h)
    val new4NoInf2 = ops.mul(v1.h, v2NoInf.h)

    val maxV = funcOnSeq(Seq(new1,new2,new3,new4,new1NoInf,new1NoInf2,new2NoInf,new2NoInf2,new3NoInf,new3NoInf2,new4NoInf,new4NoInf2),ops.max)
    val minV = funcOnSeq(Seq(new1,new2,new3,new4,new1NoInf,new1NoInf2,new2NoInf,new2NoInf2,new3NoInf,new3NoInf2,new4NoInf,new4NoInf2),ops.min)
    val Nan = multResultsNan(v1,v2) || multResultsNan(v2,v1) // when this is true infinite time 0 and new is one NaN then

    FloatingInterval(minV, maxV, v1.NaN || v2.NaN || Nan)

  def extractClosestToZero(v: FloatingInterval[F]): (F,F) =
    // what about case where zero is either l or h
    if v.h < zero then
      (v.h, v.h)
    else if v.l > zero then (v.l,v.l)
    else
      (-zero, zero)

  def funcOnSeq(seq: Seq[F], func: (F,F) => F) =
    seq.drop(1).foldLeft(seq.head)((f1,f2)=> funcWithNaN(f1,f2,func))
  override def div(v1: FloatingInterval[F], v2: FloatingInterval[F]): FloatingInterval[F] =
    //TODO special case when values from -1 to 1 are contained but are not a bound, difference -0 and +0 ?
    // extract closted number to 0 from positve and negative and use them as lowerbound/upperbound
    // if [-inf, inf] or [-0,inf],[inf,-0] then try to remove 0 /infinities
    def divResultsNan(f1: FloatingInterval[F], f2: FloatingInterval[F]): Boolean =
      if ct.runtimeClass == classOf[Float] then
        ((f1.containsNum(Float.NegativeInfinity.asInstanceOf[F]) || f1.containsNum(Float.PositiveInfinity.asInstanceOf[F]))
          && (f2.containsNum(Float.NegativeInfinity.asInstanceOf[F]) || f2.containsNum(Float.PositiveInfinity.asInstanceOf[F])))
          || ((f1.containsNum(-0.0f.asInstanceOf[F]) || f1.containsNum(0.0f.asInstanceOf[F]))
          && (f2.containsNum(-0.0f.asInstanceOf[F]) || f2.containsNum(0.0f.asInstanceOf[F])))
      else if ct.runtimeClass == classOf[Double] then
        ((f1.containsNum(Double.NegativeInfinity.asInstanceOf[F]) || f1.containsNum(Double.PositiveInfinity.asInstanceOf[F]))
          && (f2.containsNum(Double.NegativeInfinity.asInstanceOf[F]) || f2.containsNum(Double.PositiveInfinity.asInstanceOf[F])))
          || ((f1.containsNum(-0.0d.asInstanceOf[F]) || f1.containsNum(0.0d.asInstanceOf[F]))
          && (f2.containsNum(-0.0d.asInstanceOf[F]) || f2.containsNum(0.0d.asInstanceOf[F])))
      else throw new Exception("Type not supported ")


    // if v1.containsNum(zero) then println("v1 zero") // result contains 0
    //if v2.containsNum(zero) then println("v2 zero") // results in infinity if v1 contains number other than zero if both zero nan
    val new1 = ops.div(v1.l, v2.l)
    val new2 = ops.div(v1.l, v2.h)
    val new3 = ops.div(v1.h, v2.l)
    val new4 = ops.div(v1.h, v2.h)

    val v1NoInf = removeInfinities(v1)
    val v2NoInf = removeInfinities(v2)

    val new1NoInf = ops.div(v1NoInf.l, v2.l)
    val new1NoInf2 = ops.div(v1.l, v2NoInf.l) // put into Seq/array
    val new2NoInf = ops.div(v1NoInf.l, v2.h)
    val new2NoInf2 = ops.div(v1.l, v2NoInf.h)
    val new3NoInf = ops.div(v1NoInf.h, v2.l)
    val new3NoInf2 = ops.div(v1.h, v2NoInf.l)
    val new4NoInf = ops.div(v1NoInf.h, v2.h)
    val new4NoInf2 = ops.div(v1.h, v2NoInf.h)

    val closestToZero1 = extractClosestToZero(v1)
    val closestToZero2= extractClosestToZero(v2)
    val new1With0 = ops.div(closestToZero1._1, closestToZero2._1)
    val new2With0 = ops.div(closestToZero1._1, closestToZero2._2)
    val new3With0 = ops.div(closestToZero1._2, closestToZero2._1)
    val new4With0 = ops.div(closestToZero1._2, closestToZero2._2)


    val maxV = funcOnSeq(Seq(new1,new2,new3,new4,new1With0,new2With0,new3With0,new4With0,new1NoInf,new1NoInf2,new2NoInf,new2NoInf2,new3NoInf,new3NoInf2,new4NoInf,new4NoInf2),ops.max)
    val minV = funcOnSeq(Seq(new1,new2,new3,new4,new1With0,new2With0,new3With0,new4With0,new1NoInf,new1NoInf2,new2NoInf,new2NoInf2,new3NoInf,new3NoInf2,new4NoInf,new4NoInf2),ops.min)
    val Nan = divResultsNan(v1,v2) || divResultsNan(v2,v1)


    FloatingInterval(minV, maxV, v1.NaN || v2.NaN || Nan)

  override def min(v1: FloatingInterval[F], v2: FloatingInterval[F]): FloatingInterval[F] =
    // if either is definite NAN then return nAn
    FloatingInterval(ops.min(v1.l, v2.l), ops.min(v1.h, v2.h), v1.NaN || v2.NaN)

  override def max(v1: FloatingInterval[F], v2: FloatingInterval[F]): FloatingInterval[F] =
    FloatingInterval(ops.max(v1.l, v2.l), ops.max(v1.h, v2.h), v1.NaN || v2.NaN)

  override def absolute(v: FloatingInterval[F]): FloatingInterval[F] =
    val vl = ops.absolute(v.l)
    val vh = ops.absolute(v.h)
    val newH = ops.max(vl ,vh)
    val newL = if v.l < zero && v.h >= zero then zero else ops.min(vl, vh)
    FloatingInterval(newL, newH, v.NaN)

  override def negated(v: FloatingInterval[F]): FloatingInterval[F] =
    // recheck what about zero?
    val vl = ops.negated(v.l)
    val vh = ops.negated(v.h)
    val newH = ops.max(vl, vh)
    val newL = ops.min(vl, vh)
    FloatingInterval(newL, newH, v.NaN)

  override def sqrt(v: FloatingInterval[F]): FloatingInterval[F] =
    if v.h < zero then
      val Nan = (if ct.runtimeClass == classOf[Float] then Float.NaN else Double.NaN).asInstanceOf[F]
      FloatingInterval(Nan, Nan, true)
    else if v.l < zero
    // is -0.0 really necessary here?
    then FloatingInterval(ops.negated(zero),ops.sqrt(v.h), true)
    else
      FloatingInterval(ops.sqrt(v.l), ops.sqrt(v.h), v.NaN)

  override def ceil(v: FloatingInterval[F]): FloatingInterval[F] =
    FloatingInterval(ops.ceil(v.l), ops.ceil(v.h), v.NaN)

  override def floor(v: FloatingInterval[F]): FloatingInterval[F] =
    FloatingInterval(ops.floor(v.l),ops.floor(v.h), v.NaN)

  override def truncate(v: FloatingInterval[F]): FloatingInterval[F] =
    FloatingInterval(ops.truncate(v.l), ops.truncate(v.h), v.NaN)

  override def nearest(v: FloatingInterval[F]): FloatingInterval[F] =
    FloatingInterval(ops.nearest(v.l), ops.nearest(v.h), v.NaN)

  override def copysign(v: FloatingInterval[F], sign: FloatingInterval[F]): FloatingInterval[F] =
    // what happens if v is NaN ?
    if isNaN(sign) then
      if v.l == v.h then
        if valueNegative(v.l) then return FloatingInterval(v.l, ops.negated(v.l), v.NaN) else return FloatingInterval(ops.negated(v.l), v.l, v.NaN)
      else if ops.absolute(v.l) > ops.absolute(v.h) then return FloatingInterval(v.l, ops.absolute(v.l), v.NaN)
      else
        return FloatingInterval(ops.negated(v.h),v.h, v.NaN)
    if (valueNegative(sign.h) && valueNegative(v.h)) || (!valueNegative(sign.l) && !valueNegative(v.l)) then
      FloatingInterval(v.l,v.h, v.NaN)
    else if (valueNegative(sign.h) && !valueNegative(v.l)) || (!valueNegative(sign.l) && valueNegative(v.h)) then
      FloatingInterval(ops.negated(v.l),ops.negated(v.h), v.NaN)
    else if ops.absolute(v.l) > ops.absolute(v.h) then FloatingInterval(v.l, ops.absolute(v.l), v.NaN)
    else FloatingInterval(ops.negated(v.h),v.h, v.NaN)


given FloatingIntervalFloatOps[F](using Ordering[F], FloatOps[F,F], Numeric[F])(using f: Failure, j: EffectStack, ct: ClassTag[F]): FloatOps[F, FloatingInterval[F]] =
  new FloatingIntervalOps[F]()

given FloatingIntervalOrderingOps[F](using Ordering[F], Numeric[F]): OrderingOps[FloatingInterval[F], Topped[Boolean]] with
  def lt(iv1: FloatingInterval[F], iv2: FloatingInterval[F]): Topped[Boolean] =
    // what about NaN?
    val zero = Numeric[F].fromInt(0)
    if (iv1.h < iv2.l) && iv1.h != zero && iv2.l != zero then
      Topped.Actual(true)
    else if iv2.h <= iv1.l then
      Topped.Actual(false)
    else Topped.Top
  def le(iv1: FloatingInterval[F], iv2: FloatingInterval[F]): Topped[Boolean] =
    if iv1.h <= iv2.l then Topped.Actual(true)
    else if iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top

given FloatingIntervalEqOps[F](using Ordering[F], ClassTag[F]): EqOps[FloatingInterval[F], Topped[Boolean]] with
  override def equ(iv1: FloatingInterval[F], iv2: FloatingInterval[F]): Topped[Boolean] =
    if iv1.isNaN || iv2.isNaN then
      return Topped.Actual(false)
    val containsNaN = iv1.NaN || iv2.NaN
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then
      if containsNaN then Topped.Top else Topped.Actual(true)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(false)
    else Topped.Top
  override def neq(iv1: FloatingInterval[F], iv2: FloatingInterval[F]): Topped[Boolean] =
    if iv1.isNaN || iv2.isNaN then
      return Topped.Actual(true)
    val containsNaN = iv1.NaN || iv2.NaN
    if iv1.l == iv1.h && iv1.h == iv2.l && iv2.l == iv2.h then
      if containsNaN then Topped.Top else Topped.Actual(false)
    else if iv1.h < iv2.l || iv2.h < iv1.l then Topped.Actual(true)
    else Topped.Top


given FloatingIntervalAbstractly[F](using ord: Ordering[F]): Abstractly[F, FloatingInterval[F]] with
  override def apply(c: F): FloatingInterval[F] = FloatingInterval.constant(c)

//TODO include when v1.l or v1.h is NaN
given FloatingIntervalOrdering[F](using ord: Ordering[F]): PartialOrder[FloatingInterval[F]] with
  override def lteq(x: FloatingInterval[F], y: FloatingInterval[F]): Boolean =
    // if 0 is contained then -0 is also contained
    if x.NaN then
      y.NaN
    else
      y.l <= x.l && x.h <= y.h

//TODO include when v1.l or v1.h is NaN
given FloatingIntervalJoin[F](using ord: Ordering[F]): Join[FloatingInterval[F]] with
  override def apply(v1: FloatingInterval[F], v2: FloatingInterval[F]): MaybeChanged[FloatingInterval[F]] =
    MaybeChanged(FloatingInterval(v1.l.min(v2.l), v1.h.max(v2.h), v1.NaN || v2.NaN),v1)

given FloatingIntervalTop[F](using ct: ClassTag[F])(using Ordering[F]): Top[FloatingInterval[F]] with
  override def top: FloatingInterval[F] =
    if ct.runtimeClass == classOf[Float] then FloatingInterval(Float.NegativeInfinity.asInstanceOf[F], Float.PositiveInfinity.asInstanceOf[F], true)
    else if ct.runtimeClass == classOf[Double] then FloatingInterval(Double.NegativeInfinity.asInstanceOf[F], Double.PositiveInfinity.asInstanceOf[F], true)
    else throw new Exception(s"Clas not supported: ${ct.runtimeClass}")


given ConvertFloatingIntervalFloatDouble[F,D](using Numeric[F], Numeric[D])
                                             (using convert: ConvertFloatDouble[F, D]): ConvertFloatDouble[FloatingInterval[F],FloatingInterval[D]] with
  override def apply(from: FloatingInterval[F], conf: NilCC.type): FloatingInterval[D] = FloatingInterval(convert(from.l,conf),convert(from.h,conf), from.NaN)

given ConvertFloatingIntervalDoubleFloat[D,F](using Numeric[D], Numeric[F])
                                             (using convert: ConvertDoubleFloat[D,F]): ConvertDoubleFloat[FloatingInterval[D],FloatingInterval[F]] with
  override def apply(from: FloatingInterval[D], conf: NilCC.type): FloatingInterval[F] = FloatingInterval(convert(from.l,conf),convert(from.h,conf), from.NaN)


given ConvertFloatingIntervalFloatInt(using Numeric[Float], Numeric[Int])
                                     (using convert: ConvertFloatInt[Float,Int], f: Failure, e:EffectStack): ConvertFloatInt[FloatingInterval[Float],NumericInterval[Int]] with
  override def apply(from: FloatingInterval[Float], conf: Overflow && Bits): NumericInterval[Int] =
    conf match
      case (_ && config.Bits.Raw) =>
        //TODO what about negative/positve NaN values that do not occur there?
        if from.isNaN then NumericInterval(-8388607, Int.MaxValue)
        if (from.l >= 0 && floatToIntBits(from.l) != floatToIntBits(-0.0f)) then
          NumericInterval(convert(from.l, conf), if from.NaN then Int.MaxValue else convert(from.h, conf))
        else if (from.h < 0 || floatToIntBits(from.h) == floatToIntBits(-0.0f)) then
          NumericInterval(convert(from.h, conf), if from.NaN then -1 else convert(from.h, conf))
        else NumericInterval(convert(-0.0f, conf), if from.NaN then Int.MaxValue else convert(from.h, conf))
        NumericInterval(Int.MinValue, Int.MaxValue)
      case (config.Overflow.Fail && config.Bits.Signed) =>
        if from.l.isNaN || from.h.isNaN || from.h < Int.MinValue.toFloat || from.l >= -Int.MinValue.toFloat then
          (f.fail(ConversionFailure, s"float $from out of integer range"))
        else if from.l < Int.MinValue.toFloat || from.h >= -Int.MinValue.toFloat || from.NaN then
          val l = if from.l < Int.MinValue.toFloat then Int.MinValue else convert(from.l,conf)
          val h = if from.h >= -Int.MinValue.toFloat then Int.MaxValue else convert(from.h,conf)
          joinWithFailure(NumericInterval(l,h))(f.fail(ConversionFailure, s"float $from out of integer range"))
        else NumericInterval(convert(from.l,conf),convert(from.h,conf))

      case (config.Overflow.Fail && config.Bits.Unsigned) =>
        if from.l.isNaN || from.h.isNaN || from.h <= -1.0f || from.l >= -Int.MinValue.toDouble * 2.0d then
          (f.fail(ConversionFailure, s"float $from out of integer range"))
        else if from.l <= -1.0f || from.h >= -Int.MinValue.toDouble * 2.0d || from.NaN then
          // rethink values over Int.MaxValue go overflow into -127
          // rethink ranges check on values if range is not correct then trr to change them to correct values
          var l = if from.l <= -1.0f then 0 else convert(from.l,conf)
          var h = if from.h >= -Int.MinValue.toDouble * 2.0d then -1 else convert(from.h,conf)
          if l >= 0 && h < 0 then l = Int.MinValue; h = Int.MaxValue
          joinWithFailure(NumericInterval(l,h))(f.fail(ConversionFailure, s"float $from out of integer range"))
        else NumericInterval(convert(from.l,conf),convert(from.h,conf))

      case (config.Overflow.JumpToBounds && config.Bits.Unsigned) =>
        var l = convert(from.l, conf)
        var h = convert(from.h, conf)
        if l >= 0 && h < 0 then l = Int.MinValue; h = Int.MaxValue
        NumericInterval(l,h)
      case _ => NumericInterval(convert(from.l,conf),convert(from.h,conf))

given ConvertFloatingIntervalIntFloat(using Numeric[Int], Numeric[Float])
                                     (using convert: ConvertIntFloat[Int,Float], j: Join[FloatingInterval[Float]]): ConvertIntFloat[NumericInterval[Int],FloatingInterval[Float]] with
  override def apply(from: NumericInterval[Int], conf: Bits): FloatingInterval[Float] =


    var newL = convert(from.low,conf)
    var newH = convert(from.high,conf)
    if conf == config.Bits.Unsigned then
      if from.low < 0 && from.high >= 0 then newL = convert(0,conf); newH = convert(-1,conf)

    // neg inf: -8388608
    // pos inf: 2139095040
    // start pos Nan: 2139095041
    // end pos NaN: 2147483647
    // start neg NaN: -1
    // end neg NaN: -8388607
    if conf == config.Bits.Raw then
      if from.low >= 0 then
        val posInf = from.containsNum(2139095040)
        val onlyNan = from.low >= (2139095041) && from.high <= Float.MaxValue
        val containsNaN = from.high <= Float.MaxValue && from.high >= 2139095041
        val smallestValue = from.low
        val biggestNonSpecialValue = if from.high <  2139095040 then from.high else  2139095039
        if onlyNan then FloatingInterval(Float.NaN, Float.NaN, true)
        else if posInf then FloatingInterval(convert(smallestValue, conf), Float.PositiveInfinity, containsNaN)
        else FloatingInterval(convert(smallestValue, conf),convert(biggestNonSpecialValue, conf), containsNaN)
      else if from.high < 0 then
        val negInf = from.containsNum(-8388608)
        val onlyNan = from.low >= (-8388607) && from.high <= -1
        val containsNaN = from.high <= -1L && from.high >= -8388607
        val smallestValue = from.low
        val biggestNonSpecialValue = if from.high < -8388608 then from.high else -8388609
        if onlyNan then FloatingInterval(Float.NaN, Float.NaN, true)
        else if negInf then FloatingInterval(Float.NegativeInfinity, convert(smallestValue, conf), containsNaN)
        else FloatingInterval(convert(smallestValue, conf),convert(biggestNonSpecialValue, conf), containsNaN)

      else
        val posRes = apply(NumericInterval(0, from.high), conf)
        val negRes = apply(NumericInterval(from.low, -1), conf)
        j.apply(posRes,negRes).get
    else
      FloatingInterval(if newL.isNaN then newH else newL, if newH.isNaN then newL else newH, newL.isNaN || newH.isNaN)


given ConvertFloatingIntervalFloatLong(using Numeric[Float], Numeric[Long])
                                      (using convert: ConvertFloatLong[Float,Long], f: Failure, e:EffectStack): ConvertFloatLong[FloatingInterval[Float],NumericInterval[Long]] with
  override def apply(from: FloatingInterval[Float], conf: Overflow && Bits): NumericInterval[Long] =
    conf match
      case (_ && config.Bits.Raw) => NumericInterval(Long.MinValue, Long.MaxValue) // watch out for neg values compared to neg values they are reversed
      case (config.Overflow.Fail && config.Bits.Signed) =>
        if from.l.isNaN || from.h.isNaN || from.h < Long.MinValue.toFloat || from.l >= -Long.MinValue.toFloat then
          (f.fail(ConversionFailure, s"float $from out of long range"))
        else if from.l < Long.MinValue.toFloat || from.h >= -Long.MinValue.toFloat * 2.0d then
          val l = if from.l < Long.MinValue.toFloat then Long.MinValue else convert(from.l,conf)
          val h = if from.h >= -Long.MinValue.toFloat * 2.0d then Long.MaxValue else convert(from.h,conf)
          joinWithFailure(NumericInterval(l,h))(f.fail(ConversionFailure, s"float $from out of long range"))
        else NumericInterval(convert(from.l,conf),convert(from.h,conf))

      case (config.Overflow.Fail && config.Bits.Unsigned) =>
        if from.l.isNaN || from.h.isNaN || from.h <= -1.0d || from.l >= -Long.MinValue.toDouble * 2.0d then
          (f.fail(ConversionFailure, s"float $from out of long range"))
        else if from.l <= -1.0d || from.h >= -Long.MinValue.toDouble * 2.0d then
          var l = if from.l < -1.0d then 0 else convert(from.l,conf)
          var h = if from.h >= -Long.MinValue.toDouble * 2.0d then -1 else convert(from.h,conf)
          if l >= 0 && h < 0 then l = Long.MinValue; h = Long.MaxValue
          joinWithFailure(NumericInterval(l,h))(f.fail(ConversionFailure, s"float $from out of long range"))
        else NumericInterval(convert(from.l,conf),convert(from.h,conf))
      case (config.Overflow.JumpToBounds && config.Bits.Unsigned) =>
        var l = convert(from.l, conf)
        var h = convert(from.h, conf)
        if l >= 0 && h < 0 then l = Long.MinValue; h = Long.MaxValue
        NumericInterval(l,h)
      case _ => NumericInterval(convert(from.l,conf),convert(from.h,conf))

given ConvertFloatingIntervalLongFloat(using Numeric[Long], Numeric[Float])
                                      (using convert: ConvertLongFloat[Long,Float]): ConvertLongFloat[NumericInterval[Long],FloatingInterval[Float]] with
  override def apply(from: NumericInterval[Long], conf: Bits): FloatingInterval[Float] =
    var newL = convert(from.low, conf)
    var newH = convert(from.high, conf)
    if conf == config.Bits.Unsigned then
      if from.low < 0 && from.high >= 0 then
        newL = convert(0, conf)
        newH = convert(-1, conf)

    FloatingInterval(if newL.isNaN then newH else newL, if newH.isNaN then newL else newH, newL.isNaN || newH.isNaN)


given ConvertFloatingIntervalDoubleInt(using Numeric[Double], Numeric[Int])
                                      (using convert: ConvertDoubleInt[Double,Int], f: Failure, e: EffectStack): ConvertDoubleInt[FloatingInterval[Double],NumericInterval[Int]] with
  override def apply(from: FloatingInterval[Double], conf: Overflow && Bits): NumericInterval[Int] =
    conf match
      case (_ && config.Bits.Raw) => NumericInterval(Int.MinValue, Int.MaxValue)
      case (config.Overflow.Fail && config.Bits.Signed) =>
        if from.l.isNaN || from.h.isNaN || from.h <=  Int.MinValue.toDouble - 1 || from.l >= -Int.MinValue.toDouble then
          (f.fail(ConversionFailure, s"float $from out of long range"))
        else if from.l <=  Int.MinValue.toDouble - 1 || from.h >= -Int.MinValue.toDouble then
          val l = if from.l < Int.MinValue.toDouble - 1 then Int.MinValue else convert(from.l,conf)
          val h = if from.h >= -Int.MinValue.toDouble then Int.MaxValue else convert(from.h,conf)
          joinWithFailure(NumericInterval(l,h))(f.fail(ConversionFailure, s"float $from out of long range"))
        else NumericInterval(convert(from.l,conf),convert(from.h,conf))

      case (config.Overflow.Fail && config.Bits.Unsigned) =>
        if from.l.isNaN || from.h.isNaN || from.h <= -1.0d || from.l >= -Int.MinValue.toDouble * 2.0d then
          (f.fail(ConversionFailure, s"float $from out of long range"))
        else if from.l <= -1.0d || from.h >= -Int.MinValue.toDouble * 2.0d then
          var l = if from.l < -1.0d then 0 else convert(from.l,conf)
          var h = if from.h >= -Int.MinValue.toDouble * 2.0d then -1 else convert(from.h,conf)
          if l >= 0 && h < 0 then l = Int.MinValue; h = Int.MaxValue
          joinWithFailure(NumericInterval(l,h))(f.fail(ConversionFailure, s"float $from out of long range"))
        else NumericInterval(convert(from.l,conf),convert(from.h,conf)) // case whre f is between Int.maxvalue and Int.maxvalue * 2 => messes up interval
      case (config.Overflow.JumpToBounds && config.Bits.Unsigned) =>
        var l = convert(from.l, conf)
        var h = convert(from.h, conf)
        if l >= 0 && h < 0 then l = Int.MinValue; h = Int.MaxValue
        NumericInterval(l,h)
      case _ => NumericInterval(convert(from.l,conf),convert(from.h,conf))


given ConvertFloatingIntervalIntDouble(using Numeric[Int], Numeric[Double])
                                      (using convert: ConvertIntDouble[Int,Double]): ConvertIntDouble[NumericInterval[Int],FloatingInterval[Double]] with
  override def apply(from: NumericInterval[Int], conf: Bits): FloatingInterval[Double] =
    var newL = convert(from.low, conf)
    var newH = convert(from.high, conf)
    if conf == config.Bits.Unsigned then
      if from.low < 0 && from.high >= 0 then
        newL = convert(0, conf)
        newH = convert(-1, conf)
    // what if only one value is NaN
    if conf == config.Bits.Raw then return FloatingInterval(Double.NegativeInfinity, Double.PositiveInfinity, true)

    FloatingInterval(if newL.isNaN then newH else newL, if newH.isNaN then newL else newH, newL.isNaN || newH.isNaN)

given ConvertFloatingIntervalDoubleLong(using Numeric[Double], Numeric[Long])
                                       (using convert: ConvertDoubleLong[Double,Long], f: Failure, e: EffectStack): ConvertDoubleLong[FloatingInterval[Double],NumericInterval[Long]] with
  override def apply(from: FloatingInterval[Double], conf: Overflow && Bits): NumericInterval[Long] =
    conf match
      case (_ && config.Bits.Raw) =>
        if from.isNaN then NumericInterval(-4503599627370495L, Long.MaxValue)
        if (from.l >= 0 && doubleToLongBits(from.l) != doubleToLongBits(-0.0d)) then
          NumericInterval(convert(from.l, conf), if from.NaN then Long.MaxValue else convert(from.h, conf)) // what about neg NAn values
        else if (from.h < 0 || doubleToLongBits(from.h) == doubleToLongBits(-0.0d)) then
          NumericInterval(convert(from.h, conf), if from.NaN then -1 else convert(from.h, conf))
        else
          println(convert(-0.0f, conf))
          println(from)
          println(NumericInterval(convert(-0.0f, conf), if from.NaN then Long.MaxValue else convert(from.h, conf)))
          NumericInterval(convert(-0.0f, conf), if from.NaN then Long.MaxValue else convert(from.h, conf))
      case (config.Overflow.Fail && config.Bits.Signed) =>
        if from.l.isNaN || from.h.isNaN || from.h < Long.MinValue.toDouble || from.l >= -Long.MinValue.toDouble then
          (f.fail(ConversionFailure, s"float $from out of long range"))
        else if from.l < Long.MinValue.toDouble  || from.h >= -Long.MinValue.toDouble then
          val l = if from.l < Long.MinValue.toDouble then Long.MinValue else convert(from.l,conf)
          val h = if from.h >= -Long.MinValue.toDouble then Long.MaxValue else convert(from.h,conf)
          joinWithFailure(NumericInterval(l,h))(f.fail(ConversionFailure, s"float $from out of long range"))
        else NumericInterval(convert(from.l, conf), convert(from.h, conf))

      case (config.Overflow.Fail && config.Bits.Unsigned) =>
        if from.l.isNaN || from.h.isNaN || from.h <= -1.0d || from.l >= -Long.MinValue.toDouble * 2.0d then
          (f.fail(ConversionFailure, s"float $from out of long range"))
        else if from.l <= -1.0d || from.h >= -Long.MinValue.toDouble * 2.0d then
          var l = if from.l < -1.0d then 0 else convert(from.l,conf)
          var h = if from.h >= -Long.MinValue.toDouble * 2.0d then -1 else convert(from.h,conf)
          if l >= 0 && h < 0 then l = Long.MinValue; h = Long.MaxValue
          joinWithFailure(NumericInterval(l,h))(f.fail(ConversionFailure, s"float $from out of long range"))
        else NumericInterval(convert(from.l, conf), convert(from.h, conf))
      case (config.Overflow.JumpToBounds && config.Bits.Unsigned) =>
        var l = convert(from.l, conf)
        var h = convert(from.h, conf)
        if l >= 0 && h < 0 then l = Long.MinValue; h = Long.MaxValue
        NumericInterval(l,h)
      case _ => NumericInterval(convert(from.l, conf), convert(from.h, conf))
// how to handle jumpOutOfBounds and Unsigned values seperate between values above Int.MaxValue( neg Values


given ConvertFloatingIntervalLongDouble(using Numeric[Long], Numeric[Double])
                                       (using convert: ConvertLongDouble[Long,Double], j: Join[FloatingInterval[Double]])
: ConvertLongDouble[NumericInterval[Long],FloatingInterval[Double]] with
  override def apply(from: NumericInterval[Long], conf: Bits): FloatingInterval[Double] =
    var newL = convert(from.low, conf)
    var newH = convert(from.high, conf)
    if conf == config.Bits.Unsigned then
      if from.low < 0 && from.high >= 0 then
        newL = convert(0, conf)
        newH = convert(-1, conf)

    // pos inf: 9218868437227405312
    // neg inf:4503599627370496
    // neg NAN start: -4503599627370495
    // neg NAN end: -1
    // pos NAn start: 9218868437227405313
    // pos double max value pos NaN end
    if conf == config.Bits.Raw then
      if from.low >= 0 then
        val posInf = from.containsNum(9218868437227405312L)
        val onlyNan = from.low >= (9218868437227405313L) && from.high <= Long.MaxValue
        val containsNaN = from.high <= Long.MaxValue && from.high >= 9218868437227405313L
        val smallestValue = from.low
        val biggestNonSpecialValue = if from.high < 9218868437227405312L then from.high else 9218868437227405311L
        if onlyNan then FloatingInterval(Double.NaN, Double.NaN, true)
        else if posInf then FloatingInterval(convert(smallestValue, conf), Double.PositiveInfinity, containsNaN)
        else FloatingInterval(convert(smallestValue, conf),convert(biggestNonSpecialValue, conf), containsNaN)
      else if from.high < 0 then
        val negInf = from.containsNum(-4503599627370496L)
        val onlyNan = from.low >= (-4503599627370495L) && from.high <= -1
        val containsNaN = from.high <= -1L && from.high >= -4503599627370495L
        val smallestValue = from.low
        val biggestNonSpecialValue = if from.high < -4503599627370496L then from.high else -4503599627370497L
        if onlyNan then FloatingInterval(Double.NaN, Double.NaN, true)
        else if negInf then FloatingInterval(Double.NegativeInfinity, convert(smallestValue, conf), containsNaN)
        else FloatingInterval(convert(smallestValue, conf),convert(biggestNonSpecialValue, conf), containsNaN)

      else
        val posRes = apply(NumericInterval(0, from.high), conf)
        val negRes = apply(NumericInterval(from.low, -1), conf)
        j.apply(posRes,negRes).get
    else
      FloatingInterval(if newL.isNaN then newH else newL, if newH.isNaN then newL else newH, newL.isNaN || newH.isNaN)




// byte size from float/double to bytes`???
given (using convert: ConvertFloatBytes[Float, Seq[Byte]], j: Join[NumericInterval[Byte]]): ConvertFloatBytes[FloatingInterval[Float],Seq[NumericInterval[Byte]]] with
  override def apply(from: FloatingInterval[Float], conf: BytesSize && SomeCC[ByteOrder]): Seq[NumericInterval[Byte]] =
    if from.isNaN then
      val NaNbytes = Seq(NumericInterval(127.toByte,127.toByte), NumericInterval(-128.toByte, -1.toByte),
        NumericInterval(-128.toByte, 127.toByte), NumericInterval(-128.toByte, 127.toByte))
      return if conf.c2.t == ByteOrder.BIG_ENDIAN then NaNbytes else NaNbytes.reverse
    val convertL = convert(from.l, conf.c1 && SomeCC(ByteOrder.BIG_ENDIAN, conf.c2.canFail))
    val convertH = convert(from.h, conf.c1 && SomeCC(ByteOrder.BIG_ENDIAN, conf.c2.canFail))
    val l_exp = math.getExponent(from.l)
    val h_exp = math.getExponent(from.h)
    val same_exp = l_exp == h_exp
    if (from.l >= 0 && floatToIntBits(from.l) != floatToIntBits(-0.0f)) then
      println("pos")
      if same_exp then
        val byte1 = NumericInterval(convertL(0),convertH(0))
        val byte2 = NumericInterval(convertL(1),convertH(1))
        val byte3 = if convertL(2) != convertH(2) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(2),convertH(2))
        val byte4 = if convertL(3) != convertH(3) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(3),convertH(3))
        val bytes = Seq(byte1,byte2,byte3,byte4)
        println(bytes)
        if conf.c2.t == ByteOrder.BIG_ENDIAN then bytes else bytes.reverse
      else
        // check for difference in exp
        val byte1 = NumericInterval(convertL(0),convertH(0))
        val byte2 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte3 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte4 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val bytes = Seq(byte1, byte2, byte3, byte4)
        println(bytes)
        if conf.c2.t == ByteOrder.BIG_ENDIAN then bytes else bytes.reverse

    else if from.h < 0 || floatToIntBits(from.h) == floatToIntBits(-0.0f) then
      println("neg")
      if same_exp then
        val byte1 = NumericInterval(convertH(0),convertL(0))
        val byte2 = NumericInterval(convertL(1),convertH(1))
        val byte3 = if convertL(2) != convertH(2) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(2),convertH(2))
        val byte4 = if convertL(3) != convertH(3) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(3),convertH(3))
        val bytes = Seq(byte1,byte2,byte3,byte4)
        println(bytes)
        if conf.c2.t == ByteOrder.BIG_ENDIAN then bytes else bytes.reverse
      else
        // check for difference in exp
        val byte1 = NumericInterval(convertH(0),convertL(0))
        val byte2 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte3 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte4 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val bytes = Seq(byte1, byte2, byte3, byte4)
        println(bytes)
        if conf.c2.t == ByteOrder.BIG_ENDIAN then bytes else bytes.reverse
    else
      println("diff")
      val pos = apply(FloatingInterval(0, from.h, false), conf)
      val neg = apply(FloatingInterval(from.l,-0, false), conf)
      println(pos.zip(neg).map((p,n)=> j(p,n).get))
      pos.zip(neg).map((p,n)=> j(p,n).get)

given (using convert: ConvertBytesFloat[Seq[Byte], Float]):ConvertBytesFloat[Seq[NumericInterval[Byte]], FloatingInterval[Float]] with
  override def apply(from: Seq[NumericInterval[Byte]], conf: SomeCC[ByteOrder]): FloatingInterval[Float] =
    val bytes = if conf.t == ByteOrder.BIG_ENDIAN then from else from.reverse
    // when bytes are in mantisse/exponent take front part that maximazes for min value take sign = 1 and highest poss exponent
    // howevere consider cases like
    // pos Infinty = byte1 contains 127 byte2 contains -128 byte 3 & byte 4 contains zero
    // nega Infinty = byte1 contains -1 byte2 contains -128 byte 3 & byte 4 contains zero
    // NaN = byte1 contains -1 or 127 byte2 contains -neg number byte 3 & byte 4 contains num not zero ( can be zero if byte 2 contains neg number !=
    // if it doesnt't contain neg Inf => min = if byte 1 neg takes biggest neg num byte take biggest number != -1 then take biggest neg number
    // else if sign only pos then take smallest values
    // for max everything on reverse = pos sign possible take biggest values if neg take smalles values
    val containsPosInf = bytes(0).containsNum(127.toByte) && bytes(1).containsNum(-128.toByte) && bytes(2).containsNum(0.toByte) && bytes(3).containsNum(0.toByte)
    val containsNegInf = bytes(0).containsNum(-1.toByte) && bytes(1).containsNum(-128.toByte) && bytes(2).containsNum(0.toByte) && bytes(3).containsNum(0.toByte)
    val containsNaN1 = (bytes(0).containsNum(127.toByte) || bytes(0).containsNum(-1.toByte)) && bytes(1).containsNum(-128.toByte) && bytes(2).low != 0 && bytes(2).high != 0// what about +N
    val containsNaN2 = (bytes(0).containsNum(127.toByte) || bytes(0).containsNum(-1.toByte)) && bytes(1).containsNum(-128.toByte) && bytes(3).low != 0 && bytes(3).high != 0
    val containsNaN3 = (bytes(0).containsNum(127.toByte) || bytes(0).containsNum(-1.toByte)) && bytes(1).low < 0 && bytes(1).high > -128

    val onlyNaNPossible = (bytes(0).containsNum(127.toByte) || bytes(0).containsNum(-1.toByte)) &&
      ((bytes(1).low < 0 && bytes(1).high < 0) || (bytes(1).low == 128 && bytes(1).high == 128))

    var maxValue = Float.PositiveInfinity
    var minValue = Float.NegativeInfinity
    // how to exclude Nan Values
    if !containsPosInf && !onlyNaNPossible then
      val maxByte1: Byte = if bytes(0).high >= 0 then bytes(0).high else bytes(0).low

      val Byte1Maxed = maxByte1 == 127 // if byte1 then take only positive numbers if possible


      val maxByte2 : Byte=
        if maxByte1 >= 0 then // maximize Exponent // allow -1 only if maxByte1 != 127
          if Byte1Maxed || bytes(1).high < 0 || bytes(1).low >= 0 then bytes(1).high
          else -1
        else // minimize Exponent
          if bytes(1).low >= 0 || bytes(1).high < 0 then bytes(1).low
          else 0

      val maxByte3: Byte =
        if maxByte1 >= 0 then
          if bytes(2).high < 0 || bytes(2).low >= 0 then bytes(2).high
          else -1
        else
          if bytes(2).low >= 0 || bytes(2).high < 0 then bytes(2).low
          else 0

      val maxByte4: Byte =
        if maxByte1 >= 0 then
          if bytes(3).high < 0 || bytes(3).low >= 0 then bytes(3).high else -1 // recheck on how to extract -1
        else
          if bytes(3).low >= 0 || bytes(3).high < 0 then bytes(3).low
          else 0.toByte

      maxValue = convert(Seq(maxByte1,maxByte2,maxByte3,maxByte4), SomeCC(ByteOrder.BIG_ENDIAN, conf.canFail))


    if !containsNegInf && !onlyNaNPossible then
      val minByte1: Byte = if bytes(0).high < 0 || bytes(0).low >= 0 then bytes(0).high else -1

      val Byte1Minned = minByte1 == -1
      //val maxNegInByte2: Byte = if Byte1NotMin then 0 else -128
      //val maxNegVInByte2: Byte = if Byte1NotMin then -1 else -128

      val minByte2: Byte =
        if minByte1 >= 0 then // minmize Exponent
          if bytes(1).low >= 0 || bytes(1).high < 0 then bytes(1).low
          else 0
        else // maximzae Exponent
          if Byte1Minned || bytes(1).high < 0 || bytes(1).low >= 0 then bytes(1).high
          else -1

      val minByte3: Byte =
        if minByte1 >= 0 then // minmize Exponent
          if bytes(2).low >= 0 || bytes(2).high < 0 then bytes(2).low
          else 0
        else // maximzae Exponent
          if bytes(2).high < 0 || bytes(2).low >= 0 then bytes(2).high
          else -1

      val minByte4: Byte =
        if minByte1 >= 0 then // minmize Exponent
          if bytes(3).low >= 0 || bytes(3).high < 0 then bytes(3).low
          else 0
        else // maximzae Exponent
          if bytes(3).high < 0 || bytes(3).low >= 0 then bytes(3).high
          else -1

      minValue = convert(Seq(minByte1, minByte2, minByte3, minByte4), SomeCC(ByteOrder.BIG_ENDIAN, conf.canFail))

    if onlyNaNPossible && !containsNegInf && !containsPosInf then
      FloatingInterval(Float.NaN, Float.NaN, true)
    else
      FloatingInterval(minValue, maxValue,containsNaN1 || containsNaN2 || containsNaN3)

given (using convert: ConvertDoubleBytes[Double, Seq[Byte]], j: Join[NumericInterval[Byte]]):ConvertDoubleBytes[FloatingInterval[Double], Seq[NumericInterval[Byte]]] with
  override def apply(from: FloatingInterval[Double], conf: BytesSize && SomeCC[ByteOrder]): Seq[NumericInterval[Byte]] =
    if from.isNaN then
      val topByte = NumericInterval(-128.toByte, 127.toByte)
      val NaNbytes = Seq(NumericInterval(127.toByte, 127.toByte), NumericInterval(-16.toByte, -1.toByte),
        topByte,topByte,topByte,topByte,topByte,topByte)
      return if conf.c2.t == ByteOrder.BIG_ENDIAN then NaNbytes else NaNbytes.reverse
    val convertL = convert(from.l, conf.c1 && SomeCC(ByteOrder.BIG_ENDIAN, conf.c2.canFail))
    val convertH = convert(from.h, conf.c1 && SomeCC(ByteOrder.BIG_ENDIAN, conf.c2.canFail))
    val l_exp = math.getExponent(from.l)
    val h_exp = math.getExponent(from.h)
    val same_exp = l_exp == h_exp
    if (from.l >= 0 && doubleToLongBits(from.l) != doubleToLongBits(-0.0f)) then
      if same_exp then
        val byte1 = NumericInterval(convertL(0), convertH(0))
        val byte2 = NumericInterval(convertL(1), convertH(1))
        val byte3 = if convertL(2) != convertH(2) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(2), convertH(2))
        val byte4 = if convertL(3) != convertH(3) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(3), convertH(3))
        val byte5 = if convertL(4) != convertH(4) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(4), convertH(4))
        val byte6 = if convertL(5) != convertH(5) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(5), convertH(5))
        val byte7 = if convertL(6) != convertH(6) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(6), convertH(6))
        val byte8 = if convertL(7) != convertH(7) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(7), convertH(7))
        val bytes = Seq(byte1, byte2, byte3, byte4,byte5,byte6,byte7,byte8)
        println(bytes)
        if conf.c2.t == ByteOrder.BIG_ENDIAN then bytes else bytes.reverse
      else
        // check for difference in exp
        val byte1 = NumericInterval(convertL(0), convertH(0))
        val byte2 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte3 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte4 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte5 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte6 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte7 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte8 = NumericInterval(Byte.MinValue, Byte.MaxValue)

        val bytes = Seq(byte1, byte2, byte3, byte4,byte5,byte6,byte7,byte8)
        println(bytes)
        if conf.c2.t == ByteOrder.BIG_ENDIAN then bytes else bytes.reverse

    else if from.h < 0 || doubleToLongBits(from.h) == doubleToLongBits(-0.0f) then
      if same_exp then
        val byte1 = NumericInterval(convertH(0), convertL(0))
        val byte2 = NumericInterval(convertL(1), convertH(1))
        val byte3 = if convertL(2) != convertH(2) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(2), convertH(2))
        val byte4 = if convertL(3) != convertH(3) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(3), convertH(3))
        val byte5 = if convertL(4) != convertH(4) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(4), convertH(4))
        val byte6 = if convertL(5) != convertH(5) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(5), convertH(5))
        val byte7 = if convertL(6) != convertH(6) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(6), convertH(6))
        val byte8 = if convertL(7) != convertH(7) then NumericInterval(Byte.MinValue, Byte.MaxValue) else NumericInterval(convertL(7), convertH(7))
        val bytes = Seq(byte1, byte2, byte3, byte4,byte5,byte6,byte7,byte8)
        println(bytes)
        if conf.c2.t == ByteOrder.BIG_ENDIAN then bytes else bytes.reverse
      else
        // check for difference in exp
        val byte1 = NumericInterval(convertH(0), convertL(0))
        val byte2 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte3 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte4 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte5 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte6 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte7 = NumericInterval(Byte.MinValue, Byte.MaxValue)
        val byte8 = NumericInterval(Byte.MinValue, Byte.MaxValue)

        val bytes = Seq(byte1, byte2, byte3, byte4,byte5,byte6,byte7,byte8)
        println(bytes)
        if conf.c2.t == ByteOrder.BIG_ENDIAN then bytes else bytes.reverse
    else
      println("diff")
      val pos = apply(FloatingInterval(0, from.h, false), conf)
      val neg = apply(FloatingInterval(from.l, -0, false), conf)
      println(pos.zip(neg).map((p, n) => j(p, n).get))
      pos.zip(neg).map((p, n) => j(p, n).get)
given (using convert: ConvertBytesDouble[Seq[Byte], Double]):ConvertBytesDouble[Seq[NumericInterval[Byte]], FloatingInterval[Double]] with
  override def apply(from: Seq[NumericInterval[Byte]], conf: SomeCC[ByteOrder]): FloatingInterval[Double] =
    val bytes = if conf.t == ByteOrder.BIG_ENDIAN then from else from.reverse


    val containsPosInf = bytes(0).containsNum(127.toByte) && bytes(1).containsNum(-16.toByte) && (2 to 7).forall(i => bytes(i).containsNum(0))
    val containsNegInf = bytes(0).containsNum(-1.toByte) && bytes(1).containsNum(-16.toByte) && (2 to 7).forall(i => bytes(i).containsNum(0))
    val containsNaN1 = (bytes(0).containsNum(127.toByte) || bytes(0).containsNum(-1.toByte)) && bytes(1).containsNum(-16.toByte) && (2 to 7).exists(i => bytes(i).low != 0 && bytes(i).high != 0)
    val containsNaN3 = (bytes(0).containsNum(127.toByte) || bytes(0).containsNum(-1.toByte)) && bytes(1).low < 0 && bytes(1).high > -16

    val onlyNaNPossible = (bytes(0).containsNum(127.toByte) || bytes(0).containsNum(-1.toByte)) &&
      ((bytes(1).low < 0 && bytes(1).high < 0) && (bytes(1).low >= -16 && bytes(1).high <  0))

    var maxValue = Double.PositiveInfinity
    var minValue = Double.NegativeInfinity
    // how to exclude Nan Values
    if !containsPosInf && !onlyNaNPossible then

      def extractSuitableMaxByte(Byte1: Byte, intervall: NumericInterval[Byte]): Byte =
        if Byte1 >= 0 then
          if intervall.high < 0 || intervall.low >= 0 then intervall.high
          else -1
        else if intervall.low >= 0 || intervall.high < 0 then intervall.low
        else 0

      val maxByte1: Byte = if bytes(0).high >= 0 then bytes(0).high else bytes(0).low
      val Byte1Maxed = maxByte1 == 127 // if byte1 then take only positive numbers if possible

      val maxByte2: Byte =
        if maxByte1 >= 0 then
          if Byte1Maxed || (bytes(1).high < -16 || bytes(1).low >= 0) then bytes(1).high
          else -1
        else if bytes(1).low >= 0 || bytes(1).high < 0 then bytes(1).low
        else 0



      val maxByte3: Byte = extractSuitableMaxByte(maxByte1, bytes(2))
      val maxByte4: Byte = extractSuitableMaxByte(maxByte1, bytes(3))
      val maxByte5: Byte = extractSuitableMaxByte(maxByte1, bytes(4))
      val maxByte6: Byte = extractSuitableMaxByte(maxByte1, bytes(5))
      val maxByte7: Byte = extractSuitableMaxByte(maxByte1, bytes(6))
      val maxByte8: Byte = extractSuitableMaxByte(maxByte1, bytes(7))

      maxValue = convert(Seq(maxByte1,maxByte2,maxByte3,maxByte4,maxByte5,maxByte6,maxByte7,maxByte8), SomeCC(ByteOrder.BIG_ENDIAN, conf.canFail))


    if !containsNegInf && !onlyNaNPossible then
      def extractSuitableMinByte(Byte1: Byte, intervall: NumericInterval[Byte]): Byte =
        if Byte1 < 0 then
          if intervall.high < 0 || intervall.low >= 0 then intervall.high
          else -1
        else if intervall.low >= 0 || intervall.high < 0 then intervall.low
        else 0

      val minByte1: Byte = if bytes(0).high < 0 || bytes(0).low >= 0 then bytes(0).high else -1
      val Byte1Minned = minByte1 == -1 // if byte1 then take only positive numbers if possible

      val minByte2: Byte =
        if minByte1 < 0 then
          // casce where Byte1Minned = false and bytes(1).high in [-16,0]
          if Byte1Minned || (bytes(1).high < -16 || bytes(1).low >= 0) then bytes(1).high
          else -1
        else if bytes(1).low >= 0 || bytes(1).high < 0 then bytes(1).low
        else 0


      val minByte3: Byte = extractSuitableMinByte(minByte1, bytes(2))
      val minByte4: Byte = extractSuitableMinByte(minByte1, bytes(3))
      val minByte5: Byte = extractSuitableMinByte(minByte1, bytes(4))
      val minByte6: Byte = extractSuitableMinByte(minByte1, bytes(5))
      val minByte7: Byte = extractSuitableMinByte(minByte1, bytes(6))
      val minByte8: Byte = extractSuitableMinByte(minByte1, bytes(7))

      minValue = convert(Seq(minByte1, minByte2, minByte3, minByte4, minByte5, minByte6, minByte7, minByte8), SomeCC(ByteOrder.BIG_ENDIAN, conf.canFail))

    if onlyNaNPossible && !containsNegInf && !containsPosInf then
      FloatingInterval(Double.NaN, Double.NaN, true)
    else
      FloatingInterval(minValue, maxValue,containsNaN1 || containsNaN3)


class FloatingIntervalWiden[F](bounds: => Set[F], minValue: F, maxValue: F)(using num: Numeric[F]) extends Widen[FloatingInterval[F]]:
  private lazy val treeSet: TreeSet[F] = TreeSet.from(bounds)

  override def apply(v1: FloatingInterval[F], v2: FloatingInterval[F]): MaybeChanged[FloatingInterval[F]] =

    val low =
      if (v1.l <= v2.l) then v1.l
      else
        val intBits =
          if num.fromInt(0).isInstanceOf[Float] then
            intBitsToFloat(floatToIntBits(v2.l.asInstanceOf[Float]) + 1).asInstanceOf[F]
          else
            //TODO what do if intBits is negative substract Bit / what about border values as 0 or MinValue MaxValue / Infinity
            longBitsToDouble(doubleToLongBits(v2.l.asInstanceOf[Double]) + 1).asInstanceOf[F]
        treeSet.maxBefore(intBits).getOrElse(minValue)
    val high =
      if (v1.h >= v2.h) then v1.h
      else treeSet.minAfter(v2.h).getOrElse(maxValue)
    MaybeChanged(FloatingInterval(low, high, v1.NaN || v2.NaN), v1)


