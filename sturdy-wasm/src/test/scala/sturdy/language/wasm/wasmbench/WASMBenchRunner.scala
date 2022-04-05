package sturdy.language.wasm.wasmbench

//import cats.effect.IO

import org.scalatest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.time.SpanSugar.GrainOfTime
import org.scalatest.time.Span
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.{TypeAnalysis, WasmConfig}
import sturdy.language.wasm.generic.FrameData

import java.util.concurrent.{ExecutionException, TimeoutException}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.*
import ExecutionContext.Implicits.global
import scala.language.postfixOps
import java.nio.file.{Files, Path, StandardOpenOption}
import scala.util.{Failure, Success, Try}


enum Analysis:
  case ConstantTest
  case TaintTest
  case TypeTest

object TypeTest:
  def apply(p: Path, funcName: String, binary: Boolean = false): DeadCodeResult =
    Fixpoint.DEBUG = false

    val startTimeMillis = System.currentTimeMillis()

    val name = p.getFileName.toString
    val interp = new TypeAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig())
    val cfg = TypeAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val modInst = interp.initializeModule(module)
      interp.failure.fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val allInstructions = allNodes.filter(_.isInstruction)
    val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0
    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    
    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())
    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")

    val endTimeMillis = System.currentTimeMillis()
    val durationSeconds = endTimeMillis - startTimeMillis

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".types.dot")
    Files.writeString(dotPath, cfg.toGraphViz)

    DeadCodeResult(name,
      durationSeconds,
      allInstructions.size,
      deadInstructions.size,
      deadInstructionPercent,
      deadLabels.size,
      deadLabelsPercent,
      allLabels.size,
      deadLabelsBlock.size,
      deadLabelLoop.size,
      deadLabelsIf.size,
      eliminatable,
      eliminatablePercent)

class WASMBenchRunner extends AnyFunSpec:

  val filtering: Filtering = Filtering.Filtered
  val timeLimit: Span = new GrainOfTime(300).seconds
  val analysis = Analysis.TypeTest
  val rootDir: Path = Path.of(this.getClass.getResource(s"/sturdy/language/wasm/wasmbench").toURI)
  val warmup: Boolean = false
  val logOpenOption: StandardOpenOption = StandardOpenOption.APPEND
  val logErrors: Boolean = false
  val logResults: Boolean = false

  val store: Store = {
    val mdPath = rootDir.resolve(s"sturdy.metadata.$filtering.json")
    val exPath = rootDir.resolve(s"sturdy.funcdefs.$filtering.json")
    new JSONStore(mdPath, exPath)
  }
  
  describe(s"Running $analysis on every binary exposing a \"_start\" function " +
    s"in the $filtering WasmBench dataset") {

    val pred = (x: WASMBenchBinary) =>
      x.ex.exists{
        case FuncDef(_,_,Some("_start")) => true
        case _ => false
      }

    val binaries = {
      val timeOuts = List(
        "c1cfe409e18435f0371876cf25ca47621e0e59f73beb0284dbf1b61b7696f7ef",
        "2acb3a786d7aa11021da486bbdd5cf0ef7ed9b0d7c300ebb348be2884a13536a")
      store.retrieve(timeOuts).sortWith((x, y) => x.md.sizeBytes < y.md.sizeBytes)
    }

    val succLogger: CsvLogger = new CsvLogger(rootDir.resolve(s"$analysis.results.csv"), logOpenOption, logResults)
    val excLogger: CsvLogger = new CsvLogger(rootDir.resolve(s"$analysis.exceptions.csv"), logOpenOption, logErrors)


    if warmup then
      excLogger.log("hash;exceptionMsg")
      it("Warm-up until first successful run") {
        val currBin = binaries.iterator
        var cont = true
        while cont do
          val md = currBin.next().md
          val name = md.hash; val p = WASMBench.mkBinPath(name, filtering);
          lazy val result = Future { TypeTest(p, "_start", true) }
          try
            val res = Await.result(result, timeLimit)
            succLogger.log(res.productElementNames.mkString(";"))
            cont = false
            println("warmed-up!")
          catch
            case e: Exception => println(e.toString)
      }

    for {
      (WASMBenchBinary(md, ex), num) <- binaries.zipWithIndex
    } do {
      val name = md.hash; val p = WASMBench.mkBinPath(name, filtering)
//      println(s"Test nr.: $num, hash: $name")
      it(s"Test nr. $num, $name: Size in bytes: ${md.sizeBytes}") {
//        TimeLimitedTests does not always terminate test after the specified time,
//        utilize 'Future' instead

        lazy val future = Future { TypeTest(p, "_start", true) }
        Try(Await.ready(future, timeLimit)) match
          case Success(f) => f.value.get match
            case Failure(e: ExecutionException) =>
              excLogger.log(s"$name;${e.getCause.toString}"); throw e
            case Failure(e: Exception) =>
              excLogger.log(s"$name;${e.toString}"); throw e
            case Success(value) =>
              succLogger.log(value.toCsv)
          case Failure(e) =>
            excLogger.log(s"$name;${e.toString}"); throw e
        }
      }
    }


class CsvLogger(p: Path, oo: StandardOpenOption, doLog: Boolean = false):
  import StandardOpenOption.*

  (oo, doLog) match
    case (CREATE, true) =>
      if Files.exists(p) then Files.delete(p)
      val init = Files.newOutputStream(p, oo)
      init.flush(); init.close()
    case _ => ()

  val log: String => Unit = if doLog then this.doLogFun else (x: String) => ()

  private def doLogFun(str: String): Unit =
    val logStream = Files.newOutputStream(p, StandardOpenOption.APPEND)
    logStream.write(str.concat("\n").getBytes())
    logStream.flush(); logStream.close()
