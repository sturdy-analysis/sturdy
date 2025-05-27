package sturdy.language.wasm.wasmbench

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.sql.{Connection, DriverManager, ResultSet}
import scala.util.{Failure, Success, Try, Using}
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read
import org.json4s.ShortTypeHints
import org.sqlite.{SQLiteErrorCode, SQLiteException}
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.abstractions.{CfgConfig, ControlFlow}
import sturdy.language.wasm.analyses.{CallSites, ConstantTaintAnalysis, FixpointConfig, WasmConfig}
import sturdy.language.wasm.generic.FrameData

// Accesses Metadata from disk
class SQLStore(dbPath: Path) extends Store[String, WASMBenchBinary] :
  private val conn: Connection = DriverManager.getConnection(s"jdbc:sqlite:${dbPath.toAbsolutePath}")
  conn.setAutoCommit(false)

  val wbbs: Map[String, WASMBenchBinary] = runQuery("SELECT * FROM metadata;")

  def runQuery(query: String): Map[String, WASMBenchBinary] =
    implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[TypeDef])))
      + new WASMTypeSerializer
      + new LabelSerializer

    selectIter(
      md_rs =>
        val hash = md_rs.getString(1);
        val files: Iterator[File] = selectIter(
          file_rs =>
            File(
              file_rs.getString(2),
              file_rs.getString(3)
            )
        )(s"SELECT * FROM files WHERE hash = '$hash'");
        val funcDefs: Iterator[FuncDef] = selectIter(
          fd_rs =>
            FuncDef(
              Label(fd_rs.getString(1), false),
              TypeDef(
                Label(fd_rs.getString(3)),
                read(fd_rs.getString(4)),
                read(fd_rs.getString(5))
              ),
              Some(fd_rs.getString(2))
            )
        )(
          s"""
             |SELECT e.label AS '1', e.exportedAs as '2', ts.label as '3', ts.param as '4', ts.result as '5'
             |FROM exports e
             |INNER JOIN typeSignatures ts ON e.sigId = ts.id
             |WHERE e.hash = '$hash';""".stripMargin);
        val md = Metadata(
          hash,
          files.toList,
          md_rs.getInt(2),
          md_rs.getInt(3),
          read(md_rs.getString(4)),
          read(md_rs.getString(5)),
          read(md_rs.getString(6))
        )
        (hash, WASMBenchBinary(md, funcDefs.toList))
    )(query).toMap

  override def retrieve(key: String): Option[WASMBenchBinary] =
    runQuery(s"SELECT * FROM metadata WHERE hash='${key}';")
      .get(key)

  override def retrieve(keys: List[String]): List[WASMBenchBinary] =
    runQuery(
      s"""SELECT * FROM metadata
         |WHERE hash IN (${keys.mkString("'", "','", "'")});""".stripMargin)
      .values.toList

  def store(data: List[WASMBenchBinary]): Unit =
    data.foreach(wb => {
      store(wb)
    })

  def store(data: WASMBenchBinary): Unit =
    val statement = conn.createStatement()

    lazy val cmp: Unit = {
      for (stm <- data.toSqlStm)
        statement.executeUpdate(stm)
//      statement.executeBatch();
    }
    Try(cmp) match
      case Success(value) => conn.commit(); statement.close()
      case Failure(exception: SQLiteException)
        if exception.getResultCode != SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY =>
        conn.rollback();
        statement.close();
        println(s"hash: ${data.md.hash}\nerror: ${exception}\nsql:\n${data.toSqlStm}\n")
      case Failure(exception) =>
        conn.rollback();
        statement.close();


  private def selectIter[A](nextFun: ResultSet => A)(sql: String): Iterator[A] =
    val stm = conn.createStatement()
    val rs = stm.executeQuery(sql)
    conn.commit()
    new Iterator[A] {
      def hasNext = rs.next()

      def next() = nextFun(rs)
    }


sealed trait ToSql:
  def toSqlStm: List[String]

