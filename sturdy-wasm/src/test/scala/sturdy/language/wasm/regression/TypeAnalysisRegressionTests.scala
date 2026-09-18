package sturdy.language.wasm.regression

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sturdy.control.*
import sturdy.effect.failure.AFallible
import sturdy.effect.failure.AFallible.{Failing, Unfailing}
import sturdy.fix
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.Fix.given
import sturdy.language.wasm.analyses.TypeAnalysis.{NumValue, Value}
import sturdy.language.wasm.analyses.{FixpointConfig, TypeAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FrameData, WasmFailure}
import sturdy.values.Powerset

import java.nio.file.Paths
import scala.jdk.StreamConverters.*


class TypeAnalysisRegressionTests extends AnyFunSuite, Matchers:
  Fixpoint.DEBUG = false
  Fixpoint.DEBUG_PRIOR_OUTPUT = false
  Fixpoint.DEBUG_INVARIANTS = false

  runAnalysis("debug.wast", Unfailing(List(Value.Num(NumValue.Int32(TypeAnalysis.topI32)))))
  runAnalysis("symbol_table_empty.wast", Failing(Powerset((WasmFailure.UnboundFunctionIndex, "Num(Int32(int)) in mod"))))

  def runAnalysis(watFile: String, expected: AFallible[List[Value]]): Unit =
    test(watFile) {
      val uri = this.getClass.getResource("/sturdy/language/wasm/regression/"+watFile).toURI
      val path = Paths.get(uri)
      val module = wasm.Parsing.fromText(path)
      val interp = new TypeAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(FixpointConfig(StackedStates(storeNonrecursiveOutput = false, readPriorOutput = true))))
      val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)
      interp.addControlObserver(new PrintingControlObserver()(println))

      val modInst = interp.instantiateModule(module, moduleId = Some("mod"))
      val r = interp.failure.fallible(interp.invokeExported(modInst, "main",  List(Value.Num(NumValue.Int32(TypeAnalysis.topI32)))))

      val cfg = graphBuilder.get

      assert(r == expected)
    }