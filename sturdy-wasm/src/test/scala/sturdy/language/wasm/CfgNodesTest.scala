package sturdy.language.wasm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.language.wasm.analyses.{ConstantAnalysis, WasmConfig}
import sturdy.language.wasm.generic.{FrameData, FuncId, FunctionInstance, InstLoc}
import sturdy.values.Topped

import java.nio.file.{Path, Paths}

class CfgNodesTest extends AnyFlatSpec, Matchers:
  behavior of "cfg nodes generation"

  val uri = this.getClass.getResource("/sturdy/language/wasm/cfg_test.wast").toURI;
  val path = Paths.get(uri)

  testCfgNodes(path, "fac-rec", List(ConstantAnalysis.Value.Int64(Topped.Actual(3))))


def testCfgNodes(path: Path, funName: String, args: List[ConstantAnalysis.Value]) =
  val module = Parsing.fromText(path)
  val interp = new ConstantAnalysis.Instance(FrameData.empty, Iterable.empty, WasmConfig.default)
  val cfg = ConstantAnalysis.controlFlow(CfgConfig.AllNodes(sensitive = false), interp)
  val modInst = interp.initializeModule(module)
  interp.failure.fallible(
    interp.invokeExported(modInst, "test1", List(ConstantAnalysis.Value.Int32(Topped.Top)))
  )
  interp.failure.fallible(
    interp.invokeExported(modInst, "fac-rec", List(ConstantAnalysis.Value.Int64(Topped.Top)))
  )
  interp.failure.fallible(
    interp.invokeExported(modInst, "fac-iter", List(ConstantAnalysis.Value.Int64(Topped.Top)))
  )
  println(cfg.toGraphViz)

  val allNodes = ControlFlow.allCfgNodes(List(modInst))
  println(s"all nodes: ${allNodes.size}")
//  allNodes.toList.sortBy(_.toString).foreach(println(_))

  println(s"visited nodes: ${cfg.getNodes.size}")
//  interp.cfg.getNodes.sortBy(_.toString).foreach(println(_))

  val deadNodes = cfg.filterDeadNodes(allNodes)
  if (deadNodes.nonEmpty)
    println("found dead nodes:")
    println(deadNodes.toList.sortBy(_.toString))

  val missingNodes = Set.from(cfg.getNodes.map(_.node)).removedAll(allNodes)
  if (missingNodes.nonEmpty)
    println("found missing nodes:")
    println(missingNodes.toList.sortBy(x => if (x == null) then "null" else x.toString))

  assert(deadNodes.size == 5)
  assert(missingNodes.size == 1)
