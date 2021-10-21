package sturdy.language.wasm

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.generic.{FrameData, ModuleInstance}

import java.nio.file.Paths

@State(Scope.Thread)
class Binarytrees:
  // questions: * do we include module instantiation in the benchmarks
  //            * what do we want to measure? simply how long does it take to run the interpreter on the benchmarks?

  val (interp, modInst) = getInterpAndModule()
  val funcName = "_start"

  def getInterpAndModule(): (ConstantAnalysis.Instance, ModuleInstance[ConstantAnalysis.Value]) =
    val base = "/sturdy/language/wasm/benchmarksgame/src"

    val resource = classOf[Binarytrees].getResourceAsStream(base ++ "/binarytrees.wasm")
    val module = readBinaryModule(resource)
    val onlyCalls = true
    val interp = ConstantAnalysis(FrameData.empty, Iterable.empty, onlyCalls)
    val modInst = interp.initializeModule(module)
    (interp,modInst)


  @Benchmark
  def runBinarytrees() =
    interp.effects.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )