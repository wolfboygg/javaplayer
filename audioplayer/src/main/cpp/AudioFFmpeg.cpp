//
// Created by 郭磊 on 2020-01-20.
//

#include "AudioFFmpeg.h"

AudioFFmpeg::AudioFFmpeg(AudioPlayerStatus *audioPlayerStatus, AudioCallJava *audioCallJava,
                         const char *url) {
    this->audioPlayerStatus = audioPlayerStatus;
    this->audioCallJava = audioCallJava;
    this->url = url;
    pthread_mutex_init(&init_mutex, NULL);
}

void *decodeFFmpeg(void *data) {
    AudioFFmpeg *audioFFmpeg = (AudioFFmpeg *) data;
    audioFFmpeg->decodeFFmpegThread();
    pthread_exit(&audioFFmpeg->decodeThread);
}

/**
 * 开始解封装 用自线程
 */
void AudioFFmpeg::prepared() {
    pthread_create(&this->decodeThread, NULL, decodeFFmpeg, this);
}


int avformat_callback(void *ctx) {
    AudioFFmpeg *audioFFmpeg = (AudioFFmpeg *) ctx;
    if (audioFFmpeg->audioPlayerStatus->exit) {
        return AVERROR_EOF;
    }
    return 0;
}

/**
 * 真正的解封装的线程
 *
 * 这里进行释放内存的时候需要特别处理
 * 首先要进行加锁操作 用来在释放时候避免野指针的问题
 * 2.avformat_open_input要加个时间显示，否则会出现长时间不能相应的问题
 *
 */
void AudioFFmpeg::decodeFFmpegThread() {
    // 注册所有的解码器

    pthread_mutex_lock(&init_mutex);


    av_register_all();
    avformat_network_init();

    // 1.打开文件
    avFormatContext = avformat_alloc_context();
    // 要设置回调，用来处理错误回调
    avFormatContext->interrupt_callback.callback = avformat_callback;
    avFormatContext->interrupt_callback.opaque = this;
    if (avformat_open_input(&avFormatContext, url, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGD("can not open url %s", url);
        }
        // 在所有要返回的时候要解锁
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    // 2.查找到我们需要的信息
    if (avformat_find_stream_info(avFormatContext, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGD("can not find stream info");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    // 3找到流
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        if (avFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {// 表示找到了音频流
            if (audioPlayer == NULL) {
                audioPlayer = new AudioPlayer(audioPlayerStatus,
                                              avFormatContext->streams[i]->codecpar->sample_rate,
                                              this->audioCallJava);
                audioPlayer->streamIndex = i;
                audioPlayer->codecpar = avFormatContext->streams[i]->codecpar;
                // 获取当前播放文件的内容总的时间长度
                audioPlayer->duration = avFormatContext->duration / AV_TIME_BASE;
                audioPlayer->time_base = avFormatContext->streams[i]->time_base;
            }
        }
    }

    if (!audioPlayer) {
        if (LOG_DEBUG) {
            LOGD("audio player is null");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    //4.获取解码器
    AVCodec *avCodec = avcodec_find_decoder(audioPlayer->codecpar->codec_id);
    if (!avCodec) {
        if (LOG_DEBUG) {
            LOGD("can not find decoder");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    audioPlayer->avCodecContext = avcodec_alloc_context3(avCodec);
    if (!audioPlayer->avCodecContext) {
        if (LOG_DEBUG) {
            LOGD("can not alloc new decodecctx");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    // 给解码器参数赋值
    if (avcodec_parameters_to_context(audioPlayer->avCodecContext, audioPlayer->codecpar) < 0) {
        if (LOG_DEBUG) {
            LOGD("can not fill decodecctx");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }

    // 然后打开解码器
    if (avcodec_open2(audioPlayer->avCodecContext, avCodec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGD("can not open audio streams");
        }
        exit = true;
        pthread_mutex_unlock(&init_mutex);
        return;
    }
    if (audioCallJava != NULL) {
        if (audioPlayerStatus != NULL && !audioPlayerStatus->exit) {
            audioCallJava->onCallPrepared(CHILD_THREAD);
        } else {
            exit = true;
        }
    }
    pthread_mutex_unlock(&init_mutex);


}

void AudioFFmpeg::start() {
    if (audioPlayer == NULL) {
        if (LOG_DEBUG) {
            LOGD("audio player is null");
        }
        return;
    }

    // 使用播放器进行解码
    audioPlayer->play();

    int count = 0;
    while (audioPlayerStatus != NULL && !audioPlayerStatus->exit) {
        // 开始读取数据
        AVPacket *avPacket = av_packet_alloc();
        if (av_read_frame(avFormatContext, avPacket) == 0) {
            if (avPacket->stream_index == audioPlayer->streamIndex) {
                //将读取出来的frame数据放到队列中
                count++;
                if (LOG_DEBUG) {
                    LOGD("解码第 %d 帧", count);
                }
                audioPlayer->quene->putAvpacket(avPacket);
            } else {
                // 释放
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
            }
        } else {
            // 进行释放分配的空间
            av_packet_free(&avPacket);
            av_free(avPacket);
            while (audioPlayerStatus != NULL && !audioPlayerStatus->exit) {
                // 这里要继续传输数据
                if (audioPlayer->quene->getQueueSize() > 0) {// 表示还有数据
                    continue;
                } else {
                    audioPlayerStatus->exit = true;
                    break;
                }
            }
        }
    }
    exit = true;
}

AudioFFmpeg::~AudioFFmpeg() {
    pthread_mutex_destroy(&init_mutex);
}

void AudioFFmpeg::pause() {
    if (audioPlayer != NULL) {
        audioPlayer->pause();
    }
}

void AudioFFmpeg::resume() {
    if (audioPlayer != NULL) {
        audioPlayer->resume();
    }
}

/**
 * 释放ffmepg
 */
void AudioFFmpeg::release() {
    // 然后开始释放
    if (LOG_DEBUG) {
        LOGD("开始释放ffmepg")
    }
    if (audioPlayerStatus->exit) { // 已经退出
        return;
    }
    if (LOG_DEBUG) {
        LOGD("开始释放ffmepg2")
    }
    audioPlayerStatus->exit = true;

    pthread_mutex_lock(&init_mutex);

    int sleepCount = 0;
    while (!exit) {
        if (sleepCount > 1000) {
            exit = true;
        }
        if (LOG_DEBUG) {
            LOGD("wait ffmpeg exit %d", sleepCount);
        }
        sleepCount++;
        av_usleep(1000 * 10);// 暂停10毫秒
    }

    // 开始释放audio
    if (LOG_DEBUG) {
        LOGD("释放 audio")
    }

    if (audioPlayer != NULL) {
        audioPlayer->release();
        delete (audioPlayer);
        audioPlayer = NULL;
    }

    // 释放封装格式上下文
    if (avFormatContext != NULL) {
        avformat_close_input(&avFormatContext);
        avformat_free_context(avFormatContext);
        avFormatContext = NULL;
    }

    // 释放状态指针
    if (audioPlayerStatus != NULL) {
        audioPlayerStatus = NULL;
    }
    if (audioCallJava != NULL) {
        audioCallJava = NULL;
    }

    pthread_mutex_unlock(&init_mutex);

}

























