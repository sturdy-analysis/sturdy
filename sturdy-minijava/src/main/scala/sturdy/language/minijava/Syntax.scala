package sturdy.language.minijava

import sturdy.util.Labeled
import sturdy.values.Structural

// Expressions
enum Exp extends Labeled:
  // Integer Literal
  case NumLit(n: Int)                   // <INTEGER_LITERAL>
  // Variable Identifier
  case Var(name: String)                // Identifier
  // Addition
  case Add(e1: Exp, e2: Exp)            // Expression ("+") Expression
  // Subtraction
  case Sub(e1: Exp, e2: Exp)            // Expression ("-") Expression
  // Multiplication
  case Mul(e1: Exp, e2: Exp)            // Expression ("*") Expression
  // Division, ??? is it there?
  case Div(e1: Exp, e2: Exp)            // Expression ("/") Expression
  // Greater Than
  case Gt(e1: Exp, e2: Exp)             // Expression ("<") Expression
  // Equals doesnt exist?
  case Eq(e1: Exp, e2: Exp)
  // Funktioniert call genau so? In TIP ID(EXP...) in miniJava Exp.Id(EXP...)?
  case Call(fun: Exp, name: String, args: Seq[Exp])   // Expression.Identifier((Expression(,Expression)*)?)
  // Alloc in miniJava? vllt new Identifier ()? braucht aber identifier nicht expression
  //case Alloc(e: Exp)
  //Das scheint nicht richtig... new Identifier() ?

  // Wie modelliert man Array expressions?
  // Irgendwie arrays allokieren nach new int[EXP], wie funktionieren static types in der syntax?
  case AllocArray(e: Exp)                 // new int [Expression]
  // Access auf array?
  case AccessArray(e1: Exp, e2: Exp)      // Expression[Expression]
  // Array length?
  case ArrayLength(e: Exp)                // Expression.length

  // Bool constant expressions?
  case BoolLit(b: Boolean)                // true und false?

  // this expression???                   // "this" ?????
  case NullRef()
  // NOT and AND Expressions?
  case Not (e: Exp)                       // !Expression
  case And (e1: Exp, e2: Exp)             // Expression ("&&") Expression


  // Was bedeutet (Expression) ? Auch in TIP

  // Input doesnt exist?
  //case Input()
  // reference variable names doesnt exist I think
  //case VarRef(name: String)
  // same for dereference?
  //case Deref(e: Exp)
  // and for nullref?
  // Existieren records so? Classes sind praktisch records? Glaube nicht
  //case Record(fields: Seq[(String, Exp)])
  // keine records?
  //case FieldAccess(rec: Exp, field: String)


  // Was genau passiert hier?
  def intLiterals: Set[Int] = this match
    case NumLit(n: Int) => Set(n)
    case Add(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Sub(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Mul(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Div(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Eq(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Gt(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Call(fun: Exp, name: String, args: Seq[Exp]) => fun.intLiterals ++ args.flatMap(_.intLiterals)

    // Array syntax
    case AllocArray(e: Exp) => e.intLiterals
    case AccessArray(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case ArrayLength(e: Exp) => e.intLiterals
    case _ => Set()

    //case Alloc(e: Exp) => e.intLiterals

  //case Deref(e: Exp) => e.intLiterals
    //case Record(fields: Seq[(String, Exp)]) => fields.flatMap(f => f._2.intLiterals).toSet
    //case FieldAccess(rec: Exp, field: String) => rec.intLiterals

  // Brauchen wir das gleiche für bool Literals?
  def boolLiterals: Set[Boolean] = this match
    case BoolLit(b: Boolean) => Set(b)
    case Not(e: Exp) => e.boolLiterals
    case And(e1: Exp, e2: Exp) => e1.boolLiterals ++ e2.boolLiterals
    case Eq(e1: Exp, e2: Exp) => e1.boolLiterals ++ e2.boolLiterals
    case Call(fun: Exp, name: String, args: Seq[Exp]) => fun.boolLiterals ++ args.flatMap(_.boolLiterals)
    case _ => Set()

  override def toString: String = this match
    case NumLit(n) => s"$n@${this.label}"
    case Var(name) => s"$name@${this.label}"
    case Add(e1, e2) => s"Add@${this.label}"
    case Sub(e1, e2) => s"Sub@${this.label}"
    case Mul(e1, e2) => s"Mul@${this.label}"
    case Div(e1, e2) => s"Div@${this.label}"
    case Gt(e1, e2) => s"Gt@${this.label}"
    case Eq(e1, e2) => s"Eq@${this.label}"
    case Call(Var(fun), name, args) => s"Call($fun)@${this.label}"
    case Call(fun, name, args) => s"Call@${this.label}"
    //case Alloc(name) => s"$name@${this.label}"

    // Gleicher Stuff hier
    case AllocArray(e) => s"allocArray@${this.label}"
    case AccessArray(e1,e2) => s"accessArray@${this.label}"
    case ArrayLength(e) => s"arrayLength@${this.label}"

    case BoolLit(b) => s"$b@${this.label}"
    case Not(e) => s"logicalNOT@${this.label}"
    case And(e1,e2) => s"logicalAND@${this.label}"
    case NullRef() => s"Null@${this.label}"

//case Input() => s"Input@${this.label}"
    //case VarRef(name: String) => s"&$name@${this.label}"
    //case Deref(e) => s"Deref@${this.label}"
    //case NullRef() => s"Null@${this.label}"
    //case Record(fields: Seq[(String, Exp)]) => s"Record@${this.label}"
    //case FieldAccess(rec: Exp, field: String) => s"FieldAccess@${this.label}"


// Müssen wir hier bei den Statements darauf achten das miniJava einen expliziten Boolean Typ hat?
enum Stm extends Labeled:
  // Assign, IF und WHILE scheinen übernehmbar
  case Assign(lhs: Assignable, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  // Das hier ist einfach für viele Statements hintereinander?
  case Block(body: Seq[Stm])
  // Output ist explizit sysout, muss man das beachten?
  case Output(e: Exp)

  // Gibt es hier auch spezielle Error statements?
  //case Error(e: Exp)

  def intLiterals: Set[Int] = this match
    case Assign(_, e) => e.intLiterals
    case If(c, t, e) => c.intLiterals ++ t.intLiterals ++ e.map(_.intLiterals).getOrElse(Set())
    case While(c, b) => c.intLiterals ++ b.intLiterals
    case Block(body) => body.flatMap(_.intLiterals).toSet
    case Output(e) => e.intLiterals
    //case Error(e) => e.intLiterals

  def boolLiterals: Set[Boolean] = this match
    case Assign(_, e) => e.boolLiterals
    case If(c, t, e) => c.boolLiterals ++ t.boolLiterals ++ e.map(_.boolLiterals).getOrElse(Set())
    case While(c, b) => c.boolLiterals ++ b.boolLiterals
    case Block(body) => body.flatMap(_.boolLiterals).toSet
    case Output(e) => e.boolLiterals;

  override def toString: String = this match
    case Assign(x, _) => s"Assign(x)@${this.label}"
    case If(c, t, e) => s"If(c)@${this.label}"
    case While(c, b) => s"While(c)@${this.label}"
    case Block(body) => s"Block@${this.label}"
    case Output(e) => s"Output@${this.label}"
    //case Error(e) => s"Error@${this.label}"

enum Assignable:
  case AVar(name: String)
  // Wir brauchen noch ein Array assignable oder? Array sind ja an variables gebunden
  // aber wir müssen auch eine Expression für den offset evaluieren?
  case AArray(name: String, e: Exp)

  //case ADeref(e: Exp)
  // Wie genau funktionieren Fields und dereferences in miniJava? Wir haben keine expliziten Pointer
  // Funktioniert das hier für memberVariablen von Klassen?
  //case AField(rec: String, field: String)
  //case ADerefField(rec: Exp, field: String)

/* Wie genau wird mit den statischen Typen umgegangen? Z.b. für VarDeclaration?
  Brauchen wir einen enum für Typen? */


// Program besteht aus MainClass und ClassDeclarations
case class Program(main: mainClass, classes: Seq[classDeclaration]):
  def intLiterals: Set[Int] = main.intLiterals ++ classes.flatMap(_.intLiterals).toSet
  def boolLiterals: Set[Boolean] = main.boolLiterals ++ classes.flatMap(_.boolLiterals).toSet


// Main Class mit Identifier, psvm, String[args] und statements
// Nur ein statement?
case class mainClass(params: Seq[String], body: Stm):
  def intLiterals: Set[Int] = body.intLiterals
  def boolLiterals: Set[Boolean] = body.boolLiterals

// Class declarations haben identifier, können erben, mehrere varDeclarations und MethodDeclarations
case class classDeclaration(name: String, extend: String, locals: Seq[varDeclaration], funs: Seq[Function]):
  def intLiterals: Set[Int] = funs.flatMap(_.intLiterals).toSet
  def boolLiterals: Set[Boolean] = funs.flatMap(_.boolLiterals).toSet
  // Wie wird mit der Vererbung umgegangen?

// varDeclarations haben identifier und type
// Wie wird mit den Typen umgegangen?
case class varDeclaration()

// methodDeclarations haben identifier, type, mehrere Identifier,Type paare für arguments
// außerdem mehrere varDeclarations und statements und eine return EXP
case class Function(name: String, params: Seq[String], locals: Seq[varDeclaration], body: Stm, ret: Exp):
  override def toString: String = s"function $name"

  def intLiterals: Set[Int] = body.intLiterals ++ ret.intLiterals
  def boolLiterals: Set[Boolean] = body.boolLiterals ++ ret.boolLiterals


