package sturdy.language.wasm.abstractions

import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{ExceptionInstance, FunctionInstance}
import sturdy.values.references.ReferenceOps

trait ConcreteReference extends Interpreter:
  override final type Reference = FunctionInstance | ExternReference | ExceptionInstance[Value]
  override final type RefV = Reference
  override final type FunV = FunctionInstance

  given ConcreteRefOps: ReferenceOps[FunV, RefV] with {
    override def mkNullRef: RefV = FunctionInstance.Null
    override def mkExternNullRef: RefV = ExternReference.Null
    override def mkRef(trg: FunV): RefV = trg
    override def deref(v: RefV): FunV = v match {
      case ref: FunctionInstance => ref
      case _ => throw IllegalStateException("Cannot dereference externref")
    }
  }
