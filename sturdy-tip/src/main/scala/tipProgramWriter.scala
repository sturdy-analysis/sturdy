import java.io.File

object tipProgramWriter:

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
    result += "  return x1;\n"
    result += "}"
//    println(result)
    val p = new java.io.PrintWriter(new File("sturdy-tip/src/test/resources/sturdy/language/tip/10x10.tip"))
    try {
      p.write(result)
    } catch {
      case e => e.printStackTrace()
    } finally {
      p.close()
    }

  def main(args: Array[String]): Unit =
    incrementVarsToTen(3)
