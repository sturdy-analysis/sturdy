package dwarfSupport

import org.scalatest.funsuite.AnyFunSuite
import sturdy.language.wasm.Parsing
import BenchmarksgameFile.*
import sturdy.language.wasm.analyses.RelationalAnalysis

class RelationalMemoryTest extends AnyFunSuite {
  test("RelationalAnalysis can find globals in dwarf information") {
    val module = Parsing.fromBinary(getTestFile(Fankuchredux))
  }
}
