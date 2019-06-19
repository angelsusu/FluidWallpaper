package com.cry.opengldemo5.wallpaper;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.cry.opengldemo5.R;

/**
 * Created by subeiting on 2019/6/19.
 */
public class PhotoAlbumActivity extends AppCompatActivity {
    private static final int PHOTO_REQUEST_GALLERY = 10;
    private static final int PHOTO_REQUEST_CUT = 11;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final int PHOTO_IMG = 1;
    private static final int PHOTO_VIDEO = 2;

    int mPhotoAlbumType = PHOTO_IMG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(PhotoAlbumActivity.this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        ImageView imageView = findViewById(R.id.image);
        imageView.setBackground(wallpaperDrawable);
    }

    private void initView() {
        findViewById(R.id.button3).setVisibility(View.GONE);
        Button button1 = findViewById(R.id.button1);
        button1.setText("选择图片");
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoAlbumType = PHOTO_IMG;
                choosePhoto();
            }
        });
        Button button2 = findViewById(R.id.button2);
        button2.setText("选择视频");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoAlbumType = PHOTO_VIDEO;
                choosePhoto();
            }
        });
    }

    private void choosePhoto() {
        if (ContextCompat.checkSelfPermission(PhotoAlbumActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //权限还没有授予，需要在这里写申请权限的代码
            // 第二个参数是一个字符串数组，里面是你需要申请的权限 可以设置申请多个权限
            // 最后一个参数是标志你这次申请的权限，该常量在onRequestPermissionsResult中使用到
            ActivityCompat.requestPermissions(PhotoAlbumActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            startChoosePhotoActivity();
        }
    }

    private void startChoosePhotoActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        String dataType = "";
        if (mPhotoAlbumType == PHOTO_IMG) {
            dataType = "image/*";
        } else if (mPhotoAlbumType == PHOTO_VIDEO) {
            dataType = "video/*";
        }
        intent.setType(dataType);
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            Uri imageUri = data.getData();
            if (imageUri == null) {
                return;
            }
            String path = getPath(this, imageUri);
            Log.d("Interested", path);
            if (mPhotoAlbumType == PHOTO_IMG) {
                startCropActivity(imageUri);
            } else if (mPhotoAlbumType == PHOTO_VIDEO) {
                //todo
            }
        } else if (requestCode == PHOTO_REQUEST_CUT) {
            Bitmap bitmap = data.getParcelableExtra("data");
            if (bitmap == null) {
                return;
            }
            ImageView imageView = findViewById(R.id.image);
            imageView.setBackground(null);
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto();
            } else {
                // Permission Denied
                Toast.makeText(PhotoAlbumActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
     * 剪切图片
     */
    private void startCropActivity(Uri uri) {
        // 裁剪图片intent
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 405);
        intent.putExtra("aspectY", 720);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 405);
        intent.putExtra("outputY", 720);

        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}
