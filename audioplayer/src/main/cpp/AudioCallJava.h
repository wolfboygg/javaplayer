//
// Created by 郭磊 on 2020-01-20.
//

#ifndef JAVAPLAYER_AUDIOCALLJAVA_H
#define JAVAPLAYER_AUDIOCALLJAVA_H

#include <jni.h>
#include "log/AndroidLog.h"

#define MAIN_THREAD 0
#define CHILD_THREAD 1

/**
 * 处理自线程调用java代码的问题
 */
class AudioCallJava {
public:
    JavaVM *vm = NULL;
    JNIEnv *env = NULL;
    jobject jobj;

    jmethodID jmethodId;

    AudioCallJava(JavaVM *vm, JNIEnv *env, jobject *obj);
    ~AudioCallJava();

    void onCallPrepared(int type);



};


#endif //JAVAPLAYER_AUDIOCALLJAVA_H
