package sturdy.language.tip

import org.scalatest.flatspec.AnyFlatSpec
import sturdy.control.{ControlEventGraphBuilder, FileReaderControlObservable}

import scala.jdk.StreamConverters.*
import java.nio.file.{Files, OpenOption, Paths, StandardOpenOption}

class FileReaderControlEventTest extends AnyFlatSpec{

  val path = Paths.get(classOf[FileReaderControlEventTest].getResource("/sturdy/language/ce_logs").toURI);
  val path_result = path.resolve("results")

  if Files.exists(path_result) then Files.list(path_result).forEach(Files.delete(_)) ; Files.delete(path_result)
  Files.createDirectory(path_result)

  Files.list(path).toScala(List).filter(_.toString.endsWith(".ce")).sorted.foreach { p =>
    val reader = FileReaderControlObservable(p)
    val builder = reader.addControlObserver(new ControlEventGraphBuilder)


    reader.read()
    println(builder.get)

    Files.write(path_result.resolve(p.getFileName.toString + ".dot"), ("digraph G {" + builder.get.toGraphViz + "}").getBytes, StandardOpenOption.CREATE)

  }

}
