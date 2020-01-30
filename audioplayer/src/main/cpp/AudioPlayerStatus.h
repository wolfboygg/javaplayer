//
// Created by 郭磊 on 2020-01-20.
//

#ifndef JAVAPLAYER_AUDIOPLAYERSTATUS_H
#define JAVAPLAYER_AUDIOPLAYERSTATUS_H


class AudioPlayerStatus {
public:
    bool exit;
    bool load = true;
    bool seek = false;
    AudioPlayerStatus();
    ~AudioPlayerStatus();
};


#endif //JAVAPLAYER_AUDIOPLAYERSTATUS_H
