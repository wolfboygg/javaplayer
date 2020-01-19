//
// Created by 郭磊 on 2020-01-17.
//

#include "IDecode.h"

void IDecode::Update(XData pkt) {
    if (pkt.isAudio != isAudio) {
        return;
    }
    while (!isExit) {
        packsMutex.lock();

        // 阻塞
        if (pkt.size < maxList) {
            // 生产者
            packs.push_back(pkt);
            packsMutex.unlock();
            break;
        }
        packsMutex.unlock();
        XSleep(1);
    }
}

void IDecode::Clear() {
    while (!packs.empty()) {
        packs.front().Drop();
        packs.pop_front();
    }
    pts = 0;
    syncPts = 0;

}

void IDecode::Main() {
    while (!isExit) {
        if (IsPause()) {
            XSleep(2);
            continue;
        }
        packsMutex.lock();

        // 判断音视频是否同步 视频去同步到音频上
        if (!isAudio && syncPts > 0) {
            if (syncPts < pts) {
                packsMutex.unlock();
                XSleep(1);
                continue;
            }
        }

        if (packs.empty()) {
            packsMutex.unlock();
            XSleep(1);
            continue;
        }

        // 取出packet 消费者
        XData pack = packs.front();
        packs.pop_front();

        // 发送数据到解码线程 一个数据包 可能解码处很多帧
        if (this->SendPacked(pack)) {
            while (!isExit) {
                // 获取解码数据
                XData frame = RecvFrame();
                if (!frame.data) {
                    break;
                }
                this->Notify(frame);
            }
        }
        pack.Drop();
        packsMutex.unlock();
    }

}
