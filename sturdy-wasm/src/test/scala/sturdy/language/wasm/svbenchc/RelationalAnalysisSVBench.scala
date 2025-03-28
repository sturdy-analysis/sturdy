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
import sturdy.language.wasm.generic.{FrameData, FuncId, HostModules, InstLoc, wasi_snapshot_preview1}
import sturdy.language.wasm.{ConcreteInterpreter, Parsing, abstractions, testCfgDifference}
import sturdy.values.Topped
import org.scalatest.exceptions.TestFailedException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar._
import org.scalatest.Suites
import apron.*
import cats.effect.{Blocker, IO}
import org.scalatest.concurrent.TimeLimits.failAfter
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*
import scala.language.postfixOps

object SlowTest extends org.scalatest.Tag("SlowTest")
object FastTest extends org.scalatest.Tag("FastTest")

class RelationalAnalysisSVBench extends Suites(
  new RelationalAnalysisTest(Polka(true)),
  new RelationalAnalysisTest(Octagon()),
  new RelationalAnalysisTest(Box()),
)

class RelationalAnalysisTest(manager: apron.Manager) extends AnyFunSpec, Matchers:
  describe(s"Relational Analysis of SV Bench C Benchmarks with ${manager.getClass.getSimpleName}") {

    val uri = this.getClass.getResource("/sturdy/language/wasm/sv-bench/sv-bench-c/bin").toURI;

    val includedBenchmarks = Set("recursified_loop-crafted", "recursified_loop-invariants", "recursified_loop-simple", "recursified_nla-digbench", "recursive", "recursive-simple", "recursive-with-pointer")

    val stackConfig = StackConfig.StackedStates(readPriorOutput = false, storeNonrecursiveOutput = false, observers = Seq())
    val entrypoint = "_start"

    def isSlow(manager: Manager, script: String): org.scalatest.Tag =
      if(manager.isInstanceOf[Polka] && script == "system-with-recursion.wasm")
        SlowTest
      else
        FastTest

    for {benchDirectory <- Files.list(Paths.get(uri)).toScala(Iterable)
         if includedBenchmarks.contains(benchDirectory.getFileName.toString)
    } {
      describe(s"Suite ${benchDirectory.getFileName.toString}") {
        for {benchFile <- Files.list(benchDirectory).toScala(Iterable)
             if benchFile.toString.endsWith(".wasm")
        } {
          it(s"Benchmark ${benchFile.getFileName.toString}", isSlow(manager, benchFile.getFileName.toString)) {
            run(benchFile, entrypoint, stackConfig)(using manager)
          }
        }
      }
    }
  }

  def run(p: Path, entrypoint: String, stackConfig: StackConfig)(using apronManager: apron.Manager) =
    val name = p.getFileName
    val module = Parsing.fromBinary(p)

    val analysis = new RelationalAnalysis.Instance(apronManager, FrameData.empty, Iterable.empty, WasmConfig(FixpointConfig(fix.iter.Config.Innermost(stackConfig))))
    analysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
    val cfgBuilder = analysis.addControlObserver(new ControlEventGraphBuilder)

    val hostModules = HostModules(
      "wasi_snapshot_preview1" -> wasi_snapshot_preview1,
      "env" -> svbenchHostFunctions
    )
    val modInst = analysis.initializeModule(module, hostModules = hostModules)
    val result = failAfter(1 minute) {
      analysis.failure.fallible {
        analysis.invokeExported(modInst, entrypoint, List.empty).map(analysis.getInterval)
      }
    }

    val cfg = cfgBuilder.get
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".dot")
    Files.writeString(dotPath, cfg.toGraphViz)

    val (envMod, hostAssertFailId, hostAssertFail) = hostModules.getHostFunction("env", "host_assert_fail").get
    val hostAssertFailCalled = cfg.nodes.exists {
      case Node.BlockStart(FuncId(modInst, id)) => modInst == envMod && hostAssertFailId == id
      case _ => false
    }

    assert(!hostAssertFailCalled, ", analysis was to imprecise to prove property")

