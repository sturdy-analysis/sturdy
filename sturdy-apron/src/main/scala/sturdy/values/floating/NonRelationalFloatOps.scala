package sturdy.values.floating

import sturdy.apron.{ApronExpr, ApronState, ResolveState}

final class NonRelationalFloatOps[
    L,
    Addr,
    Type
  ]
  (using
   apronState: ApronState[Addr,Type],
   relationalFloatOps: RelationalFloatOps[L,Addr,Type]
  )
  extends LiftedFloatOps[L, ApronExpr[Addr,Type], ApronExpr[Addr,Type]](
    extract = expr => expr,
    inject = apronState.toNonRelational(_)(using ResolveState.Internal)
  )
