//
// Created by flo on 12/1/25.
//

#ifndef JNI_LLVM_DWARFDUMP_JNIHELPERS_H
#define JNI_LLVM_DWARFDUMP_JNIHELPERS_H

#include <jni.h>
#include <string>
#include <llvm/DebugInfo/DWARF/DWARFDie.h>

/**
 * Struct that contains a DWARFDie for easier passing of DWARFDies through the JNI.
 */
struct DWARFDieHandle {
    const llvm::DWARFDie die;
};
/**
 * Struct that contains and owns all the data contained in the .debug_info section
 */
struct DWARFContextHandle {
    std::unique_ptr<llvm::MemoryBuffer> buffer;
    std::unique_ptr<llvm::object::ObjectFile> object;
    std::unique_ptr<llvm::DWARFContext> context;
};
/** Convert a jstring to a std::string
 *  Inspiration: https://stackoverflow.com/questions/41820039/jstringjni-to-stdstringc-with-utf8-characters
 *
 * @param env JNI-Environment (provided by JNI)
 * @param source String passed by JNI as a jstring that needs to be converted
 * @return converted string of type std::string
 */
std::string jStringToStdString(JNIEnv* env, jstring source);
/**
 * Throws a Java RuntimeException with the given error message.
 * env: the JNI environment pointer
 * message: the error message
 */
void throwJavaRuntimeException(JNIEnv*,const char*);
jlong getjlongFromHandleField(JNIEnv *environment, jobject object, const char* fieldname);
void printAllTags();
void printAllAttributes();
llvm::DWARFUnit* getAsDWARFUnit(JNIEnv *environment, jobject object);
DWARFContextHandle* getAsDWARFContextHandle(JNIEnv *environment, jobject object);
DWARFDieHandle* getAsDWARFDieHandle(JNIEnv *environment, jobject object);
jsize toJSize(std::size_t n);
jlong toJLong(uint64_t n);
void ensureUnitIsParsed(llvm::DWARFUnit *unit);
llvm::dwarf::Attribute intToAttribute(int i);
#endif //JNI_LLVM_DWARFDUMP_JNIHELPERS_H