package sturdy.language.wasm.wasmbench

case class DeadCodeResult(hash: String,
                      duration: Long,
                      allInstructions: Int,
                      deadInstructions: Int,
                      deadInstructionPercent: Double,
                      deadLabels: Int,
                      deadLabelsPercent: Double,
                      allLabels: Int,
                      deadLabelsBlock: Int,
                      deadLabelLoop: Int,
                      deadLabelsIf: Int,
                      eliminatable: Int,
                      eliminatablePercent: Double
                     ):
  def toCsv: String =
    this.productIterator.mkString(";")

case class ConstantResult(constantInstructions: Int,
                          constantInstructionPercent: Int,
                          liveInstructions: Int
                         )

case class TaintResult(taintedAccesses: Int,
                       taintedAccessesPercent: Double)

case class FailureReport(hash: String,
                         msg: String):
  def toCsv: String =
    this.productIterator.mkString(";")

