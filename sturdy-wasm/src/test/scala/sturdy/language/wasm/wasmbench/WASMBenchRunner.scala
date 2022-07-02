package sturdy.language.wasm.wasmbench

import org.scalatest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.time.SpanSugar.GrainOfTime
import org.scalatest.time.Span
import sturdy.fix
import sturdy.fix.{StackConfig, Fixpoint}
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.Insensitive
import sturdy.language.wasm.analyses.{FixpointConfig, TypeAnalysis, CallSites, ConstantAnalysis, ConstantTaintAnalysis, WasmConfig}
import sturdy.language.wasm.generic.FrameData
import swam.syntax.{StoreNInst, StoreInst, LoadInst, LoadNInst}

import java.util.concurrent.{ExecutionException, TimeoutException}
import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration.*
import ExecutionContext.Implicits.global
import scala.language.postfixOps
import java.nio.file.{Path, StandardOpenOption, Paths, Files}
import scala.util.{Try, Success, Failure}

enum AnalysisScope:
  case SingleFunction(id: String)
  case MostGeneralClient

  override def toString: String = this match
    case SingleFunction(id) => s"function \"$id\""
    case MostGeneralClient => "MostGeneralClient"

enum Analysis:
  case Interval(conf: AnalysisConfig)
  case Constant(conf: AnalysisConfig)
  case Taint(conf: AnalysisConfig)
  case Type(conf: AnalysisConfig)

  def config: AnalysisConfig = this match
    case Interval(config) => config
    case Constant(config) => config
    case Taint(config) => config
    case Type(config) => config
  
  override def toString: String = this match
    case Interval(config) => s"IntervalAnalysis(config=${config.wasmConfig},scope=${config.scope})"
    case Constant(config) => s"ConstantAnalysis(config=${config.wasmConfig},scope=${config.scope})"
    case Taint(config) => s"ConstantTaintAnalysis(config=${config.wasmConfig},scope=${config.scope})"
    case Type(config) => s"TypeAnalysis(config=${config.wasmConfig},scope=${config.scope})"

  def csvHeader(): String = this match
    case Interval(_) => IntervalRunnable.getCsvHeadders
    case Constant(_) => ConstantRunnable.getCsvHeadders
    case Taint(_) => TaintRunnable.getCsvHeadders
    case Type(_) => TypeRunnable.getCsvHeadders

  def apply(set: Either[Throwable, RRecord] => Unit, bin: WASMBenchBinary, config: AnalysisConfig, binary: Boolean = false): AnalysisRunnable =
    val name = bin.md.hash
    val p = WASMBench.mkBinPath(name, Filtering.Filtered)
    val params = config.scope match
      case AnalysisScope.SingleFunction(id) =>
        bin.ex.find {
          case FuncDef(_, _, Some(str)) if str == id => true
          case _ => false
        }.get.sig.param
      case AnalysisScope.MostGeneralClient =>
        List.empty

    this match
      case Analysis.Interval(config) =>
        val args = params.map(WASMType.toIntervalAnalysisValue)
        new IntervalRunnable(set, p, config.scope, args, config.wasmConfig, binary)
      case Analysis.Type(config) =>
        val args = params.map(WASMType.toTypeAnalysisValue)
        new TypeRunnable(set, p, config.scope, args, config.wasmConfig, binary)
      case Analysis.Constant(config) =>
        val args = params.map(WASMType.toConstantAnalysisValue)
        new ConstantRunnable(set, p, config.scope, args, config.wasmConfig, binary)
      case Analysis.Taint(config) =>
        val args = params.map(WASMType.toTaintAnalysisValue)
        new TaintRunnable(set, p, config.scope, args, config.wasmConfig, binary)

type RunnerConfig = RRecord {
  val filtering: Filtering
  val analyses: List[Analysis]
  val rootDir: Path
  val datasetFilter: WASMBenchBinary => Boolean
  val skipTestsIncludingIndex: Int
  val takeUntilIndex: Option[Int]
  val onlyBinariesInCSV: Option[Path]
}

type AnalysisConfig = RRecord {
  val timeLimit: Span
  val wasmConfig: WasmConfig
  val scope: AnalysisScope
  val warmup: Boolean
  val saveResultsToDir: Path
  val logOpenOption: StandardOpenOption
  val logErrors: Boolean
  val logResults: Boolean
}

