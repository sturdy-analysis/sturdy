package sturdy.language.wasm.benchmarksgame

import apron.*
import com.github.tototoshi.csv.{CSVReader, CSVWriter, DefaultCSVFormat}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Suites}
import sturdy.control.PrintingControlObserver
import sturdy.fix.StackConfig
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.RelationalAnalysis.ByteMemoryCtx
import sturdy.language.wasm.analyses.{FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{*, given}
import sturdy.util.Profiler
import sturdy.values.{*, given}
import swam.syntax
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import java.io.File
import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.collection.immutable.SortedMap
import scala.jdk.StreamConverters.*

val performanceWriter: CSVWriter = CSVWriter.open(File("benchmarks-game-performance-test.csv"))

class BenchmarksgameRelationalPerformanceTests extends Suites(
//  BenchmarksgameRelationalPerformanceTest(newManager = Polka(true), relational = true, ssa = false),
//  BenchmarksgameRelationalPerformanceTest(newManager = Octagon(), relational = true, ssa = false),
  BenchmarksgameRelationalPerformanceTest(newManager = Box(), relational = true, ssa = false),
//  BenchmarksgameRelationalPerformanceTest(newManager = Box(), relational = false)
//  BenchmarksgameRelationalPerformanceTest(newManager = Polka(true), relational = true, ssa = true),
//  BenchmarksgameRelationalPerformanceTest(newManager = Octagon(), relational = true, ssa = true),
//  BenchmarksgameRelationalPerformanceTest(newManager = Box(), relational = true, ssa = true),
), BeforeAndAfterAll:

  override def beforeAll(): Unit =
    performanceWriter.writeRow(List("program", "analysis", "run", "env_size", "byte_size", "time"))

  override def afterAll(): Unit =
    performanceWriter.close

class BenchmarksgameRelationalPerformanceTest(newManager: => Manager, relational: Boolean, ssa: Boolean = false) extends AnyFunSpec, Matchers:

  val manager = newManager
  val funcName = "_start"
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  val fixpointConfig: FixpointConfig = FixpointConfig(
    stack = StackConfig.StackedStates(storeIntermediateOutput = false, readPriorOutput = false),
    iter = sturdy.fix.iter.Config.Innermost
  )

  // These programs contain structs, which our analysis does not yet support.
  val excluded: Set[Path] = Set("k-nucleotide.wasm", "pidigits.wasm", "test-array-of-structs.wasm", "test-arrays.wasm", "test-call-by-reference.wasm").map(prog =>
    Paths.get(uri).resolve(prog)
  )

  val analysisName: String = if (relational) manager.getClass.getSimpleName else "non-relational"

  describe(analysisName) {
    Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm") && !excluded.contains(p)).sorted.foreach { p =>
      it(s"${p.getFileName}") {

        val module = Parsing.fromBinary(p)

        def runAnalysis: (Int,Int,Long) =
          val analysis = RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = relational, localSSA = ssa))
          val abstractDomainSizeLogger = analysis.abstractDomainSizeLogger
//          analysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))

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
          val (envSize,byteSize,time) = runAnalysis
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
      }
    }
  }
