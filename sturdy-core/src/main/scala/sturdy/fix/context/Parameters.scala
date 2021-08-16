package sturdy.fix.context

import sturdy.effect.environment.Environment
import sturdy.effect.store.Store



def parametersFromEnv[Dom, Var, V](getParams: Dom => Option[Iterable[Var]])(using env: Environment[Var, V])(using env.EnvJoinComp, env.EnvJoin[V]) =
  new ParametersFromLookup[Dom, Var, V](
    getParams,
    p => env.lookup(p).get
  )

def parametersFromStore[Dom, Addr, V](getAddrs: Dom => Option[Iterable[Addr]])(using store: Store[Addr, V])(using store.StoreJoinComp, store.StoreJoin[V]) =
  new ParametersFromLookup[Dom, Addr, V](
    getAddrs,
    addr => store.read(addr).get
  )


final class ParametersFromLookup[Dom, P, V](getParams: Dom => Option[Iterable[P]], lookup: P => V) extends Sensitivity[Dom, Map[P, V]]:
  override def emptyContext: Map[P, V] = Map()

  private var params: Iterable[P] = null

  /** Indicates a context switch may appear at `dom`, only then `apply` will be called. */
  override def switchCall(dom: Dom): Boolean = getParams(dom) match
    case None => false
    case Some(params) =>
      this.params = params
      true

  /** Returns `None` if the context is definitely unchanged, returns `Some(ctx)` if `ctx` may be new. */
  override def apply(dom: Dom): Map[P, V] =
    Map() ++ params.map(p => p -> lookup(p))
