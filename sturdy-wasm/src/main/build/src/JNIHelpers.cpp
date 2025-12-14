//
// Created by flo on 12/1/25.
//
#include "JNIHelpers.h"

#include <iostream>
#include <llvm/DebugInfo/DWARF/DWARFUnit.h>

std::string jStringToStdString(JNIEnv* env, jstring source) {
    if (!source) {
        std::cout << "jStringToStdString: null source" << std::endl;
        return "";
    }
    const char* chars = env->GetStringUTFChars(source, nullptr); // Get UTF-8 chars
    std::string str{chars};                                     // Convert to std::string
    env->ReleaseStringUTFChars(source, chars);                  // Release memory
    return str;
}

void throwJavaRuntimeException(JNIEnv* env, const char* message) {
    // Find the RuntimeException class
    jclass exClass = env->FindClass("java/lang/RuntimeException");
    if (exClass == nullptr) {
        // If the class is not found, fallback: JVM will throw NoClassDefFoundError
        return;
    }
    // Throw the exception
    env->ThrowNew(exClass, message);
}
jlong getjlongFromHandleField(JNIEnv *env, jobject obj, const char* handleFieldname) {
    jclass jCls = env->GetObjectClass(obj);
    if (!jCls) {
        return 0;
    }
    // "J" is the signature for a long...
    jfieldID longFldID = env->GetFieldID(jCls, handleFieldname, "J");
    if (!longFldID) {
        return 0;
    }
    const jlong longFld = env->GetLongField(obj, longFldID);
    if (!longFld) return 0;

    return longFld;
}

//Utility function
const char *tagToString(const llvm::dwarf::Tag T) {
    using namespace llvm::dwarf;
    switch (T) {
        #define HANDLE_DW_TAG(ID, NAME, VERSION, VENDOR, KIND) \
        case DW_TAG_##NAME: return "DW_TAG_" #NAME;
        #include "llvm/BinaryFormat/Dwarf.def"
        case DW_TAG_lo_user: return "DW_TAG_lo_user";
        case DW_TAG_hi_user: return "DW_TAG_hi_user";
        case DW_TAG_user_base: return "DW_TAG_user_base";
    }
    return "<unknown DW_TAG>";
}

void printAllTags() {
    #define HANDLE_DW_TAG(ID, NAME, VERSION, VENDOR, KIND) \
    std::cout << "" #NAME << "(0x" \
    << std::hex << ID << std::dec << "),\n";
    #include "llvm/BinaryFormat/Dwarf.def"
    std::cout << "lo_user(0x4080),\n";
    std::cout << "hi_user(0xffff),\n";
    std::cout << "user_base(0x1000),\n";
}

//Utility function
const char *attrToString(unsigned int attr) {
    switch (attr) {
        // Redefine the macro to match the definition in the .def file
        #define HANDLE_DW_AT(ID, NAME, VERSION, VENDOR) \
        case ID: return "" #NAME;
        #include "llvm/BinaryFormat/Dwarf.def"
        default: return "<unknown DW_AT>";
        #undef HANDLE_DW_AT
    }
}

void printAllAttributes() {
    #define HANDLE_DW_AT(ID, NAME, VERSION, VENDOR) \
    std::cout << "" #NAME << "(0x" \
    << std::hex << ID << std::dec << "),\n";
    #include "llvm/BinaryFormat/Dwarf.def"
    #undef HANDLE_DW_AT
}

llvm::DWARFUnit* getAsDWARFUnit(JNIEnv *env, jobject dwarfUnitobj) {
    const auto FIELDNAME = "handle"; /* DO NOT CHANGE UNLESS THE FIELDNAME WAS ALSO CHANGED */
    const jlong handle = getjlongFromHandleField(env, dwarfUnitobj, FIELDNAME);
    return reinterpret_cast<llvm::DWARFUnit*>(handle);
}

DWARFContextHandle* getAsDWARFContextHandle(JNIEnv *env, jobject dwarfContextobj) {
    const auto FIELDNAME = "handle"; /* DO NOT CHANGE UNLESS THE FIELDNAME WAS ALSO CHANGED */
    const jlong handle = getjlongFromHandleField(env, dwarfContextobj, FIELDNAME);
    return reinterpret_cast<DWARFContextHandle*>(handle);
}

DWARFDieHandle* getAsDWARFDieHandle(JNIEnv *env, jobject dwarfDieobj) {
    const auto FIELDNAME = "handle";
    const jlong handle = getjlongFromHandleField(env, dwarfDieobj, FIELDNAME);
    return reinterpret_cast<DWARFDieHandle*>(handle);
}

jsize toJSize(const std::size_t n) {
    if (n > static_cast<std::size_t>(std::numeric_limits<jsize>::max())) {
        throw std::overflow_error("jsize overflow");
    }
    return static_cast<jsize>(n);
}

jlong toJLong(uint64_t n) {
    if (n > static_cast<uint64_t>(std::numeric_limits<jlong>::max())) {
        throw std::overflow_error("jlong overflow");
    }
    return static_cast<jlong>(n);
}

void ensureUnitIsParsed(llvm::DWARFUnit *unit) {
    unit->getNumDIEs();
}

llvm::dwarf::Attribute intToAttribute(const int i) {
    return static_cast<llvm::dwarf::Attribute>(static_cast<unsigned>(i));
}