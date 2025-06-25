package sturdy.language.wasm.specification

import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.CFallible
import sturdy.language.wasm.Parsing
import swam.syntax
import swam.syntax.Module

import java.nio.file.{Files, Path, Paths}
import java.util.regex.Pattern
import scala.collection.mutable
import scala.jdk.StreamConverters.*

class ParsingTestSpec extends AnyFlatSpec, Matchers:
  def parseExports(jsonFile: Path): Map[String, String] = {
    implicit val formats = DefaultFormats
    val jsonStr = Files.readString(jsonFile)
    val json = parse(jsonStr)

    val commands = (json \ "commands").extract[List[Map[String, Any]]]
    val result = scala.collection.mutable.Map.empty[String, String]

    commands.sliding(2).foreach {
      case List(moduleCmd, registerCmd)
        if moduleCmd.get("type").contains("module") &&
          registerCmd.get("type").contains("register") =>
        for {
          filename <- moduleCmd.get("filename").collect { case s: String => s }
          asName <- registerCmd.get("as").collect { case s: String => s }
        } result += (filename -> asName)

      case _ => ()
    }

    result.toMap
  }

  def parseActions(jsonFile: Path): Map[String, String] =
    val jsonStr = Files.readString(jsonFile)
    val entryPattern = Pattern.compile("""\{[^{}]*?"type"\s*:\s*"(\w+)"[^{}]*?"filename"\s*:\s*"([^"]+)"""")
    val matcher = entryPattern.matcher(jsonStr)

    val map = mutable.Map.empty[String, String]
    while matcher.find() do
      val cmdType = matcher.group(1)
      val filename = matcher.group(2)
      map += filename -> cmdType

    map.toMap

  behavior of "Parsing"

  val pathSpectest: Path = Paths.get(this.getClass.getResource("/sturdy/language/wasm/spectest.wast").toURI)
  val binaryPath: Path = Paths.get(this.getClass.getResource("/sturdy/language/wasm/binary-spec-test-wasm2").toURI)

  val spectest: Module = Parsing.fromText(pathSpectest)

  val onlyRunDirs: Seq[String] = Vector(
  )

  val onlyRunModules: Seq[String] = Vector(
  )

  val testDirs: List[Path] =
    Files.list(binaryPath)
      .filter(Files.isDirectory(_))
      .toScala(List)
      .filter(dir => onlyRunDirs.isEmpty || onlyRunDirs.exists(dir.getFileName.toString.contains(_)))

  testDirs.sorted.foreach { dir =>
    val jsonFile = dir.resolve(dir.getFileName.toString + ".json")
    val commands = parseActions(jsonFile)
    val exports = parseExports(jsonFile)
    val testInterp = ConcreteTestSpecInterpreter(Some(spectest))

    val wasmFiles = Files.list(dir)
      .filter(p => p.toString.endsWith(".wasm"))
      .toScala(List)

    wasmFiles.filter(p => if (onlyRunModules.isEmpty) true else onlyRunModules.exists(p.getFileName.toString.contains(_))).sorted.foreach { p =>
      val commandType = commands.getOrElse(p.getFileName.toString, "unknown")
      commandType match {
        case "assert_invalid" | "assert_uninstantiable" | "assert_malformed" | "assert_unlinkable" =>
          it must s"parse invalid binary file ${p.getFileName} in directory ${dir.getFileName}" in {
            println(s"Parsing invalid binary file ${p.getFileName} in directory ${dir.getFileName}")
            assertThrows[Throwable] {
              val module = Parsing.fromBinary(p)

              testInterp.interp.failure.fallible {
                testInterp.interp.initializeModule(module, testInterp.imports)
              } match {
                case CFallible.Failing(_, _) =>
                  throw new Exception(s"Instantiation failure")
                case sturdy.effect.failure.CFallible.Unfailing(_) => ()
              }
            }
          }
        case _ =>
          it must s"parse binary file ${p.getFileName} in directory ${dir.getFileName}" in {
            println(s"Parsing binary file ${p.getFileName} in directory ${dir.getFileName}")
            val module = Parsing.fromBinary(p)
            val instantiatedModule = testInterp.interp.initializeModule(module, testInterp.imports)
            if (exports.contains(p.getFileName.toString)) {
              // add module to interpreter imports
              testInterp.imports += exports(p.getFileName.toString) -> instantiatedModule
            }
            module should not be null
          }
      }

    }
  }