package sturdy.effect.symboltable
import sturdy.data.{JOption, MayJoin}
import sturdy.effect.{Concrete, Effect}
import sturdy.values.{Join, Widen}


trait TableOps[Value, TAddr, EIndex, Size, Entry, J[_] <: MayJoin[_]] extends Effect:
  def get(table: TAddr, index: EIndex): JOption[J, Entry]
  def set(table: TAddr, index: EIndex, newEntry: Entry): JOption[J, Unit]
  def putNew(table: TAddr, limit: SizedSymbolTable.Limit[Size]): Unit
  def size(key: TAddr): Size
  def grow(key: TAddr, newSize: Size, initEntry: Entry): JOption[J, Size]
  def initTable(table: TAddr, elem: Vector[Entry], elemOffset: Value, tableOffset: Value, amount: Value): JOption[J, Unit]
  def fillTable(table: TAddr, entry: Entry, tableOffset: Value, amount: Value): JOption[J, Unit]
  def copy(dstTable: TAddr, srcTable: TAddr, dstOffset: Value, srcOffset: Value, amount: Value): JOption[J, Unit]

  def getOrElse(key: TAddr, index: EIndex, default: => Entry)(using J[Entry]): Entry =
    get(key, index).getOrElse(default)


trait WrappedSymbolicTableOps[Value, TAddr, EIndex, Size, Entry, J[_] <: MayJoin[_]] extends TableOps[Value, TAddr, EIndex, Size, Entry, J]:
  val table: SizedSymbolTable[TAddr, EIndex, Entry, Size, J]
  type State = table.State

  def get(tAddr: TAddr, index: EIndex): JOption[J, Entry] = table.get(tAddr, index)
  def set(tAddr: TAddr, index: EIndex, newEntry: Entry): JOption[J, Unit] = table.set(tAddr, index, newEntry)
  def putNew(tAddr: TAddr, limit: SizedSymbolTable.Limit[Size]): Unit = table.putNew(tAddr, limit)
  def size(tAddr: TAddr): Size = table.size(tAddr)
  def grow(tAddr: TAddr, newSize: Size, initEntry: Entry): JOption[J, Size] = table.grow(tAddr, newSize, initEntry)

  def getState: State = table.getState
  def setState(st: State): Unit = table.setState(st)
  def join: Join[State] = table.join
  def widen: Widen[State] = table.widen


trait DummyTableOps[Value, TAddr, EIndex, Size, Entry, J[_] <: MayJoin[_]] extends TableOps[Value, TAddr, EIndex, Size, Entry, J]:
  def get(table: TAddr, index: EIndex): JOption[J, Entry] = ???
  def set(table: TAddr, index: EIndex, newEntry: Entry): JOption[J, Unit] = ???
  def putNew(table: TAddr, limit: SizedSymbolTable.Limit[Size]): Unit = ???
  def size(key: TAddr): Size = ???
  def grow(key: TAddr, newSize: Size, initEntry: Entry): JOption[J, Size] = ???
  def initTable(table: TAddr, elem: Vector[Entry], elemOffset: Value, tableOffset: Value, amount: Value): JOption[J, Unit] = ???
  def fillTable(table: TAddr, entry: Entry, tableOffset: Value, amount: Value): JOption[J, Unit] = ???
  def copy(dstTable: TAddr, srcTable: TAddr, dstOffset: Value, srcOffset: Value, amount: Value): JOption[J, Unit] = ???
  def getState: State = ???
  def setState(st: State): Unit = ???
  def join: Join[State] = ???
  def widen: Widen[State] = ???