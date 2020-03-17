package com.ggwolf.imagedeal;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ImageCopyActivty extends AppCompatActivity {

    private ImageView mPicture;
    private Bitmap mBitmap;
    private int mBitmapWidth;
    private int mBitmapHeight;
    int[] mArrayColor = null;
    int mArrayLength = 0;

    private CloneStampView clone_stamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_copy_activty);
        mPicture = findViewById(R.id.picture);
        clone_stamp = findViewById(R.id.clone_stamp);

//        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_1);
//
//        mBitmapWidth = mBitmap.getWidth();
//        mBitmapHeight = mBitmap.getHeight();
//
//        mArrayLength = mBitmapWidth * mBitmapHeight;
//        mArrayColor = new int[mArrayLength];
//
//        System.out.println("mArrayLength:" + mArrayLength);
//
//        int count = 0;
//        for (int i = 0; i < mBitmapHeight; i++) {
//            for (int j = 0; j < mBitmapWidth; j++) {
//                mArrayColor[count] = mBitmap.getPixel(j, i);
//                count++;
//            }
//        }
    }


    public void onClick(View view) {
//        int[] mLeftColor = new int[(mBitmapWidth / 2) * (mBitmapHeight / 2)];
//
//        int getCount = 0;
//        for (int i = 0; i < mBitmapHeight / 2; i++) {
//            for (int j = 0; j < mBitmapWidth / 2; j++) {
//                mLeftColor[getCount] = mBitmap.getPixel(j, i);
//                getCount++;
//            }
//        }
//
//
//        Bitmap bitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_8888);
//
//        // 重新设置回去
//        int count = 0;
//        int putCount = 0;
//        for (int i = 0; i < mBitmapHeight; i++) {
//            for (int j = 0; j < mBitmapWidth; j++) {
//                if (i > mBitmapHeight / 2 && j < mBitmapWidth / 2) {
//                    bitmap.setPixel(j, i, mLeftColor[putCount]);
//                    putCount++;
//                } else {
//                    bitmap.setPixel(j, i, mArrayColor[count]);
//                }
//                count++;
//            }
//        }
//
//        mPicture.setImageBitmap(bitmap);
        clone_stamp.replaceContainer();
    }


}
