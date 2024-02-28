package sturdy.language.pcf

import sturdy.util.Labeled
import sturdy.values.{Structural, Finite}

import cats.Monoid


enum Expr:
  case Var(name: String)
  case Lam(param: String, body: Expr)
  case App(function: Expr, arguments: List[Expr])
  case Zero
  case True
  case False
  case isZero(expr: Expr)
  case Mult(e1: Expr, e2: Expr)
  case Succ(e: Expr)
  case Pred(e: Expr)
  case If(condition: Expr, thenBranch: Expr, elseBranch: Expr)
  case Let(bindings: List[(String, Expr)], body: Expr)
  case Apply(e: Expr)

  private def parenthesize(condition: Boolean, str: String): String =
    if (condition) s"($str)" else str

  override def toString: String = this match
    case Var(name) => name
    case Lam(params, body) => "λ" + params.mkString(" ") + ". " + body.toString
    case App(function, arguments) =>
      val argsStr = arguments.map(_.toString).mkString(" ")
      parenthesize(true, function.toString + " " + argsStr) // Simplified; actual precedence handling would be more complex
    case Zero  => "zero"
    case True  => "True"
    case False =>  "False"
    case isZero(e) => s"isZero(${e.toSring})"
    case Mult(e1, e2) => parenthesize(true, s"${e1.toString} * ${e2.toString}")
    case Succ(e) => parenthesize(true, "succ " + e.toString)
    case Pred(e) => parenthesize(true, "pred " + e.toString)
    case IfZero(condition, thenBranch, elseBranch) =>
      parenthesize(true, s"if ${condition.toString} ${thenBranch.toString} ${elseBranch.toString}")
    case Let(bindings, body) =>
      val bindingsStr = bindings.map { case (name, expr) => s"$name -> $expr" }.mkString(", ")
      parenthesize(true, "let " + bindingsStr + " in " + body.toString)
    case Apply(e) => parenthesize(true, e.toString)
