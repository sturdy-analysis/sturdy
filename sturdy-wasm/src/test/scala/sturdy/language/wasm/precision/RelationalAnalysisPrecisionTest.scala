package sturdy.language.wasm.precision

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
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.analyses.RelationalAnalysis.*
import sturdy.language.wasm.analyses.RelationalAnalysis.Type.*
import sturdy.language.wasm.analyses.{CallSites, FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData, WasmFailure}
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.floating.FloatSpecials
import sturdy.values.integer.IntegerDivisionByZero
import sturdy.values.{Abstractly, Topped}
import swam.syntax.Module
import swam.text.*

import java.math.BigInteger
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.reflect.{ClassTag, TypeTest}

final class RelationalAnalysisPrecisionTests extends Suites(
  new RelationalAnalysisPrecisionTest(manager = new Polka(true), relational = true, ssa = true),
  new RelationalAnalysisPrecisionTest(manager = new Polka(true), relational = true),
  new RelationalAnalysisPrecisionTest(manager = new Octagon, relational = true),
  new RelationalAnalysisPrecisionTest(manager = new Box, relational = true),
  new RelationalAnalysisPrecisionTest(manager = new Box, relational = false),
)

final class RelationalAnalysisPrecisionTest(manager: apron.Manager, relational: Boolean, ssa: Boolean = false) extends AnyFlatSpec, Matchers:
  behavior of ((if(relational) "Relational" else "Non-Relational") + (if(ssa) "-SSA" else "") + " analysis with " + manager.getClass.getSimpleName)

  val precisionPath = Paths.get(this.getClass.getResource("/sturdy/language/wasm/precision.wast").toURI);
  val precisionModule = wasm.Parsing.fromText(precisionPath)

  import NumValue.*
  import RelationalAnalysis.RelI32.*
  import RelationalAnalysis.Type.*
  import Value.*

  def i32(expr: ApronExpr[VirtAddr, Type]) = Num(Int32(NumExpr(expr)))
  def i32Iv(lower: Double, upper: Double) = i32(ApronExpr.interval(lower,upper, I32Type))
  def i64(expr: ApronExpr[VirtAddr, Type]) = Num(Int64(expr))

  def iv(lower: Scalar, upper: Scalar, floatSpecials: FloatSpecials, tpe: Type): ApronExpr[VirtAddr, Type] = ApronExpr.constant(FloatInterval(lower, upper, FloatSpecials.Bottom), tpe)
  def doubleIv(lower: Double, upper: Double, floatSpecials: FloatSpecials, tpe: Type): ApronExpr[VirtAddr, Type] = ApronExpr.constant(FloatInterval(lower, upper, floatSpecials), tpe)
  val topi32 = Num(Int32(NumExpr(iv(MpqScalar(Int.MinValue), MpqScalar(Int.MaxValue), FloatSpecials.Bottom, I32Type))))
  val topi64 = Num(Int64(iv(MpqScalar(BigInteger.valueOf(Long.MinValue)), MpqScalar(BigInteger.valueOf(Long.MaxValue)), FloatSpecials.Bottom, I64Type)))

  testFunction("x_minus_x_eq_zero", List())
  testFunction("max_upper_bound", List())
  testFunction("loop_to_100", List())
  testFunction("loop_to_n", List())
  testFunction("input_of_recrusive_id_is_same_as_output", List())
  testFunction("addition", List())
  testFunction("plus_five", List())
  testFunction("abs_if_join_on_stack", List())
  testFunction("abs_if_join_on_local", List())
  testFunction("abs_br_if_join_on_local", List())
  testFunction("fac_positive", List())
  testFunction("fac_acc_positive", List())
  testFunction("fib_positive", List())
  testFunction("even_returns_boolean", List())


  def testFunction(funcName: String, args: List[Value]) =
    it must s"$funcName($args)" in {

      val config = WasmConfig(
        fix = FixpointConfig(StackConfig.StackedStates()),
        relational = relational,
        localSSA = ssa,
        soundOverflowHandling = false // Needed for recursive functions
      )
      val analysis = new RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, config)
      analysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))

      val modInst = analysis.instantiateModule(precisionModule)
      val result = analysis.failure.fallible {
        analysis.invokeExported(modInst, funcName, args).map(analysis.getInterval)
      }

      analysis.failedAssertions shouldBe Map[FixIn, Topped[Boolean]]()

      Profiler.printLastMeasured()
      Profiler.reset()

    }
