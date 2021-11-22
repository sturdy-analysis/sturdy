package sturdy.language.wasm.constant

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.AFallible
import sturdy.*
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.abstractions.CfgNode
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.analyses.CallSites
import sturdy.language.wasm.analyses.WasmConfig
import sturdy.language.wasm.generic.FrameData
import sturdy.report.Properties
import sturdy.report.Report
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.collection.mutable.ListBuffer
import scala.jdk.StreamConverters.*

class BenchmarksgameConstantTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame constant analysis"

  //val testcases = List("binarytrees", "fankuchredux", "mandelbrot", "nbody", "spectral-norm")
  val funcName = "_start"

  val uri = classOf[BenchmarksgameConstantTest].getResource("/sturdy/language/wasm/benchmarksgame").toURI();

  val report: Report = new Report

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wast")).sorted.foreach { p =>
    it must s"execute constant analysis on benchmark ${p.getFileName}" in {
      run(p)
    }
  }

  report.results.foreach(println)


  def run(p: Path, binary: Boolean = false): Unit =
    Fixpoint.DEBUG = false

    val name = p.getFileName.toString
    val module = if (binary) readBinaryModule(p) else wasm.parse(p)

    val analysis = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(ctx = CallSites(3)))
    val analysisID = analysis.toString
    val cfg = ConstantAnalysis.controlFlow(CfgConfig.AllNodes(false), analysis)
    val constants = ConstantAnalysis.constantInstructions(analysis)
    
    val modInst = analysis.initializeModule(module)
    analysis.failure.fallible(
      analysis.invokeExported(modInst, funcName, List.empty)
    )

    val allNodes = ControlFlow.allCfgNodes(List(modInst))
    val allInstructions = allNodes.filter(_.isInstruction)
    val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
    val deadInstructionPercent = (10000.0 * deadInstructions.size / allInstructions.size.toDouble).round / 100.0
    println(s"Found ${deadInstructions.size} dead instructions, $deadInstructionPercent% of the ${allInstructions.size} instructions in $name")

    report.addInfo(name, wasm.InstructionCountProperty, allInstructions.size)
    report.addResult(name, analysisID + " " + wasm.DeadInstructionCountProperty, deadInstructions.size)


    val allLabels = allNodes.filter(_.isInstanceOf[CfgNode.Labled])
    val deadLabels = ControlFlow.deadLabels(cfg)
    val deadLabelsPercent = (10000.0 * deadLabels.size / allLabels.size.toDouble).round / 100.0
    val deadLabelsGrouped = deadLabels.groupBy(_.inst.getClass.getSimpleName)
    println(s"Found ${deadLabels.size} dead labels, $deadLabelsPercent% of the ${allLabels.size} labels in $name.")
    val deadLabelsIf = deadLabelsGrouped.getOrElse("If", Set())
    val deadLabelsBlock = deadLabelsGrouped.getOrElse("Block", Set())
    val deadLabelsLoop = deadLabelsGrouped.getOrElse("Loop", Set())
    println(s"  Can optimize ${deadLabelsIf.size} if instructions; can eliminate ${deadLabelsBlock.size} block and ${deadLabelsLoop.size} loop instructions.")

    report.addInfo(name, wasm.LabelCountProperty, allInstructions.size)
    report.addResult(name, analysisID + " " + wasm.DeadLabelCountProperty, deadLabels.size)
    report.addResult(name, analysisID + " " + wasm.deadLabelCountProperty("if"), deadLabelsIf.size)
    report.addResult(name, analysisID + " " + wasm.deadLabelCountProperty("block"), deadLabelsBlock.size)
    report.addResult(name, analysisID + " " + wasm.deadLabelCountProperty("loop"), deadLabelsLoop.size)

    val liveInstructions = allInstructions.size - deadInstructions.size
    val constantInstructions = constants.get.size
    val constantInstructionPercent = (10000.0 * constantInstructions / liveInstructions.toDouble).round / 100.0
    println(s"Found $constantInstructions constant instructions, $constantInstructionPercent% of the $liveInstructions live instructions in $name")
    report.addResult(name, analysisID + " " + wasm.ConstantInstructionCountProperty, constantInstructions)

    val eliminatable = deadInstructions.size + deadLabelsBlock.size + deadLabelsLoop.size + constantInstructions
    val eliminatablePercent = (10000.0 * eliminatable / allInstructions.size.toDouble).round / 100.0
    println(s"This analysis can eliminate $eliminatable nodes, $eliminatablePercent% of the ${allInstructions.size} nodes in $name")

//    // write CFG to .dot file
//    val dotPath = p.getParent.resolve(p.getFileName.toString + ".dot")
//    Files.writeString(dotPath, cfg.toGraphViz)

  def readBinaryModule(path: Path): Module =
    implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
    Blocker[IO].use { blocker =>
      for {
        validator <- Validator[IO](blocker)
        loader = new ModuleLoader[IO]()
        binaryParser = new ModuleParser[IO](validator)
        mod <- binaryParser.parse(loader.sections(path, blocker))
      } yield mod
    }.unsafeRunSync()
