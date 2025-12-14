#include <filesystem>
#include <iostream>
#include <jni.h>
#include <memory>
#include <llvm/DebugInfo/DWARF/DWARFContext.h>

#include "JNIHelpers.h"

extern "C" {
    JNIEXPORT void JNICALL Java_llvm_DWARFContext_destroy(JNIEnv *env, jclass obj, const jlong cppHandle) {
        if (cppHandle == 0) return; //do not free if cppHandle is a nullptr

        const auto wrapper = reinterpret_cast<DWARFContextHandle *>(cppHandle);
        std::cout << "Destroying DWARFContextHandle at: " << cppHandle << std::endl;
        delete wrapper;
    }

    JNIEXPORT jlong JNICALL Java_llvm_DWARFContext_createFromFile(JNIEnv *env, jobject obj, jstring jpath) {
        const std::filesystem::path cwd = std::filesystem::current_path();
        //std::cout << "Current working directory (C++):\t" << cwd.c_str() << std::endl;

        const char *path = env->GetStringUTFChars(jpath, nullptr);
        if (!path) return 0;

        auto wrapper = std::make_unique<DWARFContextHandle>();

        // MemoryBuffer
        auto bufOrErr = llvm::MemoryBuffer::getFile(path);
        env->ReleaseStringUTFChars(jpath, path);
        if (!bufOrErr) return 0;

        wrapper->buffer = std::move(*bufOrErr);
        // ObjectFile
        auto objOrErr = llvm::object::ObjectFile::createObjectFile(wrapper->buffer->getMemBufferRef());
        if (!objOrErr) return 0;

        std::unique_ptr<llvm::object::ObjectFile> object = std::move(*objOrErr);
        wrapper->object = std::move(object);

        // DWARFContext
        wrapper->context = llvm::DWARFContext::create(*wrapper->object);
        std::cout << "Created DWARFContextHandle at: " << wrapper.get() << std::endl;
        wrapper->context->compile_units();
        return reinterpret_cast<jlong>(wrapper.release());
    }

    JNIEXPORT jlongArray JNICALL Java_llvm_DWARFContext_getCompileUnitHandles(JNIEnv *env, jobject obj) {
        const auto ptr = getAsDWARFContextHandle(env, obj);
        const auto range = ptr->context->compile_units();

        std::vector<jlong> data;
        for (auto& unit : range) {
            llvm::DWARFUnit* u = unit.get();
            //LLVM loads debug_info so lazily, that you sometimes get an empty children() iterator from a DWARFDie
            //that returns true on hasChildren()
            ensureUnitIsParsed(u);
            data.push_back(reinterpret_cast<jlong>(u));
        }
        const jsize size = toJSize(data.size());
        jlongArray result = env->NewLongArray(size);
        env->SetLongArrayRegion(result, 0, size, data.data());
        return result;
    }

    JNIEXPORT void JNICALL Java_llvm_DWARFContext_devTest(JNIEnv *env, jobject obj) {
        //const auto ptr = getAsDWARFContextHandle(env, obj);
        //const auto &ctx = ptr->context;
        printAllAttributes();
    }
} /* extern "C" */