//
// Created by 郭磊 on 2020-01-17.
//

#include "XThread.h"
#include <thread>
#include <sys/syscall.h>
#include <unistd.h>


using namespace std;

void XSleep(int mis) {
    chrono::milliseconds du(mis);
    this_thread::sleep_for(du);
}


bool XThread::Start() {
    isExit = false;
    isPause = false;
    thread th(&XThread::ThreadMain, this);
    th.detach();
    return true;
}

void XThread::Stop() {
    isExit = true;
    for (int i = 0; i < 200; ++i) {
        if (!isRunning) {
            return;
        }
        XSleep(1);
    }
}

void XThread::SetPause(bool isP) {
    isPause = isP;
    // 等待100毫秒
    for (int i = 0; i < 10; ++i) {
        if (isPause == isP) {
            break;
        }
        XSleep(10);
    }

}

void XThread::ThreadMain() {
    isRunning = true;
    Main();
    isRunning = false;
}


