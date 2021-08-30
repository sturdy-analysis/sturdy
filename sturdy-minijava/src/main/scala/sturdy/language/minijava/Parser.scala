package sturdy.language.minijava

import cats.parse.{Numbers, Parser as P, Parser0 as P0}

import scala.collection.*
import scala.language.implicitConversions


package object Parser {

  def parse(source: String): Program =
    Program.parseAll(source) match
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
    val KNEW = "new"
    val KWHILE = "while"
    val KIF = "if"
    val KELSE = "else"
    val KVAR = "var"
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
    val KTHIS = "this"
    val KPRINTLINE = "System.out.println"
    val KTRUE = "true"
    val KFALSE = "false"
    val KLEN = "length"
  }

  import LanguageKeywords.*

  val keywords = Set(
    KNEW,
    KINT,
    KBOOLEAN,
    KINTARR,
    KWHILE,
    KIF,
    KELSE,
    KVAR,
    KRETURN,
    KVOID,
    KCLASS,
    KMAIN,
    KEXT,
    KSTATIC,
    KPRIVATE,
    KPUBLIC,
    KTHIS,
    KPRINTLINE,
    KTRUE,
    KFALSE,
    KLEN
  )

  def keyword(s: String): P[Unit] =
    spaced(P.string(s))

  val letter: P[Unit] = P.ignoreCaseCharIn('a' to 'z').void
  val digit: P[Unit] = P.charIn('0' to '9').void
  val letterDigit: P[Unit] = P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).void
  val True: P[Unit] = P.string("true")
  val False: P[Unit] = P.string("false")
