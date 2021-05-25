package sturdy.lang.whilee.vals

import sturdy.lang.whilee.RunFix

trait StdRunFix[In,Out] extends RunFix[In,Out] {
  override def fix(f: (In => Out) => In => Out): In => Out = f(fix(f))
}
