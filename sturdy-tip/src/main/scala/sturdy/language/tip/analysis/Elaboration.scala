package sturdy.language.tip.analysis

import sturdy.language.tip.*
import sturdy.language.tip.Stm.*
import sturdy.language.tip.abstractions.GradualLogger

trait ElaborationOps[T] {
  def getCheck(x: Exp.Var, uv: T): Exp
}

class Elaboration[T,V](gl: GradualLogger[T,V], eo: ElaborationOps[T]) {
  //var checkFuns = List
  def elaborate(p: Program): Program = {
    Program(p.funs.map(elaborateFunction))
  }
  def elaborateFunction(f: Function): Function = {
    Function(f.name, f.params, f.locals, elaborateStm(f.body), elaborateExp(f.ret))
  }

  def elaborateStm(s: Stm): Stm = s match{
    case Assign(lhs, e) => Assign(lhs, elaborateExp(e))
    case If(cond, thenn, els) => If(elaborateExp(cond), elaborateStm(thenn), els.map(elaborateStm))
    case Block(stms) => Block(stms.map(elaborateStm))
    case While(cond, body) => While(elaborateExp(cond), elaborateStm(body))
    case Output(e) => Output(elaborateExp(e))
    case Assert(e) => Assert(elaborateExp(e))
    case Error(e) => Error(elaborateExp(e))
  }
  def elaborateExp(exp: Exp): Exp = {
    val e = exp match
      case Exp.Add(e1, e2) => Exp.Add(elaborateExp(e1), elaborateExp(e2))
      case Exp.Sub(e1, e2) => Exp.Sub(elaborateExp(e1), elaborateExp(e2))
      case Exp.Mul(e1, e2) => Exp.Mul(elaborateExp(e1), elaborateExp(e2))
      case Exp.Div(e1, e2) => Exp.Div(elaborateExp(e1), elaborateExp(e2))
      case Exp.Gt(e1, e2) => Exp.Gt(elaborateExp(e1), elaborateExp(e2))
      case Exp.Eq(e1, e2) => Exp.Eq(elaborateExp(e1), elaborateExp(e2))
      case Exp.Call(fun, args) => Exp.Call(fun, args.map(elaborateExp))
      case Exp.Alloc(e) => Exp.Alloc(elaborateExp(e))
      case Exp.Deref(e) => Exp.Deref(elaborateExp(e))
      case Exp.Record(fields) => Exp.Record(fields.map{case (k,v) => (k, elaborateExp(v))})
      case Exp.FieldAccess(rec, field) => Exp.FieldAccess(elaborateExp(rec), field)
      case _ => exp
    gl.getCheck(exp.label).map { c =>
      Exp.Call(Exp.Var(s"check_${c.unsafe}"), List(e))
    }.getOrElse{e}
  }
}


/**


e ::= e e | ..
u ::= n | b | (\x.e)
v ::= (ev u :: T)

ev = <T>

f(<int>1::Int)

def f(x: ?):
    (<Bool>x :: Bool()


def f(x: B => B, v: I):
    (f :: ? => ?) (v :: ?)


 (Bool => Bool :: ? => ?) (Int :: ?)

Top.instanceOf[B] -> Bool // warning
Bool.instanceOf[?] ->  Top // ok!

(Bool => Top) Top -> Top //warning

---------------------

(Bool => Bool :: ? => ?) (Int :: ?)

Top.instanceOf[B] -> Bool // warning
Bool.instanceOf[?] ->  Bool // ok!

(Bool => Top) Int -> Top //runtime error

--------------------------

(Bool => Bool :: ? => ?) Int

Top.instanceOf[B] -> Bool // warning
Bool.instanceOf[?] ->  Bool // ok!

(Bool => Top) Int -> Top //runtime error


def foo(f: ? => ?, x: Int):
    f(x)
foo((x:Bool) => x, 10)
 **/