package sturdy.fix

import sturdy.effect.TrySturdy

trait RecurrenceObserver[In, Out]:
  def recurrent(in: In, out: Option[TrySturdy[Out]]): Unit
  def repeating(in: In, out: TrySturdy[Out]): Unit

trait ObservableRecurrence[In, Out]:
  private var observers: List[RecurrenceObserver[In, Out]] = Nil
  def addRecurrenceObserver(ro: RecurrenceObserver[In, Out]): Unit =
    observers +:= ro

  def notifyRecurrent(in: In, out: Option[TrySturdy[Out]]): Unit =
    observers.foreach(_.recurrent(in, out))
  def notifyRepeating(in: In, out: TrySturdy[Out]): Unit =
    observers.foreach(_.repeating(in, out))


final class DomLogger[Dom] extends Logger[Dom, Any]:
  private var doms: List[Dom] = List()

  override def enter(dom: Dom): Unit = doms = dom :: doms

  override def exit(dom: Dom, codom: TrySturdy[Any]): Unit =
    doms = doms.drop(1)

  def getDoms: List[Dom] = doms

  def currentDom: Option[Dom] =
    doms.headOption