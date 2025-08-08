package sturdy.apron

import sturdy.util.Lazy
import sturdy.values.references.{PhysicalAddress, VirtualAddress}

trait RelationalExpr[Val, Addr, Type]:
  def getRelationalExpr(v: Val): Option[ApronExpr[Addr, Type]]
  def makeRelationalExpr(expr: ApronExpr[Addr,Type]): Val

given RelationalValueApronExpr[Addr, Type]: RelationalExpr[ApronExpr[Addr,Type], Addr, Type] with
  override def getRelationalExpr(v: ApronExpr[Addr, Type]): Option[ApronExpr[Addr, Type]] = Some(v)
  override def makeRelationalExpr(expr: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = expr

given RelationalValueApronExprPhysicalAddress[Val, Ctx, Type]
  (using
   virtRelationalValue: RelationalExpr[Val, VirtualAddress[Ctx], Type],
   convertExpr: Lazy[ApronExprConverter[Ctx, Type, Val]]
  ): RelationalExpr[Val, PhysicalAddress[Ctx], Type] with
  override def getRelationalExpr(v: Val): Option[ApronExpr[PhysicalAddress[Ctx], Type]] =
    virtRelationalValue.getRelationalExpr(v).map(convertExpr.value.virtToPhys(_))

  override def makeRelationalExpr(expr: ApronExpr[PhysicalAddress[Ctx], Type]): Val =
    virtRelationalValue.makeRelationalExpr(convertExpr.value.physToVirt(expr))