object RunnerConfig:
  val default: RunnerConfig = RRecord(
    "filtering" -> Filtering.Filtered,
    "analyses" -> List(
      Analysis.Constant(AnalysisConfig.callSite1),
//      Analysis.Type(AnalysisConfig.nocontext)
    ),
    "rootDir" -> Path.of(this.getClass.getResource("/sturdy/language/wasm/wasmbench").toURI),
    "datasetFilter" -> ((x: WASMBenchBinary) => true),
//      ((x: WASMBenchBinary) => x.ex.exists {
//      case FuncDef(_, _, Some(name)) if name == "_start" => true
//      case _ => false
//    }),
    "skipTestsIncludingIndex" -> -1, // default = -1
    "takeUntilIndex" -> None, //default = None
    "onlyBinariesInCSV" -> None, // Some(Paths.get("/Users/seba/tmp/wasmbench-mgc/ConstantAnalysis(config=innermost(StackedStates(true))_calls(1),scope=MostGeneralClient).timeout.csv")),
  ).asInstanceOf[RunnerConfig]

object AnalysisConfig:
  val nocontext: AnalysisConfig = RRecord(
    "timeLimit" -> new GrainOfTime(60).seconds,
    "wasmConfig" -> WasmConfig(
      ctx = Insensitive,
      fix = FixpointConfig(fix.iter.Config.Innermost(StackConfig.StackedCfgNodes()))),
    "scope" -> AnalysisScope.MostGeneralClient,
    "warmup" -> false, // default: true
    "saveResultsToDir" -> Path.of("/Users/seba/tmp/wasmbench-mgc"),
    "logOpenOption" -> StandardOpenOption.CREATE_NEW, // default: CREATE_NEW
    "logErrors" -> true, // default: true
    "logResults" -> true // default: true
  ).asInstanceOf[AnalysisConfig]

  val callSite1: AnalysisConfig = RRecord(
    "timeLimit" -> new GrainOfTime(60).seconds,
    "wasmConfig" -> WasmConfig(
      ctx = CallSites(1),
      fix = FixpointConfig(fix.iter.Config.Innermost(StackConfig.StackedCfgNodes()))),
    "scope" -> AnalysisScope.MostGeneralClient,
    "warmup" -> false, // default: true
    "saveResultsToDir" -> Path.of("/Users/seba/tmp/wasmbench-mgc"),
    "logOpenOption" -> StandardOpenOption.CREATE_NEW, // default: CREATE_NEW
    "logErrors" -> true, // default: true
    "logResults" -> true // default: true
  ).asInstanceOf[AnalysisConfig]

  def apply() = nocontext
  def apply(kv_pairs: (String, Any)*): AnalysisConfig = nocontext.updated().asInstanceOf[AnalysisConfig]

