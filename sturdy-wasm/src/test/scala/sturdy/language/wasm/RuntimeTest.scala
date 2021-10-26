package sturdy.language.wasm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.{AFallible, CFallible}
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.generic.ProcExit
import sturdy.values.Topped

import java.nio.file.{Path, Paths}

class RuntimeTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm runtime"

  val uri = classOf[RuntimeTest].getResource("/sturdy/language/wasm/runtime.wast").toURI();
  val path = Paths.get(uri)

  testExitCodeConcrete(path, "_start_orig", List.empty, 42)
  testExitCodeConcrete(path, "_start2", List(ConcreteInterpreter.Value.Int32(1)), 42)
  testExitCodeConcrete(path, "_start2", List(ConcreteInterpreter.Value.Int32(0)), 0)
  testExitCodeConcrete(path, "_start3", List(ConcreteInterpreter.Value.Int32(1)), 42)
  testExitCodeConcrete(path, "_start3", List(ConcreteInterpreter.Value.Int32(0)), 0)


  testExitCodeConstant(path, "_start_orig", List.empty, Topped.Actual(42))
  testExitCodeConstant(path, "_start2", List(ConstantAnalysis.Value.Int32(Topped.Top)), Topped.Top)
  testExitCodeConstant(path, "_start3", List(ConstantAnalysis.Value.Int32(Topped.Top)), Topped.Actual(42))
  testExitCodeConstant(path, "_start3", List(ConstantAnalysis.Value.Int32(Topped.Top)), Topped.Actual(0))
  

  def testExitCodeConcrete(path: Path, funcName: String, args: List[ConcreteInterpreter.Value], exitCode: Int) =
    it must s"execute $funcName with concrete interpreter returning exit code $exitCode" in {
      val res = runWasmFunction(path, funcName, args)
      //println(res)
      assert(res.isFailing)
      assertResult(ProcExit(ConcreteInterpreter.Value.Int32(exitCode)))(res.asInstanceOf[CFallible.Failing[_]].kind)
    }

  def testExitCodeConstant(path: Path, funcName: String, args: List[ConstantAnalysis.Value], exitCode: Topped[Int]) =
    it must s"execute $funcName with constant analysis returning exit code $exitCode" in {
      val res = analyses.constant.runConstantAnalysis(path, funcName, args)
      //println(res)
      res match
        case AFallible.Unfailing(vals) => assert(false, s"Expected $ProcExit but execution succeeded: $vals")
        case AFallible.MaybeFailing(_, fails) => assert(fails.set.exists(_._1 == ProcExit(ConstantAnalysis.Value.Int32(exitCode))))
        case AFallible.Failing(fails) => assert(fails.set.exists(_._1 == ProcExit(ConstantAnalysis.Value.Int32(exitCode))))
    }


