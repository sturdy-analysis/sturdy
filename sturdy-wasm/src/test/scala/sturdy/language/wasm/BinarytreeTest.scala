package sturdy.language.wasm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFallible
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.generic.FrameData
import sturdy.values.Topped

import java.nio.file.{Path, Paths}

class BinarytreeTest extends AnyFlatSpec, Matchers:
  behavior of "BinaryTree"

  val uri = classOf[BinarytreeTest].getResource("/sturdy/language/wasm/benchmarksgame/binarytrees.wast").toURI();
  val path = Paths.get(uri)

  val funcName = "_start"
  val exitCode = Topped.Actual(0)
  it must s"execute $funcName with constant analysis returning exit code $exitCode" in {
    val module = wasm.parse(path)
    val onlyCalls = false
    val interp = ConstantAnalysis(FrameData.empty, Iterable.empty, onlyCalls)
    val modInst = interp.initializeModule(module)
    val result = interp.effects.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )
    println(interp.cfg.toGraphViz)
  }
