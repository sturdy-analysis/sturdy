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
  def stringLit(s: String): V
  def concat(s1: V, s2: V) : V
  def substring(s: V, begin: I, end: I) : V
  def contains(s: V, w : V): B
  def length(s: V): I
  def isEmpty(s: V): B

  def charAt(s: V, i: I): V
  def equals(s1: V, s2: V): B
  def compareTo(s1: V, s2: V): I
  def startsWith(s: V, prefix: V, offset: I): B
  def endsWith(s: V, suffix: V): B
  def indexOf(s: V, word: V, fromIndex: I): I
  def replace(s: V, word: V, newWord: V): V
  //def split(s: V, splitChar: V): R
  def toLowerCase(s: V): V
  def toUpperCase(s: V): V
  def trim(s: V) :V

  /*
  matches
  format
  */

//def randomString(): V


