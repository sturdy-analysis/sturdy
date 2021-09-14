package sturdy.effect.table

import sturdy.effect.{MayCompute, NoJoin, CMayCompute}

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

object CTable:
  case class Tab[Elem](min: Int, max: Option[Int], entries: Array[Elem]):
    inline def size: Int = entries.length

import CTable.*

trait CTable[V, Elem: ClassTag](vInt: V => Int) extends Table[V,Elem]:
  override type TableJoin[A] = NoJoin[A]
  override type TableJoinComp = Unit

  protected val tables: ArrayBuffer[Tab[Elem]] = ArrayBuffer.empty

  override def tableGet(tabIdx: Int, vidx: V): CMayCompute[Elem] =
    val idx = vInt(vidx)
    val table = tables(tabIdx)
    if (idx < table.size)
      CMayCompute.Computes(table.entries(idx))
    else
      CMayCompute.ComputesNot()

  override def tableSet(tabIdx: Int, vidx: V, elem: Elem): CMayCompute[Unit] =
    val idx = vInt(vidx)
    val table = tables(tabIdx)
    if (idx < table.size)
      table.entries(idx) = elem
      CMayCompute.Computes(())
    else
      CMayCompute.ComputesNot()

  override def addEmptyTable(min: Int, max: Option[Int]): Int =
    val tix = tables.length
    val entries = Array.ofDim[Elem](min)
    tables += Tab(min,max,entries)
    tix
