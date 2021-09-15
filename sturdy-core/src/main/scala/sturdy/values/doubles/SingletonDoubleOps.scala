package sturdy.values.doubles

import sturdy.effect.failure.Failure
import sturdy.values.Singleton

given SingletonDoubleOps: DoubleOps[Singleton[Double]] with
  val ops = ConcreteDoubleOps

  def doubleLit(d: Double): Singleton[Double] = Singleton.Single(d)
  def randomDouble(): Singleton[Double] = Singleton.NoSingleton

  def add(v1: Singleton[Double], v2: Singleton[Double]): Singleton[Double] = v1.binary(ops.add, v2)
  def sub(v1: Singleton[Double], v2: Singleton[Double]): Singleton[Double] = v1.binary(ops.sub, v2)
  def mul(v1: Singleton[Double], v2: Singleton[Double]): Singleton[Double] = v1.binary(ops.mul, v2)
  def div(v1: Singleton[Double], v2: Singleton[Double]): Singleton[Double] = v1.binary(ops.div, v2)
  def min(v1: Singleton[Double], v2: Singleton[Double]): Singleton[Double] = v1.binary(ops.min, v2)
  def max(v1: Singleton[Double], v2: Singleton[Double]): Singleton[Double] = v1.binary(ops.max, v2)

  def absolute(v: Singleton[Double]): Singleton[Double] = v.unary(ops.absolute)
  def negated(v: Singleton[Double]): Singleton[Double] = v.unary(ops.negated)
  def sqrt(v: Singleton[Double]): Singleton[Double] = v.unary(ops.sqrt)
  def ceil(v: Singleton[Double]): Singleton[Double] = v.unary(ops.ceil)
  def floor(v: Singleton[Double]): Singleton[Double] = v.unary(ops.floor)
  def truncate(v: Singleton[Double]): Singleton[Double] = v.unary(ops.truncate)
  def nearest(v: Singleton[Double]): Singleton[Double] = v.unary(ops.nearest)
  def copysign(v: Singleton[Double], sign: Singleton[Double]): Singleton[Double] = v.binary(ops.copysign, sign)

  override def logNatural(v: Singleton[Double]): Singleton[Double] = v.unary(ops.logNatural)
