//
// Created by 郭磊 on 2020-01-20.
//

#ifndef JAVAPLAYER_AUDIOFFMPEG_H
#define JAVAPLAYER_AUDIOFFMPEG_H


#include "AudioPlayerStatus.h"
#include "AudioCallJava.h"
#include "pthread.h"
#include "string.h"
#include "AudioPlayer.h"

extern "C" {
#include <libavformat/avformat.h>
};

class AudioFFmpeg {
public:
    AudioCallJava *audioCallJava = NULL;
    AudioPlayer *audioPlayer = NULL;
    const char *url = NULL;
    pthread_t decodeThread = NULL;
    AVFormatContext *avFormatContext = NULL;
    AudioPlayerStatus *audioPlayerStatus = NULL;

public:
    AudioFFmpeg(AudioPlayerStatus *audioPlayerStatus, AudioCallJava *audioCallJava,
                const char *url);

    ~AudioFFmpeg();


    void prepared();

    void decodeFFmpegThread();

    void start();

    void pause();

    void resume();

};


#endif //JAVAPLAYER_AUDIOFFMPEG_H
