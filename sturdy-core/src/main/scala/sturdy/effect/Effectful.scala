package sturdy.effect

import sturdy.effect.Effectful.StarvedJoin
import sturdy.values.Join

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait Effectful extends ObservableJoin:
  type State
  def getState: State
  def setState(s: State): Unit
  
  private var _fRes: TrySturdy[_] = _
  protected def fRes: TrySturdy[_] = _fRes

  def getComputationJoiner[A]: Option[ComputationJoiner[A]] = None
  
  def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoiner {
    joinStart()
    override def inbetween(): Unit = joinSwitch()
    override def retainNone(): Unit = joinEnd()
    override def retainFirst(fRes: TrySturdy[A]): Unit = joinEnd()
    override def retainSecond(gRes: TrySturdy[A]): Unit = joinEnd()
    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit = joinEnd()
  }

trait Stateless extends Effectful:
  type State = Unit
  def getState: State = {}
  def setState(s: State): Unit = {}
  

object Effectful:
  case class StarvedJoin(ex1: SturdyThrowable, ex2: SturdyThrowable) extends Exception(s"Starved Join with $ex1 and $ex2") with SturdyThrowable
//    override val isBottom: Boolean = false // ex1.isBottom && ex2.isBottom


