package sturdy.language.wasm.benchmarksgame

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.ControlEventGraphBuilder
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.analyses.{TypeAnalysis, WasmConfig}
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.{Parsing, abstractions}

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameTypeTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled) type analysis"

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.headOption.foreach { p =>
    it must s"warm-up type analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
    }
  }

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.foreach { p =>
    it must s"execute type analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
    }
  }



  def run(p: Path, binary: Boolean = false) =
    Fixpoint.DEBUG = false
    
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new TypeAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig())
    val oldCfg = TypeAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)

    val modInst = interp.initializeModule(module)
    interp.failure.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )
    val newCfg = graphBuilder.get

    //    val allNodes = ControlFlow.allCfgNodes(List(modInst))
//    val allInstructions = allNodes.filter(_.isInstruction)
//    val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
//    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0
//    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")
//
//    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
//    val deadLabels = ControlFlow.deadLabels(cfg)
//    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
//    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)
//    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
//    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
//    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
//    val deadLabelLoop = deadLabelsGrouped.getOrElse("Loop", Set())
//    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelLoop.size} loop instructions.")
//
//    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size
//    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0
//    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")

    // write CFG to .dot file
    val dotPath = p.getParent.resolve(p.getFileName.toString + ".types.dot")
    Files.writeString(dotPath, oldCfg.toGraphViz)

    val dotPath2 = p.getParent.resolve(p.getFileName.toString + ".types.new.dot")
    Files.writeString(dotPath2, newCfg.toGraphViz)

