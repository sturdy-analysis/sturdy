package sturdy.language.tutorial

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import sturdy.language.tutorial.Stm.*
import sturdy.language.tutorial.Exp.*
import .parse

import java.net.URI

class ParserTest extends AnyFlatSpec, Matchers:
  behavior of "While parser"

  val uri: URI = classOf[ParserTest].getResource("/sturdy/language/tutorial").toURI

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".while")).sorted.foreach { p =>
    it must s"parse ${p.getFileName}" in {
      val file = Source.fromURI(p.toUri)
      val sourceCode = file.getLines().mkString("\n")
      file.close()
      val tree = parse(sourceCode)
    }
  }