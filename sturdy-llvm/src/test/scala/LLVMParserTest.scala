import org.scalatest.flatspec.AnyFlatSpec
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.StreamConverters.*
import scala.util.{Failure, Success, Try}
import org.scalatest.matchers.should.Matchers


class LLVMParserTest extends AnyFlatSpec, Matchers {

  behavior of "basics / LLVM Parser"

  private val uri = classOf[LLVMParserTest].getResource("/ir").toURI;


  Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith(".ll")).sorted.foreach { p =>
    val file = Source.fromURI(p.toUri)
    val sourceCode = file.getLines().mkString("\n")
    print(sourceCode)
    file.close()
    LLVMParser.functionDef.parseAll(sourceCode).foreach { prog =>
      println(prog)
    }
  }

}
