/*
package sturdy.language.minijava

import sturdy.effect.noJoin
import sturdy.effect.allocation.Allocation
import sturdy.effect.branching.BoolBranching
import sturdy.effect.callframe.CCallFrame
import sturdy.effect.environment.Environment
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.print.Print
import sturdy.effect.store.Store
import sturdy.effect.userinput.UserInput
import sturdy.util.Label
import sturdy.values.booleans.BooleanOps
import sturdy.values.ints.IntOps
import sturdy.values.functions.FunctionOps
import sturdy.values.records.RecordOps
import sturdy.values.relational.{EqOps, CompareOps}
import sturdy.fix
import sturdy.values.*
import sturdy.values.unit
import sturdy.values.references.ReferenceOps

import scala.collection.mutable.ListBuffer

object GenericInterpreter:
  type GenericEffects[V, Addr] =
    BoolBranching[V] with
    CCallFrame[Unit, String, Addr] with
    Store[Addr, V] with
    Allocation[Addr, AllocationSite] with
    Print[V] with
    UserInput[V] with
    Failure

  enum AllocationSite:
    case Alloc(e: Exp.Alloc)

    // Kann man genau so übernehmen? Auch für Klassen?
    case ParamBinding(fun: Function, name: String)
    case LocalBinding(fun: Function, name: String)

    // No records
    //case Record(r: Exp.Record)

  case object UnboundVariable extends FailureKind
  case object UnboundAddr extends FailureKind
  case object UserError extends FailureKind

  enum FixIn:
    case Eval(e: Exp)
    case Run(s: Stm)
    case EnterFunction(f: Function)
    override def toString: String = this match
      case Eval(e) => s"eval $e"
      case Run(s) => s"run $s"
      case EnterFunction(fun) => s"enter ${fun.name}"

  enum FixOut[V]:
    case Eval(v: V)
    case Run()
    case ExitFunction(ret: V)

  given joinFixOut[V](using j: JoinValue[V]): JoinValue[FixOut[V]] with
    override def joinValues(out1: FixOut[V], out2: FixOut[V]): FixOut[V] = (out1, out2) match
      case (FixOut.Eval(v1), FixOut.Eval(v2)) => FixOut.Eval(j.joinValues(v1, v2))
      case (FixOut.Run(), FixOut.Run()) => FixOut.Run()
      case (FixOut.ExitFunction(v1), FixOut.ExitFunction(v2)) => FixOut.ExitFunction(j.joinValues(v1, v2))
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")

  given finiteFixOut[V](using f: Finite[V]): Finite[FixOut[V]] with {}

  given widenFixOut[V](using w: fix.Widening[V]): fix.Widening[FixOut[V]] with
    override def widen(out1: FixOut[V], out2: FixOut[V]): FixOut[V] = (out1, out2) match
      case (FixOut.Eval(v1), FixOut.Eval(v2)) => FixOut.Eval(w.widen(v1, v2))
      case (FixOut.Run(), FixOut.Run()) => FixOut.Run()
      case (FixOut.ExitFunction(v1), FixOut.ExitFunction(v2)) => FixOut.ExitFunction(w.widen(v1, v2))
      case _ => throw new IllegalArgumentException(s"Cannot join outputs of different kind, $out1 and $out2")

  type GenericPhi[V] = fix.Combinator[FixIn, FixOut[V]]

trait TypeOps[V]:
  def isInteger(v: V): V
  def isBoolean(v: V): V
  // ist das korrekt?
  def isArray(v: V): V
  // Klassen sind ja auch Typen oder?
  def isIdentifier(v: V): V

// noch irgendwas mit lifted ops??
trait ArrayOps[I, V, A]:
  def allocArray(v: I): A
  def accessArray(ar: A, ix: I): V
  def arrayLength(ar: A): I

given concreteArrayOps[V]: ArrayOps[Int, V, Array[V]] with
  // Funktioniert das so mit Scala Arrays?
  def allocArray(v: Int): Array[V] = new Array[V](v)
  def accessArray(v1: Array[V], v2: Int): V = v1(v2)
  def arrayLength(v: Array[Int]): Int = v.length

import GenericInterpreter.*

trait GenericInterpreter[V, Addr, Effects <: GenericEffects[V, Addr]]
(using val effectOps: Effects)
(using intOps: IntOps[V], compareOps: CompareOps[V, V], eqOps: EqOps[V, V], functionOps: FunctionOps[Function, V, V, V], refOps: ReferenceOps[Addr, V], recOps: RecordOps[String, V, V], booleanOps: BooleanOps[V], arrayOps: ArrayOps[Int,V,V])
(using effectOps.StoreJoin[V], effectOps.StoreJoinComp, effectOps.StoreJoin[Unit], effectOps.BoolBranchJoin[Unit]):

  import intOps._
  import compareOps._
  import eqOps._
  import effectOps._
  import functionOps._
  import recOps._
  import refOps._
  import booleanOps._
  import arrayOps._

  val phi: GenericPhi[V]

  //private var functions: Map[String, Function] = Map()
  private var classTable: Map[String, classDeclaration] = Map()
  private var classVars: Map[Map[String, V]] = Map()

  private lazy val fixed = fix.Fixpoint { (rec: FixIn => FixOut[V]) =>
    def eval(e: Exp): V = rec(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
    def run(s: Stm): Unit = rec(FixIn.Run(s)) match {case FixOut.Run() => (); case v => throw new IllegalStateException()}

    def eval_open(e: Exp): V = e match {
      case Exp.NumLit(n) => intLit(n)
      case Exp.Var(x) => functions.get(x) match
        case Some(fun) => funValue(fun)
        case None =>
          val addr = getLocal(x).orElse(fail(UnboundVariable, x))
          read(addr).orElse(fail(UnboundAddr, s"$addr for variable $x"))
      case Exp.Add(e1, e2) => add(eval(e1), eval(e2))
      case Exp.Sub(e1, e2) => sub(eval(e1), eval(e2))
      case Exp.Mul(e1, e2) => mul(eval(e1), eval(e2))
      case Exp.Div(e1, e2) => div(eval(e1), eval(e2))
      case Exp.Gt(e1, e2) => gt(eval(e1), eval(e2))
      case Exp.Eq(e1, e2) =>
        val v1 = eval(e1)
        val v2 = eval(e2)
        equ(v1, v2)

      case Exp.Call(obj, name, args) =>
        //Eval identifier des class object
        val o = eval(obj)
        //Greife auf die Felder des class object zu
        val classObj = classVars.get(o)
        //Im Feld "class" steht der name der class
        val className = classObj.get("class")
        //Schaue die class in der class table nach
        val actualClass: classDeclaration = classTable.get(className)
        //Finde die zugehörige function der class
        val fun: Function = actualClass.funs.find(_.name == name)
        //locals binden? keine ahnung
        invokeFun(eval(fun), args.map(eval))(call)

      case a@Exp.Alloc(name) =>
        //Finde die class die zum identifer passt
        val actualClass: classDeclaration = classTable.get(name)
        //Wie setzt man eigentlich überhaupt lokale variablen?
        val locals = actualClass.locals ++ "class"
        //füge die neue class variable zur liste aller class variablen hinzu
        classVars(name) = locals.toMap


      case Exp.AllocArray(e) => allocArray(eval(e))

      case Exp.AccessArray(e1, e2) => accessArray(eval(e1), eval(e2))

      case Exp.ArrayLength(e) => arrayLength(eval(e))


      case Exp.BoolLit(b) => boolLit(b)

      case Not(e) => not(eval(e))

      case And(e1, e2) => and(eval(e1), eval(e2))

    }
    // Statements sind praktisch identisch
    def run_open(s: Stm): Unit = s match
      case Stm.Assign(lhs: Assignable, e: Exp) =>
        val v = eval(e)
        assign(lhs, v)
      case Stm.If(cond: Exp, thn: Stm, els: Option[Stm]) =>
        boolBranch(eval(cond), run(thn), els.map(run).getOrElse(()))
      case Stm.While(cond, body) =>
        boolBranch(eval(cond), {run(body); run(s)}, {})
      case Stm.Block(body) =>
        body.foreach(run)
      case Stm.Output(e) =>
        print(eval(e))
      case Stm.Error(e) =>
        fail(UserError, eval(e).toString)

    def assign(lhs: Assignable, v: V): Unit = lhs match
      case Assignable.AVar(x) =>
        val addr = getLocal(x).orElse(fail(UnboundVariable, x))
        write(addr, v)
      //Auch hier wie arrayOps? keine ahnung
      case Assignable.AArray(name, e) =>
        ???


    //Funktions noch editieren, local variablen irgendwie binden?
    def call(fun: Function, args: Seq[V]): V =
      var locals: Map[String, Addr] = Map()
      val paramAddrs = fun.params.map { name =>
        val addr = alloc(AllocationSite.ParamBinding(fun, name))
        locals += name -> addr
        addr
      }
      val localsAddrs = fun.locals.map { name =>
        val addr = alloc(AllocationSite.LocalBinding(fun, name))
        locals += name -> addr
        addr
      }
      inNewFrame((), locals) {
        paramAddrs.zip(args).map(write)
        try
          rec(FixIn.EnterFunction(fun)) match
            case FixOut.ExitFunction(v) => v
            case _ => throw new IllegalStateException()
        finally {
          paramAddrs.foreach(free)
          localsAddrs.foreach(free)
        }
      }

    phi {
      case FixIn.Eval(e) => FixOut.Eval(eval_open(e))
      case FixIn.Run(s) => {run_open(s); FixOut.Run()}
      case FixIn.EnterFunction(f) => FixOut.ExitFunction({run(f.body); eval(f.ret)})
    }
  }

  def eval(e: Exp): V = fixed(FixIn.Eval(e)) match {case FixOut.Eval(v) => v; case _ => throw new IllegalStateException()}
  def run(s: Stm): Unit = fixed(FixIn.Run(s)) match {case FixOut.Run() => (); case _ => throw new IllegalStateException()}

  def execute(p: Program): V =
<<<<<<< HEAD
    functions = p.funs.map(f => f.name -> f).toMap
    val main = functions("main")
    val args = main.params.map(_ => Exp.Input())
    eval(Exp.Call(Exp.Var("main"), args))
*/
=======
    classTable = p.classes.map(c => c.name -> c).toMap
    val main = p.main
    //Wie geht man mit den main class params um?
    //val args = main.params.map
    run(main.body)
>>>>>>> d62f129a6c5feb1e8d13bddb7bfee3c838bc4e4c
