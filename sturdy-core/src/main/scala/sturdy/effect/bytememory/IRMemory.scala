package sturdy.effect.bytememory

import sturdy.data.{*, given}
import sturdy.data.{JOption, JOptionA, MayJoin}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.Effect
import sturdy.ir.{IR, IROperator}
import sturdy.values.{Finite, Join, Widen}

enum IRMemoryOperator extends IROperator:
  case MemNew
  case MemNewLimit
  case MemRead(length: Int)
  case MemWrite
  case MemSize
  case MemGrow

class IRMemory[Key](using Join[IR], Widen[IR], Finite[Key]) extends Memory[Key, IR, IR, IR, WithJoin]:
  private var memories: Map[Key, IR] = Map.empty
  override def read(key: Key, addr: IR, length: Int): JOptionA[IR] =
    JOptionA.noneSome(IR.Op(IRMemoryOperator.MemRead(length), memories(key), addr))

  override def write(key: Key, addr: IR, bytes: IR): JOptionA[Unit] =
    memories += key -> IR.Op(IRMemoryOperator.MemWrite, memories(key), addr, bytes)
    JOptionA.noneSome(())

  override def size(key: Key): IR =
    IR.Op(IRMemoryOperator.MemSize, memories(key))

  override def grow(key: Key, delta: IR): JOptionA[IR] =
    val grownMem = IR.Op(IRMemoryOperator.MemGrow, memories(key), delta)
    memories += key -> grownMem
    JOptionA.noneSome(IR.Op(IRMemoryOperator.MemSize, grownMem))

  override def putNew(key: Key, initSize: IR, sizeLimit: Option[IR]): Unit =
    val newMem = sizeLimit match
      case None => IR.Op(IRMemoryOperator.MemNew, initSize)
      case Some(limit) => IR.Op(IRMemoryOperator.MemNewLimit, initSize, limit)
    memories += key -> newMem

  /** The internal state of the effect. */
  override type State = Map[Key, IR]

  override def getState: Map[Key, IR] = memories
  override def setState(st: Map[Key, IR]): Unit = memories = st
  override def join: Join[Map[Key, IR]] = implicitly
  override def widen: Widen[Map[Key, IR]] = implicitly



