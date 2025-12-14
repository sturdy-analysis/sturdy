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
        for (llvm::DWARFDie child = die.getFirstChild(); child.isValid(); child = child.getSibling()) {
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
    return die.getTag();
}

JNIEXPORT void JNICALL Java_llvm_DWARFDie_devTest(JNIEnv *env, jobject obj) {
    const auto die = getDie(env, obj);
    const auto attr = die.find(llvm::dwarf::Attribute::DW_AT_name);
    if (!attr) return;

    std::cout << "Global Var Name: " << attr->getAsCString().get() << std::endl;
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
    if (!attr) return nullptr;

    return env->NewStringUTF(foundAttr->getAsCString().get());
}