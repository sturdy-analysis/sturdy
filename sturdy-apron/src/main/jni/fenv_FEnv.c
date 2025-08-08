#include "fenv_FEnv.h"
#include <fenv.h>

JNIEXPORT jint JNICALL Java_fenv_FEnv_getRoundingMode(JNIEnv *env, jclass klass) {
    return fegetround();
}

JNIEXPORT void JNICALL Java_fenv_FEnv_setRoundingMode(JNIEnv *env, jclass klass, jint roundingMode) {
    fesetround(roundingMode);
}

JNIEXPORT jint JNICALL Java_fenv_FEnv_FE_1TONEAREST(JNIEnv *env, jclass klass) {
    return FE_TONEAREST;
}

JNIEXPORT jint JNICALL Java_fenv_FEnv_FE_1DOWNWARD(JNIEnv *env, jclass klass) {
    return FE_DOWNWARD;
}

JNIEXPORT jint JNICALL Java_fenv_FEnv_FE_1UPWARD(JNIEnv *env, jclass klass) {
    return FE_UPWARD;
}

JNIEXPORT jint JNICALL Java_fenv_FEnv_FE_1TOWARDZERO(JNIEnv *env, jclass klass) {
    return FE_TOWARDZERO;
}