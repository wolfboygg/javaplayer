//
// Created by 郭磊 on 2020-01-20.
//
/**
 * 1.添加加载状态要根据我们的解码的队列中是否有数据来决定是否在加载状态
 *  然后在c++层回调java中的方法来实现
 * 2.暂停播放
 *  使用openSLES中的 pcmPlayerPlay 播放器的状态来进行实现
 *   (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
 *
 * 3.添加播放时长的处理
 *  总的播放时常可以通过AVFormatContext来进行获取
 *  当前的播放时长要通过AVFrame的时间+播放多少内容的打时间
 *
 */

#ifndef JAVAPLAYER_AUDIOPLAYER_H
#define JAVAPLAYER_AUDIOPLAYER_H

#include "AudioPlayerStatus.h"
#include "PlayerQuene.h"
#include "AudioCallJava.h"


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

    AudioCallJava *audioCallJava;
    bool load = true;// 默认的是在加载状态

    int duration = 0;
    AVRational time_base; // 这个是和流相关的，我们需要在获取到当前流的时候就应该对它赋值
    double clock; // 总的播放时间长度
    double now_time; // 当前frame的时间
    double last_time; // 主要用来控制回调java层的频率


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
    AudioPlayer(AudioPlayerStatus *audioPlayerStatus, int sample_rate, AudioCallJava *audioCallJava);

    ~AudioPlayer();

    void play();

    void pause();

    void resume();

    int resampleAudio();

    void initOpenSLES();

    unsigned int getCurrentSampleRateForOpensles(int sample_rate);

};


#endif //JAVAPLAYER_AUDIOPLAYER_H
