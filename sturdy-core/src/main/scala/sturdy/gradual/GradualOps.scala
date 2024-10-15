package sturdy.gradual

import sturdy.gradual.fix.GradualLogger
import sturdy.values.PartialOrder

trait GradualOps[T]:
  def insertCheck(uv: T, sv: T)(using po: PartialOrder[T]): T

  def withCheck(sv: T)(uv: => T)(using po: PartialOrder[T]): T =
    insertCheck(uv, sv)

class GradualLoggerOps[T, -FixDom, -FixCodom](gl : GradualLogger[T, FixDom, FixCodom]) extends GradualOps[T]:
  override def insertCheck(uv: T, sv: T)(using po: PartialOrder[T]): T =
    println(s"uv=${uv} | sv=${sv}")
    if(po.lt(uv, sv))
      println(s"insert a check to test if value is more precise than ${uv}")
      gl.insertCheck(uv, sv)
    else if(!po.lteq(sv, uv))
      throw new Exception(s"unsafe value ${uv} is not related to safe value ${sv}")
    uv

