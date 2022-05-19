package sturdy.fix.context

import sturdy.data.MayJoin
import sturdy.effect.environment.Environment
import sturdy.effect.store.Store


def parameters[Dom, Var, V](getParams: Dom => Option[Map[Var, V]]) =
  new Parameters[Dom, Var, V](getParams)

def parametersFromEnv[Dom, Var, V, J[_] <: MayJoin[_]](getParams: Dom => Option[Iterable[Var]])(using env: Environment[Var, V, J])(using J[V]) =
  new ParametersFromLookup[Dom, Var, V](
    getParams,
    p => env.lookup(p).get
  )

def parametersFromStore[Dom, Addr, V, J[_] <: MayJoin[_]](getAddrs: Dom => Option[Iterable[Addr]])(using store: Store[Addr, V, J])(using J[V]) =
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

final class Parameters[Dom, P, V](getParams: Dom => Option[Map[P, V]]) extends Sensitivity[Dom, Map[P, V]]:
  override def emptyContext: Map[P, V] = Map()

  private var params: Map[P, V] = Map()

  override def switchCall(dom: Dom): Boolean = getParams(dom) match
    case None => false
    case Some(params) =>
      this.params = params
      true

  override def apply(dom: Dom): Map[P, V] =
    params
