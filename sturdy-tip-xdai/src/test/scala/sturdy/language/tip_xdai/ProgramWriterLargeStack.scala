package sturdy.language.tip_xdai

object ProgramWriterLargeStack:
  def bla(numFuncs: Int, numVars: Int): Unit =
    assert(numVars >= 1)
    val program = StringBuilder()
    var i = 1
    while (i < numFuncs) {
      program.append(
        s"""f$i(n) {
           |  return f${i + 1}(n);
           |}
           |""".stripMargin
      )
      i += 1
    }

    program.append(s"""f$numFuncs(n) {
                      |  return factorial(n);
                      |}
                      |""".stripMargin
    )
    program.append(
      """main() {
        |  var """.stripMargin
    )

    program.append("x1")
    i = 2
    while (i <= numVars)
      program.append(s",x$i")
      i += 1
    program.append(";\n  ")
    i = 1
    while (i <= numVars)
      program.append(s"x$i=0;")
      i += 1
    program.append("\n  return f1(10);\n}")
    program.append(s"""
                      |factorial(n) {
                      |  var r;
                      |  if (n == 1) {
                      |    r = 1;
                      |  } else {
                      |    r = n * factorial(n - 1);
                      |  }
                      |  return r;
                      |}""".stripMargin)
    TipProgramWriter.writeStingToFile(f"large${numFuncs}%05d_${numVars}Stack", program.toString)

  def main(ags: Array[String]): Unit = {
    (100 until 2000 by 100).foreach(
      bla(_, 20000)
    )
  }



