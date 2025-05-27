package sturdy.effect.environment

import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.Effect

import scala.util.boundary, boundary.break

/*
 * An abstract environment that assumes static scoping of bindings. In particular,
 * joined computations may not change the environment. Usually, this is achieved in the
 * generic interpreter by calling `scoped` around each alternative branch. For example:
 *
 *     case If(e, thn, els) => boolBranch(eval(e), scoped(thn), scoped(els))
 */
trait AEnvironmentStaticScope[Var, V] extends ConcreteEnvironment[Var, V], Effect:
  def environmentIsSound[VC](c: ConcreteEnvironment[Var, VC])(using vSoundness: Soundness[VC, V]): IsSound = boundary:
    if (c.getEnv.keySet != env.keySet) {
      val different = (c.getEnv.keySet -- env.keySet) ++ (env.keySet -- c.getEnv.keySet)
      IsSound.NotSound(s"${classOf[AEnvironmentStaticScope[_, _]].getName}: Expected identical keys but environments differ for $different in $env")
    } else {
      c.getEnv.foreachEntry { case (x, v) =>
        val subSound = vSoundness.isSound(v, env(x))
        if (subSound.isNotSound)
          break(subSound)
      }
      IsSound.Sound
    }