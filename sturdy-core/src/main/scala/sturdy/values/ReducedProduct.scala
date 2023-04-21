package sturdy.values

case class ReducedProduct[A,B](override val _1: A,
                               override val _2: B) extends Product2[A,B]:
   inline def map[C,D](f: A => C, g: B => D)(using Reduce[C,D]): ReducedProduct[C,D] =
     ReducedProduct(f(_1), g(_2)).reduce

object ReducedProduct:
  inline def binop[A,B,C,D](p1: ReducedProduct[A,B],
                            p2: ReducedProduct[A,B],
                            f: (A,A) => C,
                            g: (B,B) => D)
                           (using Reduce[C,D]): ReducedProduct[C,D] =
    ReducedProduct(f(p1._1,p2._1), g(p2._2, p2._2)).reduce

  inline def unop[A, B, C, D](p: ReducedProduct[A, B],
                              f: A => C,
                              g: B => D)
                              (using Reduce[C, D]): ReducedProduct[C, D] =
    ReducedProduct(f(p._1), g(p._2)).reduce

trait Reduce[A,B]:
  extension (p: ReducedProduct[A,B])
    def reduce: ReducedProduct[A,B]
given CombineReducedProduct[A,B,W <: Widening]
                           (using combineA: Combine[A,W],
                                  combineB: Combine[B,W],
                                  red: Reduce[A,B]):
                            Combine[ReducedProduct[A,B], W] with
  
  override def apply(v1: ReducedProduct[A, B], v2: ReducedProduct[A, B]): MaybeChanged[ReducedProduct[A, B]] =
    val res1 = combineA(v1._1, v2._1)
    val res2 = combineB(v1._2, v2._2)
    MaybeChanged(ReducedProduct(res1.get, res2.get).reduce,
                 res1.hasChanged || res2.hasChanged)

  override def lteq(x: ReducedProduct[A, B], y: ReducedProduct[A, B]): Boolean =
    combineA.lteq(x._1,y._1) && combineB.lteq(x._2, y._2)