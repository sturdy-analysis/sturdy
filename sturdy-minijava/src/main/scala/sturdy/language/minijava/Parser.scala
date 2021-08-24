package sturdy.language.minijava

import cats.parse.{Numbers, Parser as P, Parser0 as P0}

import scala.collection.*
import scala.language.implicitConversions


package object Parser {

  def parse(source: String): Program =
    program.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(err.toString)


   //LEXICAL
  // comments
  val lineComment: P[Unit] = P.string("//") *> P.charsWhile0(c => c != '\n' && c != '\r').void
  val blockComment: P[Unit] = P.string("") *> P.recursive[Unit](rec =>
    P.product01(P.charsWhile0(c => c != '*').void, P.string("") | P.char('*') ~ rec).void
  )
  val comment: P[Unit] = lineComment | blockComment
  val whitespace: P[Unit] = (P.charIn(" \t\r\n").void | comment)
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def spaced[A](p: P[A]): P[A] =
    p <* whitespaces0

  object LanguageKeywords {
    val KALLOC = "new"
    val KWHILE = "while"
    val KIF = "if"
    val KELSE = "else"
    val KVAR = "var"
    val KRETURN = "return"
    val KNULL = "null"
    val KVOID = "void"
    val KBOOLEAN = "boolean"
    val KINT = "int"
    val KCLASS = "class"
    val KEXT = "extends"
    val KSTATIC = "static"
    val KPRIVATE = "private"
    val KPUBLIC = "public"
    val KTHIS = "this"
    val KPRINTLINE = "System.out.println"
  }
  import LanguageKeywords.*

  val keywords = Set(
    KALLOC,
    KWHILE,
    KIF,
    KELSE,
    KVAR,
    KRETURN,
    KNULL,
    KNULL,
    KVOID,
    KBOOLEAN,
    KINT,
    KCLASS,
    KEXT,
    KSTATIC,
    KPRIVATE,
    KPUBLIC,
    KTHIS,
    KPRINTLINE
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


  //Klammern, Kommas und Semikolons werden seperat geparst

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


  lazy val atom: P[Exp] =
    ((variable | inParens(recExpression)) ~ inParens(list0(recExpression))).backtrack.map(Exp.Call.apply) |
      (keyword(KALLOC) *> recExpression.map(Exp.AllocArray.apply)) |
      keyword(KNULL).map(_ => Exp.NullRef.apply) |
      spaced(Numbers.signedIntString.map(s => Exp.NumLit(s.toInt))) |
      inParens(recExpression) |
      inBraces(list0((identifier <* op(':')) ~ recExpression)).map(Exp.Record.apply) |
      variable

  val access: P[Exp] =
    ((variable  | inParens(recExpression)) ~ (op('.') *> identifier).rep)
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
