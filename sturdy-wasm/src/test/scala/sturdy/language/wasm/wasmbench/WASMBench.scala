package sturdy.language.wasm.wasmbench

//import java.sql.{Connection, DriverManager}
import org.scalatest.flatspec.AnyFlatSpec
import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.JsonDSL.*
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.io.InputStream
import java.net.URI
import scala.collection.mutable
import sys.process.*
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import scala.jdk.StreamConverters.*


implicit val formats: Formats = DefaultFormats

class WASMBench extends AnyFlatSpec:
  
  val MAXSIZE: Int = 10485760

  val os: OS = {
    System.getProperty("os.name").toLowerCase() match {
      case "linux" => OS.Linux
      case "mac" => OS.Mac
      case _ => ???
    }
  }
  val filtering: Filtering = Filtering.Filtered
  val wasm2wat: Path = {
    val u: URI = this.getClass.getResource(s"/sturdy/language/wasm/wasmbench/wasm2wat_${os}").toURI
    Path.of(u)
  }

  metadataScripts()

  def metadataScripts() =
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read,write}
    implicit val formats: Formats = Serialization.formats(ShortTypeHints(List(classOf[TypeDef], classOf[Label])))

    val rootDir: Path = Path.of(this.getClass.getResource(s"/sturdy/language/wasm/wasmbench").toURI)

    val store: Store[String, WASMBenchBinary] = {
      val mdPath = rootDir.resolve(s"sturdy.metadata.$filtering.json")
      val exPath = rootDir.resolve(s"sturdy.funcdefs.$filtering.json")
      new JSONStore(mdPath, exPath)
    }

    val exportHistogram = {
      store.wbbs.values.foldLeft(mutable.HashMap.empty[String, Int])((acc, el) => {
        el.ex.map(_.exportedAs.get).foreach(name => {
          acc.get(name) match
            case Some(n) => acc.update(name, n+1)
            case None => acc.put(name, 1)
        })
        acc
      })
    }

    val sizeBytesHistogram = {
      store.wbbs.values.foldLeft(mutable.HashMap.empty[String, Int])((acc, el) => {
          val num = el.md.sizeBytes
          val lower = (num / 1000) * 1000
          val upper = lower + 999
          val key = s"from $lower to $upper"
          acc.get(key) match
            case Some(n) => acc.update(key, n+1)
            case None => acc.put(key, 1)
          acc
      })
    }

    val instructionCountHistogram = {
      store.wbbs.values.foldLeft(mutable.HashMap.empty[String, Int])((acc, el) => {
        val num = el.md.instructionCount
        val lower = (num / 100) * 100
        val upper = lower + 99
        val key = s"from $lower to $upper"
        acc.get(key) match
          case Some(n) => acc.update(key, n+1)
          case None => acc.put(key, 1)
        acc
      })
    }

    println(store.wbbs.size)
    println(exportHistogram.toSeq.sortWith((x,y) => x._2 > y._2).take(50))
    println(s"Bytes: ${sizeBytesHistogram.toSeq.sortWith((x,y) => x._2 > y._2)}")
    println(s"Instructions: ${instructionCountHistogram.toSeq.sortWith((x,y) => x._2 > y._2)}")

//
//  def wasmbench2sturdyMd(json: JObject): JValue =
//    import org.json4s.JsonDSL.*
//
//    for {
//      (hash, md) <- json.obj
//      JObject(fields) <- md
//      JField("wasm_validate_no_extensions", v) <- fields
//      JField("wasm_validate", w) <- fields
//      JField("instruction_count", bi) <- fields
//      if v == JBool(true)
//      if w != JNull
//      if bi != JNull
//    } yield {
//      val z = JObject(JField("hash",hash))
//      fields.foldLeft[JObject](z)((acc, el) => el match {
//        case ("size_bytes", v) => acc ~ ("sizeBytes" -> v)
//        case ("instruction_count", v) => acc ~ ("instructionCount" -> v)
//        case ("producers", prod) =>
//          val langs: List[(String, JValue)] = for {
//            JObject(fields) <- prod \\ "language"
//            (lang, version) <- fields
//          } yield (lang, version)
//          val procs: List[(String, JValue)] = for {
//            JObject(fields) <- prod \\ "processed-by"
//            (lang, version) <- fields
//          } yield (lang, version)
//          acc ~ ("processors" -> procs) ~ ("languages" -> langs)
//        case ("possible_source_languages", v) => acc ~ ("inferredSourceLanguages" -> v)
//        case _ => acc
//      })
//    }
//
//  def prepareMetadata(source: Path) =
//
//    val inStream: InputStream = Files.newInputStream(source)
//
//    val json = parse(inStream).asInstanceOf[JObject]
//    val sturdyMd = wasmbench2sturdyMd(json) match {
//      case JArray(arr) => arr.map(o => {
//        ((o \ "hash").values.asInstanceOf[String], o)})
//      case JObject(obj) => obj.map(o => {
//        ((o._2 \\ "hash").values.asInstanceOf[String], o._2)
//      })
//    }
//    val newPath = Paths.get(source.toString.replaceAll("metadata\\.", "sturdy\\.metadata\\."))
//    val outStream = Files.newOutputStream(newPath)
//    val output = pretty(render(JObject(sturdyMd)))
//    outStream.write(output.getBytes())
//    outStream.flush(); outStream.close()
//    read[Map[String, Metadata]](output)
//
//  def extractFuncDefsScript() =
//    import org.json4s.native.Serialization
//    import org.json4s.native.Serialization.{read,write}
//    implicit val formats: Formats = Serialization.formats(ShortTypeHints(List(classOf[TypeDef], classOf[Label])))
//
//    val mdUri = this.getClass.getResource("/sturdy/language/wasm/wasmbench/sturdy.metadata.filtered.json").toURI
//    val mdPath = Path.of(mdUri)
//
//    val mdStream: InputStream = Files.newInputStream(mdPath)
//    val md = read[Map[String, Metadata]](mdStream)
//    var erroneous = List.empty[String]
//    var funcDefs: Map[String, List[FuncDef]] = Map.empty
//    md.values.filter(m => m.sizeBytes < MAXSIZE).foreach(x => {
//      val binaryHash = x.hash
//      val md = x
//      val binPath = WASMBench.mkBinPath(binaryHash, filtering)
//      try
//        funcDefs = funcDefs + (binaryHash -> FuncExtractor.extractFuncDefs(binPath, md, wasm2wat))
//      catch
//        case e => erroneous = binaryHash :: erroneous; println(binaryHash)
//    })
//    val funcDefsPath = Paths.get(mdPath.toString.replaceAll("metadata\\.filtered\\.json", "sturdy\\.funcdefs\\.filtered\\.json"))
//    val outStream = Files.newOutputStream(funcDefsPath)
//    val output = write(funcDefs)
//    outStream.write(output.getBytes())
//    println(erroneous)


object WASMBench:
  def mkBinPath(hash: String, filtering: Filtering): Path =
    val uri: URI = this.getClass.getResource(s"/sturdy/language/wasm/wasmbench/${filtering}/${hash}.wasm").toURI
    Path.of(uri)

enum OS:
  case Mac
  case Linux

  override def toString: String = this match {
    case Mac => "mac"
    case Linux => "ubuntu"
  }

enum Filtering:
  case Filtered
  case Unfiltered

  override def toString: String = this match {
    case Filtered => "filtered"
    case Unfiltered => "unfiltered"
  }