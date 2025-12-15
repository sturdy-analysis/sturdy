#include "llvm_DWARFDie.h"

#include <iostream>
#include <llvm/DebugInfo/DWARF/DWARFUnit.h>

#include "JNIHelpers.h"

// Utility function to get a llvm::DWARFDie object
llvm::DWARFDie getDie(JNIEnv *env, jobject DWARFDieobj) {
    const auto FIELDNAME = "handle";
    const auto handle = std::bit_cast<DWARFDieHandle*>(getjlongFromHandleField(env, DWARFDieobj, FIELDNAME));
    return handle->die;
}

JNIEXPORT void JNICALL Java_llvm_DWARFDie_destroy(JNIEnv *, jclass, const jlong handle) {
    if (handle == 0) return; //do not free if cppHandle is a nullptr
    const auto wrapper = reinterpret_cast<DWARFDieHandle*>(handle);
#ifdef DEBUG_MEMORY_MANAGEMENT
    std::cout << "Destroying DWARFDieHandle at: " << handle << std::endl;
#endif
    delete wrapper;
}

JNIEXPORT jboolean JNICALL Java_llvm_DWARFDie_hasChildren(JNIEnv *env, jobject obj) {
    const auto die = getDie(env, obj);
    return die.hasChildren();
}

JNIEXPORT jlongArray JNICALL Java_llvm_DWARFDie_getChildHandles(JNIEnv *env, jobject obj) {
    const auto die = getDie(env, obj);
    //Collect offsets of child-DWARFDie objects in data

    std::vector<jlong> data;
    if (die.hasChildren()) {
        for (const auto child : die.children()) {
            auto* handle = new DWARFDieHandle(child);
            data.push_back(std::bit_cast<jlong>(handle));
        }
    }
    jsize size = 0;
    size = toJSize(data.size());
    //create the java array and insert the collected offsets
    jlongArray result = env->NewLongArray(size);
    env->SetLongArrayRegion(result, 0, size, data.data());
    return result;
}

JNIEXPORT jint JNICALL Java_llvm_DWARFDie_getTagAsInteger(JNIEnv *env, jobject obj) {
    const auto die = getDie(env, obj);
    auto res = die.getTag();
    return res;
}

JNIEXPORT void JNICALL Java_llvm_DWARFDie_devTest(JNIEnv *env, jobject obj) {
    const auto die = getDie(env, obj);
    const auto attr = die.find(llvm::dwarf::Attribute::DW_AT_location);
    if (!attr) {std::cout << "not found" << std::endl; return;}
    const auto block = attr.value().getAsBlock();
    if (!block) {std::cout << "could not get as block" << std::endl; return;}
    llvm::ArrayRef<uint8_t> blockArray = block.value();
    llvm::DWARFLocationExpression locExpr;
    locExpr.Expr.assign(blockArray.begin(), blockArray.end());
    locExpr.Range = std::nullopt;

    const uint8_t *data = locExpr.Expr.data();
    const size_t size = locExpr.Expr.size();
    size_t offset = 0;

    while (offset < size) {
        switch (const uint8_t opcode = data[offset++]) {
            case llvm::dwarf::DW_OP_addr: {
                uint64_t operand = 0;
                // read the next 8 bytes (for 64-bit DW_OP_addr) or adjust based on target
                memcpy(&operand, &data[offset], sizeof(uint64_t));
                offset += sizeof(uint64_t);
                std::cout << "DW_OP_addr symbolic: 0x" << std::hex << operand << std::endl;
                break;
            }
            case llvm::dwarf::DW_OP_WASM_location: {
                uint8_t kind = data[offset++];
                // read index depending on kind (ULEB128 or U32)
                std::cout << "DW_OP_WASM_location symbolic" << std::endl;
                break;
            }
            default:
                std::cout << "Opcode: " << static_cast<int>(opcode) << std::endl;
                break;
        }
    }
}

JNIEXPORT jlong JNICALL Java_llvm_DWARFDie_getAttrAsDWARFDieHandle(JNIEnv *env, jobject obj, jint attrVal) {
    const auto die = getDie(env, obj);
    const auto attr = intToAttribute(attrVal);
    const auto refDie = die.getAttributeValueAsReferencedDie(attr);
    if (!refDie.isValid()) return 0;
    return std::bit_cast<jlong>(new DWARFDieHandle(refDie));
}

JNIEXPORT jlong JNICALL Java_llvm_DWARFDie_getAttrAsLong(JNIEnv *env, jobject obj, jint attrVal) {
    const auto die = getDie(env, obj);
    const auto attr = intToAttribute(attrVal);
    const auto foundAttr = die.find(attr);

    //if attribute does not exist
    if (!foundAttr) {
        throwJavaRuntimeException(env, "Attribute not found");
        return std::numeric_limits<jlong>::max();
    }

    const auto unsignedConst = foundAttr.value().getAsUnsignedConstant();
    if (!unsignedConst) {
        throwJavaRuntimeException(env, "UnsignedConstant could not be extracted");
        return std::numeric_limits<jlong>::max();
    }
    return toJLong(unsignedConst.value());
}

JNIEXPORT jstring JNICALL Java_llvm_DWARFDie_getAttrAsString(JNIEnv *env, jobject obj, jint attrVal) {
    const auto die = getDie(env, obj);
    const auto attr = intToAttribute(attrVal);
    const auto foundAttr = die.find(attr);
    if (!foundAttr) return nullptr;

    return env->NewStringUTF(foundAttr->getAsCString().get());
}

JNIEXPORT jint JNICALL Java_llvm_DWARFDie_getAddrSizeFromUnit(JNIEnv *env, jobject obj) {
    const auto die = getDie(env, obj);
    return die.getDwarfUnit()->getAddressByteSize();
}

JNIEXPORT jlong JNICALL Java_llvm_DWARFDie_getOffset(JNIEnv *env, jobject obj) {
    const auto die = getDie(env, obj);
    return toJLong(die.getOffset());
}