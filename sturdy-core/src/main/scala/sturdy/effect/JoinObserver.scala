package sturdy.effect

trait JoinObserver:
  def joinStart(): Unit
  def joinSwitch(): Unit
  def joinEnd(): Unit

trait ObservableJoin:
  private var observers: List[JoinObserver] = Nil
  def addJoinObserver(jo: JoinObserver): Unit =
    observers +:= jo
  
  def joinStart(): Unit =
    observers.foreach(_.joinStart())
  def joinSwitch(): Unit =
    observers.foreach(_.joinSwitch())
  def joinEnd(): Unit =
    observers.foreach(_.joinEnd())