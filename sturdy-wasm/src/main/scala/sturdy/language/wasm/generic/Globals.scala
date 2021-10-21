package sturdy.language.wasm.generic

import sturdy.data.unit
import sturdy.{IsSound, Soundness}
import sturdy.effect.{ComputationJoiner, DelegatingComputationJoinerWithSuper, Effectful}
import sturdy.effect.failure.Failure
import sturdy.effect.symboltable.DecidableSymbolTable

trait Globals[V] extends Effectful, Failure:
  def makeGlobalsTable: DecidableSymbolTable[Unit, GlobalAddr, V]
  val globalsTable = makeGlobalsTable
  globalsTable.addEmptyTable(())
  def readGlobal(ga: GlobalAddr): V =
    globalsTable.tableGet((), ga).getOrElse(fail(UnboundGlobal, ga.toString))
  def writeGlobal(ga: GlobalAddr, v: V): Unit =
    globalsTable.tableSet((), ga, v)
  override def makeComputationJoiner[A]: ComputationJoiner[A] =
    new DelegatingComputationJoinerWithSuper(globalsTable, super.makeComputationJoiner)

  def getGlobalValues: Globals.Values[V] = globalsTable.getTables(())
  def setGlobalValues(vs: Globals.Values[V]) = globalsTable.setTables(Map(() -> vs))

  def globalsIsSound[cV](c: Globals[cV])(using Soundness[cV, V]): IsSound =
    val cvals = c.getGlobalValues
    val avals = this.getGlobalValues
    if (cvals.size != avals.size)
      return IsSound.NotSound(s"Global addresses differ, expected ${cvals.keys} but found ${avals.keys}")
    for ((addr, cv) <- cvals)
      avals.get(addr) match
        case None => return IsSound.NotSound(s"Missing address $addr in abstract globals")
        case Some(av) =>
          val valSound = Soundness.isSound(cv, av)
          if (valSound.isNotSound)
            return valSound
    IsSound.Sound

object Globals:
  type Values[V] = Map[GlobalAddr, V]
