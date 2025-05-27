package sturdy.effect

trait JoinObserver:
  def joinStart(): Unit
  def joinSwitch(leftFailed: Boolean): Unit
  def joinEnd(leftFailed: Boolean, rightFailed: Boolean): Unit
  def repeating(): Unit

trait ObservableJoin:
  private var observers: List[JoinObserver] = Nil
  def addJoinObserver(jo: JoinObserver): Unit =
    observers +:= jo

  def joinStart(): Unit =
    observers.foreach(_.joinStart())
  def joinSwitch(leftFailed: Boolean): Unit =
    observers.foreach(_.joinSwitch(leftFailed))
  def joinEnd(leftFailed: Boolean, rightFailed: Boolean): Unit =
    observers.foreach(_.joinEnd(leftFailed, rightFailed))
  def repeating(): Unit =
    observers.foreach(_.repeating())

object NoJoinsToObserve extends ObservableJoin