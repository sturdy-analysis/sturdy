package sturdy.language.tutorial

import cats.parse.{Parser0 => P0, Parser => P, Numbers}

object Parser:

  def parse(source: String): Stm =
    program.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(s"Parse error at ${source.slice(err.failedAtOffset, err.failedAtOffset+10)}: $err")

  val whitespace: P[Unit] = (P.charIn(" \t\r\n").void)
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def spaced[A](p: P[A]): P[A] =
    p <* whitespaces0

  object LanguageKeywords {
    val KWHILE = "while"
    val KIF = "if"
    val KELSE = "else"
  }
  import LanguageKeywords.*

  val keywords = Set(
    KWHILE,
    KIF,
    KELSE
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
    
  val semi: P[Unit] =
    op(';')

  def op(c: Char): P[Unit] =
    spaced(P.char(c))

  def op(s: String): P[Unit] =
    spaced(P.string(s))

  private val maybeBinOp: ((Exp, Option[Exp => Exp])) => Exp = {
    case (e1, None) => e1
    case (e1, Some(f)) => f(e1)
  }

  private val recExpression = P.defer(expression)
  private val recStatement = P.defer(statement)

  val variable: P[Exp] = identifier.map(Exp.Var.apply)

  lazy val atom: P[Exp] =
    spaced(Numbers.signedIntString.map(s => Exp.NumLit(s.toInt))) | //number literals
    inParens(recExpression) | //expression in parens
    variable //variable

  lazy val term: P[Exp] =
    (atom ~ (
      ( op('*') *> P.defer(term)).map(e2 => Exp.Mul(_, e2)) |
      ( op('/') *> P.defer(term)).map(e2 => Exp.Div(_, e2))
    ).?).map(maybeBinOp)

  lazy val operation: P[Exp] =
    (term ~ (
      (op('+') *> P.defer(operation)).map(e2 => Exp.Add(_, e2)) |
      (op('-') *> P.defer(operation)).map(e2 => Exp.Sub(_, e2))
      ).?).map(maybeBinOp)

  lazy val expression: P[Exp] =
    (operation ~ (
      (op('<') *> P.defer(expression)).map(e2 => Exp.Lt(_, e2))
    ).?).map(maybeBinOp)

  lazy val statement: P[Stm] =
    (keyword(KIF) *> inParens(recExpression) ~ recStatement ~ (keyword(KELSE) *> recStatement).?)
      .map { case ((c, t), e) => Stm.If(c, t, e) } |
    (keyword(KWHILE) *> inParens(recExpression) ~ recStatement).map(Stm.While.apply) |
    inBraces(recStatement.rep0).map(Stm.Block.apply) |
    ((identifier <* op(":=")) ~ recExpression <* semi).map(Stm.Assign.apply)

  val program: P0[Stm] =
    recStatement.rep0.map(Stm.Block.apply)