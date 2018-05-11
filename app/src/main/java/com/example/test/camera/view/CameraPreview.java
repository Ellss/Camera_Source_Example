package com.example.test.camera.view;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by wudh on 2018/5/11.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private Camera camera;
    private SurfaceHolder holder;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera=camera;
        holder=getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface()==null){
            return;
        }
        try {
            camera.stopPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }
}
