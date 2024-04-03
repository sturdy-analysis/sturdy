package sturdy.language.pcf

import cats.parse.{Numbers, Parser0 as P0, Parser as P}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.pcf.Parser

import java.nio.file.Path
import java.nio.file.{Paths, Files}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Try, Success, Failure}




class ParserTest extends AnyFlatSpec, Matchers:
  behavior of "PCF parser"

  val uri = classOf[ParserTest].getResource("/examples").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".pcf")).sorted.foreach { p =>
    it must s"execute ${p.getFileName}" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val tree = Try(Parser.parse(sourceCode))
      assert(tree.isSuccess, tree.toEither.swap.map(_.getMessage).getOrElse(""))
      println(tree.get)
    }
  }

def parse(s: String): Either[P.Error, Program] =
  Parser.program.parseAll(s)
