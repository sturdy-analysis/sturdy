package sturdy.control

trait ControlObserver[Atom, Section, Exc]:
  def handle(ev: ControlEvent[Atom, Section, Exc]): Unit

