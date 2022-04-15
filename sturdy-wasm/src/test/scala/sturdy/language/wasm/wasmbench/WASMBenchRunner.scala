package sturdy.language.wasm.wasmbench

import org.scalatest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.time.SpanSugar.GrainOfTime
import org.scalatest.time.Span
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.{CallSites, ConstantAnalysis, ConstantTaintAnalysis, FixpointConfig, TypeAnalysis, WasmConfig}
import sturdy.language.wasm.generic.FrameData
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import java.util.concurrent.{ExecutionException, TimeoutException}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.*
import ExecutionContext.Implicits.global
import scala.language.postfixOps
import java.nio.file.{Files, Path, StandardOpenOption}
import scala.util.{Failure, Success, Try}


enum Analysis:
  case Constant
  case Taint
  case Type

  def apply(p: Path, funcName: String, config: WasmConfig, binary: Boolean = false): RRecord =
    this match 
      case Analysis.Type => TypeTest(p, funcName, config, binary)
      case Analysis.Constant => ConstantTest(p, funcName, config, binary)
      case Analysis.Taint => TaintTest(p, funcName, config, binary)

object TaintTest:
  def apply(p: Path, funcName: String, config: WasmConfig, binary: Boolean = false): RRecord =
    Fixpoint.DEBUG = false

    val name = p.getFileName.toString
    val startTimeMillis = System.currentTimeMillis()
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new ConstantTaintAnalysis.Instance(FrameData.empty, Iterable.empty, config)
    val cfg = ConstantTaintAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val constants = ConstantTaintAnalysis.constantInstructions(interp)
    val memory = ConstantTaintAnalysis.taintedMemoryAccessLogger(interp)

    val modInst = interp.initializeModule(module)
    interp.failure.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val allInstructions = allNodes.filter(_.isInstruction)
    val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)

    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())

    val liveInstructions = allInstructions.size - deadInstructions.size
    val constantInstructions = constants.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0

    val allMemoryInstructions = allNodes.filter{
      case CfgNode.Instruction(inst, _) => inst match
        case _: LoadInst | _: LoadNInst | _: StoreInst | _: StoreNInst => true
        case _ => false
      case _ => false
    }
    val taintedAccesses = memory.instructions
    val taintedAccessesPercent = (10000.0 * taintedAccesses.size / allMemoryInstructions.size.toDouble).round / 100.0

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0
    val endTimeMillis = System.currentTimeMillis()
    val duration = endTimeMillis - startTimeMillis

    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")
    println(s"Found ${taintedAccesses.size} tainted memory accesses, $taintedAccessesPercent% of all load and store instructions in $name.")
    println(s"  This means, ${100.0 - taintedAccessesPercent}% of all load and store instructions in $name are safe.")
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")

    RRecord(
      "hash" -> name,
      "duration" -> duration,
      "allInstructions" -> allInstructions.size,
      "deadInstructions" -> deadInstructions.size,
      "deadInstructionPercent" -> deadInstructionPercent,
      "deadLabels" -> deadLabels.size,
      "deadLabelsPercent" -> deadLabelsPercent,
      "allLabels" -> allLabels.size,
      "deadLabelsBlock" -> deadLabelsBlock.size,
      "deadLabelLoop" -> deadLabelLoop.size,
      "deadLabelsIf" -> deadLabelsIf.size,
      "eliminatable" -> eliminatable,
      "eliminatablePercent" -> eliminatablePercent,
      "constantInstructions" -> constantInstructions,
      "constantInstructionPercent" -> constantInstructionPercent,
      "liveInstructions" -> liveInstructions,
      "taintedAccesses" -> taintedAccesses.size,
      "taintedAccessesPercent" -> taintedAccessesPercent,
    )

object ConstantTest:
  def apply(p: Path, funcName: String, config: WasmConfig, binary: Boolean = false): RRecord =
    Fixpoint.DEBUG = false

    val name = p.getFileName.toString

    val startTimeMillis = System.currentTimeMillis()
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, config)
    val cfg = ConstantAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val constants = ConstantAnalysis.constantInstructions(interp)

    val modInst = interp.initializeModule(module)
    val res = interp.failure.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val allInstructions = allNodes.filter(_.isInstruction)
    val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)

    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())

    val liveInstructions = allInstructions.size - deadInstructions.size
    val constantInstructions = constants.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0

    val endTimeMillis = System.currentTimeMillis()
    val duration = endTimeMillis - startTimeMillis

    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    println(s"Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")
    println(s"This analysis can eliminate $eliminatable instructions, $eliminatablePercent% of the ${allInstructions.size} instructions in $name")

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".dot")
    val blockCfg = cfg.withBlocks(shortLabels = true)
    Files.writeString(dotPath, blockCfg.toGraphViz)
    
    RRecord(
      "hash" -> name,
      "duration" -> duration,
      "allInstructions" -> allInstructions.size,
      "deadInstructions" -> deadInstructions.size,
      "deadInstructionPercent" -> deadInstructionPercent,
      "deadLabels" -> deadLabels.size,
      "deadLabelsPercent" -> deadLabelsPercent,
      "allLabels" -> allLabels.size,
      "deadLabelsBlock" -> deadLabelsBlock.size,
      "deadLabelLoop" -> deadLabelLoop.size,
      "deadLabelsIf" -> deadLabelsIf.size,
      "eliminatable" -> eliminatable,
      "eliminatablePercent" -> eliminatablePercent,
      "constantInstructions" -> constantInstructions,
      "constantInstructionPercent" -> constantInstructionPercent,
      "liveInstructions" -> liveInstructions,
    )

