//
// Created by 郭磊 on 2020-03-09.
//

#ifndef JAVAPLAYER_TRIANGLESAMPLE_H
#define JAVAPLAYER_TRIANGLESAMPLE_H


#include "../render/GLSampleBase.h"

// 必须是public继承
class TriangleSample : public GLSampleBase{
public:
    TriangleSample();
    ~TriangleSample();

    virtual void Init();

    virtual void Draw(int screenW, int screenH);

    virtual void Destroy();
};


#endif //JAVAPLAYER_TRIANGLESAMPLE_H
