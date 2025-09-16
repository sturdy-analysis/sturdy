package sturdy.values.integer

import sturdy.apron.{ApronExpr, ApronState}

final class NonRelationalIntegerOps[
    L,
    Addr,
    Type
  ]
  (using
   apronState: ApronState[Addr,Type],
   relationalIntOps: RelationalBaseIntegerOps[L,Addr,Type]
  )
  extends LiftedIntegerOpsWithSignInterpretation[L, ApronExpr[Addr,Type], ApronExpr[Addr,Type]](
      extract = expr => expr,
      inject = apronState.toNonRelational
  )
