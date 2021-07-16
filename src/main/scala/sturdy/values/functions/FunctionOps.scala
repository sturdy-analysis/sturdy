package sturdy.values.functions

/*
 * Function values (not closures).
 */
trait FunctionOps[F, V] {
  def invoke(fun: F, args: Seq[V]): V
}
