package sturdy.language.wasm.benchmarksgame

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.RecordingControlObserver
import sturdy.fix.Fixpoint
import sturdy.language.wasm
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameConcreteTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled) concrete interp"

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.headOption.foreach { p =>
    it must s"warm-up constant analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
    }
  }

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.foreach { p =>
    it must s"execute constant analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
    }
  }

  def run(p: Path, binary: Boolean = false) =
    Fixpoint.DEBUG = false
    
    val name = p.getFileName
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp = new ConcreteInterpreter.Instance(FrameData.empty, Iterable.empty)
    val controlRecorder = interp.addControlObserver(new RecordingControlObserver)

    println(s"Running $p")
    val modInst = interp.initializeModule(module)
    val res = interp.failure.fallible(
      interp.invokeExported(modInst, funcName, List.empty)
    )
    println(res)
    println(s"Recorded ${controlRecorder.events.size} control events")

