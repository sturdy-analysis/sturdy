// package sturdy.apron
// 
// import apron.{Environment, Interval}
// 
// trait ApronScope[Context]:
  // def apronEnv: Environment
  // def isBound(v: ApronVar[Context]): Boolean = apronEnv.hasVar(v)
  // def getBound(v: ApronVar[Context]): Interval
  // def getBound(v: ApronExpr[Context]): Interval
  // def getFreedReference(v: ApronVar[Context]): Option[ApronExpr[Context]]
  // def isFreed(v: ApronVar[Context]): Boolean = getFreedReference(v).nonEmpty