package sturdy.language.wasm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sturdy.language.wasm.abstractions.CfgConfig
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.language.wasm.generic.{FunctionInstance, FuncId, InstLoc, FrameData}
import sturdy.values.Topped

import java.nio.file.{Path, Paths}

class CfgNodesTest extends AnyFlatSpec, Matchers:
  behavior of "cfg nodes generation"

  val uri = classOf[CfgNodesTest].getResource("/sturdy/language/wasm/cfg_test.wast").toURI();
  val path = Paths.get(uri)

  testCfgNodes(path, "fac-rec", List(ConstantAnalysis.Value.Int64(Topped.Actual(3))))


def testCfgNodes(path: Path, funName: String, args: List[ConstantAnalysis.Value]) =
  val module = parse(path)
  val interp = ConstantAnalysis(FrameData.empty, Iterable.empty, CfgConfig.AllNodes(sensitive = false))
  val modInst = interp.initializeModule(module)
  val result = interp.effects.fallible(
    interp.invokeExported(modInst, "test1", List(ConstantAnalysis.Value.Int32(Topped.Actual(1))))
  )
  //val result2 = interp.effects.fallible(
  //  interp.invokeExported(modInst, "test1", List(ConstantAnalysis.Value.Int32(Topped.Actual(0))))
  //)
  println(interp.cfg.toGraphViz)

  val allNodes = ControlFlow.allCfgNodes(List(modInst))
  println("all nodes:")
  allNodes.toList.sortBy(_.toString).foreach(println(_))

  println("visited nodes:")
  interp.cfg.getNodes.sortBy(_.toString).foreach(println(_))

  val deadNodes = interp.cfg.filterDeadNodes(allNodes)
  if (deadNodes.nonEmpty)
    println("found dead nodes:")
    println(deadNodes.toList.sortBy(_.toString))

  val missingNodes = Set.from(interp.cfg.getNodes.map(_.node)).removedAll(allNodes)
  if (missingNodes.nonEmpty)
    println("found missing nodes:")
    println(missingNodes.toList.sortBy(x => if (x == null) then "null" else x.toString))
  //val FunctionInstance.Wasm(mi,fi,func,_) = modInst.functions(0)
  //val body = func.body
  //val loc = InstLoc.InFunction(FuncId(mi,fi),0)
  //println(loc.withLoc(body))
