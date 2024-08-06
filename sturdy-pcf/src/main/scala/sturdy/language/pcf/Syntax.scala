package sturdy.language.pcf

import sturdy.util.Labeled
import sturdy.values.Finite

enum Exp extends Labeled:
  case Var(name: String)
  case Num(n: Int)
  case BinOpApp(op: BinOp, e1: Exp, e2: Exp)
  case Read
  case If(cond: Exp, thn: Exp, els: Exp)
  case Lam(x: String, body: Exp)
  case App(fun: Exp, arg: Exp)
  case Rec(f: String, body: Exp)

enum BinOp:
  case Add
  case Sub
  case Mul
  case Eq
  case Gt

case class Program(toplevel: Seq[(String, Exp)]):
  lazy val definitions: Map[String, Exp] = toplevel.toMap

given Finite[Exp] with {}