object TypeTest:
  def apply(p: Path, funcName: String, config: WasmConfig, binary: Boolean = false): RRecord =
    Fixpoint.DEBUG = false

    val startTimeMillis = System.currentTimeMillis()

    val name = p.getFileName.toString
    val interp = new TypeAnalysis.Instance(FrameData.empty, Iterable.empty, config)
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

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)

    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0

    val endTimeMillis = System.currentTimeMillis()
    val duration = endTimeMillis - startTimeMillis

    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".types.dot")
    Files.writeString(dotPath, cfg.toGraphViz)
    
    RRecord(
      "hash" -> name,
      "duration" -> duration,
      "allInstructions" -> allInstructions.size,
      "deadInstructions" -> deadInstructions.size,
      "deadInstructionPercent" -> deadInstructionPercent,
      "deadLabels" -> deadLabels.size,
      "deadLabelsPercent" -> deadLabelsPercent,
      "allLabels" -> allLabels.size,
      "deadLabelsBlock" -> deadLabelsBlock.size,
      "deadLabelLoop" -> deadLabelLoop.size,
      "deadLabelsIf" -> deadLabelsIf.size,
      "eliminatable" -> eliminatable,
      "eliminatablePercent" -> eliminatablePercent,
    )

type RunnerConfig = RRecord{
  val filtering: Filtering
  val timeLimit: Span
  val analysis: Analysis
  val wasmConfig: WasmConfig
  val rootDir: Path
  val warmup: Boolean
  val logOpenOption: StandardOpenOption
  val logErrors: Boolean
  val logResults: Boolean
}

object WASMBenchRunner:
  val runnerConfig: RunnerConfig = RRecord(
    "filtering" -> Filtering.Filtered,
    "timeLimit" -> new GrainOfTime(1200).seconds,
    "analysis" -> Analysis.Constant,
    "wasmConfig" -> WasmConfig(ctx = CallSites(1), fix = FixpointConfig(iter = sturdy.fix.iter.Config.Topmost)),
    "rootDir" -> Path.of(this.getClass.getResource(s"/sturdy/language/wasm/wasmbench").toURI),
    "warmup" -> true,
    "logOpenOption" -> StandardOpenOption.CREATE,
    "logErrors" -> true,
    "logResults" -> true,
  ).asInstanceOf[RunnerConfig]

class WASMBenchRunner extends AnyFunSpec:
  
  import WASMBenchRunner.runnerConfig.{filtering, timeLimit, analysis, wasmConfig, rootDir, warmup, logOpenOption, logErrors, logResults}

  val store: Store[String, WASMBenchBinary] = {
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
//      val timeOuts = List(
//        "c1cfe409e18435f0371876cf25ca47621e0e59f73beb0284dbf1b61b7696f7ef")
      store.retrieve(pred).sortWith((x, y) => x.md.sizeBytes < y.md.sizeBytes)
    }
    
    val succLogger: CsvLogger = new CsvLogger(rootDir.resolve(s"$analysis.$wasmConfig.results.csv".replace(' ', '-')), logOpenOption, logResults)
    val excLogger: CsvLogger = new CsvLogger(rootDir.resolve(s"$analysis.$wasmConfig.exceptions.csv".replace(' ', '-')), logOpenOption, logErrors)


    if warmup then
      excLogger.log("hash;exceptionMsg")
      it("Warm-up until first successful run") {
        val currBin = binaries.iterator
        var cont = true
        while cont do
          val md = currBin.next().md
          val name = md.hash; val p = WASMBench.mkBinPath(name, filtering)
          lazy val result = Future { analysis(p, "_start", wasmConfig, true) }
          try
            val res = Await.result(result, timeLimit)
            succLogger.log(res.getCsvHeaders)
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

        lazy val future = Future { analysis(p, "_start", wasmConfig, true) }
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
