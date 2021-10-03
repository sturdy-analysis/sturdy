package sturdy.language.minijava

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.minijava.Parser.LanguageKeywords.KRETURN
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import Parser.*

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class ParserTest extends AnyFlatSpec, Matchers:
  behavior of "MiniJava parser"


  /*

  val ListOfTypes = List( "int[]", "int", "someType", "boolean" )
  for (a <- ListOfTypes){
    val tree = parseType(a)
    println(tree)
    //assert(tree.isRight)
  }



  val ListOfVars = List("xyz", "VaR7a8Le23A", "some_Name")
  for (a <- ListOfVars){
    val tree = parseVar(a)
    println(tree)
    //assert(tree.isRight)
  }


  val ListOfAtoms = List("1","-4", "(true)", "false", "new int[4+3]", "new someObject()")
  for (a <- ListOfAtoms){
    val tree = parseAtom(a)
    println(tree)
    //assert(tree.isRight)
  }

*/
  val ListOfFunCall = List( "(someArry).length","Object.do(10)", "new Object.do()")
  for (a <- ListOfFunCall){
    val tree = parseFunCall(a)
    println(tree)
    //assert(tree.isRight)
  }


  /*
  val ListOfAccess = List("x[1]", "someIterable[index]" )

  for (a <- ListOfAccess){
    val tree = parseAccess(a)
    println(tree)
    //assert(tree.isRight)
  }


*/

  val ListOfTerms = List(
    "mylist[20]",
    "42",
    "6*x",
    "100/5",
    "a",
    "!true")

  for (a <- ListOfTerms){
    val tree = parseTerm(a)
    println(tree)
    //assert(tree.isRight)
  }

  /*

  val ListOfOps = List("x * y + 5", "42", "true && false", "(a + b) * 2", "!false && true", "var1 || var2" )
  for (a <- ListOfOps){
    val tree = parseOp(a)
    println(tree)
    //assert(tree.isRight)
  }


  val ListOfExpr = List("new Fac().ComputeFac(10)",  "x.methode(1)", "expr.length", "5 == true", "10> 9"  )
  for (a <- ListOfExpr){
    val tree = parseExp(a)
    println(tree)
    //assert(tree.isRight)
  }



  val ListOfAss = List( "x", "myList[1+2]"  )
  for (a <- ListOfAss){
    val tree = parseAss(a)
    println(tree)
    //assert(tree.isRight)
  }



  val ListOfStm = List( "System.out.println(1+2);",
                        "a = 1;",
                        "a[4] = 1;",
                        "b=c;",
                        "a = new Foo();",
                        "if(true){a = 1; b = 2;} else{a =0; b = -1;}",
                        "while(x>10){x = x - 1;}",
    "{a = 1; x = a; if(x == a){System.out.println(1);}}")
  for (a <- ListOfStm){
    val tree = parseStm(a)
    println(tree)
    //assert(tree.isRight)
  }




  val ListOfvarDec = List( "int x;",
                        "int[] myArray;",
                        "boolean y;",
                        "somePredefinedType foo ; ",
  )
  for (a <- ListOfvarDec){
    val tree = parseVarDec(a)
    println(tree)
    //assert(tree.isRight)
  }




  val ListOfFun = List( "public int foo(int x, boolean y){ int y; if(x == 1){y = x +1;} return y; }",
    "public int ComputeFac(boolean b){ int num_aux; if(b == true){numAux = 1;} else {numAux = 2;} return numAux; }" )
  for (a <- ListOfFun){
    val tree = parseFun(a)
    println(tree)
    //assert(tree.isRight)
  }




  val ListOfMainFun = List( "public static void main(String[] arg ){ }" )
  for (a <- ListOfMainFun){
    val tree = parseMainFun(a)
    println(tree)
    //assert(tree.isRight)
  }




  val ListOfClass = List( "class Fac extends Baz { int res; boolean b; public int ComputeFac(int num){ int num_aux; if(num > 1){num_aux = 1;} else {num_aux = 2;} return num_aux; } }",
    "class VeryHeavy { public int foo(int arg ){ System.out.println(arg);return arg;}}")
  for (a <- ListOfClass){
    val tree = parseClass(a)
    println(tree)
    //assert(tree.isRight)
  }


  val ListOfMainClass = List( "class Simple {public static void main(String[] arg ){ System.out.println(1);}}")
  for (a <- ListOfMainClass){
    val tree = parseMainClass(a)
    println(tree)
    //assert(tree.isRight)
  }

*/



    val uri = classOf[ParserTest].getResource("/sturdy/language/minijava").toURI();

   Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".minijava")).foreach { p =>
     it must s"execute ${p.getFileName}" in {
       val file = Source.fromURI(p.toUri)
       val sourceCode = file.getLines().mkString("\n")
       file.close()
       val tree = parse(sourceCode)
       println(tree)
       assert(tree.isRight)
     }
   }



  def parse(s: String): Either[P.Error, Program] =
    Parser.program.parseAll(s)


  def parseVar(s:String): Either[P.Error, Exp] =
    Parser.variable.parseAll(s)

  def parseType(s:String): Either[P.Error, Type] =
    Parser.types.parseAll(s)

  def parseAtom(s:String): Either[P.Error, Exp] =
    Parser.atom.parseAll(s)

  def parseAccess(s:String): Either[P.Error, Exp] =
    Parser.access.parseAll(s)

  def parseFunCall(s:String): Either[P.Error, Exp] =
    Parser.funCall.parseAll(s)

  def parseTerm(s:String): Either[P.Error, Exp] =
    Parser.term.parseAll(s)

  def parseOp(s:String): Either[P.Error, Exp] =
    Parser.operation.parseAll(s)


  def parseExp(s:String): Either[P.Error, Exp] =
    Parser.expression.parseAll(s)

  def parseStm(s:String): Either[P.Error, Stm] =
    Parser.statement.parseAll(s)


  def parseAss(s:String): Either[P.Error, Assignable] =
    Parser.assignable.parseAll(s)

  def parseVarDec(s:String): Either[P.Error, varDeclaration] =
    Parser.varDecl.parseAll(s)

  def parseClass(s:String): Either[P.Error, classDeclaration] =
    Parser.Class.parseAll(s)

  def parseMainClass(s:String): Either[P.Error, mainClass] =
    Parser.MainClass.parseAll(s)


  def parseFun(s:String): Either[P.Error, Function] =
    Parser.function.parseAll(s)

  def parseMainFun(s:String): Either[P.Error, MainFunction] =
    Parser.main_methode.parseAll(s)