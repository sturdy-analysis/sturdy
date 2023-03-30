package sturdy.incremental

import scala.annotation.targetName

/**
 * Implements change structures described in
 * _A theory of changes for higher-order languages: incrementalizing λ-calculi by static differentiation_
 * _Yufei Cai, Paolo G. Giarrusso, Tillmann Rendel, Klaus Ostermann_
 * _PLDI'14_
 */
trait Delta[A]:
  /** nil(x) = x ⊖ x */
  def nil(x: A): Delta[A]

  /** sub(x,y) = x ⊖ y */
  def sub(x: A, y: A): Delta[A]

  /** dx.add(x) = x ⊕ dx */
  def add(x: A): A

  /** this(x,x).isNilChange == true */
  def isNilChange: Boolean

given DeltaUnit: Delta[Unit] with
  def nil(_x: Unit) = DeltaUnit
  def sub(_x: Unit, _y: Unit) = DeltaUnit
  def add(x: Unit): Unit = ()
  def isNilChange = true

given DeltaProduct[A,B] (using da: Delta[A], db: Delta[B]): Delta[(A,B)] with
  def nil(x: (A,B)) = DeltaProduct(using da.nil(x._1), db.nil(x._2))
  def sub(x: (A,B), y: (A,B)) = DeltaProduct(using da.sub(x._1,y._1), db.sub(x._2, y._2))
  def add(x: (A,B)): (A,B) = (da.add(x._1), db.add(x._2))
  def isNilChange = da.isNilChange && db.isNilChange
//  extension (x: Unit)
//    @targetName("add")
//    def ⊕(dx: Unit): Unit = ()
//    @targetName("sub")
//    def ⊖(y: Unit): Unit = ()
//    def isNilChange(dx: Unit) = true

//given ProductChangeStructure[A,B,DA,DB](using ca: ChangeStructure[A,DA], cb: ChangeStructure[B,DB]): ChangeStructure[(A,B),(DA,DB)] with
//  extension (x: (A,B))
//    @targetName("add")
//    def ⊕(dx: (DA,DB)): (A,B) = (x._1 ⊕ dx._1, x._2 ⊕ dx._2)
//    @targetName("sub")
//    def ⊖(y: (A,B)): (DA,DB) = (x._1 ⊖ y._1, x._2 ⊖ y._2)
//
//    def isNilChange(dx: (DA,DB)) =
//      x._1.isNilChange(dx._1) &&
//      x._2.isNilChange(dx._2)

//case class Replace[A](element: A)
//sealed class ReplaceChangeStructure[A] extends ChangeStructure[A]:
//  type Delta = Replace[A]
//  extension (x: A)
//    @targetName("add")
//    def ⊕(dx: Delta): A = dx.element
//    @targetName("sub")
//    def ⊖(y: A): Delta = Replace(x)
//    def isNilChange(dx: Delta) = ???