package sturdy.language.tip.analysis

import apron.{Manager, Polka, Octagon, Box, Texpr1Node}
import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.Suites
import org.scalatest.exceptions.TestFailedException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.{*, given}
import sturdy.data.given
import sturdy.Soundness
import sturdy.control.{ControlEventGraphBuilder, PrintingControlObserver}
import sturdy.effect.allocation.CAllocatorIntIncrement
import sturdy.effect.failure.CFallible.Failing
import sturdy.effect.failure.{AFallible, AssertionFailure, afallibleAbstractly, falliblePO, given}
import sturdy.effect.print.given
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.language.tip.Parser.*
import sturdy.language.tip.Parser.LanguageKeywords.KRETURN
import sturdy.language.tip.abstractions.{CfgNode, isFunOrWhile}
import sturdy.language.tip.*
import sturdy.util.{IntLabel, Labeled, LinearStateOperationCounter, Profiler, SynLabel}
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.BaseType
import sturdy.values.{*, given}

import java.nio.file.{Files, Path, Paths}
import scala.collection.immutable.ArraySeq
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}

class RelationalAnalysisTests extends Suites(
  new RelationalAnalysisTest(Polka(true)),
  new RelationalAnalysisTest(Octagon()),
  new RelationalAnalysisTest(Box()),
)

class RelationalAnalysisTest(manager: Manager) extends AnyFlatSpec, Matchers:
   behavior of ("Tip Relational analysis with " + manager.getClass.getSimpleName)

   val uri = classOf[RelationalAnalysisTest].getResource("/sturdy/language/tip").toURI;
   val recursiveProgram: Array[String] = Source.fromFile(classOf[RelationalAnalysisTest].getResource("/sturdy/language/recursive_programs").toURI).getLines.toArray

   Fixpoint.DEBUG = true
   Files.list(Paths.get(uri)).toScala(List).filter(p =>
     p.toString.endsWith(".tip")
//    p.endsWith("loop.tip")
   ).sorted.foreach { p =>
     it must s"soundly analyze ${p.getFileName} with stacked states" in {
       runRelationalAnalysis(p, StackConfig.StackedStates(readPriorOutput = false))
     }
   }

   def runRelationalAnalysis(p: Path, stackConfig: StackConfig) =
     val file = Source.fromURI(p.toUri)
     val sourceCode = file.getLines().mkString("\n")
     file.close()
     Labeled.reset()
     val program = Parser.parse(sourceCode)

     if (program.funs.exists(_.name == "main")) {
       val analysis = new RelationalAnalysis.Instance(manager, Map(), stackConfig, 0)
       analysis.addControlObserver(new PrintingControlObserver()(println))
       val cfgBuilder = analysis.addControlObserver(ControlEventGraphBuilder())

       val aresult = analysis.failure.fallible(analysis.execute(program))
       val interp = ConcreteInterpreter(() => ConcreteInterpreter.Value.IntValue(0))
       val cresult = interp.failure.fallible(interp.execute(program))

       given CAllocatorIntIncrement[AllocationSite] = interp.alloc

       println(s"CONCRETE RESULT: $cresult")
       println(s"ABSTRACT RESULT: $aresult")

       // compute number of assertions in program
       val allAsserts = program.assertions
       if (allAsserts.nonEmpty) {
         val reachableAsserts = cfgBuilder.get.nodes.collect { case sturdy.control.Node.Atomic(a: Stm.Assert) => a }.toSet
         val unreachableAsserts = allAsserts.diff(reachableAsserts)
         val unreachablePercent = (100 * unreachableAsserts.size / allAsserts.size.toDouble).round
         val failedAsserts = aresult.failures.set.collect { case (AssertionFailure(a: Stm.Assert), _) => a }
         val failedPercent = (100 * failedAsserts.size / allAsserts.size.toDouble).round
         val provedAsserts = reachableAsserts.diff(failedAsserts)
         val provedPercent = (100 * provedAsserts.size / allAsserts.size.toDouble).round

         println(s"Assertions: ${allAsserts.size} assertions, ${provedAsserts.size} ($provedPercent%) proved, ${unreachableAsserts.size} ($unreachablePercent%) unreachable, ${failedAsserts.size} ($failedPercent%) failed")
//         assertResult(true, s", ${failedAsserts.size} assertion(s) have failed in ${p.getFileName}")(failedAsserts.isEmpty)
         assertResult(true, s", ${unreachableAsserts.size} assertion(s) were unreachable in ${p.getFileName}")(unreachableAsserts.isEmpty)
       }

       val soundness = new RelationalAnalysisSoundness(analysis)
       import soundness.given
       cresult match
         case Failing(TipFailure.StackOverflow, _) =>
         case _ => assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(cresult, aresult))
       assertResult(IsSound.Sound, p.getFileName)(Soundness.isSound(interp, analysis))

     } else {
       null
     }

   "The ordering on RelationalVar" should "be Print <= Alloc <= Temp <= Local" in {
     import RelationalAnalysis.given
     import RelationalAnalysis.AddrCtx.*

     val input = Input(sturdy.language.tip.Exp.Input())
     val alloc1 = Alloc(sturdy.language.tip.Exp.Alloc(sturdy.language.tip.Exp.NumLit(1)))
     val tempInt = Temp(FixIn.Eval(sturdy.language.tip.Exp.Var("y")))
     val localX = Local("x","fun")

     Ordering[RelationalAnalysis.AddrCtx].compare(input, alloc1) shouldBe -1
     Ordering[RelationalAnalysis.AddrCtx].compare(alloc1, input) shouldBe 1

     Ordering[RelationalAnalysis.AddrCtx].compare(input, tempInt) shouldBe -1
     Ordering[RelationalAnalysis.AddrCtx].compare(tempInt, input) shouldBe 1

     Ordering[RelationalAnalysis.AddrCtx].compare(input, localX) shouldBe -1
     Ordering[RelationalAnalysis.AddrCtx].compare(localX, input) shouldBe 1

     Ordering[RelationalAnalysis.AddrCtx].compare(alloc1, tempInt) shouldBe -1
     Ordering[RelationalAnalysis.AddrCtx].compare(tempInt, alloc1) shouldBe 1

     Ordering[RelationalAnalysis.AddrCtx].compare(alloc1, localX) shouldBe -1
     Ordering[RelationalAnalysis.AddrCtx].compare(localX, alloc1) shouldBe 1
   }