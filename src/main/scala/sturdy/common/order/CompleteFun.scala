package sturdy.common.order


object CompleteVal {
  object CompleteUnit extends CompleteVal[Unit] {
    override def join(x: Unit, y: Unit): Unit = ()
  }
}

trait CompleteVal[T] {
    def join(x: T, y: T): T
}

trait CompleteFun {
//  def join(x: T, y: T): T

  def join[A,B](c: CompleteVal[B], f: A=>B, g: A=>B): A=>B

//  def join[B](f: =>B, g: =>B): B = join(()=>f, ()=>g)()
}
