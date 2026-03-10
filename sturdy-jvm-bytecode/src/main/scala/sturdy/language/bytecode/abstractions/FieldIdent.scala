package sturdy.language.bytecode.abstractions

import org.opalj.br.{ArrayType, ByteType, ClassType, Field, FieldType}
import sturdy.values.Finite

// type to identify fields
case class FieldIdent(declaringClass: ClassType, name: String, fieldType: FieldType):
  // check whether this identifier matches a field
  def matchesField(field: Field): Boolean =
    // TODO: figure out how handle declaring class
    name == field.name && fieldType == field.fieldType

// definitions for frequently used fields
object FieldIdent:
  final val StringValue = FieldIdent(ClassType.String, "value", ArrayType(ByteType))

given Finite[FieldIdent] with {}

extension (field: Field)
  def getIdent: FieldIdent =
    FieldIdent(field.classFile.thisType, field.name, field.fieldType)
