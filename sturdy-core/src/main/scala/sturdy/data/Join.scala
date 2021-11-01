package sturdy.data

import sturdy.values.Join
import sturdy.effect.Effectful

type NoJoin[A] = Unit
type WithJoin[A] = (Join[A], Effectful)

inline def joinComputations[A](f: => A)(g: => A)(using j: WithJoin[A]): A =
  j._2.joinComputations(f)(g)(using j._1)

inline def mapJoin[A, B](as: Iterable[A], f: A => B)(using j: WithJoin[B]): B =
  j._2.mapJoin(as, f)(using j._1)

//given JoinedJoin[A](using j: WithJoin[A]): Join[A] = j._1
//given JoinedJoinEffects[A](using j: WithJoin[A]): Effectful = j._2
given MakeJoined[A](using jv: Join[A], je: Effectful): WithJoin[A] = (jv, je)
