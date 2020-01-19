//
// Created by 郭磊 on 2020-01-17.
//

#include "IObserver.h"

void IObserver::AddObs(IObserver *obs) {
    if (!obs) {
        return;
    }
    mux.lock();
    obss.push_back(obs);
    mux.unlock();

}

// 通知所有的观察者
void IObserver::Notify(XData data) {
    mux.lock();
    for (int i = 0; i < obss.size(); i++) {
        obss[i]->Update(data);
    }
    mux.unlock();
}