class WASMBenchRunner extends AnyFunSpec :

  import RunnerConfig.default.{
    filtering, analyses, rootDir, datasetFilter,
    onlyBinariesInCSV, skipTestsIncludingIndex, takeUntilIndex}

  val store: Store[String, WASMBenchBinary] = {
    val mdPath = rootDir.resolve(s"sturdy.metadata.$filtering.json")
    val exPath = rootDir.resolve(s"sturdy.funcdefs.$filtering.json")
    new JSONStore(mdPath, exPath)
  }

  describe(s"Running $analyses on every binary " +
    s"in the $filtering WasmBench dataset.") {

    val allbinaries = {
      //      val timeOuts = List(
      //        "c1cfe409e18435f0371876cf25ca47621e0e59f73beb0284dbf1b61b7696f7ef")
      val take =
        takeUntilIndex.map({v => (x: List[WASMBenchBinary]) => x.take(v)})
          .getOrElse(x => x)
      take(
        store.retrieve(datasetFilter).sortWith((x, y) => x.md.sizeBytes < y.md.sizeBytes))
    }

    var binaries = allbinaries.drop(skipTestsIncludingIndex + 1)

    if (onlyBinariesInCSV.isDefined) {
      val csvStore = new ResultStore[Result](onlyBinariesInCSV.get)
      val hashes = csvStore.retrieve(_ => true).map(r => r.hash.split('.')(0)).toSet
      binaries = binaries.filter(b => hashes.contains(b.md.hash))
    }

    println(s"Considering ${binaries.size} binaries")

    analyses.foreach(
      analysis =>
        val cfg = analysis.config
        cfg.saveResultsToDir.toFile.mkdirs()
        val (succLogger, excLogger) = FileLogger.succ_excLogger(analysis.toString, cfg)
        if (cfg.logOpenOption == StandardOpenOption.CREATE) {
          excLogger.log("hash;exceptionMsg")
          succLogger.log(analysis.csvHeader())
        }
        run(binaries, analysis, succLogger, excLogger)
    )

    def run(bins: List[WASMBenchBinary], an: Analysis, sLogger: FileLogger, eLogger: FileLogger): Unit = {
      val cfg = an.config
      if (cfg.warmup) {
        it(s"Warm-up until first successful run in $an") {
          val currBin = bins.iterator
          var cont = true
          while cont do
            val bin = currBin.next()

            println(s"Running warmup on ${bin.md.hash}")
            var result: Either[Throwable, RRecord] =
              Left(TimeoutException(s"Test timed out after ${cfg.timeLimit.toSeconds} seconds"))

            val t = new Thread(an(v => {
              result = v
            }, bin, cfg, true))

            t.start()
            t.join(cfg.timeLimit.toMillis)
            if (t.isAlive) {
              t.interrupt()
              t.join()
              t.stop()
            }

            result match {
              case Left(e) => if t.isAlive then {
                t.interrupt();
                t.join()
              };
                println(e.toString)
              case Right(v) => cont = false; println("warmed-up!")
            }
        }
      }

      val indexedBinaries = bins.zip((skipTestsIncludingIndex + 1) to allbinaries.length)
      if (indexedBinaries.size != bins.size)
        throw new IllegalStateException(s"Indexing error: ${indexedBinaries.size} != ${bins.size}")
      for {
        (bin, num) <- indexedBinaries
      } do {
        val md = bin.md
        val name = bin.md.hash
        val p = WASMBench.mkBinPath(name, filtering)
        //      println(s"Test nr.: $num, hash: $name")
        it(s"Test nr. $num of ${allbinaries.size}, $name: Size in bytes: ${md.sizeBytes} in $an") {
          //        TimeLimitedTests does not always terminate test after the specified time,
          //        utilize 'Thread.join(millis)' instead

          println(s"Running test nr. $num of ${allbinaries.size}, $name: Size in bytes: ${md.sizeBytes} in $an")

          var result: Either[Throwable, RRecord] = Left(TimeoutException(s"Test timed out after ${cfg.timeLimit.toSeconds} seconds"))

          try {
            val t = new Thread(an(v => {
              result = v
            }, bin, cfg, true), "WASMBenchRunner-Worker")

            t.start()
            t.join(cfg.timeLimit.toMillis)
            if (t.isAlive) {
              t.interrupt()
              t.join()
              t.stop()
            }
          } catch {
            case e: InterruptedException => // nothing
            case e: OutOfMemoryError =>
              result = Left(e)
              System.gc()
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
      init.flush();
      init.close()
    case _ => ()

  val log: String => Unit = if doLog then this.doLogFun else (x: String) => ()

  private def doLogFun(str: String): Unit =
    val logStream = Files.newOutputStream(p, StandardOpenOption.APPEND)
    logStream.write(str.concat(newLine).getBytes())
    logStream.flush();
    logStream.close()

object FileLogger:
  def succLogger(an: String, cfg: AnalysisConfig): FileLogger =
    new FileLogger(cfg.saveResultsToDir.resolve(
      s"$an.results.csv".replace(' ', '_')),
      cfg.logOpenOption,
      cfg.logResults)

  def excLogger(an: String, cfg: AnalysisConfig): FileLogger =
    new FileLogger(cfg.saveResultsToDir.resolve(
      s"$an.exceptions.csv".replace(' ', '_')),
      cfg.logOpenOption,
      cfg.logErrors)

  def succ_excLogger(an: String, cfg: AnalysisConfig): (FileLogger, FileLogger) =
    (succLogger(an, cfg), excLogger(an, cfg))
