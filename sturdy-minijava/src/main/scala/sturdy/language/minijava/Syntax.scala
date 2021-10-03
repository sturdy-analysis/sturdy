package sturdy.language.minijava

import sturdy.util.Labeled
import sturdy.values.Structural

enum Exp extends Labeled:
  case NumLit(n: Int)                   // <INTEGER_LITERAL>
  case Var(name: String)                // Identifier
  case Add(e1: Exp, e2: Exp)            // Expression ("+") Expression
  case Sub(e1: Exp, e2: Exp)            // Expression ("-") Expression
  case Mul(e1: Exp, e2: Exp)            // Expression ("*") Expression
  case Div(e1: Exp, e2: Exp)            // Expression ("/") Expression
  case Gt(e1: Exp, e2: Exp)             // Expression ("<") Expression
  case Eq(e1: Exp, e2: Exp)             // Expression ("==") Expression
  case Call(obj : Exp, name: String, args: Seq[Exp])   // Expression.Identifier((Expression(,Expression)*)?)
  case Alloc(name: String)                // new Identifier()

  case AllocArray(e: Exp)                 // new int [Expression]
  case AccessArray(e1: Exp, e2: Exp)      // Expression[Expression]
  case ArrayLength(e: Exp)                // Expression.length
  case BoolLit(b: String)  //case BoolLit(b: Boolean)                 // true und false

  // this expression                      // "this"
  case This(u:Unit)

  case Not (e: Exp)                       // !Expression
  case And (e1: Exp, e2: Exp)             // Expression ("&&") Expression
  case Or (e1: Exp, e2: Exp)              //Expression ("||") Expression



  def intLiterals: Set[Int] = this match
    case NumLit(n: Int) => Set(n)
    case Add(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Sub(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Mul(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Div(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Eq(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Gt(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case Call(fun: Exp, name: String, args: Seq[Exp]) => fun.intLiterals ++ args.flatMap(_.intLiterals)

    case AllocArray(e: Exp) => e.intLiterals
    case AccessArray(e1: Exp, e2: Exp) => e1.intLiterals ++ e2.intLiterals
    case ArrayLength(e: Exp) => e.intLiterals
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
    case Alloc(name) => s"$name@${this.label}"

    case AllocArray(e) => s"allocArray@${this.label}"
    case AccessArray(e1,e2) => s"accessArray@${this.label}"
    case ArrayLength(e) => s"arrayLength@${this.label}"

    case BoolLit(b) => s"$b@${this.label}"
    case Not(e) => s"logicalNOT@${this.label}"
    case And(e1,e2) => s"logicalAND@${this.label}"
    case Or(e1,e2) => s"logicalOr@${this.label}"

    case This(u) => "this"


enum Stm extends Labeled:
  case Assign(lhs: Assignable, e: Exp)
  case If(cond: Exp, thn: Stm, els: Option[Stm])
  case While(cond: Exp, body: Stm)
  case Block(body: Seq[Stm])
  case Output(e: Exp)

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
  case AArray(name: String, e: Exp)

enum Type:
  case Void() // case Void()
  case Int() // case Int(n: Int)
  case Boolean() // case Boolean(b: Boolean)
  case IntArray() //IntArray(a: Array[Int])
  case Identifier(name: String) //case Identifier(name: String)

// varDeclarations haben identifier und type
case class varDeclaration(t: Type, name: String)

// methodDeclarations haben identifier, return type, mehrere Identifier,Type paare für arguments
// außerdem mehrere varDeclarations und statements und eine return EXP
case class Function(retType: Type, name: String,  params: Seq[Tuple2[Type,String]], locals: Seq[varDeclaration], body: Stm, ret: Exp):
  def intLiterals: Set[Int] = body.intLiterals ++ ret.intLiterals

// MainMethode
case class MainFunction(arg: String, locals: Seq[varDeclaration],  body: Stm):
  def intLiterals: Set[Int] = body.intLiterals

// Class declarations haben identifier, können erben, mehrere varDeclarations und MethodDeclarations
case class classDeclaration(name: String, extend: Option[String], locals: Seq[varDeclaration], funs: Seq[Function]):
  def intLiterals: Set[Int] = funs.flatMap(_.intLiterals).toSet

// Main Class
case class mainClass(name: String, mainFun : MainFunction):
  def intLiterals: Set[Int] = mainFun.intLiterals

// Program besteht aus MainClass und ClassDeclarations
case class Program(main: mainClass, classes: Seq[classDeclaration]):
  def intLiterals: Set[Int] = main.intLiterals ++ classes.flatMap(_.intLiterals).toSet