package sturdy.effect.symboltable

import sturdy.apron.{ApronRecencyState, RelationalValue}
import sturdy.data.{*, given}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.values.references.VirtualAddress
import sturdy.values.{*, given}

import scala.reflect.ClassTag

final class RelationalSymbolTable[Key: Finite, Symbol: Finite, Entry: Join: Widen, Ctx, Type]
  (
    symbolTableAllocator: Allocator[Ctx, (Key,Symbol,Type)]
  )
  (using
    apronState: ApronRecencyState[Ctx, Type, Entry],
    relationalValue: RelationalValue[Entry, VirtualAddress[Ctx], Type]
  )extends DecidableSymbolTable[Key, Symbol, Entry]:


  override type State = RelationalSymbolTableState
  override def getState: State = RelationalSymbolTableState(tables)
  override def setState(st: State): Unit = tables == st.tables
  override def join: Join[State] = (s1, s2) => combineTables(widen = false, s1.tables, s2.tables).map(RelationalSymbolTableState.apply)
  override def widen: Widen[State] = (s1, s2) => combineTables(widen = true, s1.tables, s2.tables).map(RelationalSymbolTableState.apply)

  private def combineTables(widen: Boolean, m1: Map[Key, Map[Symbol, Entry]], m2: Map[Key, Map[Symbol, Entry]]): MaybeChanged[Map[Key, Map[Symbol, Entry]]] =
    combineMaps(m1, m2, (key: Key, t1: Map[Symbol, Entry], t2: Map[Symbol, Entry]) =>
      combineMaps(t1, t2, (sym: Symbol, e1: Entry, e2: Entry) =>
        (relationalValue.getRelationalVal(e1), relationalValue.getRelationalVal(e2)) match
          case (Some(expr1), Some(expr2)) =>
            val allocator = AAllocatorFromContext[Type, Ctx](
              (tpe: Type) => symbolTableAllocator((key, sym, tpe))
            )
            apronState.combineExpr(widen, allocator)(expr1, expr2).map(relationalValue.makeRelationalVal)
          case (Some(_), None) | (None, Some(_)) | (None, None) =>
            if(widen)
              Widen(e1,e2)
            else
              Join(e1,e2)
      )
    )

  private def combineMaps[K,V](vs1: Map[K, V], vs2: Map[K, V], combineVals: (K,V,V) => MaybeChanged[V]): MaybeChanged[Map[K, V]] =
    var joined = vs1
    var changed = false
    for ((k, v2) <- vs2)
      joined.get(k) match
        case None =>
          joined += k -> v2
          changed = true
        case Some(v1) =>
          val joinedV = combineVals(k, v1, v2)
          joined += k -> joinedV.get
          changed |= joinedV.hasChanged
    MaybeChanged(joined, changed)

  case class RelationalSymbolTableState(tables: Map[Key, Map[Symbol, Entry]]):
    override def equals(obj: Any): Boolean =
      obj match
        case other: RelationalSymbolTableState =>
          MapEquals(this.tables, other.tables, MapEquals(_, _))
        case _ => false

  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    for (tab <- tables.values.iterator;
         entry <- tab.values.iterator;
         addr <- valueIterator(entry))
    yield (addr)