package sturdy.language.jimple

import cats.parse.{Numbers, Parser as P, Parser0 as P0}

import scala.collection.*
import scala.language.{implicitConversions, postfixOps}

object Parser:

  def parse(source: String): Class =
    classes.parseAll(source) match
      case Right(p) => p
      case Left(err) => throw new IllegalArgumentException(s"Parse error at ${source.slice(err.failedAtOffset, err.failedAtOffset+10)}: $err")

  /* LEXICAL */
  object LanguageKeywords {
    val KIF = "if"
    val KNULL = "null"
    val KINT = "int"
    val KLONG = "long"
    val KFLOAT = "float"
    val KDOUBLE = "double"
    val KVOID = "void"
    val KCAUGHTEXCEPTION = "@caughtexception"
    val KPARAMETER = "@parameter"
    val KTHIS = "@this"
    val KINTERFACEINVOKE = "interfaceinvoke"
    val KSPECIALINVOKE = "specialinvoke"
    val KVIRTUALINVOKE = "virtualinvoke"
    val KSTATICINVOKE = "staticinvoke"
    val KCASE = "case"
    val KCATCH = ".catch"
    val KFROM = "from"
    val KTO = "to"
    val KUSING = "using"
    val KCMP = "cmp"
    val KCMPG = "cmpg"
    val KCMPL = "cmpl"
    val KUSHR = "ushr"
    val KXOR = "xor"
    val KLENGTH = "length"
    val KINSTANCEOF ="instanceof"
    val KNEW = "new"
    val KNEWMULTIARRAY = "new multiarray"
    val KBREAKPOINT = "breakpoint"
    val KENTERMONITOR = "entermonitor"
    val KEXITMONITOR = "exitmonitor"
    val KGOTO = "goto"
    val KTHEN = "then"
    val KLOOKUPSWITCH = "lookupswitch"
    val KDEFAULT = "default"
    val KNOP = "nop"
    val KRET = "ret"
    val KRETURN = "return"
    val KTABLESWITCH = "tableswitch"
    val KTHROW = "throw"
    val KCLASS = "class"
    val KEXTENDS = "extends"
    val KIMPLEMENTS = "implements"
//    val KPUBLIC = "public"
//    val KPRIVATE = "private"
    val KSTATIC = "static"
  }

  import LanguageKeywords.*

  val keywords = Set(
    KIF,
    KNULL,
    KINT,
    KLONG,
    KFLOAT,
    KDOUBLE,
    KVOID,
    KCAUGHTEXCEPTION,
    KPARAMETER,
    KTHIS,
    KINTERFACEINVOKE,
    KSPECIALINVOKE,
    KVIRTUALINVOKE,
    KSTATICINVOKE,
    KCASE,
    KCATCH,
    KFROM,
    KTO,
    KUSING,
    KCMP,
    KCMPG,
    KCMPL,
    KUSHR,
    KXOR,
    KLENGTH,
    KINSTANCEOF,
    KNEW,
    KNEWMULTIARRAY,
    KBREAKPOINT,
    KENTERMONITOR,
    KEXITMONITOR,
    KGOTO,
    KTHEN,
    KLOOKUPSWITCH,
    KDEFAULT,
    KNOP,
    KRET,
    KRETURN,
    KTABLESWITCH,
    KTHROW,
    KCLASS,
    KEXTENDS,
    KIMPLEMENTS,
//    KPUBLIC,
//    KPRIVATE,
    KSTATIC
  )

  val lineComment: P[Unit] = P.string("//") *> P.charsWhile0(c => c != '\n' && c != '\r').void
  val blockComment: P[Unit] = P.string("/*") *> P.recursive[Unit](rec =>
  P.product01(P.charsWhile0(c => c != '*').void, P.string("*/") | P.char('*') ~ rec).void
  )
  val comment: P[Unit] = lineComment | blockComment
  val whitespace: P[Unit] = (P.charIn(" \t\r\n").void | comment)
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  def keyword(s: String): P[Unit] =
    spaced(P.string(s) *> P.not(letterDigit))

  def keywordNoSpace(s: String): P[Unit] =
    P.string(s) *> P.not(letter)

  val letter: P[Unit] = P.ignoreCaseCharIn('a' to 'z').void
  val digit: P[Unit] = P.charIn('0' to '9').void
  val letterDigit: P[Unit] = P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).void

  val id: P[String] =
    (letter ~ letterDigit.rep0)
    .string
    .filter(!keywords(_)).backtrack

  val identifier: P[String] =
    spaced(id)

  val generatedIdentifier: P[String] =
    spaced((P.string("$") ~ id).string)

  def spaced[A](p: P[A]): P[A] =
    p <* whitespaces0

  def op(c: Char): P[Char] =
    spaced(P.char(c).string.map(_.head))

  def op(s: String): P[String] =
    spaced(P.string(s).string)

  def isPublic(s: String): Boolean =
    return s == "public"

  val int: P[Int] =
    spaced(Numbers.signedIntString).map(_.toInt)

  val long: P[Long] =
    spaced(Numbers.jsonNumber).map(_.toLong)

  val double: P[Double] =
    spaced(Numbers.jsonNumber).map(_.toDouble)

  val float: P[Float] =
    spaced(Numbers.jsonNumber).map(_.toFloat)

  val string: P[String] =
    spaced(P.string("\"") *> P.charsWhile0(c => c!= '\"') <* P.string("\""))

  val className: P[String] =
    (spaced(P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).rep ~
      (P.char('.') ~ P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).rep)
        .rep0)).string.backtrack

  val semi: P[Unit] =
    op(';').void

  /* STRUCTURAL */
  def inBrackets[A](p: P0[A]): P[A] =
    op('[') *> p <* op(']')

  def inParens[A](p: P0[A]): P[A] =
    op('(') *> p <* op(')')

  def inBraces[A](p: P0[A]): P[A] =
    op('{') *> p <* op('}')

  def inQuotes[A](p: P0[A]): P[A] =
    op("\"") *> p <* op("\"")

  def inDiamonds[A](p: P0[A]): P[A] =
    op('<') *> p <* op('>')

  def calculateLocalDecs(t: Type, i: String, is: Seq[String]): Seq[LocalDec] =
    var ret = List(LocalDec(t, i))
    if(is.length != 0) {
      for (s <- is) {
        ret = ret :+ LocalDec(t, s)
      }
    }
    return ret

  val constants: P[Constant] =
    int.backtrack.map(Constant.IntC.apply) |
      double.backtrack.map(Constant.DoubleC.apply) | //TODO: Int is cast as double if double comes first
      long.backtrack.map(Constant.LongC.apply) |
      float.backtrack.map(Constant.FloatC.apply) |
      string.map(Constant.StringC.apply) |
      keyword(KNULL).map(_ => Constant.NullC())

  val types: P[Type] =
    keyword(KINT).map(_ => Type.IntT()) |
      keyword(KLONG).map(_ => Type.LongT()) |
      keyword(KFLOAT).map(_ => Type.FloatT()) |
      keyword(KDOUBLE).map(_ => Type.DoubleT()) |
      keyword(KVOID).map(_ => Type.VoidT()) |
      (className ~ op("[]").?).map{
        case (s, None) => Type.RefT(s)
        case (s, Some(x)) => Type.RefT(s+x)
      }

  val identityValues: P[IdentityVal] =
    keyword(KCAUGHTEXCEPTION).map(_ => IdentityVal.CaughtExcRef()) |
      (keywordNoSpace(KPARAMETER) *> constants ~ (op(":") *> types))
        .map {
          case (y: Constant.IntC, x) => IdentityVal.ParamRef(y, x)
          case _ => throw new IllegalArgumentException } |
      ((keyword(KTHIS) ~ op(":")) *> types).map(t => IdentityVal.ThisRef(t))

  val invokeTypes: P[InvokeType] =
    keyword(KINTERFACEINVOKE).map(_ => InvokeType.InterfaceI) |
      keyword(KSPECIALINVOKE).map(_ => InvokeType.SpecialI) |
      keyword(KVIRTUALINVOKE).map(_ => InvokeType.VirtualI)

  val immediates: P[Immediate] =
    constants.map(Immediate.ConstI.apply) |
      (identifier | generatedIdentifier).map(s => Immediate.LocalI(Local(s)))

  val localDeclarations: P[Seq[LocalDec]] =
    ((types ~ (identifier | generatedIdentifier ) ~ ((op(',') *> (identifier | generatedIdentifier )).rep0)) <* semi)
      .backtrack.map {case ((t, id: String), ids) => calculateLocalDecs(t, id, ids) }

  val cases: P[Case] =
    (keyword(KCASE) *> constants ~ (op(":") *> identifier))
      .map {
        case (y: Constant.IntC, x) => Case(y, x)
        case _ => throw new IllegalArgumentException
      }

  val methodSignatures: P[MethodSignature] =
    ((className <* op(':')) ~ types ~ (identifier | inDiamonds(identifier)) ~ inParens(types.rep0))
      .map { case (((className, returnType), name), paramTypes) =>
        MethodSignature(name,paramTypes,returnType, className)
      }

  val fieldSignatures: P[FieldSignature] =
    ((className <* op(':')) ~ types ~ identifier).map { case ((className, fieldType), name) => FieldSignature(name, fieldType, className)}

  val exceptionRanges: P[ExceptionRange] =
    ((keyword(KCATCH) *> types) ~
      (keyword(KFROM) *> identifier) ~
      (keyword(KTO) *> identifier) ~
      (keyword(KUSING) *> identifier))
      .map {
        case (((excType: Type.RefT,from),to),using) =>
          ExceptionRange(excType, from, to, using)
        case _ => throw new IllegalArgumentException
      }

  val binaryOperators: P[BinOp] =
    op('+').map(_ => BinOp.Add) |
      op('&').map(_ => BinOp.And) |
      keyword(KCMP).map(_ => BinOp.Cmp) |
      keyword(KCMPG).map(_ => BinOp.Cmpg) |
      keyword(KCMPL).map(_ => BinOp.Cmpl) |
      op('/').map(_ => BinOp.Div) |
      op('*').map(_ => BinOp.Mul) |
      op('|').map(_ => BinOp.Or) |
      op('%').map(_ => BinOp.Rem) |
      op("<<").map(_ => BinOp.Shl) |
      op(">>").map(_ => BinOp.Shr) |
      op('-').map(_ => BinOp.Sub) |
      keyword(KUSHR).map(_ => BinOp.Ushr) |
      keyword(KXOR).map(_ => BinOp.Xor)

  val conditionalOperators: P[CondOp] =
    op("==").map(_ => CondOp.Eq) |
      op(">=").map(_ => CondOp.Ge) |
      op("<=").map(_ => CondOp.Le) |
      op("!=").map(_ => CondOp.Ne) |
      op('>').map(_ => CondOp.Gt) |
      op('<').map(_ => CondOp.Lt)

  val unaryOperators: P[UnOp] =
    keyword(KLENGTH) *> immediates.map(UnOp.Length.apply) |
      op('-') *> immediates.map(UnOp.Neg.apply)

  val variables: P[Var] =
    (immediates ~ inBrackets(immediates)).backtrack.map((i1,i2) => Var.ArrayRefV(i1,i2)).backtrack |
      ((immediates <* op(".")) ~ inDiamonds(fieldSignatures)).backtrack.map((i,f) => Var.InstanceFieldRefV(i,f)).backtrack |
      inDiamonds(fieldSignatures).backtrack.map(Var.StaticFieldRefV.apply) |
      (identifier | generatedIdentifier).map(i => Var.LocalV(Local(i)))

  val expressions: P[Exp] =
    (immediates ~ (binaryOperators) ~ immediates)
      .backtrack.map {
        case ((i1,operator : BinOp),i2) => Exp.BinopE(i1, i2, operator) }|
      (immediates ~ conditionalOperators ~ immediates)
        .backtrack.map{
          case ((i1, cond), i2) => Exp.ConditionE(i1, i2, cond)} |
      (inParens(types) ~ immediates).map((t,i) => Exp.CastE(t,i)) |
      (immediates ~ (keyword(KINSTANCEOF) *> types))
        .backtrack.map {
          case (i, t : Type.RefT) => Exp.InstanceOfE(i, t)
          case _ => throw new IllegalArgumentException } |
      keyword(KSTATICINVOKE) *> (inDiamonds(methodSignatures) ~ inParens(immediates.rep0))
        .map((m, ls) => Exp.StaticInvokeE(m, ls)) |
      (invokeTypes ~ immediates ~ (op('.') *> inDiamonds(methodSignatures)) ~ inParens(immediates.rep0))
        .map{
          case (((t, i), m), ls) => Exp.InvokeE(t, i, m, ls)
        } |
      (keyword(KNEW) *> types ~ inBrackets(immediates)).backtrack.map((t,i) => Exp.NewArrayE(t, i)) |
      (keyword(KNEW) *> types <* op("()"))
        .map{
          case t : Type.RefT => Exp.NewE(t)
          case _ => throw new IllegalArgumentException }
        .backtrack |
      (keyword(KNEWMULTIARRAY) *> types ~ inBrackets(immediates).rep0 ~ op("[]").rep0)
        .map{
          case ((t, dims), edims) => Exp.NewMultArrE(t, dims, edims.length)
        }.backtrack |
      (unaryOperators ~ immediates).map((operator, i) => Exp.UnopE(i, operator))

  val rValues: P[RVal] =
    (immediates ~ inBrackets(immediates)).backtrack.map((i1,i2) => RVal.ArrayRefR(i1,i2)) |
      constants.map(RVal.ConstR.apply) |
      expressions.map(e => RVal.ExpressionR(e)) |
      ((immediates <* op(".")) ~ inBrackets(fieldSignatures)).map((i,f) => RVal.InstanceFieldRefR(i,f)) |
      inDiamonds(fieldSignatures).map(RVal.StaticFieldRefR.apply) |
      identifier.map(i => RVal.LocalR(Local(i)))

  val identityStatements: P[Stmt.IdentityS] =
    (identifier ~ (op(":=") *> identityValues <* semi)).backtrack.map((s, iv) => Stmt.IdentityS(Local(s), iv))

  val statements: P[Stmt] =
    keyword(KBREAKPOINT) *> semi.map(_ => Stmt.BreakpointS()) |
      (variables ~ (op('=') *> rValues <* semi)).backtrack.map((l, r) => Stmt.AssignS(l, r)) |
      identityStatements |
      keyword(KENTERMONITOR) *> (immediates <* semi).map(Stmt.EnterMonitorS.apply) |
      keyword(KEXITMONITOR) *> (immediates <* semi).map(Stmt.ExitMonitorS.apply) |
      keyword(KGOTO) *> (identifier <* semi).map(Stmt.GotoS.apply) |
      (keyword(KIF) *> expressions ~ (keyword(KGOTO) *> identifier <* semi))
        .map{
          case (e : Exp.ConditionE, s) => Stmt.IfS(e, s)
          case _ => throw new IllegalArgumentException } |
      (expressions <* semi).backtrack.map{
        case e : (Exp.InvokeE | Exp.StaticInvokeE) => Stmt.InvokeS(e)
        case _ => throw new IllegalArgumentException } |
      (keyword(KLOOKUPSWITCH) *> inParens(immediates) ~ inBraces(cases.rep0 ~ (
        keyword(KDEFAULT) *> semi *> keyword(KGOTO) *> identifier)))
        .map {
          case (i, (ls, s)) => Stmt.LookupSwitchS(i, ls, s) } |
      keyword(KNOP) *> semi.map(_ => Stmt.NopS()) |
      keyword(KRET).backtrack *> (identifier <* semi).backtrack.map(s => Stmt.RetS(Local(s))) |
      (keywordNoSpace(KRETURN) *> semi).backtrack.map(_ => Stmt.ReturnVoidS()) |
      (keyword(KRETURN) *> (immediates <* semi)).map(Stmt.ReturnS.apply) |
      (keyword(KTABLESWITCH) *> inParens(immediates) ~ inBraces(cases.rep0 ~ (
        keyword(KDEFAULT) *> semi *> keyword(KGOTO) *> identifier)))
        .map {
        case (i, (ls, s)) => Stmt.TableSwitchS(i, ls, s) } |
      keyword(KTHROW) *> (immediates <* semi).map(Stmt.ThrowS.apply) |
      (id <* op(':')).map((l) => Stmt.LabelS(l))

  val methodheaders: P[MethodHeader] =
    ((op("public") | op("private") ) ~ keyword(KSTATIC).? //TODO: No public or private!
      ~ types ~ (identifier | inDiamonds(identifier)) ~ inParens(types.rep0))
      .map{
        //case ((((isP: Unit, None), ret), id), params) => MethodHeader(false, false, false, ret, id, params)
        //case ((((isP: Unit, Some(x)), ret), id), params) => MethodHeader(false, false, true, ret, id, params)
        case ((((isP: String, None), ret), id), params) => MethodHeader(isPublic(isP), !isPublic(isP), false, ret, id, params)
        case ((((isP: String, Some(x)), ret), id), params) => MethodHeader(isPublic(isP), !isPublic(isP), true, ret, id, params)
        case _ => throw new IllegalArgumentException
      }



  val methods: P[Method] =
    (methodheaders ~ inBraces(localDeclarations.rep0 ~ identityStatements.rep0 ~
      statements.rep0 ~ exceptionRanges.rep0))
      .map {
        case (header, (((locals, idStmts), stmts), exceptions)) =>
          Method(header, locals.flatten, idStmts, stmts, exceptions)
      }.backtrack

  val classes: P[Class] =
    keyword(KCLASS) *> (identifier ~ (keyword(KEXTENDS) *> types).? ~ (keyword(KIMPLEMENTS) *> types.rep).? ~ inBraces(methods.rep0))
      .map{
        case (((id, None), None), methods) => Class(id, None, Seq.empty[Type.RefT], methods)
        case (((id, None), Some(impl): Option[Seq[Type.RefT]]), methods) => Class(id, None, impl, methods)
        case (((id, Some(ext): Option[Type.RefT]), None), methods) => Class(id, Some(ext), Seq.empty[Type.RefT], methods)
        case (((id, Some(ext): Option[Type.RefT]), Some(impl): Option[Seq[Type.RefT]]), methods) => Class(id, Some(ext), impl, methods)
        case _ => throw new IllegalArgumentException
      }

//  val program: P0[Program] = ???