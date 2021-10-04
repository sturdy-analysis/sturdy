package sturdy.language.minijava

import cats.parse.{Parser0 => P0, Parser => P, Numbers}

import scala.collection._
import scala.language.implicitConversions

object Parser:

  def parse(source: String): Program =
    program.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(err.toString)

  //LEXICAL
  // comments

  val lineComment: P[Unit] = P.string("//") *> P.charsWhile0(c => c != '\n' && c != '\r').void
  val blockComment: P[Unit] = P.string("/*") *> P.recursive[Unit](rec =>
    P.product01(P.charsWhile0(c => c != '*').void, P.string("*/") | P.char('*') ~ rec).void
  )
  val comment: P[Unit] = lineComment | blockComment
  val whitespace: P[Unit] = (P.charIn(" \t\r\n").void | comment)
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def spaced[A](p: P[A]): P[A] =
    p <* whitespaces0

  object LanguageKeywords{
    val KTHIS = "this"
    val KNEW = "new"
    val KWHILE = "while"
    val KIF = "if"
    val KELSE = "else"
    val KRETURN = "return"
    val KVOID = "void"
    val KINT = "int"
    val KBOOLEAN = "boolean"
    val KINTARR = "int[]"
    val KCLASS = "class"
    val KMAIN = "main"
    val KEXT = "extends"
    val KSTATIC = "static"
    val KPRIVATE = "private"
    val KPUBLIC = "public"
    val KPRINTLINE = "System.out.println"
    val KTRUE = "true"
    val KFALSE = "false"
    val KLEN = "length"
  }

  import LanguageKeywords.*

  val keywords = Set(
    KTHIS,
    KNEW,
    KINT,
    KBOOLEAN,
    KINTARR,
    KWHILE,
    KIF,
    KELSE,
    KRETURN,
    KVOID,
    KCLASS,
    KMAIN,
    KEXT,
    KSTATIC,
    KPRIVATE,
    KPUBLIC,
    KPRINTLINE,
    KTRUE,
    KFALSE,
    KLEN
  )




  def keyword(s: String): P[Unit] =
    spaced(P.string(s))

  val letter: P[Unit] = P.ignoreCaseCharIn('a' to 'z').void
  val digit: P[Unit] = P.charIn('0' to '9').void
  val letterDigit: P[Unit] = P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ Set('_')).void
  val True: P[Unit] = P.string("true")
  val False: P[Unit] = P.string("false")



  val id: P[String] =
    (letter ~ letterDigit.rep0 )
      .string
      .filter(s => (!keywords.contains(s)) ).backtrack

  val identifier: P[String] =
    spaced(id)


  val bool_val: P[String] =
    True.string.filter(b=> true).backtrack |
      False.string.filter(b => true).backtrack

  val bool: P[String] =
    spaced(bool_val)

  //Klammern, Kommas und Semikolons werden seperat geparst

  def inParens[A](p: P0[A]): P[A] =
    op('(') *> p <* op(')')

  def inBraces[A](p: P0[A]): P[A] =
    op('{') *> p <* op('}')

  def inBrackets[A](p: P0[A]): P[A] =
    op('[') *> p <* op(']')

  def list0[A](p: P[A]): P0[List[A]] =
    p.repSep0(op(','))

  def list[A](p: P[A]): P[List[A]] =
    p.repSep(op(',')).map(_.toList)

  val semi: P[Unit] =
    op(';')

  val dot: P[Unit] =
    op('.')

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


  val variable: P[Exp] =
    identifier.map(Exp.Var.apply)

  val boolean: P[Exp] =
    bool.map(Exp.BoolLit.apply)

  val types : P[Type] =
    keyword(KINTARR).map(_ => Type.IntArray()).backtrack | // int[]
      keyword(KINT).map(_ => Type.Int())|  // int
      keyword(KBOOLEAN).map(_ => Type.Boolean()) |  // boolean
      identifier.map(s => Type.Identifier(s)) //  Identifier x



  lazy val atom: P[Exp] =
    spaced(Numbers.signedIntString.map(s => Exp.NumLit(s.toInt))) | //<INTEGER_LITERAL>
      inParens(recExpression).backtrack | //"(" Expressions ")"
      keyword(KTHIS).map(Exp.This.apply) |
      variable.backtrack| //Variablen
      boolean.backtrack | //BoolLiterals
      (keyword(KNEW) *> keyword(KINT) *> op("[") *> recExpression <* op("]")).map(Exp.AllocArray.apply).backtrack |  // new int[Exp]
      (keyword(KNEW) *> identifier <* op("(") <* op(")") ).map(Exp.Alloc.apply).backtrack // new identifier()


  val access: P[Exp] =
    ((variable | inParens(recExpression)) ~ (op('[') *> recExpression <* op(']')).rep)
      .map { case (e, fields) => fields.foldLeft(e)(Exp.AccessArray.apply) }
      .backtrack // Exp[Exp]


  val term: P[Exp] =
    access |
    (op('!') *> recExpression).map(e => Exp.Not(e)) |
    (atom ~ (
      (op('*') *> recExpression).map(e2 => Exp.Mul(_, e2)) | // * operation
        (op('/') *> recExpression).map(e2 => Exp.Div(_, e2))
        ).?).map(maybeBinOp)



  val operation: P[Exp] =
    (term ~ (
      (op('+') *> recExpression).map(e2 => Exp.Add(_, e2)) | // + operation
        (op('-') *> recExpression).map(e2 => Exp.Sub(_, e2)) | //- operation
        (op("&&") *> recExpression).map(e2 => Exp.And(_, e2)) | // logical and
        (op("||") *> recExpression).map(e2 => Exp.Or(_, e2))
      ).?).map(maybeBinOp)

  val funCall: P[Exp] =
    ((atom <* op(".")).backtrack ~ (
      keyword(KLEN).map(_ => Exp.ArrayLength.apply) |
        (identifier ~ inParens(list0(recExpression))).map((name, args) => Exp.Call(_, name, args))
    )).map((e, f) => f(e))

  lazy val expression: P[Exp] =
    funCall |
    (operation ~ (
      (op('>') *> recExpression).map(e2 => Exp.Gt(_, e2)) |
        (op("==") *> recExpression).map(e2 => Exp.Eq(_, e2))
      ).?).map(maybeBinOp)

  val assignable: P[Assignable] =
    ((identifier ~ (op("[") *> recExpression <* op("]")))).map(
      (name,e) => Assignable.AArray(name,e)).backtrack |
      (identifier).map( name => Assignable.AVar(name)) //Avar: Id = Exp


  lazy val statement: P[Stm] =
    (keyword(KIF) *> inParens(recExpression) ~ recStatement ~ (keyword(KELSE) *> recStatement).?)
      .map { case ((c, t), e) => Stm.If(c, t, e) } | // If-Else
      (keyword(KWHILE) *> inParens(recExpression) ~ recStatement).map( (cond, stm) => Stm.While(cond, stm)) | //While
      inBraces(recStatement.rep0).map(Stm.Block.apply) | // Block
      (keyword(KPRINTLINE) *> inParens(recExpression) <* semi).map(Stm.Output.apply) | //"System.out.println" "(" Expression ")" ";"
      ((assignable <* op('='))  ~ recExpression <* semi).map(Stm.Assign.apply) // Identifier	::=	<IDENTIFIER>


  val varDecl: P[varDeclaration] =
    (types ~ identifier <* semi).map{case (t , name) => varDeclaration(t,name)}.backtrack // type id;


  //MethodDecl -> public Type id ( FormalList ) { VarDecl* Statement* return Exp ; }
  //"public" Type Identifier "(" ( Type Identifier ( "," Type Identifier )* )? ")" "{" ( VarDeclaration )* ( Statement )* "return" Expression ";" "}"
  // FormalList = ( Type1 id1, Type2 id2 ,....)
  val function: P[Function] =
  (keyword(KPUBLIC) *>(types ~ identifier ~ inParens(list0(types ~ identifier)) ~
    inBraces(
      varDecl.rep0 ~
        statement.rep0 ~
        (keyword(KRETURN) *> expression <* semi)
    )
    )).map { case ( ((return_type,name), params), ((locals, body), ret)) =>
    Function(return_type, name, params, locals, Stm.Block(body), ret)
  }

  val main_methode :P[MainFunction] =
    (keyword(KPUBLIC) *> keyword(KSTATIC) *> keyword(KVOID) *> keyword(KMAIN) *>
      inParens(op("String") *> op("[") *> op("]") *> identifier) ~ inBraces(
        varDecl.rep0 ~
          statement.rep0
      )).map{ case(arg,(locals, body)) => MainFunction(arg, locals, Stm.Block(body)) }.backtrack

  //normal class
  // "class" Identifier ( "extends" Identifier )? "{" ( VarDeclaration )* ( MethodDeclaration )* "}"
  val Class: P[classDeclaration] =
  ((keyword(KCLASS) *> identifier) ~ (keyword(KEXT) *> identifier).? ~ inBraces(
    varDecl.rep0 ~
      function.rep0 )).map {
    case ((name, extend), (locals, funs))
    => classDeclaration(name, extend, locals, funs)
  }

  //main class
  // "class" Identifier "{" "public" "static" "void" "main" "(" "String" "[" "]" Identifier ")" "{" Statement "}" "}"
  val MainClass : P[mainClass] =
  (keyword(KCLASS) *> identifier ~ inBraces(
    main_methode)).map{
    case(name, mainfun) => mainClass(name, mainfun)
  }


  //Programm
  //	MainClass ( ClassDeclaration )* <EOF>
  val program: P0[Program] =
  (whitespaces0 *> MainClass ~ Class.rep0 <* whitespaces0).map{
    case(main, other_classes) => Program(main, other_classes)
  } <* P.end
// whitespaces0 *> MainClass.rep0.map(Program.apply) <* P.end
