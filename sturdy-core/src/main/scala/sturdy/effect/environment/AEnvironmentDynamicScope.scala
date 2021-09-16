package sturdy.effect.environment

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.MayComputeOne
import sturdy.effect.MayComputeOne.*
import sturdy.effect.Effectful
import sturdy.effect.Join
import sturdy.values.Abstractly
import sturdy.values.JoinValue
import sturdy.values.MayMust


/*
 * An abstract environment that supports dynamic scoping. The environment tracks if a
 * variable is definitely bound, maybe bound, or unbound and calls the corresponding
 * continuations upon lookup. Internally, the environment tracks dirty variables that
 * have been (re)bound to optimize the join computation, since only values of dirty
 * variables need joining.
 */
trait AEnvironmentDynamicScope[Var, V](_init: Map[Var, MayMust[V]])(using j: JoinValue[V])
  extends Environment[Var, V], Effectful:

  override type EnvJoin[A] = Join[A]

  protected var env: Map[Var, MayMust[V]] = _init
  protected var dirtyVars: Set[Var] = Set()

  def getEnv: Map[Var, MayMust[V]] = env
  protected def setEnv(env: Map[Var, MayMust[V]]) = this.env = env

  override def lookup(x: Var): MayComputeOne[V] =
    env.get(x) match
      case None => ComputesNot()
      case Some(MayMust.Must(v)) => Computes(v)
      case Some(MayMust.May(v)) => MaybeComputes(v)

  override def bind(x: Var, v: V): Unit =
    dirtyVars += x
    env += x -> (MayMust.Must(v))

  override def scoped[A](f: => A): A =
    val snapshot = env
    val snapshotDirty = dirtyVars
    try f finally {
      env = snapshot
      dirtyVars = snapshotDirty
    }

  override def clear(): Unit =
    dirtyVars ++= env.keys
    env = Map()
  
  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    val snapshot = env
    var joinedEnv = env
    var joinedDirtyVars = dirtyVars

    // These variables are definitely bound by f but were unbound before. We need to consilate them with g.
    var newDefiniteVarsInF: Map[Var, MayMust[V]] = Map()

    val joinedResult = super.joinComputations {
      env = snapshot
      dirtyVars = Set()

      val fResult = f

      joinedDirtyVars ++= dirtyVars
      for (x <- dirtyVars) do
        val newVal = env(x)
        joinedEnv.get(x) match
          case None =>
            // This binding is new, so we add an entry for it
            joinedEnv += x -> newVal
            if newVal.isMust then
            // This binding is definite in f. If g does not definitely bind x, we must later mark this binding as non-definite.
              newDefiniteVarsInF += x -> MayMust.May(newVal.get)
          case Some(oldVal) =>
            // This binding already existed in store before.
            if newVal.isMust then
              // This binding is definite in f.
              joinedEnv += x -> newVal
              // If g does not definitely bind x, we must later mark this binding as non-definite _and_ join it with the old value (which is retained through g).
              newDefiniteVarsInF += x -> oldVal.map(v => j.joinValues(v, newVal.get))
            else
            // This binding is not definite in f
              joinedEnv += x -> MayMust.May(j.joinValues(oldVal.get, newVal.get))
      fResult
    } {
      env = snapshot
      dirtyVars = Set()

      val gResult = g

      joinedDirtyVars ++= dirtyVars
      for (x <- dirtyVars) do
        joinedEnv.get(x) match
          case None =>
            // This binding is new in g and thus did neither occur in f nor in the original store.
            joinedEnv += x -> MayMust.May(env(x).get)
          case Some(oldVal) =>
            // This binding already existed in store before or was added by f.
            val newVal = env(x)

            // If the binding was definite in f, then oldDefinite==true and oldVal==fVal.
            // If it was non-definite in f, then oldDefinite==false and oldVal==joinValues(prevVal, fVal).
            // If it was not bound by f, then oldDefinite==prevDefinite and oldVal==prevVal.

            if (newVal.isMust) {
              // This binding is definite in g.
              joinedEnv += x -> oldVal.map(v => j.joinValues(v, newVal.get))
            } else {
              // This binding is not definite in g
              newDefiniteVarsInF.get(x) match {
                case Some(weakenedFVal) =>
                  // Binding was definite in f, weaken it
                  joinedEnv += x -> oldVal.map(_ => j.joinValues(weakenedFVal.get, newVal.get))
                case None =>
                  // Binding was not bound or non-definite in f
                  joinedEnv += x -> oldVal.map(v => j.joinValues(v, newVal.get))
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

  def environmentIsSound[cVar, cV](c: CEnvironment[cVar, cV])(using varAbstractly: Abstractly[cVar, Var], vSoundness: Soundness[cV, V]): IsSound = {
    val abstractedKeys = c.getEnv.keySet.map(varAbstractly.abstractly)
    if (!abstractedKeys.subsetOf(env.keySet)) {
      val missing = c.getEnv.keySet.flatMap{ k =>
        val ak = varAbstractly.abstractly(k)
        if (env.keySet.contains(ak))
          None
        else
          Some(s"abs($k)=$ak")
      }
      IsSound.NotSound(s"${classOf[AEnvironmentDynamicScope[_, _]].getName}: Expected all concrete keys to be contained, but $missing are missing in $env")
    } else if (env.exists(e => e._2.isMust && !abstractedKeys.contains(e._1))) {
      val missing = env.filter(_._2.isMust).keySet -- abstractedKeys
      IsSound.NotSound(s"${classOf[AEnvironmentDynamicScope[_, _]].getName}: Expected all definitely bound keys to be bound in concrete environment, but $missing are missing in $env")
    } else {
      c.getEnv.foreachEntry { case (x, v) =>
        val subSound = vSoundness.isSound(v, env(varAbstractly.abstractly(x)).get)
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }
