package sturdy.language.tip.analysis

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.*
import sturdy.data.given
import sturdy.effect.allocation.CAllocatorIntIncrement
import sturdy.effect.failure.{AFallible, afallibleAbstractly, falliblePO, given}
import sturdy.effect.print.given
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.abstractions.isFunOrWhile
import sturdy.language.tip.analysis.IntervalAnalysis.{*, given}
import sturdy.language.tip.analysis.IntervalAnalysisSoundness.given
import sturdy.language.tip.*
import sturdy.util.{Labeled, LinearStateOperationCounter, Profiler}
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import sturdy.*
import sturdy.effect.{TrySturdy, failure}
import sturdy.ir.{Export, IRInterpreterConcrete, IRValue}

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class IRAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip IR analysis"

  val uri = classOf[IRAnalysisTest].getResource("/sturdy/language/tip").toURI;

  Fixpoint.DEBUG = true

  Files.list(Paths.get(uri)).toScala(List).filter(p =>
    p.toString.endsWith(".tip") && p.toString.contains("/while")
  ).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      println(s"analyze ${p.getFileName}")
      runIntervalAnalysis(p, StackConfig.StackedStates())
    }
  }

  def runIntervalAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (program.funs.exists(_.name == "main")) {
      val analysis = new IRAnalysis.Instance(Map(), Map(), stackConfig, 0)

      val aresult = analysis.failure.fallible(analysis.execute(program))
      Profiler.printLastMeasured()
      LinearStateOperationCounter.addToListAndReset()
      println(s"${LinearStateOperationCounter.toString} in the last tests")
      println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")

      println(s"Abstract run")

//      given CAllocatorIntIncrement[AllocationSite] = interp.alloc
//      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
//      assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))
      println(aresult)

      println(s"RESULT")
      val ir = IRAnalysis.valueToIR(aresult.get.get)
      println(Export.toGraphViz(ir))


      println(s"IR run")
      val externals = ir.externals.map(name => name -> IRValue(0)).toMap
      val irInterp = new IRInterpreterConcrete(externals)
      val v = try irInterp.interpret(ir) catch {
        case e: StackOverflowError =>
          println(e.getClass.getName)
          null
      }
      println(v)

      println(s"Concrete run")
      val interp = ConcreteInterpreter(() => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      println(cresult)

      if (!cresult.isFailing) {
        assert(v != null, s": IR interpretation looped while concrete interpreter yielded ${cresult.get}")
        assertResult(cresult.get.asInt(using interp))(v.c)
      }

      (aresult, analysis)
    } else {
      null
    }

