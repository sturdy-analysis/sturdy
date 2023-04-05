package sturdy.language.wasm

import cats.effect.{Blocker, IO}
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.io.InputStream
import java.nio.file.Path


def Parsing.fromBinary(stream: InputStream): Module =
  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)
  Blocker[IO].use { blocker =>
    for {
      validator <- Validator[IO](blocker)
      loader = new ModuleLoader[IO]()
      binaryParser = new ModuleParser[IO](validator)
      mod <- binaryParser.parse(loader.sections(stream.readAllBytes()))
    } yield mod
  }.unsafeRunSync()