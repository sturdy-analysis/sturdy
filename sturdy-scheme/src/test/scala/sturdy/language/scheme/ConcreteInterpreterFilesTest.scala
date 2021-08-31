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
    var uri = classOf[ConcreteInterpreterFilesTest].getResource("/sturdy/language/scheme/"++f).toURI();
    var file = Source.fromURI(uri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    uri = classOf[ConcreteInterpreterFilesTest].getResource("/sturdy/language/scheme/macros_modified.scm").toURI();
    file = Source.fromURI(uri)
    val macrosCode = file.getLines().mkString("\n")
    file.close()

    if debug then
      println(s"input: \n$sourceCode")
      println(s"macros: \n$macrosCode")
    if debug then
      val sexp = SExpParser.parse(sourceCode)
      println(s"sparse: \n$sexp")
      val sexpmacro = SExpParser.parse(macrosCode)
      println(s"smacroparse: \n$sexpmacro")
    val tree = parse(sourceCode)
    val treemacro = parse(macrosCode+sourceCode)
    if debug then
      println(s"parse: \n$tree")
      println(s"parsemacro: \n$treemacro")
    val interp = ConcreteInterpreter(Map(), Map())

    interp.execute(treemacro)




  "parse + concrete interpret" should "test_binops" in {
      val res = run("test_binops.scm", false)
      assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_random" in {
    pending
    val res = run("test_random.scm", true)
    assertResult(NumVal(IntVal(1)))(res)
  }

  it should "parse macros" in {
    val res = run("macros_modified.scm", false)
    assertResult(VoidVal)(res)
  }

  it should "test_car" in {
    val res = run("test_car.scm", false)
    assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_cdr" in {
    val res = run("test_cdr.scm", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test_closure_gc" in {
    val res = run("test_closure_gc.scm", false)
    assertResult(NumVal(IntVal(16)))(res)
  }

  it should "test_cons" in {
    val res = run("test_cons.scm", false)
    assertResult(BoolVal(true))(res)
  }

  it should "test_eq" in {
    val res = run("test_eq.scm", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_equal" in {
    val res = run("test_equal.scm", false)
    assertResult(BoolVal(true))(res)
  }

  it should "test_factorial" in {
    val res = run("test_factorial.scm", false)
    assertResult(NumVal(IntVal(3628800)))(res)
  }

  it should "test_faulty_list" in {
    val res = run("test_faulty_list.scm", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_if" in {
    val res = run("test_if.scm", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_inner_define" in {
    val res = run("test_inner_define.scm", false)
    assertResult(NumVal(IntVal(10)))(res)
  }

  it should "test_list" in {
    val res = run("test_list.scm", false)
    assertResult(QuoteVal(SymbolVal("+")))(res)
  }

  it should "test_lits" in {
    val res = run("test_lits.scm", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test_null" in {
    val res = run("test_null.scm", false)
    assertResult(BoolVal(true))(res)
  }

  it should "test_opvar_boolbool" in {
    val res = run("test_opvar_boolbool.scm", false)
    assertResult(BoolVal(true))(res)
  }

  it should "test_opvar_numbool" in {
    val res = run("test_opvar_numbool.scm", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_opvar_numnum" in {
    val res = run("test_opvar_numnum.scm", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test_opvars" in {
    val res = run("test_opvars.scm", false)
    assertResult(NumVal(IntVal(10)))(res)
  }

  it should "test_rec_defines" in {
    val res = run("test_rec_defines.scm", false)
    assertResult(NumVal(IntVal(720)))(res)
  }

  it should "test_rec_empty" in {
    val res = run("test_rec_empty.scm", false)
    assertResult(NilVal)(res)
  }

  it should "test_rec_nonempty" in {
    val res = run("test_rec_nonempty.scm", false)
    assertResult(NumVal(IntVal(1)))(res)
  }

  it should "test_simple_floats" in {
    val res = run("test_simple_floats.scm", false)
    assertResult(BoolVal(false))(res)
  }

  it should "test_simple_list" in {
    val res = run("test_simple_list.scm", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  it should "test_subtraction" in {
    val res = run("test_subtraction.scm", false)
    assertResult(NumVal(IntVal(-4)))(res)
  }

  it should "test_symbols" in {
    val res = run("test_symbols.scm", false)
    assertResult(QuoteVal(SymbolVal("sym3")))(res)
  }

  it should "test_unops" in {
    val res = run("test_unops.scm", false)
    assertResult(BoolVal(false))(res)
  }
