package sturdy.effect.environment

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.JoinComputation
import sturdy.values.Abstractly
import sturdy.values.JoinValue

/*
 * An abstract environment that supports dynamic scoping. The environment tracks if a
 * variable is definitely bound, maybe bound, or unbound and calls the corresponding
 * continuations upon lookup. Internally, the environment tracks dirty variables that
 * have been (re)bound to optimize the join computation, since only values of dirty
 * variables need joining.
 */
trait AEnvironmentDynamicScope[Var, V](_init: Map[Var, (Boolean, V)])(using j: JoinValue[V])
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
          joinComputations(found(v))(notFound)

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

  override def clear(): Unit =
    dirtyVars ++= env.keys
    env = Map()
  
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

      joinedDirtyVars ++= dirtyVars
      for (x <- dirtyVars) do
        val (definite, newVal) = env(x)
        joinedEnv.get(x) match
          case None =>
            // This binding is new, so we add an entry for it
            joinedEnv += x -> ((definite, newVal))
            if definite then
            // This binding is definite in f. If g does not definitely bind x, we must later mark this binding as non-definite.
              newDefiniteVarsInF += x -> ((false, newVal))
          case Some((oldDefinite, oldVal)) =>
            // This binding already existed in store before.
            if definite then
              // This binding is definite in f.
              joinedEnv += x -> ((true, newVal))
              // If g does not definitely bind x, we must later mark this binding as non-definite _and_ join it with the old value (which is retained through g).
              newDefiniteVarsInF += x -> ((oldDefinite, j.joinValues(oldVal, newVal)))
            else
            // This binding is not definite in f
              joinedEnv += x -> ((false, j.joinValues(oldVal, newVal)))
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
            joinedEnv += x -> ((false, env(x)._2))
          case Some((oldDefinite, oldVal)) =>
            // This binding already existed in store before or was added by f.
            val (definite, newVal) = env(x)

            // If the binding was definite in f, then oldDefinite==true and oldVal==fVal.
            // If it was non-definite in f, then oldDefinite==false and oldVal==joinValues(prevVal, fVal).
            // If it was not bound by f, then oldDefinite==prevDefinite and oldVal==prevVal.

            if (definite) {
              // This binding is definite in g.
              joinedEnv += x -> ((oldDefinite, j.joinValues(oldVal, newVal)))
            } else {
              // This binding is not definite in g
              newDefiniteVarsInF.get(x) match {
                case Some((_, weakenedFVal)) =>
                  // Binding was definite in f, weaken it
                  joinedEnv += x -> ((oldDefinite, j.joinValues(weakenedFVal, newVal)))
                case None =>
                  // Binding was not bound or non-definite in f
                  joinedEnv += x -> ((oldDefinite, j.joinValues(oldVal, newVal)))
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
    } else if (env.exists(e => e._2._1 && !abstractedKeys.contains(e._1))) {
      val missing = env.filter(_._2._1).keySet -- abstractedKeys
      IsSound.NotSound(s"${classOf[AEnvironmentDynamicScope[_, _]].getName}: Expected all definitely bound keys to be bound in concrete environment, but $missing are missing in $env")
    } else {
      c.getEnv.foreachEntry { case (x, v) =>
        val subSound = vSoundness.isSound(v, env(varAbstractly.abstractly(x))._2)
        if (subSound.isNotSound)
          return subSound
      }
      IsSound.Sound
    }
  }
