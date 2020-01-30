//
// Created by 郭磊 on 2020-01-15.
//

#include <jni.h>
#include <string.h>
#include "AudioFFmpeg.h"


/**
 * jni加载的时候进行回调
 */


JavaVM *javaVM = NULL;
AudioCallJava *audioCallJava;
AudioFFmpeg *audioFFmpeg;
AudioPlayerStatus *audioPlayerStatus;

bool native_exit = true;

// 开启自线程进行播放
pthread_t start_thread;


extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    javaVM = vm;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_audioplayer_AudioPlayer_n_1prepared(JNIEnv *env, jobject thiz, jstring path) {
    const char *url = env->GetStringUTFChars(path, 0);

    if (audioFFmpeg == NULL) {
        if (audioCallJava == NULL) {
            audioCallJava = new AudioCallJava(javaVM, env, &thiz);
        }
        // 加载状态调用
        audioCallJava->onCallOnLoad(MAIN_THREAD, true);
        audioPlayerStatus = new AudioPlayerStatus();
        audioFFmpeg = new AudioFFmpeg(audioPlayerStatus, audioCallJava, url);
        audioFFmpeg->prepared();
    }


    // 这里还不能释放，释放完了不能进行解码了
    // env->ReleaseStringUTFChars(path, url);

}

void *startThreadCallback(void *ctx) {
    AudioFFmpeg *audioFFmpeg = (AudioFFmpeg *) (ctx);
    audioFFmpeg->start();
    pthread_exit(&start_thread);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_audioplayer_AudioPlayer_n_1start(JNIEnv *env, jobject thiz) {
    if (audioFFmpeg != NULL) {
        pthread_create(&start_thread, NULL, startThreadCallback, audioFFmpeg);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_audioplayer_AudioPlayer_n_1pause(JNIEnv *env, jobject thiz) {
    if (audioFFmpeg != NULL) {
        audioFFmpeg->pause();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_audioplayer_AudioPlayer_n_1resume(JNIEnv *env, jobject thiz) {
    if (audioFFmpeg != NULL) {
        audioFFmpeg->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_audioplayer_AudioPlayer_n_1stop(JNIEnv *env, jobject thiz) {
    // 释放
    if (!native_exit) {
        return;
    }
    native_exit = false;
    if (audioFFmpeg != NULL) {
        audioFFmpeg->release();
        delete (audioFFmpeg);
        audioFFmpeg = NULL;

        if (audioPlayerStatus != NULL) {
            delete (audioPlayerStatus);
            audioPlayerStatus = NULL;
        }

        if (audioCallJava != NULL) {
            delete (audioCallJava);
            audioCallJava = NULL;
        }
    }
    native_exit = true;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_audioplayer_AudioPlayer_n_1seek(JNIEnv *env, jobject thiz, jint secds) {
    if (audioFFmpeg != NULL) {
        audioFFmpeg->seek(secds);
    }
}
