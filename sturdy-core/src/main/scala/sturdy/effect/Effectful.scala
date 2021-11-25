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
  
  def getComputationJoiner[A]: Option[ComputationJoiner[A]] = None

trait Stateless extends Effectful:
  type State = Unit
  def getState: State = {}
  def setState(s: State): Unit = {}


object Effectful:
  case class StarvedJoin(ex1: SturdyThrowable, ex2: SturdyThrowable) extends Exception(s"Starved Join with $ex1 and $ex2") with SturdyThrowable
//    override val isBottom: Boolean = false // ex1.isBottom && ex2.isBottom


