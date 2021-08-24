package sturdy.language.scheme

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.scheme.ConcreteInterpreter.*
import sturdy.language.scheme.ConcreteInterpreter.Num.*
import sturdy.language.scheme.ConcreteInterpreter.Value.*
import sturdy.language.scheme.SchemeExpParser.*
import sturdy.language.scheme.Program.*

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class ConcreteInterpreterFilesTest extends AnyFlatSpec, Matchers:

  def run(f: String, debug: Boolean): Value =
    val uri = classOf[ConcreteInterpreterFilesTest].getResource("/sturdy/language/scheme/"++f).toURI();
    val file = Source.fromURI(uri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    if debug then println(s"input: \n$sourceCode")
    val tree = parse(sourceCode)
    if debug then println(s"parse: \n$tree")
    val interp = ConcreteInterpreter(Map(), Map())
    interp.execute(Program(parse(sourceCode).map(Form.Expression.apply)))


  "parse + concrete interpret" should "test_binops" in {
    for _ <- 0 until 10 do
      val res = run("test_binops.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_car" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_car.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }
  it should "test_cdr" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_cdr.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_closure_gc" in {
    for _ <- 0 until 10 do
      val res = run("test_closure_gc.scm", false)
      assertResult(NumVal(IntVal(16)))(res)
  }

  it should "test_cons" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_cons.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_eq" in {
    for _ <- 0 until 10 do
      val res = run("test_eq.scm", false)
      assertResult(BoolVal(false))(res)
  }

  it should "test_equal" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_equal.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_factorial" in {
    for _ <- 0 until 10 do
      val res = run("test_factorial.scm", false)
      assertResult(NumVal(IntVal(3628800)))(res)
  }
  it should "test_faulty_list" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_faulty_list.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }
  it should "test_if" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_if.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }
  it should "test_inner_define" in {
    for _ <- 0 until 10 do
      val res = run("test_inner_define.scm", false)
      assertResult(NumVal(IntVal(10)))(res)
  }
  it should "test_list" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_list.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }
  it should "test_lits" in {
    for _ <- 0 until 10 do
      val res = run("test_lits.scm", false)
      assertResult(NumVal(IntVal(3)))(res)
  }
  it should "test_null" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_null.scm", false)
      assertResult(BoolVal(true))(res)
  }
  it should "test_opvar_boolbool" in {
    for _ <- 0 until 10 do
      val res = run("test_opvar_boolbool.scm", true)
      assertResult(BoolVal(true))(res)
  }
  it should "test_opvar_numbool" in {
    for _ <- 0 until 10 do
      val res = run("test_opvar_numbool.scm", false)
      assertResult(BoolVal(false))(res)
  }
  it should "test_opvar_numnum" in {
    for _ <- 0 until 10 do
      val res = run("test_opvar_numnum.scm", false)
      assertResult(NumVal(IntVal(3)))(res)
  }
  it should "test_opvars" in {
    for _ <- 0 until 10 do
      val res = run("test_opvars.scm", false)
      assertResult(NumVal(IntVal(10)))(res)
  }
  it should "test_random" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_random.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }
  it should "test_rec_defines" in {
    for _ <- 0 until 10 do
      val res = run("test_rec_defines.scm", false)
      assertResult(NumVal(IntVal(720)))(res)
  }
  it should "test_rec_empty" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_rec_empty.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }
  it should "test_rec_nonempty" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_rec_nonempty.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }
  it should "test_simple_floats" in {
    for _ <- 0 until 10 do
      val res = run("test_simple_floats.scm", false)
      assertResult(BoolVal(false))(res)
  }
  it should "test_simple_list" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_simple_list.scm", false)
      assertResult(BoolVal(false))(res)
  }
  it should "test_subtraction" in {
    for _ <- 0 until 10 do
      val res = run("test_subtraction.scm", false)
      assertResult(NumVal(IntVal(-4)))(res)
  }
  it should "test_symbols" in {
    pending
    for _ <- 0 until 10 do
      val res = run("test_symbols.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }
  it should "test_unops" in {
    for _ <- 0 until 10 do
      val res = run("test_unops.scm", false)
      assertResult(BoolVal(false))(res)
  }
