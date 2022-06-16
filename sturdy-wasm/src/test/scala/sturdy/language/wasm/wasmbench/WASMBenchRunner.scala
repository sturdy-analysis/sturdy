package sturdy.language.wasm.wasmbench

import org.scalatest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.time.SpanSugar.GrainOfTime
import org.scalatest.time.Span
import sturdy.fix.{Fixpoint, StackConfig}
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
  case Constant(config: WasmConfig)
  case Taint(config: WasmConfig)
  case Type(config: WasmConfig)

  def apply(set: Either[Throwable, RRecord] => Unit, bin: WASMBenchBinary, funcName: String, binary: Boolean = false): Runnable =
    val name = bin.md.hash
    val p = WASMBench.mkBinPath(name, Filtering.Filtered)
    val typeSig = bin.ex.find{
      case FuncDef(_,_,Some(str)) if str == funcName => true
      case _ => false
    }.get.sig
    this match
      case Analysis.Type(config) =>
        val args =
          typeSig.param.map(
            ty => WASMType.toTypeAnalysisValue(ty)
          )
        new TypeRunnable(set, p, funcName, args, config, binary)
      case Analysis.Constant(config) =>
        val args =
          typeSig.param.map(
            ty => WASMType.toConstantAnalysisValue(ty)
          )
        new ConstantRunnable(set, p, funcName, args, config, binary)
      case Analysis.Taint(config) =>
        val args =
          typeSig.param.map(
            ty =>  WASMType.toTaintAnalysisValue(ty)
          )
        new TaintRunnable(set, p, funcName, args, config, binary)


type RunnerConfig = RRecord{
  val filtering: Filtering
  val timeLimit: Span
  val analyses: List[Analysis]
  val rootDir: Path
  val warmup: Boolean
  val logOpenOption: StandardOpenOption
  val logErrors: Boolean
  val logResults: Boolean
  val skipTestsIncludingIndex: Int
  val saveResultsToDir: Path
  val onlyBinariesInCSV: Option[Path]
  val funcName: String
}

object WASMBenchRunner:
  val runnerConfig: RunnerConfig = RRecord(
    "filtering" -> Filtering.Filtered,
    "timeLimit" -> new GrainOfTime(300).seconds,
    "analyses" -> List(Analysis.Constant(
      WasmConfig(ctx = CallSites(1), fix = FixpointConfig(iter = sturdy.fix.iter.Config.Outermost()))
    )),
    "rootDir" -> Path.of(this.getClass.getResource("/sturdy/language/wasm/wasmbench").toURI),
    "warmup" -> false, // default: true
    "logOpenOption" -> StandardOpenOption.APPEND, // default: CREATE
    "logErrors" -> true, // default: true
    "logResults" -> true, // default: true
    "skipTestsIncludingIndex" -> (649 + 56),
    "saveResultsToDir" -> Path.of("/home/code/thesis/wasmbench/results"),
    "onlyBinariesInCSV" -> None, //Some(Paths.get("/home/code/thesis/sturdy.scala/sturdy-wasm/src/test/scala/sturdy/language/wasm/wasmbench/onlyBinariesInCsv.csv")),
    "funcName" -> "main"
  ).asInstanceOf[RunnerConfig]

