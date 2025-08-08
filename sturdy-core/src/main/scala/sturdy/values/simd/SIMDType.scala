package sturdy.values.simd

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.config.{Bits, BytesSize, Padding}
import sturdy.values.convert.{&&, SomeCC}
import sturdy.values.types.BaseType

import java.nio.ByteOrder
import scala.reflect.ClassTag


/*given TypeSIMDOps[B: ClassTag, V](using f: Failure, j: EffectStack): SIMDOps[B, BaseType[B], V, Byte] with
  ...
*/

given TypedConvertBytesVector: ConvertBytesVec[BaseType[Seq[Byte]], BaseType[Array[Byte]]] with
  def apply(from: BaseType[Seq[Byte]], conf: BytesSize && Padding && Bits && SomeCC[ByteOrder]): BaseType[Array[Byte]] = ???


  

given TypedConvertVectorBytes: ConvertVecBytes[BaseType[Array[Byte]], BaseType[Seq[Byte]]] with
  def apply(from: BaseType[Array[Byte]], conf: BytesSize && SomeCC[ByteOrder]): BaseType[Seq[Byte]] = ???