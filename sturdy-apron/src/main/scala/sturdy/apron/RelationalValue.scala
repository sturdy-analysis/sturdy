package sturdy.apron

import sturdy.util.Lazy
import sturdy.values.references.{PhysicalAddress, VirtualAddress}

trait RelationalValue[Val, Addr, Type]:
  def getRelationalVal(v: Val): Option[ApronExpr[Addr, Type]]
  def makeRelationalVal(expr: ApronExpr[Addr,Type]): Val

given RelationalValueApronExpr[Addr, Type]: RelationalValue[ApronExpr[Addr,Type], Addr, Type] with
  override def getRelationalVal(v: ApronExpr[Addr, Type]): Option[ApronExpr[Addr, Type]] = Some(v)
  override def makeRelationalVal(expr: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = expr

given RelationalValueApronExprPhysicalAddress[Val, Ctx, Type]
  (using
   virtRelationalValue: RelationalValue[Val, VirtualAddress[Ctx], Type],
   convertExpr: Lazy[ApronExprConverter[Ctx, Type, Val]]
  ): RelationalValue[Val, PhysicalAddress[Ctx], Type] with
  override def getRelationalVal(v: Val): Option[ApronExpr[PhysicalAddress[Ctx], Type]] =
    virtRelationalValue.getRelationalVal(v).map(convertExpr.value.virtToPhys(_))

  override def makeRelationalVal(expr: ApronExpr[PhysicalAddress[Ctx], Type]): Val =
    virtRelationalValue.makeRelationalVal(convertExpr.value.physToVirt(expr))