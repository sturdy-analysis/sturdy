package sturdy.ir.toyfun

import sturdy.data.{CombineEquiList, CombineEquiSeq, CombineFiniteKeyMap, WidenFiniteKeyMap}
import sturdy.effect.{RecurrentCall, TrySturdy}
import sturdy.effect.TrySturdy.*
import sturdy.ir.{Export, IR, IRInterpreterConcrete, IRValue}
import sturdy.values
import sturdy.values.MaybeChanged.{Changed, Unchanged}
import sturdy.values.{Finite, Join, MaybeChanged, Widen}
import sturdy.values.booleans.IRBooleanOperator
import sturdy.values.integer.IRIntegerOperator
import sturdy.values.ordering.{IREqualityOperator, IROrderingOperator}
import sturdy.values.booleans.ConcreteIntBools

class Compile(defs: Map[String, Def]) {

  enum Env:
    case Std(m: Map[String, IR])
    case Fix(fix: IR.Fix, names: Set[String])

    def getStdEnv: Map[String, IR] = this match
      case Std(m) => m
      case Fix(fix, names) => names.map(name => name -> IR.Fixresult(fix, name)).toMap

  private var path: Set[IR] = Set(IR.Const(1))
  private var env: Map[String, IR] = Map()

  // TODO: may need structural equality on IR values instead of reference equality
  case class State(env: Map[String, IR]):

