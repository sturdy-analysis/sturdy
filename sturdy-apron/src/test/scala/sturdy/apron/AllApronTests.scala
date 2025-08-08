package sturdy.apron

import org.scalatest.Suites
import sturdy.effect.callframe.*
import sturdy.effect.store.*
import sturdy.values.convert.*
import sturdy.values.floating.*
import sturdy.values.integer.*
import sturdy.values.ordering.*

class AllApronTests extends Suites(
  new ApronJoinTest,
  new ApronLibraryTest,
  new RelationalCallFrameTest,
  new RecencyRelationalStoreTest,
  new RelationalStoreTest,
  new RelationalConvertTest,
  new RelationalFloatingOpsTest,
  new RelationalIntegerOpsTest,
  new RelationalEqOpsTest,
  new RelationalOrderingOpsTest
)
