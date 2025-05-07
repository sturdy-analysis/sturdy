package sturdy.language.wasm.wasmbench


import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.scalatest.flatspec.AnyFlatSpec

import java.io.InputStream
import java.net.URI
import java.nio.file.{Files, Path, StandardOpenOption}
import scala.collection.mutable
import scala.jdk.StreamConverters.*

/*

This class serves as way to run various scripts and query the metadata and test results

It includes
- the procedure to process the WasmBench metadata file
- the procedure to extract all the binaries' exported functions
  extraction logic in class FuncExtractor
-
*/

class WASMBench extends AnyFlatSpec:
  import RunnerConfig.default.{filtering, rootDir}

  val MAXSIZE: Int = 10485760

  val os: OS = {
    System.getProperty("os.name").toLowerCase() match {
      case "linux" => OS.Linux
      case s if s.startsWith("mac") => OS.Mac
      case s => println(s); ???
    }
  }
  val wasm2wat: Path = {
    val u: URI = this.getClass.getResource(s"/sturdy/language/wasm/wasmbench/wasm2wat_${os}").toURI
    Path.of(u)
  }
    
  // adjust argument accordingly
//  printBinariesFromJacarteInResult(saveResultsToDir.resolve("Constant.topmost-calls(1).results.csv"))


//  val mdPath: Path = rootDir.resolve(s"metadata.$filtering.json")
//  val output: Path = rootDir.resolve(s"sturdy.metadata.$filtering.json")
//  prepareMetadata(mdPath, output)
  
  
  extractFuncDefsScript()
