package sturdy.values.strings

import sturdy.data.{JOption, MayJoin}
import sturdy.effect.failure.Failure


class LiftedStringOps[S, I, B, US, UI, UB](extractS: S => US, extractI: I => UI, injectS: US => S, injectI: UI => I, injectB: UB => B)(using ops: StringOps[US, UI, UB]) extends StringOps[S, I, B]:
  def stringLit(s: String): S = injectS(ops.stringLit(s))
  override def concat(s1: S, s2: S): S = injectS(ops.concat(extractS(s1), extractS(s2)))
  override def substring(s: S, begin: I, end: I): S = injectS(ops.substring(extractS(s), extractI(begin), extractI(end)))
  override def contains(s: S, w: S): B = injectB(ops.contains(extractS(s), extractS(w)))
  override def length(s: S): I = injectI(ops.length(extractS(s)))
  override def isEmpty(s: S): B = injectB(ops.isEmpty(extractS(s)))

  override def charAt(s: S, i: I): S = injectS(ops.charAt(extractS(s), extractI(i)))

  override def equals(s1: S, s2: S): B = injectB(ops.equals(extractS(s1), extractS(s2)))

  override def compareTo(s1: S, s2: S): I = injectI(ops.compareTo(extractS(s1), extractS(s2)))

  override def startsWith(s: S, prefix: S, offset: I): B = injectB(ops.startsWith(extractS(s), extractS(prefix), extractI(offset)))

  override def endsWith(s: S, suffix: S): B = injectB(ops.endsWith(extractS(s), extractS(suffix)))

  override def indexOf(s: S, word: S, fromIndex: I): I = injectI(ops.indexOf(extractS(s), extractS(word), extractI(fromIndex)))

  override def replace(s: S, word: S, newWord: S): S = injectS(ops.replace(extractS(s), extractS(word), extractS(newWord)))
  
  override def toLowerCase(s: S): S = injectS(ops.toLowerCase(extractS(s)))

  override def toUpperCase(s: S): S =  injectS(ops.toUpperCase(extractS(s)))

  override def trim(s: S): S =  injectS(ops.trim(extractS(s)))

  override def toInt(s: S): I = injectI(ops.toInt(extractS(s)))


