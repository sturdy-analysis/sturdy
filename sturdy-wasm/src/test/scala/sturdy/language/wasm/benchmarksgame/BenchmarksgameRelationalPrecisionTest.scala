package sturdy.language.wasm.benchmarksgame

import apron.*
import com.github.tototoshi.csv.{CSVReader, CSVWriter, DefaultCSVFormat}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sturdy.control.PrintingControlObserver
import sturdy.fix.StackConfig
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.{FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.abstractions.RelationalInfo.{*, given}
import sturdy.language.wasm.analyses.RelationalAnalysis.HeapCtx
import sturdy.language.wasm.generic.{FixIn, FrameData, FuncId, InstLoc, ModuleInstance}
import sturdy.util.Profiler
import sturdy.values.{*, given}
import swam.syntax

import java.io.File
import java.nio.file.{Files, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameRelationalPrecisionTest extends AnyFunSuite, Matchers, BeforeAndAfterAll:

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;
  val fixpointConfig: FixpointConfig = FixpointConfig(
    stack = StackConfig.StackedStates(),
    iter = sturdy.fix.iter.Config.Innermost
  )

  val csvWriter = {
    val writer = CSVWriter.open(File("relational-precision-test.csv"))
    writer.writeRow(List("filename", "relational_more_precise", "non_relational_more_precise", "same_precision", "incomparable", "total_compared", "not_compared"))
    writer
  }

  override def afterAll(): Unit =
    csvWriter.close()

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith("binarytrees.wasm")).sorted.foreach { p =>
    test(s"${p.getFileName}") {
      val module = Parsing.fromBinary(p)

      val relationalAnalysis = RelationalAnalysis.Instance(Polka(true),FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = true))
      val relationalMemoryLogger = relationalAnalysis.memoryLogger
      relationalAnalysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
      var moduleInst = relationalAnalysis.instantiateModule(module, moduleId = Some(p.getFileName))
      relationalAnalysis.failure.fallible(
        relationalAnalysis.invokeExported(moduleInst, funcName, List.empty)
      )
      Profiler.printLastMeasured()
      Profiler.reset()

      val nonRelationalAnalysis = RelationalAnalysis.Instance(Polka(true),FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = false))
      val nonRelationalMemoryLogger = nonRelationalAnalysis.memoryLogger
      nonRelationalAnalysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
      moduleInst = nonRelationalAnalysis.instantiateModule(module, moduleId = Some(p.getFileName))
      nonRelationalAnalysis.failure.fallible(
        nonRelationalAnalysis.invokeExported(moduleInst, funcName, List.empty)
      )
      Profiler.printLastMeasured()
      Profiler.reset()



      println(s"Relational: ${relationalMemoryLogger}\nNon-Relational: ${nonRelationalMemoryLogger}")

//      val relationalInfos = relationalInfoLogger.getAllInstructionInfos
//      val nonRelationalInfos = nonRelationalInfoLogger.getAllInstructionInfos
//
//      var relationalMorePrecise: Map[(InstLoc,syntax.Inst),(List[UnconstrainedInfo],List[UnconstrainedInfo])] = Map.empty
//      var nonRelationalMorePrecise: Map[(InstLoc,syntax.Inst),(List[UnconstrainedInfo],List[UnconstrainedInfo])] = Map.empty
//      var samePrecision: Map[(InstLoc,syntax.Inst),(List[UnconstrainedInfo],List[UnconstrainedInfo])] = Map.empty
//      var incomparable: Map[(InstLoc,syntax.Inst),(List[UnconstrainedInfo],List[UnconstrainedInfo])] = Map.empty
//      var totalComparedInstructions: Int = 0
//
//      relationalInfos.foreach((loc,infos1) =>
//        nonRelationalInfos.get(loc).foreach(infos2 =>
//          if(infos1.length != infos2.length)
//            throw IllegalStateException(s"List of infos $infos1 and $infos2 for $loc do not have the same length")
//          else {
//            totalComparedInstructions += 1
//            PartialOrder[List[UnconstrainedInfo]].tryCompare(infos1, infos2) match
//              case Some(n) =>
//                if (n == 0)
//                  samePrecision += loc -> (infos1,infos2)
//                else if (n < 0)
//                  relationalMorePrecise += loc -> (infos1,infos2)
//                else /* if(n > 0) */
//                  nonRelationalMorePrecise += loc -> (infos1,infos2)
//              case None =>
//                incomparable += loc -> (infos1,infos2)
//          }
//        )
//      )
//
//      assert(totalComparedInstructions == relationalMorePrecise.size + nonRelationalMorePrecise.size + samePrecision.size + incomparable.size)

//      csvWriter.writeRow(
//        List(p.getFileName,
//          relationalMorePrecise.size.toString,
//          nonRelationalMorePrecise.size.toString,
//          samePrecision.size.toString,
//          incomparable.size.toString,
//          totalComparedInstructions,
//          (relationalInfos.keySet ++ nonRelationalInfos.keySet).size - totalComparedInstructions
//        )
//      )
    }

    def parseLoadsCSV(p: java.nio.file.Path, moduleInstance: ModuleInstance): Map[InstLoc, Set[HeapCtx]] =
      val reader = CSVReader.open(p.toString + ".loads.csv")(using new DefaultCSVFormat {
        override val delimiter = ';'
      })
      reader.iterator.drop(1).map(parseLoadsCSVLine(using moduleInstance)).toMap

    def parseLoadsCSVLine(using moduleInstance: ModuleInstance)(line: Seq[String]): (InstLoc, Set[HeapCtx]) =
      val Seq(instLocStr, heapCtxStr) = line
      val Array(func, pc) = instLocStr.split(':')
      val instLoc = InstLoc.InFunction(func, pc.toInt)
      val heapCtxs = heapCtxStr.split(',').map(parseHeapCtx).toSet
      (instLoc, heapCtxs)

    def parseHeapCtx(using moduleInstance: ModuleInstance)(heapCtxStr: String): HeapCtx =
      heapCtxStr.take(1) match {
        case "F" => HeapCtx.Fill(FixIn.MostGeneralClientLoop(moduleInstance))
        case "G" => HeapCtx.Global(heapCtxStr.drop(1))
        case "S" =>
          val Array(funcName, offset) = heapCtxStr.drop(1).split(':')
          HeapCtx.Stack(FuncId(funcName), Topped.Actual(offset.toInt))
      }
  }