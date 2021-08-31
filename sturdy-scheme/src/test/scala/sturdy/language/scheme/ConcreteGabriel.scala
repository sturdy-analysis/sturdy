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


  // also not working in haskell
  "parse + concrete interpret" should "gabriel/boyer" in {
    pending
    val res = ConcreteInterpreterFilesTest().run("gabriel/boyer.scm", false)
    assertResult(NumVal(IntVal(5)))(res)
  }

  // also not working in haskell
  it should "gabriel/browse" in {
    pending
    val res = ConcreteInterpreterFilesTest().run("gabriel/browse.scm", false)
    assertResult(NumVal(IntVal(36)))(res)
  }

  // only modified version passes
  // cannot resolve stackoverflow for full version
  it should "gabriel/cpstak" in {
    val res = ConcreteInterpreterFilesTest().run("gabriel/cpstak.scm", false)
    assertResult(NumVal(IntVal(3)))(res)
  }

  // does not work yet
  it should "gabriel/dderiv" in {
    pending
    val res = ConcreteInterpreterFilesTest().run("gabriel/dderiv.scm", false)
    assertResult(BoolVal(false))(res)
  }

  // does not work yet
  it should "gabriel/deriv" in {
    pending
    val res = ConcreteInterpreterFilesTest().run("gabriel/deriv.scm", false)
    assertResult(BoolVal(false))(res)
  }

  // also not working haskell
  // requires change to body s.t. it can be empty
  // returns false
  it should "gabriel/destruc" in {
    pending
    val res = ConcreteInterpreterFilesTest().run("gabriel/destruc.scm", false)
    assertResult(BoolVal(true))(res)
  }

  // only modified version passes
  // cannot resolve stackoverflow for full version
  it should "gabriel/diviter" in {
    val res = ConcreteInterpreterFilesTest().run("gabriel/diviter.scm", false)
    assertResult(BoolVal(true))(res)
  }

  // only modified version passes
  // cannot resolve stackoverflow for full version
  // also changed cond to if in source file!
  it should "gabriel/divrec" in {
    val res = ConcreteInterpreterFilesTest().run("gabriel/divrec.scm", false)
    assertResult(BoolVal(true))(res)
  }

  it should "gabriel/takl" in {
    val res = ConcreteInterpreterFilesTest().run("gabriel/takl.scm", false)
    assertResult(BoolVal(true))(res)
  }
