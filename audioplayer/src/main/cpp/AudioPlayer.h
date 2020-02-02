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
 *  4.释放内存 在停止播放的时需要进行处理
 *     1.释放我们创建的queue
 *     2.释放openSL
 *     3.释放Audio
 *     4.释放FFMpeg
 *
 *     释放内存的时候要先释放然后在delete然后在置空
 *
 *
 *  5.添加音量控制 通过OpenSL的声音接口进行控制
 *
 *  6.添加声道控制功能
 *      通过OpenSL ES的声道控制接口进行处理
 *
 *
 *
 */

#ifndef JAVAPLAYER_AUDIOPLAYER_H
#define JAVAPLAYER_AUDIOPLAYER_H

#include "AudioPlayerStatus.h"
#include "PlayerQuene.h"
#include "AudioCallJava.h"
#include "SoundTouch.h"

extern "C" {
#include "libavcodec/avcodec.h"
#include "libswresample/swresample.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

using namespace soundtouch;


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

    // 声音控制接口
    SLVolumeItf  pcmPlayerVolume = NULL;
    int volumePercent = 100;

    SLMuteSoloItf  pcmPlayerMute = NULL;
    int muteSolo = 2;

    // 缓冲队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue = NULL;

    // 实现变调功能
    SoundTouch *soundTouch = NULL;
    SAMPLETYPE *sampleBuffer = NULL;
    bool finished = true;
    uint8_t *out_buffer = NULL;
    int nb = 0;
    int num = 0;

    float pitch = 1.0f;
    float speed = 1.0f;



public:
    AudioPlayer(AudioPlayerStatus *audioPlayerStatus, int sample_rate, AudioCallJava *audioCallJava);

    ~AudioPlayer();

    void play();

    void pause();

    void resume();

    int resampleAudio(void **pcmbuf);

    void initOpenSLES();

    unsigned int getCurrentSampleRateForOpensles(int sample_rate);

    /**
     * 这个方法是让播放时器停止播放
     */
    void stop();

    /**
     * 这个方法用来释放我们需要释放内存
     */
    void release();

    void setVolume(int percent);

    void setMuteSolo(int mute);

    int getSoundTouchData();

    void setPitch(float pitch);

    void setSpeed(float speed);

    int getPCMDB(char *pcmdata, size_t pcmsize);

};


#endif //JAVAPLAYER_AUDIOPLAYER_H
