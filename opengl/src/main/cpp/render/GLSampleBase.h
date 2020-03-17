//
// Created by 郭磊 on 2020-03-09.
//

#ifndef JAVAPLAYER_GLSAMPLEBASE_H
#define JAVAPLAYER_GLSAMPLEBASE_H

#include "stdint.h"
#include <GLES3/gl3.h>

class GLSampleBase {
public:
    GLSampleBase() {
        m_VertexShader = 0;
        m_FragmentShader = 0;
        m_ProgramObj = 0;
    }

    virtual ~GLSampleBase() {};

    virtual void Init() = 0;

    virtual void Draw(int screenW, int screenH) = 0;

    virtual void Destroy() = 0;

protected:
    GLuint m_VertexShader;
    GLuint m_FragmentShader;
    GLuint m_ProgramObj;
};


#endif //JAVAPLAYER_GLSAMPLEBASE_H
