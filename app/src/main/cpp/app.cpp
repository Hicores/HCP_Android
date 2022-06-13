#include <jni.h>
#include "xposed-detector.h"

jint JNI_OnLoad(JavaVM *jvm, void *) {
    JNIEnv *env;
    if (jvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_VERSION_1_6;
    }
    get_xposed_status(env, android_get_device_api_level());
    return JNI_VERSION_1_6;
}