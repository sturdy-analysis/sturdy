package sturdy.gradual

import sturdy.gradual.fix.GradualLogger
import sturdy.values.PartialOrder

trait GradualOps[T](using po: PartialOrder[T]):
  def insertCheck(uv: T, sv: T): T

  def withCheck(sv: T)(uv: => T): T =
    insertCheck(uv, sv)

class GradualLoggerOps[T, -FixDom, -FixCodom](using gl : GradualLogger[T, FixDom, FixCodom], po: PartialOrder[T]) extends GradualOps[T]:
  override def insertCheck(uv: T, sv: T): T =
    if(po.lt(uv, sv))
      gl.insertCheck(uv, sv)
    else if(!po.lteq(sv, uv))
      throw new Exception(s"unsafe value $uv is not related to safe value $sv")
    uv