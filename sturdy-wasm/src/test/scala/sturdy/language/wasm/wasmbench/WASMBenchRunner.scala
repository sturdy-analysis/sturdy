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
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.util.{Failure, Success, Try}


enum Analysis:
  case Constant
  case Taint
  case Type
  case All(constantCfg: WasmConfig, taintCfg: WasmConfig, typeCfg: WasmConfig)

  def apply(set: Either[Throwable, RRecord] => Unit, p: Path, funcName: String, config: WasmConfig, binary: Boolean = false): Runnable =
    this match 
      case Analysis.Type => new TypeRunnable(set, p, funcName, config, binary)
      case Analysis.Constant => new ConstantRunnable(set, p, funcName, config, binary)
      case Analysis.Taint => new TaintRunnable(set, p, funcName, config, binary)
      case All(_,_,_) => ???


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
  val skipTestsIncludingIndex: Int
  val saveResultsToDir: Path
}

object WASMBenchRunner:
  val runnerConfig: RunnerConfig = RRecord(
    "filtering" -> Filtering.Filtered,
    "timeLimit" -> new GrainOfTime(600).seconds,
    "analysis" -> // Analysis.Constant,
    Analysis.All(
      WasmConfig(ctx = CallSites(1), fix = FixpointConfig(iter = sturdy.fix.iter.Config.Topmost)),
      WasmConfig(ctx = CallSites(1), fix = FixpointConfig(iter = sturdy.fix.iter.Config.Topmost)),
      WasmConfig(ctx = CallSites(1), fix = FixpointConfig(iter = sturdy.fix.iter.Config.Topmost))
    ),
    "wasmConfig" -> WasmConfig(ctx = CallSites(1), fix = FixpointConfig(iter = sturdy.fix.iter.Config.Topmost)),
    "rootDir" -> Path.of(this.getClass.getResource(s"/sturdy/language/wasm/wasmbench").toURI),
    "warmup" -> true, // default: true
    "logOpenOption" -> StandardOpenOption.CREATE, // default: CREATE
    "logErrors" -> true, // default: true
    "logResults" -> true, // default: true
    "skipTestsIncludingIndex" -> -1,
    "saveResultsToDir" -> Path.of("/Volumes/home/tmp/wasmbench/results-v0")
  ).asInstanceOf[RunnerConfig]

