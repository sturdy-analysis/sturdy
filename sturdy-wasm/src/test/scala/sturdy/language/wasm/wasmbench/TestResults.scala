package sturdy.language.wasm.wasmbench:

  class RRecord(elems: (String, Any)*) extends Selectable:
    val ordering = elems.map(_._1).zipWithIndex.sortWith((l,r) => {l._2 < r._2})
    val fields = elems.toMap
    def selectDynamic(name: String): Any = fields(name)
    def toCsv: String =
      val res = ordering.foldLeft("")((acc,el) => {
        s"${acc}${fields(el._1)};"
      })
      res.dropRight(1)
    def getCsvHeaders: String =
      ordering.map(_._1).mkString(";")
    def keyValuePairs: Seq[(String, Any)] =
      ordering.foldLeft(Seq.empty)((acc,el) => {
        acc.appended(el._1 -> fields(el._1))
      })
    def updated(other: RRecord): RRecord =
      this.updated(other.keyValuePairs*)
    def updated(new_elems: (String,Any)*): RRecord =
      val new_ordering = this.ordering.appendedAll(new_elems.map(_._1).zipWithIndex.sortWith((l,r) => {l._2 < r._2}))
      val new_fields = new_elems.foldLeft(fields)((acc,elem) => {
        acc + elem
      })
      new RRecord{
        override val ordering = new_ordering
        override val fields = new_fields
      }
    def without(key: String): RRecord =
      var idx = -1
      val new_ordering = ordering.foldLeft[Seq[(String,Int)]](Seq.empty)((acc,el) => {
        if el._1 == key then {
          idx = el._2
          acc
        }
        else if el._2 >= idx && idx != -1 then {
          acc.appended((el._1,el._2 - 1))
        }
        else acc.appended((el._1,el._2))
      })
      val new_fields = fields.filter(kv => {
        if kv._1 == key then false else true
      })
      val res = new RRecord{
        override val fields = new_fields
        override val ordering = new_ordering
      }
      res

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