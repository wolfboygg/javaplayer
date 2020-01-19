//
// Created by 郭磊 on 2020-01-17.
//

#include "IDemux.h"

void IDemux::Main() {
    while (!isExit) {
        // 进行解封装
        if (IsPause()) {
            XSleep(2);
            continue;
        }
        XData d = Read();
        if (d.size > 0) {
            Notify(d);
        } else {
            XSleep(2);
        }
    }
}
