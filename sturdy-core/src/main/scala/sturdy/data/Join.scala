package sturdy.data

import sturdy.Executable
import sturdy.effect.EffectStack
import sturdy.values.Join

//sealed trait Joining[A]:
//  type Val
//  type Eff
//case class NoJoin[A]() extends Joining[A]:
//  override type Val = Unit
//  override type Eff = Unit
//case class WithJoin[A]() extends Joining[A]:
//  override type Val = Join[A]
//  override type Eff = EffectStack

enum MayJoin[A]:
  case NoJoin()
  case WithJoin(j: Join[A], eff: EffectStack)

type NoJoin[A] = MayJoin.NoJoin[A]
type WithJoin[A] = MayJoin.WithJoin[A]

given noJoin[A]: NoJoin[A] = MayJoin.NoJoin()

inline def joinComputations[A](f: Executable[A])(g: Executable[A])(using w: WithJoin[A]): A =
  w.eff.joinComputations(f)(g)(using w.j)

inline def joinWithFailure[A](f: => A)(g: => Nothing)(using eff: EffectStack): A =
  eff.joinWithFailure(f)(g)

inline def mapJoin[A, B](as: Iterable[A], f: A => B)(using w: WithJoin[B]): B =
  w.eff.mapJoin(as, f)(using w.j)

//given JoinedJoin[A](using j: WithJoin[A]): Join[A] = j._1
//given JoinedJoinEffects[A](using j: WithJoin[A]): Effectful = j._2
given MakeJoined[A](using jv: Join[A], je: EffectStack): WithJoin[A] = MayJoin.WithJoin(jv, je)

