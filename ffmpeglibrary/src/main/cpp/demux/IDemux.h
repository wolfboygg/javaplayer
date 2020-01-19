//
// Created by 郭磊 on 2020-01-17.
//

#ifndef JAVAPLAYER_IDEMUX_H
#define JAVAPLAYER_IDEMUX_H


#include "../thread/IObserver.h"
#include "../XParameter.h"

extern "C" {
#include <libavformat/avformat.h>
};

class IDemux : public IObserver {
public:
    // 打开文件 或者流媒体 rtmp http rtsp
    virtual bool Open(const char *url) = 0;
    // seek的位置
    virtual bool Seek(double pos) = 0;

    virtual void Close() = 0;

    // 读取视频参数
    virtual XParameter GetVPara() = 0;

    // 读取音频参数
    virtual XParameter GetAPara() = 0;

    // 读取每一帧
    virtual XData Read() = 0;

    // 总时间长度
    int totalMs = 0;

protected:
    virtual void Main();

};


#endif //JAVAPLAYER_IDEMUX_H
