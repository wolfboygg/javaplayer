package com.ggwolf.ffmpeglibrary.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ggwolf.ffmpeglibrary.MediaHelper;
import com.ggwolf.ffmpeglibrary.R;

public class CDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cdemo);
        findViewById(R.id.test_c_thread).setOnClickListener(this::onClick);
        findViewById(R.id.producter_customer).setOnClickListener(this::onClick);
        findViewById(R.id.c_call_java).setOnClickListener(this::onClick);
        findViewById(R.id.audio_player).setOnClickListener(this::onClick);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.test_c_thread) {
            MediaHelper.getInstance().normalThread();
        } else if (id == R.id.producter_customer) {
            MediaHelper.getInstance().mutexThread();
        } else if (id == R.id.c_call_java) {
            // 使用c++调用java方法
            MediaHelper.getInstance().setmListenner((code, msg) -> Log.i("guo", "call c++ back result code = " + code + "---->msg = " + msg));
            MediaHelper.getInstance().cCallJavaMethod();
        } else if (R.id.audio_player == id) {

        }
    }
}
