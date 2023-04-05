package sturdy.values.ints

import sturdy.values.Abstractly


trait IntIntervalLikeCompanion:
    type T
    
    val Top: T
    def bounded(l: Long, h: Long): T

trait IntIntervalLike:
    type T
    type Bounds

    val l: Bounds
    val h: Bounds

    def join(other: T): T
    def +(y: T): T
    def -(y: T): T
    def *(y: T): T
    def /(y: T): T
    //def withBounds2(f: (_, _) => _, that: T): T
    
    override def toString: String = s"[$l,$h]"

   
