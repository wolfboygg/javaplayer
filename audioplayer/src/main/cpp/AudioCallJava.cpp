//
// Created by 郭磊 on 2020-01-20.
//

#include "AudioCallJava.h"

AudioCallJava::AudioCallJava(JavaVM *vm, JNIEnv *env, jobject *obj) {
    this->vm = vm;
    this->env = env;
    this->jobj = *obj;
    this->jobj = env->NewGlobalRef(this->jobj);

    // 获取jmethod
    jclass clz = env->GetObjectClass(jobj);
    if (!clz) {
        if (LOG_DEBUG) {
            LOGD("get jclass wrong");
        }
        return;
    }
    jmethodId = env->GetMethodID(clz, "onCallPrepared", "()V");
    /**
     * 调用准备状态的方法id
     * 注意java中方法的签名中boolean是Z
     */
    jmethodIdOnLoad = env->GetMethodID(clz, "onCallLoad", "(Z)V");

}

AudioCallJava::~AudioCallJava() {

}

/**
 * type 用来处理自线程和主线程调用的问题
 * @param type
 */

void AudioCallJava::onCallPrepared(int type) {
    if (type == MAIN_THREAD) {

        // 直接进行调用
        this->env->CallVoidMethod(this->jobj, jmethodId);

    } else if (type == CHILD_THREAD) {

        // 需要从自线程中绑定获取JNIEnv对象
        JNIEnv *jniEnv = NULL;
        if (this->vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGD("get child thread jnienv wrong");
            }
            return;
        }
        //然后进行调用
        jniEnv->CallVoidMethod(this->jobj, jmethodId);
        this->vm->DetachCurrentThread();

    }

}

void AudioCallJava::onCallOnLoad(int type, bool load) {
    if (type == MAIN_THREAD) {

        // 直接进行调用
        this->env->CallVoidMethod(this->jobj, jmethodIdOnLoad, load);

    } else if (type == CHILD_THREAD) {

        // 需要从自线程中绑定获取JNIEnv对象
        JNIEnv *jniEnv = NULL;
        if (this->vm->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGD("get child thread jnienv wrong");
            }
            return;
        }
        //然后进行调用
        jniEnv->CallVoidMethod(this->jobj, jmethodIdOnLoad, load);
        this->vm->DetachCurrentThread();

    }
}
