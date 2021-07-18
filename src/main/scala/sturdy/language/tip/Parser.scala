package sturdy.language.tip

import cats.parse.{Parser0 => P0, Parser => P, Numbers}

import scala.collection._
import scala.language.implicitConversions

/**
 *  Parser for TIP programs, adapted for cats-parse from https://github.com/cs-au-dk/TIP/blob/master/src/tip/parser/TipParser.scala
 */
object Parser {

  /* LEXICAL */

  val whitespace: P[Unit] = P.charIn(" \t\r\n").void
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def spaced[A](p: P[A]): P[A] =
    p.surroundedBy(whitespaces0)

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
    P.string(s)

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
    spaced(P.char('(')) *> p <* spaced(P.char(')'))

  def inBraces[A](p: P0[A]): P[A] =
    spaced(P.char('{')) *> p <* spaced(P.char('}'))

  def list[A](p: P[A]): P0[List[A]] =
    p.repSep0(P.char(','))

  val semi: P[Unit] =
    spaced(P.char(';'))

  /* STRUCTURAL */

  val expression: P[Exp] = spaced(P.char('0')).map(_ => Exp.NumLit(0))

  val statement: P[Stm] = P.fail

  val varDecl: P[List[String]] =
    keyword(KVAR) *> list(identifier) <* semi

  val function: P[Function] =
    (identifier ~ inParens(list(identifier)) ~
      inBraces(
        varDecl.rep0 ~
        statement.rep0 ~
        (keyword(KRETURN) *> expression <* semi)
      )
    ).map { case ((name, params), ((locals, body), ret)) => Function(name, params, locals.flatten, Stm.Block(body), ret) }

  val program: P0[Program] =
    function.rep0.map(Program.apply)


  //  def Return: Rule1[AReturnStmt] = rule {
  //    push(cursor) ~ LanguageKeywords.KRETURN ~ Expression ~ ";" ~> ((cur: Int, e: AExpr) => AReturnStmt(e, cur))
  //  }
  //

  //  def Declaration: Rule1[AVarStmt] = rule {
  //    push(cursor) ~ LanguageKeywords.KVAR ~ oneOrMore(IdentifierDeclaration)
  //      .separatedBy(",") ~ ";" ~> ((cur: Int, idSeq: Seq[AIdentifierDeclaration]) => AVarStmt(idSeq.toList, cur))
  //  }
  //

  //
  //  def FunBlock: Rule1[AFunBlockStmt] = rule {
  //    push(cursor) ~ "{" ~ VarStatements ~ Statements ~ Return ~ "}" ~> (
  //      (
  //        cur: Int,
  //        declarations: Seq[AVarStmt],
  //        others: Seq[AStmtInNestedBlock],
  //        ret: AReturnStmt
  //      ) => AFunBlockStmt(declarations.toList, others.toList, ret, cur)
  //      )
  //  }
  //

  //  def TipFunction: Rule1[AFunDeclaration] = rule {
  //    push(cursor) ~ Identifier ~ "(" ~ zeroOrMore(IdentifierDeclaration).separatedBy(",") ~ ")" ~ FunBlock ~> {
  //      (cur: Int, id: AIdentifier, args: Seq[AIdentifierDeclaration], b: AFunBlockStmt) =>
  //        AFunDeclaration(id.name, args.toList, b, cur)
  //    }
  //  }
  //


