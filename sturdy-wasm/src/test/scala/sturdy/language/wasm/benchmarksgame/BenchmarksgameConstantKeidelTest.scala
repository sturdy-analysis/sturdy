package sturdy.language.wasm.benchmarksgame

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.effect.failure.AFallible
import sturdy.language.wasm.generic.finiteFixIn
import sturdy.fix
import sturdy.fix.{Fixpoint, InsensitiveStack, KeidelFixpoint}
import sturdy.fix.iter.Config
import sturdy.language.wasm
import sturdy.language.wasm.{ConcreteInterpreter, Parsing}
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow, Fix}
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData}
import sturdy.values.Topped
import sturdy.language.wasm.analyses.ConstantAnalysis.Value
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import scala.reflect.ClassTag
import scala.reflect.TypeTest
import sturdy.effect.{AnalysisState, EffectStack}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinableConcreteCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableConcreteOperandStack, given}
import sturdy.effect.symboltable.{ConstantSymbolTable, JoinableConcreteSymbolTable}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.values.floating.FloatOps
import swam.syntax.*
import swam.FuncType
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}
import sturdy.data.{*, given}
import sturdy.util.{LinearStateOperationCounter, Profiler}

import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameConstantKeidelTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame (recompiled) constant analysis"

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.headOption.foreach { p =>
    it must s"warm-up constant analysis on benchmark ${p.getFileName}" in {
      run(p, binary = true)
      LinearStateOperationCounter.clearAll()
      Profiler.reset()
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

    val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty) {

      override val fixpointSuper: fix.Fixpoint[FixIn, FixOut[Value]] = new KeidelFixpoint(
        Fix.isFunOrLoopToIndex, Seq(Config.Innermost, Config.Innermost), new InsensitiveStack[FixIn, InState]())

      override val fixpoint = null
    }

    val modInst = interp.initializeModule(module)
    val res = Profiler.addTime("analysis") {
      interp.failure.fallible(
        interp.invokeExported(modInst, funcName, List.empty)
      )
    }

    LinearStateOperationCounter.addToListAndReset()
    println(interp.analysisState.getAllState)
    println(s"${LinearStateOperationCounter.toString} in the last tests")
    println(s"#linear state operations in the last tests: ${LinearStateOperationCounter.getSummedOperationsPerTest}")

    Profiler.printLastMeasured()
