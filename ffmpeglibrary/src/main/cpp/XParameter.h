//
// Created by 郭磊 on 2020-01-17.
//

#ifndef JAVAPLAYER_XPARAMETER_H
#define JAVAPLAYER_XPARAMETER_H

struct AVCodecParameters;
struct AVRational;

class XParameter {
public:
    AVCodecParameters *para = 0;

    int channels = 2;
    int sample_rate = 44100;

    AVRational *time_base;
};


#endif //JAVAPLAYER_XPARAMETER_H
