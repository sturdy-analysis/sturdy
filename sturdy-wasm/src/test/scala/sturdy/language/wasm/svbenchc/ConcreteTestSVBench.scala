package sturdy.language.wasm.svbenchc

import org.scalatest.Suites
import org.scalatest.concurrent.TimeLimits.failAfter
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.*
import sturdy.control.{ControlEventGraphBuilder, Node, PrintingControlObserver}
import sturdy.language.wasm
import sturdy.language.wasm.generic.*
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*
import scala.language.postfixOps

object SlowTest extends org.scalatest.Tag("SlowTest")
object FastTest extends org.scalatest.Tag("FastTest")

class ConcreteTestsSVBench extends Suites(
  new ConcreteTestSVBench()
)

class ConcreteTestSVBench() extends AnyFunSpec, Matchers:
  describe(s"Concrete run of SV Bench C Benchmarks") {

    val wasmBinaries = this.getClass.getResource("/sturdy/language/wasm/sv-bench/sv-bench-c/bin").toURI;

    val includedBenchmarks = Set("recursified_loop-crafted", "recursified_loop-invariants", "recursified_loop-simple", "rescursified_nla-digbench", "recursive", "recursive-simple", "recursive-with-pointer")
    val onlyTest: Set[String] = Set()

    val entrypoint = "_start"


    for {benchDirectory <- Files.list(Paths.get(wasmBinaries)).toScala(List).sorted
         if includedBenchmarks.contains(benchDirectory.getFileName.toString)
    } {
      describe(s"Suite ${benchDirectory.getFileName.toString}") {
        for {benchFile <- Files.list(benchDirectory).toScala(List).sorted
             if benchFile.toString.endsWith(".wat")  &&
                (onlyTest.isEmpty || onlyTest.contains(benchFile.getFileName.toString))
        } {
          it(s"Benchmark ${benchFile.getFileName.toString}", FastTest) {
            run(benchDirectory, benchFile, entrypoint)
          }
        }
      }
    }
  }

  def run(benchmark: Path, testFile: Path, entrypoint: String) =
    val testName = testFile.getFileName
    val yamlFile = testFile.toString.substring(0,testFile.toString.lastIndexOf(".")) + ".yml"
    val expectedVerdict = unreachableCallExpectedVerdict(Paths.get(yamlFile))

    val module = if testName.endsWith("wasm") then Parsing.fromBinary(testFile) else Parsing.fromText(testFile)

    val analysis = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
    // analysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
    val cfgBuilder = analysis.addControlObserver(new ControlEventGraphBuilder)

    val hostModules = HostModules(
      "wasi_snapshot_preview1" -> wasi_snapshot_preview1,
      "env" -> svbenchHostFunctions
    )
    val modInst = analysis.initializeModule(module, hostModules = hostModules)
    val result = failAfter(1 minute) {
      analysis.failure.fallible {
        analysis.invokeExported(modInst, entrypoint, List.empty)
      }
    }

    val cfg = cfgBuilder.get
    val dotPath = testFile.getParent.resolve(testFile.getFileName.toString + ".dot")
    Files.writeString(dotPath, cfg.toGraphViz)

    val (envMod, hostAssertFailId, hostAssertFail) = hostModules.getHostFunction("env", "host_assert_fail").get
    val actualVerdict = !cfg.nodes.exists {
      case Node.BlockStart(FuncId(modInst, id)) => modInst == envMod && hostAssertFailId == id
      case _ => false
    }
    assertResult(expectedVerdict) {actualVerdict}


  def unreachableCallExpectedVerdict(yamlPath: Path): Boolean =
    val yamlConf = io.circe.yaml.parser.parse(Files.readString(yamlPath)).toTry.get
    var prop = yamlConf.hcursor.downField("properties").downArray.downField("property_file")
    while (!prop.failed && !prop.as[String].contains("../properties/unreach-call.prp")) {
      prop = prop.up.right.downField("property_file")
    }
    if (prop.as[String].contains("../properties/unreach-call.prp")) {
      prop.up.downField("expected_verdict").as[Boolean].getOrElse(cancel("No expected verdict for unreachable-call"))
    } else {
      cancel("No expected verdict for unreachable-call")
    }
