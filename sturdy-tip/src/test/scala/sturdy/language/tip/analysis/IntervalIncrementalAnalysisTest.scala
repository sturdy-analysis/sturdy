package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.data.given
import sturdy.Soundness
import sturdy.effect.allocation.CAllocationIntIncrement
import sturdy.effect.failure.{AFallible, given}
import sturdy.effect.print.given
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.{afallibleAbstractly, falliblePO}
import sturdy.fix.StackConfig
import sturdy.incremental.{Identifiable, ListDelta}
import sturdy.util.{Labeled, LinearStateOperationCounter, Profiler}
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.analysis.IntervalAnalysisSoundness.given
import sturdy.language.tip.analysis.IntervalAnalysis.{*, given}
import sturdy.language.tip.abstractions.isFunOrWhile

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class IntervalIncrementalAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip interval incremental analysis"

  val uri: URI = classOf[IntervalIncrementalAnalysisTest].getResource("/sturdy/language/tip").toURI
  val updateUri: URI = classOf[IntervalIncrementalAnalysisTest].getResource("/sturdy/language/tip/incremental.update").toURI

  Files.list(Paths.get(uri)).toScala(List).flatMap(p =>
    val update = Paths.get(updateUri.getPath, p.getFileName.toString)
    if(p.toString.contains("") && p.toString.endsWith(".tip") && Files.exists(update))
      Iterator((p,update))
    else
      Iterator()
  ).sorted.foreach { case (p: Path, update: Path) =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      runIntervalIncrementalAnalysis(p, update)
    }
  }

  def runIntervalIncrementalAnalysis(initial: Path, update: Path): Unit =
    val initialProgram = parse(initial)
    val updatedProgram = parse(update)

    if (initialProgram.funs.exists(_.name == "main")) {
      val initialRun = new IntervalAnalysis.InitialRunInstance(Map(), Map(),0)
      val aresult = initialRun.failure.fallible(initialRun.execute(initialProgram))
      println("hllo")
//      val incrementalUpdate = new IncrementalUpdateInstance(initialRun)
//      incrementalUpdate(initialProgram, updatedProgram)
    }

  def parse(p: Path): Program =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    Parser.parse(sourceCode)