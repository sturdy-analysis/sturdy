package sturdy.data

import sturdy.values.JoinValue
import sturdy.effect.Effectful

type NoJoin[A] = Unit
type Join[A] = (JoinValue[A], Effectful)

given JoinedJoinValue[A](using j: Join[A]): JoinValue[A] = j._1
given JoinedJoinEffects[A](using j: Join[A]): Effectful = j._2
given MakeJoined[A](using jv: JoinValue[A], je: Effectful): Join[A] = (jv, je)
