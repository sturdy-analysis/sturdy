package sturdy.util

import scala.collection.immutable.BitSet

object BitSet {

  def toString(bs: BitSet): String = bitSetToRanges(bs).map(rangeToString).mkString(" ")

  private def bitSetToRanges(bs: BitSet): List[Range] = {
    if (bs.isEmpty){
      Nil
    } else {
      val sorted = bs.toList.sorted
      val (ranges, lastStart, lastEnd) = sorted.tail.foldLeft((List.empty[Range], sorted.head, sorted.head)) {
        case ((acc, start, end), current) =>
          if (current == end + 1)
            (acc, start, current)
          else
            (Range(start, end + 1) :: acc, current, current)
      }
      (Range(lastStart, lastEnd + 1) :: ranges).reverse
    }
  }
  private def rangeToString(r: Range): String =
    if(r.size == 1)
      s"${r.start}"
    else
      s"[${r.start},${r.end-1}]"
}
