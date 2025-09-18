package sturdy.language.wasm.benchmarksgame

import apron.*
import org.scalatest.Suites
import sturdy.fix.Fixpoint
import sturdy.language.wasm.analyses.RelationalAnalysis

class BenchmarksgameRelationalControlEventsTests extends Suites(
  new BenchmarksgameRelationalControlEventsTest(Polka(true), relational = true),
//  new BenchmarksgameRelationalControlEventsTest(Octagon(), relational = true),
//  new BenchmarksgameRelationalControlEventsTest(Box(), relational = true),
  new BenchmarksgameRelationalControlEventsTest(Polka(true), relational = false),
)

class BenchmarksgameRelationalControlEventsTest(manager: apron.Manager, relational: Boolean)
  extends BenchmarksgameControlEventsTest[RelationalAnalysis.type](
    RelationalAnalysis,
    (rootFrame,initVars,config) => RelationalAnalysis.Instance(manager,rootFrame,initVars,config.copy(relational = relational))
  ):
  override def suiteName: String = "Benchmarks Game with " + manager.getClass.getSimpleName
