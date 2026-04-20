package sturdy.language.wasm.benchmarksgame

import apron.*

import com.github.tototoshi.csv.{CSVReader, CSVWriter, DefaultCSVFormat}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Suites}

import sturdy.control.PrintingControlObserver
import sturdy.fix.StackConfig
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.RelationalAnalysis.ByteMemoryCtx
import sturdy.language.wasm.analyses.{FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{*, given}
import sturdy.language.wasm.testscript.SlowTest
import sturdy.util.Profiler
import sturdy.values.{*, given}
import swam.syntax
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import java.io.File
import java.net.URI
import java.nio.file.{FileSystemNotFoundException, FileSystems, Files, Path, Paths}

import scala.jdk.StreamConverters.*

object SlowTest extends org.scalatest.Tag("SlowTest")

val performanceWriter: CSVWriter = CSVWriter.open(File("scalability.csv"))

class BenchmarksgameRelationalPerformanceTests extends Suites(
  BenchmarksgameRelationalPerformanceTest(newManager = Polka(true), relational = true),
  BenchmarksgameRelationalPerformanceTest(newManager = Octagon(), relational = true),
  BenchmarksgameRelationalPerformanceTest(newManager = Box(), relational = true),
), BeforeAndAfterAll:

  override def beforeAll(): Unit =
    performanceWriter.writeRow(List("program", "analysis", "run", "env_size", "byte_size", "time"))

  override def afterAll(): Unit =
    performanceWriter.close

class BenchmarksgameRelationalPerformanceTest(newManager: => Manager, relational: Boolean) extends AnyFlatSpec, Matchers:

  val manager = newManager
  val funcName = "_start"
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;
  val path = if (uri.getScheme == "jar") {
    // For JAR resources - get existing FileSystem or create new one
    val fs = try {
      FileSystems.getFileSystem(uri)
    } catch {
      case _: FileSystemNotFoundException =>
        FileSystems.newFileSystem(uri, new java.util.HashMap[String, Any]())
    }
    fs.getPath("/sturdy/language/wasm/benchmarksgame/src")
  } else {
    // For regular file system paths
    Paths.get(uri)
  }


  val fixpointConfig: FixpointConfig = FixpointConfig(
    stack = StackConfig.StackedStates(storeIntermediateOutput = false, readPriorOutput = false),
    iter = sturdy.fix.iter.Config.Innermost
  )

  // These programs contain structs, which our analysis does not yet support.
  val excluded: Set[Path] = Set("k-nucleotide.wasm", "pidigits.wasm", "test-array-of-structs.wasm", "test-arrays.wasm", "test-call-by-reference.wasm").map(prog =>
    path.resolve(prog)
  )

  val analysisName: String = if (relational) manager.getClass.getSimpleName else "non-relational"

  def isSlow(manager: Manager, p: Path): Boolean =
    (manager.isInstanceOf[apron.Polka] || manager.isInstanceOf[apron.Octagon])

  def runBenchmark(p: Path) =
    val module = Parsing.fromBinary(p)

    def runAnalysis: (Int, Int, Long) =
      // We want to disable widening thresholds for better performance.
      // A bug causes binarytrees.wasm to diverge without widening thresholds.
      val analysis = RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = relational, soundOverflowHandling = false, wideningThresholds = p.toString.contains("binarytrees.wasm")))
      val abstractDomainSizeLogger = analysis.abstractDomainSizeLogger
//      analysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))

      val moduleInst = analysis.instantiateModule(module, moduleId = Some(p.getFileName))

      val start = System.nanoTime()
      analysis.failure.fallible(
        analysis.invokeExported(moduleInst, funcName, List.empty)
      )
      val end = System.nanoTime()
      val analysisTime = end - start
      (abstractDomainSizeLogger.getEnvSize, abstractDomainSizeLogger.getByteSize, analysisTime)

    // warmup
    (1 to 3).foreach(i =>
      println(s"Warmup $i")
      runAnalysis
    )

    System.gc()
    Thread.sleep(3000)

    // measure
    val analysisTimes = (1 to 5).map(i =>
      println(s"Measure $i")
      val (envSize, byteSize, time) = runAnalysis
      performanceWriter.writeRow(
        List(
          p.getFileName.toString,
          analysisName,
          s"$i",
          s"${envSize}",
          s"${byteSize}",
          s"${time}"
        )
      )
      System.gc()
      Thread.sleep(3000)
    )

  Files.list(path).toScala(List).filter(p => p.toString.endsWith(".wasm") && !excluded.contains(p)).sorted.foreach { p =>
    if(isSlow(manager, p))
      it must s"analyze ${p.getFileName} with ${analysisName}" taggedAs (SlowTest) in {
        runBenchmark(p)
      }
    else
      it must s"analyze ${p.getFileName} with ${analysisName}" in {
        runBenchmark(p)
      }
  }
