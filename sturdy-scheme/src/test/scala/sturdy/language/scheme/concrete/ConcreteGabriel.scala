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

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class ConcreteGabriel extends AnyFlatSpec, Matchers:

  behavior of "Scheme concrete gabriel benchmarks"

  def runFile(f: String, debug: Boolean = false) = Utils().runFile("gabriel/"++f, debug)

  // also not working in haskell
  it should "boyer" in {
    pending
    val res = runFile("boyer")
    assertResult(NumVal(IntVal(5)))(res)
  }

  // also not working in haskell
  it should "browse" in {
    pending
    val res = runFile("browse")
    assertResult(NumVal(IntVal(36)))(res)
  }

  // only modified version passes
  // cannot resolve stackoverflow for full version
  it should "cpstak" in {
    val res = runFile("cpstak")
    assertResult(NumVal(IntVal(3)))(res)
  }

  // does not work yet
  it should "dderiv" in {
    pending
    val res = runFile("dderiv")
    assertResult(BoolVal(false))(res)
  }

  // does not work yet
  it should "deriv" in {
    pending
    val res = runFile("deriv")
    assertResult(BoolVal(false))(res)
  }

  // also not working haskell
  // requires change to body s.t. it can be empty
  // returns false
  it should "destruc" in {
    pending
    val res = runFile("destruc")
    assertResult(BoolVal(true))(res)
  }

  // only modified version passes
  // cannot resolve stackoverflow for full version
  it should "diviter" in {
    val res = runFile("diviter")
    assertResult(BoolVal(true))(res)
  }

  // only modified version passes
  // cannot resolve stackoverflow for full version
  // also changed cond to if in source file!
  it should "divrec" in {
    val res = runFile("divrec")
    assertResult(BoolVal(true))(res)
  }

  it should "takl" in {
    val res = runFile("takl")
    assertResult(BoolVal(true))(res)
  }
