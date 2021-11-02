package sturdy.report

val AnalysisProperty = "analysis"
val ProgramProperty = "program"
val ResultValueProperty = "result value"

class Properties:
  private var properties: Map[String, String] = Map()
  def +=(kv: (String, Any)): Unit =
    properties += kv._1 -> kv._2.toString
  def ++=(kvs: Map[String, Any]): Unit =
    kvs.foreach(this += _)

def logProperty(kv: (String, Any))(using props: Properties): Unit =
  props += kv
def logProperties(kvs: Map[String, Any])(using props: Properties): Unit =
  kvs.foreach(props += _)