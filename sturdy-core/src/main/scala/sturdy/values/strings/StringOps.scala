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
trait StringOps[B, V]:
  def stringLit(i: B): V
//def randomString(): V


