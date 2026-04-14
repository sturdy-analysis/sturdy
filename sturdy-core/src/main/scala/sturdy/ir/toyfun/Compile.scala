package sturdy.ir.toyfun

import sturdy.data.WidenFiniteKeyMap
import sturdy.effect.{RecurrentCall, TrySturdy}
import sturdy.effect.TrySturdy.*
import sturdy.ir.{Export, IR}
import sturdy.values
import sturdy.values.MaybeChanged.{Changed, Unchanged}
import sturdy.values.{Finite, Join, MaybeChanged, Widen}
import sturdy.values.booleans.IRBooleanOperator
import sturdy.values.integer.IRIntegerOperator
import sturdy.values.ordering.{IREqualityOperator, IROrderingOperator}

import scala.collection.immutable.{SortedMap, TreeMap}

class Compile(defs: Map[String, Def]) {

  private var path: Set[IR] = Set()
  private var env: SortedMap[String, IR] = SortedMap()

  // TODO: may need structural equality on IR values instead of reference equality
  case class State(env: SortedMap[String, IR]):

    def testIsLeq(other: State): Unit =
      if (this.env.keySet != other.env.keySet)
        throw new RuntimeException(s"Cannot compare states with different keys: ${env.keySet} vs ${other.env.keySet}")
      printIndented(s"TEST LEQ")
      printIndented(s"  $env")
      printIndented(s"  ${other.env}")
      val keys = this.env.keySet.toList
      keys.foreach { k =>
        val v1 = this.env(k)
        val v2 = other.env(k)
        if (v1 != v2)
          v1.testIsLeq(v2)
      }

    def inputWiden(other: State): MaybeChanged[State] = {
      if (this.env.keySet != other.env.keySet)
        throw new RuntimeException(s"Cannot widen states with different keys: ${env.keySet} vs ${other.env.keySet}")
      printIndented(s"WIDEN INPUT $env")
      printIndented(s"            ${other.env}")
      val keys = this.env.keySet.toList

      if (this.env.values.forall(!_.isInstanceOf[IR.Fixresult])) {
        val inits = other.env
        val steps = other.env.map((k,_) => (k,IR.Fixvar(k)))

        printIndented(s"Path condition: ${path.map(_.toString).mkString(" AND ")}")
        val loopWhile = path.map(IR.mkNot)
        printIndented(s"Loop while: $loopWhile")
        val fix: IR.Fix = IR.Fix(inits, steps, loopWhile.toList)
        printIndented(s"NEW FIX: $fix")
        val fixEnv = SortedMap() ++ keys.map(k => k -> IR.Fixresult(fix, k))
        val changed = other.env != fixEnv

        val newstate = State(fixEnv)
        other.testIsLeq(newstate)
        MaybeChanged(newstate, changed)
      } else {
        val previousFixes = this.env.collect { case (k, v: IR.Fixresult) => k -> v.fix }
        val inits = this.env.map((k, _) => (k, previousFixes(k).inits(k)))
        def rewriteFixResult(ir: IR): IR = ir.map {
          case IR.Fixresult(fix, name) if previousFixes.get(name).contains(fix) => IR.Fixvar(name)
          case ir => ir
        }
        val steps = other.env.map((k, v) => (k, rewriteFixResult(v)))

        printIndented(s"Path condition: ${path.map(_.toString).mkString(" AND ")}")
        path = path.map(rewriteFixResult)
        printIndented(s"Path rewritten: ${path.map(_.toString).mkString(" AND ")}")
        val loopWhile = path.map(IR.mkNot)
        val previousConds = previousFixes.flatMap(_._2.loopWhileAny).toSet
        val (loopCondBefore, loopCondNew) = loopWhile.partition(previousConds)
        val loopCondNewNot = loopCondNew.map(IR.mkNot)
        printIndented(s"Loop while from before $loopCondBefore")
        printIndented(s"         new condition $loopCondNew")
        printIndented(s"     new not condition $loopCondNewNot")

        val fix: IR.Fix = IR.Fix(inits, steps, loopCondBefore.toList ++ loopCondNewNot)
        val fixEnv = SortedMap() ++ keys.map(k => k -> IR.Fixresult(fix, k))
        if (this.env == fixEnv)
          Unchanged(other)
        else {
          printIndented(s"NEW FIX: $fix")
          val newstate = State(fixEnv)
          other.testIsLeq(newstate)
          Changed(newstate)
        }
      }
    }

  private var callstack: Map[String, State] = Map()
  private var recurrent: Set[String] = Set()
  private var cache: Map[String, IR] = Map()

  given Finite[String] with {}

  private val joinIR: Join[IR] = (v1: IR, v2: IR) =>
    if (v1 == v2 || v1.isInstanceOf[IR.Unknown])
      Unchanged(v1)
    else {
      printIndented(s"join $v1")
      printIndented(s"     $v2")
      Changed(IR.Unknown())
    }

  def compileFun(fun: String, argIRs: List[IR]): IR = {
    assert(path.isEmpty)
    assert(env.isEmpty)
    assert(callstack.isEmpty)
    assert(recurrent.isEmpty)
    assert(cache.isEmpty)
    compileCall(fun, argIRs)
  }

