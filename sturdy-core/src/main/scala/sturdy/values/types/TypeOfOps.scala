package sturdy.values.types

import sturdy.data.*

trait TypeOfOps[V, Ty, J[_] <: MayJoin[_]]:
  def typeOf[A](v: V)(f: Ty => A): J[A] ?=> A

  final def typeOf[A](v1: V, v2: V)(f: (Ty, Ty) => A): J[A] ?=> A =
    typeOf(v1)(t1 => typeOf(v2)(t2 => f(t1, t2)))

class ConcreteTypeOfOps[V, Ty](ty: V => Ty) extends TypeOfOps[V, Ty, NoJoin]:
  def typeOf[A](v: V)(f: Ty => A): NoJoin[A] ?=> A = f(ty(v))
