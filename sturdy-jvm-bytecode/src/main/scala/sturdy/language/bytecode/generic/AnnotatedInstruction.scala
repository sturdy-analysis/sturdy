package sturdy.language.bytecode.generic

import org.opalj.br.instructions.*


enum op:
  case undefinedOP
  case unOP
  case binOP
class AnnotatedInstruction(inst: Instruction) {
  val instruction: Instruction = inst
  var annoOP: op = op.undefinedOP

  inst match{
    case inst: BIPUSH => annoOP = op.unOP
    case inst: IADD.type => annoOP = op.binOP
  }




}
