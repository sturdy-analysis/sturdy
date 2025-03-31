package sturdy.effect.operandstack

import sturdy.effect.PathSensitiveEffect
import sturdy.values.{PathSensitive, assertPath}

trait PathSensitiveOperandStack[V : PathSensitive] extends DecidableOperandStack[V], PathSensitiveEffect:
  override def assert(cond: Any): Unit =
    val (affected, unchanged) = stack.splitAt(stack.size - framePointer)
    stack = affected.map(_.assertPath(cond)) ++ unchanged

