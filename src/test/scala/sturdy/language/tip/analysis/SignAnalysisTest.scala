package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.GenericInterpreter.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Program, Parser}
import sturdy.language.whilelang.ConcreteInterpreter.*
import sturdy.language.whilelang.ConcreteInterpreter.Value.*

import sturdy.effect.failure.given
import sturdy.util.Labeled
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.language.tip.analysis.SignAnalysisSoundness.given

import java.nio.file.{Paths, Files, Path}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class SignAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip sign analysis"

  val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip").toURI();

  Files.list(Paths.get(uri)).toScala(List).sorted.filter(p => p.toString.endsWith(".tip")).foreach { p =>
    it must s"soundly analyze ${p.getFileName}" in {
      runFile(p, 10)
    }
  }

  def runFile(p: Path, steps: Int): Unit =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.effectOps.fallible(interp.execute(program))
//      println("\n" + cresult)
//      println(interp.effectOps.getPrinted)
//      println(interp.effectOps.getStore)
//      println(interp.effectOps.getAddressContexts.map{case (i,AllocationSite.Alloc(a)) => (i,a.label); case a => a})

      val analysis = SignAnalysis(Map(), Map(), steps)
      val aresult = analysis.effectOps.fallible(analysis.execute(program))
//      println("\n" + analysis.effectOps.getPrinted)
//      println(analysis.effectOps.getStore)

      given CAllocationIntIncrement[AllocationSite] = interp.effectOps
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
    }

  def runAnalysis(p: Path, steps: Int) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      //      println(s"Running ${p.getFileName}")

      val analysis = SignAnalysis(Map(), Map(), steps)
      (analysis.effectOps.fallible(analysis.execute(program)), analysis.effectOps)
    } else {
      null
    }

//  it should "run this file" in {
//    val uri = classOf[SignAnalysisTest].getResource("/sturdy/language/tip/code.tip").toURI();
//    val (res, effects) = runAnalysis(Paths.get(uri), 10)
//    println(res)
//    println(effects.getEnv)
//    println(effects.getStore)
////    Labeled.reset()
////    runFile(Paths.get(uri), 3)
//  }
