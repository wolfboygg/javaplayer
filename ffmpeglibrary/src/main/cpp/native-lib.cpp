//
// Created by 郭磊 on 2020-01-15.
//

#include <jni.h>
#include <string.h>
#include "log/AndroidLog.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <__locale>

extern "C" {
#include <libavfilter/avfilter.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavcodec/jni.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
#include <unistd.h>
#include <android/native_window_jni.h>

}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ggwolf_ffmpeglibrary_MediaHelper_urlprotocolinfo(JNIEnv *env, jobject instance) {
    // 获取ffmpeg支持的url协议
    char info[40000] = {0};
    av_register_all();
    struct URLProtocol *pup = NULL;
    struct URLProtocol **p_temp = &pup;
    avio_enum_protocols((void **) p_temp, 0);
    while ((*p_temp) != NULL) {
        sprintf(info, "%sInput: %s\n", info, avio_enum_protocols((void **) p_temp, 0));
    }
    pup = NULL;
    avio_enum_protocols((void **) p_temp, 1);
    while ((*p_temp) != NULL) {
        sprintf(info, "%sInput: %s\n", info, avio_enum_protocols((void **) p_temp, 1));
    }

    return env->NewStringUTF(info);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ggwolf_ffmpeglibrary_MediaHelper_avformatinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};
    av_register_all();
    AVInputFormat *if_temp = av_iformat_next(NULL);
    AVOutputFormat *of_temp = av_oformat_next(NULL);
    while (if_temp != NULL) {
        sprintf(info, "%sInput:%s\n", info, if_temp->name);
        if_temp = if_temp->next;
    }
    while (of_temp != NULL) {
        sprintf(info, "%sOutput:%s\n", info, of_temp->name);
        of_temp = of_temp->next;
    }
    return env->NewStringUTF(info);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ggwolf_ffmpeglibrary_MediaHelper_avcodecinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};
    av_register_all();

    AVCodec *c_temp = av_codec_next(NULL);

    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%s --> decode:", info);
        } else {
            sprintf(info, "%s --> encode:", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s(video):", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s(audio):", info);
                break;
            default:
                sprintf(info, "%s(other):", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, c_temp->name);
        c_temp = c_temp->next;
    }

    return env->NewStringUTF(info);

}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ggwolf_ffmpeglibrary_MediaHelper_avfilterinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};

    av_register_all();

    AVFilter *f_temp = const_cast<AVFilter *>(avfilter_next(NULL));

    while (f_temp != NULL) {
        sprintf(info, "%s%s\n", info, f_temp->name);
        f_temp = f_temp->next;
    }
    return env->NewStringUTF(info);

}

static long long GetNowMs() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    int sec = tv.tv_sec % 360000;
    long long t = sec * 1000 + tv.tv_usec / 1000;
    return t;
}