    def testIsRelated(other: State, leq: Boolean): Unit =
      if (this.env.keySet != other.env.keySet)
        throw new RuntimeException(s"Cannot compare states with different keys: ${env.keySet} vs ${other.env.keySet}")
      printIndented(s"TEST ${if (leq) "LEQ" else "EQ"}:")
      printIndented(s"  $env")
      printIndented(s"  ${other.env}")
      val keys = this.env.keySet.toList
      keys.foreach { k =>
        val v1 = this.env(k)
        val v2 = other.env(k)
        if (v1 != v2)
          v1.testIsRelated(v2, IR.mkAnd(path.filter(!_.isRecursive).toList), leq)
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
        val cond = IR.Op(IROrderingOperator.LT, IR.Const(0), IR.Op(IRIntegerOperator.SUB, IR.External("N"), IR.Const(1)))
        val fix: IR.Fix = IR.Fix(inits, steps, List(cond))
        printIndented(s"NEW FIX: $fix")
        val fixEnv = Map() ++ keys.map(k => k -> IR.Fixresult(fix, k))

        val changed = other.env != fixEnv

        val newstate = State(fixEnv)
        other.testIsRelated(newstate, leq = false)
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
        val loopWhile = path.map(rewriteFixResult).filter(_.isRecursive)
        printIndented(s"Loop while new condition $loopWhile")

        val fix: IR.Fix = IR.Fix(inits, steps, loopWhile.toList)
        val fixEnv = Map() ++ keys.map(k => k -> IR.Fixresult(fix, k))
        if (this.env == fixEnv)
          Unchanged(other)
        else {
          printIndented(s"NEW FIX: $fix")
          val newstate = State(fixEnv)
          other.testIsRelated(newstate, leq = false)
          Changed(newstate)
        }
      }
    }

  private var callstack: Map[String, State] = Map()
  private var recurrent: Set[String] = Set()
  private var cache: Map[String, IR] = Map()

  given Finite[String] with {}

  private implicit def joinIRSeq: Join[Seq[IR]] = new CombineEquiSeq()
  private implicit def joinIRList: Join[List[IR]] = new CombineEquiList()
  private implicit def joinIRMap: Join[Map[String, IR]] = new CombineFiniteKeyMap()
  private implicit def joinIR: Join[IR] = (v1: IR, v2: IR) =>
    if (v1 == v2)
      Unchanged(v1)
    else (v1, v2) match {
      case (IR.Bot(), v2) => MaybeChanged(v2, v1)
      case (v1, IR.Bot()) => Unchanged(v1)
      case (IR.Unknown(), v2) => Unchanged(IR.Unknown())
      case (v1, IR.Unknown()) => MaybeChanged(IR.Unknown(), v1)
      case (IR.Op(op1, args1), IR.Op(op2, args2)) if op1 == op2 && args1.size == args2.size =>
        val args = Join(args1, args2)
        MaybeChanged(IR.Op(op1, args.get), args.hasChanged)
      case (IR.Select(cond1, left1, right1), IR.Select(cond2, left2, right2)) if cond1 == cond2 =>
        val left = Join(left1, left2)
        val right = Join(right1, right2)
        MaybeChanged(IR.Select(cond1, left.get, right.get), left.hasChanged || right.hasChanged)
      case (IR.Assert(cond1, data1), IR.Assert(cond2, data2)) =>
        val cond = Join(cond1, cond2)
        val data = Join(data1, data2)
        MaybeChanged(IR.Assert(cond.get, data.get), cond.hasChanged || data.hasChanged)
      case (IR.Fix(inits1, steps1, conds1), IR.Fix(inits2, steps2, conds2)) if inits1.keySet == inits2.keySet =>
        val inits = Join(inits1, inits2)
        val steps = Join(steps1, steps2)
        val conds = Join(conds1, conds2)
        MaybeChanged(IR.Fix(inits.get, steps.get, conds.get), inits.hasChanged || steps.hasChanged || conds.hasChanged)
      case (IR.Fixresult(fix1, name1), IR.Fixresult(fix2, name2)) if name1 == name2 =>
        val fix = joinIR(fix1, fix2)
        MaybeChanged(IR.Fixresult(fix.get.asInstanceOf[IR.Fix], name1), fix.hasChanged)
      case (v1, v2) => Changed(IR.Join(v1, v2))

//      case IR.Join(left, right) => f(IR.Join(left.map(f), right.map(f)))
//      case IR.Fix(inits, steps, cond) => f(IR.Fix(inits.map((k, v) => k -> v.map(f)), steps.map((k, v) => k -> v.map(f)), cond.map(f)))
//      case IR.Fixvar(name) => f(this)
//      case IR.Fixresult(fix, name) => f(IR.Fixresult(fix.map(f).asInstanceOf[IR.Fix], name))
//      case IR.Feedback(inits, cond, steps) => throw new UnsupportedOperationException()
//      case IR.FeedbackAsk(index, feedback) => throw new UnsupportedOperationException()
    }

  def compileFun(fun: String, argIRs: List[IR]): IR = {
    assert(path == Set(IR.Const(1)))
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
    case Exp.Add(e1, e2) => IR.Op(IRIntegerOperator.ADD, compile(e1), compile(e2))
    case Exp.Sub(e1, e2) => IR.Op(IRIntegerOperator.SUB, compile(e1), compile(e2))
    case Exp.Mul(e1, e2) => IR.Op(IRIntegerOperator.MUL, compile(e1), compile(e2))
    case Exp.If(cond, thenBranch, elseBranch) =>
      val condIR = compile(cond)
      val normalize = condIR.normalize
      val maybeBoolean = normalize.tryDecide(path)
      val oldPath = path
      maybeBoolean match {
        case Some(true) =>
          path = oldPath + condIR
          try compile(thenBranch)
          finally path = oldPath
        case Some(false) =>
          path = oldPath + IR.mkNot(condIR)
          try compile(elseBranch)
          finally path = oldPath
        case None =>
          println(s"Cannot decide condition $condIR under path ${path.map(_.toString).mkString(" AND ")}")
          path = oldPath + condIR
          val thenIR = TrySturdy(compile(thenBranch))
          path = oldPath + IR.mkNot(condIR)
          val elseIR = TrySturdy(compile(elseBranch))
          path = oldPath

          val joined = (thenIR.isBottom, elseIR.isBottom) match
            case (false, false) => IR.Select(condIR, thenIR.getOrThrow, elseIR.getOrThrow)
            case (false, true) => IR.Select(condIR, thenIR.getOrThrow, IR.Bot())
            case (true, false) => IR.Select(condIR, IR.Bot(), elseIR.getOrThrow)
            case (true, true) => IR.Bot()
          joined
      }
    case Exp.Let(name, value, body) =>
      val valueIR = compile(value)
      env += (name -> valueIR)
      compile(body)
    case Exp.Call(fun, args) =>
      val argIRs = args.map(compile)
      compileCall(fun, argIRs)
  }

  private def compileCall(fun: String, argIRs: Seq[IR]): IR =
    val d = defs.getOrElse(fun, throw new RuntimeException(s"Undefined function $fun"))
    if (d.params.length != argIRs.length)
      throw new RuntimeException(s"Function ${d.name} expects ${d.params.length} arguments, but got ${argIRs.length}")
    val oldEnv = env
    env = Map() ++ d.params.zip(argIRs)
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
            printIndented(s"  join ${cached.getOrElse(None)}")
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
        printIndented(s"WIDEN INPUT for $fun")
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
  val compiler = new Compile(Map("diff" -> natdiff, "fac" -> fac, "fib" -> fib, "mul" -> mul, "fac_mul" -> fac_mul))
  val interpreter = new IRInterpreterConcrete[Int](Map("N" -> IRValue(10)), () => throw NotImplementedError())

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

  println(interpreter.run(fac_N_norm_dedup))

  private val fib_N = compiler.compileFun("fib", List(IR.External("N"), IR.Const(0), IR.Const(1)))
  fib_N.resolveFix()
  val fib_N_norm = fib_N.normalize
  val fib_N_norm_dedup = fib_N_norm.dedup
  println(Export.toGraphViz(fib_N_norm_dedup, _.nodeString))

  println(interpreter.run(fib_N_norm_dedup))

  private val fac_mul_N = compiler.compileFun("fac_mul", List(IR.External("N"), IR.Const(0)))
  fac_mul_N.resolveFix()
  val fac_mul_norm = fac_mul_N.normalize
  val fac_mul_norm_dedup = fac_mul_norm.dedup
  println(Export.toGraphViz(fac_mul_norm_dedup, _.nodeString))

  println(interpreter.run(fac_mul_norm_dedup))
