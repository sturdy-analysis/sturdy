package sturdy.language.bytecode.generic

import org.opalj.bi.ACC_STATIC
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.*
import org.opalj.br.*
import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin, noJoin}
import sturdy.effect.allocation.Allocator
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.store.Store
import sturdy.effect.symboltable.DecidableSymbolTable
import sturdy.effect.{EffectList, EffectStack}
import sturdy.fix
import sturdy.language.bytecode.abstractions.{ArrayOpContext, FieldAccessContext, FieldIdent, InvokeContext, InvokeType, Site, getIdent}
import sturdy.language.bytecode.generic.FixIn.Eval
import sturdy.language.bytecode.util.ClassTypeValues
import sturdy.language.bytecode.{accessControl, resolveClass, resolveField}
import sturdy.values.MaybeChanged.Unchanged
import sturdy.values.arrays.ArrayOps
import sturdy.values.booleans.BooleanBranching
import sturdy.values.convert.NilCC
import sturdy.values.objects.ObjectOps
import sturdy.values.{Finite, Join, MaybeChanged, Widen}

import java.net.URL
import scala.annotation.tailrec
import scala.collection.immutable.ArraySeq

enum JvmExcept[V]:
  case Jump(pc: Int)
  case Ret(pc: V)
  case Return(returnValue: V)
  case ThrowObject(exception: V)

// throw an exception given its class and a site
type ThrowClass = Site => ClassType => Nothing

enum FixIn:
  case Eval(inst: Instruction, mth: Method, pc: Int)
  case Jump(pc: Int, mth: Method)

enum FixOut:
  case Eval()
  case Jump()

given Join[FixOut] with
  override def apply(v1: FixOut, v2: FixOut): MaybeChanged[FixOut] = Unchanged(v1)

given Finite[FixIn] with {}

given Finite[FixOut] with {}

