package sturdy.language.wasm.simple

import apron.*
import cats.effect.{Blocker, IO}
import org.scalatest.Suites
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.apron.*
import sturdy.control.{ControlEventGraphBuilder, PrintingControlObserver}
import sturdy.effect.failure.{AFallible, FailureKind}
import sturdy.fix
import sturdy.fix.StackConfig
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.analyses.RelationalAnalysis.*
import sturdy.language.wasm.analyses.RelationalAnalysis.Type.*
import sturdy.language.wasm.analyses.{CallSites, FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, WasmFailure}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.floating.FloatSpecials
import sturdy.values.{Abstractly, Topped}
import sturdy.values.integer.IntegerDivisionByZero
import swam.syntax.Module
import swam.text.*

import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.reflect.{ClassTag, TypeTest}

final class WasmRelationalAnalysisSimpleTests extends Suites(
  new RelationalAnalysisSimpleTest(using new Polka(true)),
  new RelationalAnalysisSimpleTest(using new Octagon),
  new RelationalAnalysisSimpleTest(using new Box),
)

final class RelationalAnalysisSimpleTest(using apronManager: apron.Manager) extends AnyFlatSpec, Matchers:
  behavior of "Relational Analysis Simple Tests with " + apronManager.getClass.getSimpleName

  val uriSimple = this.getClass.getResource("/sturdy/language/wasm/simple.wast").toURI;
  val uriFact = this.getClass.getResource("/sturdy/language/wasm/fact.wast").toURI;
  val simple = Paths.get(uriSimple)
  val fact = Paths.get(uriFact)


  def i32(expr: ApronExpr[VirtAddr, Type]) = Value.Int32(Left(expr))
  val topi32 = Value.Int32(RelationalAnalysis.topI32)
  val topi64 = Value.Int64(RelationalAnalysis.topI64)
  val topf32 = Value.Float32(RelationalAnalysis.topF32)
  val topf32NoSpecials = Value.Float32(RelationalAnalysis.topF32.setFloatSpecials(FloatSpecials.Bottom))
  val topf64 = Value.Float64(RelationalAnalysis.topF64)
  val topf64NoSpecials = Value.Float64(RelationalAnalysis.topF64.setFloatSpecials(FloatSpecials.Bottom))

  {
    import sturdy.language.wasm.ConcreteInterpreter.Value
    testFunctionConstantArgs(simple, "noop", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "const", List(Value.Int32(5)), List(Value.Int32(5)))
    testFunctionConstantArgs(simple, "first", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(1)))
    testFunctionConstantArgs(simple, "second", List(Value.Int32(1), Value.Int32(2)), List(Value.Int32(2)))
      testFunctionConstantArgs(simple, "test-call-indirect", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "call-first", List.empty, List(Value.Int32(0)))
    testFunctionConstantArgs(simple, "nesting", List(Value.Float32(0), Value.Float32(2)), List(Value.Float32(0)))
    testFunctionConstantArgs(simple, "as-br_table-index", List.empty, List.empty)
    testFunctionConstantArgs(simple, "test-br1", List.empty, List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br2", List.empty, List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-br3", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br3", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-br-and-return", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br-and-return", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-br-and-return2", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br-and-return2", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-br-and-return3", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-br-and-return3", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-unreachable", List.empty, List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-unreachable2", List.empty, List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-unreachable3", List.empty, List(Value.Int32(42)))
    testFailingFunction(simple, "test-unreachable4", List.empty, WasmFailure.UnreachableInstruction)
    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(0)), List(Value.Int32(42)))
    testFunctionConstantArgs(simple, "test-unreachable5", List(Value.Int32(1)), List(Value.Int32(43)))
    testFunctionConstantArgs(simple, "test-global", List(Value.Int32(0)), List(Value.Int32(1)))
    testFunctionConstantArgs(simple, "test-global", List(Value.Int32(1)), List(Value.Int32(2)))
    testFunctionConstantArgs(simple, "test-call-indirect-parametric", List(Value.Int32(0)), List(Value.Int32(0)))
    testFailingFunction(simple, "division", List(i32(ApronExpr.intLit(1, I32Type)), i32(ApronExpr.intLit(0, I32Type))), IntegerDivisionByZero)
    testFunctionConstantArgs(simple, "effects", List(Value.Int32(1)), List(Value.Int32(-14)))
    testFunctionConstantArgs(fact, "fac-rec", List(Value.Int64(0)), List(Value.Int64(1)))
  }
  testFunction(simple, "const", List(topi32), List(topi32))
  testFunction(simple, "first", List(i32(ApronExpr.intLit(1, I32Type)), topi32), List(i32(ApronExpr.intLit(1, I32Type))))
  testFunction(simple, "first", List(topi32, i32(ApronExpr.intLit(2, I32Type))), List(topi32))
  testFunction(simple, "second", List(i32(ApronExpr.intLit(1, I32Type)), topi32), List(topi32))
  testFunction(simple, "second", List(topi32, i32(ApronExpr.intLit(2, I32Type))), List(i32(ApronExpr.intLit(2, I32Type))))
  testFunction(simple, "test-mem0", List(topi32), List(topi32))
  testFunction(simple, "test-mem", List(topi32), List(topi32))
  testFunction(simple, "test-size", List.empty, List(topi32))
  testFunction(simple, "test-memgrow", List.empty, List(topi32, topi32))
  testFunction(simple, "nesting", List(topf32NoSpecials, Value.Float32(ApronExpr.floatConstant(DoubleScalar(2), FloatSpecials.Top, F32Type))), List(topf32NoSpecials))
  testFunction(simple, "nesting", List(Value.Float32(ApronExpr.floatConstant(DoubleScalar(1), FloatSpecials.Top, F32Type)), topf32NoSpecials), List(topf32NoSpecials))
  testFunction(simple, "test-br3", List(topi32), List(i32(ApronExpr.intInterval(42, 43, I32Type))))
  testFunction(simple, "test-br-and-return", List(topi32), List(i32(ApronExpr.intInterval(42, 43, I32Type))))
  testFunction(simple, "test-br-and-return2", List(topi32), List(i32(ApronExpr.intInterval(42, 43, I32Type))))
  testFunction(simple, "test-br-and-return3", List(topi32), List(i32(ApronExpr.intInterval(42, 43, I32Type))))
  testFunction(simple, "test-br-and-return4", List(topi32), List(i32(ApronExpr.intLit(42, I32Type))))
  testFunction(simple, "test-unreachable5", List(topi32), List(i32(ApronExpr.intInterval(42, 43, I32Type))))
  testFunction(simple, "test-global", List(topi32), List(i32(ApronExpr.intInterval(1, 2, I32Type))))
  testFunction(simple, "test-call-indirect-parametric", List(topi32), List(i32(ApronExpr.intLit(0, I32Type))))
  testFailingFunction(simple, "division", List(i32(ApronExpr.intLit(1, I32Type)), topi32), IntegerDivisionByZero)
  testFunction(simple, "effects", List(topi32), List(i32(ApronExpr.intInterval(-14, -6, I32Type))))

  testFunction(simple, "operand_stack_is_immutable", List(), List(i32(ApronExpr.intInterval(0, 0, I32Type))))

  (1 to 8).foreach { arg =>
    testFunction(fact, "fac-rec", List(Value.Int64(ApronExpr.intLit(arg, I64Type))), List(topi64))
  }
  testFunction(fact, "fac-rec", List(Value.Int64(ApronExpr.intLit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-iter", List(Value.Int64(ApronExpr.intLit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-rec-named", List(Value.Int64(ApronExpr.intLit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-iter-named", List(Value.Int64(ApronExpr.intLit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-opt", List(Value.Int64(ApronExpr.intLit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-rec", List(topi64), List(topi64))
  testFunction(fact, "fac-iter", List(topi64), List(topi64))
  testFunction(fact, "fac-rec-named", List(topi64), List(topi64))
  testFunction(fact, "fac-iter-named", List(topi64), List(topi64))
  testFunction(fact, "fac-opt", List(topi64), List(topi64))

  def testFunctionConstantArgs(path: Path, funcName: String, args: List[ConcreteInterpreter.Value], expectedResult: List[ConcreteInterpreter.Value]) =
    testFunction(path, funcName, args.map(Abstractly.apply), expectedResult.map(Abstractly.apply))

  def testFunction(path: Path, funcName: String, args: List[Value], expected: List[Value]) =
    it must s"execute $funcName withs args $args with result $expected with stacked states" in {
      val res = runAnalysis(path, funcName, args, StackConfig.StackedStates())
      res match
        case AFallible.Unfailing(vals) => assertResult(expected)(vals)
        case AFallible.MaybeFailing(vals, _) => assertResult(expected)(vals)
        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
        case AFallible.Diverging(recur) => assert(false, s"Expected $expected but execution diverged: $recur")
    }
//    it must s"execute $funcName withs args $args with result $expected with stacked frames" in {
//      val res = runIntervalAnalysis(path, funcName, args, StackConfig.StackedCfgNodes())
//      res match
//        case AFallible.Unfailing(vals) => assertResult(expected)(vals)
//        case AFallible.MaybeFailing(vals, _) => assertResult(expected)(vals)
//        case AFallible.Failing(fails) => assert(false, s"Expected $expected but execution failed: $fails")
//        case AFallible.Diverging(recur) => assert(false, s"Expected $expected but execution diverged: $recur")
//    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind): Unit =
    it must s"execute $funcName with args $args throwing exception $failureKind with stacked states" in {
      val res = runAnalysis(path, funcName, args, StackConfig.StackedStates())
      res match
        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
    }
//    it must s"execute $funcName with args $args throwing exception $failureKind with stacked frames" in {
//      val res = runIntervalAnalysis(path, funcName, args, StackConfig.StackedCfgNodes())
//      res match
//        case AFallible.Unfailing(vals) => assert(false, s"Expected $failureKind but execution succeeded: $vals")
//        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == failureKind))
//        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == failureKind))
//        case AFallible.Diverging(recur) => assert(false, s"Expected $failureKind but execution diverged: $recur")
//    }


def runAnalysis(path: Path, funName: String, args: List[Value], stackConfig: StackConfig)(using apronManager: apron.Manager): AFallible[List[Value]] =
  Profiler.reset()

  val module = wasm.Parsing.fromText(path)
  val interp = new RelationalAnalysis.Instance(apronManager, FrameData.empty, Iterable.empty, WasmConfig(FixpointConfig(fix.iter.Config.Innermost(stackConfig))))
  val constants = interp.constantInstructions
  interp.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
  val cfg = interp.addControlObserver(new ControlEventGraphBuilder)

  val modInst = interp.initializeModule(module)
  val result = interp.failure.fallible {
    interp.invokeExported(modInst, funName, args).map(interp.getInterval)
  }
//  println(cfg.toGraphViz)

//  val deadInstructions = ControlFlow.deadInstruction(cfg, List(modInst))
//  val deadLabels = ControlFlow.deadLabels(cfg)
  val constantInstructions = constants.get
//  println(s"Found ${deadInstructions.size} dead instructions")
//  println(s"Found ${deadLabels.size} dead labels")
  println(s"Found ${constantInstructions.size} constant instructions")
  println(cfg.get.toGraphViz)

  LinearStateOperationCounter.addToListAndReset()
  println(s"${LinearStateOperationCounter.toString} in the last tests")
  println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")
  Profiler.printLastMeasured()
  result
