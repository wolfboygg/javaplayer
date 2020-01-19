//
// Created by 郭磊 on 2020-01-17.
//

#ifndef JAVAPLAYER_FFDEMUX_H
#define JAVAPLAYER_FFDEMUX_H


#include "IDemux.h"
#include "../log/AndroidLog.h"

using namespace std;

struct AVFormatContext;

class FFDemux : public IDemux {
public:
    // 打开文件 或者流媒体 rtmp http rtsp
    virtual bool Open(const char *url);

    // seek位置pos 0.0~1.0
    virtual bool Seek(double pos);

    virtual void Close();

    // 获取视频参数
    virtual XParameter GetVPara();

    // 获取音频参数
    virtual XParameter GetAPara();

    // 读取一帧数据 数据由调用着清理
    virtual XData Read();

    FFDemux();

private:
    AVFormatContext *ic = 0;
    std::mutex mux;
    int videoStream = 0;
    int audioStream = 1;


};


#endif //JAVAPLAYER_FFDEMUX_H
