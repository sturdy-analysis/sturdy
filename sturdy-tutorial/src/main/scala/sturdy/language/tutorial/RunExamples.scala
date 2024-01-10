package sturdy.language.tutorial

import Exp.*
import Stm.*
import Sign.*
import BackwardIntervalInstances.*
import BackwardIntervalInstances.Interval.*
import ConstantInstances.*
import ConstantInstances.Const.*


class ProgramRunner[V](val st: Stm, val initialState: ST[V], val interpreter: BackGenericInterpreter[V]) {
  def run(): ST[V] =
    val star = "***************************************"
    println("Original Program:")
    val transformedSt = st.transform
    println(transformedSt.toString)
    println(star)
    println("Postcondition: " + initialState)
    println(star)
    val finalState = interpreter.runProg(transformedSt, initialState)
    println("Trace:")
    interpreter.getTrace
    println(star)
    println("Final State: " + finalState)
    finalState
}



object Examples {

  def example1(): (Stm, Map[String, Sign]) =
    val st = Assign("y", Mul(Var("x"), NumLit(2)))
    val initialState = Map("y" -> Pos, "x" -> TopSign)
    (st, initialState)

  def example2(): (Stm, Map[String, Sign]) =
    val st = If(Gt(Var("x"), NumLit(0)),
      Assign("y", Sub(NumLit(0), Var("x"))),
      Some(Assign("y", Var("x")))
    )
    val initialState = Map("x" -> TopSign, "y" -> Pos)
    (st, initialState)

  def example3(): (Stm, Map[String, Sign]) =
    val st = Block(Seq(
      Assign("y", NumLit(1)),
      Assign("x", NumLit(10)),
      Assign("z", NumLit(-10)),
      While(Gt(Var("x"), NumLit(0)), Block(Seq(
        Assign("x", Add(Var("x"), NumLit(1))),
        Assign("y", Mul(NumLit(2), Var("y")))
      )))
    ))
    val initialState = Map("y" -> Pos, "x" -> Zero, "z" -> TopSign)
    (st, initialState)

  def example4(): (Stm, Map[String, Sign]) =
    val st = Block(List(
      Assign("y1", NumLit(1)),
      Assign("x1", NumLit(10)),
      Assign("z1", NumLit(-10)),
      While(Gt(Var("x1"), NumLit(0)), Block(List(
        Assign("x2", Add(Var("x1"), NumLit(1))),
        Assign("y2", Mul(NumLit(2), Var("y1"))),
        Assign("x1", Var("x2")), // Update x1 and y1 for the next iteration
        Assign("y1", Var("y2"))
      )))
    ))
    val initialState = Map("y1" -> Pos, "x1" -> TopSign, "z1" -> Neg, "x2" -> TopSign, "y2" -> TopSign)
    (st, initialState)

  def example5(): (Stm, Map[String, Interval]) =
    val st = If(Gt(Var("x0"), NumLit(0)),
      Assign("x1", Var("x0")),
      Some(Assign("x1", Sub(NumLit(0), Var("x0"))))
    )
    val initialState = Map("x0" -> Interval.ITop, "x1" -> Interval.I(2,5))
    (st, initialState)

  def example6(): (Stm, Map[String, Interval]) =
    val st = Block(Seq(
      Assign("x", NumLit(10)),
      While(Gt(Var("x"), NumLit(0)), Block(Seq(
        Assign("x", NumLit(2))
      )))
    ))
    val finalState = Map("x" -> Interval.I(1,5)) // Initial state with x having an unconstrained value
    (st, finalState)

  def example7(): (Stm, Map[String, Sign]) =
    val st = Block(Seq(
      Assign("x", NumLit(10)),
      While(Gt(Var("x"), NumLit(20)), Block(Seq(
        Assign("x", Add(Var("x"), NumLit(0)))
      )))
    ))
    val finalState = Map("x" -> Pos)
    (st, finalState)


  def example9(): (Stm, Map[String, Sign]) =
    val st = Block(Seq(
      Assign("y", NumLit(1)),
      Assign("x", NumLit(-10)),
      Assign("z", NumLit(10)),
      While(Lt(Var("x"), NumLit(0)),
        Block(Seq(
          Assign("x", Sub(Var("x"), NumLit(0))),
          Assign("y", Mul(NumLit(2), Var("y")))
        ))
      )
    ))
    val initialState = Map(
      "y" -> Pos, // Assuming Pos represents a positive interval
      "x" -> TopSign, // ITop for an unconstrained interval
      "z" -> TopSign
    )
    (st, initialState)

  def example10(): (Stm, Map[String, Sign]) = {
    val st = Block(Seq(
      Assign("n", NumLit(5)),
      Assign("fact", NumLit(1)),
      While(Gt(Var("n"), NumLit(0)),
        Block(Seq(
          Assign("fact", Mul(Var("fact"), Var("n"))),
          Assign("n", Sub(Var("n"), NumLit(1)))
        ))
      )
    ))
    val initialState = Map(
      "n" -> Zero, // Assuming Zero represents a zero interval
      "fact" -> Pos // Assuming Pos represents a positive interval
    )
    (st, initialState)
  }

  def example11(): (Stm, Map[String, Const]) = {
    val st = Block(Seq(
      Assign("a", NumLit(3)),
      Assign("b", NumLit(5)),
      Assign("c", Add(Var("a"), Var("b"))), // c = 3 + 5
      Assign("d", Sub(Var("c"), NumLit(3))) // d = 8 - 3
    ))
    val postState = Map(
      "d" -> C // Known postcondition for d
    )
    (st, postState)
  }



}


object MainApp {
  def main(args: Array[String]): Unit = {
    val signInterpreter = new BackwardSignInterpreter()
    val intervalInterpreter = new BackwardIntervalInterpreter()
    val constantInterpreter = new BackwardConstantInterpreter()
//

    // To run example1
//    val (st3, initialState3) = Examples.example3()
//    runProgram(st3, initialState3, interpreter)

    val (st, finalState) = Examples.example11()
    runProgram(st, finalState, constantInterpreter)

  }

  private def runProgram[V](st: Stm, initialState: Map[String, V], interpreter: BackGenericInterpreter[V]): Unit =
    new ProgramRunner(st, initialState, interpreter).run()
}

