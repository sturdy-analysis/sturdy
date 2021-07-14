package mixins

trait ~>[A, B] {
  def apply(a: A): B
}
