package sturdy.language.wasm.abstractions

import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{ExceptionInstance, FunctionInstance}
import sturdy.values.references.ReferenceOps
import sturdy.values.{*, given}

trait PowersetReference extends Interpreter:
  override final type Reference = Powerset[FunctionInstance | ExternReference] | ExceptionInstance[Value]
  override final type RefV = Reference
  override final type FunV = Powerset[FunctionInstance]

  given Structural[FunctionInstance | ExternReference] with {}

  given CombineReference[W <: Widening]: Combine[Reference, W] with
    override def apply(r1: Reference, r2: Reference): MaybeChanged[Reference] = (r1, r2) match
      case (p1: Powerset[?], p2: Powerset[?]) =>
        val joined = p1.set ++ p2.set
        MaybeChanged(Powerset(joined).asInstanceOf[Powerset[FunctionInstance | ExternReference]], joined.size > p1.set.size)
      case (e1: ExceptionInstance[?], e2: ExceptionInstance[?]) if e1 == e2 =>
        Unchanged(e1.asInstanceOf[ExceptionInstance[Value]])
      case _ =>
        throw CannotJoinException(s"Cannot join $r1 and $r2")

  given PowersetReferenceOps: ReferenceOps[FunV, RefV] with {
    override def mkNullRef: RefV = Powerset(Set(FunctionInstance.Null))

    override def mkExternNullRef: RefV = Powerset(Set(ExternReference.Null))

    override def mkRef(trg: FunV): RefV = trg.asInstanceOf[Powerset[FunctionInstance | ExternReference]]

    override def deref(v: RefV): FunV = v match
      case Powerset(s) =>
        val funcs = s.collect { case fi: FunctionInstance => fi }
        if (funcs.size != s.size) {
          throw IllegalStateException("Cannot dereference externref")
        }
        Powerset(funcs)
      case _: ExceptionInstance[?] =>
        throw IllegalStateException("Cannot dereference exnref")
  }
