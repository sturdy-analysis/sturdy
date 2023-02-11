package sturdy.language.jimple

import sturdy.data.{*, given}
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.symboltable.ConcreteSymbolTable
import sturdy.effect.failure.CFailure
import sturdy.effect.failure.Failure
import sturdy.values.integer.given
import sturdy.values.floating.given

// the concrete interpreter for jimple
object ConcreteInterpreter extends Interpreter:

  // concrete implementation of value types
  override type J[A] = NoJoin[A]
  override type VIntC = Int
  override type VLongC = Long
  override type VFloatC = Float
  override type VDoubleC = Double
  override type VNullC = Unit
  override type VStringC = String
  override type VClass = RuntimeUnit
  override type VObject =  Map[String,(Type,Value)]
  override type VRTU = RuntimeUnit

  // no need for top values in the concrete interpreter
  override def topInt: Int = throw new UnsupportedOperationException
  override def topDouble: Double = throw new UnsupportedOperationException
  override def topFloat: Float = throw new UnsupportedOperationException
  override def topLong: Long = throw new UnsupportedOperationException
  override def topNull: Unit = throw new UnsupportedOperationException
  override def topClass: VClass = throw new UnsupportedOperationException
  override def topObject: VObject = throw new UnsupportedOperationException
  override def topString: String = throw new UnsupportedOperationException

  // defintion of types needed for the arguments of an instance of the concrete interpreter
  type ClassTable = ConcreteSymbolTable[String, String, Container]
  type RunTimeTable = ConcreteSymbolTable[String, String, RuntimeUnit]
  type Environment = Map[Identifier, Value]

  // definition of an instance of the concrete interpreter that extends the generic interpreter
  // takes an inital environment, an initial class table and an inital runtime table as arguments
  class Instance(initEnvironment: Environment, initClassTable: ClassTable, initRunTimeTable: RunTimeTable) extends GenericInstance:

    // no joining needed in concrete interpreter
    override def jvV: NoJoin[Value] = implicitly
    override def jvUnit: NoJoin[Unit] = implicitly
    override def jvRTU: NoJoin[VRTU] = implicitly

    // instantiation of effect componenets
    val callFrame: ConcreteCallFrame[CallFrameData, Identifier, Value] =
      new ConcreteCallFrame[CallFrameData, Identifier, Value]((), initEnvironment)
    val classTable: ConcreteSymbolTable[String, String, Container] =
      initClassTable
    val runTimeTable: ConcreteSymbolTable[String, String, RuntimeUnit] =
      initRunTimeTable
    val globalsTable: ConcreteSymbolTable[String, Identifier, Value] =
      new ConcreteSymbolTable[String, Identifier, Value]
    val failure: CFailure = new CFailure
    private given Failure = failure

    /* concrete implementations of new value operations */
    // retrieving the VClass value to a given container from the runtime table
    given ClassOps[Container, RuntimeUnit] with
      def classValue(c: Container): VClass = runTimeTable.get(c.getID, c.getID).getOrElse(failure(ClassNotLoaded, c.getID))

    // objects are of type VObject, have a Type and have fields mapping from String to Value
    given ObjectOps[String, Value, VObject, Type] with
      // creating an object from the given arguments
      def makeObject(fs: Seq[(String, Value)], ot: Type): VObject =
        var obj: VObject = Map()
        for ((s,v) <- fs)
          obj = obj + (s -> (ot, v))
        obj

      // reading the field of an object from the map based on its name
      def lookupObjectField(obj: VObject, field: String): Value =
        obj.get(field).getOrElse(failure(UnboundField, field))(1)

      // updating the field in the object map with the given values
      def updateObjectField(obj: VObject, field: String, newVal: Value): VObject =
        obj.updated(field, (obj.get(field).getOrElse(failure(UnboundField, field))(0), newVal))

      // the null object is simply an empty map
      def nullObject: Value = Value.ObjectValue(Map())

    // turning a string into a VStringC
    given StringOps[String, VStringC] with
      def stringValue(s: String): VStringC = s

    // implementing the jimpleOps with Value, Type and NoJoin
    val jimpleOps: JimpleOps[Value, Type, NoJoin] = implicitly

  // a call to apply creates a new Instance with the given initial arguments
  def apply(initEnvironment: Environment, initClassTable: ClassTable, initRunTimeTable: RunTimeTable): Instance =
    new Instance(initEnvironment,initClassTable, initRunTimeTable)
