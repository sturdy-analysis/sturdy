package sturdy.effect.except

import sturdy.effect.PathSensitiveEffect
import sturdy.values.{PathSensitive, assertPath}

trait PathSensitiveExcept[Exc, E : PathSensitive] extends JoinedExcept[Exc, E], PathSensitiveEffect:
  override def assert(cond: Any): Unit = 
    exception = exception.map(_.assertPath(cond))
