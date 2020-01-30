package com.ggwolf.audioplayer.listener;

import com.ggwolf.audioplayer.AudioTimeInfoBean;

public interface OnTimeInfoListener {
    void onTimeInfo(AudioTimeInfoBean timeInfo); // 对于当前的时间信息封装成一个类来存储
}
