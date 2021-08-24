package sturdy.language.schemelang

import cats.parse.{Numbers, Parser as P, Parser0 as P0}

import scala.collection.*
import scala.language.implicitConversions


object SExprParser:

  // create own file for this?
  enum SExpr:
    case id(id: String)
    case integer(i: Int)
    case rational(i1: Int, i2: Int)
    case double(d: Double)
    case string(str: String)
    case boolean(b: Boolean)
    case sexpr(exprs: List[SExpr])

  def parse(source: String): List[SExpr] =
    program.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(err.toString)

  val comment: P[Unit] = P.string(";") *> P.until0(P.charIn(""""\\""")).void
  val whitespace: P[Unit] = (P.charIn(" \t\r\n").void | comment)
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def spaced[A](p: P[A]): P[A] =
    p <* whitespaces0

  val id: P[String] = (P.charWhere(c => !"()".contains(c)) ~ P.until0(P.charIn(" ()"))).string
  val integer: P[Int] = P.charIn('0' to '9').rep.string.map(s => s.toInt)
  val rational: P[(Int,Int)]
    = (P.charIn('0' to '9').string.map(s => s.toInt)
    ~ P.char('/').void
    ~ P.charIn('0' to '9').string.map(s => s.toInt)).map({ case ((i1, _), i2) => (i1, i2) })
  val double: P[Double]
    = P.char('.') *> P.charIn('0' to '9').rep.string.map(d => ("0."++d).toDouble)
    | (P.charIn('0' to '9').rep *> P.char('.') *> P.charIn('0' to '9')).string.map(s => s.toDouble)

  val boolean: P[Boolean]
    = P.stringIn(List("#t", "#true").toIterable).map(_ => true)
    | P.stringIn(List("#f", "#false").toIterable).map(_ => false)
  val string: P[String] = P.char('"') *> P.until0(P.char('"')).string <* P.char('"')

  private val recSexpr = P.defer(sexpr)

  lazy val sexpr: P[SExpr]
    = spaced(string).map(SExpr.string.apply)
    | spaced(rational).map(SExpr.rational.apply).backtrack // not pretty
    | spaced(double).map(SExpr.double.apply).backtrack // not pretty
    | spaced(integer).map(SExpr.integer.apply)
    | spaced(boolean).map(SExpr.boolean.apply)
    | spaced(id).map(SExpr.id.apply)
    | spaced(P.char('(') *> recSexpr.rep.map(es => SExpr.sexpr(es.toList)) <* P.char(')'))

  // should probably not be part of the sexpr pasrser
  val program: P0[List[SExpr]] =
    whitespaces0 *> sexpr.rep0.map(List.apply) <* P.end