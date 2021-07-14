package sturdy.effect.environment

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

/*
 * An abstract environment that supports dynamic scoping. The environment tracks if a
 * variable is definitely bound, maybe bound, or unbound and calls the corresponding
 * continuations upon lookup. Internally, the environment tracks dirty variables that
 * have been (re)bound to optimize the join computation, since only values of dirty
 * variables need joining.
 */
trait AEnvironmentDynamicScope[Var, V](_init: Map[Var, (Boolean, V)])(using JoinValue[V])
  extends Environment[Var, V], JoinComputation:

  protected var env: Map[Var, (Boolean, V)] = _init
  protected var dirtyVars: Set[Var] = Set()

  override type EnvJoin[A] = JoinValue[A]

  def getEnv: Map[Var, (Boolean, V)] = env

  override def lookup[A](x: Var, found: V => A, notFound: => A): EnvJoined[A] =
    env.get(x) match
      case None => notFound
      case Some((definite, v)) =>
        if definite then
          found(v)
        else
          joinValues(found(v), notFound)

  override def bind(x: Var, v: V): Unit =
    dirtyVars += x
    env += x -> ((true, v))

  override def scoped[A](f: => A): A =
    val snapshot = env
    val snapshotDirty = dirtyVars
    try f finally {
      env = snapshot
      dirtyVars = snapshotDirty
    }

  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    val snapshot = env
    var joinedEnv = env
    var joinedDirtyVars = dirtyVars

    // These variables are definitely bound by f but were unbound before. We need to consilate them with g.
    var newDefiniteVarsInF: Map[Var, (Boolean, V)] = Map()

    val joinedResult = super.joinComputations {
      env = snapshot
      dirtyVars = Set()
      val fResult = f
      for (x <- dirtyVars) do
        joinedEnv.get(x) match
          case None =>
            val tup@(definite, v) = env(x)
            joinedEnv += x -> tup
            if (definite) then
              // This binding is new and definite in f. If g does not definitely bind x, we must later weaken this binding.
              newDefiniteVarsInF += x -> ((false, v))
          case Some((oldDefinite, oldVal)) =>
            // This binding already existed in env before.
            val (newDefinite, newVal) = env(x)
            joinedEnv += x -> ((oldDefinite && newDefinite, joinValues(oldVal, newVal)))
      fResult
    } {
      env = snapshot
      dirtyVars = Set()
      val gResult = g
      for (x <- dirtyVars) do
        joinedEnv.get(x) match
          case None =>
            // This binding is new in g and thus did _not_ occur in f.
            joinedEnv += x -> ((false, env(x)._2))
          case Some((oldDefinite, oldVal)) =>
            // This binding already existed in env before.
            val (newDefinite, newVal) = env(x)
            joinedEnv += x -> ((oldDefinite && newDefinite, joinValues(oldVal, newVal)))
            // we have used g to (possibly) weaken the binding of x
            newDefiniteVarsInF -= x

      // g did not bind x, hence weaken the binding of x
      joinedEnv ++= newDefiniteVarsInF
      gResult
    }

    env = joinedEnv
    dirtyVars = joinedDirtyVars
    joinedResult

