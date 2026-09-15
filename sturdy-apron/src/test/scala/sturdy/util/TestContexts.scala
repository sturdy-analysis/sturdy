package sturdy.util

import scala.math.Ordered.orderingToOrdered
import sturdy.values.Finite
import TestTypes.*
import sturdy.apron.ApronExpr
import sturdy.effect.Stateless
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.values.references.VirtualAddress


object TestContexts:


  enum Ctx:
    case Var(name: String)
    case TempVar(tpe: Type, n: Int)

  given Finite[Ctx] with {}

  given Ordering[Ctx] = {
    case (Ctx.Var(n1), Ctx.Var(n2)) => n1.compare(n2)
    case (Ctx.TempVar(t1, n1), Ctx.TempVar(t2, n2)) => (t1, n1).compare((t2, n2))
    case (Ctx.Var(_), Ctx.TempVar(_, _)) => 1
    case (Ctx.TempVar(_, _), Ctx.Var(_)) => -1
    case _ => -1
  }

  given variableAllocator: Allocator[Ctx, (String, Any)] =
    AAllocatorFromContext((v,_) => Ctx.Var(v))

  given tempVariableAllocator: Allocator[Ctx, Type] = new Allocator[Ctx, Type] with Stateless:
    var n = 0

    override def alloc(ctx: Type): Ctx =
      n += 1
      Ctx.TempVar(ctx, n)
      
  given combineExprAllocator: Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type],ApronExpr[VirtualAddress[Ctx], Type])] = new Allocator[Ctx, (ApronExpr[VirtualAddress[Ctx], Type],ApronExpr[VirtualAddress[Ctx], Type])] with Stateless:
    var n = 0
    override def alloc(exprs: (ApronExpr[VirtualAddress[Ctx], Type],ApronExpr[VirtualAddress[Ctx], Type])): Ctx =
      val tpe = exprs._1._type
      n += 1
      Ctx.TempVar(tpe, n)