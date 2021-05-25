package mixins

//object Example extends App {
//
//  trait Semantics[Var, Val, Addr, Exc]
//    extends Environment[Var, Val]
//       with Store[Addr, Val]
//       with Exception[Exc] {
//  }
//
//  type ConcreteSemanticsType = Semantics[String, Double, Int, String]
//
//  def example(sem: ConcreteSemanticsType): Double = sem.env.scoped {
//    import sem._
//    env.extend("x", 1.0)
//    env.extend("y", 2.0)
//    env.lookup("z") {
//      v => store.write(10, v)
//    } {
//    }
//    val result = store.readOrElse(10) {
//      exc.throwing("Invalid address")
//    }
//    result
//  }
//
//  def exampleHandled(sem: ConcreteSemanticsType): Double =
//    sem.trying(example(sem)) {
//      case "Unbound variable" => -1.0
//      case "Invalid address" => -2.0
//    }
//
//  object ConcreteSemantics extends Semantics[String, Double, Int, String]
//    with ConcreteEnvironment[String, Double]
//    with ConcreteStore[Int, Double]
//    with ConcreteException[String]
//
//  println(exampleHandled(ConcreteSemantics))
//}
