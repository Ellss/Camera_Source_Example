package com.example.test.camera.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;


/**
 * Created by wudh on 2018/5/7.
 */
//图片帮助类
public class BitmapUtils {
    //计算图片的缩放值
    public static int calculateinSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        final int width =options.outWidth;
        final int height =options.outHeight;
        int inSampleSize =1;
        if (reqHeight<height||reqWidth<width){
            final int widthRadio=Math.round((float) width/(float)reqWidth);
            final int heightRadio=Math.round((float) height/(float)reqHeight);
            inSampleSize=widthRadio<heightRadio?widthRadio:heightRadio;
        }
        return inSampleSize;
    }
    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSamllerBitmap(String path,int reqWidth,int reqHeight){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        //只加载图像框架
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(path,options);

        options.inSampleSize=calculateinSampleSize(options,reqWidth,reqHeight);
        //设置加载全部图像内容
        options.inJustDecodeBounds=false;
        return BitmapFactory.decodeFile(path,options);
    }
    //旋转照片
    public static Bitmap rotateBitmap(Bitmap bitmap,int angle){
        if (null!=bitmap){
            Matrix matrix=new Matrix();
            matrix.postRotate(angle);

            bitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        }
        return bitmap;
    }
    //得到照片旋转角度
    public static int getBitmapAngle(String path){
        int angle=0;
        try {
            ExifInterface exifInterface=new ExifInterface(path);
            int orientation =exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle=90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle=180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle=270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return angle;
    }
}
