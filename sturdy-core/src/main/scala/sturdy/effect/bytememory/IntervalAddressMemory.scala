package sturdy.effect.bytememory

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, EffectStack, Effectful}
import sturdy.values.*

import scala.reflect.ClassTag
import sturdy.values.integer.NumericInterval

/** A memory that tracks byte properties `B` for memory accesses via address ranges `NumericInterval[Int]`.
 */
class IntervalAddressMemory[Key, B: ClassTag](emptyB: B, rangeLimit: Int)(using tb: Top[B], jb: Join[B]) extends Memory[Key, NumericInterval[Int], Seq[B], Topped[Int], WithJoin], Effectful:
  private val constantAddressMemory: ConstantAddressMemory[Key, B] = new ConstantAddressMemory(emptyB)

  override def read(key: Key, addr: NumericInterval[Int], length: Int): JOptionA[Seq[B]] = addr match
    case NumericInterval.Bounded(low, high) if high - low <= rangeLimit =>
      EffectStack.pureJoinFold(low to high, addr => constantAddressMemory.read(key, Topped.Actual(addr), length))
    case _ =>
      constantAddressMemory.read(key, Topped.Top, length)

  override def write(key: Key, addr: NumericInterval[Int], bytes: Seq[B]): JOptionA[Unit] = addr match
    case NumericInterval.Bounded(low, high) if high - low <= rangeLimit =>
      EffectStack.localEffect(constantAddressMemory)
        .joinFold(low to high, addr => constantAddressMemory.write(key, Topped.Actual(addr), bytes))
    case _ =>
      constantAddressMemory.write(key, Topped.Top, bytes)

  override def size(key: Key): Topped[Int] =
    constantAddressMemory.size(key)

  override def grow(key: Key, delta: Topped[Int]): JOption[WithJoin, Topped[Int]] =
    constantAddressMemory.grow(key, delta)

  override def putNew(key: Key, initSize: Topped[Int], sizeLimit: Option[Topped[Int]]): Unit =
    constantAddressMemory.putNew(key, initSize, sizeLimit)

  override type State = constantAddressMemory.State
  override def getState: State = constantAddressMemory.getState
  override def setState(s: State): Unit = constantAddressMemory.setState(s)

  override def getComputationJoiner[A]: Option[ComputationJoiner[A]] = constantAddressMemory.getComputationJoiner

  def memoryIsSound(c: ConcreteMemory[Key])(using Soundness[Byte, B]): IsSound =
    constantAddressMemory.memoryIsSound(c)