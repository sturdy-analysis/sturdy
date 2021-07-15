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

  override type EnvJoin[A] = JoinValue[A]

  protected var env: Map[Var, (Boolean, V)] = _init
  protected var dirtyVars: Set[Var] = Set()

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
        val (definite, newVal) = env(x)
        joinedEnv.get(x) match
          case None =>
            // This binding is new, so we add an entry for it
            joinedEnv += x -> ((definite, newVal))
            if definite then
            // This binding is definite in f. If g does not definitely bind x, we must later mark this binding as non-definite.
              newDefiniteVarsInF += x -> ((false, newVal))
          case Some((_, oldVal)) =>
            // This binding already existed in store before.
            if definite then
              // This binding is definite in f.
              joinedEnv += x -> ((true, newVal))
              // If g does not definitely bind x, we must later mark this binding as non-definite _and_ join it with the old value (which is retained through g).
              newDefiniteVarsInF += x -> ((false, joinValues(oldVal, newVal)))
            else
            // This binding is not definite in f
              joinedEnv += x -> ((false, joinValues(oldVal, newVal)))
      fResult
    } {
      env = snapshot
      dirtyVars = Set()
      val gResult = g
      for (x <- dirtyVars) do
        joinedEnv.get(x) match
          case None =>
            // This binding is new in g and thus did neither occur in f nor in the original store.
            joinedEnv += x -> ((false, env(x)._2))
          case Some((oldDefinite, oldVal)) =>
            // This binding already existed in store before or was added by f.
            val (definite, newVal) = env(x)

            // If the binding was definite in f, then oldDefinite==true and oldVal==fVal.
            // If it was non-definite in f, then oldDefinite==false and oldVal==joinValues(prevVal, fVal).
            // If it was not bound by f, then oldDefinite==prevDefinite and oldVal==prevVal.

            if (definite) {
              // This binding is definite in g.
              joinedEnv += x -> ((oldDefinite, joinValues(oldVal, newVal)))
            } else {
              // This binding is not definite in g
              newDefiniteVarsInF.get(x) match {
                case Some((_, weakenedFVal)) =>
                  // Binding was definite in f, weaken it
                  joinedEnv += x -> ((oldDefinite, joinValues(weakenedFVal, newVal)))
                case None =>
                  // Binding was not bound or non-definite in f
                  joinedEnv += x -> ((oldDefinite, joinValues(oldVal, newVal)))
              }
            }
            newDefiniteVarsInF -= x

      // g did not definitely bind x, hence weaken the binding of x
      joinedEnv ++= newDefiniteVarsInF
      gResult
    }

    env = joinedEnv
    dirtyVars = joinedDirtyVars
    joinedResult

