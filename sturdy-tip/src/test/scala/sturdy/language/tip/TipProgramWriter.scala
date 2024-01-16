package sturdy.language.tip

import java.io.File

object TipProgramWriter:

  def otherFactorial(numVars: Int): Unit =
    assert(numVars >= 2)
    val varsSepByComma = (2 until numVars).foldLeft("x1")((substr, index) => substr + s", x$index")
//    val setVarsToOne = (1 until numVars).foldLeft("")((substr, index) => substr + s"x$index=1;")
    val program =
      s"""
         |factorial(n) {
         |  var r,$varsSepByComma;
         |  if (n == 1) {
         |    r = 1;
         |  } else {
         |    r = n * factorial(n - 1);
         |  }
         |  return r;
         |}
         |
         |main() {
         |  return factorial(10);
         |}
      """.stripMargin

    writeStingToFile(s"otherFacManyVars$numVars", program)

  def factorial(numVars: Int): Unit =
    assert(numVars >= 2)

    val p = new java.io.PrintWriter(new File(s"sturdy-tip/src/test/resources/sturdy/language/tip/factorialManyVars$numVars.tip"))
    val startOfProgram = """
       |factorial(n) {
       |  var r;
       |  if (n == 1) {
       |    r = 1;
       |  } else {
       |    r = n * factorial(n - 1);
       |  }
       |  return r;
       |}
       |
       |main() {
       |  var """.stripMargin

    try {
      p.write(startOfProgram)
      val declaration: StringBuilder = new StringBuilder("x1")
      var i = 2
      while (i <= numVars)
        declaration.append(",x")
        declaration.append(i)
        i += 1
      declaration.append(";\n  ")
      p.append(declaration)
      val initialization: StringBuilder = new StringBuilder()
      i = 1
      while (i <= numVars)
        initialization.append("x")
        initialization.append(i)
        initialization.append("=1;")
        i += 1
      p.append(initialization)
      p.append(
        """
          |  return factorial(10);
          |}
          |""".stripMargin)

    } catch {
      case e => e.printStackTrace()
    } finally {
      p.close()
    }

  def incrementVarsToTen(numVars: Int): Unit =
    assert(numVars >= 2)

    var result = "inc(x) {\n"
    result += "  return x + 1;\n"
    result += "}\n\n"

    result += "main() {\n"
    result += "  var x0"
    result = (1 until numVars).foldLeft(result)((substr, index) => substr + s", x$index")
    result += ";\n"
    result = (0 until numVars).foldLeft(result)((substr, index) => substr + s"  x$index = 0;\n")
    result += "  while(10 > x0) {\n"
    result = (0 until numVars).foldLeft(result)((substr, index) => substr + s"    x$index = inc(x$index);\n")
    result += "  }\n"
    result += "  return x;\n"
    result += "}"
//    println(result)
    writeStingToFile("incrementVarsToTen", result)


  def writeStingToFile(fileName: String, content: String): Unit =
    val p = new java.io.PrintWriter(new File(s"sturdy-tip/src/test/resources/sturdy/language/tip/$fileName.tip"))
    try {
      p.write(content)
    } catch {
      case e => e.printStackTrace()
    } finally {
      p.close()
    }

  def main(args: Array[String]): Unit =
//    incrementVarsToTen(10000)
    for (storeSize <- List(10, 100, 1000, 10000, 100000, 200000, 300000, 400000, 500000)) {
      factorial(storeSize)
    }
