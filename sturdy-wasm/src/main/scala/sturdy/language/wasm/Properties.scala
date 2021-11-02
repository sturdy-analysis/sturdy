package sturdy.language.wasm

val ModuleCountProperty = "modules"

def FunctionCountProperty = "functions"
val TableCountProperty = "tables"

/** Instructions */
val InstructionCountProperty = "instructions"
val DeadInstructionCountProperty = "dead-instructions"
def deadInstructionCountProperty(instName: String) = s"dead-instructions-$instName"
val ConstantInstructionCountProperty = "constant-instructions"

/** Labels */
val LabelCountProperty = "labels"
val DeadLabelCountProperty = "dead-labels"
def deadLabelCountProperty(instName: String) = s"dead-labels-$instName"
