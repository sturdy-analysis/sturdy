package sturdy.values.floats

import sturdy.values.config
import sturdy.values.convert.*

import java.nio.ByteOrder

trait FloatOps[V]:
  def floatLit(f: Float): V
  def randomFloat(): V

  def add(v1: V, v2: V): V
  def sub(v1: V, v2: V): V
  def mul(v1: V, v2: V): V
  def div(v1: V, v2: V): V
  def min(v1: V, v2: V): V 
  def max(v1: V, v2: V): V 

  def absolute(v: V): V 
  def negated(v: V): V 
  def sqrt(v: V): V 
  def ceil(v: V): V 
  def floor(v: V): V 
  def truncate(v: V): V 
  def nearest(v: V): V 
  def copysign(v: V, sign: V): V

type ConvertFloatInt[VFrom, VTo] = Convert[Float, Int, VFrom, VTo, config.Overflow && config.Bits]
type ConvertFloatLong[VFrom, VTo] = Convert[Long, Int, VFrom, VTo, config.Overflow && config.Bits]
type ConvertFloatDouble[VFrom, VTo] = Convert[Float, Double, VFrom, VTo, NilCC.type]
type ConvertFloatBytes[VFrom, VTo] = Convert[Float, Seq[Byte], VFrom, VTo, SomeCC[ByteOrder]]
type ConvertBytesFloat[VFrom, VTo] = Convert[Seq[Byte], Float, VFrom, VTo, SomeCC[ByteOrder]]
