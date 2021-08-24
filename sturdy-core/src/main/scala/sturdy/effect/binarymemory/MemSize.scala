package sturdy.effect.binarymemory

trait MemSize[V,Size]:
  def valToSize(v: V): Size
  def sizeToVal(s: Size): V
