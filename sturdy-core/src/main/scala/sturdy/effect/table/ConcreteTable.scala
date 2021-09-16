package sturdy.effect.table

import sturdy.effect.MayCompute
import sturdy.effect.MayComputeConcrete
import sturdy.effect.NoJoin

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

object ConcreteTable:
  case class Tab[Elem](min: Int, max: Option[Int], entries: Array[Elem]):
    inline def size: Int = entries.length

trait ConcreteTable[V, Elem: ClassTag](vInt: V => Int) extends Table[V,Elem]:
  import ConcreteTable.*

  override type TableJoin[A] = NoJoin[A]

  protected val tables: ArrayBuffer[Tab[Elem]] = ArrayBuffer.empty

  override def tableGet(tabIdx: Int, vidx: V): MayComputeConcrete[Elem] =
    val idx = vInt(vidx)
    val table = tables(tabIdx)
    if (idx < table.size)
      MayComputeConcrete.Computes(table.entries(idx))
    else
      MayComputeConcrete.ComputesNot()

  override def tableSet(tabIdx: Int, vidx: V, elem: Elem): MayComputeConcrete[Unit] =
    val idx = vInt(vidx)
    val table = tables(tabIdx)
    if (idx < table.size)
      table.entries(idx) = elem
      MayComputeConcrete.Computes(())
    else
      MayComputeConcrete.ComputesNot()

  override def addEmptyTable(min: Int, max: Option[Int]): Int =
    val tix = tables.length
    val entries = Array.ofDim[Elem](min)
    tables += Tab(min,max,entries)
    tix
