package sturdy.effect.store

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

import scala.collection.mutable.ListBuffer

/*
 * An abstract threadded store. The store tracks dirty addresses that have been (re)written to
 * optimize the join computation, since only values of dirty addresses need joining.
 */
trait AStoreGenericThreadded[Addr, V](using JoinValue[V])
  extends JoinComputation:

  protected var store: Map[Addr, V] = Map()
  protected var dirtyAddrs: Set[Addr] = Set()

  def getStore: Map[Addr, V] = store

  protected def weakUpdate(x: Addr, v: V)(using JoinValue[V]): Unit =
    store.get(x) match
      case None => store += x -> v
      case Some(old) => store += x -> joinValues(old, v)
  
  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    val snapshot = store
    var joinedDirtyAddrs = dirtyAddrs
    dirtyAddrs = Set()

    var fStore: Map[Addr, V] = null
    var fDirtyAddrs: Set[Addr] = null

    val joinedResult = super.joinComputations(f) {
      fStore = store
      fDirtyAddrs = dirtyAddrs

      store = snapshot
      dirtyAddrs = Set()
      g
    }

    for (x <- fDirtyAddrs)
      store.get(x) match
        case None => store += x -> fStore(x)
        case Some(gVal) => store += x -> joinValues(fStore(x), gVal)
    dirtyAddrs ++= fDirtyAddrs

    joinedResult


