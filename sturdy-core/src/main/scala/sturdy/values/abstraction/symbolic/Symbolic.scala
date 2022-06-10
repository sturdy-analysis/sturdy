package sturdy.values.abstraction.symbolic

trait Symbolic[Tree, Sym]:
  def assign(s: Sym, t: Tree): Unit
  def embedTree(t: Tree): Sym

trait SymbolicValue[Tree, Sym, V] extends Symbolic[Tree, Sym]:
  def embedValue(v: V): Sym
  def extractValue(s: Sym): V

  def embedTreeAndExtractValue(t: Tree): V =
    extractValue(embedTree(t))
