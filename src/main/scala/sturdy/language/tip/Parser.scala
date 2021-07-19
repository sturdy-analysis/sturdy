package sturdy.language.tip

import cats.parse.{Parser0 => P0, Parser => P, Numbers}

import scala.collection._
import scala.language.implicitConversions

/**
 *  Parser for TIP programs, adapted for cats-parse from https://github.com/cs-au-dk/TIP/blob/master/src/tip/parser/TipParser.scala
 */
object Parser:

  /* LEXICAL */

  val lineComment: P[Unit] = P.string("//") *> P.charsWhile0(c => c != '\n' && c != '\r').void
  val blockComment: P[Unit] = P.string("/*") *> P.recursive[Unit](rec =>
    P.product01(P.charsWhile0(c => c != '*').void, P.string("*/") | P.char('*') ~ rec).void
  )
  val comment: P[Unit] = lineComment | blockComment
  val whitespace: P[Unit] = (P.charIn(" \t\r\n").void | comment)
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def spaced[A](p: P[A]): P[A] =
    p <* whitespaces0

  object LanguageKeywords {
    val KALLOC = "alloc"
    val KINPUT = "input"
    val KWHILE = "while"
    val KIF = "if"
    val KELSE = "else"
    val KVAR = "var"
    val KRETURN = "return"
    val KNULL = "null"
    val KOUTPUT = "output"
    val KERROR = "error"
  }
  import LanguageKeywords.*

  val keywords = Set(
    KALLOC,
    KINPUT,
    KWHILE,
    KIF,
    KELSE,
    KVAR,
    KRETURN,
    KNULL,
    KOUTPUT,
    KERROR
  )

  def keyword(s: String): P[Unit] =
    spaced(P.string(s))

  val letter: P[Unit] = P.ignoreCaseCharIn('a' to 'z').void
  val digit: P[Unit] = P.charIn('0' to '9').void
  val letterDigit: P[Unit] = P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).void

  val id: P[String] =
    (letter ~ letterDigit.rep0)
      .string
      .filter(s => !keywords.contains(s)).backtrack

  val identifier: P[String] =
    spaced(id)

  def inParens[A](p: P0[A]): P[A] =
    op('(') *> p <* op(')')

  def inBraces[A](p: P0[A]): P[A] =
    op('{') *> p <* op('}')

  def list0[A](p: P[A]): P0[List[A]] =
    p.repSep0(op(','))

  def list[A](p: P[A]): P[List[A]] =
    p.repSep(op(',')).map(_.toList)

  val semi: P[Unit] =
    op(';')

  def op(c: Char): P[Unit] =
    spaced(P.char(c))

  def op(s: String): P[Unit] =
    spaced(P.string(s))

  /* STRUCTURAL */

  private val maybeBinOp: ((Exp, Option[Exp => Exp])) => Exp = {
    case (e1, None) => e1
    case (e1, Some(f)) => f(e1)
  }

  private val recAtom: P[Exp] = P.defer(atom)
  private val recExpression = P.defer(expression)
  private val recStatement = P.defer(statement)


  val variable: P[Exp] = identifier.map(Exp.Var.apply)

  val deref: P[Exp] =
    (op('*') *> recAtom).map(Exp.Deref.apply)

  lazy val atom: P[Exp] =
    ((variable | inParens(recExpression)) ~ inParens(list0(recExpression))).backtrack.map(Exp.Call.apply) |
    (keyword(KALLOC) *> recExpression.map(Exp.Alloc.apply)) |
    keyword(KINPUT).map(_ => Exp.Input()) |
    keyword(KNULL).map(_ => Exp.NullRef()) |
    (op('&') *> identifier).map(Exp.VarRef.apply) |
    deref |
    spaced(Numbers.signedIntString.map(s => Exp.NumLit(s.toInt))) |
    inParens(recExpression) |
    inBraces(list0((identifier <* op(':')) ~ recExpression)).map(Exp.Record.apply) |
    variable

  val access: P[Exp] =
    ((variable | deref | inParens(recExpression)) ~ (op('.') *> identifier).rep)
      .map { case (e, fields) => fields.foldLeft(e)(Exp.FieldAccess.apply) }
      .backtrack

  val term: P[Exp] =
    access |
    (atom ~ (
      (op('*') *> recExpression).map(e2 => Exp.Mul(_, e2)) |
      (op('/') *> recExpression).map(e2 => Exp.Div(_, e2))
    ).?).map(maybeBinOp)

  val operation: P[Exp] =
    (term ~ (
      (op('+') *> recExpression).map(e2 => Exp.Add(_, e2)) |
      (op('-') *> recExpression).map(e2 => Exp.Sub(_, e2))
    ).?).map(maybeBinOp)

  lazy val expression: P[Exp] =
    (operation ~ (
      (op('>') *> operation).map(e2 => Exp.Gt(_, e2)) |
      (op("==") *> operation).map(e2 => Exp.Eq(_, e2))
    ).?).map(maybeBinOp)

  val assignable: P[Assignable] =
    (op('*') *> recExpression).map(Assignable.ADeref.apply) |
    (inParens(op('*') *> recExpression) ~ (op('.') *> identifier)).map(Assignable.ADerefField.apply) |
    (identifier ~ (op('.') *> identifier).?)
      .map {
        case (x, None) => Assignable.AVar(x)
        case (x, Some(y)) => Assignable.AField(x, y)
      }

  lazy val statement: P[Stm] =
    (keyword(KIF) *> inParens(recExpression) ~ recStatement ~ (keyword(KELSE) *> recStatement).?)
      .map { case ((c, t), e) => Stm.If(c, t, e) } |
    (keyword(KWHILE) *> inParens(recExpression) ~ recStatement).map(Stm.While.apply) |
    inBraces(recStatement.rep0).map(Stm.Block.apply) |
    (keyword(KOUTPUT) *> recExpression <* semi).map(Stm.Output.apply) |
    (keyword(KERROR) *> recExpression <* semi).map(Stm.Error.apply) |
    ((assignable <* op('=')) ~ recExpression <* semi).map(Stm.Assign.apply)

  val varDecl: P[List[String]] =
    keyword(KVAR) *> list(identifier) <* semi

  val function: P[Function] =
    (identifier ~ inParens(list0(identifier)) ~
      inBraces(
        varDecl.rep0 ~
        statement.rep0 ~
        (keyword(KRETURN) *> expression <* semi)
      )
    ).map { case ((name, params), ((locals, body), ret)) =>
      Function(name, params, locals.flatten, Stm.Block(body), ret)
    }

  val program: P0[Program] =
    whitespaces0 *> function.rep0.map(Program.apply) <* P.end


//trait Comments { this: Parser =>
//
//  var lastBreaks = mutable.MutableList[Int](0)
//
//  implicit def offset2Loc(i: Int): Loc = {
//    val idx = lastBreaks.lastIndexWhere(brk => brk <= i)
//    Loc(idx + 1, i - lastBreaks(idx) + 1)
//  }
//
//  def NewLine: Rule0 = rule {
//    (str("\r\n") | str("\n\r") | str("\r") | str("\n")) ~> { () =>
//      lastBreaks += cursor; ()
//    }
//  }
//
//  def NonClosing: Rule0 = rule {
//    zeroOrMore("*" ~ !"/" | noneOf("*\n\r") | NewLine)
//  }
//
//  def BlockComment: Rule0 = rule("/*" ~ (BlockComment | NonClosing) ~ "*/")
//
//  def Comment: Rule0 = rule(BlockComment | "//" ~ zeroOrMore(noneOf("\n\r")) ~ NewLine)
//}
