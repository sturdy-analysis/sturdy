package sturdy.language.minijava

import sturdy.util.Labeled
import sturdy.values.Structural

// Expressions
enum Exp extends Labeled:
  // Integer Literal
  case NumLit(n: Int)
  // Input doesnt exist?
  //case Input()
  // Variable Identifier
  case Var(name: String)
  // Addition
  case Add(e1: Exp, e2: Exp)
  // Subtraction
  case Sub(e1: Exp, e2: Exp)
  // Multiplication
  case Mul(e1: Exp, e2: Exp)
  // Division, ??? is it there?
  case Div(e1: Exp, e2: Exp)
  // Greater Than; miniJava Syntax shows a LT, is this an issue?
  case Gt(e1: Exp, e2: Exp)
  // Equals doesnt exist?
  //case Eq(e1: Exp, e2: Exp)
  // Funktioniert call genau so? In TIP ID(EXP...) in miniJava Exp.Id(EXP...)?
  case Call(fun: Exp, args: Seq[Exp])
  // Alloc in miniJava? vllt new Identifier ()? braucht aber identifier nicht expression
  //case Alloc(e: Exp)
  case Alloc(name: String) //Das scheint nicht richtig....
  // reference variable names doesnt exist I think
  //case VarRef(name: String)
  // same for dereference?
  //case Deref(e: Exp)
  // and for nullref?
  //case NullRef()
  // Existieren records so? Classes sind praktisch records?
  case Record(fields: Seq[(String, Exp)])
  // Class functions sind field accesses?
  case FieldAccess(rec: Exp, field: String)

  // Wie modelliert man Array expressions?
  // Irgendwie arrays allokieren nach new int[EXP], wie funktionieren static types in der syntax?
  case AllocArray(e: Exp)
  // Access auf array?
  case AccessArray(e1: Exp, e2: Exp)
  // Array length?
  case ArrayLength(e: Exp)

  // Bool constant expressions?
  case BoolLit(b: Boolean)

  // this expression???

  // NOT and AND Expressions?
  case Not (e: Exp)
  case And (e1: Exp, e2: Exp)

  // Was bedeutet (Expression) ? Auch in TIP

  // Was genau passiert hier?
  def intLiterals: Set[Int] = this match
    case NumLit(n: Int) => Set(n)
    case Add(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Sub(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Mul(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Div(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Gt(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    //case Eq(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Call(fun: Exp, args: Seq[Exp]) => fun.intLiterals ++ args.flatMap(_.intLiterals)
    //case Alloc(e: Exp) => e.intLiterals
    //case Deref(e: Exp) => e.intLiterals
    case Record(fields: Seq[(String, Exp)]) => fields.flatMap(f => f._2.intLiterals).toSet
    case FieldAccess(rec: Exp, field: String) => rec.intLiterals

    // Array syntax
    case AllocArray(e: Exp) => e.intLiterals
    case AccessArray(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case ArrayLength(e: Exp) => e.intLiterals
    case _ => Set()

  // Brauchen wir das gleiche für bool Literals? oder geht das expliziter mit true und false?
  def boolLiterals: Set[Boolean] = this match
    case BoolLit(b: Boolean) => Set(b)
    case Not(e: Exp) => e.boolLiterals
    case And(e1: Exp, e2: Exp) => e1.boolLiterals ++ e2.boolLiterals
    case _ => Set()

  override def toString: String = this match
    case NumLit(n) => s"$n@${this.label}"
    //case Input() => s"Input@${this.label}"
    case Var(name) => s"$name@${this.label}"
    case Add(e1, e2) => s"Add@${this.label}"
    case Sub(e1, e2) => s"Sub@${this.label}"
    case Mul(e1, e2) => s"Mul@${this.label}"
    case Div(e1, e2) => s"Div@${this.label}"
    case Gt(e1, e2) => s"Gt@${this.label}"
    //case Eq(e1, e2) => s"Eq@${this.label}"
    case Call(Var(fun), args) => s"Call($fun)@${this.label}"
    case Call(fun, args) => s"Call@${this.label}"
    case Alloc(name) => s"$name@${this.label}"
    //case VarRef(name: String) => s"&$name@${this.label}"
    //case Deref(e) => s"Deref@${this.label}"
    //case NullRef() => s"Null@${this.label}"
    case Record(fields: Seq[(String, Exp)]) => s"Record@${this.label}"
    case FieldAccess(rec: Exp, field: String) => s"FieldAccess@${this.label}"

    // Gleicher Stuff hier
    case AllocArray(e) => s"allocArray@${this.label}"
    case AccessArray(e1,e2) => s"accessArray@${this.label}"
    case ArrayLength(e) => s"arrayLength@${this.label}"

    case BoolLit(b) => s"$b@${this.label}"
    case Not(e) => s"logicalNOT@${this.label}"
    case And(e1,e2) => s"logicalAND@${this.label}"

// Müssen wir hier bei den Statements darauf achten das miniJava einen expliziten Boolean Typ hat?
enum Stm extends Labeled:
  // Assign, IF und WHILE scheinen übernehmbar
  case Assign(lhs: Assignable, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  // Das hier ist einfach für viele Statements hintereinander?
  case Block(body: Seq[Stm])
  // Output ist explizit sysout für ints, muss man das beachten?
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

  override def toString: String = this match
    case Assign(x, _) => s"Assign(x)@${this.label}"
    case If(c, t, e) => s"If(c)@${this.label}"
    case While(c, b) => s"While(c)@${this.label}"
    case Block(body) => s"Block@${this.label}"
    case Output(e) => s"Output@${this.label}"
    //case Error(e) => s"Error@${this.label}"

enum Assignable:
  case AVar(name: String)
  //case ADeref(e: Exp)
  // Wie genau funktionieren Fields und dereferences in miniJava? Wir haben keine expliziten Pointer
  // Funktioniert das hier für memberVariablen von Klassen?
  case AField(rec: String, field: String)
  //case ADerefField(rec: Exp, field: String)
  // Wir brauchen noch ein Array assignable oder? Array sind ja an variables gebunden
  // aber wir müssen auch eine Expression für den offset evaluieren?
  case AArray(name: String, e: Exp)

/* Wie genau wird mit den statischen Typen umgegangen? Z.b. für VarDeclaration?
  Brauchen wir einen enum für Typen? */


// Program besteht aus MainClass und ClassDeclarations
case class Program(main: mainClass, classes: Seq[classDeclaration])
  //def intLiterals: Set[Int] = mainClass.intLiterals ++ classes.flatMap(_.intLiterals).toSet
// Main Class mit Identifier, psvm, String[args] und statements
case class mainClass()

// Class declarations haben identifier, können erben, mehrere varDeclarations und MethodDeclarations
case class classDeclaration()

// varDeclarations haben identifier und type
case class varDeclaration()

// methodDeclarations haben identifier, type, mehrere Identifier,Type paare für arguments
// außerdem mehrere varDeclarations und statements und eine return EXP
case class Function()