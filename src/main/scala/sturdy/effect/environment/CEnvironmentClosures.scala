package sturdy.effect.environment

import sturdy.effect.closure.Closure
//import sturdy.values.closures.Closure

/*
 * A concrete environment.
 */
trait CEnvironmentClosures[Var, Addr, Expr](_init: Map[Var, Addr] = Map())
  extends Environment[Var, Addr], Closure[Expr, (Expr, Map[Var, Addr])]:
  override type EnvJoin[_] = Unit
  override type ClsJoin[_] = Unit

  protected var env: Map[Var, Addr] = _init
  def getEnv: Map[Var, Addr] = env
  def setEnv(env: Map[Var, Addr]) = this.env = env
  
  override def lookup[A](x: Var, found: Addr => A, notFound: => A): EnvJoined[A] =
    env.get(x).map(found).getOrElse(notFound)

  override def bind(x: Var, v: Addr): Unit = env = env + (x -> v)

  override def scoped[A](f: => A): A =
    val snapshot = env
    try f finally {
      env = snapshot
    }

  override def closure(e: Expr): (Expr, Map[Var, Addr]) = (e, getEnv)

  // TODO: create functionality similar to Reader.local?
  override def apply[A,B](foo: (Expr, B) => A, cls: (Expr, Map[Var, Addr]), foo_args: B): ClsJoined[A] =
    val snapshot = env
    try {
      setEnv(cls._2)
      foo(cls._1, foo_args)
    } finally {
      env = snapshot
    }

