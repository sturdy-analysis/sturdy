package sturdy.language.jimple

import org.scalatest.flatspec.AnyFlatSpec
import cats.parse.Parser as P
import Parser.*
import java.net.URI

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

// Testing the Parser Jimple -> Syntax
class ParserTest extends AnyFlatSpec:
  behavior of "Jimple parser"

  // path to the jdk jimple files
  val jdkUri: URI = classOf[ParserTest].getResource("/sturdy/language/jimple/jdk").toURI

  // path to the test jimple files
  val testsUri: URI = classOf[ParserTest].getResource("/sturdy/language/jimple").toURI

  // Testing whether one file returns the exact right result
  it must s"Test.jimple must parse into Test.ast.txt" in{
    // path to specific test files
    val file = Source.fromURI(classOf[ParserTest].getResource("/sturdy/language/jimple/Test.jimple").toURI)
    // reading the code to a string
    val sourceCode = file.getLines().mkString("\n")
    file.close()

    // path to the expected results file
    val compareFile = Source.fromURI(classOf[ParserTest].getResource("/sturdy/language/jimple/Test.ast.txt").toURI)
    // reading the expected results to a string
    val compareAST = compareFile.getLines().mkString("\n")
    compareFile.close()

    // parsing the source code
    val tree = parse(sourceCode)

    // Equality assertion between results and expected results
    assert(tree.toString.equals(compareAST))
  }

  // parsing all files of the jdk rt.jar
  Files.list(Paths.get(jdkUri)).toScala(List).filter(p => p.toString.endsWith(".jimple")).sorted.foreach { p =>
    runTest(p)
  }

  // parsing all test files
  Files.list(Paths.get(testsUri)).toScala(List).filter(p => p.toString.endsWith(".jimple")).sorted.foreach { p =>
    runTest(p)
  }

  def runTest(p: Path): Unit =
    it must s"parse ${p.getFileName}" in {
      // reading file to a string
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      // parsing source code
      val tree = parse(sourceCode)

      // assertion that parsing did not fail
      assert(tree.isRight)
    }

  // Calling the parse function from the parser.
  def parse(s: String): Either[P.Error, Program] =
    Parser.programs.parseAll(s)
