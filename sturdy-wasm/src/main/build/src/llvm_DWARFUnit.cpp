#include "llvm_DWARFUnit.h"

#include <llvm/DebugInfo/DWARF/DWARFUnit.h>

#include "JNIHelpers.h"

JNIEXPORT jlong JNICALL Java_llvm_DWARFUnit_getUnitDIEHandle(JNIEnv *env, jobject obj) {
    const auto ptr = getAsDWARFUnit(env, obj);

    auto result = std::make_unique<DWARFDieHandle>(ptr->getUnitDIE());
    return reinterpret_cast<jlong>(result.release());
}
