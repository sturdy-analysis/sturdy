package sturdy.values.types

trait TypeOps[V]:
  def isNumber(v: V): V
  def isInteger(v: V): V
  def isDouble(v: V): V
  def isRational(v: V): V
  def isNull(v: V): V
  def isCons(v: V): V
  def isBoolean(v: V): V