package sturdy.language.tutorial

import scala.collection.mutable

/**
 * This is our reference interpreter for the while language. The Syntax of the language is defined in [[Syntax]].
 * We have
 *   - only integers as values
 *   - numeric operations on integers like add, sub, mult, div
 *   - booleans are encoded as integers: true as != 0 and false as 0
 *   - mutable variables (need to be initialized before reading)
 *   - if, while and assign statements
 *   
 * Programs define functions Int -> Int. The argument is encoded as variable "arg", the result as variable "result".
 * 
 * The [[ReferenceInterpreter]] defines the semantics of the language as a standard big-step interpreter.
 */
class ReferenceInterpreter:
  type Store = mutable.Map[String,Int]

  /** The store for mutable variables. */
  private var store: Store = mutable.Map()

  import Exp.*
  import Stm.*

  /**
   * Defines evaluation of expression. Evaluation can fail in case of division by zero.
   */
  private def evalExp(e: Exp): Int = e match
    case NumLit(n) => n
    case Var(name) => store.getOrElse(name, throw new Exception(s"uninitilized variable: $name"))
    case Add(e1,e2) => evalExp(e1) + evalExp(e2)
    case Sub(e1,e2) => evalExp(e1) - evalExp(e2)
    case Mul(e1,e2) => evalExp(e1) * evalExp(e2)
    case Div(e1,e2) =>
      val e2Val = evalExp(e2)  
      if (e2Val == 0)
        throw new Exception("division by zero")
      else
        evalExp(e1) / e2Val
    case Lt(e1,e2) => if (evalExp(e1) < evalExp(e2)) 1 else 0

  /**
   * Defines evaluation of Statements.
   */
  private def evalStm(s: Stm): Unit = s match
    case Assign(name, e) => store(name) = evalExp(e)
    case If(cond,thn,els) =>
      val condVal = evalExp(cond)
      if (condVal != 0)
        evalStm(thn)
      else
        els.map(evalStm).getOrElse(())
    case w@While(cond,body) =>
      val condVal = evalExp(cond)
      if (condVal != 0) {
        evalStm(body)
        evalStm(w)
      }
    case Block(body) => body.foreach(evalStm)

  /** 
   * Runs a program s with input arg and returns the value of the store for variable result after evaluating all statements.
   */
  def runProg(arg: Int, s: Stm): Int =
    store = mutable.Map("arg" -> arg)
    evalStm(s)
    store.getOrElse("result", throw new Exception(s"unitialized variable: result"))
    