//  metadataScripts()

  def printBinariesFromJacarteInResult(pathToResultCSV: Path) = {
    // Procedure to query metadata and results

    println(s"\nBinaries from Jacarte in ${pathToResultCSV.toString}")

    val resStore = mkResultStore(pathToResultCSV)
    val hashes = resStore.retrieve(_ => true).map(r => r.hash.split('.')(0))

    val store = mkMdStore
    val export_start = store.retrieve(wb => {
      wb match
        case WASMBenchBinary(md, ex) =>
          ex.exists {
            case FuncDef(_, _, Some("_start")) => true
            case _ => false
          }
        case _ => false
    })
    val ofWhichJacarte = export_start.filter(p => {
      p.md.files.exists(p => {
        p.absolutePath.contains("Jacarte/CROW_tmp/rosetta")
      })
    })

    println(s"${export_start.size} binaries export \"_start\", of which ${ofWhichJacarte.size} are from the \"Jacarte/CROW_tmp/rosetta\" repo.")
    println(s"Share of \"Jacarte/CROW_tmp/rosetta\" binaries in all binaries that export \"_start\": ${ofWhichJacarte.size.toFloat / export_start.size}")

    val ofWhichPassed = ofWhichJacarte.filter(p => {
      hashes.contains(p.md.hash)
    })

    val exportPolybench = ofWhichPassed.filter(p => {
      val a = p.ex.exists{
        case FuncDef(_, _, Some("polybench_prepare_instruments")) => true
        case _ => false
      }
      val b = p.ex.exists{
        case FuncDef(_,_,Some("malloc")) => true
        case _ => false
      }
      a && b
    })

    val nonJacarteResults = resStore.retrieve(r => {
      !ofWhichPassed.exists{
        case WASMBenchBinary(Metadata(hash,_,_,_,_,_,_), _) => hash == r.hash.split('.')(0)
        case _ => false
      }
    })

    println(s"number of successfully tested binaries: ${hashes.size}, of which are from Jacarte/CROW_tmp/: ${ofWhichPassed.size}")
    println(s"of which export \"polybench_prepare_instruments\" and \"malloc\": ${exportPolybench.size}")
    println("\nnon Jacarte/CROW_tmp/ binaries:")
    for {
      r <- nonJacarteResults
    } do {
      println(s"${r.hash}")
    }

  }

  def errorHistogram = {
    val errStore = {
      val p = rootDir.resolve("Constant.topmost-calls(1).exceptions.csv")
      new ErrorStore(p)
    }

    val histogram = errStore.wbbs.foldLeft(mutable.HashMap.empty[String, Int])((acc, el) => {
      acc.get(el._2) match
        case Some(n) => acc.update(el._2, n+1)
        case None => acc.put(el._2, 1)
      acc
    })

    val logger = new FileLogger(rootDir.resolve("TaintTest.exceptions.histogram.csv"), StandardOpenOption.CREATE, true)
    logger.log("errMsg;count")

    for {
      (k,v) <- histogram
    } do logger.log(s"$k;$v")
    println(histogram)
  }

  def mkMdStore: Store[String, WASMBenchBinary] =
    import org.json4s.native.Serialization

    implicit val formats: Formats =
      Serialization.formats(ShortTypeHints(List(classOf[TypeDef])))
        + new WASMTypeSerializer
        + new LabelSerializer

    val store: Store[String, WASMBenchBinary] = {
      val mdPath = rootDir.resolve(s"sturdy.metadata.$filtering.json")
      val exPath = rootDir.resolve(s"sturdy.funcdefs.$filtering.json")
      new JSONStore(mdPath, exPath)
    }
    store

  def mkResultStore(path: Path): Store[String, Result] =
    new ResultStore[Result](path)

  def metadataScripts() =
    import org.json4s.native.Serialization
    implicit val formats: Formats =
      Serialization.formats(ShortTypeHints(List(classOf[TypeDef])))
        + new WASMTypeSerializer
        + new LabelSerializer

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

    val logger = new FileLogger(rootDir.resolve("exports.histogram.csv"), StandardOpenOption.CREATE, true)
    logger.log("funName;count")
    for {
      (k,v) <- exportHistogram
    } do {
      logger.log(s"$k;$v")
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

    val repoHistogram = {
      store.wbbs.values.foldLeft(mutable.HashMap.empty[String, Int])((acc, el) => {
        val file = el.md.files.head
        val repoRegex = "(?<=wasm-study/github/clone/repos/).+?/.+?(?=/)".r
        if (file.collectionMethod == "github") then
          val key = repoRegex
            .findFirstIn(file.absolutePath)
            .get
          acc.get(key) match
            case Some(n) => acc.update(key, n+1)
            case None => acc.put(key,1)
        acc
      })
    }

    println(store.wbbs.size)
    println(exportHistogram.toSeq.sortWith((x,y) => x._2 > y._2).take(50))
    println(s"Bytes: ${sizeBytesHistogram.toSeq.sortWith((x,y) => x._2 > y._2)}")
    println(s"Instructions: ${instructionCountHistogram.toSeq.sortWith((x,y) => x._2 > y._2)}")
    println(s"Repos: ${repoHistogram.toSeq.sortWith((x,y) => x._2 > y._2)}")

  def filterWasmBenchMd(json: JObject): JValue =
    import org.json4s.JsonDSL.*

    // exclude contains hashes of binaries that don't halt or consume too much memory during testing
    val exclude = List(
      "b022a54c3b5546fd09f00e6cb6ed12d04530298cef64182db9e12b8d9b4e4737",
      "681460c7ceeb6c96f37934f7b2d216dff3e7aa3e02f3325d5417113b607b1c03"
    )

    for {
      (hash, md) <- json.obj
      JObject(fields) <- md
      JField("wasm_validate_no_extensions", v) <- fields
      JField("wasm_validate", w) <- fields
      JField("instruction_count", bi) <- fields
      if !exclude.contains(hash)
      if v == JBool(true)
      if w != JNull
      if bi != JNull
      if bi != JNothing
    } yield {
      val z = JObject(JField("hash",hash))
      fields.foldLeft[JObject](z)((acc, el) => el match {
        case ("size_bytes", v) => acc ~ ("sizeBytes" -> v)
        case ("instruction_count", v) => acc ~ ("instructionCount" -> v)
        case ("files", v) =>
          val arr = v.asInstanceOf[JArray].arr
          val files: List[JValue] =
            arr.map{
              case JObject(obj) =>
                obj.foldLeft[List[(String, JValue)]](List.empty)((a, e) => e match {
                  case ("absolute_path", v) => ("absolutePath", v)::a
                  case ("collection_method",v) => ("collectionMethod",v)::a
                  case _ => a
                })
            }
          acc ~ ("files", files)
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

  def prepareMetadata(source: Path, out: Path) =
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.read

    implicit val formats: Formats = DefaultFormats

    val inStream: InputStream = Files.newInputStream(source)

    val json = parse(inStream).asInstanceOf[JObject]
    val sturdyMd = filterWasmBenchMd(json) match {
      case JArray(arr) => arr.map(o => {
        ((o \ "hash").values.asInstanceOf[String], o)})
      case JObject(obj) => obj.map(o => {
        ((o._2 \\ "hash").values.asInstanceOf[String], o._2)
      })
    }
    val outStream = Files.newOutputStream(out)
    val output = pretty(render(JObject(sturdyMd)))
    outStream.write(output.getBytes())
    outStream.flush(); outStream.close()
    read[Map[String, Metadata]](output)

  def extractFuncDefsScript() =

    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{read, write}
    implicit val formats: Formats =
      Serialization.formats(ShortTypeHints(List(classOf[TypeDef])))
        + new WASMTypeSerializer
        + new LabelSerializer

    val mdPath = rootDir.resolve("sturdy.metadata.filtered.json")

    val mdStream: InputStream = Files.newInputStream(mdPath)
    val md = read[Map[String, Metadata]](mdStream)
    var erroneous = List.empty[String]
    val logger =
      new FileLogger(
        rootDir.resolve("sturdy.funcdefs.filtered.json"),
        StandardOpenOption.CREATE,
        true,
        "")
    logger.log("{\n")

    val op: (() => Unit) => Metadata => Unit = sepFun => md => {
      val binaryHash = md.hash
      val binPath = WASMBench.mkBinPath(binaryHash, filtering)
      try
        val funcDefs = FuncExtractor.extractFuncDefs(binPath, md, wasm2wat)
        sepFun()
        val serialized = write(funcDefs)
        logger.log(s"\"${binaryHash}\":${serialized}")
      catch
        case e => erroneous = binaryHash :: erroneous; println(binaryHash)
    }
    op(() => ())(md.values.head)
    md.values.drop(1).foreach(op(() => logger.log(",\n")))

    logger.log("}\n")
    println(erroneous)

object WASMBench:
  import RunnerConfig.default.{filtering, rootDir}
  def mkBinPath(hash: String, filtering: Filtering): Path =
    val uri: URI = this.getClass.getResource(s"/sturdy/language/wasm/wasmbench/${filtering}/${hash}.wasm").toURI
    Path.of(uri)

  def mkMdStore: Store[String, WASMBenchBinary] =
    import org.json4s.native.Serialization

    implicit val formats: Formats =
      Serialization.formats(ShortTypeHints(List(classOf[TypeDef])))
        + new WASMTypeSerializer
        + new LabelSerializer

    val store: Store[String, WASMBenchBinary] = {
      val mdPath = rootDir.resolve(s"sturdy.metadata.$filtering.json")
      val exPath = rootDir.resolve(s"sturdy.funcdefs.$filtering.json")
      new JSONStore(mdPath, exPath)
    }
    store

  def mkResultStore(path: Path): Store[String, Result] =
    new ResultStore[Result](path)

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