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
    // 先释放了队列，然后在销毁线程锁
    clearAVPacket();
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

void PlayerQuene::clearAVPacket() {
    // 进行清空队列操作 队列操作先要将线程等待结束然后才能释放
    pthread_cond_signal(&condPacket);
    // 然后要进行加锁操作才能进行释放
    pthread_mutex_lock(&mutexPacket);

    // 使用while循环进行释放
    while (!queuePacket.empty()) {
        if (LOG_DEBUG) {
            LOGD("queue packet clear")
        }
        AVPacket *packet = queuePacket.front();
        queuePacket.pop();
        av_packet_free(&packet);
        av_free(packet);
        packet = NULL;
    }

    pthread_mutex_unlock(&mutexPacket);
}
