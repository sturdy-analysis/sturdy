package sturdy.language.tip_xdai.references.sign

import sturdy.data.WithJoin
import sturdy.language.tip_xdai.references.GenericInterpreter
import sturdy.language.tip_xdai.core.abstractions.{BoolValue, TopValue}
import sturdy.language.tip_xdai.core.{TypeError, Value}
import sturdy.values.references.{AbstractReference, AllocationSiteAddr, LiftedReferenceOps, NullDereference, PowersetAddr, ReferenceOps, abstractReferenceOps}


trait SignInterpreter extends GenericInterpreter[Value, AbstractSignAddr, WithJoin]:
  final def topReference: AbstractReference[AbstractSignAddr] =
    val addrs = store.getState.asInstanceOf[Map[AllocationSiteAddr, _]].keySet
    val aa = PowersetAddr(addrs)
    AbstractReference.NullAddr(aa, false)

  override val refOps: ReferenceOps[AbstractSignAddr, Value] = new LiftedReferenceOps[Value, AbstractSignAddr, AbstractReference[AbstractSignAddr]](
    {
      case TopValue => topReference
      case RefValue(ref) => ref
      case _ => failure(TypeError, s"Expected Reference but got $this")
    },
    RefValue.apply
  )
