package sturdy.language.wasm.generic

import swam.LabelIdx

import scala.collection.mutable.ArrayBuffer

class LabelStack:
  private var labelReturnArities: ArrayBuffer[LabelIdx] = ArrayBuffer()
  inline def lookupLabel(lableIndex: LabelIdx): LabelIdx = labelReturnArities(lableIndex)
  inline def pushLabel(returnArity: LabelIdx): Unit =
    labelReturnArities += returnArity
  inline def popLabel(): Unit =
    labelReturnArities.remove(labelReturnArities.size - 1)
  
  def withFresh[A](f: => A): A =
    val snapshot = labelReturnArities
    labelReturnArities = ArrayBuffer()
    try f finally 
      labelReturnArities = snapshot