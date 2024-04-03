package sturdy.language.pcf

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.Soundness
import sturdy.language.pcf.SignInterpreter.{*, given}
import sturdy.language.pcf.Parser.*
import sturdy.language.pcf.{Parser, Program}
import sturdy.effect.failure.given
import sturdy.fix.{Fixpoint, StackConfig}

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class SignAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "PCF sign backward analysis"

  val uri = classOf[SignAnalysisTest].getResource("/examples").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.getFileName.toString == "factorial.pcf"
  ).foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runSignAnalysis(p)
    }
  }

  def runSignAnalysis(p: Path): Unit =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)
    println(s"Program: $program")
    if (program.definitions.contains("main")) {
      val analysis = new SignInterpreter.Instance(Map(),  StackConfig.StackedStates())
      val result = analysis.failure.fallible(analysis.evalProgram(program))
      println(result)
    }

//  def runSignAnalysis(p: Path, stackConfig: StackConfig) =
//    val file = Source.fromURI(p.toUri)
//    val sourceCode = file.getLines().mkString("\n")
//    file.close()
//    val program = Parser.parse(sourceCode)
//
//    if (program.funs.exists(_.name == "main")) {
//      val analysis = new SignInterpreter.Instance(Map(), Map(), stackConfig)
//      val aresult = analysis.failure.fallible(analysis.execute(program))
//      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
//      val cresult = interp.failure.fallible(interp.execute(program))
//    } else {
//      null
//    }

