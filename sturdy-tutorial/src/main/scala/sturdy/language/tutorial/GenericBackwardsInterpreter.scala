package sturdy.language.tutorial

import sturdy.data.{JOption, JOptionC, MayJoin}
import MayJoin.*
import sturdy.effect.*
import sturdy.effect.failure.FailureKind
import sturdy.fix
import sturdy.language.tutorial.GenericInterpreter.*
import sturdy.values.{Combine, Finite, MaybeChanged, Unchanged, Widening}

type ST[V] = Map[String,V]


trait Unifiable[V]:
  def canUnify(v1: V, v2: V): Boolean

trait InvertOps[A]:
  def invConst(c: Int, a: A): Boolean
  def invAdd(a1: A, a2: A, a3: A): (A, A, A)
  def invSub(a1: A, a2: A, a3: A): (A, A, A)
  def invMul(a1: A, a2: A, a3: A): (A, A, A)
  def invDiv(a1: A, a2: A, a3: A): (A, A, A)
  def invLt(a1: A, a2: A, a3: A): (A, A, A)
  def invGt(a1: A, a2: A, a3: A): (A, A, A)

  def trueVal: A
  def falseVal: A
  def topVal: A


trait MyWiden[V]:
  def mywiden(prevState: ST[V], newState: ST[V]): ST[V] = newState




trait BackJoin[V]:
  type S = ST[V]
  def join(v1: V, v2: V): V
  def joinMaps(m1: S, m2: S): S =
    val keys = m1.keySet ++ m2.keySet
    (for {
      key <- keys
      value = (m1.get(key), m2.get(key)) match
        case (Some(v1), Some(v2)) => join(v1, v2)
        case (Some(v1), None)     => v1
        case (None, Some(v2))     => v2
        case (None, None)         => throw new IllegalArgumentException("Key not found in either map")
    } yield (key -> value)).toMap


/*
 */
