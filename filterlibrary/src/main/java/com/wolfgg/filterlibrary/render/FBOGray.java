package com.wolfgg.filterlibrary.render;

import android.content.Context;

import com.wolfgg.filterlibrary.R;
import com.wolfgg.filterlibrary.base.BaseRender;
import com.wolfgg.filterlibrary.utils.ShaderUtils;

public class FBOGray extends BaseRender {

    public FBOGray(Context context) {
        super(context);
    }

    @Override
    public String loadVertexShader() {
        return ShaderUtils.getRawResource(mContext, R.raw.vertex_shader);
    }

    @Override
    public String loadFragmentShader() {
        return ShaderUtils.getRawResource(mContext, R.raw.fragment_shader_gray);
    }
}
