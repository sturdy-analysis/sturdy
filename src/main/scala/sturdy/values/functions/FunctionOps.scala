package sturdy.values.functions

/*
 * Function values (not closures).
 */
trait FunctionOps[F, A, R, V] {
  def funValue(fun: F): V
  def invokeFun(fun: V, args: Seq[A])(invoke: (F, Seq[A]) => R): R
}

given ConcreteFunctionOps[F, A, R]: FunctionOps[F, A, R, F] with
  def funValue(fun: F): F = fun
  def invokeFun(fun: F, args: Seq[A])(invoke: (F, Seq[A]) => R): R = invoke(fun, args)
