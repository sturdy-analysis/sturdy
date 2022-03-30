package sturdy.language.wasm.wasmbench

import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read,write}



import java.io.InputStream
import java.nio.file.{Files, Path, Paths}

class JSONStore(source: Path, maxSize: Int, firstRun: Boolean = false) extends Store:

  var md: Map[String, Metadata] = {
    implicit val formats: Formats = Serialization.formats(NoTypeHints)

    val inStream: InputStream = Files.newInputStream(source)

    if firstRun then
      val json = parse(inStream).asInstanceOf[JObject]
      val sturdyMd = wasmbench2sturdyMd(json) match {
        case JArray(arr) => arr.map(o => {
          ((o \ "hash").values.asInstanceOf[String], o)})
        case JObject(obj) => obj.map(o => {
          ((o._2 \\ "hash").values.asInstanceOf[String], o._2)
        })
      }
      val newPath = Paths.get(source.toString.replaceAll("metadata\\.", "sturdy\\.metadata\\."))
      val outStream = Files.newOutputStream(newPath)
      val output = pretty(render(JObject(sturdyMd)))
      outStream.write(output.getBytes())
      read[Map[String, Metadata]](output)
    else {
      read[Map[String, Metadata]](inStream)
    }


  }

  override def load(): List[Metadata] = md.values.toList.filter(p => if p.sizeBytes > maxSize then false else true)
  override def load(keys: List[String]): List[Metadata] =
    keys.foldLeft(List.empty[Metadata])((acc, el) => md.get(el) match
      case Some(m) => m :: acc
      case None => acc)
  override def load(key: String): Option[Metadata] = md.get(key)

  override def store(data: List[Metadata]): Unit =
    for {
      m <- data
    } do {
      if md.isDefinedAt(m.hash) then
        md = md.updated(m.hash, m)
      else
        this.store(m)
    }
  override def store(data: Metadata): Unit = md = md + (data.hash -> data)

  private def wasmbench2sturdyMd(json: JObject): JValue =
    import org.json4s.JsonDSL.*

    for {
      (hash, md) <- json.obj
      JObject(fields) <- md
      JField("wasm_validate_no_extensions", v) <- fields
      JField("wasm_validate", w) <- fields
      JField("instruction_count", bi) <- fields
      if v == JBool(true)
      if w != JNull
      if bi != JNull
    } yield {
      val z = JObject(JField("hash",hash))
      fields.foldLeft[JObject](z)((acc, el) => el match {
        case ("size_bytes", v) => acc ~ ("sizeBytes" -> v)
        case ("instruction_count", v) => acc ~ ("instructionCount" -> v)
        case ("producers", prod) =>
          val langs: List[(String, JValue)] = for {
            JObject(fields) <- prod \\ "language"
            (lang, version) <- fields
          } yield (lang, version)
          val procs: List[(String, JValue)] = for {
            JObject(fields) <- prod \\ "processed-by"
            (lang, version) <- fields
          } yield (lang, version)
          acc ~ ("processors" -> procs) ~ ("languages" -> langs)
        case ("possible_source_languages", v) => acc ~ ("inferredSourceLanguages" -> v)
        case _ => acc
      })
    }

  private def jobj2metadata(obj: JObject): Metadata =
    implicit val formats: Formats = Serialization.formats(NoTypeHints)
    read[Metadata](compact(render(obj)))

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