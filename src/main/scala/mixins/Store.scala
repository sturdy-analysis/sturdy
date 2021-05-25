package mixins

trait Store[Addr,Val] {
  val store: Store[Addr, Val] = this

  def read[A](addr: Addr)(success: Val ~> A)(fail: Thunk[A]): A
  def write(addr: Addr, v: Val): Unit

  def readOrElse(addr: Addr)(fail: Unit ~> Val): Val =
    read(addr)(identity)(fail)
}

trait ConcreteStore[Addr, Val] extends Store[Addr, Val] {
  private var data: Map[Addr, Val] = Map()

  override def read[A](addr: Addr)(success: Val ~> A)(fail: Thunk[A]): A =
    data.get(addr) match {
      case Some(value) => success(value)
      case None => fail()
    }

  override def write(addr: Addr, v: Val): Unit =
    data += addr -> v
}

trait AbstractStore[Addr, Val] extends Store[Addr, Val] {
  private var data: Map[Addr, (Boolean,Val)] = Map()

  override def read[A](addr: Addr)(success: Val ~> A)(fail: Thunk[A]): A =
    data.get(addr) match {
      case Some((true, value)) => success(value)
      case Some((false, value)) => success(value) // join fail
      case None => fail()
    }

  override def write(addr: Addr, v: Val): Unit =
    data += addr -> (true, v)
}