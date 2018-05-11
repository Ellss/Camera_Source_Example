package com.example.test.camera.ui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.test.camera.R;
import com.example.test.camera.util.BitmapUtils;
import com.example.test.camera.view.CameraPreview;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wudh on 2018/5/11.
 */

public class CameraActivity extends Activity implements View.OnClickListener{

    Camera camera=null;

    private LinearLayout surfaceviewContainer;
    private LinearLayout photoContainer;
    private ImageView ivTakePhoto;
    private ImageView ivFoucs;
    private ImageView ivShow;
    private CameraPreview cameraPreview;
    private List<Size> photoSizes, previewSizes;
    private boolean isFoucs=false;
    private String photoPath=null;
    private List<String> photosList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        if (checkCameraHardware()){
            openCamera();
            initView();
        }else {
            Toast.makeText(this,"当前不支持相机!",Toast.LENGTH_SHORT).show();
        }
    }
    private void initView(){
        ivTakePhoto=(ImageView) findViewById(R.id.iv_takephoto);
        ivShow=(ImageView) findViewById(R.id.iv_show);
        ivFoucs=(ImageView) findViewById(R.id.iv_foucs);
        ivTakePhoto.setOnClickListener(this);

        photoContainer=(LinearLayout)findViewById(R.id.ll_container_photo);
    }
    private void openCamera(){
        if (camera==null){
            //获取相机实例
            camera=camera.open();
            //获取相机参数实例
            Camera.Parameters parameters=camera.getParameters();
            //将相机顺时针旋转90度
            camera.setDisplayOrientation(90);
            //设置输出图片类型
            parameters.setPictureFormat(ImageFormat.JPEG);
            //获取默认照片和相机尺寸 private List<Size> photoSizes, previewSizes;
            photoSizes=camera.getParameters().getSupportedPictureSizes();
            //从大到小还是从小到大顺序
            boolean isF = photoSizes.get(0).width < photoSizes.get(photoSizes
                    .size() - 1).width;
            for (Size size : photoSizes) {
                System.out.println("width:" + size.width + "-height:"
                        + size.height);
                if (isF) {
                    //设置width:2048-height:1536 图片大小
                    if (size.width > 1800) {
                        parameters.setPictureSize(size.width, size.height);
                        break;
                    }
                } else {
                    if (size.width < 2000) {
                        parameters.setPictureSize(size.width, size.height);
                        break;
                    }
                }
            }
            previewSizes = parameters.getSupportedPreviewSizes();
            boolean isFp = previewSizes.get(0).width < previewSizes
                    .get(previewSizes.size() - 1).width;
            for (Size size : previewSizes) {
                System.out.println("width:" + size.width + "-height:"
                        + size.height);
                if (isFp) {
                    //设置width:1920-height:1088 预览
                    if (size.width > 1800) {
                        parameters.setPreviewSize(size.width, size.height);
                        break;
                    }
                } else {
                    if (size.width < 2000) {
                        parameters.setPreviewSize(size.width, size.height);
                        break;
                    }
                }
            }
            camera.setParameters(parameters);
            if (surfaceviewContainer==null){
                surfaceviewContainer=(LinearLayout)findViewById(R.id.ll_container_surfaceview);
            }
            cameraPreview=new CameraPreview(CameraActivity.this,camera);
            surfaceviewContainer.addView(cameraPreview);
        }
    }
    // 判断相机是否支持
    private boolean checkCameraHardware() {
        if (this.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    //自动对焦回调
    private Camera.AutoFocusCallback afcb =new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //对焦状态
            isFoucs=success;
            if (success){
                //对焦成功的样式图片
                ivFoucs.setBackgroundResource(R.drawable.ic_focus_successed);
                try{
                    camera.takePicture(null,null,pcb);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                //对焦失败的样式图片
                ivFoucs.setBackgroundResource(R.drawable.ic_focus_failed);
            }
        }
    };
    private Camera.PictureCallback pcb=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //播放系统拍照声音
            shootSound();
            isFoucs = false;
            ivFoucs.setBackgroundResource(R.drawable.ic_focusing_begin);
            //得到文件路径
            String path = getPhotoPath();
            //相片高度及默认高度
            int height = camera.getParameters().getPictureSize().height;
            if (height <= 0) {
                height = 320;
            }

            camera.startPreview();
            photosList.add(path);
            //输出文件流
            addPhoto(path,data);
        }
    };
    private void addPhoto(String path,byte[] data){
        if (path!=null){
            try {
                FileOutputStream os=new FileOutputStream(path);
                os.write(data);
                os.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        File file=new File(path);
        if (file!=null){
            Bitmap bitmap= BitmapUtils.getSamllerBitmap(path,200,200);
            bitmap=BitmapUtils.rotateBitmap(bitmap,90);//BitmapUtils.getBitmapAngle(path)
            ivShow.setImageBitmap(bitmap);
        }else {
            Toast.makeText(CameraActivity.this,"显示照片失败！",Toast.LENGTH_SHORT);
        }
    }
    /**
     * 播放系统拍照声音
     */
    public void shootSound() {
        AudioManager meng = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        if (volume != 0) {
            MediaPlayer shootMP = null;
            if (shootMP == null)
                shootMP = MediaPlayer
                        .create(this,
                                Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            if (shootMP != null)
                shootMP.start();
        }
    }
    private String getPhotoPath(){
        if (photoPath==null){
            photoPath = Environment.getExternalStorageDirectory().getPath() + "/test1/photo/";
            File file = new File(photoPath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return photoPath+ System.currentTimeMillis()+".jpg";
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_takephoto:
                if (isFoucs){
                    camera.takePicture(null,null,pcb);
                }else {
                    camera.autoFocus(afcb);
                }

                break;
        }
    }
    //释放相机资源
    private void releaseCamera(){
        if (camera!=null){
            camera.stopPreview();
            camera.release();
            camera=null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }
}
