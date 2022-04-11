package sturdy.language.wasm.wasmbench

case class Metadata(hash: String,
                    sizeBytes: Int,
                    instructionCount: Int,
                    processors: Map[String, String],
                    languages: Map[String, String],
                    inferredSourceLanguages: List[String])

case class WASMBenchBinary(md: Metadata, ex: List[FuncDef])