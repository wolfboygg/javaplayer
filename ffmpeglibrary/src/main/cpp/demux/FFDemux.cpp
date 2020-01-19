//
// Created by 郭磊 on 2020-01-17.
//

#include "FFDemux.h"


static double r2d(AVRational rational) {
    return rational.den == 0 || rational.num == 0 ? 0. : (double) rational.num /
                                                         (double) rational.den;
}

bool FFDemux::Open(const char *url) {
    LOGD("Open file %s begin", url);
    Close();

    mux.lock();
    int re = avformat_open_input(&ic, url, 0, 0);
    if (re != 0) {
        mux.unlock();
        LOGD("avformat_open_input failed!!!,reasion is:%s", av_err2str(re));
        return false;
    }

    LOGD("avformat_open_input success!!");

    // 读取文件信息
    re = avformat_find_stream_info(ic, 0);
    if (re < 0) {
        mux.unlock();
        LOGD("avformat_find_stream_info failed!!!,reasion is:%s", av_err2str(re));
        return false;
    }
    LOGD("avformat_find_stream_info success!!");
    this->totalMs = ic->duration / (AV_TIME_BASE / 1000);
    mux.unlock();
    LOGD("total ms = %d!!", this->totalMs);
    return true;
}

bool FFDemux::Seek(double pos) {
    // seek到某个帧，这个帧必须是关键帧才可以
    if (pos < 0 || pos > 1) {
        LOGD("Seek value must 0.0~1.0")
        return false;
    }
    bool re;
    mux.lock();

    if (!ic) {
        mux.unlock();
        return false;
    }

    avformat_flush(ic);
    long long seekPts = 0;
    seekPts = ic->streams[videoStream]->duration * pos;

    // 往后跳到关键帧
    re = av_seek_frame(ic, videoStream, seekPts, AVSEEK_FLAG_FRAME | AVSEEK_FLAG_BACKWARD);
    if (re < 0) {
        mux.unlock();
        LOGD("av_seek_frame failed!!!");
        return false;
    }
    mux.unlock();
    return true;
}

void FFDemux::Close() {
    mux.lock();
    if (ic) {
        avformat_close_input(&ic);
    }
    mux.unlock();

}

XParameter FFDemux::GetVPara() {
    mux.lock();
    if (!ic) {
        mux.unlock();
        LOGD("GetVPara failed! ic is NULL!!!");
        return XParameter();
    }
    // 获取视频流的索引
    int re = av_find_best_stream(ic, AVMEDIA_TYPE_VIDEO, -1, -1, 0, 0);
    if (re < 0) {
        mux.unlock();
        LOGD("av_find_best_stream video failed!!");
        return XParameter();
    }

    videoStream = re;
    XParameter para;
    para.para = ic->streams[videoStream]->codecpar;
    para.time_base = &(ic->streams[videoStream]->time_base);
    mux.unlock();
    return para;
}

XParameter FFDemux::GetAPara() {
    mux.lock();
    if (!ic) {
        mux.unlock();
        LOGD("GetVPara failed! ic is NULL!!!");
        return XParameter();
    }

    int re = av_find_best_stream(ic, AVMEDIA_TYPE_AUDIO, -1, -1, 0, 0);
    if (re < 0) {
        mux.unlock();
        LOGD("av_find_best_stream audio failed!!");
        return XParameter();
    }

    audioStream = re;
    XParameter para;
    para.para = ic->streams[audioStream]->codecpar;
    para.time_base = &(ic->streams[audioStream]->time_base);
    mux.unlock();
    return para;
}

// 读取每一帧，数据由调用这取清理
XData FFDemux::Read() {
    mux.lock();
    if (!ic) {
        mux.unlock();
        LOGD("Read failed! ic is NULL!!!");
        return XData();
    }
    XData d;
    AVPacket *pkt = av_packet_alloc();
    int re = av_read_frame(ic, pkt);
    if (re != 0) {// 表示读取成功
        mux.unlock();
        av_packet_free(&pkt);
        return XData();
    }
    d.data = (unsigned char *) pkt;
    d.size = pkt->size;
    if (pkt->stream_index == audioStream) {
        d.isAudio = true;
    } else if (pkt->stream_index == videoStream) {
        d.isAudio = false;
    } else {
        av_packet_free(&pkt);
        mux.unlock();
        return XData();
    }
    // 转换pts
    pkt->pts = pkt->pts * (1000 * r2d(ic->streams[pkt->stream_index]->time_base));
    pkt->dts = pkt->pts * (1000 * r2d(ic->streams[pkt->stream_index]->time_base));

    d.pts = (int) pkt->pts;

    mux.unlock();
    return d;
}

FFDemux::FFDemux() {
    static bool isFirst = true;
    if (isFirst) {
        isFirst = false;
        // 注册所有的解封装器
        av_register_all();
        avcodec_register_all();
        avformat_network_init();
        LOGD("register ffmpeg");
    }
}
