//
// Created by 郭磊 on 2020-01-19.
//

#ifndef JAVAPLAYER_JAVALISTENER_H
#define JAVAPLAYER_JAVALISTENER_H


#include <jni.h>

class JavaListener {

public:
    JavaVM *vm;
    JNIEnv *jniEnv;
    jobject instance;
    jmethodID jmethodId;


    JavaListener(JavaVM *javaVm, JNIEnv *env, jobject obj);

    ~JavaListener();

    void OnError(int threadType, int code, const char* msg);

};


#endif //JAVAPLAYER_JAVALISTENER_H
