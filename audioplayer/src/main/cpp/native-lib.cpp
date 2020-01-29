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
        audioPlayerStatus = new AudioPlayerStatus();
        audioFFmpeg = new AudioFFmpeg(audioPlayerStatus, audioCallJava, url);
        audioFFmpeg->prepared();
    }


    // 这里还不能释放，释放完了不能进行解码了
    // env->ReleaseStringUTFChars(path, url);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_audioplayer_AudioPlayer_n_1start(JNIEnv *env, jobject thiz) {
    if (audioFFmpeg != NULL) {
        audioFFmpeg->start();
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