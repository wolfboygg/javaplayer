
cmake_minimum_required(VERSION 3.4.1)


include_directories(
        src/main/cpp/util
        src/main/cpp/render

)

add_library( # Sets the name of the library.
        studyrender #libnative-lib.so

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/native-lib.cpp
        src/main/cpp/util/GLUtils.cpp
        src/main/cpp/render/MyGLRenderContext.cpp
        src/main/cpp/sample/TriangleSample.cpp
        )

target_link_libraries( # Specifies the target library.
        studyrender
        EGL
        GLESv3
        android


        log)