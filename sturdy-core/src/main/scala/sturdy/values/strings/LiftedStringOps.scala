package sturdy.values.strings

import sturdy.data.{JOption, MayJoin}
import sturdy.effect.failure.Failure


class LiftedStringOps[S, I, B, US, UI, UB](extractS: S => US, extractI: I => UI, injectS: US => S, injectI: UI => I, injectB: UB => B)(using ops: StringOps[US, UI, UB]) extends StringOps[S, I, B]:
  def stringLit(s: String): S = injectS(ops.stringLit(s))
  override def concat(s1: S, s2: S): S = injectS(ops.concat(extractS(s1), extractS(s2)))
  override def substring(s: S, begin: I, end: I): S = injectS(ops.substring(extractS(s), extractI(begin), extractI(end)))
  override def contains(s: S, w: S): B = injectB(ops.contains(extractS(s), extractS(w)))



