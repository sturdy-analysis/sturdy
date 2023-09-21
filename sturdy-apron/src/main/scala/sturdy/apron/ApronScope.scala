package sturdy.apron

import apron.{Environment, Interval}

trait ApronScope:
  def apronEnv: Environment
  def isBound(v: ApronVar): Boolean = apronEnv.hasVar(v.av)
  def getBound(v: ApronVar): Interval
  def getBound(v: ApronExpr): Interval
  def getFreedReference(v: ApronVar): Option[ApronExpr]
  def isFreed(v: ApronVar): Boolean = getFreedReference(v).nonEmpty