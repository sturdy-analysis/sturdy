package mixins

trait Environment[Var, Val] {
  val env: Environment[Var, Val] = this

  def lookup[A](success: Val ~> A)(fail: Thunk[A]): Var ~> A
  def extend: (Var, Val) ~> Unit
  def scoped[A](f: Thunk[A]): Thunk[A]
}

//trait ConcreteEnvironment[Var, Val] extends Environment[Var, Val] {
//  private var data: Map[Var, Val] = Map()
//
//  override def lookup[A](x: Var)(success: Val ~> A)(fail: Thunk[A]): A =
//    data.get(x) match {
//      case Some(value) => success(value)
//      case None => fail()
//    }
//
//  override def extend(x: Var, v: Val): Unit =
//    data += x -> v
//
//  override def scoped[A](f: Thunk[A]): A = {
//    val old = data
//    val a = f()
//    data = old
//    a
//  }
//}