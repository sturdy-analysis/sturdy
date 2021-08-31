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

  behavior of "Scheme concrete Scala-AM benchmarks"

  def runFile(f: String, debug: Boolean = false) = Utils().runFile("scala-am/"++f)

  it should "collatz" in {
    val res = runFile("collatz")
    assertResult(NumVal(IntVal(5)))(res)
  }

  it should "gcipd" in {
    val res = runFile("gcipd")
    assertResult(NumVal(IntVal(36)))(res)
  }

  it should "nqueens" in {
    val res = runFile("nqueens")
    assertResult(NumVal(IntVal(92)))(res)
  }

  // also not working haskell
  it should "primtest" in {
    pending
    val res = runFile("primtest")
    assertResult(BoolVal(false))(res)
  }

  it should "rsa" in {
    val res = runFile("rsa")
    assertResult(BoolVal(true))(res)
  }

