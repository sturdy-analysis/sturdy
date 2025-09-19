package sturdy.language.wasm.benchmarksgame

import apron.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sturdy.fix.StackConfig
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.{FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.abstractions.RelationalInfo.{*, given}
import sturdy.language.wasm.generic.{FrameData, ModuleInstance}
import sturdy.util.Profiler
import sturdy.values.{*, given}

import java.nio.file.{Files, Paths}
import scala.jdk.StreamConverters.*

class BenchmarksgameRelationalPrecisionTest extends AnyFunSuite, Matchers:

  val funcName = "_start"
  val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;
  val fixpointConfig: FixpointConfig = FixpointConfig(
    stack = StackConfig.StackedStates(
      readPriorOutput = false
    ),
    iter = sturdy.fix.iter.Config.Innermost
  )

  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith("binarytrees.wasm")).sorted.foreach { p =>
    test(s"${p.getFileName}") {
      val module = Parsing.fromBinary(p)

      val relationalAnalysis = RelationalAnalysis.Instance(Polka(true),FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = true))
      val relationalInfoLogger = relationalAnalysis.constrainedInstructionsLogger
      var moduleInst = relationalAnalysis.instantiateModule(module, moduleId = Some(p.getFileName))
      relationalAnalysis.failure.fallible(
        relationalAnalysis.invokeExported(moduleInst, funcName, List.empty)
      )

      val nonRelationalAnalysis = RelationalAnalysis.Instance(Polka(true),FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = false))
      val nonRelationalInfoLogger = nonRelationalAnalysis.constrainedInstructionsLogger
      moduleInst = nonRelationalAnalysis.instantiateModule(module, moduleId = Some(p.getFileName))
      nonRelationalAnalysis.failure.fallible(
        nonRelationalAnalysis.invokeExported(moduleInst, funcName, List.empty)
      )

      val relationalInfos = relationalInfoLogger.getAllInstructionInfos
      val nonRelationalInfos = nonRelationalInfoLogger.getAllInstructionInfos
      val comparedInfos = relationalInfos.iterator.flatMap((loc,infos1) =>
        nonRelationalInfos.get(loc).map(infos2 =>
          if(infos1.length != infos2.length)
            throw IllegalStateException(s"List of infos $infos1 and $infos2 for $loc do not have the same length")
          else
            (loc,PartialOrder[List[Info]].tryCompare(infos1, infos2))
        )
      ).toMap

      var relationalMorePrecise: Int = 0
      var nonRelationalMorePrecise: Int = 0
      var samePrecision: Int = 0
      var incomparable: Int = 0
      var totalComparedInstructions: Int = 0
      for(comp <- comparedInfos.values) {
        totalComparedInstructions += 1
        comp match
          case Some(n) =>
            if(n == 0)
              samePrecision += 1
            else if(n < 0)
              relationalMorePrecise += 1
            else /* if(n > 0) */
              nonRelationalMorePrecise += 1
          case None => incomparable += 1
      }
      assert(totalComparedInstructions == relationalMorePrecise + nonRelationalMorePrecise + samePrecision + incomparable)

      println(
        s"Relational more precise:    $relationalMorePrecise\n" +
        s"NonRelational more precise: $nonRelationalMorePrecise\n" +
        s"Same Precision:             $samePrecision\n" +
        s"Incomparable:               $incomparable"
      )

      Profiler.reset()
    }
  }