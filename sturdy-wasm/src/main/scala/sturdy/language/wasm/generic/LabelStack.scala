package sturdy.language.wasm.generic

import swam.LabelIdx

import scala.collection.mutable.ArrayBuffer

class LabelStack:
  private var size: Int = 0
  private var labelReturnArities: ArrayBuffer[Int] = ArrayBuffer()

  def lookupLabel(lableIndex: LabelIdx): Int =
    labelReturnArities(labelReturnArities.size - lableIndex - 1)

  def pushLabel(returnArity: Int): Unit =
    labelReturnArities += returnArity

  def popLabel(): Int =
    labelReturnArities.remove(labelReturnArities.size - 1)
  
  def withFresh[A](f: => A): A =
    val snapshot = labelReturnArities
    labelReturnArities = ArrayBuffer()
    try f finally 
      labelReturnArities = snapshot


  override def toString: String = s"LabelStack(${labelReturnArities.mkString(", ")})"
