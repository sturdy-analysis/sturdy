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

import java.math.BigInteger
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

  import Value.*
  import NumValue.*
  import RelationalAnalysis.Type.*
  import RelationalAnalysis.RelI32.*

  def i32(expr: ApronExpr[VirtAddr, Type]) = Num(Int32(NumExpr(expr)))
  def i64(expr: ApronExpr[VirtAddr, Type]) = Num(Int64(expr))

  def iv(lower: Scalar, upper: Scalar, floatSpecials: FloatSpecials, tpe: Type): ApronExpr[VirtAddr, Type] = ApronExpr.constant(FloatInterval(lower, upper, FloatSpecials.Bottom), tpe)
  def doubleIv(lower: Double, upper: Double, floatSpecials: FloatSpecials, tpe: Type): ApronExpr[VirtAddr, Type] = ApronExpr.constant(FloatInterval(lower, upper, floatSpecials), tpe)
  val topi32 = Num(Int32(NumExpr(iv(MpqScalar(Int.MinValue), MpqScalar(Int.MaxValue), FloatSpecials.Bottom, I32Type))))
  val topi64 = Num(Int64(iv(MpqScalar(BigInteger.valueOf(Long.MinValue)), MpqScalar(BigInteger.valueOf(Long.MaxValue)), FloatSpecials.Bottom, I64Type)))
  val topf32 = Num(Float32(doubleIv(Float.MinValue, Float.MaxValue, FloatSpecials.Top, F32Type)))
  val topf32NoSpecials = Num(Float32(doubleIv(Float.MinValue, Float.MaxValue, FloatSpecials.Bottom, F32Type)))
  val topf64 = Num(Float64(doubleIv(Double.MinValue, Double.MaxValue, FloatSpecials.Top, F64Type)))
  val topf64NoSpecials = Num(Float64(doubleIv(Double.MinValue, Double.MaxValue, FloatSpecials.Bottom, F64Type)))

  testFunction(simple, "const", List(topi32), List(topi32))
  testFunction(simple, "first", List(i32(ApronExpr.lit(1, I32Type)), topi32), List(i32(ApronExpr.lit(1, I32Type))))
  testFunction(simple, "first", List(topi32, i32(ApronExpr.lit(2, I32Type))), List(topi32))
  testFunction(simple, "second", List(i32(ApronExpr.lit(1, I32Type)), topi32), List(topi32))
  testFunction(simple, "second", List(topi32, i32(ApronExpr.lit(2, I32Type))), List(i32(ApronExpr.lit(2, I32Type))))
  testFunction(simple, "test-mem0", List(topi32), List(topi32))
  testFunction(simple, "test-mem", List(topi32), List(topi32))
  testFunction(simple, "test-size", List.empty, List(i32(ApronExpr.lit(1, I32Type))))
  testFunction(simple, "test-memgrow", List.empty, List(i32(doubleIv(-1, 1, FloatSpecials.Integer, I32Type)), i32(doubleIv(1, 2, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "nesting", List(topf32NoSpecials, Num(Float32(ApronExpr.floatConstant(DoubleScalar(2), FloatSpecials.Top, F32Type)))), List(topf32))
  testFunction(simple, "nesting", List(Num(Float32(ApronExpr.floatConstant(DoubleScalar(1), FloatSpecials.Top, F32Type))), topf32NoSpecials), List(topf32))
  testFunction(simple, "test-br3", List(topi32), List(i32(doubleIv(42, 43, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "test-br-and-return", List(topi32), List(i32(doubleIv(42, 43, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "test-br-and-return2", List(topi32), List(i32(doubleIv(42, 43, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "test-br-and-return3", List(topi32), List(i32(doubleIv(42, 43, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "test-br-and-return4", List(topi32), List(i32(doubleIv(42, 42, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "test-unreachable5", List(topi32), List(i32(doubleIv(42, 43, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "test-global", List(topi32), List(i32(doubleIv(1, 2, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "test-call-indirect-parametric", List(topi32), List(i32(doubleIv(0, 0, FloatSpecials.Integer, I32Type))))
  testFailingFunction(simple, "division", List(i32(ApronExpr.lit(1, I32Type)), topi32), IntegerDivisionByZero)
  testFunction(simple, "effects", List(topi32), List(i32(doubleIv(-35, 21, FloatSpecials.Integer, I32Type))))

  testFunction(simple, "operand_stack_is_immutable", List(), List(i32(doubleIv(0, 0, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "plus_five", List(i32(ApronExpr.lit(0, I32Type))), List(i32(doubleIv(1, 5, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "loop_to_100", List(i32(ApronExpr.lit(0, I32Type))), List(i32(doubleIv(100, Integer.MAX_VALUE, FloatSpecials.Integer, I32Type))))
  testFunction(simple, "abs", List(i32(doubleIv(-10, 10, FloatSpecials.Integer, I32Type))), List(i32(doubleIv(0, 10, FloatSpecials.Integer, I32Type))))

  testFunction(fact, "fac-rec", List(i64(ApronExpr.lit(1, I64Type))), List(i64(ApronExpr.lit(1, I64Type))))
  (2 to 8).foreach { arg =>
    testFunction(fact, "fac-rec", List(i64(ApronExpr.lit(arg, I64Type))), List(topi64))
  }
  testFunction(fact, "fac-rec", List(i64(ApronExpr.lit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-iter", List(i64(ApronExpr.lit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-rec-named", List(i64(ApronExpr.lit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-iter-named", List(i64(ApronExpr.lit(25, I64Type))), List(topi64))
  testFunction(fact, "fac-opt", List(i64(ApronExpr.lit(25, I64Type))), List(topi64))
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
  val interp = new RelationalAnalysis.Instance(apronManager, FrameData.empty, Iterable.empty, WasmConfig(FixpointConfig(stackConfig)))
  val constants = interp.instructionsIntervals
  interp.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
  val cfg = interp.addControlObserver(new ControlEventGraphBuilder)

  val modInst = interp.instantiateModule(module)
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