class WASMBenchRunner extends AnyFunSpec:
  
  import WASMBenchRunner.runnerConfig.{filtering, timeLimit, analysis, wasmConfig, rootDir, warmup, logOpenOption,
    logErrors, logResults, skipTestsIncludingIndex, saveResultsToDir}

  val store: Store[String, WASMBenchBinary] = {
    val mdPath = rootDir.resolve(s"sturdy.metadata.$filtering.json")
    val exPath = rootDir.resolve(s"sturdy.funcdefs.$filtering.json")
    new JSONStore(mdPath, exPath)
  }

  describe(s"Running $analysis on every binary exposing a \"_start\" function " +
    s"in the $filtering WasmBench dataset") {

    val pred = (x: WASMBenchBinary) =>
      x.ex.exists {
        case FuncDef(_, _, Some("_start")) => true
        case _ => false
      }

    val binaries = {
      //      val timeOuts = List(
      //        "c1cfe409e18435f0371876cf25ca47621e0e59f73beb0284dbf1b61b7696f7ef")
      store.retrieve(pred).sortWith((x, y) => x.md.sizeBytes < y.md.sizeBytes)
    }.drop(skipTestsIncludingIndex + 1)

    saveResultsToDir.toFile.mkdirs()
    analysis match {
      case Analysis.All(constantCfg, taintCfg, typeCfg) =>

        val (constantSuccLogger, constantExcLogger) = {
          def s(string: String) = s"${Analysis.Constant}.$constantCfg.$string.csv".replace(' ', '-')
          val a = new CsvLogger(saveResultsToDir.resolve(s("results")), logOpenOption, logResults)
          val b = new CsvLogger(saveResultsToDir.resolve(s("exceptions")), logOpenOption, logResults)
          (a,b)
        }
        val (typeSuccLogger, typeExcLogger) = {
          def s(string: String) = s"${Analysis.Type}.$typeCfg.$string.csv".replace(' ', '-')
          val a = new CsvLogger(saveResultsToDir.resolve(s("results")), logOpenOption, logResults)
          val b = new CsvLogger(saveResultsToDir.resolve(s("exceptions")), logOpenOption, logResults)
          (a,b)
        }
        val (taintSuccLogger, taintExcLogger) = {
          def s(string: String) = s"${Analysis.Taint}.$taintCfg.$string.csv".replace(' ', '-')
          val a = new CsvLogger(saveResultsToDir.resolve(s("results")), logOpenOption, logResults)
          val b = new CsvLogger(saveResultsToDir.resolve(s("exceptions")), logOpenOption, logResults)
          (a,b)
        }
        run(binaries, Analysis.Type, typeCfg, typeSuccLogger, typeExcLogger)
        run(binaries, Analysis.Constant, constantCfg, constantSuccLogger, constantExcLogger)
        run(binaries, Analysis.Taint, taintCfg, taintSuccLogger, taintExcLogger)
      case _ =>
        val succLogger: CsvLogger = new CsvLogger(saveResultsToDir.resolve(s"$analysis.$wasmConfig.results.csv".replace(' ', '-')), logOpenOption, logResults)
        val excLogger: CsvLogger = new CsvLogger(saveResultsToDir.resolve(s"$analysis.$wasmConfig.exceptions.csv".replace(' ', '-')), logOpenOption, logErrors)
        run(binaries, analysis, wasmConfig, succLogger, excLogger)
    }

    def run(bins: List[WASMBenchBinary], an: Analysis, cfg: WasmConfig, sLogger: CsvLogger, eLogger: CsvLogger): Unit = {
      if warmup then
        eLogger.log("hash;exceptionMsg")
      it(s"Warm-up until first successful run in $an") {
        val currBin = bins.iterator
        var cont = true
        while cont do
          val md = currBin.next().md
          val name = md.hash;
          val p = WASMBench.mkBinPath(name, filtering)

          var result: Either[Throwable, RRecord] = Left(TimeoutException(s"Test timed out after ${timeLimit.toSeconds} seconds"))

          val t = new Thread(an(v => {result = v}, p, "_start", cfg, true))

          t.start()
          t.join(timeLimit.toMillis)
          if (t.isAlive) {
            t.interrupt()
            t.join()
            t.stop()
          }

          result match {
            case Left(e) => if t.isAlive then {t.interrupt(); t.join()}; println(e.toString)
            case Right(v) => cont = false; sLogger.log(v.getCsvHeaders); println("warmed-up!")
          }
      }

      for {
        (WASMBenchBinary(md, ex), num) <- bins.zipWithIndex
      } do {
        val name = md.hash;
        val p = WASMBench.mkBinPath(name, filtering)
        //      println(s"Test nr.: $num, hash: $name")
        it(s"Test nr. $num, $name: Size in bytes: ${md.sizeBytes} in $an") {
          //        TimeLimitedTests does not always terminate test after the specified time,
          //        utilize 'Thread.join(millis)' instead

          var result: Either[Throwable, RRecord] = Left(TimeoutException(s"Test timed out after ${timeLimit.toSeconds} seconds"))

          val t = new Thread(an(v => {result = v}, p, "_start", cfg, true))

          t.start()
          t.join(timeLimit.toMillis)
          if (t.isAlive) {
            t.interrupt()
            t.join()
            t.stop()
          }

          result match {
            case Left(e) =>
              eLogger.log(s"$name;${e.toString}")
              throw e
            case Right(v) =>
              sLogger.log(v.toCsv)
          }
        }
      }
    }

//    if warmup then
//      excLogger.log("hash;exceptionMsg")
//      it("Warm-up until first successful run") {
//        val currBin = binaries.iterator
//        var cont = true
//        while cont do
//          val md = currBin.next().md
//          val name = md.hash;
//          val p = WASMBench.mkBinPath(name, filtering)
//
//          var result: Either[Throwable, RRecord] = Left(TimeoutException(s"Test timed out after ${timeLimit.toSeconds} seconds"))
//
//          val t = new Thread(analysis(v => {result = v}, p, "_start", wasmConfig, true))
//
//          t.start()
//          t.join(timeLimit.toMillis)
//
//          result match {
//            case Left(e) => if t.isAlive then {t.interrupt; t.join}; println(e.toString)
//            case Right(v) => cont = false; succLogger.log(v.getCsvHeaders); println("warmed-up!")
//          }
//      }
//
//    for {
//      (WASMBenchBinary(md, ex), num) <- binaries.zipWithIndex
//    } do {
//      val name = md.hash;
//      val p = WASMBench.mkBinPath(name, filtering)
//      //      println(s"Test nr.: $num, hash: $name")
//      it(s"Test nr. $num, $name: Size in bytes: ${md.sizeBytes}") {
//        //        TimeLimitedTests does not always terminate test after the specified time,
//        //        utilize 'Thread.join(millis)' instead
//
//        var result: Either[Throwable, RRecord] = Left(TimeoutException(s"Test timed out after ${timeLimit.toSeconds} seconds"))
//
//        val t = new Thread(analysis(v => {result = v}, p, "_start", wasmConfig, true))
//
//        t.start()
//        t.join(timeLimit.toMillis)
//
//        result match {
//          case Left(e) =>
//            if t.isAlive then {t.interrupt(); t.join()}
//            excLogger.log(s"$name;${e.toString}")
//            throw e
//          case Right(v) =>
//            succLogger.log(v.toCsv)
//        }
//      }
//    }
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
