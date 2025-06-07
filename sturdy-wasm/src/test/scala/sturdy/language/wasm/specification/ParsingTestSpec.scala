package sturdy.language.wasm.specification

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.wasm.Parsing
import swam.syntax

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

class ParsingTestSpec extends AnyFlatSpec, Matchers:
  behavior of "Parsing"

  val pathSpectest: Path = Paths.get(this.getClass.getResource("/sturdy/language/wasm/spectest.wast").toURI)
  val binaryPath: URI = this.getClass.getResource("/sturdy/language/wasm/binary-spec-test-wasm2").toURI

  val spectest: syntax.Module = Parsing.fromText(pathSpectest)

  val wasmFiles: List[Path] = Files.walk(Paths.get(binaryPath))
    .filter(Files.isRegularFile(_))
    .filter(p => p.toString.endsWith(".wasm"))
    .toScala(List)

  wasmFiles.sorted.foreach { p =>
    it must s"parse binary file ${p.getFileName}" in {
      println(s"Parsing binary file ${p.getFileName}")
      val module = Parsing.fromBinary(p)
      module should not be null
    }
  }