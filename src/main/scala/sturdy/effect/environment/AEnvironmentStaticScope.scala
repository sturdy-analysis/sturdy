package sturdy.effect.environment

import sturdy.effect.JoinComputation

/*
 * An abstract environment that assumes static scoping of bindings. In particular,
 * joined computations may not change the environment. Usually, this is achieved in the
 * generic interpreter by calling `scoped` around each alternative branch. For example:
 *
 *     case If(e, thn, els) => if_(eval(e), scoped(thn), scoped(els))
 */
trait AEnvironmentStaticScope[Var, V] extends CEnvironment[Var, V], JoinComputation:
  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    val snapshot = env
    super.joinComputations(ensureUnchangedEnv(f, snapshot))(ensureUnchangedEnv(g, snapshot))

  def ensureUnchangedEnv[A](f: => A, oldEnv: Map[Var, V]): A =
    val result = f
    if !(oldEnv eq this.env) then
      throw new IllegalStateException(s"Statically scoped environment has changed at join point, which is illegal. Old environment was $oldEnv, new environment is ${this.env}.")
    result
