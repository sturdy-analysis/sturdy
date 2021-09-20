package sturdy.language.wasm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class TestScriptInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "TestScript interpreter"

  val uri = classOf[TestScriptParserTest].getResource("/sturdy/language/wasm/scripts").toURI();

  Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".wast")).foreach { p =>
    it must s"execute ${p.getFileName}" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val script = parseScript(sourceCode)
      val interp = TestScriptInterpreter()
      interp.run(script)
    }
  }

