package sturdy.language.wasm.wasmbench

case class File(collectionMethod: String, absolutePath: String)

case class Metadata(hash: String,
                    files: List[File],
                    sizeBytes: Int,
                    instructionCount: Int,
                    processors: Map[String, String],
                    languages: Map[String, String],
                    inferredSourceLanguages: List[String])

case class WASMBenchBinary(md: Metadata, ex: List[FuncDef]) extends WASMBenchBinaryToSql

object Teest extends App :
  val md = Metadata(
    "hash: String",
    List(File("a", "b")),
    1,
    1,
    Map.empty,
    Map.empty,
    List.empty)
  val ex = List(FuncDef(
    Label("test"),
    TypeDef(
      Label("test"),
      List(WASMType.I32),
      List(WASMType.I32)
    ),
    Some("main")
  ))
  val a = WASMBenchBinary(md, ex)
  println(a.toSqlStm)