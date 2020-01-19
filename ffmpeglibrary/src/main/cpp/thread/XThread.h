//
// Created by 郭磊 on 2020-01-17.
//

#ifndef JAVAPLAYER_XTHREAD_H
#define JAVAPLAYER_XTHREAD_H

// sleep 毫秒级别
void XSleep(int mix);

class XThread {
public:
    // 启动线程
    virtual bool Start();

    // 通过控制isExit安全停止线程(不一定成功)
    virtual void Stop();

    virtual void SetPause(bool isP);

    virtual bool IsPause() {
        return this->isPause;
    }

    // 主函数入口
    virtual void Main() {}

protected:
    bool isExit = false;
    bool isRunning = false;
    bool isPause = false;
    bool isPausing = false;

private:
    void ThreadMain();
};


#endif //JAVAPLAYER_XTHREAD_H
