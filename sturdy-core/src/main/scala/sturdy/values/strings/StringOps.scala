package sturdy.values.strings

import sturdy.data.JOption
import sturdy.data.MayJoin
import sturdy.effect.failure.FailureKind
import sturdy.values.config
import sturdy.values.convert.&&
import sturdy.values.convert.Convert
import sturdy.values.convert.NilCC
import sturdy.values.convert.SomeCC

import java.nio.ByteBuffer
import java.nio.ByteOrder


case object StringIndexOutOfBounds extends FailureKind
case object StringNegativeIndex extends FailureKind

/** String operations represented as V */
trait StringOps[V, I, B]:
  def stringLit(i: String): V
  def concat(s1: V, s2: V) : V
  def substring(s: V, begin: I, end: I) : V
  def contains(s: V, w : V) : B
//def randomString(): V


