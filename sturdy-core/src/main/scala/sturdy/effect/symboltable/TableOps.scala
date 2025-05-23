package sturdy.effect.symboltable
import sturdy.data.{JOption, MayJoin}
import sturdy.effect.{Concrete, Effect}


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

trait StatelessTableOps[Value, TAddr, EIndex, Size, Entry, J[_] <: MayJoin[_]] extends TableOps[Value, TAddr, EIndex, Size, Entry, J], Concrete
