package sturdy.language.wasm.generic

import scala.collection.mutable.ArrayBuffer

class LabelStack:
  private val labelReturnArities: ArrayBuffer[Int] = ArrayBuffer()
  inline def lookupLabel(lableIndex: Int): Int = labelReturnArities(lableIndex)
  inline def pushLabel(returnArity: Int): Unit =
    labelReturnArities += returnArity
  inline def popLabel(): Unit =
    labelReturnArities.remove(labelReturnArities.size - 1)