//
// Created by 郭磊 on 2020-01-20.
//

#include "PlayerQuene.h"

PlayerQuene::PlayerQuene(AudioPlayerStatus *audioPlayerStatus) {
    this->audioPlayerStatus = audioPlayerStatus;
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);

}

PlayerQuene::~PlayerQuene() {
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);

}

int PlayerQuene::putAvpacket(AVPacket *avPacket) {
    // 保证线程同步
    pthread_mutex_lock(&mutexPacket);

    // 进行入队
    queuePacket.push(avPacket);
    if (LOG_DEBUG) {
        LOGD("放入一个AVPacket到队列，个数为%d", queuePacket.size());
    }
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

int PlayerQuene::getAvpacket(AVPacket *packet) {

    pthread_mutex_lock(&mutexPacket);
    while (audioPlayerStatus != NULL && !audioPlayerStatus->exit) {
        if (queuePacket.size() > 0) {// 能进行获取
            AVPacket *avPacket = queuePacket.front();
            if (av_packet_ref(packet, avPacket) == 0) {
                queuePacket.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            if (LOG_DEBUG) {
                LOGD("从队列中获取一个AVPacket, 还剩下 %d 个", queuePacket.size());
            }
            break;
        } else {// 进行等待
            pthread_cond_wait(&condPacket, &mutexPacket);

        }
    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

int PlayerQuene::getQueueSize() {
    int size = 0;
    pthread_mutex_lock(&mutexPacket);
    size = queuePacket.size();
    pthread_mutex_unlock(&mutexPacket);
    return size;
}
