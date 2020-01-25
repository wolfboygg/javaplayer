//
// Created by 郭磊 on 2020-01-15.
//

#ifndef JAVAPLAYER_ANDROIDLOG_H
#define JAVAPLAYER_ANDROIDLOG_H

#endif //JAVAPLAYER_ANDROIDLOG_H

#include "android/log.h"

#define LOG_DEBUG true

#define LOGD(FORMAT,...) __android_log_print(ANDROID_LOG_DEBUG,"FFMPEG",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"FFMPEG",FORMAT,##__VA_ARGS__);
