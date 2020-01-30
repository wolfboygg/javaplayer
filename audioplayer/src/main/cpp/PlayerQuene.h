//
// Created by 郭磊 on 2020-01-20.
//

#ifndef JAVAPLAYER_PLAYERQUENE_H
#define JAVAPLAYER_PLAYERQUENE_H

#include "queue"
#include "pthread.h"
#include "log/AndroidLog.h"
#include "AudioPlayerStatus.h"
using namespace std;

extern "C" {
#include "libavcodec/avcodec.h"
};

class PlayerQuene {

public:
    std::queue<AVPacket *> queuePacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;
    AudioPlayerStatus *audioPlayerStatus = NULL;

public:
    PlayerQuene(AudioPlayerStatus *audioPlayerStatus);
    ~PlayerQuene();

    int putAvpacket(AVPacket *avPacket);
    int getAvpacket(AVPacket *packet);

    int getQueueSize();

    void clearAVPacket();


};


#endif //JAVAPLAYER_PLAYERQUENE_H