  //  def InputLine = rule {
//    Program ~ EOI
//  }
//
//  def WS = rule {
//    NewLine | CharPredicate(" \t\f") | Comment
//  }
//
//  def OptSpace = rule {
//    quiet(zeroOrMore(WS))
//  }
//
//  implicit def wspStr(s: String): Rule0 = rule {
//    quiet(OptSpace ~ str(s) ~ OptSpace)
//  }
//
//  def AssignableExpression: Rule1[Assignable] = rule {
//    (DirectFieldWrite | IndirectFieldWrite | Identifier | DerefWrite) ~> { x: Assignable =>
//      x
//    }
//  }
//
//  def DirectFieldWrite: Rule1[ADirectFieldWrite] = rule {
//    push(cursor) ~ Identifier ~ wspStr(".") ~ Identifier ~> ((cur: Int, id: AIdentifier, field: AIdentifier) => ADirectFieldWrite(id, field.name, cur))
//  }
//
//  def IndirectFieldWrite: Rule1[AIndirectFieldWrite] = rule {
//    push(cursor) ~ wspStr("(") ~ wspStr("*") ~ Expression ~ wspStr(")") ~ wspStr(".") ~ Identifier ~> (
//      (
//        cur: Int,
//        exp: AExpr,
//        field: AIdentifier
//      ) => AIndirectFieldWrite(exp, field.name, cur)
//      )
//  }
//
//  def DerefWrite: Rule1[ADerefWrite] = rule {
//    push(cursor) ~ wspStr("*") ~ Expression ~> ((cur: Int, exp: AExpr) => ADerefWrite(exp, cur))
//  }
//
//  def Expression: Rule1[AExpr] = rule {
//    Operation ~
//      optional(
//        push(cursor) ~ ">" ~ Operation ~> ((e1: AExpr, cur: Int, e2: AExpr) => ABinaryOp(GreatThan, e1, e2, cur))
//          | push(cursor) ~ "==" ~ Operation ~> ((e1: AExpr, cur: Int, e2: AExpr) => ABinaryOp(Eqq, e1, e2, cur))
//      )
//  }
//
//  def Record: Rule1[ARecord] = rule {
//    push(cursor) ~ "{" ~ zeroOrMore(Field).separatedBy(wspStr(",")) ~ "}" ~> ((cur: Int, fields: Seq[ARecordField]) => ARecord(fields.toList, cur))
//  }
//
//  def Access: Rule1[AFieldAccess] =
//    rule {
//      (Identifier | DeRef | Parens) ~ oneOrMore("." ~ Identifier) ~> (
//        (
//          e1: AExpr,
//          fs: Seq[AIdentifier]
//        ) => fs.foldLeft(e1)((e: AExpr, f: AIdentifier) => AFieldAccess(e, f.name, f.loc))
//        )
//    }.asInstanceOf[Rule1[AFieldAccess]]
//
//  def Field: Rule1[ARecordField] = rule {
//    push(cursor) ~ Identifier ~ wspStr(":") ~ Expression ~> ((cur: Int, id: AIdentifier, expr: AExpr) => ARecordField(id.name, expr, id.loc))
//  }
//
//  def Operation: Rule1[AExpr] = rule {
//    Term ~
//      optional(
//        push(cursor) ~ "+" ~ Expression ~> ((e1: AExpr, cur: Int, e2: AExpr) => ABinaryOp(Plus, e1, e2, cur))
//          | push(cursor) ~ "-" ~ Expression ~> ((e1: AExpr, cur: Int, e2: AExpr) => ABinaryOp(Minus, e1, e2, cur))
//      )
//  }
//
//  def Term: Rule1[AExpr] = rule {
//    Access | (Atom ~ optional(
//      push(cursor) ~ "*" ~ Term ~> ((e1: AExpr, cur: Int, e2: AExpr) => ABinaryOp(Times, e1, e2, cur))
//        | push(cursor) ~ "/" ~ Term ~> ((e1: AExpr, cur: Int, e2: AExpr) => ABinaryOp(Divide, e1, e2, cur))
//    ))
//  }
//
//  def Atom: Rule1[AExpr] = rule {
//    (FunApp
//      | Number
//      | Parens
//      | PointersExpression
//      | push(cursor) ~ wspStr(LanguageKeywords.KINPUT) ~> ((cur: Int) => AInput(cur))
//      | Identifier
//      | Record)
//  }
//
//  def Parens = rule {
//    "(" ~ Expression ~ ")" ~> (a => a)
//  }
//
//  def Number: Rule1[AExpr] = rule {
//    push(cursor) ~ capture(Digits) ~> ((cur: Int, n: String) => ANumber(n.toInt, cur))
//  }
//
//  def Digits = rule {
//    optional("-") ~ oneOrMore(CharPredicate.Digit)
//  }
//
//  private var c: Int = _
//
//  def Id: Rule1[String] = rule {
//    run(c = cursor) ~ atomic(capture((CharPredicate.Alpha | '_') ~ zeroOrMore(CharPredicate.AlphaNum | '_')).named("identifier")) ~ quiet(
//      test(!keywords.contains(input.sliceString(c, cursor)))
//    )
//  }
//
//  def Identifier: Rule1[AIdentifier] = rule {
//    push(cursor) ~ OptSpace ~ Id ~> ((cur: Int, id: String) => AIdentifier(id, cur))
//  }
//
//
//  def PointersExpression = rule {
//    (push(cursor) ~ wspStr(LanguageKeywords.KALLOC) ~ Expression ~> ((cur: Int, exp: AExpr) => AAlloc(exp, cur))
//      | push(cursor) ~ wspStr("null") ~> ((cur: Int) => ANull(cur))
//      | Ref
//      | DeRef)
//  }
//
//  def Ref = rule {
//    push(cursor) ~ "&" ~ Identifier ~> ((cur: Int, id: AIdentifier) => AVarRef(id, cur))
//  }
//
//  def DeRef: Rule1[AUnaryOp] = rule {
//    push(cursor) ~ "*" ~ Atom ~> ((cur: Int, e: AExpr) => AUnaryOp(DerefOp, e, cur))
//  }
//
//  def Statements: Rule1[Seq[AStmtInNestedBlock]] = rule {
//    zeroOrMore(Statement)
//  }
//
//  def Statement: Rule1[AStmtInNestedBlock] = rule {
//    Output | Assigment | Block | While | If | Error
//  }
//
//  def Assigment: Rule1[AStmtInNestedBlock] = rule {
//    push(cursor) ~ AssignableExpression ~ "=" ~ Expression ~ ";" ~> { (cur: Int, e1: Assignable, e2: AExpr) =>
//      AAssignStmt(e1, e2, cur)
//    }
//  }
//
//  def Block: Rule1[AStmtInNestedBlock] = rule {
//    push(cursor) ~ "{" ~ Statements ~ "}" ~> ((cur: Int, body: Seq[AStmtInNestedBlock]) => ANestedBlockStmt(body.toList, cur))
//  }

//  def While: Rule1[AStmtInNestedBlock] = rule {
//    push(cursor) ~ LanguageKeywords.KWHILE ~ "(" ~ Expression ~ ")" ~ Statement ~> ((cur: Int, e: AExpr, b: AStmtInNestedBlock) => AWhileStmt(e, b, cur))
//  }
//
//  def If: Rule1[AStmtInNestedBlock] = rule {
//    (push(cursor) ~ LanguageKeywords.KIF ~ "(" ~ Expression ~ ")" ~ Statement ~ optional(LanguageKeywords.KELSE ~ Statement)) ~> {
//      (cur: Int, e: AExpr, bt: AStmtInNestedBlock, bf: Option[AStmtInNestedBlock]) =>
//      {
//        AIfStmt(e, bt, bf, cur)
//      }
//    }
//  }
//
//  def Output: Rule1[AStmtInNestedBlock] = rule {
//    push(cursor) ~ LanguageKeywords.KOUTPUT ~ Expression ~ ";" ~> ((cur: Int, e: AExpr) => AOutputStmt(e, cur))
//  }
//
//  def Error: Rule1[AStmtInNestedBlock] = rule {
//    push(cursor) ~ LanguageKeywords.KERROR ~ Expression ~ ";" ~> ((cur: Int, e: AExpr) => AErrorStmt(e, cur))
//  }
//
//  def FunApp: Rule1[AExpr] = rule {
//    push(cursor) ~ (Parens | Identifier) ~ FunActualArgs ~> ((cur: Int, fun: AExpr, args: Seq[AExpr]) => ACallFuncExpr(fun, args.toList, cur))
//  }
//
//  def FunActualArgs: Rule1[Seq[AExpr]] = rule {
//    "(" ~ zeroOrMore(Expression).separatedBy(",") ~ ")"
//  }
}

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
