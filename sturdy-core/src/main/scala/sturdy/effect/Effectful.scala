//package sturdy.effect
//
//import sturdy.data.CombineUnit
//import sturdy.effect.Effect.StarvedJoin
//import sturdy.values.{Combine, Widening}
//
//import scala.util.Failure
//import scala.util.Success
//import scala.util.Try
//
//trait Effect extends ObservableJoin:
//  type State
//  def getState: State
//  def setState(s: State): Unit
//
//  def combine[W <: Widening]: Combine[State, W] = throw new UnsupportedOperationException()
//
//  def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = None
//
//
//
//trait Stateless extends Effect:
//  type State = Unit
//  def getState: State = {}
//  def setState(s: State): Unit = {}
//
//  override def combine[W <: Widening]: Combine[Unit, W] = CombineUnit
//
//object Effect:
//  case class StarvedJoin(ex1: SturdyThrowable, ex2: SturdyThrowable) extends Exception(s"Starved Join with $ex1 and $ex2") with SturdyThrowable
////    override val isBottom: Boolean = false // ex1.isBottom && ex2.isBottom
//
//
