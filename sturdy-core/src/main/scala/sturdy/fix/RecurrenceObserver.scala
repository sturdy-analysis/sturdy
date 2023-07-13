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
