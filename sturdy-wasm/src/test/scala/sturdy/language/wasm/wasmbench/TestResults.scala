package sturdy.language.wasm.wasmbench:

  class RRecord(elems: (String, Any)*) extends Selectable:
    private val ordering = elems.map(_._1).zipWithIndex.sortWith((l,r) => {l._2 < r._2})
    private val fields = elems.toMap
    def selectDynamic(name: String): Any = fields(name)
    def toCsv: String =
      val res = ordering.foldLeft("")((acc,el) => {
        s"${acc}${fields(el._1)};"
      })
      res.dropRight(1)
    def getCsvHeaders: String =
      ordering.map(_._1).mkString(";")
    def updated(new_elems: (String,Any)*): RRecord =
      val new_ordering = this.ordering.appendedAll(new_elems.map(_._1).zipWithIndex.sortWith((l,r) => {l._2 < r._2}))
      val new_fields = new_elems.foldLeft(fields)((acc,elem) => {
        acc + elem
      })
      new RRecord{
        private val ordering = new_ordering
        private val fields = Map.empty
      }

  type Result = RRecord {
    val hash: String;
    val duration: Double;
    val allInstructions: Int;
    val deadInstructions: Int;
    val deadInstructionPercent: Double;
    val deadLabels: Int;
    val deadLabelsPercent: Double;
    val allLabels: Int;
    val deadLabelsBlock: Int;
    val deadLabelLoop: Int;
    val deadLabelsIf: Int;
    val eliminatable: Int;
    val eliminatablePercent: Double;
  }

  type ConstantResult = RRecord {
    val constantInstructions: Int
    val constantInstructionPercent: Double
    val liveInstructions: Int
  }

  type TaintResult = RRecord {
    val taintedAccesses: Int
    val taintedAccessesPercent: Double
  }
//
//  case class DeadCodeResult(hash: String,
//                            duration: Double,
//                            allInstructions: Int,
//                            deadInstructions: Int,
//                            deadInstructionPercent: Double,
//                            deadLabels: Int,
//                            deadLabelsPercent: Double,
//                            allLabels: Int,
//                            deadLabelsBlock: Int,
//                            deadLabelLoop: Int,
//                            deadLabelsIf: Int,
//                            eliminatable: Int,
//                            eliminatablePercent: Double
//                           ):
//    def toCsv: String =
//      this.productIterator.mkString(";")
//
//  case class ConstantResult(constantInstructions: Int,
//                            constantInstructionPercent: Double,
//                            liveInstructions: Int
//                           ):
//    def toCsv: String =
//      this.productIterator.mkString(";")
//
//  case class TaintResult(taintedAccesses: Int,
//                         taintedAccessesPercent: Double):
//    def toCsv: String =
//      this.productIterator.mkString(";")
//
//  case class FailureReport(hash: String,
//                           msg: String):
//    def toCsv: String =
//      this.productIterator.mkString(";")


  object Test extends App:

    type Test = RRecord {val ab: String; val bb: String; val c: Int}
    val test = RRecord(
      "bb" -> "346",
      "ab" -> "sdfhj",
      "c" -> 12,
      "123" -> "sfartt",
      "sf41234532523t" -> 2).asInstanceOf[Test]

    for i <- Range.inclusive(0,100) do
      println(s"keys: ${test.getCsvHeaders}, values: ${test.toCsv}")