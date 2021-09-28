package sturdy.language.wasm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class TestScriptInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "TestScript interpreter"

  val pathSpectest = Paths.get(classOf[TestScriptParserTest].getResource("/sturdy/language/wasm/spectest.wast").toURI())
  val uri = classOf[TestScriptParserTest].getResource("/sturdy/language/wasm/scripts").toURI();

  val spectest = parse(pathSpectest)

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val script = parseScript(sourceCode)
      val interp = TestScriptInterpreter(Some(spectest))
      interp.run(script)
    }
  }