  private def compile(e: Exp): IR = e match {
    case Exp.Var(name) => env(name)
    case Exp.Num(n) => IR.Const(n)
    case Exp.Eq(e1, e2) => IR.Op(IREqualityOperator.EQ, compile(e1), compile(e2))
    case Exp.Lt(e1, e2) => IR.Op(IROrderingOperator.LT, compile(e1), compile(e2))
    case Exp.Le(e1, e2) => IR.Op(IROrderingOperator.LE, compile(e1), compile(e2))
    case Exp.Sub(e1, e2) => IR.Op(IRIntegerOperator.SUB, compile(e1), compile(e2))
    case Exp.Mul(e1, e2) => IR.Op(IRIntegerOperator.MUL, compile(e1), compile(e2))
    case Exp.Let(name, value, body) =>
      val valueIR = compile(value)
      env += (name -> valueIR)
      compile(body)
    case Exp.If(cond, thenBranch, elseBranch) =>
      val condIR = compile(cond)
      val oldPath = path

      path = oldPath + condIR
      val thenIR = TrySturdy(compile(thenBranch))
      path = oldPath + IR.mkNot(condIR)
      val elseIR = TrySturdy(compile(elseBranch))
      path = oldPath

      val joined = (thenIR.isBottom, elseIR.isBottom) match
        case (false, false) => IR.SelectSmart(condIR, thenIR.getOrThrow, elseIR.getOrThrow)
        case (false, true) => IR.SelectSmart(condIR, thenIR.getOrThrow, IR.Bot())
        case (true, false) => IR.SelectSmart(condIR, IR.Bot(), elseIR.getOrThrow)
        case (true, true) => IR.Bot()
      joined
    case Exp.Call(fun, args) =>
      val argIRs = args.map(compile)
      compileCall(fun, argIRs)
  }

  private def compileCall(fun: String, argIRs: Seq[IR]): IR =
    val d = defs.getOrElse(fun, throw new RuntimeException(s"Undefined function $fun"))
    if (d.params.length != argIRs.length)
      throw new RuntimeException(s"Function ${d.name} expects ${d.params.length} arguments, but got ${argIRs.length}")
    val oldEnv = env
    env = SortedMap() ++ d.params.zip(argIRs)
    val bodyIR = enterFun(fun, d.body)
    env = oldEnv
    bodyIR


  private var _indent: Int = 0
  private def printIndented(s: String): Unit = Predef.println(s"${"  " * _indent}$s")
  /** Outermost fixed-point computation, looping at the FIRST call of a function */
  private def enterFun(fun: String, body: Exp): IR = {
    callstack.get(fun) match
      case None =>
        printIndented(s"ENTER FIRST $fun with env $env")
        callstack = callstack + (fun -> State(env))
        val oldEnv = env
        _indent += 1
        val bodyIR = compile(body)
        _indent -= 1
        callstack = callstack - fun
        if (recurrent.contains(fun)) {
          recurrent = recurrent - fun
          val cached = cache.get(fun)
          printIndented(s"CO-RECURRENT $fun with cache $cached")
          val joined = cached.map(ir => joinIR(ir, bodyIR)).getOrElse(Changed(bodyIR))
          if (joined.hasChanged) {
            printIndented(s"REPEAT $fun:")
            printIndented(s"  join $cached")
            printIndented(s"  with $bodyIR")
            printIndented(s"     = $joined")
            cache = cache + (fun -> joined.get)
            env = oldEnv
            enterFun(fun, body)
          } else {
            printIndented(s"STABLE $fun with $bodyIR")
            bodyIR
          }
        } else {
          printIndented(s"NON-RECURRENT $fun with $bodyIR")
          bodyIR
        }
      case Some(state) =>
        val widened = state.inputWiden(State(env))
        widened match {
          case Unchanged(_) =>
            printIndented(s"RECURRENT $fun with env $env and previous state ${state.env}")
            recurrent = recurrent + fun
            cache.get(fun) match
              case None => throw RecurrentCall(fun)
              case Some(cached) => cached
          case Changed(a) =>
            printIndented(s"RE-ENTER UNSTABLE INPUT $fun: ${state.env} widened $env = $a")
            callstack = callstack + (fun -> a)
            env = a.env
            _indent += 1
            val bodyIR = compile(body)
            _indent -= 1
            callstack = callstack + (fun -> state)
            printIndented(s"EXIT UNSTABLE INPUT $fun with $bodyIR")
            bodyIR
        }
  }
}

object RunCompile extends App:
  val compiler = new Compile(Map("diff" -> natdiff, "fac" -> fac, "fac_main" -> fac_main))

//  private val diff_5_2 = compiler.compileFun("diff", List(IR.Const(5), IR.Const(2)))
//  println(Export.toGraphViz(diff_5_2))
//
//  private val diff_X_Y = compiler.compileFun("diff", List(IR.External("X"), IR.External("Y")))
//  println(Export.toGraphViz(diff_X_Y))

  private val fac_N = compiler.compileFun("fac", List(IR.External("N"), IR.Const(1)))
  fac_N.resolveFix()
  val fac_N_norm = fac_N.normalize
  val fac_N_norm_dedup = fac_N_norm.dedup
  println(Export.toGraphViz(fac_N_norm_dedup, _.nodeString))
