//
// Created by 郭磊 on 2020-01-20.
//

#include "AudioFFmpeg.h"

AudioFFmpeg::AudioFFmpeg(AudioPlayerStatus *audioPlayerStatus, AudioCallJava *audioCallJava,
                         const char *url) {
    this->audioPlayerStatus = audioPlayerStatus;
    this->audioCallJava = audioCallJava;
    this->url = url;
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

/**
 * 真正的解封装的线程
 */
void AudioFFmpeg::decodeFFmpegThread() {
    // 注册所有的解码器
    av_register_all();
    avformat_network_init();

    // 1.打开文件
    avFormatContext = avformat_alloc_context();
    if (avformat_open_input(&avFormatContext, url, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGD("can not open url %s", url);
        }
        return;
    }
    // 2.查找到我们需要的信息
    if (avformat_find_stream_info(avFormatContext, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGD("can not find stream info");
        }
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
            }
        }
    }

    if (!audioPlayer) {
        if (LOG_DEBUG) {
            LOGD("audio player is null");
        }
        return;
    }

    //4.获取解码器
    AVCodec *avCodec = avcodec_find_decoder(audioPlayer->codecpar->codec_id);
    if (!avCodec) {
        if (LOG_DEBUG) {
            LOGD("can not find decoder");
        }
        return;
    }

    audioPlayer->avCodecContext = avcodec_alloc_context3(avCodec);
    if (!audioPlayer->avCodecContext) {
        if (LOG_DEBUG) {
            LOGD("can not alloc new decodecctx");
        }
        return;
    }
    // 给解码器参数赋值
    if (avcodec_parameters_to_context(audioPlayer->avCodecContext, audioPlayer->codecpar) < 0) {
        if (LOG_DEBUG) {
            LOGD("can not fill decodecctx");
        }
        return;
    }

    // 然后打开解码器
    if (avcodec_open2(audioPlayer->avCodecContext, avCodec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGD("can not open audio streams");
        }
        return;
    }
    audioCallJava->onCallPrepared(CHILD_THREAD);


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
    while(audioPlayerStatus != NULL && !audioPlayerStatus->exit) {
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
            while(audioPlayerStatus != NULL && !audioPlayerStatus->exit) {
                // 这里要继续传输数据
                if (audioPlayer->quene->getQueueSize()> 0) {// 表示还有数据
                    continue;
                } else {
                    audioPlayerStatus->exit = true;
                    break;
                }
            }
        }
    }
}

AudioFFmpeg::~AudioFFmpeg() {

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

























