package sturdy.effect.stack

import apron.*
import sturdy.{IsSound, Soundness, seqIsSound}
import sturdy.apron.{ApronCons, ApronExpr, ApronRecencyState, ApronState, ApronType, ApronVar, IntApronType, RelationalValue, given}
import sturdy.data.{JOption, JOptionA, JOptionC, NoJoin, WithJoin, given}
import sturdy.effect.EffectStack
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.operandstack.JoinableDecidableOperandStack
import sturdy.effect.store.{RecencyClosure, RecencyRelationalStore, RecencyStore, RelationalStore, given}
import sturdy.util.{Lazy, lazily}
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

import scala.collection.immutable.{ArraySeq, HashMap}
import scala.reflect.ClassTag

final class RelationalStack
  [
    Val: Join: Widen,
    Ctx: Ordering : Finite,
    Type: ApronType : Join : Widen
  ]
  (
    val stackAllocator: Allocator[Ctx, (Int,Type)]
  )
  (using
    apronState: ApronRecencyState[Ctx, Type, Val],
    relationalValue: RelationalValue[Val, VirtualAddress[Ctx], Type]
  )
  extends JoinableDecidableOperandStack[Val]:

  override def join: Join[List[Val]] = combineFrames(false, _, _)
  override def widen: Widen[List[Val]] = combineFrames(true, _, _)

  private def combineFrames(widen: Boolean, ops1: List[Val], ops2: List[Val]): MaybeChanged[List[Val]] =
    var hasChanged = false
    val joinedFrame = ops1.zipAll[Val, Val](ops2, null.asInstanceOf[Val], null.asInstanceOf[Val]).zipWithIndex.map {
      case ((v1, null),_) => v1
      case ((null, v2),_) => v2
      case ((v1, v2),idx) =>
        val v = combineValues(widen, idx, v1, v2)
        hasChanged |= v.hasChanged
        v.get
    }
    MaybeChanged(joinedFrame, hasChanged)

  private def combineValues(widen: Boolean, idx: Int, v1: Val, v2: Val): MaybeChanged[Val] =
    (relationalValue.getRelationalVal(v1), relationalValue.getRelationalVal(v2)) match
      case (Some(e1), Some(e2)) =>
        val allocator = AAllocatorFromContext[Type, Ctx](
          (tpe: Type) => stackAllocator((idx, tpe))
        )
        apronState.combineExpr(widen, allocator)(e1, e2).map(relationalValue.makeRelationalVal)
      case (Some(_), None) | (None, Some(_)) | (None, None) =>
        if(widen)
          Widen(v1,v2)
        else
          Join(v1,v2)

  override def joinWith(other: List[Val]): List[Val] =
    val (frame, rest) = stack.splitAt(stack.size - framePointer)
    val otherFrame = other.take(stack.size - framePointer)
    val joinedFrame = frame.zipAll[Val, Val](otherFrame, null.asInstanceOf[Val], null.asInstanceOf[Val]).zipWithIndex.map {
      case ((v1, null),_) => v1
      case ((null, v2),_) => v2
      case ((v1, v2),idx) => combineValues(false, idx, v1, v2).get
    }
    joinedFrame ++ rest
