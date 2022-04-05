package sturdy.language.wasm.wasmbench

import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read,write}

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}


class JSONStore(mdSource: Path, exSource: Path) extends Store:

  var wbbs: Map[String, WASMBenchBinary] = {

    val nullSerializer = FieldSerializer[Metadata](
      { case JField("instructionCount", JInt(0)) => Some(JField("instructionCount", JInt(0)))},
      { case JField("instructionCount", JNull) => JField("instructionCount", JInt(0))},
    )
    implicit val formats: Formats = Serialization.formats(ShortTypeHints(List(classOf[TypeDef], classOf[Label]))) + nullSerializer

    val mdStream: InputStream = Files.newInputStream(mdSource)
    val exStream: InputStream = Files.newInputStream(exSource)

    val md = read[Map[String, Metadata]](mdStream)
    val ex = read[Map[String,List[FuncDef]]](exStream)

    val g = ex.foldLeft[Map[String, WASMBenchBinary]](Map.empty){
      case (acc, (hash, lis)) => md.get(hash) match {
        case Some(datum) => acc + (hash -> WASMBenchBinary(md = datum, ex = lis))
        case None => acc
      }
    }
    g

//    ex.map{
//      case (hash, lis) => md.get(hash) match {
//        case Some(datum) => (hash, WASMBenchBinary(md = datum, ex = lis))
//      }
//    }
  }

  override def retrieve(predicate: WASMBenchBinary => Boolean): List[WASMBenchBinary] =
    wbbs.values.filter(predicate).toList
  override def retrieve(keys: List[String]): List[WASMBenchBinary] =
    keys.foldLeft(List.empty[WASMBenchBinary])((acc, el) => wbbs.get(el) match
      case Some(m) => m :: acc
      case None => acc)
  override def retrieve(key: String): Option[WASMBenchBinary] = wbbs.get(key)

  override def store(data: List[WASMBenchBinary]): Unit =
    for {
      m <- data
    } do {
      if wbbs.isDefinedAt(m.md.hash) then
        wbbs = wbbs.updated(m.md.hash, m)
      else
        this.store(m)
    }
  override def store(data: WASMBenchBinary): Unit = wbbs = wbbs + (data.md.hash -> data)


//object Test extends App:
//  import org.json4s.native.JsonMethods.*
//  import org.json4s.*
//  import org.json4s.native.Serialization
//  import org.json4s.native.Serialization.{read,write}
//  implicit val formats: Formats = Serialization.formats(NoTypeHints)
//
//  val t = JObject(List(
//      ("hash",JString("957e860e09bde8e26a45cd986dab1fe18289d290638bb9498602e98ebed41a15")),
//      ("sizeBytes",JInt(44304)),
//      ("processors",JObject(List(
//        ("clang",JString("10.0.0 (https://github.com/llvm/llvm-project d32170dbd5b0d54436537b6b75beaf44324e0c28"))))),
//      ("languages",JObject(List(
//        ("C_plus_plus_14",JString("")),
//        ("C99",JString(""))))),
//      ("instructionCount",JInt(14719)),
//      ("inferredSourceLanguages",JArray(List(JString("C"), JString("C++"))))))
//
//  val m = read[Metadata](compact(render(t)))
//  println(m)