trait WASMBenchBinaryToSql extends ToSql {
  self: WASMBenchBinary =>
  override def toSqlStm: List[String] =
    import org.json4s.native.Serialization.write
    implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[TypeDef])))
      + new WASMTypeSerializer()
      + new LabelSerializer()

    val mdSql =
      s"""INSERT INTO metadata VALUES (
         |'${md.hash}',
         |${md.sizeBytes},
         |${md.instructionCount},
         |'${write(md.processors)}',
         |'${write(md.languages)}',
         |'${write(md.inferredSourceLanguages)}');
         |""".stripMargin

    val res = {
      mdSql
        :: md.files.map(file => {
        s"""INSERT INTO files VALUES (
           |'${md.hash}',
           |'${file.absolutePath.replace("'", "(single quote)")}',
           |'${file.collectionMethod}');
           |""".stripMargin
      })
        ::: ex.map(fd => {
        val label = fd.sig.label
        val paramSerialized = write(fd.sig.param)
        val resultSerialized = write(fd.sig.result)

        s"""
           |PRAGMA foreign_keys = ON; PRAGMA temp_store = 2;
           |CREATE TEMP TABLE _Variables(Key TEXT, Value INTEGER);
           |INSERT INTO typeSignatures VALUES (
           |  NULL,
           |  '${label.getString}',
           |  '${paramSerialized}',
           |  '${resultSerialized}')
           |ON CONFLICT DO NOTHING;
           |WITH typeSig(key, val) AS (SELECT 'sigId', id FROM typeSignatures WHERE
           |  param == '${paramSerialized}' AND
           |  result == '${resultSerialized}'
           |)
           |INSERT OR REPLACE INTO _Variables(Key, Value) SELECT key, val FROM typeSig;
           |INSERT INTO exports VALUES (
           |  '${md.hash}',
           |  '${fd.exportedAs.get}',
           |  '${fd.label}',
           |  (SELECT Value FROM _Variables WHERE Key = 'sigId'));
           |DROP TABLE IF EXISTS _Variables;
           |""".stripMargin
      })
    }
    res
}

object SQLStore:
  def reset(dbPath: Path): Unit =
    val conn: Connection = DriverManager.getConnection(s"jdbc:sqlite:${dbPath.toAbsolutePath}")
    conn.setAutoCommit(false)
    val stm = conn.createStatement()
    val sql = Path.of(this.getClass.getResource("/sturdy/language/wasm/wasmbench/metadata_schema.sql").toURI)
    try
      stm.executeUpdate(Files.readString(sql))
      conn.commit()
    catch
      case _ => conn.rollback()
    finally
      stm.close()
      conn.close()




object SQLscript extends App:
  val dbPath = Paths.get(
    "/home/code/thesis/sturdy.scala/sturdy-wasm/src/test/" +
      "resources/sturdy/language/wasm/wasmbench/metadata.db")
//  SQLStore.reset(dbPath)

//  val store = WASMBench.mkMdStore
  val logger =
    new FileLogger(
      Paths.get("/home/code/thesis/wasmbench/swam_exceptions.csv"),
      StandardOpenOption.APPEND,
      true
    )
  logger.log("hash;exception;absolutePath;sizeBytes")
  val sqlStore =
    new SQLStore(dbPath)
  var counter = 3255
  sqlStore.runQuery("SELECT * FROM metadata ORDER BY sizeBytes;")
    .toSeq.sortWith(_._2.md.sizeBytes < _._2.md.sizeBytes)
    .drop(counter)
    .foreach(kv => {
    counter += 1;
    val binPath = WASMBench.mkBinPath(kv._1, Filtering.Filtered)
    Try {
      println(kv._1)
      Parsing.fromBinary(binPath)
    } match {
      case Failure(exception) =>
        println(s"${counter}. error.")
        logger.log(s"${kv._1};${exception.getMessage};${kv._2.md.files(0).collectionMethod};${kv._2.md.sizeBytes}")
      case Success(value) => println(s"${counter}, success.")
    }
  })



//  println(module.funcs(0))
//  val config = WasmConfig(ctx = CallSites(1), fix = FixpointConfig(iter = sturdy.fix.iter.Config.Outermost()))
//  val interp = new ConstantTaintAnalysis.Instance(config)
//  val cfg = ConstantTaintAnalysis.controlFlow(CfgConfig.AllNodes(false), interp)
//  println(ControlFlow.)