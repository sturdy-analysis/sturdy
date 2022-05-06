package sturdy.language.wasm.analyses

import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.generic.{FixIn, FixOut, FrameData}


import sturdy.data.{*, given}
import sturdy.effect.{EffectStack, AnalysisState}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinableConcreteCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableConcreteOperandStack, given}
import sturdy.effect.symboltable.{JoinableConcreteSymbolTable, ConstantSymbolTable}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.{Interpreter, ConcreteInterpreter}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.values.floating.FloatOps
import swam.syntax.*
import swam.FuncType
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}



class ConstantAnalysisSturdyInstance(rootFrameData: FrameData, rootFrameValues: Iterable[ConstantAnalysis.Value], config: WasmConfig)
  extends ConstantAnalysis.Instance(rootFrameData, rootFrameValues):
  
  override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[ConstantAnalysis.Value]] = new fix.ContextualFixpoint {
    override type Ctx = config.ctx.Ctx
    val (contextPreparation, sensitivity) = config.ctx.make[ConstantAnalysis.Value]
    import config.ctx.finiteCtx
    override protected def contextFree = contextPreparation
    override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
    override protected def contextSensitive = config.fix.get(using analysisState, effectStack)
  }

  override val fixpointSuper = fixpoint
  override def toString: String = s"constant $config"
  
