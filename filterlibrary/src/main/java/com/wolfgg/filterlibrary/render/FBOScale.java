package com.wolfgg.filterlibrary.render;

import android.content.Context;
import android.opengl.GLES20;

import com.wolfgg.filterlibrary.R;
import com.wolfgg.filterlibrary.base.BaseRender;
import com.wolfgg.filterlibrary.utils.ShaderUtils;

public class FBOScale extends BaseRender {

    private int time;

    public FBOScale(Context context) {
        super(context);
    }

    @Override
    public String loadVertexShader() {
        return ShaderUtils.getRawResource(mContext, R.raw.vertex_shader_change_big);
    }

    @Override
    public String loadFragmentShader() {
        return ShaderUtils.getRawResource(mContext, R.raw.fragment_shader);
    }

    @Override
    public void dealSelfFunction(int program) {
        // 获取time然后进行处理
        time = GLES20.glGetUniformLocation(program, "Time");
//        GLES20.glUniform1f(time, );
    }
}
