package sturdy.language.jimple

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import JsonStringUtil.escapedString

// Paser turning Jimple code into Syntax elements
object Parser:

  // Parsing source code returns a Program. If an error occurs along the way, an exception is thrown.
  def parse(source: String): Program =
    programs.parseAll(source) match
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
    val KCATCH = "catch"
    val KFROM = "from"
    val KTO = "to"
    val KWITH = "with"
    val KCMP = "cmp"
    val KCMPG = "cmpg"
    val KCMPL = "cmpl"
    val KLENGTHOF = "lengthof"
    val KINSTANCEOF ="instanceof"
    val KNEW = "new"
    val KNEWARRAY = "newarray"
    val KNEWMULTIARRAY = "newmultiarray"
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
    val KPUBLIC = "public"
    val KPRIVATE = "private"
    val KSTATIC = "static"
    val KFINAL = "final"
    val KTHROWS = "throws"
    val KTRANSIENT = "transient"
    val KNATIVE = "native"
    val KVOLATILE = "volatile"
    val KINTERFACE = "interface"
    val KABSTRACT = "abstract"
    val KENUM = "enum"
    val KSYNCHRONIZED = "synchronized"
    val KINFINITY = "#Infinity"
    val KNEGINFINITY = "#-Infinity"
    val KINFINITYF = "#InfinityF"
    val KNEGINFINITYF = "#-InfinityF"
    val KNAN = "#NaN"
    val KNANF = "#NaNF"
    val KSTRICTFP = "strictfp"
  }

  import LanguageKeywords.*

  val keywords: Set[String] = Set(
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
    KWITH,
    KCMP,
    KCMPG,
    KCMPL,
    KLENGTHOF,
    KINSTANCEOF,
    KNEW,
    KNEWARRAY,
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
    KPUBLIC,
    KPRIVATE,
    KSTATIC,
    KFINAL,
    KTHROWS,
    KTRANSIENT,
    KNATIVE,
    KVOLATILE,
    KINTERFACE,
    KABSTRACT,
    KENUM,
    KSYNCHRONIZED,
    KINFINITY,
    KNEGINFINITY,
    KINFINITYF,
    KNEGINFINITYF,
    KNAN,
    KNANF,
    KSTRICTFP
  )

  // dealing with comments and whitespaces
  val lineComment: P[Unit] = P.string("//") *> P.charsWhile0(c => c != '\n' && c != '\r').void
  val blockComment: P[Unit] = P.string("/*") *> P.recursive[Unit](rec =>
    P.product01(P.charsWhile0(c => c != '*').void, P.string("*/") | P.char('*') ~ rec).void)
  val comment: P[Unit] = lineComment | blockComment
  val whitespace: P[Unit] = P.charIn(" \t\r\n").void | comment
  val whitespaces0: P0[Unit] = whitespace.rep0.void

  // function to parse a given keyword s and discard it
  def keyword(s: String): P[Unit] =
    spaced(P.string(s) *> P.not(letterDigit))

  // some keywords do not have spaces around them
  def keywordNoSpace(s: String): P[Unit] =
    P.string(s) *> P.not(letter)

  /* definitions of different types of identifiers */
  val letter: P[Unit] = P.ignoreCaseCharIn('a' to 'z').void
  val digit: P[Unit] = P.charIn('0' to '9').void
  val letterDigit: P[Unit] = P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).void

  val id: P[String] =
    ((letter | P.char('$') | P.char('_')) ~ (letterDigit | P.char('_') | P.char('$')).rep0 )
      .string
      .filter(!keywords(_)).backtrack

  val quotedId: P[String] =
    (op("\'") ~ (letter | P.char('$') | P.char('_')) ~ (letterDigit | P.char('_') | P.char('$')).rep0  ~ op("\'"))
      .string

  // identifiers can be in quotes
  val identifier: P[String] =
    spaced(id.backtrack | quotedId)

  // classIds contain path delimiters
  val classId: P[String] =
    ((P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')) | P.char('[')).rep ~
      (P.char('/') ~ (P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')) | P.char('$') | P.char('\'') | P.char('_')).rep)
        .rep0).string.backtrack

  def spaced[A](p: P[A]): P[A] =
    p <* whitespaces0

  def op(c: Char): P[Char] =
    spaced(P.char(c).string.map(_.head))

  def op(s: String): P[String] =
    spaced(P.string(s).string)

  // Function returning a boolean, based on the string given.
  // Helps to determine visibility of methods, classes, etc.
  val visibilities: Map[Int, String] = Map(0 -> "public", 1 -> "private", 2 -> "protected")
  def getVisibility(s: String, m: Int): Boolean =
    s == visibilities.apply(m)

  /* Numbers are based on cats-parse Numbers */
  val int: P[Int] =
    spaced(Numbers.bigInt).map(_.toInt).backtrack

  val long: P[Long] =
    spaced(Numbers.bigInt).map(_.toLong)

  val double: P[Double] =
    spaced(Numbers.jsonNumber).map(_.toDouble)

  val float: P[Float] =
    spaced(Numbers.jsonNumber).map(_.toFloat)

  // a string is surrounded by ""
  val string: P[String] =
    spaced(escapedString('"'))

  // A class name has the package name in which the class is contained at the beginning of it.
  // The name therefore contains periods.
  val className: P[String] =
    (P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).rep ~
      (P.char('.') ~ (P.charIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')) | P.char('$') | P.char('\'') | P.char('_') | P.char('-')).rep)
        .rep0).string.backtrack

  // helper function, since jimple has semicolons at the end of most statements
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

  // Helper function to split up several declarations of the same type.
  def calculateLocalDecs(t: Type, i: String, is: Seq[String]): Seq[LocalDec] =
    var ret = List(LocalDec(t, i))
    if(is.nonEmpty) {
      for (s <- is) {
        ret = ret :+ LocalDec(t, s)
      }
    }
    ret

  def addBrackets(s: String, i: Integer): String =
    var ret = ""
    for(j <- 0 until i)
      ret = s.concat("[]")
    ret

  // parsing all constants
  val constants: P[Constant] =
    (float <* spaced(P.ignoreCaseChar('f'))).map(Constant.FloatC.apply).backtrack |
      (long <* (P.not(P.char('.').peek) ~ spaced(P.ignoreCaseChar('l')))).backtrack.map(Constant.LongC.apply) |
      (int <* P.not(P.char('.').peek)).backtrack.map(Constant.IntC.apply) |
      double.map(Constant.DoubleC.apply) |
      string.map(Constant.StringC.apply) |
      keyword(KNULL).map(_ => Constant.NullC) |
      keyword(KNANF).map(_ => Constant.FloatNanC).backtrack |
      keyword(KNAN).map(_ => Constant.NanC) |
      keyword(KNEGINFINITYF).map(_ => Constant.FloatNegInfinityC).backtrack |
      keyword(KNEGINFINITY).map(_ => Constant.NegInfinityC).backtrack |
      keyword(KINFINITYF).map(_ => Constant.FloatInfinityC).backtrack |
      keyword(KINFINITY).map(_ => Constant.InfinityC)

  // parsing all possible types
  val baseTypes: P[Type] =
    keyword(KINT).map(_ => Type.IntT).backtrack |
      keyword(KLONG).map(_ => Type.LongT).backtrack |
      keyword(KFLOAT).map(_ => Type.FloatT).backtrack |
      keyword(KDOUBLE).map(_ => Type.DoubleT).backtrack |
      keyword(KVOID).map(_ => Type.VoidT).backtrack |
      spaced(className).map(s => Type.RefT(s)).backtrack

  // all types can either occur by themselves or as arrays
  val types: P[Type] =
    (baseTypes ~ op("[]").rep).map {
      case (t, x) => Type.ArrayT(t, x.length)
    }.backtrack |
      baseTypes

  // parsing identity values, depending on their keyword
  val identityValues: P[IdentityVal] =
    keyword(KCAUGHTEXCEPTION).map(_ => IdentityVal.CaughtExcRef()) |
      (keywordNoSpace(KPARAMETER) *> constants ~ (op(":") *> types))
        .map {
          case (y: Constant.IntC, x) => IdentityVal.ParamRef(y, x)
          case _ => throw new IllegalArgumentException } |
      ((keyword(KTHIS) ~ op(":")) *> types).map(t => IdentityVal.ThisRef(t))

  // parsing invocation kinds
  val invokeTypes: P[InvokeType] =
    keyword(KINTERFACEINVOKE).map(_ => InvokeType.InterfaceI) |
      keyword(KSPECIALINVOKE).map(_ => InvokeType.SpecialI) |
      keyword(KVIRTUALINVOKE).map(_ => InvokeType.VirtualI)

  // parsing all immediates
  val immediates: P[Immediate] =
    constants.map(Immediate.ConstI.apply)
      | (keyword(KCLASS) *> inQuotes(classId <* semi.?)).map(Immediate.ClassI.apply)
      | identifier.map(s => Immediate.LocalI(Local(s))).backtrack

  // parsing all local declarations with the help of calculateLocalDecs
  val localDeclarations: P[Seq[LocalDec]] =
    ((types ~ identifier ~ (op(',') *> identifier).rep0) <* semi)
      .backtrack.map {case ((t, id: String), ids) => calculateLocalDecs(t, id, ids) }

  // parsing casees of lookup or table switches
  val cases: P[Case] =
    (keyword(KCASE) *> constants ~ (op(":") *> keyword(KGOTO) *> identifier <* semi))
      .map {
        case (y: Constant.IntC, x) => Case(y, x)
        case _ => throw new IllegalArgumentException
      }

  // parsing method signatures
  val methodSignatures: P[MethodSignature] =
    (spaced(className <* op(':')) ~ spaced(types) ~ (identifier | inDiamonds(identifier)) ~ inParens((types <* op(',').?).rep0))
      .map { case (((className, returnType), name), paramTypes) =>
        MethodSignature(name,paramTypes,returnType, className)
      }

  // parsing field signatures
  val fieldSignatures: P[FieldSignature] =
    ((className <* op(':')) ~ types ~ identifier).map { case ((className, fieldType), name) => FieldSignature(name, fieldType, className)}

  // parsing exception ranges
  val exceptionRanges: P[ExceptionRange] =
    ((keyword(KCATCH) *> types) ~
      (keyword(KFROM) *> identifier) ~
      (keyword(KTO) *> identifier) ~
      (keyword(KWITH) *> identifier))
      .map {
        case (((excType: Type.RefT,from),to),using) =>
          ExceptionRange(excType, from, to, using)
        case _ => throw new IllegalArgumentException
      }

  // parsing all binary operators
  val binaryOperators: P[BinOp] =
    op('+').map(_ => BinOp.Add) |
      op('&').map(_ => BinOp.And) |
      keyword(KCMPG).map(_ => BinOp.Cmpg).backtrack |
      keyword(KCMPL).map(_ => BinOp.Cmpl).backtrack |
      keyword(KCMP).map(_ => BinOp.Cmp) |
      op('/').map(_ => BinOp.Div) |
      op('*').map(_ => BinOp.Mul) |
      op('|').map(_ => BinOp.Or) |
      op('%').map(_ => BinOp.Rem) |
      op(">>>").map(_ => BinOp.Ushr).backtrack |
      op("<<").map(_ => BinOp.Shl).backtrack |
      op(">>").map(_ => BinOp.Shr).backtrack |
      op('-').map(_ => BinOp.Sub) |
      op('^').map(_ => BinOp.Xor)

  // parsing all conditional operators
  val conditionalOperators: P[CondOp] =
    op("==").map(_ => CondOp.Eq) |
      op(">=").map(_ => CondOp.Ge) |
      op("<=").map(_ => CondOp.Le) |
      op("!=").map(_ => CondOp.Ne) |
      op('>').map(_ => CondOp.Gt) |
      op('<').map(_ => CondOp.Lt)

  // parsing all unary operators
  val unaryOperators: P[UnOp] =
    keyword(KLENGTHOF).map(_ => UnOp.Length) |
      op('-').map(_ => UnOp.Neg) |
      op("neg").map(_ => UnOp.NegWord)

  // parsing all elements on the left side of assignments
  val variables: P[Var] =
    (immediates ~ inBrackets(immediates)).backtrack.map((i1,i2) => Var.ArrayRefV(i1,i2)).backtrack |
      ((immediates <* op(".")) ~ inDiamonds(fieldSignatures)).backtrack.map((i,f) => Var.InstanceFieldRefV(i,f)).backtrack |
      inDiamonds(fieldSignatures).backtrack.map(Var.StaticFieldRefV.apply) |
      identifier.map(i => Var.LocalV(Local(i)))

  // parsing all expressions
  val expressions: P[Exp] =
    (immediates ~ binaryOperators ~ immediates)
      .backtrack.map {
      case ((i1,operator),i2) => Exp.BinopE(i1, i2, operator) }|
      (immediates ~ conditionalOperators ~ immediates)
        .backtrack.map{
        case ((i1, cond), i2) => Exp.ConditionE(i1, i2, cond)} |
      (inParens(types) ~ immediates).map((t,i) => Exp.CastE(t,i)) |
      (immediates ~ (keyword(KINSTANCEOF) *> types))
        .backtrack.map {
        case (i, t : (Type.RefT | Type.ArrayT)) => Exp.InstanceOfE(i, t)
        case _ => throw new IllegalArgumentException } |
      keyword(KSTATICINVOKE) *> (inDiamonds(methodSignatures) ~ inParens((immediates <* op(',').?).rep0))
        .map((m, ls) => Exp.StaticInvokeE(m, ls)) |
      (invokeTypes ~ immediates ~ (op('.') *> inDiamonds(methodSignatures)) ~ inParens((immediates <* op(',').?).rep0))
        .map{
          case (((t, i), m), ls) => Exp.InvokeE(t, i, m, ls)
        } |
      (keyword(KNEWARRAY) *> inParens(types) ~ inBrackets(immediates)).backtrack.map((t,i) => Exp.NewArrayE(t, i)) |
      (keyword(KNEW) *> types)
        .map{
          case t : Type.RefT => Exp.NewE(t)
          case _ => throw new IllegalArgumentException }
        .backtrack |
      (keyword(KNEWMULTIARRAY) *> inParens(types) ~ inBrackets(immediates.?).rep0)
        .map{
          case (t, dims) => Exp.NewMultArrE(t, dims)
        }.backtrack |
      (unaryOperators ~ immediates).map((operator, i) => Exp.UnopE(i, operator))

  // parsing all elements on the right side of assignments
  val rValues: P[RVal] =
    (keyword(KCLASS) *> inQuotes(classId <* semi.?)).map(n => RVal.ClassR(n)).backtrack |
      (immediates ~ inBrackets(immediates)).backtrack.map((i1,i2) => RVal.ArrayRefR(i1,i2)) |
      expressions.map(e => RVal.ExpressionR(e)) |
      constants.backtrack.map(RVal.ConstR.apply) |
      ((immediates <* op(".")) ~ inDiamonds(fieldSignatures)).backtrack.map((i,f) => RVal.InstanceFieldRefR(i,f)) |
      inDiamonds(fieldSignatures).map(RVal.StaticFieldRefR.apply) |
      identifier.map(i => RVal.LocalR(Local(i)))

  // parsing identity statements
  val identityStatements: P[Stmt.IdentityS] =
    (identifier ~ (op(":=") *> identityValues <* semi)).backtrack.map((s, iv) => Stmt.IdentityS(Local(s), iv))

  // parsing all statements
  val statements: P[Stmt] =
    keyword(KBREAKPOINT) *> semi.map(_ => Stmt.BreakpointS) |
      (variables ~ (op('=') *> rValues <* semi)).backtrack.map((l, r) => Stmt.AssignS(l, r)) |
      (identifier ~ (op(":=") *> identityValues <* semi)).backtrack.map{
        case (s, iv: IdentityVal.CaughtExcRef) => Stmt.ExceptionIdentityS(Local(s), iv)
        case (_, _) => throw new IllegalArgumentException } |
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
        keyword(KDEFAULT) *> op(':') *> keyword(KGOTO) *> identifier <* semi)) <* semi)
        .map {
          case (i, (ls, s)) => Stmt.LookupSwitchS(i, ls, s) } |
      keyword(KNOP) *> semi.map(_ => Stmt.NopS) |
      keyword(KRET).backtrack *> (identifier <* semi).backtrack.map(s => Stmt.RetS(Local(s))) |
      (keywordNoSpace(KRETURN) *> semi).backtrack.map(_ => Stmt.ReturnVoidS) |
      (keyword(KRETURN) *> (immediates <* semi)).map(Stmt.ReturnS.apply) |
      (keyword(KTABLESWITCH) *> inParens(immediates) ~ inBraces(cases.rep0 ~ (
        keyword(KDEFAULT) *> op(':') *> keyword(KGOTO) *> identifier <* semi)) <* semi)
        .map {
          case (i, (ls, s)) => Stmt.TableSwitchS(i, ls, s) } |
      keyword(KTHROW) *> (immediates <* semi).map(Stmt.ThrowS.apply) |
      (id <* op(':')).map(l => Stmt.LabelS(l)) |
      (exceptionRanges <* semi).map(Stmt.CatchS.apply)

  // parsing method headers
  val methodheaders: P[MethodHeader] =
    (((op("public") | op("private") | op("protected") | P.anyChar.peek) ~ keyword(KABSTRACT).? ~ keyword(KSTATIC).? ~ keyword(KFINAL).? ~ keyword(KSTRICTFP).? ~ keyword(KSYNCHRONIZED).? ~ keyword(KTRANSIENT).? ~ keyword(KVOLATILE).?).with1
      ~ types ~ (identifier | inDiamonds(identifier)) ~ inParens((types <* op(',').?).rep0) ~ (keyword(KTHROWS) *> (types <* op(',').?).rep0).?)
      .map{
        case (((((((((((_: Unit, isAbstract), isStatic), isFinal), isStrict), isSynchronized), isTransient), isVolatile), ret), id), params), throws: Option[Seq[Type.RefT]]) => MethodHeader(false, false, false, isStatic.isDefined, isFinal.isDefined, isStrict.isDefined, isSynchronized.isDefined, isTransient.isDefined, isVolatile.isDefined, isAbstract.isDefined, ret, id, params, throws.getOrElse(Seq.empty[Type.RefT]))
        case (((((((((((isP: String, isAbstract), isStatic), isFinal), isStrict), isSynchronized), isTransient), isVolatile), ret), id), params), throws: Option[Seq[Type.RefT]]) => MethodHeader(getVisibility(isP, 0), getVisibility(isP, 1), getVisibility(isP, 2), isStatic.isDefined, isFinal.isDefined, isStrict.isDefined, isSynchronized.isDefined, isTransient.isDefined,  isVolatile.isDefined, isAbstract.isDefined, ret, id, params, throws.getOrElse(Seq.empty[Type.RefT]))
        case _ => throw new IllegalArgumentException
      }.backtrack

  // parsing global variables
  val globalvars: P[ClassBodyElement.GlobalVarCB] =
    (((op("public") | op("private") | op("protected") | P.anyChar.peek) ~ keyword(KSTATIC).? ~ keyword(KFINAL).? ~ keyword(KENUM).? ~ keyword(KTRANSIENT).? ~ keyword(KVOLATILE).?).with1 ~ types ~ identifier <* semi)
      .backtrack.map{
      case (((((((_: Unit, isStatic), isFinal), isEnum), isTransient), isVolatile), t), id) => ClassBodyElement.GlobalVarCB(false, false, false, isStatic.isDefined, isFinal.isDefined, isEnum.isDefined, isTransient.isDefined, isVolatile.isDefined, t, id)
      case (((((((isP: String, isStatic), isFinal), isEnum), isTransient), isVolatile), t), id) => ClassBodyElement.GlobalVarCB(getVisibility(isP, 0), getVisibility(isP, 1), getVisibility(isP, 2), isStatic.isDefined, isFinal.isDefined, isEnum.isDefined, isTransient.isDefined, isVolatile.isDefined, t, id)
      case _ => throw new IllegalArgumentException
    }

  //parsing native method calls
  val nativecalls: P[ClassBodyElement.NativeCallCB] =
    (((op("public") | op("private") | op("protected") | P.anyChar.peek) ~ keyword(KSTATIC).? ~ keyword(KFINAL).? ~ keyword(KSYNCHRONIZED).? ~ (keyword(KNATIVE) *> keyword(KTRANSIENT).?)).with1 ~ types ~ identifier ~ inParens((types <* op(',').?).rep0) ~ (keyword(KTHROWS) *> (types <* op(',').?).rep0).? <* semi)
      .backtrack.map {
      case ((((((((_: Unit, isStatic), isFinal), isSynchronized), isTransient), t), id), params), except: Option[Type.RefT]) => ClassBodyElement.NativeCallCB(false, false, false, isStatic.isDefined, isFinal.isDefined, isSynchronized.isDefined, isTransient.isDefined, t, id, params, except)
      case ((((((((isP: String, isStatic), isFinal), isSynchronized), isTransient), t), id), params), except: Option[Type.RefT]) => ClassBodyElement.NativeCallCB(getVisibility(isP, 0), getVisibility(isP, 1), getVisibility(isP, 2), isStatic.isDefined, isFinal.isDefined, isSynchronized.isDefined, isTransient.isDefined, t, id, params, except)
      case _ => throw new IllegalArgumentException
    }

  // parsing methods
  val methods: P[ClassBodyElement.MethodCB] =
    (methodheaders ~ inBraces(localDeclarations.rep0 ~ identityStatements.rep0 ~
      statements.rep0 ~ exceptionRanges.rep0))
      .backtrack.map {
      case (header, (((locals, idStmts), stmts), exceptions)) =>
        ClassBodyElement.MethodCB(header, locals.flatten, idStmts, stmts, exceptions)
    }

  // parsing all class body elements, mainly by calling the respective helper functions
  val classbodyelements: P[ClassBodyElement] =
    globalvars.backtrack
      | nativecalls.backtrack
      | (methodheaders <* semi).map(ClassBodyElement.MethodHeaderCB.apply).backtrack
      | methods

  // parsing classes
  val classes: P[Container.ClassC] =
    ((((op("public") | op("private") | P.anyChar.peek) ~ keyword(KABSTRACT).? ~ keyword(KSTATIC).? ~ keyword(KFINAL).? ~ keyword(KENUM).?).with1 <* keyword(KCLASS)) ~ spaced(className) ~ (keyword(KEXTENDS) *> types).? ~ (keyword(KIMPLEMENTS) *> (types <* op(',').?).rep0).? ~ inBraces(classbodyelements.rep0) )
      .map{
        case ((((((((_: Unit, isAbstract), isStatic), isFinal), isEnum), id), extend: Option[Type.RefT]), implement: Option[Seq[Type.RefT]]), body: Seq[ClassBodyElement]) => Container.ClassC(false, false, isAbstract.isDefined, isStatic.isDefined, isFinal.isDefined, isEnum.isDefined, id, extend, implement.getOrElse(Seq.empty[Type.RefT]), body)
        case ((((((((isP: String, isAbstract), isStatic), isFinal), isEnum), id), extend: Option[Type.RefT]), implement: Option[Seq[Type.RefT]]), body: Seq[ClassBodyElement]) => Container.ClassC(getVisibility(isP, 0), getVisibility(isP, 1), isAbstract.isDefined, isStatic.isDefined, isFinal.isDefined, isEnum.isDefined, id, extend, implement.getOrElse(Seq.empty[Type.RefT]), body)
        case _ => throw new IllegalArgumentException
      }

  // parsing interfaces
  val interfaces: P[Container.InterfaceC] =
    ((((op("public") | op("private") | P.anyChar.peek) <* op("annotation").?).with1 <* keyword(KINTERFACE)) ~ spaced(className) ~ (keyword(KEXTENDS) *> types).? ~ (keyword(KIMPLEMENTS) *> (types <* op(',').?).rep0).? ~ inBraces(classbodyelements.rep0))
      .map{
        case ((((_: Unit, id), extend: Option[Type.RefT]), implement: Option[Seq[Type.RefT]]), body: Seq[ClassBodyElement]) => Container.InterfaceC(false, false, id, extend, implement.getOrElse(Seq.empty[Type.RefT]), body)
        case ((((isP: String, id), extend: Option[Type.RefT]), implement: Option[Seq[Type.RefT]]), body: Seq[ClassBodyElement]) => Container.InterfaceC(getVisibility(isP, 0), getVisibility(isP, 1), id, extend, implement.getOrElse(Seq.empty[Type.RefT]), body)
        case _ => throw new IllegalArgumentException
      }

  // parsing all containers by calling their respective helper functions
  val containers: P[Container] =
    classes.backtrack | interfaces

  // parsing a program by parsing all its containers
  val programs: P0[Program] =
    whitespaces0 *> containers.rep0.map(Program.apply) <* P.end
