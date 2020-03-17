#include <jni.h>
#include <string>
#include <MyGLRenderContext.h>
#include "util/LogUtil.h"

#define NATIVE_RENDER_CLASS_NAME  "com/ggwolf/opengl/study/MyNativeRender"
#define  NATIVE_BG_RENDER_CLASS_NATIVE ""

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_Init
 * Signature: ()V
 */
JNIEXPORT void JNICALL native_Init(JNIEnv *env, jobject instance) {
    LOGCATD("native_Init");
}

/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_UnInit
 * Signature: ()V
 */

JNIEXPORT void JNICALL native_UnInit(JNIEnv *env, jobject instance) {

}

/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_SetParamsInt
 * Signature: (III)V
 */

JNIEXPORT void JNICALL
native_SetParamsInt(JNIEnv *env, jobject instance, jint paramType, jint value0, jint value1) {

}

/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_UpdateTransformMatrix
 * Signature: (FFFF)V
 */

JNIEXPORT void JNICALL
native_UpdateTransformMatrix(JNIEnv *env, jobject instance, jfloat rotateX, jfloat rotateY,
                             jfloat scaleX, jfloat scaleY) {

}


/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_SetImageData
 * Signature: (III[B)V
 */
JNIEXPORT void JNICALL
native_SetImageData(JNIEnv *env, jobject instance, jint format, jint width, jint height,
                    jbyteArray imageData) {

}

/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_SetImageDataWithIndex
 * Signature: (IIII[B)V
 */
JNIEXPORT void JNICALL
native_SetImageDataWithIndex(JNIEnv *env, jobject instance, jint index, jint format, jint width,
                             jint height, jbyteArray imageData) {

}

/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_OnSurfaceCreated
 * Signature: ()V
 */
JNIEXPORT void JNICALL native_OnSurfaceCreated(JNIEnv *env, jobject instance) {
    MyGLRenderContext::getInstance()->OnSurfaceCreated();
}


/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_OnSurfaceChange
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
native_OnSurfaceChanged(JNIEnv *env, jobject instance, jint width, jint height) {
    MyGLRenderContext::getInstance()->OnSurfaceChanged(width, height);
}

/*
 * Class:     com_ggwolf_opengl_study_MyNativeRender
 * Method:    native_OnDrawFrame
 * Signature: ()V
 */
JNIEXPORT void JNICALL native_OnDrawFrame(JNIEnv *env, jobject instance) {
    MyGLRenderContext::getInstance()->OnDrawFrame();
}


#ifdef __cplusplus
};
#endif



// 动态注册我们需要的方法

static JNINativeMethod g_RenderMethods[] = {
        {"native_Init",                  "()V",       (void *) (native_Init)},
        {"native_UnInit",                "()V",       (void *) (native_UnInit)},
        {"native_SetImageData",          "(III[B)V",  (void *) (native_SetImageData)},
        {"native_SetImageDataWithIndex", "(IIII[B)V", (void *) (native_SetImageDataWithIndex)},
        {"native_SetParamsInt",          "(III)V",    (void *) (native_SetParamsInt)},
        {"native_UpdateTransformMatrix", "(FFFF)V",   (void *) (native_UpdateTransformMatrix)},
        {"native_OnSurfaceCreated",      "()V",       (void *) (native_OnSurfaceCreated)},
        {"native_OnSurfaceChanged",      "(II)V",     (void *) (native_OnSurfaceChanged)},
        {"native_OnDrawFrame",           "()V",       (void *) (native_OnDrawFrame)},
};


static int
RegisterNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *methods, int methodNum) {
    LOGCATE("RegisterNativeMethods");
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGCATD("RegisterNativeMethods final. clazz == null");
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, methods, methodNum) < 0) {
        LOGCATD("RegisterNativeMethods fail");
    }
    return JNI_TRUE;
}

static void
UnregisterNativeMethods(JNIEnv *env, const char *className) {
    LOGCATD("UnregisterNativeMethods");
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGCATD("UnregisterNativeMethods fial. clazz == null");
    }
    if (env != NULL) {
        env->UnregisterNatives(clazz);
    }
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGCATD("JNI_OnLoad");
    jint jniRet = JNI_ERR;
    JNIEnv *env = NULL;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return jniRet;
    }

    // 动态注册方法
    jint regRet = RegisterNativeMethods(env, NATIVE_RENDER_CLASS_NAME, g_RenderMethods,
                                        sizeof(g_RenderMethods) /
                                        sizeof(g_RenderMethods[0]));
    if (regRet != JNI_TRUE) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;

}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    UnregisterNativeMethods(env, NATIVE_RENDER_CLASS_NAME);
}


























