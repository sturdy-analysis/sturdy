package sturdy.language.scheme

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.scheme.ConcreteInterpreter.*
import sturdy.language.scheme.ConcreteInterpreter.Num.*
import sturdy.language.scheme.ConcreteInterpreter.Value.*
import sturdy.language.scheme.Program.*
import sturdy.language.scheme.SchemeExpParser.*
import sturdy.language.scheme.{ConcreteInterpreter, SExpParser}

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class ConcreteMiscellaneousFiles extends AnyFlatSpec, Matchers:

  behavior of "Scheme miscellaneous file tests"

  def runFile(f: String, debug: Boolean = false): Value = Utils().runFile("miscellaneous/"++f, debug)

  it should "test_binops" in {
      val res = runFile("test_binops")
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_random" in {
    pending
    val res = runFile("test_random")
    assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_car" in {
    val res = runFile("test_car", false)
    assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_cdr" in {
    val res = runFile("test_cdr", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test_closure_gc" in {
    val res = runFile("test_closure_gc", false)
    assertResult(NumVal(IntVal(16)))(res)
  }

  it should "test_cons" in {
    val res = runFile("test_cons", false)
    assertResult(BoolVal(true))(res)
  }

  it should "test_eq" in {
    val res = runFile("test_eq", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_equal" in {
    val res = runFile("test_equal", false)
    assertResult(BoolVal(true))(res)
  }

  it should "test_factorial" in {
    val res = runFile("test_factorial", false)
    assertResult(NumVal(IntVal(3628800)))(res)
  }

  it should "test_faulty_list" in {
    val res = runFile("test_faulty_list", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_if" in {
    val res = runFile("test_if", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_inner_define" in {
    val res = runFile("test_inner_define", false)
    assertResult(NumVal(IntVal(10)))(res)
  }

  it should "test_list" in {
    val res = runFile("test_list", false)
    assertResult(QuoteVal(SymbolVal("+")))(res)
  }

  it should "test_lits" in {
    val res = runFile("test_lits", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test_null" in {
    val res = runFile("test_null", false)
    assertResult(BoolVal(true))(res)
  }

  it should "test_opvar_boolbool" in {
    val res = runFile("test_opvar_boolbool", false)
    assertResult(BoolVal(true))(res)
  }

  it should "test_opvar_numbool" in {
    val res = runFile("test_opvar_numbool", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_opvar_numnum" in {
    val res = runFile("test_opvar_numnum", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test_opvars" in {
    val res = runFile("test_opvars", false)
    assertResult(NumVal(IntVal(10)))(res)
  }

  it should "test_rec_defines" in {
    val res = runFile("test_rec_defines", false)
    assertResult(NumVal(IntVal(720)))(res)
  }

  it should "test_rec_empty" in {
    val res = runFile("test_rec_empty", false)
    assertResult(NilVal)(res)
  }

  it should "test_rec_nonempty" in {
    val res = runFile("test_rec_nonempty", false)
    assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_simple_floats" in {
    val res = runFile("test_simple_floats", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_simple_list" in {
    val res = runFile("test_simple_list", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test_subtraction" in {
    val res = runFile("test_subtraction", false)
    assertResult(NumVal(IntVal(-4)))(res)
  }

  it should "test_symbols" in {
    val res = runFile("test_symbols", false)
    assertResult(QuoteVal(SymbolVal("sym3")))(res)
  }

  it should "test_unops" in {
    val res = runFile("test_unops", false)
    assertResult(BoolVal(false))(res)
  }
