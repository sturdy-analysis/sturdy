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
import sturdy.language.tip.analysis.IntervalAnalysisSoundness.given

import java.nio.file.{Paths, Files, Path}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}

class IntervalAnalysisTest extends AnyFlatSpec, Matchers:

  def runAnalysis(p: Path, steps: Int) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      println(s"Running ${p.getFileName}")

      val analysis = IntervalAnalysis(Map(), Map(), steps)
      (analysis.effectOps.fallible(analysis.execute(program)), analysis.effectOps)
    } else {
      null
    }

  def runFile(p: Path, steps: Int): Int =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      println(s"Running ${p.getFileName} ...")

      val interp = ConcreteInterpreter(Map(), Map(), () => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.effectOps.fallible(interp.execute(program))
      //      println("\n" + cresult)
      //      println(interp.effectOps.getPrinted)
      //      println(interp.effectOps.getStore)
      //      println(interp.effectOps.getAddressContexts.map{case (i,AllocationSite.Alloc(a)) => (i,a.label); case a => a})

      val analysis = IntervalAnalysis(Map(), Map(), steps)
      val aresult = analysis.effectOps.fallible(analysis.execute(program))
      println(aresult)
      println(analysis.effectOps.getPrinted)
      println(analysis.effectOps.getStore)

      given CAllocationIntIncrement[AllocationSite] = interp.effectOps
      assertResult(IsSound.Sound)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound)(Soundness.isSound(interp, analysis))
      if ((Soundness.isSound(cresult, aresult) && Soundness.isSound(interp, analysis)) == IsSound.Sound)
        println(s"Running ${p.getFileName}: sound")
        1
      else {
        println(s"Running ${p.getFileName}: unsound")
        0
      }
    } else {
      println(s"${p.getFileName}: no main function")
      -1
    }


  "TIP interval analysis" should "run all examples" in {
    val uri = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip").toURI();
    val tipDir = Paths.get(uri)
    var files = 0
    var successful = 0
    Files.list(tipDir).toScala(List).sorted.filter(p => p.toString.endsWith(".tip")).foreach { p =>
      val res = runFile(p, 10)
      if (res == 1) {
        files += 1
        successful += 1
      } else if (res == 0) {
        files += 1
      }
    }
    assertResult(files)(successful)
  }

  it should "run all fix examples" in {
    val uri = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip").toURI();
    val tipDir = Paths.get(uri)
    var files = 0
    var successful = 0
    Files.list(tipDir).toScala(List).sorted.filter(p => p.toString.endsWith(".tip") && p.getFileName.toString.startsWith("fix")).foreach { p =>
      val res = runFile(p, 10)
      if (res == 1) {
        files += 1
        successful += 1
      } else if (res == 0) {
        files += 1
      }
    }
    assertResult(files)(successful)
  }

  it should "run this file" in {
    val uri = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip/fix6.tip").toURI();
    val (res, effects) = runAnalysis(Paths.get(uri), 10)
    println(res)
    println(effects.getEnv)
    println(effects.getStore)
    println(effects.getPrinted)

//    Labeled.reset()
//    runFile(Paths.get(uri), 3)
  }

object O extends App {
  def runAnalysis(p: Path, steps: Int) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      println(s"Running ${p.getFileName}")

      val analysis = IntervalAnalysis(Map(), Map(), steps)
      (analysis.effectOps.fallible(analysis.execute(program)), analysis.effectOps)
    } else {
      null
    }



  val uri = classOf[IntervalAnalysisTest].getResource("/sturdy/language/tip/fix5.tip").toURI();
  val (res, effects) = runAnalysis(Paths.get(uri), 10)
  println(res)
  println(effects.getEnv)
  println(effects.getStore)
  println(effects.getPrinted)
}