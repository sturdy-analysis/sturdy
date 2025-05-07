package sturdy.language.wasm.wasmbench

import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}

/*
  
In memory store of metadata.
Contains classes to read from metadata.json file and result.csv files.

*/


class JSONStore(mdSource: Path, exSource: Path) extends Store[String, WASMBenchBinary]:
  private lazy val cache : Map[String, WASMBenchBinary] =
    val nullSerializer = FieldSerializer[Metadata](
      { case JField("instructionCount", JInt(0)) => Some(JField("instructionCount", JInt(0))) },
      { case JField("instructionCount", JNull) => JField("instructionCount", JInt(0)) },
    )
    implicit val formats: Formats =
      Serialization.formats(ShortTypeHints(List(classOf[TypeDef])))
        + new WASMTypeSerializer
        + new LabelSerializer

    val mdStream: InputStream = Files.newInputStream(mdSource)
    val exStream: InputStream = Files.newInputStream(exSource)

    val md = read[Map[String, Metadata]](mdStream)
    val ex = read[Map[String, List[FuncDef]]](exStream)
    mdStream.close()
    exStream.close()

    ex.foldLeft[Map[String, WASMBenchBinary]](Map.empty) {
      case (acc, (hash, lis)) => md.get(hash) match {
        case Some(datum) => acc + (hash -> WASMBenchBinary(md = datum, ex = lis))
        case None => acc
      }
    }
  val wbbs: Map[String, WASMBenchBinary] = cache

  override def retrieve(predicate: WASMBenchBinary => Boolean): List[WASMBenchBinary] =
    wbbs.values.filter(predicate).toList
  override def retrieve(keys: List[String]): List[WASMBenchBinary] =
    keys.foldLeft(List.empty[WASMBenchBinary])((acc, el) => wbbs.get(el) match
      case Some(m) => m :: acc
      case None => acc)
  override def retrieve(key: String): Option[WASMBenchBinary] = wbbs.get(key)

  override def store(data: List[WASMBenchBinary]): Unit = ???
//    for {
//      m <- data
//    } do {
//      if wbbs.isDefinedAt(m.md.hash) then
//        wbbs = wbbs.updated(m.md.hash, m)
//      else
//        this.store(m)
//    }
  override def store(data: WASMBenchBinary): Unit = ??? // wbbs = wbbs + (data.md.hash -> data)

class ResultStore[A <: RRecord](src: Path) extends Store[String, A]:
  import scala.jdk.CollectionConverters.*
  private lazy val cache : Map[String, A] =
    val in = Files.newBufferedReader(src).lines().iterator().asScala
    val headers = in.next().split(";")

    in.map(s => {
      val d = s.split(";")
      (d(0), RRecord(headers.zip(d): _*).asInstanceOf[A])
    }).toMap
  val wbbs: Map[String, A] = cache

  def store(data: List[A]): Unit = ???
  def store(data: A): Unit = ???

class ErrorStore(src: Path) extends Store[String, String]:
  import scala.jdk.CollectionConverters.*
  private lazy val cache : Map[String, String] =
    val in = Files.newBufferedReader(src).lines().iterator().asScala
    val headers = in.next().split(";")

    in.map(s => {
      val d = s.split(";")
      (d(0), d(1))
    }).toMap

  val wbbs: Map[String, String] = cache

  def store(data: List[String]): Unit = ???
  def store(data: String): Unit = ???
