package sturdy.effect.bytememory

import sturdy.IsSound
import sturdy.Soundness
import sturdy.data.{*, given}
import sturdy.effect.{ComputationJoiner, EffectStack, Effect}
import sturdy.values.*
import scala.reflect.ClassTag
import sturdy.values.integer.NumericInterval
import sturdy.values.integer.{*, given}
import sturdy.effect.failure.Failure
/** A memory that tracks byte properties `B` for memory accesses via address ranges `NumericInterval[Int]`.
 */
class IntervalAddressMemory[Key, B: ClassTag](emptyB: B, rangeLimit: Int)(using tb: Top[B])(using Join[B], Widen[B], Finite[Key]) extends Memory[Key, NumericInterval[Int], Seq[B], Topped[Int], WithJoin], Effect:
  private val constantAddressMemory: ConstantAddressMemory[Key, B] = new ConstantAddressMemory(emptyB)

  override def read(key: Key, addr: NumericInterval[Int], length: Int, alignment: Int): JOptionA[Seq[B]] =
    if (addr.countOfNumsInInterval <= rangeLimit)
      EffectStack.pureJoinFold(addr.low to addr.high, addr => constantAddressMemory.read(key, Topped.Actual(addr), length))
    else
      constantAddressMemory.read(key, Topped.Top, length)

  override def write(key: Key, addr: NumericInterval[Int], bytes: Seq[B], alignment: Int): JOptionA[Unit] =
    if (addr.countOfNumsInInterval <= rangeLimit)
      EffectStack.localEffect(constantAddressMemory)
        .joinFold(addr.low to addr.high, addr => constantAddressMemory.write(key, Topped.Actual(addr), bytes))
    else
      constantAddressMemory.write(key, Topped.Top, bytes)

  override def size(key: Key): Topped[Int] =
    constantAddressMemory.size(key)

  override def grow(key: Key, delta: Topped[Int]): JOption[WithJoin, Topped[Int]] =
    constantAddressMemory.grow(key, delta)

  override def fill(key: Key, addr: NumericInterval[Int], size: Topped[Int], value: Seq[B]): JOption[WithJoin, Unit] = ???

  override def copy(key: Key, srcAddr: NumericInterval[Int], dstAddr: NumericInterval[Int], size: Topped[Int]): JOption[WithJoin, Unit] = ???

  override def init(key: Key, tableAddr: NumericInterval[Int], dataAddr: NumericInterval[Int], size: Topped[Int], dataBytes: Seq[B]): JOption[WithJoin, Unit] = ???

  override def putNew(key: Key, initSize: Topped[Int], sizeLimit: Option[Topped[Int]]): Unit =
    constantAddressMemory.putNew(key, initSize, sizeLimit)

  override type State = constantAddressMemory.State
  override def getState: State = constantAddressMemory.getState
  override def setState(s: State): Unit = constantAddressMemory.setState(s)
  override def join: Join[constantAddressMemory.State] = constantAddressMemory.join
  override def widen: Widen[constantAddressMemory.State] = constantAddressMemory.widen

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = constantAddressMemory.makeComputationJoiner

  def memoryIsSound(c: ConcreteMemory[Key])(using Soundness[Byte, B]): IsSound =
    constantAddressMemory.memoryIsSound(c)