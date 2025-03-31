package sturdy.effect.callframe

import sturdy.effect.PathSensitiveEffect
import sturdy.values.{PathSensitive, assertPath}

import scala.reflect.ClassTag

trait PathSensitiveCallFrame[Data, Var, V : PathSensitive, Site] extends DecidableMutableCallFrame[Data, Var, V, Site], PathSensitiveEffect:
  override def assert(cond: Any): Unit =
    for (i <- vars.indices)
      vars(i) = vars(i).assertPath(cond)
