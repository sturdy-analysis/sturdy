package sturdy.language.wasm.wasmbench:

  class RRecord(elems: (String, Any)*) extends Selectable:
    private val fields = elems.toMap
    def selectDynamic(name: String): Any = fields(name)
    def toCsv: String =
      fields.values.mkString(";")
    def getCsvHeaders: String =
      fields.keys.mkString(";")

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
    val test = RRecord("bb" -> "346", "ab" -> "sdfhj", "c" -> 12).asInstanceOf[Test]

    for i <- Range.inclusive(0,100) do
      println(s"keys: ${test.getCsvHeaders}, values: ${test.toCsv}")