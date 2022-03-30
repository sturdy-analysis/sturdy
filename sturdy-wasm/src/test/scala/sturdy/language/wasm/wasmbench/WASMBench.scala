package sturdy.language.wasm.wasmbench

//import java.sql.{Connection, DriverManager}
import org.scalatest.flatspec.AnyFlatSpec
import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.JsonDSL.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.io.InputStream
import java.net.URI

import sys.process.*

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

import scala.jdk.StreamConverters.*

implicit val formats: Formats = DefaultFormats


class WASMBench extends AnyFlatSpec:

//  val dbUri = this.getClass.getResource("/sturdy/language/wasm/wasmbench/wasmbench.db").toURI;
//  val conn= DriverManager.getConnection(s"jdbc:sqlite:${Path.of(dbUri)}")

  val mdPath: Path = {
    val mdUri = this.getClass.getResource("/sturdy/language/wasm/wasmbench/metadata.filtered.json").toURI
//    val mdUri = this.getClass.getResource("/sturdy/language/wasm/wasmbench/sturdy.metadata.test.json").toURI
    Path.of(mdUri)
  }
//  val inStream: InputStream = Files.newInputStream(mdPath)
//  val md: JObject = parse(inStream).asInstanceOf[JObject]

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

  run()

  def jsonFold(json: JObject): JValue =
    for {
      (hash, md) <- json.obj
      JObject(fields) <- md
      JField("wasm_validate_no_extensions", v) <- fields
      if v == JBool(true)
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

  def langs(md: JArray): Set[String] =
    val langs: List[String] = for {
      JObject(fields) <- md
      JField("languages", kv) <- fields
      JObject(x) <- kv
      (lang, ver) <- x
    } yield lang
    langs.toSet

  def procs(md: JArray): Set[String] =
    val procs: List[String] = for {
      JObject(fields) <- md
      JField("processors", kv) <- fields
      JObject(x) <- kv
      (k, v) <- x
    } yield k
    procs.toSet

  def inferredLangs(md: JArray): Set[String] =
    val inferredLangs: List[String] = for {
      JObject(fields) <- md
      JField("inferredSourceLanguages", ja) <- fields
      JArray(arr) <- ja
      JString(s) <- arr
    } yield s
    inferredLangs.toSet

  def exports(bin: JObject): Set[String] =
    val typeDefRegex = "\\(type \\(;[0-9]+;\\).*".r
    val funcExportRegex = "\\(export \".*\" \\(func .*\\)\\)".r
    val binaryURI = {
      val binaryHash = (bin \ "hash").values
      this.getClass.getResource(s"/sturdy/language/wasm/wasmbench/${this.filtering}/${binaryHash}.wasm").toURI
    }
    val binaryPath = Path.of(binaryURI)
    val wat = s"$wasm2wat $binaryPath".!!


    val typeDefs = typeDefRegex
      .findAllMatchIn(wat)
      .map{ case Match(str) => str }
      .toSet
    val funcExports = funcExportRegex
      .findAllMatchIn(wat)
      .map{ case Match(str) => str }
      .toSet

    funcExports



  def run() =
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read,write}
    implicit val formats: Formats = Serialization.formats(ShortTypeHints(List(classOf[TypeDef], classOf[Label])))

    println(mdPath)
    val store = new JSONStore(mdPath, 10485760, true)
    var erroneous = List.empty[String]
    var funcDefs: Map[String, List[FuncDef]] = Map.empty
    store.load().foreach(x => {
      val binaryHash = x.hash
      val md = x
      val binPath = {
        val uri: URI = this.getClass.getResource(s"/sturdy/language/wasm/wasmbench/${this.filtering}/${binaryHash}.wasm").toURI
        Path.of(uri)
      }
      try
        funcDefs = funcDefs + (binaryHash -> FuncExtractor.extractFuncDefs(binPath, md, wasm2wat))
      catch
        case e => erroneous = binaryHash :: erroneous; println(binaryHash)
    })
    val funcDefsPath = Paths.get(mdPath.toString.replaceAll("metadata\\.filtered\\.json", "sturdy\\.funcdefs\\.filtered\\.json"))
    val outStream = Files.newOutputStream(funcDefsPath)
    val output = write(funcDefs)
    outStream.write(output.getBytes())
    println(erroneous)


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