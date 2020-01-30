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
    jmethodID jmethodIdOnLoad;
    jmethodID jmethodIdOnTimeInfo;

    AudioCallJava(JavaVM *vm, JNIEnv *env, jobject *obj);
    ~AudioCallJava();

    void onCallPrepared(int type);

    void onCallOnLoad(int type, bool load);

    void onCallTimeInfo(int type, int currentTime, int totalTime);



};


#endif //JAVAPLAYER_AUDIOCALLJAVA_H
