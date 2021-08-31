package sturdy.language.scheme

import cats.parse.{Numbers, Parser as P, Parser0 as P0}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFailureException
import sturdy.language.scheme.ConcreteInterpreter.*
import sturdy.language.scheme.ConcreteInterpreter.Num.*
import sturdy.language.scheme.ConcreteInterpreter.Value.*
import sturdy.language.scheme.Program.*
import sturdy.language.scheme.Form.*
import sturdy.language.scheme.Define.*
import sturdy.language.scheme.Exp.*
import sturdy.language.scheme.SchemeExpParser.*
import sturdy.language.scheme.{ConcreteInterpreter, SExpParser}

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*

class Utils extends AnyFlatSpec, Matchers:

  def runAny(ds: List[Any]): Value =
    val interp = ConcreteInterpreter(Map(), Map())
    val forms = ds.map {
      case d: Define => Form.Definition(d)
      case e: Exp => Form.Expression(e)
    }
    interp.execute(Program(forms))

  def runProgram(p: Program): Value =
    val interp = ConcreteInterpreter(Map(), Map())
    interp.execute(p)

  def runFile(f: String, debug: Boolean = false): Value =
    var uri = classOf[Utils].getResource("/sturdy/language/scheme/"++f++".scm").toURI();
    var file = Source.fromURI(uri)
    val sourceCode = file.getLines().mkString("\n")
    if debug then println(s"input: \n$sourceCode")
    file.close()
    uri = classOf[Utils].getResource("/sturdy/language/scheme/macros_modified.scm").toURI();
    file = Source.fromURI(uri)
    val macrosCode = file.getLines().mkString("\n")
    if debug then println(s"macros: \n$macrosCode")
    file.close()
    if debug then
      val sexp = SExpParser.parse(sourceCode)
      println(s"sparse: \n$sexp")
      val sexpmacro = SExpParser.parse(macrosCode)
      println(s"smacroparse: \n$sexpmacro")
    if debug then
      val tree = parse(sourceCode)
      println(s"parse: \n$tree")
    val treemacro = parse(macrosCode+sourceCode)
    if debug then println(s"parsemacro: \n$treemacro")

    val interp = ConcreteInterpreter(Map(), Map())
    interp.execute(treemacro)


  def bench(f: String, warmupRuns: Int, measurementRuns: Int): Unit =
    var uri = classOf[Utils].getResource("/sturdy/language/scheme/"++f++".scm").toURI();
    var file = Source.fromURI(uri)
    val sourceCode = file.getLines().mkString("\n")
    file.close()
    uri = classOf[Utils].getResource("/sturdy/language/scheme/macros_modified.scm").toURI();
    file = Source.fromURI(uri)
    val macrosCode = file.getLines().mkString("\n")
    file.close()
    val treemacro = parse(macrosCode+sourceCode)

    var times: List[Double] = Nil
    for _ <- 0 until warmupRuns + measurementRuns do
      val t1 = System.nanoTime
      ConcreteInterpreter(Map(), Map()).execute(treemacro)
      val t2 = System.nanoTime
      val time = (t2 - t1) / 1e6d // measured in ms
      times = time::times
    times = times.take(measurementRuns)
    val mean = times.sum / measurementRuns
    val std = scala.math.sqrt(times.map(x => (x - mean) * (x - mean)).sum / measurementRuns)
    val max = times.max
    val min = times.min
    val div = "========================================================================================================="
    println(div)
    println(s"benchmark file: $f")
    println(s"warmup runs: $warmupRuns")
    println(s"measurement runs: $measurementRuns")
    println(s"avg: $mean ms" )
    println(s"std: $std ms")
    println(s"max: $max ms")
    println(s"min: $min ms")



