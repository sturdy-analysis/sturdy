package sturdy.language.wasm.constant

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.AFallible
import sturdy.effect.failure.FailureKind
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.language.wasm.analyses.TypeAnalysis
import sturdy.language.wasm.analyses.TypeAnalysis.*
import sturdy.language.wasm.analyses.CallSites
import sturdy.language.wasm.analyses.TypeAnalysis
import sturdy.language.wasm.analyses.WasmConfig
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.generic.UnreachableInstruction
import sturdy.values.Topped
import sturdy.values.integer.IntegerDivisionByZero

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*

import scala.reflect.ClassTag
import scala.reflect.TypeTest


class TypeAnalysisTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm type analysis"

  val uriSimple = classOf[TypeAnalysisTest].getResource("/sturdy/language/wasm/simple.wast").toURI();
  val uriFact = classOf[TypeAnalysisTest].getResource("/sturdy/language/wasm/fact.wast").toURI();
  val simple = Paths.get(uriSimple)
  val fact = Paths.get(uriFact)

  testFunction(simple, "const", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "first", List(Value.Int32(topI32), Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "second", List(Value.Int32(topI32), Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "test-mem", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "nesting", List(Value.Float32(topF32), Value.Float32(topF32)), List(Value.Float32(topF32)))
  testFunction(simple, "test-br3", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "test-br-and-return", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "test-br-and-return2", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "test-br-and-return3", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "test-br-and-return4", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "test-unreachable5", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "test-global", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFunction(simple, "test-call-indirect-parametric", List(Value.Int32(topI32)), List(Value.Int32(topI32)))
  testFailingFunction(simple, "division", List(Value.Int32(topI32), Value.Int32(topI32)), IntegerDivisionByZero)
  testFunction(simple, "effects", List(Value.Int32(topI32)), List(Value.Int32(topI32)))

  testFunction(fact, "fac-rec", List(Value.Int64(topI64)), List(Value.Int64(topI64)))
  testFunction(fact, "fac-iter", List(Value.Int64(topI64)), List(Value.Int64(topI64)))
  testFunction(fact, "fac-rec-named", List(Value.Int64(topI64)), List(Value.Int64(topI64)))
  testFunction(fact, "fac-iter-named", List(Value.Int64(topI64)), List(Value.Int64(topI64)))
  testFunction(fact, "fac-opt", List(Value.Int64(topI64)), List(Value.Int64(topI64)))



  def testFunction(path: Path, funcName: String, args: List[Value], expected: List[Value]) =
    it must s"execute $funcName withs args $args with result $expected" in {
      val res = runTypeAnalysis(path, funcName, args)
      res match
        case AFallible.Unfailing(vals) => assertResult(expected)(vals)
        case AFallible.MaybeFailing(vals, _) => assertResult(expected)(vals)
        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind) =
    it must s"execute $funcName with args $args throwing exception $failureKind" in {
      val res = runTypeAnalysis(path, funcName, args)//args.map(ConstantAnalysis.liftConcreteValue))
      res match
        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
    }


def runTypeAnalysis(path: Path, funName: String, args: List[Value]): AFallible[List[Value]] =
  val module = wasm.parse(path)

  val interp = TypeAnalysis(FrameData.empty, Iterable.empty)(WasmConfig(ctx = CallSites(0)))
  val cfg = TypeAnalysis.controlFlow(CfgConfig.AllNodes(true), interp)

  val modInst = interp.initializeModule(module)
  val result = interp.effects.fallible(
    interp.invokeExported(modInst, funName, args)
  )
//  println(cfg.toGraphViz)

  val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
  val deadLabels = ControlFlow.deadLabels(cfg)
  println(s"Found ${deadInstructions.size} dead instructions")
  println(s"Found ${deadLabels.size} dead labels")
  result
