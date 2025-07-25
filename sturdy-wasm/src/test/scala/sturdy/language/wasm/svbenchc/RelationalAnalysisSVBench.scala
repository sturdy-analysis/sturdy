package sturdy.language.wasm.svbenchc

import sturdy.control.{ControlEventGraphBuilder, Node, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.failure.AFallible
import sturdy.fix
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.fix.cfg.ControlFlowGraph.CNode
import sturdy.language.wasm
import sturdy.language.wasm.Parsing.WasmParseError
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.{CallSites, FixpointConfig, RelationalAnalysis, TypeAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FrameData, FuncId, HostModules, InstLoc, defaultHostModules}
import sturdy.language.wasm.{ConcreteInterpreter, Parsing, abstractions, testCfgDifference}
import sturdy.values.Topped
import org.scalatest.exceptions.{TestCanceledException, TestFailedDueToTimeoutException, TestFailedException}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.*
import org.scalatest.Suites
import apron.*
import cats.effect.{Blocker, IO}
import org.scalatest.concurrent.{Signaler, ThreadSignaler}
import org.scalatest.concurrent.TimeLimits.failAfter
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.io.File
import java.nio.file.{Files, Path, Paths}
import com.github.tototoshi.csv.*

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*
import scala.language.postfixOps

object SlowTest extends org.scalatest.Tag("SlowTest")
object FastTest extends org.scalatest.Tag("FastTest")

class RelationalAnalysisSVBench extends Suites(
//  new RelationalAnalysisTest(Polka(true)),
  new RelationalAnalysisTest(Octagon()),
  new RelationalAnalysisTest(Box()),
)

val csvWriter = {
  val writer = CSVWriter.open(File("relational-svbench.csv"))
  writer.writeRow(List("suite", "test_case", "abstract_domain", "expected_verdict", "passed", "timed_out"))
  writer
}

class RelationalAnalysisTest(manager: apron.Manager) extends AnyFunSpec, Matchers:
  describe(s"Relational Analysis of SV Bench C Benchmarks with ${manager.getClass.getSimpleName}") {

    val wasmBinaries = this.getClass.getResource("/sturdy/language/wasm/sv-bench/sv-bench-c/bin").toURI;

    val includedBenchmarks = // Set("recursified_loop-simple")
      Set("recursified_loop-crafted", "recursified_loop-invariants", "recursified_loop-simple", "recursified_nla-digbench", "recursive", "recursive-simple", "recursive-with-pointer")

    val stackConfig = StackConfig.StackedStates(readPriorOutput = false, storeNonrecursiveOutput = false, observers = Seq())
    val entrypoint = "_start"

    def isSlow(manager: Manager, script: String): org.scalatest.Tag =
      val slow = Set("recursified_bresenham-ll.wasm")
      if(manager.isInstanceOf[Polka] && slow.contains(script))
        SlowTest
      else
        FastTest

    for {benchDirectory <- Files.list(Paths.get(wasmBinaries)).toScala(List).sorted
         if includedBenchmarks.contains(benchDirectory.getFileName.toString)
    } {
      describe(s"Suite ${benchDirectory.getFileName.toString}") {
        for {benchFile <- Files.list(benchDirectory).toScala(List).sorted
             if benchFile.toString.endsWith(".wasm")
        } {
          it(s"Benchmark ${benchFile.getFileName.toString}", isSlow(manager, benchFile.getFileName.toString)) {
            run(benchDirectory, benchFile, entrypoint, stackConfig)(using manager)
          }
        }
      }
    }
  }

  def run(benchmark: Path, testFile: Path, entrypoint: String, stackConfig: StackConfig)(using apronManager: apron.Manager) =
    val testName = testFile.getFileName
    val yamlFile = testFile.toString.substring(0,testFile.toString.lastIndexOf(".")) + ".yml"
    val expectedVerdict = try {
      unreachableCallExpectedVerdict(Paths.get(yamlFile))
    } catch {
      case exc: TestCanceledException =>
        logTestResult(benchmark, testName, expectedVerdict = None, passed = false, timedOut = false)
        throw exc
    }

    val module = Parsing.fromBinary(testFile)

    val analysis = new RelationalAnalysis.Instance(apronManager, FrameData.empty, Iterable.empty, WasmConfig(FixpointConfig(stackConfig)))
    analysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
    val cfgBuilder = analysis.addControlObserver(new ControlEventGraphBuilder)

    val hostModules = defaultHostModules
    val modInst = analysis.initializeModule(module, hostModules = hostModules)
    given Signaler = ThreadSignaler
    val result = try {
      failAfter(1 minute) {
        analysis.failure.fallible {
          analysis.invokeExported(modInst, entrypoint, List.empty).map(analysis.getInterval)
        }
      }
    } catch {
      case exc: TestFailedDueToTimeoutException =>
        logTestResult(benchmark, testName, expectedVerdict = Some(expectedVerdict), passed = false, timedOut = true)
        throw exc
    }

    val cfg = cfgBuilder.get
    val dotPath = testFile.getParent.resolve(testFile.getFileName.toString + ".dot")
    Files.writeString(dotPath, cfg.toGraphViz)

    val (envMod, hostAssertFailId, hostAssertFail) = hostModules.getHostFunction("env", "host_assert_fail").get
    val actualVerdict = !cfg.nodes.exists {
      case Node.BlockStart(FuncId(modInst, id)) => modInst == envMod && hostAssertFailId == id
      case _ => false
    }
    try {
      assertResult(expectedVerdict) { actualVerdict }
    } catch {
      case exc: TestFailedException =>
        logTestResult(benchmark, testName, expectedVerdict = Some(expectedVerdict), passed = false, timedOut = false)
        throw exc
    }
    logTestResult(benchmark, testName, expectedVerdict = Some(expectedVerdict), passed = true, timedOut = false)

  def logTestResult(benchmark: Path, testFile: Path, expectedVerdict: Option[Boolean], passed: Boolean, timedOut: Boolean): Unit =
    csvWriter.writeRow(List(benchmark.getFileName.toString, testFile.toString, manager.getClass.getSimpleName, expectedVerdict.map(_.toString).getOrElse(""), passed.toString, timedOut.toString))


  def unreachableCallExpectedVerdict(yamlPath: Path): Boolean =
    val yamlConf = io.circe.yaml.parser.parse(Files.readString(yamlPath)).toTry.get
    var prop = yamlConf.hcursor.downField("properties").downArray.downField("property_file")
    while (!prop.failed && !prop.as[String].contains("../properties/unreach-call.prp")) {
      prop = prop.up.right.downField("property_file")
    }
    if(prop.as[String].contains("../properties/unreach-call.prp")) {
      prop.up.downField("expected_verdict").as[Boolean].getOrElse(cancel("No expected verdict for unreachable-call"))
    } else {
      cancel("No expected verdict for unreachable-call")
    }

