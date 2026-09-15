package sturdy.values.convert

import sturdy.apron.{ApronExpr, ApronState, ResolveState}

final class NonRelationalConvert[From, To, V, Addr,Type, Config <: ConvertConfig[_]]
  (using
   apronState: ApronState[Addr,Type],
   relationalConvert: Convert[From, To, V, ApronExpr[Addr,Type], Config]
  )
  extends LiftedConvert[From, To, V, ApronExpr[Addr,Type], V, ApronExpr[Addr,Type], Config](
    extract = expr => expr,
    inject = apronState.toNonRelational(_)(using ResolveState.Internal)
  )
