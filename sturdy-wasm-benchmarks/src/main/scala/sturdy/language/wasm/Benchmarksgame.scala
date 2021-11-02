package sturdy.language.wasm

import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Warmup
import sturdy.fix.Fixpoint
import org.openjdk.jmh.annotations.{Scope, OutputTimeUnit, Mode, Benchmark, State, BenchmarkMode}
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.analyses.FixpointConfig
import sturdy.language.wasm.analyses.WasmConfig
import sturdy.language.wasm.generic.{ModuleInstance, FrameData}

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
class Binarytrees:
  var modInst: ModuleInstance = _
  @Setup(Level.Invocation)
  def setup(analysis: ConstantAnalysisState): Unit =
    val base = "/sturdy/language/wasm/benchmarksgame/src"
    val resource = classOf[Benchmarksgame].getResourceAsStream(base ++ "/binarytrees.wasm")
    val module = readBinaryModule(resource)
    modInst = analysis.interp.initializeModule(module)


@BenchmarkMode(Array(Mode.AverageTime))
@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
class Benchmarksgame:
  Fixpoint.DEBUG = false

  @Benchmark
  def runBinarytrees(analysis: ConstantAnalysisState, binarytrees: Binarytrees) =
    val funcName = "_start"
    analysis.interp.effects.fallible(
      analysis.interp.invokeExported(binarytrees.modInst, funcName, List.empty)
    )