//  val int_type: P[Unit] = P.string("int")
//  val boolean_type = P.string("boolean")

  val id: P[String] =
    (letter ~ letterDigit.rep0)
      .string
      .filter(s => !keywords.contains(s)).backtrack

  val identifier: P[String] =
    spaced(id)

  val bool_val: P[String] =
    True.string.filter(s => !keywords.contains(s)).backtrack | False.string.filter(s => !keywords.contains(s)).backtrack

  val bool: P[String] =
    spaced(bool_val)


 /* val type_val : P[String] =
    int_type.string.filter(s => !keywords.contains(s)).backtrack | boolean_type.string.filter(s => !keywords.contains(s)).backtrack

  val types : P[String] =
    spaced(type_val)*/

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

  def op(c: Char): P[Unit] =
    spaced(P.char(c))

  def op(s: String): P[Unit] =
    spaced(P.string(s))

  /* STRUCTURAL */

  private val maybeBinOp: ((Exp, Option[Exp => Exp])) => Exp = {
    case (e1, None) => e1
    case (e1, Some(f)) => f(e1)
  }

 // private val recAtom: P[Exp] = P.defer(atom)
  private val recExpression = P.defer(expression)
  private val recStatement = P.defer(statement)


  val variable: P[Exp] = identifier.map(Exp.Var.apply)

  val boolean: P[Exp] = bool.map(Exp.BoolLit.apply)

  val typed : P[Type] =
      keyword(KINT).map(_ => Type.Int()) |  // int
      keyword(KBOOLEAN).map(_ => Type.Boolean()) |  // boolean
      keyword(KINTARR).map(_ => Type.IntArray()) | // int[]
      (identifier.map(s => Type.Identifier(s)) <* variable)   // id x

  lazy val atom: P[Exp] =

    (keyword(KNEW) *> variable) | // new identifier
      spaced(Numbers.signedIntString.map(s => Exp.NumLit(s.toInt))) | //<INTEGER_LITERAL>
      inParens(recExpression) | //"(" Expressions ")"
      variable | //Variablen
      boolean | //BoolLit
      (keyword(KNEW) *> (keyword(KINT) *> inBrackets(atom).map(e => Exp.AllocArray(e)))) | // new int [Exp] x
      (keyword(KNEW) *> identifier) ~  inParens(list0(recExpression)) | // new Identifier ()
      (identifier | inParens(recExpression) <* op('.') <* keyword(KLEN)) | // Expression.length
        (op('!') *> variable).map(e => Exp.Not(e)) // Logical Not
      // "This" fehlt noch
      // Funktionsaufruf fehlt noch

  val access: P[Exp] =
    (variable| inParens(recExpression)|expression) ~ inParens(recExpression)   // Exp[Exp]


  val term: P[Exp] =
    access |
      (atom ~ (
        (op('*') *> recExpression).map(e2 => Exp.Mul(_, e2)) |
          (op('/') *> recExpression).map(e2 => Exp.Div(_, e2))
        ).?).map(maybeBinOp)

  val operation: P[Exp] =
    (term ~ (
      (op('+') *> recExpression).map(e2 => Exp.Add(_, e2)) | // + operation
        (op('-') *> recExpression).map(e2 => Exp.Sub(_, e2)) | // - operation
        (op("&&") *> recExpression).map(e2 => Exp.And(_, e2))
      ).?).map(maybeBinOp)

  lazy val expression: P[Exp] =
    (operation ~ (
      (op('>') *> operation).map(e2 => Exp.Gt(_, e2)) |
        (op("==") *> operation).map(e2 => Exp.Eq(_, e2))
      ).?).map(maybeBinOp)

  val assignable: P[Assignable] =
        identifier.map {
          case (x : String) => Assignable.AVar(x) //AVar
          case (x : String,e: Exp) => Assignable.AArray(x, e) //AArray
        }

        // Fall var[X] = y

  lazy val statement: P[Stm] =
    (keyword(KIF) *> inParens(recExpression) ~ recStatement ~ (keyword(KELSE) *> recStatement).?)
      .map { case ((c, t), e) => Stm.If(c, t, e) } | // If-Else
      (keyword(KWHILE) *> inParens(recExpression) ~ recStatement).map(Stm.While.apply) | //While
      inBraces(recStatement.rep0).map(Stm.Block.apply) | //Block
      (keyword(KPRINTLINE) *> inParens(recExpression).map(Stm.Output.apply)) | //PrintLine
      ((assignable <* op('=')) ~ recExpression <* semi).map(Stm.Assign.apply) //id = Exp
  //Fall id[Exp] = Exp fehlt noch? oder ist das schon mit bei assignable mitenthalten


  val varDecl: P[varDeclaration] =
    (((typed ~ identifier) <* op('=')) ~ expression).map{case (t : Type, name : String) => varDeclaration(t,name)} // type id


  //MethodDecl -> public Type id ( FormalList ) { VarDecl* Statement* return Exp ; }
  // FormalList = ( Type1 id1, Type2 id2 ,....)
  val function: P[Function] =
    (keyword((KPUBLIC))|keyword(KPRIVATE) *> keyword(KSTATIC) *> ( typed ~ identifier ~ inParens(list0(typed) ~ list0(identifier)) ~
      inBraces(
        varDecl.rep0 ~
          statement.rep0 ~
          (keyword(KRETURN) *> expression <* semi)
      )
      )).map { case ((name : String, returnType : Type, params : Seq[Tuple2[Type,String]]), ((locals : Seq[sturdy.language.minijava.varDeclaration], body : Stm), ret : Exp)) =>
    Function(name, returnType, params, locals.flatten, body, ret)
  }

  //main class
  val MainClass : P[mainClass] =
    (keyword(KCLASS) *> identifier ~ (keyword(KPUBLIC) *> keyword(KSTATIC) *> keyword(KVOID) *> keyword(KMAIN) *> inParens(op("String []") ~ identifier)) ~inBraces(statement)).map {
      case (name: String, params: Seq[String], body: Stm) => mainClass(name, params, body) //class id { public static void main ( String [] id ) { Statement } }


        //class
  val Class: P[classDeclaration] =
    keyword(KCLASS) *> identifier ~ (keyword(KEXT) *> list(identifier) ~ inBraces(list0(varDecl) ~ list0(function))).map {
      case (name: String, extend: Option[String], locals: Seq[varDeclaration], funs: Seq[Function])
      => classDeclaration(name, extend, locals, funs)
    } //class id extends id { VarDecl* MethodDecl* }


      //Program fehlt noch

  val program: P0[Program] =
    whitespaces0 *> MainClass.map{case (main : mainClass, classes :Seq[classDeclaration]) => Program(main, classes) } <* P.end

    }