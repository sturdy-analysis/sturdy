package sturdy.values.ordering

import sturdy.apron.{ApronBool, ApronExpr, ApronState}


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
    inject = apronState.toNonRelational
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
    inject = apronState.toNonRelational
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
      inject = apronState.toNonRelational
    )