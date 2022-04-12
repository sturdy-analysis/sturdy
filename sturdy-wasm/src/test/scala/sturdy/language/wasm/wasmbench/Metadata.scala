package sturdy.language.wasm.wasmbench

case class File(collectionMethod: String, absolutePath: String)

case class Metadata(hash: String,
                    files: List[File],
                    sizeBytes: Int,
                    instructionCount: Int,
                    processors: Map[String, String],
                    languages: Map[String, String],
                    inferredSourceLanguages: List[String])

case class WASMBenchBinary(md: Metadata, ex: List[FuncDef])