package sturdy.language.wasm.constant

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.wasm.TestScriptParserTest
import sturdy.language.wasm.parse
import sturdy.language.wasm.parseScript

import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.StreamConverters.*

class TestScriptConstantAnalysisInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "TestScript constant analysis"

  val pathSpectest = Paths.get(classOf[TestScriptParserTest].getResource("/sturdy/language/wasm/spectest.wast").toURI())
  val uri = classOf[TestScriptParserTest].getResource("/sturdy/language/wasm/scripts").toURI();

  val spectest = parse(pathSpectest)

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      println(s"Executing TestScript constant analysis on ${p.getFileName}")
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val script = parseScript(sourceCode)
      val interp = TestScriptConstantAnalysisInterpreter(Some(spectest))
      interp.run(script)
      val interpTop = TestScriptConstantAnalysisInterpreter(Some(spectest), true)
      interpTop.run(script)
    }
  }

