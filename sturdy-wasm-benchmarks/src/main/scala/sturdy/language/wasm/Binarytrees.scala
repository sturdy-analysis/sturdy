package sturdy.language.wasm

import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit, Scope, State}
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.generic.{FrameData, ModuleInstance}

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
class Binarytrees:
  // questions: * do we include module instantiation in the benchmarks
  //            * what do we want to measure? simply how long does it take to run the interpreter on the benchmarks?
  //            * what about print output of the interpreter? can we suppress it?

  val (interp, modInst) = getInterpAndModule()
  val funcName = "_start"

  def getInterpAndModule(): (ConstantAnalysis.Instance, ModuleInstance) =
    val base = "/sturdy/language/wasm/benchmarksgame/src"

    val resource = classOf[Binarytrees].getResourceAsStream(base ++ "/binarytrees.wasm")
    val module = readBinaryModule(resource)
    val onlyCalls = true
    val interp = ConstantAnalysis(FrameData.empty, Iterable.empty, onlyCalls)
    val modInst = interp.initializeModule(module)
    (interp,modInst)


  @Benchmark
  @BenchmarkMode(Array(Mode.SampleTime))
  @OutputTimeUnit(TimeUnit.SECONDS)
  def runBinarytrees() =
    interp.effects.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )