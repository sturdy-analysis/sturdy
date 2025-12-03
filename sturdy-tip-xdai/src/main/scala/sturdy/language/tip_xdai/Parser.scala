package sturdy.language.tip_xdai

import cats.parse.{Numbers, Parser as P, Parser0 as P0}

import scala.collection.*
import scala.language.implicitConversions
import sturdy.language.tip_xdai.core.*
import sturdy.language.tip_xdai.arithmetic.*
import sturdy.language.tip_xdai.record.{ADerefField, AField, FieldAccess, Record}
import sturdy.language.tip_xdai.references.{ADeref, Alloc, Deref, NullRef, VarRef}

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
    val KASSERT = "assert"
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
    KERROR,
    KASSERT
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
  private val recStatement = P.defer(statement)

  val variable: P[Exp] = identifier.map(Var.apply)

  val deref: P[Exp] =
    (op('*') *> P.defer(atom)).map(Deref.apply)

  lazy val atom: P[Exp] =
    ((variable | inParens(recExpression)) ~ inParens(list0(recExpression))).backtrack.map(Call.apply) |
    (keyword(KALLOC) *> recExpression.map(Alloc.apply)) |
    keyword(KINPUT).map(_ => Input()) |
    keyword(KNULL).map(_ => NullRef()) |
    (op('&') *> identifier).map(VarRef.apply) |
    deref |
    spaced(Numbers.signedIntString.map(s => NumLit(s.toInt))) |
    inParens(recExpression) |
    inBraces(list0((identifier <* op(':')) ~ recExpression)).map(Record.apply) |
    variable

  val access: P[Exp] =
    ((variable | deref | inParens(recExpression)) ~ (op('.') *> identifier).rep)
      .map { case (e, fields) => fields.foldLeft(e)(FieldAccess.apply) }
      .backtrack

  lazy val term: P[Exp] =
    access |
    (atom ~ (
      (op('*') *> P.defer(term)).map(e2 => Mul(_, e2)) |
      (op('/') *> P.defer(term)).map(e2 => Div(_, e2))
    ).?).map(maybeBinOp)

  lazy val operation: P[Exp] =
    (term ~ (
      (op('+') *> P.defer(operation)).map(e2 => Add(_, e2)) |
      (op('-') *> P.defer(operation)).map(e2 => Sub(_, e2))
    ).?).map(maybeBinOp)

  lazy val expression: P[Exp] =
    (operation ~ (
      (op('>') *> P.defer(expression)).map(e2 => Gt(_, e2)) |
      (op("==") *> P.defer(expression)).map(e2 => Eq(_, e2))
    ).?).map(maybeBinOp)

  val assignable: P[Assignable] =
    (op('*') *> atom).map(ADeref.apply) |
    (inParens(op('*') *> atom) ~ (op('.') *> identifier)).map(ADerefField.apply) |
    (identifier ~ (op('.') *> identifier).?)
      .map {
        case (x, None) => AVar(x)
        case (x, Some(y)) => AField(x, y)
      }

  lazy val statement: P[Stm] =
    (keyword(KIF) *> inParens(recExpression) ~ recStatement ~ (keyword(KELSE) *> recStatement).?)
      .map { case ((c, t), e) => If(c, t, e) } |
    (keyword(KWHILE) *> inParens(recExpression) ~ recStatement).map(While.apply) |
    inBraces(recStatement.rep0).map(Block.apply) |
    (keyword(KOUTPUT) *> recExpression <* semi).map(Output.apply) |
    (keyword(KASSERT) *> inParens(recExpression) <* semi).map(Assert.apply) |
    (keyword(KERROR) *> recExpression <* semi).map(Error.apply) |
    ((assignable <* op('=')) ~ recExpression <* semi).map(Assign.apply)

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
      Function(name, params, locals.flatten, Block(body), ret)
    }

  val program: P0[Program] =
    whitespaces0 *> function.rep0.map(Program.apply) <* P.end
