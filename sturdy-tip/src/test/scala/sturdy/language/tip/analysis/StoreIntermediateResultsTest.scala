package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.IsSound
import sturdy.data.given
import sturdy.Soundness
import sturdy.control.{ControlEvent, ControlEventGraphBuilder, ControlGraph, ControlTree, FixpointControlEvent, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.allocation.CAllocatorIntIncrement
import sturdy.effect.failure.{AFallible, given}
import sturdy.effect.print.given
import sturdy.language.tip.ConcreteInterpreter
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.{Parser, Program}
import sturdy.effect.failure.{afallibleAbstractly, falliblePO}
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.fix.iter.Config.*
import sturdy.util.{Labeled, LinearStateOperationCounter, Profiler}
import sturdy.{*, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.analysis.IntervalAnalysisSoundness.given
import sturdy.language.tip.analysis.IntervalAnalysis.{*, given}
import sturdy.language.tip.abstractions.isFunOrWhile

import java.io.{BufferedOutputStream, FileOutputStream}
import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class StoreIntermediateResultsTest extends AnyFlatSpec, Matchers:

  behavior of "Tip interval analysis intermediate result tests"

  val uri: URI = classOf[StoreIntermediateResultsTest].getResource("/sturdy/language/tip").toURI;

  Fixpoint.DEBUG = true
  Fixpoint.DEBUG_PRIOR_OUTPUT = true

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.toString.contains("/intermediate") && p.toString.endsWith(".tip")
  ).sorted.foreach { p =>
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    var resInnerNoInter: AFallible[Value] = null
    var resInnerWithInter: AFallible[Value] = null
    var resOuterNoInter: AFallible[Value] = null
    var resOuterWithInter: AFallible[Value] = null
    var resTopNoInter: AFallible[Value] = null
    var resTopWithInter: AFallible[Value] = null

    val csvPath = p.getParent.resolve(p.getFileName.toString + "_fix.csv")
    Files.deleteIfExists(csvPath)
    val f = Files.createFile(csvPath)
    val buf = BufferedOutputStream(new FileOutputStream(f.toFile))
    buf.write("config, push, read, recurrent, pop, stable, invalidate\n".getBytes)
    def write(config: String): Unit = {
      buf.write(s"$config, ${Profiler.getData("fix_push", 0)}, ${Profiler.getData("fix_read", 0)}, ${Profiler.getData("fix_recurrent", 0)}, ${Profiler.getData("fix_pop", 0)}, ${Profiler.getData("fix_stable", 0)}, ${Profiler.getData("fix_invalidate", 0)}\n".getBytes)
    }

    it must s"soundly analyze ${p.getFileName} with stacked states (innermost, storeIntermediateOutput = false)" in {
      resInnerNoInter = runIntervalAnalysis(p, program, Innermost, StackConfig.StackedStates(storeIntermediateOutput = false))._1
      write("inner_noInter")
    }
    it must s"soundly analyze ${p.getFileName} with stacked states (innermost, storeIntermediateOutput = true)" in {
      resInnerWithInter = runIntervalAnalysis(p, program, Innermost, StackConfig.StackedStates(storeIntermediateOutput = true))._1
      write("inner_withInter")
    }
    it must s"soundly analyze ${p.getFileName} with stacked states (innermost, storeIntermediateOutput = true, storeNonrecursiveOutput = true)" in {
      resInnerWithInter = runIntervalAnalysis(p, program, Innermost, StackConfig.StackedStates(storeNonrecursiveOutput = true,  storeIntermediateOutput = true))._1
      write("inner_withAll")
    }
    it must s"soundly analyze ${p.getFileName} with stacked states (outermost, storeIntermediateOutput = false)" in {
      resOuterNoInter = runIntervalAnalysis(p, program, Outermost, StackConfig.StackedStates(storeIntermediateOutput = false))._1
      write("outer_noInter")
    }
    it must s"soundly analyze ${p.getFileName} with stacked states (outermost, storeIntermediateOutput = true)" in {
      resOuterWithInter = runIntervalAnalysis(p, program, Outermost, StackConfig.StackedStates(storeIntermediateOutput = true))._1
      write("outer_withInter")
    }
    it must s"soundly analyze ${p.getFileName} with stacked states (outermost, storeIntermediateOutput = true, storeNonrecursiveOutput = true)" in {
      resOuterWithInter = runIntervalAnalysis(p, program, Outermost, StackConfig.StackedStates(storeNonrecursiveOutput = true, storeIntermediateOutput = true))._1
      write("outer_withAll")
    }
    it must s"soundly analyze ${p.getFileName} with stacked states (topmost, storeIntermediateOutput = false)" in {
      resTopNoInter = runIntervalAnalysis(p, program, Topmost, StackConfig.StackedStates(storeIntermediateOutput = false))._1
      write("top_noInter")
    }
    it must s"soundly analyze ${p.getFileName} with stacked states (topmost, storeIntermediateOutput = true)" in {
      resTopWithInter = runIntervalAnalysis(p, program, Topmost, StackConfig.StackedStates(storeIntermediateOutput = true))._1
      write("top_withInter")
    }
    it must s"soundly analyze ${p.getFileName} with stacked states (topmost, storeIntermediateOutput = true, storeNonrecursiveOutput = true)" in {
      resTopWithInter = runIntervalAnalysis(p, program, Topmost, StackConfig.StackedStates(storeNonrecursiveOutput = true, storeIntermediateOutput = true))._1
      write("top_withAll")
    }

    it must s"collect data for ${p.getFileName}" in {
      buf.close()
      println(s"Wrote CSV into $csvPath")
    }

  }

  def runIntervalAnalysis(p: Path, program: Program, iterConfig: fix.iter.Config, stackConfig: StackConfig): (AFallible[Value], Instance) =

    if (program.funs.exists(_.name == "main")) {
      Profiler.reset()
      val analysis = new IntervalAnalysis.Instance(Map(), Map(), stackConfig, 0, iterConfig)
//      val rec = analysis.addControlObserver(new RecordingControlObserver)
//      analysis.addControlObserver(new PrintingControlObserver()(println))
//      val graphBuilder = analysis.addControlObserver(new ControlEventGraphBuilder)

//      val onlyCalls = false
//      val cfg = IntervalAnalysis.controlFlow(sensitive = true, onlyCalls, analysis)

      val aresult = analysis.failure.fallible(analysis.execute(program))
      Profiler.printLastMeasured()
//      LinearStateOperationCounter.addToListAndReset()
//      println(s"${LinearStateOperationCounter.toString} in the last tests")
//      println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")

//      println(graphBuilder.get.toGraphViz)

      val interp = ConcreteInterpreter(() => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      given CAllocatorIntIncrement[AllocationSite] = interp.alloc
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      println(aresult)
//      println(rec)
      (aresult, analysis)
    } else {
      (null, null)
    }

