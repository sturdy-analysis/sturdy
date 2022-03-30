package sturdy.language.wasm.wasmbench



case class Metadata(hash: String, 
                    sizeBytes: Int, 
                    instructionCount: Int, 
                    processors: Map[String, String], 
                    languages: Map[String, String], 
                    inferredSourceLanguages: List[String]) 

class MetadataBuilder(
                       var hash: String,
                       var sizeBytes: Int,
                       var instructionCount: Int,
                       var processors: Map[String, String],
                       var languages: Map[String, String],
                       var inferredSourceLanguages: List[String]):
  
  def whash(hash: String): Unit = this.hash = hash
  def wSizeBytes(sizeBytes: Int): Unit = this.sizeBytes = sizeBytes
  def wInstCount(instructionCount: Int): Unit = this.instructionCount = instructionCount
  def wProcs(processors: Map[String, String]): Unit = this.processors = processors
  def wLangs(languages: Map[String, String]): Unit = this.languages = languages
  def wSrcLang(inferredSourceLanguages: List[String]): Unit = this.inferredSourceLanguages = inferredSourceLanguages

  def build: Metadata =
    Metadata(hash, sizeBytes, instructionCount, processors, languages, inferredSourceLanguages)

object MetadataBuilder:
  def apply(): MetadataBuilder =
    new MetadataBuilder("", 0,0,Map.empty, Map.empty,List.empty)



