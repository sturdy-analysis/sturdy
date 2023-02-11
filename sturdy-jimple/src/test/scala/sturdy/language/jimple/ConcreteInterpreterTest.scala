package sturdy.language.jimple

import sturdy.effect.symboltable.ConcreteSymbolTable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import Parser.*

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

// Testing the concrete and thereby the generic interpreter
class ConcreteInterpreterTest extends AnyFlatSpec, Matchers {
  behavior of "Jimple concrete interpreter"

  // defining the expected results of all tests as array of strings
  val expectedResults : Array[String] =
    Array(
      "((),List(ObjectValue(Map()), ObjectValue(Map()), ObjectValue(Map()), ObjectValue(Map()), IntConstValue(42), ObjectValue(Map())))",
      "((),List(ObjectValue(Map()), IntConstValue(3), IntConstValue(2)))"
    )

  // loading paths of all needed jimple files
  val jdk : Path = Paths.get(classOf[ConcreteInterpreterTest].getResource("/sturdy/language/jimple/jdk").toURI)
  val addition: Path = Paths.get(classOf[ParserTest].getResource("/sturdy/language/jimple/Addition.jimple").toURI)
  val mini: Path = Paths.get(classOf[ParserTest].getResource("/sturdy/language/jimple/Mini.jimple").toURI)

  // running the tests
  testFile(addition, 0)
  testFile(mini, 1)

  // function to run the concrete interpreter test on file at path p with expected results expectedResults(i)
  def testFile(p: Path, i: Int): Unit =
    it must s"correctly execute ${p.getFileName}" in {
      // declarations for initial class and runtime tables
      val classTable: ConcreteSymbolTable[String, String, Container] = new ConcreteSymbolTable[String, String, Container]
      val runtimeTable: ConcreteSymbolTable[String, String, RuntimeUnit] = new ConcreteSymbolTable[String, String, RuntimeUnit]
      // filling class table and runtime table with all jdk classes
      parseJDK(classTable, runtimeTable)

      // generating the program and loading it into the class and runtime tables
      val program = parseAndLoad(p, classTable, runtimeTable)
      // instantiating the concrete interpreter with the pre-filled tables
      val concInterp = ConcreteInterpreter(Map(), classTable, runtimeTable)

      // evaluating the program
      val result = concInterp.evalProgram(program)
      // equality assertion with the expected results
      assert(result.equals(expectedResults(i)))
    }

  // helper function to fill the tables with all elements of the jdk
  def parseJDK(classTable: ConcreteSymbolTable[String, String, Container], runtimeTable: ConcreteSymbolTable[String, String, RuntimeUnit]): Unit =
    Files.list(jdk).toScala(List).filter(p => p.toString.endsWith(".jimple")).sorted.foreach { p =>
      parseAndLoad(p, classTable, runtimeTable)
    }

  // helper function to split up the body of a class into its globals and methods
  def splitUpBody(className: String, body: collection.Seq[ClassBodyElement]): (Map[QName, ClassBodyElement.GlobalVarCB], Map[QName, ClassBodyElement.MethodCB]) =
    var fields = Map[QName, ClassBodyElement.GlobalVarCB]()
    var methods = Map[QName, ClassBodyElement.MethodCB]()

    // each body element is evaluated and put into the right map
    body.foreach{ bodyEl =>
      bodyEl match
        case c:ClassBodyElement.GlobalVarCB => c match
          case ClassBodyElement.GlobalVarCB(_, _, _, _, _, _, _, _, _, id) =>
            fields = fields + ((className, id) -> c)
        case c:ClassBodyElement.MethodCB => c match
          case ClassBodyElement.MethodCB(_, _, _, _, _) =>
            methods = methods + ((className, bodyEl.getID) -> c)
        case ClassBodyElement.MethodHeaderCB(_) =>
          ()
        case ClassBodyElement.NativeCallCB(_,_,_,_,_,_,_,_,_,_,_) =>
          ()
        case null =>
          println(bodyEl.toString)
          throw new IllegalArgumentException(s"A ClassBodyElement is expected.")

    }
    (fields, methods)

  // function for parsing a file at path p and loading the classes into the passed tables, returning the program that should be evaluated
  def parseAndLoad(p: Path, classTable: ConcreteSymbolTable[String, String, Container], runtimeTable: ConcreteSymbolTable[String, String, RuntimeUnit]): Program =
    // getting the source code from the file
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()

    // parsing the file
    val parsed = parse(sourceCode)
    // filling the tables with the elements of each container
    parsed.funs.foreach{ c =>
      val ident = c.getID
      classTable.putNew(ident)
      runtimeTable.putNew(ident)
      classTable.set(ident, ident, c)
      c match {
        case Container.ClassC(_, _, isAbstract, _, _, _, _, _, _, body) =>
          val bodyEls = splitUpBody(ident, body)
          val runtimeUnit = RuntimeUnit(ident, bodyEls._1, bodyEls._2, isAbstract)
          runtimeTable.set(ident, ident, runtimeUnit)
        case Container.InterfaceC(_, _, _, _, _, body) =>
          val bodyEls = splitUpBody(ident, body)
          val runtimeUnit = RuntimeUnit(ident, bodyEls._1, bodyEls._2, true) // interfaces are just like abstract classes
          runtimeTable.set(ident, ident, runtimeUnit)
      }
    }
    parsed
}
