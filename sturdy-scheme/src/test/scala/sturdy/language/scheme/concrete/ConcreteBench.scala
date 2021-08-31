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

object ConcreteBench:
  def main(args: Array[String]) =
    val benchmarks = List(
      "gabriel/cpstak",
      "gabriel/diviter",
      "gabriel/divrec",
      "gabriel/takl",
      "scala-am/collatz",
      "scala-am/gcipd",
      "scala-am/nqueens",
      "scala-am/rsa"
    )
    val warmups = 10
    val measurementRuns = 5

    benchmarks.foreach { x =>
      Utils().bench(x, warmups, measurementRuns)
    }

