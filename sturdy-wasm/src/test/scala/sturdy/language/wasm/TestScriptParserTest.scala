package sturdy.language.wasm

import cats.effect.*
import fastparse.*
import org.scalatest.Assertions.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import swam.text.Command
import swam.text.parser.TestScriptParser

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.jdk.StreamConverters.*

class TestScriptParserTest extends AnyFlatSpec, Matchers:
  behavior of "TestScript parser"

  val uri = classOf[TestScriptParserTest].getResource("/sturdy/language/wasm/scripts").toURI();

  Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".wast")).foreach { p =>
    it must s"parse ${p.getFileName}" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      parseScript(sourceCode)
    }
  }

def parseScript(wast: String): Seq[Command] =
  val script = _root_.fastparse.parse(wast, TestScriptParser.script(_))
  assert(script.isSuccess)
  script.get.value