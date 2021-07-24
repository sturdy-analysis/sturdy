package sturdy.effect.closure

import sturdy.effect.environment.Environment
import sturdy.effect.failure.Failure

import scala.util.Random

trait Closure[Expr, V]:
  type ClsJoin[A]
  final type ClsJoined[A] = ClsJoin[A] ?=> A

  def closure(e: Expr): V
//  def apply[A,B](foo: (Expr,List[B]) => A, cls: V, args: List[B]): ClsJoined[A]
  def apply[A,B](foo: (Expr,B) => A, cls: V, foo_args: B): ClsJoined[A]
