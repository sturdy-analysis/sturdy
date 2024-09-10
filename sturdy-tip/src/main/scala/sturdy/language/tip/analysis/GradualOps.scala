package sturdy.language.tip.analysis

import sturdy.language.tip.abstractions.GradualLogger
import sturdy.values.PartialOrder

trait GradualOps[T, V]:
  def logger: GradualLogger[T,V]
  def insertCheck(uv: T, sv: T)(using po: PartialOrder[T]): T =
    println(s"uv=${uv} | sv=${sv}")
    if(po.lt(uv, sv))
      println(s"insert a check to test if value is more precise than ${uv}")
      logger.insertCheck(uv, sv)
    else if(!po.lteq(sv, uv))
      throw new Exception(s"unsafe value ${uv} is not related to safe value ${sv}")
    uv

  def withCheck(sv: T)(uv: => T)(using po: PartialOrder[T]) =
    insertCheck(uv,sv)
