package sturdy.language.wasm.benchmarksgame

import cats.effect.Blocker
import cats.effect.IO
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
import sturdy.language.wasm.analyses.ConstantTaintAnalysis
import sturdy.language.wasm.analyses.CallSites
import sturdy.language.wasm.analyses.WasmConfig
import sturdy.language.wasm.generic.FrameData
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.*
import swam.validation.Validator

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.jdk.StreamConverters.*

class BenchmarksgameTaintTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled) taint analysis"

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.headOption.foreach { p =>
    it must s"warm-up constant taint analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
    }
  }

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.foreach { p =>
    it must s"execute constant taint analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
    }
  }


  def run(p: Path, binary: Boolean = false) =
    Fixpoint.DEBUG = false

    val name = p.getFileName
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new ConstantTaintAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(ctx = CallSites(3)))
    val cfg = ConstantTaintAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
    val constants = ConstantTaintAnalysis.constantInstructions(interp)
    val memory = ConstantTaintAnalysis.taintedMemoryAccessLogger(interp)

    val modInst = interp.instantiateModule(module)
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

    val liveInstructions = allInstructions.size - deadInstructions.size
    val constantInstructions = constants.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")

    val allMemoryInstructions = allNodes.filter{
      case CfgNode.Instruction(inst, _) => inst match
        case _: LoadInst | _: LoadNInst | _: StoreInst | _: StoreNInst => true
        case _ => false
      case _ => false
    }
    val taintedAccesses = memory.instructions
    val taintedAccessesPercent = (10000.0 * taintedAccesses.size / allMemoryInstructions.size.toDouble).round / 100.0
    println(s"Found ${taintedAccesses.size} tainted memory accesses, $taintedAccessesPercent% of all load and store instructions in $name.")
    println(s"  This means, ${100.0 - taintedAccessesPercent}% of all load and store instructions in $name are safe.")

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")
