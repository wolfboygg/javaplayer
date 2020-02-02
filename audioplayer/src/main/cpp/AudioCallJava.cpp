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

    /**
     * jni层回调java层的方法
     */
    jmethodIdOnTimeInfo = env->GetMethodID(clz, "onCallTimeInfo", "(II)V");

    /**
     * jni在打开解码器的时候要进行错误的回调
     */
    jmethodIdOnErrorInfo = env->GetMethodID(clz, "onCallErrorInfo", "(ILjava/lang/String;)V");

    jmethodIdOnComplete = env->GetMethodID(clz, "onCallComplete", "()V");

    jmethodIdOnPcmDb = env->GetMethodID(clz, "onCallPcmDB", "(I)V");

    jmethodIdRecordAAC = env->GetMethodID(clz, "encodecPcmToAcc", "(I[B)V");

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

void AudioCallJava::onCallTimeInfo(int type, int currentTime, int totalTime) {
    // 这里用来进行回调
    if (type == MAIN_THREAD) {

        // 直接进行调用
        this->env->CallVoidMethod(this->jobj, jmethodIdOnTimeInfo, currentTime, totalTime);

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
        jniEnv->CallVoidMethod(this->jobj, jmethodIdOnTimeInfo, currentTime, totalTime);
        this->vm->DetachCurrentThread();

    }
}

void AudioCallJava::onCallErrorInfo(int type, int code, char *msg) {
    // 这里用来进行回调
    if (type == MAIN_THREAD) {
        // 直接进行调用
        jstring jmsg = this->env->NewStringUTF(msg);
        this->env->CallVoidMethod(this->jobj, jmethodIdOnErrorInfo, code, jmsg);
        this->env->DeleteLocalRef(jmsg);

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
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(this->jobj, jmethodIdOnErrorInfo, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
        this->vm->DetachCurrentThread();

    }

}

void AudioCallJava::onCallComplete(int type) {
    // 这里用来进行回调
    if (type == MAIN_THREAD) {
        // 直接进行调用
        this->env->CallVoidMethod(this->jobj, jmethodIdOnComplete);

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
        jniEnv->CallVoidMethod(this->jobj, jmethodIdOnComplete);
        this->vm->DetachCurrentThread();

    }

}

void AudioCallJava::onCallPcmDB(int type, int db) {
    if (type == MAIN_THREAD) {
        // 直接进行调用
        this->env->CallVoidMethod(this->jobj, jmethodIdOnPcmDb, db);

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
        jniEnv->CallVoidMethod(this->jobj, jmethodIdOnPcmDb, db);
        this->vm->DetachCurrentThread();

    }
}

void AudioCallJava::onCallPcmRecord(int type, int size, void *buffer) {

    if (type == MAIN_THREAD) {
        // 直接进行调用
        // 将buffer转换为jni层的数组
        jbyteArray jbuffer = env->NewByteArray(size);
        env->SetByteArrayRegion(jbuffer, 0, size, static_cast<const jbyte *>(buffer));
        this->env->CallVoidMethod(this->jobj, jmethodIdRecordAAC, size, jbuffer);
        env->DeleteLocalRef(jbuffer);

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
        jbyteArray jbuffer = jniEnv->NewByteArray(size);
        jniEnv->SetByteArrayRegion(jbuffer, 0, size, static_cast<const jbyte *>(buffer));
        jniEnv->CallVoidMethod(this->jobj, jmethodIdRecordAAC, size, jbuffer);
        jniEnv->DeleteLocalRef(jbuffer);
        this->vm->DetachCurrentThread();

    }

}
