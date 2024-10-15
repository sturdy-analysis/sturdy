package sturdy.gradual

trait ElaborationOps[V, E]:
  def abstractToExpr(abstr: V): E
  
trait Elaboration[P]:
  def elaborate(p: P): P

