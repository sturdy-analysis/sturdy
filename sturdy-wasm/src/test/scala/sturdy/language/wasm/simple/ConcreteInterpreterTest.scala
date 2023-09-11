package sturdy.language.wasm.simple

import cats.effect.Blocker
import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.AFallible
import sturdy.effect.failure.FailureKind
import sturdy.language.wasm.generic.{FrameData, FunctionInstance, WasmFailure}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.ConcreteInterpreter.Value
import sturdy.effect.failure.CFallible
import sturdy.language.wasm.Parsing

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.StreamConverters.*
import swam.syntax.Module
import swam.text.*


class ConcreteInterpreterTest extends AnyFlatSpec, Matchers:
  behavior of "Wasm concrete interpreter"

  //val uriSimple = this.getClass.getResource("/sturdy/language/wasm/simple.wast").toURI;
  //val uriFact = this.getClass.getResource("/sturdy/language/wasm/fact.wast").toURI;
  //val uriSimpleTable = this.getClass.getResource("/sturdy/language/wasm/simple_table.wast").toURI;
  //val uriSimpleTable2 = this.getClass.getResource("/sturdy/language/wasm/simple_table2.wast").toURI;
  val uriSimpleTable3 = this.getClass.getResource("/sturdy/language/wasm/simple_table3.wast").toURI;
  val uriSimpleTable4 = this.getClass.getResource("/sturdy/language/wasm/simple_table4.wast").toURI;
  val uriSimpleTable5 = this.getClass.getResource("/sturdy/language/wasm/simple_table5.wast").toURI;
  val uriRefNull = this.getClass.getResource("/sturdy/language/wasm/my_ref_null.wast").toURI;
  val uriRefFunc = this.getClass.getResource("/sturdy/language/wasm/my_ref_func.wast").toURI;
  //val simple = Paths.get(uriSimple)
  //val fact = Paths.get(uriFact)
  //val st = Paths.get(uriSimpleTable)
  //val st2 = Paths.get(uriSimpleTable2)
  val st3 = Paths.get(uriSimpleTable3)
  val st4 = Paths.get(uriSimpleTable4)
  val st5 = Paths.get(uriSimpleTable5)
  val rn = Paths.get(uriRefNull)
  val rf = Paths.get(uriRefFunc)

 /* testFunction(st3, "test_table_size_t1", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(3))))
  testFunction(st3, "test_table_size_t2", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(2))))
  testFunction(st3, "test_sum", List(Value.Num(ConcreteInterpreter.NumValue.Int32(3)), Value.Num(ConcreteInterpreter.NumValue.Int32(4))), List(Value.Num(ConcreteInterpreter.NumValue.Int32(7))))
  testFunction(st3, "test_ref_null", List.empty, List(Value.Ref(ConcreteInterpreter.RefValue.Null)))
  //testFunction(st3, "test_ref_null", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(1))))
  testFunction(st3, "test_table_get", List(Value.Num(ConcreteInterpreter.NumValue.Int32(2))), List(Value.Num(ConcreteInterpreter.NumValue.Int32(0))))
 // testFunction(st3, "test_table_get", List.empty, List(Value.Ref(ConcreteInterpreter.RefValue.Func(0))))
  //testFunction(st3, "test_call_const", List(Value.Num(ConcreteInterpreter.NumValue.Int32(0))), List(Value.Num(ConcreteInterpreter.NumValue.Int32(8))))
  //testFunction(st3, "test_table_set", List(Value.Num(ConcreteInterpreter.RefValue.Func(0))), List(Value.Num(ConcreteInterpreter.RefValue.Func(0))))
  testFunction(st3, "test_call_indirect", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(3))))
*/


  testFunction(st3, "test_table_get_t1", List(Value.Num(ConcreteInterpreter.NumValue.Int32(0))), List(Value.FuncRef(1)))
  testFunction(st3, "test_table_get_t1", List(Value.Num(ConcreteInterpreter.NumValue.Int32(1))), List(Value.FuncRef(0)))
  testFunction(st3, "test_table_get_t2", List(Value.Num(ConcreteInterpreter.NumValue.Int32(1))), List(Value.FuncRef(2)))
  testFunction(st3, "test_table_get_t3", List(Value.Num(ConcreteInterpreter.NumValue.Int32(6))), List(Value.FuncRef(0)))
  testFunction(st3, "test_table_set_t1", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(4))))
  testFunction(st3, "test_set_call", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(3))))

/*

  testFunction(st4, "init", List.empty, List.empty)
  testFunction(st4, "test_table_size_t3", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(1))))
  testFunction(st4, "size_after_table_set", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(3))))
  testFunction(st4, "test_ref_func", List.empty, List(Value.FuncRef(0)))
  testFunction(st4, "test_ref_null_func", List.empty, List(Value.FuncNull))
  testFunction(st4, "test_ref_null_extern", List.empty, List(Value.ExternNull))
  testFunction(st4, "test_table_get", List.empty, List(Value.FuncRef(0)))
  testFunction(st4, "test_set_null", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(2))))


  testFunction(rf, "test_table_set", List(Value.Num(ConcreteInterpreter.NumValue.Int32(1))), List.empty)
  testFunction(rf, "call_after_set", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(4))))
  testFunction(rf, "test_table_get", List.empty, List(Value.FuncRef(1)))
  testFunction(rf, "test_table_size", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(5))))
  testFunction(st5, "test_table_grow", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(10))))
  testFunction(st5, "test_table_fill", List.empty, List(Value.Num(ConcreteInterpreter.NumValue.Int32(10))))
*/
 // testFunction(rn, "externref", List.empty, List(Value.FuncRef(ConcreteInterpreter.Value.ExternNull)))
 // testFunction(rn, "funcref", List.empty, List(Value.FuncRef(ConcreteInterpreter.RefValue.FuncNull)))

  //testFunction(rf, "f", List(Value.Num(ConcreteInterpreter.NumValue.Int32(1))), List(Value.Num(ConcreteInterpreter.NumValue.Int32(1))))
  //testFunction(rf, "g", List.empty, List(Value.Ref(ConcreteInterpreter.RefValue.Func(0))))

  def testFunction(path: Path, funcName: String, args: List[Value], expectedResult: List[Value]) =
    it must s"execute $funcName withs args $args with result $expectedResult" in {
      val res = runWasmFunction(path, funcName, args)
      assertResult(CFallible.Unfailing(expectedResult))(res)
    }

  def testFailingFunction(path: Path, funcName: String, args: List[Value], failureKind: FailureKind) =
    it must s"execute $funcName with args $args throwing exception $failureKind" in {
      val res = runWasmFunction(path, funcName, args)
      assert(res.isFailing)
      val kind = res.asInstanceOf[CFallible.Failing[_]].kind
      assert(kind == failureKind)
    }


def runWasmFunction(path: Path, funName: String, args: List[Value]): CFallible[Iterable[Value]] =
  val module = Parsing.fromText(path)
  val interp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
  val modInst = interp.initializeModule(module)
  interp.failure.fallible(
    interp.invokeExported(modInst, funName, args)
  )
