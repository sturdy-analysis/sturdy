package sturdy.language.wasm.benchmarksgame

import cats.effect.{Blocker, IO}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.control.{BasicControlEvent, BranchingControlEvent, ControlEventGraphBuilder, ControlEventParser, ControlObserver, ExceptionControlEvent, FixpointControlEvent, PrintingControlObserver, RecordingControlObserver}
import sturdy.effect.failure.AFallible
import sturdy.fix.{Fixpoint, StackConfig}
import sturdy.language.wasm
import sturdy.language.wasm.{ConcreteInterpreter, Parsing, testCfgDifference}
import sturdy.language.wasm.abstractions.{CfgConfig, CfgNode, ControlFlow}
import sturdy.language.wasm.analyses.*
import sturdy.language.wasm.generic.FrameData
import sturdy.util.{LinearStateOperationCounter, Profiler}
import sturdy.values.Topped
import swam.ModuleLoader
import swam.binary.ModuleParser
import swam.syntax.Module
import swam.validation.Validator

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.attribute.FileAttribute
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import scala.collection.mutable
import scala.jdk.StreamConverters.*

class BenchmarksgamePerfControlEventTest extends AnyFlatSpec, Matchers:
  behavior of "Benchmarksgame performance test for control event with interval analysis"

  private val funcName = "_start"
  private val uri = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI

  private val tests_names: mutable.ListBuffer[String] = mutable.ListBuffer.empty
  private val n_events: mutable.ListBuffer[Int] = mutable.ListBuffer.empty
  private val control: mutable.ListBuffer[Long] = mutable.ListBuffer.empty
  private val baseline: mutable.ListBuffer[Long] = mutable.ListBuffer.empty
  private val event_run: mutable.ListBuffer[Long] = mutable.ListBuffer.empty
  private val event_get: mutable.ListBuffer[Long] = mutable.ListBuffer.empty
  private val tree_run: mutable.ListBuffer[Long] = mutable.ListBuffer.empty
  private val tree_get: mutable.ListBuffer[Long] = mutable.ListBuffer.empty
  private val graph_nodes: mutable.ListBuffer[Int] = mutable.ListBuffer.empty
  private val graph_edges: mutable.ListBuffer[Int] = mutable.ListBuffer.empty

  measure(10)


  def measure(runs: Int): Unit =
    Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith("binarytrees.wasm")).sorted.headOption.foreach { p =>
      it must s"warm-up interval analysis on benchmark ${p.getFileName}" in {
        run(p, binary = true, StackConfig.StackedStates(), true)
        LinearStateOperationCounter.clearAll()
        Profiler.reset()
      }
    }

    List.range(0, runs).foreach(i => {
      Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".wasm")).sorted.foreach { p =>
        it must s"iteration $i : execute interval analysis with stacked states on benchmark ${p.getFileName}" in {
          tests_names.addOne(p.getFileName.toString)
          run(p, binary = true, StackConfig.StackedStates(), false)
        }
      }
    })

    it must s"Final result" in {

      println("tests_names : " + tests_names)
      println("n_events : " + n_events)
      println("control : " + control)
      println("baseline : " + baseline)
      println("event_run : " + event_run)
      println("event_get : " + event_get)
      println("tree_run : " + tree_run)
      println("tree_get : " + tree_get)
      println("graph_nodes : " + graph_nodes)
      println("graph_edges : " + graph_edges)

      val tree_time = tree_run.zip(tree_get).map(_ + _)
      val tree_slowdown = tree_time.zip(baseline).map(_.toDouble / _)
      val event_slowdown = event_run.zip(baseline).map(_.toDouble / _)

      println(tree_slowdown)
      println(event_slowdown)

      println("control : " + (control.sum.toDouble / control.size.toDouble) * 1e-9)
      println("baseline : " + (baseline.sum.toDouble / baseline.size.toDouble) * 1e-9)
      println("event_run : " + (event_run.sum.toDouble / event_run.size.toDouble) * 1e-9)
      println("event_get : " + (event_get.sum.toDouble / event_get.size.toDouble) * 1e-9)
      println("tree_run : " + (tree_run.sum.toDouble / tree_run.size.toDouble) * 1e-9)
      println("tree_get : " + (tree_get.sum.toDouble / tree_get.size.toDouble) * 1e-9)
      true
    }

  private def run(p: Path, binary: Boolean, stackConfig: StackConfig, ignore: Boolean) =
    Fixpoint.DEBUG = false

    val name = p.getFileName
    val module = if (binary) Parsing.fromBinary(p) else wasm.Parsing.fromText(p)

    val interp_control = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Innermost(stackConfig))))
    val recorder = interp_control.addControlObserver(new RecordingControlObserver)

    val res_control = Profiler.addTime("control") {
      interp_control.failure.fallible(
        interp_control.invokeExported(interp_control.initializeModule(module), funcName, List.empty)
      )
    }

    val interp_baseline = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Innermost(stackConfig))))

    val interp_event = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Innermost(stackConfig))))
    val graphBuilder = interp_event.addControlObserver(new ControlEventGraphBuilder)

    val interp_tree = new IntervalAnalysis.Instance(FrameData.empty, Iterable.empty,
      WasmConfig(fix = FixpointConfig(iter = sturdy.fix.iter.Config.Innermost(stackConfig))))
    val parser = interp_tree.addControlObserver(new ControlEventParser)

    val res_baseline = Profiler.addTime("baseline") {
      interp_baseline.failure.fallible(
        interp_baseline.invokeExported(interp_baseline.initializeModule(module), funcName, List.empty)
      )
    }

    var hash_event = ""
    val res_event = Profiler.addTime("event_run") {
      val modInst_event = interp_event.initializeModule(module)
      hash_event = modInst_event.toString
      interp_event.failure.fallible(interp_event.invokeExported(modInst_event, funcName, List.empty)
      )
    }

    var hash_tree = ""
    val res_tree = Profiler.addTime("tree_run") {
      val modInst_tree = interp_tree.initializeModule(module)
      hash_tree = modInst_tree.toString
      interp_tree.failure.fallible(interp_tree.invokeExported(modInst_tree, funcName, List.empty)
      )
    }

    val cfg_event = Profiler.addTime("event_get") {
      graphBuilder.get
    }

    val cfg_tree = Profiler.addTime("tree_get") {
      parser.getFinalTree.toGraph
    }

    if !ignore then
      n_events.addOne(recorder.events.size)
      control.addOne(Profiler.get("control").getOrElse(0))
      baseline.addOne(Profiler.get("baseline").getOrElse(0))
      event_run.addOne(Profiler.get("event_run").getOrElse(0))
      event_get.addOne(Profiler.get("event_get").getOrElse(0))
      tree_run.addOne(Profiler.get("tree_run").getOrElse(0))
      tree_get.addOne(Profiler.get("tree_get").getOrElse(0))
      graph_nodes.addOne(cfg_event.nodes.size)
      graph_edges.addOne(cfg_event.edges.size)

    Profiler.saveTimesAndReset()

    println(cfg_event.nodes.size)
    println(cfg_tree.nodes.size)

    assert(cfg_tree.toGraphViz == cfg_event.toGraphViz.replace(hash_event, hash_tree))


