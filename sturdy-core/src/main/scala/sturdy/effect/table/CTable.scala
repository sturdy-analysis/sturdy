package sturdy.effect.table

import sturdy.effect.{CMayCompute, MayCompute, NoJoin}

import scala.collection.mutable.ArrayBuffer

object CTable:
  case class Tab[Elem](min: Int, max: Option[Int], table: ArrayBuffer[Elem])

import CTable.*

trait CTable[Elem] extends Table[Int,Elem]:
  override type TableJoin[A] = NoJoin[A]
  override type TableJoinComp = Unit

  protected val tables: ArrayBuffer[Tab[Elem]]

  override def tableGet(tabIdx: Int, idx: Int): CMayCompute[Elem] =
    val Tab(_,_,table) = tables(tabIdx)
    if (idx < table.length)
      CMayCompute.Computes(table(idx))
    else
      CMayCompute.ComputesNot()

  override def tableSet(tabIdx: Int, idx: Int, elem: Elem): CMayCompute[Unit] =
    val Tab(_,_,table) = tables(tabIdx)
    if (idx < table.length)
      table(idx) = elem
      CMayCompute.Computes(())
    else
      CMayCompute.ComputesNot()

  override def addEmptyTable(min: Int, max: Option[Int], default: Elem): Int =
    val newTab = ArrayBuffer.fill(min)(default)
    tables += Tab(min,max,newTab)
    tables.length
