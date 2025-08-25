package sturdy.language.wasm.abstractions

import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.FunctionInstance
import sturdy.values.references.ReferenceOps
import sturdy.values.{Powerset, Structural, given}

trait PowersetReference extends Interpreter:
  override final type Reference = Powerset[FunctionInstance | ExternReference]
  override final type RefV = Reference
  override final type FunV = Powerset[FunctionInstance]
  
  given Structural[FunctionInstance | ExternReference] with {}

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
  }