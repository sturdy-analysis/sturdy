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


/** Integer operations for base type B, represented as V */
trait StringOps[V]:
  def stringLit(i: String): V
  def concat(s1: V, s2: V) : V
//def randomString(): V


