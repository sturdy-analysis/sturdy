package sturdy.language.wasm.wasmbench

import cats.effect.Blocker
import cats.effect.IO
import cats.effect.Timer
import org.json4s.{Formats, ShortTypeHints}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.AFallible
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.abstractions.CfgNode
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.analyses.CallSites
import sturdy.language.wasm.analyses.FixpointConfig
import sturdy.language.wasm.analyses.WasmConfig
import sturdy.language.wasm.generic.FrameData
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.jdk.StreamConverters.*

import org.json4s.*
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read,write}

import org.scalatest.concurrent.TimeLimitedTests

class WASMBenchConstantTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled) constant analysis"
  implicit val formats: Formats = Serialization.formats(ShortTypeHints(List(classOf[TypeDef], classOf[Label])))
  
  val mdUri = this.getClass.getResource("/sturdy/language/wasm/wasmbench/sturdy.metadata.filtered.json").toURI
  val mdPath = Path.of(mdUri)
//  val store = new JSONStore(mdPath,false)
//
//  val typingsUri: URI = this.getClass.getResource("/sturdy/language/wasm/wasmbench/sturdy.funcdefs.filtered.json").toURI
//  val inStream: InputStream = Files.newInputStream(Path.of(typingsUri))
//  val exports = read[Map[String,List[FuncDef]]](inStream)


  val logPath = mdPath.getParent.resolve("wasmbench._start.ConstantTest.csv")
  val logStream = Files.newOutputStream(logPath)
  log("file;" +
    "allInstructions;deadInstructions;deadInstructions%;" +
    "allLabels;deadLabels;deadLabels%;" +
    "deadLabelIfs;deadLabelsBlocks;deadLabelLoops;" +
    "allInstructions;constantInstructions;constantInstructions%" +
    "eliminateable;eliminatable%"
  )

//  val exportNamedFunc = {
//    exports.foldLeft(Map.empty[String, FuncDef])( (acc, el) => {
//      val startFunc = el._2.filter{
//        case FuncDef(_, _, Some("_start")) => true
//        case _ => false
//      }
//      startFunc.headOption match
//        case Some(fd) => acc + (el._1 -> fd)
//        case None => acc
//    })
//  }
//  val noTypeStart = exportNamedFunc.filter(t => {
//    t._2.sig match
//      case TypeDef(_,None,None) => true
//      case _ => false
//  })


  val funcName = "_start"

  val noHalt = "b022a54c3b5546fd09f00e6cb6ed12d04530298cef64182db9e12b8d9b4e4737"
  val path = WASMBench.mkBinPath(noHalt, Filtering.Filtered)
  it must s"execute constant analysis on benchmark ${path.getFileName}" in {
    run(path, binary = true)
  }

//  for {
//    hash <- noTypeStart.keySet
//  } do {
//    val path = WASMBench.mkBinPath(hash, Filtering.Filtered)
//    it must s"execute constant analysis on benchmark ${path.getFileName}" in {
//      run(path, binary = true)
//    }
//  }


  def log(str: String): Unit = {
    logStream.write(str.concat("\n").getBytes())
  }

  def run(p: Path, binary: Boolean = false) =
    Fixpoint.DEBUG = false

    var output = s"${p.getFileName};"

    val name = p.getFileName
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Topmost)))
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
    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
    output += s"${deadInstructions.size};$deadInstructionPercent;${allInstructions.size};"

    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    output += s"${deadLabels.size};$deadLabelsPercent;${allLabels.size};"

    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())
    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
    output += s"${deadLabelsIf.size};${deadLabelsBlock.size};${deadLabelLoop.size};"

    val liveInstructions = allInstructions.size - deadInstructions.size
    val constantInstructions = constants.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")
    output += s"$constantInstructions;$constantInstructionPercent;$liveInstructions;"

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")
    output += s"$eliminatable;$eliminatablePercent"

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".dot")
    val blockCfg = cfg.withBlocks(shortLabels = true)
    Files.writeString(dotPath, blockCfg.toGraphViz)

    log(output)





