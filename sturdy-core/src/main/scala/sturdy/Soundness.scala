package sturdy

import sturdy.values.*

import scala.util.Try

enum IsSound:
  case Sound
  case NotSound(reason: String)

  def isSound: Boolean = this match
    case Sound => true
    case _ => false
  def isNotSound: Boolean = this match
    case Sound => false
    case _ => true

  def &&(other: IsSound): IsSound =
    if (this.isSound)
      other
    else
      this

trait Soundness[C, A]:
  def isSound(c: C, a: A): IsSound

object Soundness:
  def isSound[C, A](c: C, a: A)(using s: Soundness[C, A]): IsSound = s.isSound(c, a)

given AbstractlySound[C, A](using abs: Abstractly[C, A], po: PartialOrder[A]): Soundness[C, A] with
  override def isSound(c: C, a: A): IsSound =
    if (po.lteq(abs.abstractly(c), a))
      IsSound.Sound
    else
      IsSound.NotSound(s"Value $c abstracts to ${abs.abstractly(c)} but was not less-than-eq the abstract value $a")

def seqIsSound[v1,v2](using vSoundness: Soundness[v1,v2]): Soundness[Seq[v1], Seq[v2]] = new Soundness[Seq[v1], Seq[v2]] {
  override def isSound(c: Seq[v1], a: Seq[v2]): IsSound =
    if (c.length != a.length)
      IsSound.NotSound(s"Sequence lengths are not equal: $c $a")
    else
      c.zip(a).foldLeft(IsSound.Sound){
        case (s, (cE,aE)) => s && vSoundness.isSound(cE,aE)
      }
}