//
// Created by 郭磊 on 2020-01-19.
//

#include "JavaListener.h"
#include "../log/AndroidLog.h"

JavaListener::JavaListener(JavaVM *javaVm, JNIEnv *env, jobject obj) {
    vm = javaVm;
    jniEnv = env;
    instance = obj;

    jclass clz = env->GetObjectClass(obj);
    jmethodId = env->GetMethodID(clz, "onError", "(ILjava/lang/String;)V");


}

JavaListener::~JavaListener() {

}

void JavaListener::OnError(int threadType, int code, const char *msg) {
    // 先判断当前线程的类型，然后进行处理
    LOGD("thread type is:%d", threadType);
    if (threadType == 0) {// 子线程

        // 如果是主线程，需要获取对应的线程的JNIEnv
        JNIEnv *env;
        vm->AttachCurrentThread(&env, 0);
        // 这里一定要用新获取的env
        jstring jmsg = env->NewStringUTF(msg);
        env->CallVoidMethod(instance, jmethodId, code, jmsg);
        // 进行释放
        env->DeleteLocalRef(jmsg);

        vm->DetachCurrentThread();


    } else if (threadType == 1) {// 主线程
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(instance, jmethodId, code, jmsg);
        // 进行释放
        jniEnv->DeleteLocalRef(jmsg);
    }
}
