package sturdy.language.wasm.benchmarksgame

import apron.*
import org.scalatest.Suites
import sturdy.language.wasm.analyses.RelationalAnalysis

class BenchmarksgameRelationalControlEventsTests extends Suites(
  new BenchmarksgameRelationalControlEventsTest(Polka(true)),
  new BenchmarksgameRelationalControlEventsTest(Octagon()),
  new BenchmarksgameRelationalControlEventsTest(Box()),
)

class BenchmarksgameRelationalControlEventsTest(manager: apron.Manager)
  extends BenchmarksgameControlEventsTest[RelationalAnalysis.type](
    RelationalAnalysis,
    RelationalAnalysis.Instance(manager,_,_,_)
  ):
  override def suiteName: String = "Benchmarks Game with " + manager.getClass.getSimpleName
