//
// Created by 郭磊 on 2020-01-17.
//

#ifndef JAVAPLAYER_IOBSERVER_H
#define JAVAPLAYER_IOBSERVER_H

#include <vector>
#include <mutex>
#include "../XData.h"
#include "XThread.h"

class IObserver : public XThread {
public:
    // 观察者接收数据函数
    virtual void Update(XData data) {}

    // 主题函数 ，添加观察者(线程安全)
    void AddObs(IObserver *obs);

    // 通知所有的观察者(线程安全)
    void Notify(XData data);

protected:
    std::vector<IObserver *> obss;
    std::mutex mux;

};


#endif //JAVAPLAYER_IOBSERVER_H
