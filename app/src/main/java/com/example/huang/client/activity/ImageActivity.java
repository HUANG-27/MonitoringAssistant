package com.example.huang.client.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import com.example.huang.client.R;
import com.example.huang.client.entity.ImageData;
import com.example.huang.client.tool.ImageTool;
import com.example.huang.client.tool.LocateTool;
import com.example.huang.client.tool.SensorTool;
import com.example.huang.client.config.App2;
import com.example.huang.client.tool.TimeTool;

public class ImageActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private RelativeLayout layoutTakePicture;
    private TextureView textureView;

    private LinearLayout layoutViewPicture;
    private ImageView imageViewViewPicture;

    private Camera camera;
    private static ImageData imageData;
    private Bitmap bitmap;
    private final int DEFAULT_CAMERA_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        initView();
        textureView.setSurfaceTextureListener(this);
    }

    // 初始化View
    private void initView() {
        textureView = (TextureView) findViewById(R.id.textureView_viewPicture);

        layoutTakePicture = (RelativeLayout) findViewById(R.id.layout_takePicture);
        layoutTakePicture.setVisibility(View.VISIBLE);
        //拍照
        ImageButton imageButtonTakePhoto = (ImageButton) findViewById(R.id.imageButton_takePicture);
        imageButtonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        //闪光灯
        final ImageButton imageButtonFlash = (ImageButton) findViewById(R.id.imageButton_flash);
        imageButtonFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String) v.getTag();
                switch (tag) {
                    case "auto":
                        v.setTag("off");
                        v.setBackgroundResource(R.drawable.icon_flash_off);
                        setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        break;
                    case "off":
                        v.setTag("on");
                        v.setBackgroundResource(R.drawable.icon_flash_on);
                        setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        break;
                    case "on":
                        v.setTag("auto");
                        v.setBackgroundResource(R.drawable.icon_flash_auto);
                        setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        break;
                }
            }
        });

        layoutViewPicture = (LinearLayout) findViewById(R.id.layout_viewPicture);
        layoutViewPicture.setVisibility(View.INVISIBLE);
        //预览图片
        imageViewViewPicture = (ImageView) findViewById(R.id.imageView_viewPicture);
        //确定
        Button imageButtonOkay = (Button) findViewById(R.id.imageButton_pictureOk);
        imageButtonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存并返回图片
                ImageTool.saveImage(bitmap, imageData.getFileName());
                App2.uploadData = imageData;
                setResult(RESULT_OK);
                finish();
            }
        });
        //取消重拍
        Button imageButtonCancelPhoto = (Button) findViewById(R.id.imageButton_pictureCancel);
        imageButtonCancelPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageData = null;
                layoutViewPicture.setVisibility(View.INVISIBLE);
                layoutTakePicture.setVisibility(View.VISIBLE);
                camera.startPreview();
            }
        });
    }

    // 拍照
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void takePicture() {
        imageData = new ImageData();
        imageData.setMonitor(App2.monitor);
        imageData.setTarget(App2.focusTarget);
        imageData.setTime(LocalDateTime.now());
        imageData.setFileName(App2.appDataFolder + File.separatorChar
                + TimeTool.formatDateTime(imageData.getTime()) + ".mp4");
        //位置数据
        imageData.setLocation(LocateTool.myLocation);
        imageData.setOrientation(SensorTool.getOrientation());

        if (camera != null) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    camera.stopPreview();
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    bitmap = ImageTool.rotateBitmapByDegree(bitmap, 90);
                    //打开预览
                    layoutViewPicture.setVisibility(View.VISIBLE);
                    layoutTakePicture.setVisibility(View.INVISIBLE);
                    //预览
                    imageViewViewPicture.setImageBitmap(bitmap);
                }
            });
        }
    }

    private void setFlashMode(String mode) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(mode);
            camera.setParameters(parameters);
        }
    }

    private void initCamera(SurfaceTexture surfaceTexture) {
        try {
            camera = Camera.open(DEFAULT_CAMERA_ID);
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModesList = parameters.getSupportedFocusModes();
            if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);     //设置相机闪光为自动

            int width = textureView.getWidth();
            int height = textureView.getHeight();
            List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
            Camera.Size optionPreviewSize = getOptimalSize(previewSizeList, height, width);
            parameters.setPreviewSize(optionPreviewSize.width, optionPreviewSize.height);
            List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
            Camera.Size optionPictureSize = getOptimalSize(pictureSizeList, height, width);
            parameters.setPictureSize(optionPictureSize.width, optionPictureSize.height);

            camera.setParameters(parameters);

            camera.setDisplayOrientation(90);
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        try {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        initCamera(surface);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releaseCamera();
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
