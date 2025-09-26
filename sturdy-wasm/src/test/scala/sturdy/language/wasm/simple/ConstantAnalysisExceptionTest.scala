package sturdy.language.wasm.simple

import cats.effect.{Blocker, IO}
import org.scalatest.Assertions.assertResult
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.*
import sturdy.effect.failure.AFallible.Unfailing
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.fix
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.analyses.ConstantAnalysis.Value
import sturdy.language.wasm.analyses.{CallSites, ConstantAnalysis, FixpointConfig, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, WasmFailure}
import sturdy.language.wasm.{ConcreteInterpreter, ControlEventGraphBuilderDebug, testCfgDifference}
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


class ConstantAnalysisExceptionTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm constant analysis"
  Fixpoint.DEBUG = false
  Fixpoint.DEBUG_PRIOR_OUTPUT = false
  Fixpoint.DEBUG_INVARIANTS = false


  private val uri = this.getClass.getResource("/sturdy/language/wasm/debug.wast").toURI;
  private val path = Paths.get(uri)

  private val module = wasm.Parsing.fromText(path)
  private val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig(FixpointConfig(StackedStates(storeNonrecursiveOutput = false, readPriorOutput = true))))
  private val graphBuilder = interp.addControlObserver(new ControlEventGraphBuilder)

  private val modInst = interp.initializeModule(module, moduleId=Some("mod"))
  val r = interp.failure.fallible(interp.invokeExported(modInst, "main",List(Value.Int32(Top))))

  private val cfg = graphBuilder.get

  assert(r == Unfailing(List(Value.Int32(Top))))