static double r2d(AVRational r) {
    return r.num == 0 || r.den == 0 ? (double) 0 : r.num / (double) r.den;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_ffmpeglibrary_MediaHelper_openVideo(JNIEnv *env, jobject thiz, jstring path,
                                                    jobject surface) {
    // 开始视频文件的解封装-->解码--->播放

    const char *url = env->GetStringUTFChars(path, 0);

    // 1.初始化解封装上下文
    av_register_all();

    AVFormatContext *ic = NULL;

    // 2.打开文件
    int ret = avformat_open_input(&ic, url, 0, 0);
    LOGD("ret is %d", ret);
    if (ret != 0) {// 返回0表示成功了
        LOGD("avformat_open_input failed! %s!", av_err2str(ret));
    } else {
        LOGD("avformat_open_input %s success!", url);
    }

    // 3.获取信息量
    ret = avformat_find_stream_info(ic, 0);
    if (ret < 0) {
        LOGD("avformat_find_stream_info failed!");
    }
    LOGD("duration= %lld nb_stream = %d", ic->duration, ic->nb_streams);


    int fps = 0;
    int videoStream = 0;
    int audioStream = 1;

    // 4.找到视频和音频流的位置
    for (int i = 0; i < ic->nb_streams; ++i) {
        AVStream *as = ic->streams[i];
        if (as->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoStream = i;
            LOGD("视频数据, videoStream is %d", videoStream);
            fps = r2d(as->avg_frame_rate);
            LOGD("fps = %d,width = %d, height = %d codeid = %d pixformat = %d", fps,
                 as->codecpar->width,
                 as->codecpar->height,
                 as->codecpar->codec_id,
                 as->codecpar->format);
        } else if (as->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            LOGD("音频数据 audioStream is %d", audioStream);
            audioStream = i;
            LOGD("sample_rate = %d channels = %d sample_foramt = %d",
                 as->codecpar->sample_rate,
                 as->codecpar->channels,
                 as->codecpar->format);
        }
    }

    // 5.获取音频流信息，和上面遍历去除音视频信息是一样的，这种更加直接
    audioStream = av_find_best_stream(ic, AVMEDIA_TYPE_AUDIO, -1, -1, NULL, 0);
    LOGD("av_find_best_stream audioStream %d", audioStream);

    //=============================视频解码======================================

    // 6. 解码  (分为 硬解码和软解码)
    // 1-1.视频软解码
    AVCodec *vcodec = avcodec_find_decoder(ic->streams[videoStream]->codecpar->codec_id);

    //1-2. 硬解码
//    AVCodec *vcodec = avcodec_find_decoder_by_name("h264_mediacodec"); // 这种方式必须要在jni注册方法中进行注册本地硬解码
    if (!vcodec) {
        LOGD("avcodec_find_decoder failed");
    }

    // 2.初始化解码器
    AVCodecContext *vc = avcodec_alloc_context3(vcodec);
    if (!vc) {
        LOGD("video avcodec_alloc_context3 failed");
    }

    // 3.解码器参数设置
    ret = avcodec_parameters_to_context(vc, ic->streams[videoStream]->codecpar);
    if (ret < 0) {
        LOGD("avcodec_parameters_to_context 解码器参数设置失败");
    }

    vc->thread_count = 1;

    // 4.打开解码器
    ret = avcodec_open2(vc, 0, 0);
    if (ret != 0) {
        LOGD("avcodec_open2 打开解码器失败");
    }
    LOGD("vc timebase = %d/%d,", vc->time_base.num, vc->time_base.den);

    //=============================音频解码======================================
    // 1.音频解码
    AVCodec *acodec = avcodec_find_decoder(ic->streams[audioStream]->codecpar->codec_id);
    if (!acodec) {
        LOGD("音频解码器获取失败")
    }
    // 2.音频解码器初始化
    AVCodecContext *ac = avcodec_alloc_context3(acodec);
    if (!ac) {
        LOGD("audio avcodec_alloc_context3 failed");
    }
    ret = avcodec_parameters_to_context(ac, ic->streams[audioStream]->codecpar);
    if (ret < 0) {
        LOGD("aduio avcodec_parameters_to_context set failed");
    }
    ac->thread_count = 1;

    // 3.打开解码器
    ret = avcodec_open2(ac, 0, 0);
    if (ret != 0) {
        LOGD("audio avcodec_open2 打开解码器失败");
    }

    //=============================开始解码======================================
    //1.定义帧数据
    AVPacket *pkt = av_packet_alloc();
    AVFrame *frame = av_frame_alloc();

    // 测试性能
    long long start = GetNowMs();// 获取当前时间戳
    int frameCount = 0;

    //2.像素格式转换的上下文
    SwsContext *vctx = NULL;

    int outWidth = 1280;
    int outHeight = 720;
    char *rgb = new char[1920 * 1080 * 4];
    char *pcm = new char[48000 * 4 * 2];

    //3.音频重采样上下文初始化
    SwrContext *actx = swr_alloc();
    actx = swr_alloc_set_opts(actx,
                              av_get_default_channel_layout(2),
                              AV_SAMPLE_FMT_S16,
                              ac->sample_rate,
                              av_get_default_channel_layout(ac->channels),
                              ac->sample_fmt,
                              ac->sample_rate,
                              0, 0
    );
    ret = swr_init(actx);
    if (ret != 0) {
        LOGD("swr_init failed");
    } else {
        LOGD("swr_init success");
    }

    //4.显示窗口初始化
    ANativeWindow *nwin = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_setBuffersGeometry(nwin, outWidth, outHeight, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer wbuf;

    for (;;) {
        // 这里是测试每秒解码的帧数 每三秒解码多少帧
        if (GetNowMs() - start >= 3000) {
            LOGD("now decode fps is %d", frameCount / 3);
            start = GetNowMs();
            frameCount = 0;
        }

        ret = av_read_frame(ic, pkt);
        if (ret != 0) {
            LOGD("读到结尾处 end...");
            int pos = 20 * r2d(ic->streams[videoStream]->time_base);
            av_seek_frame(ic, videoStream, pos, AVSEEK_FLAG_BACKWARD | AVSEEK_FLAG_FRAME);
            break;
        }

        AVCodecContext *cc = vc;
        if (pkt->stream_index == audioStream) {
            cc = ac;
        }
        // 1. 发送到线程中解码
        ret = avcodec_send_packet(cc, pkt);

        // 清理
        int p = pkt->pts;
        av_packet_unref(pkt);

        if (ret != 0) {
            LOGD("avcodec_send_packet failed!");
            continue;
        }

        // 每一帧可能对应多个帧数据， 所以要遍历获取
        for (;;) {
            //2 解帧数据
            ret = avcodec_receive_frame(cc, frame);
            LOGD("avcodec_receive_frame ret is %d, error:", ret, av_err2str(ret));
            if (ret != 0) {
                LOGD("avcodec_receive_frame failed");
                break;
            }

            if (cc == vc) {
                frameCount++;
                // 3.初始化像素格式转换的上下文
                vctx = sws_getCachedContext(
                        vctx,
                        frame->width,
                        frame->height,
                        (AVPixelFormat) frame->format,
                        outWidth,
                        outHeight,
                        AV_PIX_FMT_RGBA,
                        SWS_FAST_BILINEAR,
                        0, 0, 0
                );

                if (!vctx) {
                    LOGD("sws_getCachedContext failed");
                } else {
                    uint8_t *data[AV_NUM_DATA_POINTERS] = {0};
                    data[0] = (uint8_t *) rgb;
                    int lines[AV_NUM_DATA_POINTERS] = {0};
                    lines[0] = outWidth * 4;
                    int h = sws_scale(
                            vctx,
                            (const uint8_t **) frame->data,
                            frame->linesize,
                            0,
                            frame->height,
                            data,
                            lines
                    );
                    LOGD("sws_scale = %d", h);
                    if (h > 0) {
                        // 塞到surface中
                        ANativeWindow_lock(nwin, &wbuf, 0);
                        uint8_t *dst = static_cast<uint8_t *>(wbuf.bits);
                        memcpy(dst, rgb, outWidth * outHeight * 4);
                        ANativeWindow_unlockAndPost(nwin);
                    }
                }

            } else {
                // 音频数据
                uint8_t *out[2] = {0};
                out[0] = (uint8_t *) pcm;
                // 音频重采样
                int len = swr_convert(actx, out, frame->nb_samples, (const uint8_t **) frame->data,
                                      frame->nb_samples);
                LOGD("swr_convert = %d", len);
            }

        }
    }

    delete[]rgb;
    delete[]pcm;

    // close context
    avformat_close_input(&ic);
    env->ReleaseStringUTFChars(path, url);

}




extern "C"
JNIEXPORT
jint JNI_OnLoad(JavaVM *vm, void *res) {
    av_jni_set_java_vm(vm, 0);
    return JNI_VERSION_1_4;
}


extern "C"
JNIEXPORT
void JNI_OnUnload(JavaVM *vm, void *reserved) {

}


static SLObjectItf engineSL = NULL;

static SLEngineItf CreateSL() {
    SLresult re;
    SLEngineItf en;

    re = slCreateEngine(&engineSL, 0, 0, 0, 0, 0);
    if (re != SL_RESULT_SUCCESS) {
        return NULL;
    }
    re = (*engineSL)->Realize(engineSL, SL_BOOLEAN_FALSE);
    if (re != SL_RESULT_SUCCESS) {
        return NULL;
    }
    re = (*engineSL)->GetInterface(engineSL, SL_IID_ENGINE, &en);
    if (re != SL_RESULT_SUCCESS) {
        return NULL;
    }

    return en;

}


void pcmCall(SLAndroidSimpleBufferQueueItf bf, void *context) {
    LOGD("PcmCall");
    static FILE *fp = NULL;
    static char *buf = NULL;

    if (!buf) {
        buf = new char[1024 * 1024];
    }
    if (!fp) {
        fp = fopen("/storage/emulated/0/out.pcm", "rb");
    }

    if (!fp) {
        return;
    }

    // 开始输送
    if (feof(fp) == 0) {
        int len = fread(buf, 1, 1024, fp);
        if (len > 0) {
            (*bf)->Enqueue(bf, buf, len);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ggwolf_ffmpeglibrary_MediaHelper_playPcm(JNIEnv *env, jobject thiz) {
    // 使用GLSL来播放音频
    // 1.创建引擎
    SLEngineItf eng = CreateSL();
    if (eng) {
        LOGD("CreateSL success");
    } else {
        LOGD("CreateSL failed");
    }

    //2 创建混音器
    SLObjectItf mix = NULL;
    SLresult re = 0;
    re = (*eng)->CreateOutputMix(eng, &mix, 0, 0, 0);
    if (re != SL_RESULT_SUCCESS) {
        LOGD("CreateOutputMix failed");
    }
    re = (*mix)->Realize(mix, SL_BOOLEAN_FALSE);
    if (re != SL_RESULT_SUCCESS) {
        LOGD("Realize failed");
    }

    SLDataLocator_OutputMix outMix = {SL_DATALOCATOR_OUTPUTMIX, mix};
    SLDataSink audioSink = {&outMix, 0};

    // 3配置音频信息
    SLDataLocator_AndroidSimpleBufferQueue que = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 10};

    // 音频格式
    SLDataFormat_PCM pcm = {
            SL_DATAFORMAT_PCM,
            2,// 声道数
            SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN // 字节序 小端
    };

    SLDataSource ds = {&que, &pcm};

    // 4.创建播放器
    SLObjectItf player = NULL;
    SLPlayItf iplayer = NULL;
    SLAndroidSimpleBufferQueueItf pcmQue = NULL;

    const SLInterfaceID ids[] = {SL_IID_BUFFERQUEUE};
    const SLboolean req[] = {SL_BOOLEAN_TRUE};

    re = (*eng)->CreateAudioPlayer(eng, &player, &ds, &audioSink,
                                   sizeof(ids) / sizeof(SLInterfaceID), ids, req);

    if (re != SL_RESULT_SUCCESS) {
        LOGD("CreateAudioPlayer failed!");
    } else {
        LOGD("CreateAudioPlayer success!");
    }

    (*player)->Realize(player, SL_BOOLEAN_FALSE);

    // 获取player接口
    re = (*player)->GetInterface(player, SL_IID_PLAY, &iplayer);
    if (re != SL_RESULT_SUCCESS) {
        LOGD("GetInterface SL_IID_PLAY failed!")
    }

    re = (*player)->GetInterface(player, SL_IID_BUFFERQUEUE, &pcmQue);
    if (re != SL_RESULT_SUCCESS) {
        LOGD("GetInterface SL_IID_BUFFERQUEUE failed");
    }

    // 设置回调函数
    (*pcmQue)->RegisterCallback(pcmQue, pcmCall, 0);
    // 设置播放状态
    (*iplayer)->SetPlayState(iplayer, SL_PLAYSTATE_PLAYING);

    // 启动队列回调
    (*pcmQue)->Enqueue(pcmQue, "", 1);

}