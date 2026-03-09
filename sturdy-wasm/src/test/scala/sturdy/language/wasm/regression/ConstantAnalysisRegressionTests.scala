package sturdy.language.wasm.regression

import cats.effect.{Blocker, IO}
import org.scalatest.Assertions.assertResult
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sturdy.control.*
import sturdy.effect.failure.AFallible.Unfailing
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.fix
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.context.Sensitivity
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.analyses.ConstantAnalysis.{Value, NumValue}
import sturdy.language.wasm.analyses.{CallSites, ConstantAnalysis, FixpointConfig, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, WasmFailure}
import sturdy.language.wasm.{ConcreteInterpreter, testCfgDifference}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped.Top
import sturdy.values.integer.{IntegerDivisionByZero, NumericIntervalAbstractly}
import sturdy.values.{Abstractly, Topped}
import swam.syntax.Module
import swam.text.*

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.reflect.{ClassTag, TypeTest}


class ConstantAnalysisRegressionTests extends AnyFunSuite, Matchers:
  Fixpoint.DEBUG = false
  Fixpoint.DEBUG_PRIOR_OUTPUT = false
  Fixpoint.DEBUG_INVARIANTS = false

  runAnalysis("debug.wast", Unfailing(List(Value.Num(NumValue.Int32(Top)))))
  runAnalysis("br_if_broken.wast", Unfailing(List(Value.Num(NumValue.Int32(Top)))))

  def runAnalysis(watFile: String, expected: AFallible[List[Value]]): Unit =
    test(watFile) {
      val uri = this.getClass.getResource("/sturdy/language/wasm/regression/"+watFile).toURI;
      val path = Paths.get(uri)
      val module = wasm.Parsing.fromText(path)
      val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(FixpointConfig(StackedStates(storeNonrecursiveOutput = false, readPriorOutput = true))))
      val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)
      interp.addControlObserver(new PrintingControlObserver()(println))

      val modInst = interp.instantiateModule(module, moduleId = Some("mod"))
      val r = interp.failure.fallible(interp.invokeExported(modInst, "main", List(Value.Num(NumValue.Int32(Top)))))

      val cfg = graphBuilder.get

      assert(r == expected)
    }