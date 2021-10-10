package sturdy.fix.context

import sturdy.effect.environment.Environment
import sturdy.effect.store.Store



def parametersFromEnv[Dom, Var, V, MayJoin[_]](getParams: Dom => Option[Iterable[Var]])(using env: Environment[Var, V, MayJoin])(using MayJoin[V]) =
  new ParametersFromLookup[Dom, Var, V](
    getParams,
    p => env.lookup(p).get
  )

def parametersFromStore[Dom, Addr, V, MayJoin[_]](getAddrs: Dom => Option[Iterable[Addr]])(using store: Store[Addr, V, MayJoin])(using MayJoin[V]) =
  new ParametersFromLookup[Dom, Addr, V](
    getAddrs,
    addr => store.read(addr).get
  )


final class ParametersFromLookup[Dom, P, V](getParams: Dom => Option[Iterable[P]], lookup: P => V) extends Sensitivity[Dom, Map[P, V]]:
  override def emptyContext: Map[P, V] = Map()

  private var params: Iterable[P] = null

  override def switchCall(dom: Dom): Boolean = getParams(dom) match
    case None => false
    case Some(params) =>
      this.params = params
      true

  override def apply(dom: Dom): Map[P, V] =
    Map() ++ params.map(p => p -> lookup(p))
