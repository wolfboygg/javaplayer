//
// Created by 郭磊 on 2020-03-09.
//

#include "MyGLRenderContext.h"
#include "../sample/TriangleSample.h"

MyGLRenderContext *MyGLRenderContext::m_pContext = nullptr;

MyGLRenderContext::MyGLRenderContext() {

    m_pCurrentSample = new TriangleSample();
    m_pBeforeSample = nullptr;

}

MyGLRenderContext::~MyGLRenderContext() {
    // 释放资源
    if (m_pBeforeSample) {
        delete m_pBeforeSample;
        m_pBeforeSample = nullptr;
    }

    if (m_pCurrentSample) {
        delete m_pCurrentSample;
        m_pCurrentSample = nullptr;
    }
}

void MyGLRenderContext::SetImageData(int format, int width, int height, uint8_t *pData) {

}

void MyGLRenderContext::SetImageDataWithIndex(int index, int format, int width, int height,
                                              uint8_t *pData) {

}

void MyGLRenderContext::SetParamsInt(int paramType, int value0, int value1) {

}

void
MyGLRenderContext::UpdateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY) {

}

void MyGLRenderContext::OnSurfaceCreated() {
    LOGCATD("MyGLRenderContext::OnSurfaceCreated");
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);// 白色清底
}

void MyGLRenderContext::OnSurfaceChanged(int width, int height) {
    LOGCATD("MyGLRenderContext::OnSurfaceChanged [w,h] = [%d, %d]", width, height);
    glViewport(0, 0, width, height);
    m_ScreenW = width;
    m_ScreenH = height;
}

void MyGLRenderContext::OnDrawFrame() {
    // 绘制我们的需要的
    LOGCATD("MyGLRenderContext::OnDrawFrame");
    if (m_pBeforeSample) {
        m_pBeforeSample->Destroy();
        delete m_pBeforeSample;
        m_pBeforeSample = nullptr;
    }
    if (m_pCurrentSample) {
        m_pCurrentSample->Init();
        m_pCurrentSample->Draw(m_ScreenW, m_ScreenH);
    }
}

MyGLRenderContext *MyGLRenderContext::getInstance() {
    if (m_pContext == nullptr) {
        LOGCATD("MyGLRenderContext::getInstance");
        m_pContext = new MyGLRenderContext();
    }
    return m_pContext;
}

void MyGLRenderContext::DestroyInstance() {
    LOGCATD(" MyGLRenderContext::DestroyInstance");
    if (m_pContext) {
        delete m_pContext;
        m_pContext = nullptr;
    }
}
