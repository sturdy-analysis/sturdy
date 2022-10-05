package sturdy.language.tip

import sturdy.data.MayJoin
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.environment.Environment
import sturdy.effect.failure.Failure
import sturdy.effect.print.Print
import sturdy.effect.store.Store
import sturdy.effect.userinput.UserInput
import sturdy.language.tip.TipFailure.{UnboundAddr, UnboundVariable, UserError, VariableReferencesNotSupported}
import sturdy.values.booleans.BooleanBranching
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.IntegerOps
import sturdy.values.records.RecordOps
import sturdy.values.references.ReferenceOps
import sturdy.values.relational.{EqOps, OrderingOps}

trait TipSyntaxOps[E, S, F, P]:
  def unknownArg(p: String): E
  def call(fun: Function, args: Seq[E]): E

  def noop: S
  def whileloop(cond: E, body: S): S
  def print(e: E): S
  def block(s: Seq[S]): S
  def error(e: E): S

  def assignVar(lhs: String, e: E): S
  def assignRef(ref: E, e: E): S

  def function(name: String, params: Seq[String], locals: Seq[String], body: S, ret: E): F
  def program(funs: Seq[F]): P


trait GenericSyntactic[E, S, F, P, Addr, J[_] <: MayJoin[_]]:

  implicit def joinE: J[E]

  // value components
  val intOps: IntegerOps[Int, E]; import intOps.*
  val compareOps: OrderingOps[E, E]; import compareOps.*
  val eqOps: EqOps[E, E]; import eqOps.*
  val functionOps: FunctionOps[Function, Seq[E], E, E]; import functionOps.*
  val refOps: ReferenceOps[E, E]; import refOps.*
  val recOps: RecordOps[Field, E, E]; import recOps.*
  val branchOps: BooleanBranching[E, S]; import branchOps.*

  // effect components
  val env: Environment[String, E, J]
  val input: UserInput[E]
  val failure: Failure

  // syntax components
  val syntactic: TipSyntaxOps[E, S, F, P]

  def eval(e: Exp): E = e match
    case Exp.NumLit(n) => intOps.integerLit(n)
    case Exp.Input() => input.read()
    case Exp.Var(name) => env.lookup(name).getOrElse(failure(TipFailure.UnboundVariable, name))
    case Exp.Add(e1, e2) => add(eval(e1), eval(e2))
    case Exp.Sub(e1, e2) => sub(eval(e1), eval(e2))
    case Exp.Mul(e1, e2) => mul(eval(e1), eval(e2))
    case Exp.Div(e1, e2) => div(eval(e1), eval(e2))
    case Exp.Gt(e1, e2) => gt(eval(e1), eval(e2))
    case Exp.Eq(e1, e2) => equ(eval(e1), eval(e2))
    case Exp.Call(fun, args) =>
      invokeFun(eval(fun), args.map(eval(_)))(syntactic.call)
    case Exp.Alloc(e) =>
      refValue(eval(e))
    case Exp.VarRef(x) =>
      failure(VariableReferencesNotSupported, s"&$x")
    case Exp.Deref(e) =>
      refAddr(eval(e))
    case Exp.NullRef() =>
      nullValue
    case r@Exp.Record(fields) =>
      val fieldVals = fields.map(fe => Field(fe._1) -> eval(fe._2))
      val rec = makeRecord(fieldVals)
      refValue(rec)
    case Exp.FieldAccess(rec, field) =>
      val recVal = refAddr(eval(rec))
      lookupRecordField(recVal, Field(field))


  def run(s: Stm): S = s match
    case Stm.Assign(lhs: Assignable, e: Exp) =>
      val v = eval(e)
      assign(lhs, v)
    case Stm.If(cond: Exp, thn: Stm, els: Option[Stm]) =>
      boolBranch(eval(cond), run(thn), els.map(run(_)).getOrElse(syntactic.noop))
    case Stm.While(cond, body) =>
      syntactic.whileloop(eval(cond), run(body))
    case Stm.Block(body) =>
      syntactic.block(body.map(run(_)))
    case Stm.Output(e) =>
      syntactic.print(eval(e))
    case Stm.Error(e) =>
      syntactic.error(eval(e))

  def assign(lhs: Assignable, v: E): S = lhs match
    case Assignable.AVar(x) =>
      syntactic.assignVar(x, v)
    case Assignable.ADeref(e) =>
      val ref = refAddr(eval(e))
      syntactic.assignRef(ref, v)
    case Assignable.AField(recVar, field) =>
      val recV = eval(Exp.Var(recVar))
      val ref = refAddr(recV)
      val updated = updateRecordField(ref, Field(field), v)
      syntactic.assignRef(ref, updated)
    case Assignable.ADerefField(rec, field) =>
      val recV = eval(rec)
      val ref = refAddr(recV)
      val updated = updateRecordField(ref, Field(field), v)
      syntactic.assignRef(ref, updated)

  def call(fun: Function, args: Seq[E]): F =
    val locals: Iterable[(String, E)] =
      fun.params.zip(args) ++
      fun.locals.map(x => (x, integerLit(-1)))
    env.freshScoped {
      locals.foreach(env.bind)
      val body = run(fun.body)
      val ret = eval(fun.ret)
      syntactic.function(fun.name, fun.params, fun.locals, body, ret)
    }

  def execute(p: Program): P =
    val funs = p.funs.map { f =>
      val args = f.params.map(syntactic.unknownArg)
      call(f, args)
    }
    syntactic.program(funs)
