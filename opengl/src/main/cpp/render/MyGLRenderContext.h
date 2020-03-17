//
// Created by 郭磊 on 2020-03-09.
//

#ifndef JAVAPLAYER_MYGLRENDERCONTEXT_H
#define JAVAPLAYER_MYGLRENDERCONTEXT_H

#include "stdint.h"
#include "GLSampleBase.h"
#include <GLES3/gl3.h>
#include "LogUtil.h"

class MyGLRenderContext {

public:
    MyGLRenderContext();

    ~MyGLRenderContext();

    void SetImageData(int format, int width, int height, uint8_t *pData);

    void SetImageDataWithIndex(int index, int format, int width, int height, uint8_t *pData);

    void SetParamsInt(int paramType, int value0, int value1);

    void UpdateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY);

    void OnSurfaceCreated();

    void OnSurfaceChanged(int width, int height);

    void OnDrawFrame();

    static MyGLRenderContext *getInstance();

    static void DestroyInstance();

private:
    static MyGLRenderContext *m_pContext;
    GLSampleBase *m_pBeforeSample;
    GLSampleBase *m_pCurrentSample;

    int m_ScreenW;
    int m_ScreenH;


};


#endif //JAVAPLAYER_MYGLRENDERCONTEXT_H