class WASMBenchRunner extends AnyFunSpec:
  
  import WASMBenchRunner.runnerConfig.{filtering, timeLimit, analyses, rootDir, warmup, logOpenOption,
    logErrors, logResults, skipTestsIncludingIndex, saveResultsToDir, onlyBinariesInCSV, funcName}

  val store: Store[String, WASMBenchBinary] = {
    val mdPath = rootDir.resolve(s"sturdy.metadata.$filtering.json")
    val exPath = rootDir.resolve(s"sturdy.funcdefs.$filtering.json")
    new JSONStore(mdPath, exPath)
  }

  describe(s"Running $analyses on every binary exposing a \"$funcName\" function " +
    s"in the $filtering WasmBench dataset") {

    val pred = (x: WASMBenchBinary) =>
      x.ex.exists {
        case FuncDef(_, _, Some(x)) if x == funcName => true
        case _ => false
      }

    var binaries = {
      //      val timeOuts = List(
      //        "c1cfe409e18435f0371876cf25ca47621e0e59f73beb0284dbf1b61b7696f7ef")
      store.retrieve(pred).sortWith((x, y) => x.md.sizeBytes < y.md.sizeBytes)
    }.drop(skipTestsIncludingIndex + 1)

    if (onlyBinariesInCSV.isDefined) {
      val csvStore = new ResultStore[Result](onlyBinariesInCSV.get)
      val hashes = csvStore.retrieve(_ => true).map(r => r.hash.split('.')(0)).toSet
      binaries = binaries.filter(b => hashes.contains(b.md.hash))
    }

    println(s"Considering ${binaries.size} binaries")

    saveResultsToDir.toFile.mkdirs()

    analyses.foreach(
      analysis => analysis match
        case Analysis.Type(config) =>
          val succLogger: FileLogger =
            new FileLogger(saveResultsToDir.resolve(
              s"$analysis.$config.$funcName.results.csv".replace(' ', '-')),
              logOpenOption,
              logResults)
          val excLogger: FileLogger =
            new FileLogger(saveResultsToDir.resolve(
              s"$analysis.$config.$funcName.exceptions.csv".replace(' ', '-')),
              logOpenOption,
              logErrors)
          run(binaries, analysis, succLogger, excLogger)
        case Analysis.Constant(config) =>
          val succLogger: FileLogger =
            new FileLogger(saveResultsToDir.resolve(
              s"$analysis.$config.$funcName.results.csv".replace(' ', '-')),
              logOpenOption,
              logResults)
          val excLogger: FileLogger =
            new FileLogger(saveResultsToDir.resolve(
              s"$analysis.$config.$funcName.exceptions.csv".replace(' ', '-')),
              logOpenOption,
              logErrors)
          run(binaries, analysis, succLogger, excLogger)
        case Analysis.Taint(config) =>
          val succLogger: FileLogger =
            new FileLogger(saveResultsToDir.resolve(
              s"$analysis.$config.$funcName.results.csv".replace(' ', '-')),
              logOpenOption,
              logResults)
          val excLogger: FileLogger =
            new FileLogger(saveResultsToDir.resolve(
              s"$analysis.$config.$funcName.exceptions.csv".replace(' ', '-')),
              logOpenOption,
              logErrors)
          run(binaries, analysis, succLogger, excLogger)
    )


    def run(bins: List[WASMBenchBinary], an: Analysis, sLogger: FileLogger, eLogger: FileLogger): Unit = {
      if (warmup) {
        eLogger.log("hash;exceptionMsg")
        it(s"Warm-up until first successful run in $an") {
          val currBin = bins.iterator
          var cont = true
          while cont do
            val bin = currBin.next()

            var result: Either[Throwable, RRecord] = Left(TimeoutException(s"Test timed out after ${timeLimit.toSeconds} seconds"))

            val t = new Thread(an(v => {
              result = v
            }, bin, funcName, true))

            t.start()
            t.join(timeLimit.toMillis)
            if (t.isAlive) {
              t.interrupt()
              t.join()
              t.stop()
            }

            result match {
              case Left(e) => if t.isAlive then {
                t.interrupt(); t.join()
              }; println(e.toString)
              case Right(v) => cont = false; sLogger.log(v.getCsvHeaders); println("warmed-up!")
            }
        }
      }

      for {
        (bin, num) <- bins.zipWithIndex
      } do {
        val md = bin.md
        val name = bin.md.hash
        val p = WASMBench.mkBinPath(name, filtering)
        //      println(s"Test nr.: $num, hash: $name")
        it(s"Test nr. $num, $name: Size in bytes: ${md.sizeBytes} in $an") {
          //        TimeLimitedTests does not always terminate test after the specified time,
          //        utilize 'Thread.join(millis)' instead

          var result: Either[Throwable, RRecord] = Left(TimeoutException(s"Test timed out after ${timeLimit.toSeconds} seconds"))

          val t = new Thread(an(v => {result = v}, bin, funcName, true))

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
  }


class FileLogger(p: Path, oo: StandardOpenOption, doLog: Boolean = false, newLine: String = System.lineSeparator()):
  import StandardOpenOption.*

  (oo, doLog) match
    case (CREATE, true) | (CREATE_NEW, true) =>
      if Files.exists(p) then Files.delete(p)
      val init = Files.newOutputStream(p, oo)
      init.flush(); init.close()
    case _ => ()

  val log: String => Unit = if doLog then this.doLogFun else (x: String) => ()

  private def doLogFun(str: String): Unit =
    val logStream = Files.newOutputStream(p, StandardOpenOption.APPEND)
    logStream.write(str.concat(newLine).getBytes())
    logStream.flush(); logStream.close()
