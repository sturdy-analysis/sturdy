package sturdy.language.wasm.benchmarksgame

import apron.*
import com.github.tototoshi.csv.{CSVReader, CSVWriter, DefaultCSVFormat}
import org.scalatest.{BeforeAndAfterAll, Suites}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sturdy.control.PrintingControlObserver
import sturdy.fix.StackConfig
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.{FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.analyses.RelationalAnalysis.HeapCtx
import sturdy.language.wasm.generic.{*, given}
import sturdy.util.Profiler
import sturdy.values.{*, given}
import swam.syntax

import java.io.File
import java.nio.file.{Files, Paths}
import scala.collection.immutable.SortedMap
import scala.jdk.StreamConverters.*

class BenchmarksgameRelationalPrecisionTests extends Suites(
  BenchmarksgameRelationalPrecisionTest(Box())
)

class BenchmarksgameRelationalPrecisionTest(manager: Manager) extends AnyFunSuite, Matchers, BeforeAndAfterAll:

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;
  val fixpointConfig: FixpointConfig = FixpointConfig(
    stack = StackConfig.StackedStates(),
    iter = sturdy.fix.iter.Config.Innermost
  )

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith("mandelbrot.wasm")).sorted.foreach { p =>
    test(s"${p.getFileName}") {
      val module = Parsing.fromBinary(p)

      val relationalAnalysis = RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = true))
      val relationalMemoryLogger = relationalAnalysis.memoryLogger
      relationalAnalysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
      var moduleInst = relationalAnalysis.instantiateModule(module, moduleId = Some(p.getFileName))
      relationalAnalysis.failure.fallible(
        relationalAnalysis.invokeExported(moduleInst, funcName, List.empty)
      )
      Profiler.printLastMeasured()
      Profiler.reset()

      val nonRelationalAnalysis = RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = false))
      val nonRelationalMemoryLogger = nonRelationalAnalysis.memoryLogger
      nonRelationalAnalysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
      moduleInst = nonRelationalAnalysis.instantiateModule(module, moduleId = Some(p.getFileName))
      nonRelationalAnalysis.failure.fallible(
        nonRelationalAnalysis.invokeExported(moduleInst, funcName, List.empty)
      )
      Profiler.printLastMeasured()
      Profiler.reset()

      val expected = parseMemOpsCSV(p, moduleInst)

      println(relationalMemoryLogger.computePrecision(expected))
      println(nonRelationalMemoryLogger.computePrecision(expected))
    }
  }

  def parseMemOpsCSV(p: java.nio.file.Path, moduleInstance: ModuleInstance): SortedMap[InstLoc, Set[HeapCtx]] =
    val reader = CSVReader.open(p.toString + ".memops.csv")
    SortedMap.from(reader.iterator.drop(1).map(parseLoadsCSVLine(using moduleInstance)))

  def parseLoadsCSVLine(using moduleInstance: ModuleInstance)(line: Seq[String]): (InstLoc, Set[HeapCtx]) =
    val Seq(instLocStr, _memOp, heapCtxStr) = line
    val Array(func, pc) = instLocStr.split(':')
    val instLoc = InstLoc.InFunction(func, pc.toInt)
    val heapCtxs = heapCtxStr.split(';').map(_.trim).map(parseHeapCtx).toSet
    (instLoc, heapCtxs)

  def parseHeapCtx(using moduleInstance: ModuleInstance)(heapCtxStr: String): HeapCtx =
    heapCtxStr.take(1) match {
      case "F" => HeapCtx.Fill(FixIn.MostGeneralClientLoop(moduleInstance))
      case "G" => HeapCtx.Global(heapCtxStr.drop(1))
      case "S" =>
        val Array(funcName, offset) = heapCtxStr.drop(1).split('+')
        HeapCtx.Stack(FuncId(funcName), Topped.Actual(offset.toInt))
      case "H" =>
        val Array(func, pc, offset) = heapCtxStr.drop(1).split(Array(':', '+'))
        val instLoc = InstLoc.InFunction(func, pc.toInt)
        HeapCtx.Heap(instLoc, Topped.Actual(offset.toInt))
    }