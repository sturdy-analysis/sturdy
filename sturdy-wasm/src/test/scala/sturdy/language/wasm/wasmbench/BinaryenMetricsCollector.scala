package sturdy.language.wasm.wasmbench

import sturdy.language.wasm
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}

import java.nio.file.Path
import scala.io.Source

class BinaryenMetricsCollector(set: Either[Throwable, RRecord] => Unit,
                               p: Path, config: AnalysisConfig,
                               binary: Boolean) extends AnalysisRunnable {

  val timeoutSeconds = config.timeLimit.toSeconds

  override def setRes(v: Either[Throwable, RRecord]): Unit = set(v)

  override def start(): RRecord = {
    val name = p.getFileName.toString

    val originalModule = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)
    val originalInterp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
    val originalModInst = originalInterp.initializeModule(originalModule)
    val allNodes = ControlFlow.allCfgNodes(List(originalModInst))
    val allInstructions = allNodes.filter(_.isInstruction)

    val binaryenPath = p.getParent.getParent
      .resolve(s"binaryen-out-${timeoutSeconds}s")
      .resolve(s"$name.by")
    if (!binaryenPath.toFile.exists())
      throw new IllegalStateException(s"Not found $binaryenPath")

    val binaryenModule = if (binary) Parsing.fromBinary(binaryenPath) else wasm.Parsing.fromText(binaryenPath)
    val binaryenInterp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
    val binaryenModInst = binaryenInterp.initializeModule(binaryenModule)
    val binaryenAllNodes = ControlFlow.allCfgNodes(List(binaryenModInst))
    val binaryenAllInstructions = binaryenAllNodes.filter(_.isInstruction)

    val liveInstructions = binaryenAllInstructions.size
    val deadInstructions = allInstructions.size - liveInstructions
    val deadInstructionPercent = (10000.0 * deadInstructions / allInstructions.size.toDouble).round / 100.0

    val bytimePath = binaryenPath.getParent.resolve(s"$name.bytime")
    val source = Source.fromFile(bytimePath.toFile)
    val line = source.getLines().toList.last
    val subline = line.substring("        ".size, "        ".size + 4)
    println(s"bytime: $subline")
    val durationSeconds = subline.toDouble
    val duration = (durationSeconds * 1000).toInt
    source.close()

    RRecord(
      "hash" -> name,
      "duration" -> duration,
      "allInstructions" -> allInstructions.size,
      "deadInstructions" -> deadInstructions,
      "deadInstructionPercent" -> deadInstructionPercent,
      "liveInstructions" -> liveInstructions
    )
  }
}
object BinaryenMetricsCollector:
  def getCsvHeadders: String =
    RRecord(
      "hash" -> 0,
      "duration" -> 0,
      "allInstructions" -> 0,
      "deadInstructions" -> 0,
      "deadInstructionPercent" -> 0,
      "liveInstructions" -> 0
    ).getCsvHeaders