trait GenericInterpreter[V, Addr, ObjType, ObjRep, TypeRep, ExcV, J[_] <: MayJoin[_]]:
  val fixpoint: fix.Fixpoint[FixIn, FixOut]
  val fixpointSuper: fix.Fixpoint[FixIn, FixOut]
  private type Fixed = FixIn => FixOut

  val bytecodeOps: BytecodeOps[V, ReferenceType]

  import bytecodeOps.*

  val objectOps: ObjectOps[FieldIdent, Addr, V, ClassFile, V, Site, Method, String, MethodDescriptor, V, InvokeContext, FieldAccessContext, J]
  val arrayOps: ArrayOps[Addr, V, V, V, ArrayType, Site, ArrayOpContext, J]

  implicit val joinUnit: J[Unit]
  implicit val jvV: J[V]
  implicit val joinAddr: J[Addr]

  implicit val failure: Failure
  implicit val except: Except[JvmExcept[V], ExcV, J]
  val stack: DecidableOperandStack[V]
  val objAlloc: Allocator[Addr, Site]
  val objFieldAlloc: Allocator[Addr, Site]
  val arrayAlloc: Allocator[Addr, Site]
  val arrayValAlloc: Allocator[Addr, Site]
  val staticAlloc: Allocator[Addr, Site]
  val store: Store[Addr, V, J]
  type FrameData = Int
  val frame: DecidableMutableCallFrame[FrameData, Int, V, Site]

  // result of the initialization
  enum InitializationResult:
    // initialization is currently running
    case Ongoing
    // initialization was successful
    case Success
    // class is marked as erroneous
    case Failure(/* TODO: hold the exception that must have been thrown */)

  given Join[InitializationResult] with
    override def apply(v1: InitializationResult, v2: InitializationResult): MaybeChanged[InitializationResult] =
      ??? // TODO

  given Widen[InitializationResult] with
    override def apply(v1: InitializationResult, v2: InitializationResult): MaybeChanged[InitializationResult] =
      ??? // TODO

  // holds the initialization results of classes
  val classInitializationState: DecidableSymbolTable[Unit, ClassType, InitializationResult]
  // holds the static fields of classes
  // may only be accessed after successful initialization
  val staticFieldTable: DecidableSymbolTable[Unit, FieldIdent, Addr]

  // needs to be lazy due to the initialization order, do not use the companion object here!
  implicit val effectStack: EffectStack = new EffectStack(EffectList(stack, failure, except, objFieldAlloc, objAlloc, arrayValAlloc, arrayAlloc, store, frame, staticFieldTable))

  implicit val project: Project[URL]

  private def fail(k: FailureKind, what: String) = failure.fail(k, what)

  private lazy val num = GenericInterpreterNumerics[V, ReferenceType](bytecodeOps)
  private lazy val native = GenericInterpreterNativeMethods[V, Addr, ObjType, Addr, Addr, ObjRep, TypeRep, ExcV, InvokeContext, J](this)

  def eval(inst: Instruction, mth: Method, pc: Int)(using Fixed): Unit =
    val site = Site.Instruction(mth, pc)
    inst match
      // No Op opcode 0
      case NOP =>
        ()

      // push NULL on stack opcode 1
      case ACONST_NULL =>
        stack.push(objectOps.makeNull())

      // Lit Ops opcode 2 - 17
      case inst if 2 <= inst.opcode && inst.opcode <= 17 =>
        stack.push(num.evalNumericOp(inst))

      // LDC opcode 18
      case inst: LoadInt =>
        stack.push(num.evalNumericOp(inst))

      case inst: LoadFloat =>
        stack.push(num.evalNumericOp(inst))

      case LoadClass(_) =>
        val cls = createObject(ClassType.Class, Site.Instruction(mth, pc, variant = 1))
        stack.push(cls)

      case LoadString(value) =>
        stack.push(makeStringObj(site)(value))

      case LoadMethodHandle(_) =>
        ??? // TODO
      case LoadMethodType(_) =>
        ??? // TODO

      // LDC_W opcode 19
      case inst: LoadInt_W =>
        stack.push(num.evalNumericOp(inst))
      case inst: LoadFloat_W =>
        stack.push(num.evalNumericOp(inst))
      case LoadClass_W(_) =>
        val cls = createObject(ClassType.Class, Site.Instruction(mth, pc, variant = 1))
        stack.push(cls)
      case LoadString_W(value) =>
        stack.push(makeStringObj(site)(value))
      case LoadMethodHandle_W(_) =>
        ??? // TODO
      case LoadMethodType_W(_) =>
        ??? // TODO


      // LDC2_W opcode 20
      case inst: LoadLong =>
        stack.push(num.evalNumericOp(inst))
      case inst: LoadDouble =>
        stack.push(num.evalNumericOp(inst))
      case LoadDynamic2_W(_, _, _) =>
        ??? // TODO

      // load Local variable opcode 21 - 45
      case inst@LoadLocalVariableInstruction(_, lvIndex) =>
        val v = frame.getLocalOrElse(lvIndex, fail(BytecodeFailure.UnboundLocal, s" ${inst.toString} , ${lvIndex.toString}"))
        stack.push(v)

      // load from array (opcode 46 - 53)
      case IALOAD | LALOAD | FALOAD | DALOAD | AALOAD | BALOAD | CALOAD | SALOAD =>
        val index = stack.popOrAbort()
        val arrayref = stack.popOrAbort()
        val v = arrayOps.getVal(site)(arrayref, index).getOrElse:
          throwClass(site)(ClassType.ArrayIndexOutOfBoundsException)
        stack.push(v)

      // store local variable opcode 54 - 78
      case inst@StoreLocalVariableInstruction(_, lvIndex) =>
        val v1 = stack.popOrAbort()
        frame.setLocalOrElse(lvIndex, v1, fail(BytecodeFailure.UnboundLocal, s" ${inst.toString} , ${lvIndex.toString}"))

      // store in array (opcode 79 - 86)
      case inst@(IASTORE | LASTORE | FASTORE | DASTORE | AASTORE | BASTORE | CASTORE | SASTORE) =>
        // truncate values if needed
        val value = inst match
          case BASTORE => convert_i32_i8(stack.popOrAbort(), NilCC)
          case CASTORE => convert_i32_u16(stack.popOrAbort(), NilCC)
          case SASTORE => convert_i32_i16(stack.popOrAbort(), NilCC)
          case _ => stack.popOrAbort()
        val index = stack.popOrAbort()
        val arrayref = stack.popOrAbort()
        arrayOps.setVal(site)(arrayref, index, value).getOrElse:
          throwClass(site)(ClassType.ArrayIndexOutOfBoundsException)

      // operand stack management instructions (opcodes 87 - 95)
      case POP =>
        stack.popOrAbort()

      case POP2 =>
        val v = stack.popOrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(v)) {
          stack.popOrAbort()
        } {}

      case DUP =>
        val value = stack.popOrAbort()
        stack.pushN(List(
          value, value
        ))

      case DUP_X1 =>
        val (value2, value1) = stack.pop2OrAbort()
        stack.pushN(List(
          value1, value2, value1
        ))

      case DUP_X2 =>
        val (value2, value1) = stack.pop2OrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(value2)) {
          // form 1
          val value3 = stack.popOrAbort() // must be 32bit, currently unchecked
          stack.pushN(List(
            value1, value3, value2, value1
          ))
        } {
          // form 2
          stack.pushN(List(
            value1, value2, value1
          ))
        }

      case DUP2 =>
        val value1 = stack.popOrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(value1)) {
          // form 2
          val value2 = stack.popOrAbort() // must be 32bit, currently unchecked
          stack.pushN(List(
            value2, value1, value2, value1
          ))
        } {
          // form 1
          stack.pushN(List(
            value1, value1
          ))
        }

      case DUP2_X1 =>
        val (value2, value1) = stack.pop2OrAbort() // value2 must be 32bit, currently unchecked
        branchOpsUnit.boolBranch(sizeOps.is32Bit(value1)) {
          // form 1
          val value3 = stack.popOrAbort() // must be 32bit, currently unchecked
          stack.pushN(List(
            value2, value1, value3, value2, value1
          ))
        } {
          // form 2
          stack.pushN(List(
            value1, value2, value1
          ))
        }

      case DUP2_X2 =>
        // 32bit ~ category 1 computational type
        // 64bit ~ category 2 computational type
        val (value2, value1) = stack.pop2OrAbort()
        branchOpsUnit.boolBranch(sizeOps.is32Bit(value1)) {
          // value1 32bit
          branchOpsUnit.boolBranch(sizeOps.is32Bit(value2)) {
            // value2 32bit
            val value3 = stack.popOrAbort()
            branchOpsUnit.boolBranch(sizeOps.is32Bit(value3)) {
              // value3 32bit
              // -> form 1
              val value4 = stack.popOrAbort() // must be 32bit, currently unchecked
              stack.pushN(List(
                value2, value1, value4, value3, value2, value1
              ))
            } {
              // value3 64bit
              // -> form 3
              stack.pushN(List(
                value2, value1, value3, value2, value1
              ))
            }
          } {
            // value2 64bit
            throw IllegalStateException("dup2_x2 called on illegal stack: value1 category 1, value2 category 2")
          }
        } {
          // value1 64bit
          branchOpsUnit.boolBranch(sizeOps.is32Bit(value2)) {
            // value2 32bit
            // -> form 2
            val value3 = stack.popOrAbort() // must be 32bit, currently unchecked
            stack.pushN(List(
              value1, value3, value2, value1
            ))
          } {
            // value2 64bit
            // -> form 4
            stack.pushN(List(
              value1, value2, value1
            ))
          }
        }

      case SWAP =>
        val (value2, value1) = stack.pop2OrAbort()
        stack.pushN(List(
          value1, value2
        ))

      // Arithmetic Ops opcode 96 - 115
      case inst if 96 <= inst.opcode && inst.opcode <= 115 =>
        val (v1, v2) = stack.pop2OrAbort()
        stack.push(num.evalNumericBinOp(() => throwClass(site)(ClassType.ArithmeticException))(inst, v1, v2))

      // Negation Ops opcode 116 - 119
      case inst if 116 <= inst.opcode && inst.opcode <= 119 =>
        val v1 = stack.popOrAbort()
        stack.push(num.evalNumericUnOp(inst, v1))

      // Bitshift Ops opcode 120 - 131
      case inst if 120 <= inst.opcode && inst.opcode <= 131 =>
        val (v1, v2) = stack.pop2OrAbort()
        stack.push(num.evalNumericBinOp(() => throwClass(site)(ClassType.ArithmeticException))(inst, v1, v2))

      // iinc opcode 132
      case inst@IINC(lvIndex, constValue) =>
        val toInc = frame.getLocalOrElse(lvIndex, fail(BytecodeFailure.UnboundLocal, s" ${inst.toString} , ${lvIndex.toString}"))
        frame.setLocalOrElse(lvIndex, i32ops.add(toInc, i32ops.integerLit(constValue)), fail(BytecodeFailure.UnboundLocal, s" ${inst.toString} , ${lvIndex.toString}"))

      // Conversions opcode 133 - 147
      case inst if 133 <= inst.opcode && inst.opcode <= 147 =>
        val v1 = stack.popOrAbort()
        stack.push(num.evalConvertOp(inst, v1))

      // Numeric Comparison opcode 148 - 152
      case inst if 148 <= inst.opcode && inst.opcode <= 152 =>
        val (v1, v2) = stack.pop2OrAbort()
        stack.push(num.evalNumericBinOp(() => throwClass(site)(ClassType.ArithmeticException))(inst, v1, v2))

      // Branching opcode 153 - 166
      case IFEQ(branchoffset) =>
        handleIfCondInst(eqOps.equ, pc + branchoffset)
      case IFNE(branchoffset) =>
        handleIfCondInst(eqOps.neq, pc + branchoffset)
      case IFLT(branchoffset) =>
        handleIfCondInst(compareOps.lt, pc + branchoffset)
      case IFGE(branchoffset) =>
        handleIfCondInst(compareOps.ge, pc + branchoffset)
      case IFGT(branchoffset) =>
        handleIfCondInst(compareOps.gt, pc + branchoffset)
      case IFLE(branchoffset) =>
        handleIfCondInst(compareOps.le, pc + branchoffset)
      case IF_ICMPEQ(branchoffset) =>
        handleIfCmpInst(eqOps.equ, pc + branchoffset)
      case IF_ICMPNE(branchoffset) =>
        handleIfCmpInst(eqOps.neq, pc + branchoffset)
      case IF_ICMPLT(branchoffset) =>
        handleIfCmpInst(compareOps.lt, pc + branchoffset)
      case IF_ICMPGE(branchoffset) =>
        handleIfCmpInst(compareOps.ge, pc + branchoffset)
      case IF_ICMPGT(branchoffset) =>
        handleIfCmpInst(compareOps.gt, pc + branchoffset)
      case IF_ICMPLE(branchoffset) =>
        handleIfCmpInst(compareOps.le, pc + branchoffset)
      case IF_ACMPEQ(branchoffset) =>
        handleIfCmpInst(eqOps.equ, pc + branchoffset)
      case IF_ACMPNE(branchoffset) =>
        handleIfCmpInst(eqOps.neq, pc + branchoffset)

      // JUMPS opcode 167 - 171
      case GOTO(branchoffset) =>
        except.throws(JvmExcept.Jump(pc + branchoffset))
      case JSR(_) =>
        throw UnsupportedOperationException("unsupported instruction: jsr")
      /*
      stack.push(i32ops.integerLit(pc))
      except.throws(JvmExcept.Jump(pc + inst.branchoffset))
      */
      case RET(_) =>
        throw UnsupportedOperationException("unsupported instruction: ret")
      /*
      val index = frame.getLocalOrElse(inst.lvIndex, fail(BytecodeFailure.UnboundLocal, s" ${inst.toString()} , ${inst.lvIndex.toString}"))
      except.throws(JvmExcept.Ret(index))
      */

      case TABLESWITCH(defaultOffset, low, high, jumpOffsets) =>
        val index = stack.popOrAbort()
        var target = defaultOffset
        branchOpsUnit.boolBranch(compareOps.lt(index, i32ops.integerLit(low))) {} {
          branchOpsUnit.boolBranch(compareOps.gt(index, i32ops.integerLit(high))) {} {
            // not greater than high or less than low
            val indexMap = jumpOffsets.zipWithIndex.map(pair => (i32ops.integerLit(pair._2), pair._1)).toMap
            target = indexMap(i32ops.sub(index, i32ops.integerLit(low)))
          }
        }
        except.throws(JvmExcept.Jump(pc + target))

      case LOOKUPSWITCH(defaultOffset, npairs) =>
        val key = stack.popOrAbort()
        val transformedIndices = npairs.map(pair => (i32ops.integerLit(pair.key), pair.value)).toMap
        val offset = transformedIndices.getOrElse(key, defaultOffset)
        except.throws(JvmExcept.Jump(pc + offset))

      // return instructions
      case IRETURN | LRETURN | FRETURN | DRETURN | ARETURN =>
        val returnValue = stack.popOrAbort()
        stack.clearCurrentOperandFrame()
        stack.push(returnValue)
        except.throws(JvmExcept.Return(returnValue))
      case RETURN =>
        stack.clearCurrentOperandFrame()
        except.throws(JvmExcept.Return(voidOps.voidRep))

      // Load and Store Statics opcode 178 - 179
      case GETSTATIC(declaringClass, name, fieldType) =>
        val ident = FieldIdent(declaringClass, name, fieldType)
        val addr = getStaticFieldAddr(mth.classFile.thisType, mth, site, ident)
        val v = store.readOrElse(addr, fail(BytecodeFailure.UnboundStaticVar, name))
        stack.push(v)

      case PUTSTATIC(declaringClass, name, fieldType) =>
        val ident = FieldIdent(declaringClass, name, fieldType)
        val field = project.classHierarchy.allSuperclassesIterator(declaringClass, true)(project).flatMap(cfs => cfs.fields).find:
          ident.matchesField
        .getOrElse:
          throwClass(site)(ClassTypeValues.NoSuchFieldError)
        runAccessControl(site)(field, mth)
        if field.isNotStatic then
          throwClass(site)(ClassTypeValues.IncompatibleClassChangeError)
        if field.isFinal && !(field.classFile == mth.classFile && mth.isStaticInitializer) then
          throwClass(site)(ClassTypeValues.IllegalAccessError)
        val addr = getStaticFieldAddr(mth.classFile.thisType, mth, site, ident)
        val v = stack.popOrAbort()
        store.write(addr, v)

      // Load and Store Fields opcode 180 - 181
      case GETFIELD(declaringClass, name, fieldType) =>
        val ident = FieldIdent(declaringClass, name, fieldType)
        val obj = stack.popOrAbort()
        // typeOps currently can't deal with nulls, so this needs to be checked first
        branchOpsUnit.boolBranch(objectOps.isNull(obj)) {} {
          if typeOps.typeOf(obj).isArrayType then
            throwClass(site)(ClassTypeValues.LinkageError)
        }
        val field = resolveField(mth.classFile.thisType, ident)(using project, except, throwClass(site))
        if field.isStatic then
          throwClass(site)(ClassTypeValues.IncompatibleClassChangeError)
        val v = objectOps.getField(site, mth.classFile)(obj, ident)
        stack.push(v)

      case PUTFIELD(declaringClass, name, fieldType) =>
        val ident = FieldIdent(declaringClass, name, fieldType)
        val value = stack.popOrAbort()
        val obj = stack.popOrAbort()
        val field = resolveField(mth.classFile.thisType, ident)(using project, except, throwClass(site))
        runAccessControl(site)(field, mth)
        if field.isStatic then
          throwClass(site)(ClassTypeValues.IncompatibleClassChangeError)
        if field.isFinal && !(field.classFile == mth.classFile && mth.isConstructor) then
          throwClass(site)(ClassTypeValues.IllegalAccessError)
        objectOps.setField(site, mth.classFile)(obj, ident, value).option(fail(BytecodeFailure.FieldNotFound, ident.toString))(identity)

      // Invoke Functions opcode 182 - 186
      case INVOKESTATIC(declaringClass, _, name, methodDescriptor) =>
        ensureInitialization(mth, site)(declaringClass)
        val cf = getClassFile(site)(declaringClass)
        val candidate = findMethod(site)(cf, name, methodDescriptor).get
        if candidate.isAbstract || candidate.isNotStatic then
          throwClass(site)(ClassTypeValues.IncompatibleClassChangeError)
        val numArgs = methodDescriptor.parametersCount
        val args = stack.popNOrAbort(numArgs)
        val ret = invoke(site)(candidate, args)
        if !methodDescriptor.returnType.isVoidType then
          stack.push(ret)

      case INVOKEVIRTUAL(declaringClass, name, methodDescriptor) =>
        val numArgs = methodDescriptor.parametersCount
        val args = stack.popNOrAbort(numArgs)
        val obj = stack.popOrAbort()
        val ret = objectOps.invokeMethod(site, InvokeType.Virtual, mth.classFile)(getClassFile(site)(declaringClass.mostPreciseClassType), name, methodDescriptor, obj, args)(invokeWrapper(site))
        if !methodDescriptor.returnType.isVoidType then
          stack.push(ret)

      case INVOKESPECIAL(declaringClass, isInterface, name, methodDescriptor) =>
        val numArgs = methodDescriptor.parametersCount
        val args = stack.popNOrAbort(numArgs)
        val obj = stack.popOrAbort()
        val ret = objectOps.invokeMethod(site, InvokeType.Special(isInterface), mth.classFile)(getClassFile(site)(declaringClass), name, methodDescriptor, obj, args)(invokeWrapper(site))
        if !methodDescriptor.returnType.isVoidType then
          stack.push(ret)

      case INVOKEINTERFACE(declaringClass, name, methodDescriptor) =>
        val numArgs = methodDescriptor.parametersCount
        val args = stack.popNOrAbort(numArgs)
        val obj = stack.popOrAbort()
        val ret = objectOps.invokeMethod(site, InvokeType.Interface, mth.classFile)(getClassFile(site)(declaringClass), name, methodDescriptor, obj, args)(invokeWrapper(site))
        if !methodDescriptor.returnType.isVoidType then
          stack.push(ret)

      case INVOKEDYNAMIC(_, _, _) =>
        throw UnsupportedOperationException("unsupported instruction: invokedynamic")
      /*
        // TODO: this is only implemented for methods named "makeConcatWithConstants"
        val (bootstrapMethod, name, _) = INVOKEDYNAMIC.unapply(inst).value
        val receiver = bootstrapMethod.handle
        receiver match
          case InvokeStaticMethodHandle(receiverType, _, name, methodDescriptor) =>
            if (name == "makeConcatWithConstants"){
              if(stack.size < 2){
                val test3 = bootstrapMethod.arguments.head.toJava
                val test4 = test3.drop(2).dropRight(1)
                eval(LoadString(test4), mth, pc)
              }

              val source = javaLibClassFileWrapper(receiverType.mostPreciseClassType)
              val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
              val invokedMth = cfs.findMethod(name, methodDescriptor).get
              val args = stack.popNOrAbort(2)
              evalNativeStatic(invokedMth, args)
            }
          case _ => ??? // TODO: not implemented
      */
      /*
      receiver match
        case receiver: InvokeStaticMethodHandle =>
          if (project.isLibraryType(receiver.receiverType.mostPreciseClassType)) {
            val mthTypeSource = javaLibClassFileWrapper(ClassType("java/lang/invoke/MethodType"))
            val mthTypeCFS: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, mthTypeSource).head
            val mthTypeMth = mthTypeCFS.findMethod("methodType", MethodDescriptor(ClassType.Class, ClassType("java/lang/invoke/MethodType")))
            stack.push(createNativeObj(inst.methodDescriptor.returnType.asClassType))
            stack.push(createNativeObj(inst.methodDescriptor.parameterType(0).asClassType))
            val mthTypeObj = invoke(mthTypeMth.get, true)

            val source = javaLibClassFileWrapper(receiver.receiverType.mostPreciseClassType)
            val cfs: ClassFile = org.opalj.br.reader.Java8Framework.ClassFile(nativeSource, source).head
            val mth = cfs.findMethod(receiver.name, receiver.methodDescriptor).get
            invoke(mth, true)

          }
          else{
            ???
          }
      */

      // NEW opcode 187
      case NEW(classType) =>
        checkAccessControlForRefType(site)(classType, mth)
        if project.classHierarchy.isInterface(classType).isYesOrUnknown || getClassFile(site)(classType).isAbstract then
          throwClass(site)(ClassTypeValues.InstantiationError)
        ensureInitialization(mth, site)(classType)
        stack.push(createObject(classType, site))

      // Arrays opcode 188 - 190
      case NEWARRAY(componentType) =>
        handleNewArray(componentType, site)

      case ANEWARRAY(componentType) =>
        checkAccessControlForRefType(site)(componentType, mth)
        handleNewArray(componentType, site)

      case ARRAYLENGTH =>
        val array = stack.popOrAbort()
        stack.push(arrayOps.arrayLength(site)(array))

      // athrow opcode 191
      case ATHROW =>
        val objectref = stack.popOrAbort()
        // if the exception is null, a NPE has to be thrown instead
        branchOpsUnit.boolBranch(objectOps.isNull(objectref)) {
          throwClass(site)(ClassType.NullPointerException)
        } {
          except.throws(JvmExcept.ThrowObject(objectref))
        }

      // checkcast opcode 192
      case CHECKCAST(referenceType) =>
        val objectref = stack.peekOrAbort()
        branchOpsUnit.boolBranch(objectOps.isNull(objectref)) {} {
          // not null
          // this performs access control, the type should be resolved by opal
          resolveClass(referenceType, mth.classFile.thisType)(using project.classHierarchy, project, except, throwClass(site))
          branchOpsUnit.boolBranch(typeOps.instanceOf(objectref, referenceType)) {} {
            throwClass(site)(ClassType.ClassCastException)
          }
        }

      // instanceof opcode 193
      case INSTANCEOF(referenceType) =>
        val objectref = stack.popOrAbort()
        branchOpsUnit.boolBranch(objectOps.isNull(objectref)) {
          stack.push(i32ops.integerLit(0))
        } {
          // this performs access control, the type should be resolved by opal
          resolveClass(referenceType, mth.classFile.thisType)(using project.classHierarchy, project, except, throwClass(site))
          // not null
          branchOpsUnit.boolBranch(typeOps.instanceOf(objectref, referenceType)) {
            stack.push(i32ops.integerLit(1))
          } {
            stack.push(i32ops.integerLit(0))
          }
        }

      // monitorenter opcode 194
      case MONITORENTER =>
        // TODO: why was this the implementation?
        // stack.popOrAbort()
        throw UnsupportedOperationException("unsupported instruction: monitorenter")

      // monitorexit opcode 195
      case MONITOREXIT =>
        // TODO: why was this the implementation?
        // stack.popOrAbort()
        throw UnsupportedOperationException("unsupported instruction: monitorexit")

      // WIDE opcode 196
      case WIDE =>
        // opal should handle everything when loading the class, so we need to do nothing
        ()

      // multianewarray opcode 197
      case MULTIANEWARRAY(arrayType, dimensions) =>
        checkAccessControlForRefType(site)(arrayType, mth)
        val dims = stack.popNOrAbort(dimensions)
        dims.foreach: dim =>
          branchOpsUnit.boolBranch(compareOps.lt(dim, i32ops.integerLit(0))) {
            throwClass(site)(ClassType.NegativeArraySizeException)
          } {}
        val arrayref = createMultiArray(arrayType, dims, site)
        stack.push(arrayref)

      // ifnull, ifnonnull opcode 198 - 199
      case IFNULL(branchoffset) =>
        handleIfInst(objectOps.isNull, pc + branchoffset)
      case IFNONNULL(branchoffset) =>
        val v = stack.popOrAbort()
        val flag = objectOps.isNull(v)
        branchOpsUnit.boolBranch(flag) {} {
          except.throws(JvmExcept.Jump(pc + branchoffset))
        }

      // goto_w opcode 200
      case GOTO_W(branchoffset) =>
        except.throws(JvmExcept.Jump(pc + branchoffset))

      // jsr_wt opcode 201
      case JSR_W(_) =>
        throw UnsupportedOperationException("unsupported instruction: jsr_w")
      /*
      stack.push(i32ops.integerLit(pc))
      except.throws(JvmExcept.Jump(pc + inst.branchoffset))
      */

      // breakpoint
      case inst if inst.opcode == 202 =>
        ()

  // returns the class file of a given type or throws an exception
  def getClassFile(site: Site)(classType: ClassType): ClassFile =
    project.classFile(classType).getOrElse:
      throwClass(site)(ClassTypeValues.NoClassDefFoundError)

  // ensures that the static initializer of a given class has been invoked
  // and its static fields have been added to the static address map and store
  def ensureInitialization(mth: Method, site: Site)(classType: ClassType)(using Fixed): Unit =
    try classInitializationState.get((), classType).option(initializeClass(mth, site)(classType)):
      case InitializationResult.Ongoing | InitializationResult.Success => ()
      case InitializationResult.Failure() => throwClass(site)(ClassTypeValues.NoClassDefFoundError)
    catch case _: NoSuchElementException =>
      // this should happen iff this is the first initialization of a class, initialize tables
      classInitializationState.putNew(())
      staticFieldTable.putNew(())
      initializeClass(mth, site)(classType)

  // expects unit to be present in the state and field tables
  def initializeClass(mth: Method, site: Site)(classType: ClassType)(using Fixed): Unit =
    // need to make sure the class is registered in the table to avoid exceptions
    classInitializationState.set((), classType, InitializationResult.Ongoing)
    val cf = getClassFile(site)(classType)
    // ensure initialization of superclass first
    cf.superclassType.foreach:
      ensureInitialization(mth, site)(_)
    // initialize all static fields to their default value
    cf.fields.filter: field =>
      ACC_STATIC.isSet(field.accessFlags)
    .foreach: field =>
      val ident = field.getIdent
      val addr = staticAlloc(Site.StaticInitialization(ident))
      staticFieldTable.set((), ident, addr)
      store.write(addr, fieldValue(site)(field))
    // add class as initialized for different calls to this function
    // not every class has a static initializer, need to only invoke it if it exists
    cf.staticInitializer.foreach: mth =>
      except.tryCatch {
        // static initializers are void
        val _ = invoke(site)(mth, Seq())
      } {
        case JvmExcept.ThrowObject(_) =>
          classInitializationState.set((), classType, InitializationResult.Failure())
          throwClass(site)(ClassType.ExceptionInInitializerError)
        case e => except.throws(e)
      }
    // if nothing was thrown, the initialization was successful
    classInitializationState.set((), classType, InitializationResult.Success)

  def createObject(classType: ClassType, site: Site): V =
    val cf = getClassFile(site)(classType)
    val inheritedFields = project.classHierarchy.allSuperclassesIterator(classType, true)(project).map(cfs => cfs.fields).toSeq.distinct
    val fields = inheritedFields.flatMap(buildFieldSeq(site))
    objectOps.makeObject(objAlloc(site), cf, fields)

  private def handleNewArray(componentType: FieldType, site: Site): Unit =
    val count = stack.popOrAbort()
    val arrayref = branchOpsV.boolBranch(compareOps.lt(count, i32ops.integerLit(0))) {
      throwClass(site)(ClassType.NegativeArraySizeException)
    } {
      createArray(count, componentType, site)
    }
    stack.push(arrayref)

  private def runAccessControl(site: Site)(e: Field | Method | ClassFile, mth: Method): Unit =
    if !accessControl(e, mth.classFile.thisType)(using project.classHierarchy) then
      throwClass(site)(ClassTypeValues.IllegalAccessError)

  private def checkAccessControlForRefType(site: Site)(refType: ReferenceType, mth: Method): Unit =
    runAccessControl(site)(getClassFile(site)(resolveClass(refType, mth.classFile.thisType)(using project.classHierarchy, project, except, throwClass(site))), mth)

  def createArray(size: V, componentType: FieldType, site: Site): V =
    val arrayVals = arrayOps.initArray(size)
    val convertedArrayVals = arrayVals.zipWithIndex.map: tuple =>
      (defaultValue(componentType), Site.ArrayElementInitialization(site, tuple._2))
    arrayOps.makeArray(arrayAlloc(site), convertedArrayVals, ArrayType(componentType), size)

  private def createMultiArray(arrayType: ArrayType, dims: List[V], site: Site): V =
    val (size, elementSupplier) = dims match
      case size :: Nil => (size, () => defaultValue(arrayType.componentType))
      case size :: xs => (size, () => createMultiArray(arrayType.componentType.asArrayType, xs, site))
      case Nil => throw IllegalStateException("dims.size must be >= 1 at all times")
    val initialArray = arrayOps.initArray(size)
    val filledArray = initialArray.zipWithIndex.map: tuple =>
      (elementSupplier(), Site.ArrayElementInitialization(site, tuple._2))
    arrayOps.makeArray(arrayAlloc(site), filledArray, arrayType, size)

  def invokeWrapper(site: Site)(obj: V, mth: Method, args: Seq[V])(using Fixed): V =
    invoke(site)(mth, obj +: args)

  // every invoke instruction will call this function
  def invoke(site: Site)(mth: Method, args: Seq[V])(using Fixed): V =
    val newFrameData = 0
    // TODO: remove this println summary
    if (mth.name == "println" || mth.name == "print")
      val string = arrayOps.getArray(site)(objectOps.getField(site, getClassFile(site)(ClassType.String))(args(1), FieldIdent.StringValue)).map(_.get)
      arrayOps.printString(string)
      return voidOps.voidRep
    // we are currently unable to properly deal with System.exit
    if mth.classFile.thisType.simpleName == "System" && mth.name == "exit" then
      fail(AbortEval.Exit(args.head), "System.exit")

    if native.nativeFunList.contains(mth.name) then
      val ret = native.invokeClassMethod(mth, args)
      return if mth.descriptor.returnType.isVoidType then voidOps.voidRep else ret

    val body = mth.body.getOrElse:
      if mth.isNative then
        fail(AbortEval.Native(mth), "native method encountered")
      else
        throw UnsupportedOperationException(s"body of ${mth.toString} is empty")
    val locals = body.localVariableTable.map(_.map(lv => convertTypes(lv.fieldType))).getOrElse:
      ArraySeq.fill(body.maxLocals)(ValType.I32)

    val argsAndLocals = args.view ++ locals.map(defaultValue)

    stack.withNewFrame(0):
      frame.withNew(newFrameData, argsAndLocals.zipWithIndex.map((x, y) => (y, Some(x))), Site.Instruction(mth, newFrameData)):
        run(0, mth)
        if mth.descriptor.returnType.isVoidType then
          voidOps.voidRep
        else
          stack.popOrAbort()

  def evalNativeStatic(mth: Method, args: Seq[V]): Unit =
    // TODO: better handling
    mth.name match
      case "makeConcatWithConstants" =>
        //val testBase = objectOps.getField(args(0), (ClassType.String,"value")).get
        val site = Site.Instruction(mth, 0)
        val baseString = arrayOps.getArray(site)(objectOps.getField(site, getClassFile(site)(ClassType.String))(args.head, FieldIdent.StringValue)).map(vals => vals.get)
        val constantString = arrayOps.getArray(site)(objectOps.getField(site, getClassFile(site)(ClassType.String))(args(1), FieldIdent.StringValue)).map(vals => vals.get)
        val concattedString = (baseString ++ constantString).zipWithIndex
        val stringArray = arrayOps.makeArray(arrayAlloc(site),
          concattedString.map(vals => (vals._1, Site.ArrayElementInitialization(site, vals._2))), ArrayType(IntegerType), i32ops.integerLit(concattedString.size))
        val stringObj = createObject(ClassType.String, Site.Instruction(mth, 0))
        objectOps.setField(site, getClassFile(site)(ClassType.String))(stringObj, FieldIdent.StringValue, stringArray)
        stack.push(stringObj)
      case _ =>
        native.evalNative(mth, args)

  // external entrypoint to invoke a function, expecting its arguments on the stack
  def invokeExternal(mth: Method, isStatic: Boolean): V = external:
    val args = stack.popNOrAbort(stack.size)
    invoke(Site.External)(mth, args)

  def evalExternal(inst: Instruction): Unit = external:
    eval(inst, null, 0)

  // wraps eval in fixed
  inline def evalFix(inst: Instruction, mth: Method, pc: Int)(using fixed: Fixed): FixOut =
    fixed(FixIn.Eval(inst, mth, pc))

  private def fixed: Fixed = fixpointSuper:
    case FixIn.Eval(inst, mth, pc) =>
      eval(inst, mth, pc)
      FixOut.Eval()
    case FixIn.Jump(pc, mth) =>
      run_open(pc, mth)
      FixOut.Jump()

  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  // wraps run_open in fixed
  def run(pc: Int, mth: Method)(using fixed: Fixed): Unit =
    fixed(FixIn.Jump(pc, mth)) match
      case FixOut.Jump() => ()
      case out => throw new MatchError(out)

  def run_open(pc: Int, mth: Method)(using Fixed): Unit =
    except.tryCatch {
      runBody(pc, mth)
    } {
      exceptionHandler(pc, mth)
    }

  private def exceptionHandler(pc: Int, mth: Method)(using Fixed): JvmExcept[V] => Unit =
    case JvmExcept.Jump(targetPC) =>
      run(targetPC, mth)
    case JvmExcept.Ret(_) =>
      ??? // TODO
    case JvmExcept.Return(_) =>
      ()
    case JvmExcept.ThrowObject(exception) =>
      // otherwise, handle the exception
      val currPC = frame.data
      val body = mth.body.get
      val handler = body.exceptionHandlersFor(currPC)
        // try to find handler for exception
        .find(handlerException => typeOps.instanceOf(exception, handlerException.catchType.get) == i32ops.integerLit(1))
        // try to find finally clause to invoke if no handler was found
        .orElse(body.handlersFor(currPC).find(_.catchType.isEmpty))
        .getOrElse:
          // no handler found, throw the exception again
          stack.clearCurrentOperandFrame()
          except.throws(JvmExcept.ThrowObject(exception))
      // handler has been found
      stack.push(exception)
      run(handler.handlerPC, mth)

  // evaluates each instruction of the given method's body, starting with pc
  @tailrec
  private def runBody(pc: Int, mth: Method)(using Fixed): Unit =
    // TODO: maybe handle nonexistent body differently
    val body = mth.body.get
    // get is safe to call as unapply always returns Some here
    val currInst = body.iterator.find(_.pc == pc).map(_.instruction).getOrElse:
      throw IllegalStateException(s"can't find instruction with pc $pc")
    frame.setData(pc)
    evalFix(currInst, mth, pc)

    if currInst.nextInstructions(pc)(body).nonEmpty then
      val nextPC = currInst.indexOfNextInstruction(pc)(body)
      runBody(nextPC, mth)

  def convertTypes(opalType: FieldType): ValType = opalType match
    case _: ByteType => ValType.I32
    case _: ShortType => ValType.I32
    case _: IntegerType => ValType.I32
    case _: FloatType => ValType.F32
    case _: LongType => ValType.I64
    case _: DoubleType => ValType.F64
    case _: BooleanType => ValType.I32
    case _: CharType => ValType.I32
    case _: ClassType => ValType.Obj
    case _: ArrayType => ValType.Array

  def defaultValue(ty: ValType): V = ty match
    case ValType.I32 => i32ops.integerLit(0)
    case ValType.I64 => i64ops.integerLit(0)
    case ValType.F32 => f32ops.floatingLit(0)
    case ValType.F64 => f64ops.floatingLit(0)
    case ValType.Obj => objectOps.makeNull()
    case ValType.Array => objectOps.makeNull()

  def defaultValue: FieldType => V = convertTypes.andThen(defaultValue)

  // determines the value of a field
  // its constant value if it exists, the type's default value otherwise
  def fieldValue(site: Site)(field: Field): V =
    field.constantFieldValue.map:
      valueFromConstField(site)
    .getOrElse(defaultValue(field.fieldType))

  // literals for numeric types, the site is needed to allocate a string
  // TODO: consider handling constant strings differently
  def valueFromConstField(site: Site): ConstantFieldValue[?] => V =
    case ConstantDouble(d) => f64ops.floatingLit(d)
    case ConstantFloat(f) => f32ops.floatingLit(f)
    case ConstantInteger(i) => i32ops.integerLit(i)
    case ConstantLong(l) => i64ops.integerLit(l)
    case ConstantString(s) => makeStringObj(site)(s)

  // copied from the loadstring/loadstring_w cases of eval
  def makeStringObj(site: Site)(value: String): V =
    val string = value.toCharArray.map(l => l.toInt).toSeq
    val convString = string.map(l => i32ops.integerLit(l)).zipWithIndex
    val stringArray = arrayOps.makeArray(
      arrayAlloc(site),
      convString.map(vals => (vals._1, Site.ArrayElementInitialization(site, vals._2))),
      ArrayType(CharType),
      i32ops.integerLit(value.length)
    )
    val stringObj = createObject(ClassType.String, site)
    objectOps.setField(site, getClassFile(site)(ClassType.String))(stringObj, FieldIdent.StringValue, stringArray)
    stringObj

  // constructs the fields for an object allocation
  def buildFieldSeq(site: Site)(fields: Fields): Seq[(V, Site, FieldIdent)] = fields.map: field =>
    val ident = FieldIdent(field.classFile.thisType, field.name, field.fieldType)
    val fieldSite = Site.FieldInitialization(site, ident)
    (fieldValue(fieldSite)(field), fieldSite, ident)

  // helper function for all if instructions
  private def handleIfInst(predicate: V => V, target: Int): Unit =
    val v = stack.popOrAbort()
    val flag = predicate(v)
    branchOpsUnit.boolBranch(flag) {
      except.throws(JvmExcept.Jump(target))
    } {}

  // helper function for if<cond> instructions
  // issues a jump to target iff the comparison is successful
  private def handleIfCondInst(cmpOp: (V, V) => V, target: Int): Unit =
    handleIfInst(cmpOp(_, i32ops.integerLit(0)), target)

  // helper function for if_icmp<cond> and if_acmp<cond> instructions
  // issues a jump to target iff the comparison is successful
  private def handleIfCmpInst(cmpOp: (V, V) => V, target: Int): Unit =
    val v2 = stack.popOrAbort()
    handleIfInst(cmpOp(_, v2), target)

  // tries to find a method in the given class file or the first one while moving upwards in the inheritance hierarchy
  private def findMethod(site: Site)(cf: ClassFile, name: String, descriptor: MethodDescriptor): Option[Method] =
    cf.findMethod(name, descriptor).orElse:
      cf.superclassType.flatMap: superCf =>
        findMethod(site)(getClassFile(site)(superCf), name, descriptor)

  private def getStaticFieldAddr(caller: ClassType, mth: Method, site: Site, ident: FieldIdent)(using Fixed): Addr =
    ensureInitialization(mth, site)(ident.declaringClass)
    val resolvedField = resolveField(caller, ident)(using project, except, throwClass(site))
    staticFieldTable.get((), resolvedField.getIdent).option(fail(BytecodeFailure.FieldNotFound, ident.toString))(identity)

  enum AbortEval extends FailureKind:
    // abort eval due to System.exit
    case Exit(v: V)
    // native method encountered
    case Native(m: Method)

  given throwClass: ThrowClass = site => classType =>
    val exc = createObject(classType, site)
    except.throws(JvmExcept.ThrowObject(exc))
