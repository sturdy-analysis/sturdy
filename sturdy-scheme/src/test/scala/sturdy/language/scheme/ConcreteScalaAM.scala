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

class ConcreteScalaAM extends AnyFlatSpec, Matchers:


  "parse + concrete interpret" should "scala-am/collatz" in {
    val res = ConcreteInterpreterFilesTest().run("scala-am/collatz.scm", false)
    assertResult(NumVal(IntVal(5)))(res)
  }

  it should "scala-am/gcipd" in {
    val res = ConcreteInterpreterFilesTest().run("scala-am/gcipd.scm", false)
    assertResult(NumVal(IntVal(36)))(res)
  }

  it should "scala-am/nqueens" in {
    val res = ConcreteInterpreterFilesTest().run("scala-am/nqueens.scm", false)
    assertResult(NumVal(IntVal(92)))(res)
  }

  // also not working haskell
  it should "scala-am/primtest" in {
    pending
    val res = ConcreteInterpreterFilesTest().run("scala-am/primtest.scm", false)
    assertResult(BoolVal(false))(res)
  }

  it should "scala-am/rsa" in {
    val res = ConcreteInterpreterFilesTest().run("scala-am/rsa.scm", false)
    assertResult(BoolVal(true))(res)
  }

