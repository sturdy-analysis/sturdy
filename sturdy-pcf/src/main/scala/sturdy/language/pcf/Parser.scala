package sturdy.language.pcf

import cats.parse.{Numbers, Parser as P, Parser0 as P0}

import scala.collection.*
import scala.language.implicitConversions

/**
 *  Parser for TIP programs, adapted for cats-parse from https://github.com/cs-au-dk/TIP/blob/master/src/tip/parser/TipParser.scala
 */
object Parser:

  def parse(source: String): Program =
    program.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(s"Parse error at ${source.slice(err.failedAtOffset, err.failedAtOffset+10)}: $err")

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
    val KIF = "if"
    val KELSE = "else"
    val KLAM = "lam"
    val KREC = "rec"
    val KREAD = "read"
  }
  import LanguageKeywords.*

  val keywords = Set(
    KIF,
    KELSE,
    KLAM,
    KREC,
    KREAD
  )

  def keyword(s: String): P[Unit] =
    spaced(P.string(s) *> P.not(letterDigit))

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

  private val recExpression = P.defer(expression)

  val variable: P[Exp] = identifier.map(Exp.Var.apply)

  lazy val atom: P[Exp] =
    (keyword(KREAD) *> inParens(P.unit)).map(_ => Exp.Read) |
    ((variable | inParens(recExpression)) ~ inParens(recExpression)).backtrack.map(Exp.App.apply) |
    (keyword(KLAM) *> identifier ~ (op('.') *> recExpression)).map(Exp.Lam.apply) |
    (keyword(KREC) *> identifier ~ (op('.') *> recExpression)).map(Exp.Rec.apply) |
    (keyword(KIF) *> inParens(recExpression) ~ recExpression ~ (keyword(KELSE) *> recExpression))
      .map { case ((c, t), e) => Exp.If(c, t, e) } |
    spaced(Numbers.signedIntString.map(s => Exp.Num(s.toInt))) |
    inParens(recExpression) |
    variable

  lazy val term: P[Exp] =
    (atom ~ (
      (op('*') *> P.defer(term)).map(e2 => Exp.BinOpApp(BinOp.Mul, _, e2))
    ).?).map(maybeBinOp)

  lazy val operation: P[Exp] =
    (term ~ (
      (op('+') *> P.defer(operation)).map(e2 => Exp.BinOpApp(BinOp.Add, _, e2)) |
      (op('-') *> P.defer(operation)).map(e2 => Exp.BinOpApp(BinOp.Sub, _, e2))
    ).?).map(maybeBinOp)

  lazy val expression: P[Exp] =
    (operation ~ (
      (op('>') *> P.defer(expression)).map(e2 => Exp.BinOpApp(BinOp.Gt, _, e2)) |
      (op("==") *> P.defer(expression)).map(e2 => Exp.BinOpApp(BinOp.Eq, _, e2))
    ).?).map(maybeBinOp)

  val toplevel: P[(String, Exp)] =
    identifier ~ (op('=').backtrack *> expression)

  val program: P0[Program] =
    whitespaces0 *> toplevel.rep0.map(Program.apply) <* P.end
