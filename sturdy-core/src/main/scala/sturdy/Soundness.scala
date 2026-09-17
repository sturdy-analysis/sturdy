package sturdy

import sturdy.data.{JOptionA, JOptionC}
import sturdy.values.*

import scala.util.Try

enum IsSound:
  case Sound
  case NotSound(reason: String, ex: Exception = new Exception())

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

  override def toString: String = this match
    case Sound => "Sound"
    case NotSound(msg, ex) => s"NotSound($msg) in\n${ex.getStackTrace.mkString("\n")}"

object IsSound:
  def apply(isSound: Boolean, unsoundMsg: String): IsSound =
    if(isSound)
      IsSound.Sound
    else
      IsSound.NotSound(unsoundMsg)

trait Soundness[-C, -A]:
  def isSound(c: C, a: A): IsSound

object Soundness:
  def isSound[C, A](c: C, a: A)(using s: Soundness[C, A]): IsSound = s.isSound(c, a)

given AbstractlySound[C, A](using abs: Abstractly[C, A], po: PartialOrder[A]): Soundness[C, A] with
  override def isSound(c: C, a: A): IsSound =
    if (po.lteq(abs.apply(c), a))
      IsSound.Sound
    else {
      IsSound.NotSound(s"Value $c abstracts to ${abs.apply(c)} but was not less-than-eq the abstract value $a")
    }
    
given JOptionSound[C, A](using sound: Soundness[C, A]): Soundness[JOptionC[C], JOptionA[A]] with {
  override def isSound(c: JOptionC[C], a: JOptionA[A]): IsSound = (c, a) match {
    case (_: JOptionC.None[C], _: JOptionA.None[A]) => IsSound.Sound
    case (_: JOptionC.None[C], _: JOptionA.NoneSome[A]) => IsSound.Sound
    case (_: JOptionC.None[C], _: JOptionA.Some[A]) => IsSound.NotSound("none not included")
    case (JOptionC.Some(_), _: JOptionA.None[A]) => IsSound.NotSound("value not included")
    case (JOptionC.Some(cv), JOptionA.NoneSome(av)) => sound.isSound(cv, av)
    case (JOptionC.Some(cv), JOptionA.Some(av)) => sound.isSound(cv, av)
  }
}

def seqIsSound[v1,v2](using vSoundness: Soundness[v1,v2]): Soundness[Seq[v1], Seq[v2]] = new Soundness[Seq[v1], Seq[v2]] {
  override def isSound(c: Seq[v1], a: Seq[v2]): IsSound =
    if (c.length != a.length)
      IsSound.NotSound(s"Sequence lengths are not equal: $c $a")
    else
      c.zip(a).foldLeft(IsSound.Sound){
        case (s, (cE,aE)) => s && vSoundness.isSound(cE,aE)
      }
}