trait BackGenericInterpreter[V]:
  var trace: List[(String, ST[V])] = List()
  type S = ST[V]
  val invertOps: InvertOps[V]
  val numericOps: NumericOps[V]
  val unifiable: Unifiable[V]
  val backJoin: BackJoin[V]
  val widen: MyWiden[V]

  private def mapToString(m: Map[String,V]):String =
    val mapContent = m.map {
      case (key, value) => s"$key ↦ ${value.toString()}"
    }.mkString(", ")
    val finalVal = s"{$mapContent}"
    finalVal

  def getTrace: Unit =
    //println(trace)
    val lst = trace.head
    val remlst = trace.tail
    remlst.reverse.foreach { case y@(stmt, prestate) =>
      println(s"\u001b[1m  ${mapToString(prestate)}\u001b[0m")
      println(stmt.toString())
    }
    val (_,postState) = lst
    println(s"\u001b[1m  ${mapToString(postState)} \u001b[0m")


  import numericOps.*
  import failure.*
  import Exp.*
  import Stm.*

  private def binOps( op:Exp, e1: Exp, e2: Exp, res: V, st: ST[V]): ST[V] =
    val f1: V = eval(e1,st) // obtain the forward evaluation of e1 and e2
    val f2: V = eval(e2,st)
    //println(s"Operation:${op} , e1 = ${e1}:${f1} and e2 = ${e2}:${f2}, res = ${res} \n st = ${st}")

    val (r1, r2,_) = op match
      case Add(_,_) => invertOps.invAdd(f1, f2, res)
      case Sub(_,_) => invertOps.invSub(f1, f2, res)
      case Mul(_,_) => invertOps.invMul(f1, f2, res)
      case Div(_,_) => invertOps.invDiv(f1, f2, res)
      case Lt(_,_)  => invertOps.invLt(f1, f2, res)
      case Gt(_,_)  => invertOps.invGt(f1, f2, res)

    val st2 = backeval(e2, r2, st)
    backeval(e1, r1, st2)

  def eval(e: Exp,st:ST[V]): V = e match
    case NumLit(n) => lit(n)
    case Var(name) => st.get(name).getOrElse(invertOps.topVal)
    case Add(e1, e2) => add(eval(e1,st), eval(e2,st))
    case Sub(e1, e2) => sub(eval(e1,st), eval(e2,st))
    case Mul(e1, e2) => mul(eval(e1,st), eval(e2,st))
    case Div(e1, e2) => div(eval(e1,st), eval(e2,st))
    case Lt(e1, e2) =>  lt(eval(e1,st), eval(e2,st))
    case Gt(e1, e2) =>  gt(eval(e1,st), eval(e2,st))



  // The evaluation of expressions
  // In most cases we simply use the numericOps component.
  // For evaluating variabes we look them up in the store and call "fail" in case the variable in not initialized.
  def backeval(e: Exp, res:V,state:ST[V]): ST[V] = e match
    case NumLit(n) =>
      invertOps.invConst(n,res) match
        case true  => state
        case false => throw new RuntimeException(s"Error in the constant case: Can't unify $n and $res")
    case Var(x) => state.get(x) match
      case Some(v) => unifiable.canUnify(v,res) match
        case true  =>
          // println(s"Came here: $e :: $v :: res: $res, state: $state")
          state.updated(x,res)
        case false =>
          throw new RuntimeException(s"Unification Error:${v} with ${res}")
      case None      => state.updated(x,res)
    case Add(e1, e2) => binOps(e,e1,e2,res,state)
    case Sub(e1, e2) => binOps(e,e1,e2,res,state)
    case Mul(e1, e2) => binOps(e,e1,e2,res,state)
    case Div(e1, e2) => binOps(e,e1,e2,res,state)
    case Lt(e1, e2)  => binOps(e,e1,e2,res,state)
    case Gt(e1, e2)  => binOps(e,e1,e2,res,state)



  def backrun(s: Stm,state:ST[V]): ST[V] = s match
    case Assign(v, e) =>
      var newState: ST[V] = Map ()
      state.get(v) match
        case Some(res) => newState = backeval(e,res,state).updated(v,invertOps.topVal)
        case None      => newState = backeval(e,invertOps.topVal,state).updated(v,invertOps.topVal)
      trace = trace :+ (s"$v = $e", newState)  // Update trace for assignments
      newState

    case If(cond, thn, els) =>
      val stThen = backeval(cond,invertOps.trueVal,backrun(thn,state))
      els match
        case None => stThen
        case Some(someElse) =>
          val stElse = backeval(cond,invertOps.falseVal,backrun(someElse,state))
          backJoin.joinMaps(stThen,stElse)

    case While(cond, body) =>
      var currentState = backeval(cond, invertOps.falseVal, state)
      var fixedPointReached = false

      while (!fixedPointReached)
        val bodyState = backrun(body, currentState)
        val newState = backeval(cond, invertOps.trueVal, bodyState)
        fixedPointReached = (newState == currentState)
        currentState = newState

      currentState

//    case While(cond, body) =>
//      var currentState = backeval(cond, invertOps.falseVal, state)
//      var previousState: Option[ST[V]] = None
//      var iterationCount = 0
//      val wideningThreshold = 5
//
//      while previousState.isEmpty || !previousState.get.equals(currentState) do
//        val bodyState = backrun(body, currentState)
//        val newState = backeval(cond, invertOps.trueVal, bodyState)
//
//        currentState =
//          if iterationCount >= wideningThreshold then widen.mywiden(previousState.get, newState)
//          else newState
//
//        previousState = Some(currentState)
//        iterationCount += 1
//
//      currentState

    case Block(body) =>
      var currentState = state
      for (stmt <- body.reverse)
        currentState = backrun(stmt, currentState)
      currentState

  def runProg(s: Stm, state:ST[V]): ST[V] =
    trace = trace :+ ("Postcondition", state)
    backrun(s,state)


