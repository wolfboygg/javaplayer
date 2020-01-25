//
// Created by 郭磊 on 2020-01-20.
//

#ifndef JAVAPLAYER_AUDIOPLAYER_H
#define JAVAPLAYER_AUDIOPLAYER_H

#include "AudioPlayerStatus.h"
#include "PlayerQuene.h"


extern "C" {
#include "libavcodec/avcodec.h"
#include "libswresample/swresample.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class AudioPlayer {
public:
    int streamIndex = -1;
    AVCodecContext *avCodecContext = NULL;
    AVCodecParameters *codecpar = NULL;
    PlayerQuene *quene = NULL;
    AudioPlayerStatus *audioPlayerStatus;

    pthread_t thread_play;
    AVPacket *avPacket = NULL;
    AVFrame *avFrame = NULL;
    int ret = -1;
    uint8_t *buffer = NULL; // 这个buffer最终要扔到sl的缓冲队列中进行播放
    int data_size = 0;
    int sample_rate = 0;

    // 引擎接口
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine = NULL;

    // 混音器
    SLObjectItf outputMixObject = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverbItf = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    // pcm
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;

    // 缓冲队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue = NULL;


public:
    AudioPlayer(AudioPlayerStatus *audioPlayerStatus, int sample_rate);

    ~AudioPlayer();

    void play();

    int resampleAudio();

    void initOpenSLES();

    unsigned int getCurrentSampleRateForOpensles(int sample_rate);

};


#endif //JAVAPLAYER_AUDIOPLAYER_H
