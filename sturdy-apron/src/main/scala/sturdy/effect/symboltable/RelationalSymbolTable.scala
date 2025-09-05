package sturdy.effect.symboltable

import sturdy.IsSound
import sturdy.Soundness
import sturdy.apron.{ApronExpr, ApronRecencyState, StatelessRelationalExpr}
import sturdy.data.{*, given}
import sturdy.effect.ComputationJoiner
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}

import scala.reflect.ClassTag
import scala.util.boundary
import boundary.break

final class RelationalSymbolTable[Key: Finite, Symbol: Finite, Entry: Join: Widen, Ctx, Type]
  (
    symbolTableAllocator: Allocator[Ctx, (Key,Symbol)]
  )
  (using
    apronState: ApronRecencyState[Ctx, Type, Entry],
    relationalValue: StatelessRelationalExpr[Entry, VirtualAddress[Ctx], Type]
  ) extends DecidableSymbolTable[Key, Symbol, Entry]:

  val table: JoinableDecidableSymbolTable[Key, Symbol, PowVirtualAddress[Ctx]] = new JoinableDecidableSymbolTable()

  override def get(key: Key, symbol: Symbol): JOptionC[Entry] =
    table.get(key,symbol).flatMap { virts =>
      apronState.recencyStore.read(virts).asInstanceOf[JOptionA[Entry]].toJOptionC
    }.asInstanceOf[JOptionC[Entry]]

  override def set(key: Key, symbol: Symbol, newEntry: Entry): JOptionC[Unit] =
    val ctx = symbolTableAllocator.alloc((key,symbol))
    val virt = PowVirtualAddress(apronState.recencyStore.alloc(ctx))
    apronState.recencyStore.write(virt, newEntry)
    table.set(key, symbol, virt)
    

  override def putNew(key: Key): Unit =
    table.putNew(key)

  override type State = table.State
  override def getState: State = table.getState
  override def setState(st: State): Unit = table.setState(st)
  override def setBottom: Unit = table.setBottom
  override def join: Join[State] = table.join
  override def widen: Widen[State] = table.widen
  override def stackWiden: StackWidening[State] = table.stackWiden
  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = table.makeComputationJoiner
  override def addressIterator[Addr: ClassTag](valueIterator: Any => Iterator[Addr]): Iterator[Addr] =
    table.addressIterator[Addr](valueIterator)

  override def tableIsSound[cEntry](c: ConcreteSymbolTable[Key, Symbol, cEntry])(using Soundness[cEntry, Entry]): IsSound = boundary:
    c.entries.foreachEntry { (key, cTab) =>
      for ((sym, cEntry) <- cTab)
        val virt = table.getOrElse(key, sym, break(IsSound.NotSound(s"Table $key misses symbol $sym, bound to $cEntry in the concrete table.")))
        val aEntry = apronState.recencyStore.read(virt).asInstanceOf[JOptionA[Entry]].toOption.getOrElse(
          throw IllegalStateException(s"When reading symbol $sym, bound to $virt is not bound in ${apronState.recencyStore.getState}")
//          break(IsSound.NotSound(s"When reading symbol $sym, bound to $virt is not bound in ${apronState.recencyStore.getState}"))
        )
        val eSound = Soundness.isSound(cEntry, aEntry)
        if (!eSound.isSound)
          break(eSound)
    }
    IsSound.Sound

//  override type State = RelationalSymbolTableState
//  override def getState: State = RelationalSymbolTableState(tables)
//  override def setState(st: State): Unit = tables = st.tables
//  override def join: Join[State] = (s1, s2) => combineTables(widen = false, s1.tables, s2.tables).map(RelationalSymbolTableState.apply)
//  override def widen: Widen[State] = (s1, s2) => combineTables(widen = true, s1.tables, s2.tables).map(RelationalSymbolTableState.apply)
//
//  private def combineTables(widen: Boolean, m1: Map[Key, Map[Symbol, Entry]], m2: Map[Key, Map[Symbol, Entry]]): MaybeChanged[Map[Key, Map[Symbol, Entry]]] =
//    combineMaps(m1, m2, (key: Key, t1: Map[Symbol, Entry], t2: Map[Symbol, Entry]) =>
//      combineMaps(t1, t2, (sym: Symbol, e1: Entry, e2: Entry) =>
//        (relationalValue.getRelationalExpr(e1), relationalValue.getRelationalExpr(e2)) match
//          case (Some(expr1), Some(expr2)) =>
//            val allocator = AAllocatorFromContext[Type, Ctx](
//              (tpe: Type) => symbolTableAllocator((key, sym, tpe))
//            )
//            apronState.combineExpr(widen, allocator)(expr1, expr2).map(relationalValue.makeRelationalExpr)
//          case (Some(_), None) | (None, Some(_)) | (None, None) =>
//            if(widen)
//              Widen(e1,e2)
//            else
//              Join(e1,e2)
//      )
//    )
//
//  private def combineMaps[K,V](vs1: Map[K, V], vs2: Map[K, V], combineVals: (K,V,V) => MaybeChanged[V]): MaybeChanged[Map[K, V]] =
//    var joined = vs1
//    var changed = false
//    for ((k, v2) <- vs2)
//      joined.get(k) match
//        case None =>
//          joined += k -> v2
//          changed = true
//        case Some(v1) =>
//          val joinedV = combineVals(k, v1, v2)
//          joined += k -> joinedV.get
//          changed |= joinedV.hasChanged
//    MaybeChanged(joined, changed)
//
//  case class RelationalSymbolTableState(tables: Map[Key, Map[Symbol, Entry]]):
//    override def equals(obj: Any): Boolean =
//      obj match
//        case other: RelationalSymbolTableState =>
//          MapEquals(this.tables, other.tables, MapEquals(_, _))
//        case _ => false
//