package sturdy.language.wasm

import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.analyses.WasmConfig
import sturdy.language.wasm.generic.FrameData

@State(Scope.Thread)
class ConstantAnalysisState:
  var interp: ConstantAnalysis.Instance = _
  @Setup(Level.Invocation)
  def setup(): Unit =
    interp = new ConstantAnalysis.Instance(WasmConfig.default)