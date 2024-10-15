package sturdy.language.tip

import sturdy.gradual.GradualOps
import sturdy.language.tip.abstractions.TipGradualLogger
import sturdy.values.PartialOrder
import sturdy.values.integer.{IntegerOps, LiftedIntegerOps}

class TipGradualOps[T, V](using logger: TipGradualLogger[T, V]) extends GradualOps[T]:
  override def insertCheck(uv: T, sv: T)(using po: PartialOrder[T]): T =
    println(s"uv=${uv} | sv=${sv}")
    if(po.lt(uv, sv))
      println(s"insert a check to test if value is more precise than ${uv}")
      logger.insertCheck(uv, sv)
    else if(!po.lteq(sv, uv))
      throw new Exception(s"unsafe value ${uv} is not related to safe value ${sv}")
    uv

  override def withCheck(sv: T)(uv: => T)(using po: PartialOrder[T]) =
    insertCheck(uv,sv)
