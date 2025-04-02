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
import sturdy.language.tip.ConcreteInterpreter.Value
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
import sturdy.language.tip.analysis.IRAnalysis.foreachIrValue

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class IRAnalysisTest extends AnyFlatSpec, Matchers:

  behavior of "Tip IR analysis"

  val uri = classOf[IRAnalysisTest].getResource("/sturdy/language/ir").toURI;

  Fixpoint.DEBUG = true

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.getFileName.toString.startsWith("loop_doub") &&
    p.toString.endsWith(".tip")
  ).sorted.foreach { p =>
    it must s"soundly analyze ${p.getFileName} with stacked states" in {
      println(s"analyze ${p.getFileName}")
      runIRAnalysis(p, StackConfig.StackedStates())
    }
  }

  def runIRAnalysis(p: Path, stackConfig: StackConfig) =
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    val program = Parser.parse(sourceCode)

    if (!program.funs.exists(_.name == "main"))
      fail()

    val analysis = new IRAnalysis.Instance(stackConfig)
    val aresult = analysis.failure.fallible(analysis.execute(program))
    println(s"Abstract result: $aresult")
    if (aresult.isFailing)
      fail()

    val res = aresult.get.get
    foreachIrValue(res) { ir =>
      // Files.write(Path.of("/home/armand/test.dot"), graphViz.getBytes())
      println(Export.toGraphViz(ir))

      val interpreter = new IRInterpreterConcrete[Int](Map("a" -> IRValue(0), "b" -> IRValue(0), "c" -> IRValue(0)), () => IRValue(0))
      val irEval = interpreter.interpret(ir)
      println(s"IR Result: $irEval")
      println(s"Feedback store: ${interpreter.feedbackStore}")

      val interp = ConcreteInterpreter(() => ConcreteInterpreter.Value.IntValue(0))
      val cresult = interp.failure.fallible(interp.execute(program))
      println(s"Concrete result: $cresult")

      cresult.get match
        case Value.TopValue => ???
        case Value.IntValue(i) => assert(i == irEval.c)
        case Value.RefValue(addr) => ???
        case Value.FunValue(fun) => ???
        case Value.RecValue(rec) => ???
    }
