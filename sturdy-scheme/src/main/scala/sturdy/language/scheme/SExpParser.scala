package sturdy.language.scheme

import cats.parse.{Parser0 => P0, Parser => P, Numbers}

import scala.collection.*
import scala.language.implicitConversions
import SExp.*



object SExpParser:

  def parse(source: String): List[SExp] =
    program.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(err.toString)
  val comment: P[Unit] = P.string(";") *> P.charsWhile0(c => c != '\n' && c != '\r').void
  val whitespace: P[Unit] = (P.charIn(" \t\r\n").void | comment)
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def spaced[A](p: P[A]): P[A] = p <* whitespaces0

  val id: P[String] = (P.charWhere(c => !"()".contains(c)) ~ P.until0(whitespace | P.charIn("()"))).string

  val integer: P[Int] = P.charIn('0' to '9').rep.string.map(s => s.toInt)

  val rational: P[(Int, Int)]
    = (P.charIn('0' to '9').string.map(s => s.toInt)
    ~ P.char('/').void
    ~ P.charIn('0' to '9').string.map(s => s.toInt)).map({ case ((i1, _), i2) => (i1, i2) })

  val double: P[Double]
    = P.char('.') *> P.charIn('0' to '9').rep.string.map(d => ("0." ++ d).toDouble)
    | (P.charIn('0' to '9').rep *> P.char('.') *> P.charIn('0' to '9')).string.map(s => s.toDouble)

  val boolean: P[Boolean]
    = P.stringIn(List("#t", "#true").toIterable).map(_ => true)
    | P.stringIn(List("#f", "#false").toIterable).map(_ => false)

  val string: P[String] = P.char('"') *> P.until0(P.char('"')).string <* P.char('"')

  private val recSexpr = P.defer(sexpr)

  lazy val sexpr: P[SExp]
    = spaced(string).map(SExp.string.apply)
    | spaced(rational).map(SExp.rational.apply).backtrack // not pretty
    | spaced(double).map(SExp.double.apply).backtrack // not pretty
    | spaced(integer).map(SExp.integer.apply)
    | spaced(boolean).map(SExp.boolean.apply)
    | spaced(P.char('\'') *> recSexpr.map(SExp.quoted))
    | spaced(id).map(SExp.id.apply)
    | spaced(P.char('(') *> recSexpr.rep0.map(es => SExp.sexpr(es.toList)) <* P.char(')'))

  // should probably not be part of the sexpr pasrser
  val program: P0[List[SExp]] = whitespaces0 *> sexpr.rep0.map(List.apply) <* P.end
