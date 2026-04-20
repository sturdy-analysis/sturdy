package sturdy.values.ordering

import sturdy.apron.{ApronBool, ApronExpr, ApronState, ResolveState}


final class NonRelationalOrderingOps[
    V,
    Addr,
    Type
  ]
  (using
   apronState: ApronState[Addr,Type],
   relationalIntOps: OrderingOps[V,ApronBool[Addr,Type]]
  )
  extends LiftedOrderingOps[V, ApronBool[Addr,Type], V, ApronBool[Addr,Type]](
    extract = expr => expr,
    inject = apronState.toNonRelational(_)(using ResolveState.Internal)
  )

final class NonRelationalUnsignedOrderingOps[
    V,
    Addr,
    Type
  ]
  (using
   apronState: ApronState[Addr, Type],
   relationalIntOps: UnsignedOrderingOps[V, ApronBool[Addr, Type]]
  )
  extends LiftedUnsignedOrderingOps[V, ApronBool[Addr, Type], V, ApronBool[Addr, Type]](
    extract = expr => expr,
    inject = apronState.toNonRelational(_)(using ResolveState.Internal)
  )

final class NonRelationalEqOps[
    V,
    Addr,
    Type
  ]
  (using
   apronState: ApronState[Addr,Type],
   relationalIntOps: EqOps[V,ApronBool[Addr,Type]]
  )
    extends LiftedEqOps[V, ApronBool[Addr,Type], V, ApronBool[Addr,Type]](
      extract = expr => expr,
      inject = apronState.toNonRelational(_)(using ResolveState.Internal)
    )