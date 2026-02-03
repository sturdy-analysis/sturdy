package dwarfSupport

import apron.*
import com.github.tototoshi.csv.{CSVReader, CSVWriter, DefaultCSVFormat}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.{BeforeAndAfterAll, Suites}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sturdy.control.PrintingControlObserver
import sturdy.fix.StackConfig
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.{FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.analyses.RelationalAnalysis.ByteMemoryCtx
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

val writer: CSVWriter = CSVWriter.open(File("benchmarks-game-precision-test.csv"))

class SimpleExampleRelationalTest extends Suites(
  //BenchmarksgameRelationalPrecisionTest(newManager = Polka(true), relational = true, ssa = false),
  //BenchmarksgameRelationalPrecisionTest(newManager = Octagon(), relational = true, ssa = false),
  MinimalExampleRelationalTest(newManager = Box(), relational = true, ssa = false), //<-run this
  //BenchmarksgameRelationalPrecisionTest(newManager = Polka(true), relational = true, ssa = true),
  //BenchmarksgameRelationalPrecisionTest(newManager = Octagon(), relational = true, ssa = true),
  MinimalExampleRelationalTest(newManager = Box(), relational = true, ssa = true),
  //BenchmarksgameRelationalPrecisionTest(newManager = Box(), relational = false)
), BeforeAndAfterAll:

  override def beforeAll(): Unit =
    writer.writeRow(List("program", "analysis", "ssa", "precise_loads", "imprecise_loads", "precise_stores", "imprecise_stores", "env_size", "byte_size", "time"))

  override def afterAll(): Unit =
    writer.close

class MinimalExampleRelationalTest(newManager: => Manager, relational: Boolean, ssa: Boolean = false) extends AnyFunSpec, Matchers:

  val manager = newManager
  val funcName = "_start"
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/dwarf-test/out/").toURI;

  val fixpointConfig: FixpointConfig = FixpointConfig(
    stack = StackConfig.StackedStates(storeIntermediateOutput = false, readPriorOutput = false),
    iter = sturdy.fix.iter.Config.Innermost
  )

  // These programs are not analyzed
  val excludedDirs: Set[String] = Set(
    "constchardifferences",
    "subprogramBody",
    "nestedfunction"
  )

  val analysisName: String = if (relational) s"${manager.getClass.getSimpleName}, rel: $relational, ssa: $ssa" else "non-relational"

  describe(analysisName) {
    Files.list(Paths.get(uri)).toScala(List)
      .filter(Files.isDirectory(_))
      .filterNot(dir => excludedDirs.contains(dir.getFileName.toString))
      .flatMap { dir => Files.list(dir).toScala(List) }
      .filter(p => p.toString.endsWith(".wasm"))
      .sorted
      .foreach { p =>
        println(s"analyzing path: $p")
        it(s"${p.getFileName}") {
          if(manager.isInstanceOf[Polka] && ssa && p.endsWith("reverse-complement.wasm")) 
            cancel("timeout")
            
          val module = Parsing.fromBinary(p)
          
          val analysis = RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = relational, localSSA = ssa))
          val memoryLogger = analysis.memoryLogger
          val abstractDomainSizeLogger = analysis.abstractDomainSizeLogger
          analysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
          
          val moduleInst = analysis.instantiateModule(module, moduleId = Some(p.getFileName))
          
          Profiler.addTime("analysis-time") {
            analysis.failure.fallible(
              analysis.invokeExported(moduleInst, funcName, List.empty)
            )
          }
          val analysisTime = Profiler.get("analysis-time").get
          
          Profiler.printLastMeasured()
          Profiler.reset()
          
          //csv files do not exist (yet)
          //val expected = parseMemOpsCSV(p, moduleInst)
          
          //writeCSV(p, analysis, memoryLogger, expected, abstractDomainSizeLogger, analysisTime)
      }
    }
  }

  def writeCSV(programPath: Path,
               analysis: RelationalAnalysis.Instance,
               memoryLogger: analysis.MemoryLogger,
               expected: SortedMap[InstLoc, Set[ByteMemoryCtx]],
               abstractDomainSizeLogger: analysis.AbstractDomainSizeLogger,
               time: Long
              ): Unit =
    val program = programPath.getFileName
    val precision: memoryLogger.Precision = memoryLogger.computePrecision(expected)
    val preciseLoads = filterLoads(precision.precise)
    val impreciseLoads = filterLoads(precision.imprecise)
    val preciseStores = filterStores(precision.precise)
    val impreciseStores= filterStores(precision.imprecise)

    writer.writeRow(
      List(
        program,
        analysisName,
        s"$ssa",
        s"${preciseLoads.size}",
        s"${impreciseLoads.size}",
        s"${preciseStores.size}",
        s"${impreciseStores.size}",
        s"${abstractDomainSizeLogger.getEnvSize}",
        s"${abstractDomainSizeLogger.getByteSize}",
        s"${time}"
      )
    )

  inline def filterLoads(map: SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])]): SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])] =
    map.filter { case (key, (_: (LoadInst | LoadNInst), _)) => true; case _ => false }

  inline def filterStores(map: SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])]): SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])] =
    map.filter { case (key, (_: (StoreInst | StoreNInst), _)) => true; case _ => false }


  def parseMemOpsCSV(p: java.nio.file.Path, moduleInstance: ModuleInstance): SortedMap[InstLoc, Set[ByteMemoryCtx]] =
    val reader = CSVReader.open(p.toString + ".memops.csv")
    SortedMap.from(reader.iterator.drop(1).map(parseLoadsCSVLine(using moduleInstance)))

  def parseLoadsCSVLine(using moduleInstance: ModuleInstance)(line: Seq[String]): (InstLoc, Set[ByteMemoryCtx]) =
    val Seq(instLocStr, _memOp, heapCtxStr) = line
    val Array(func, pc) = instLocStr.split(':')
    val instLoc = InstLoc.InFunction(func, pc.toInt)
    val heapCtxs = heapCtxStr.split(';').map(_.trim).map(parseHeapCtx).toSet
    (instLoc, heapCtxs)

  def parseHeapCtx(using moduleInstance: ModuleInstance)(heapCtxStr: String): ByteMemoryCtx =
    heapCtxStr.take(1) match {
      case "F" => ByteMemoryCtx.Fill(FixIn.MostGeneralClientLoop(moduleInstance))
      case "G" => ByteMemoryCtx.Global(heapCtxStr.drop(1))
      case "S" =>
        val Array(funcName, offset) = heapCtxStr.drop(1).split('+')
        ByteMemoryCtx.Stack(FuncId(funcName), offset.toInt)
      case "H" =>
        val Array(func, pc, offset) = heapCtxStr.drop(1).split(Array(':', '+'))
        val instLoc = InstLoc.InFunction(func, pc.toInt)
        ByteMemoryCtx.Heap(instLoc, offset.toInt)
    }
