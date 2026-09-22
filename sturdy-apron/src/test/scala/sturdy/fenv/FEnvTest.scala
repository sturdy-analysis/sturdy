package sturdy.fenv

import org.scalatest.funsuite.AnyFunSuite

class FEnvTest extends AnyFunSuite {
  test("Get and set rounding mode") {
    // whether setting succeeds is implementation-dependent.
    // Therefore, we only test that the functions do not throw.
    try {
      val originalMode = FEnv.getRoundingMode
      assert(originalMode.isDefined)

      // Test setting each rounding mode
      for (mode <- RoundingMode.values) {
        FEnv.setRoundingMode(mode)
      }

      // Restore the original rounding mode
      originalMode.foreach { mode =>
        val restoreResult = FEnv.setRoundingMode(mode)
        assert(restoreResult, s"Failed to restore original rounding mode to $mode")
      }
    } catch {
      case e: Throwable =>
        fail(s"Exception occurred during rounding mode test: ${e.getMessage}")
    }
  }
}
