//
// Created by 郭磊 on 2020-01-17.
//

#ifndef JAVAPLAYER_XDATA_H
#define JAVAPLAYER_XDATA_H

extern "C" {
#include <libavformat/avformat.h>
};

enum {
    AVPACKET_TYPE = 0,
    UCHAR_TYPE = 1
};

struct XData {
    int type = 0;
    int pts = 0;

    unsigned char *data = 0;
    unsigned char *datas[8] = {0};
    int size = 0;
    bool isAudio = false;
    int width = 0;
    int height = 0;
    int format = 0;

    bool Alloc(int size, const char *data = 0);
    void Drop();
};


#endif //JAVAPLAYER_XDATA_H
