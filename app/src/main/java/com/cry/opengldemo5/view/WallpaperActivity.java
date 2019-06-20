package com.cry.opengldemo5.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cry.opengldemo5.R;
import com.cry.opengldemo5.adapter.GridSpacingItemDecoration;
import com.cry.opengldemo5.adapter.VarietyTypeRecyclerViewAdapter;
import com.cry.opengldemo5.adapter.WallpaperViewType;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfoManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xieguohua on 2019/6/17.
 */
public class WallpaperActivity extends AppCompatActivity {

    public static final String WALLPAPER_TYPE = "wallpaperType";
    private final String CROP_IMAGE_NAME = "crop.jpg";
    private RecyclerView mRecyclerView;
    private VarietyTypeRecyclerViewAdapter mAdapter;
    private WallpaperViewType mWallpaperViewType;
    private int mWallpaperType;
    private List<LiveWallpaperInfo> mLiveWallpaperInfoList = new ArrayList<>();

    private static final int PHOTO_REQUEST_GALLERY = 10;
    private static final int PHOTO_REQUEST_CUT = 11;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Uri uriTempFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        mWallpaperType = getIntent().getIntExtra(WALLPAPER_TYPE, 0);
        initData();
        initView();
        initRecyclerView();
    }

    private void initView() {
        Button button = findViewById(R.id.open_button);
        button.setText(mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE ?
                "从相册选择图片" : "从相册选择视频");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
    }

    private void initData() {
        if (mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.wallpaper1));
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.wallpaper2));
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.test_wallpaper_six));
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.test_wallpaper_one));
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.test_wallpaper_two));
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.wallpaper4));
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.test_wallpaper_three));
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.test_wallpaper_four));
            mLiveWallpaperInfoList.add(createImageWallpaperInfo(R.drawable.test_wallpaper_five));
        } else if (mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
            mLiveWallpaperInfoList.add(createVideoWallpaperInfo("video/video1.mp4"));
            mLiveWallpaperInfoList.add(createVideoWallpaperInfo("video/video2.mp4"));
        }
    }

    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 40, true));
        mAdapter = new VarietyTypeRecyclerViewAdapter(this);
        mWallpaperViewType = new WallpaperViewType(this);
        mAdapter.addViewType(mWallpaperViewType);
        mRecyclerView.setAdapter(mAdapter);

        List<VarietyTypeRecyclerViewAdapter.ItemViewDataWrapper> data = new ArrayList<>();
        for (LiveWallpaperInfo item : mLiveWallpaperInfoList) {
            data.add(new VarietyTypeRecyclerViewAdapter.ItemViewDataWrapper(item, mWallpaperViewType));
        }
        mAdapter.setListData(data);
        mAdapter.notifyDataSetChanged();
    }

    public static void startWallpaperActivity(Context context, int wallpaperType) {
        Intent intent = new Intent(context, WallpaperActivity.class);
        intent.putExtra(WALLPAPER_TYPE, wallpaperType);
        context.startActivity(intent);
    }

    private LiveWallpaperInfo createImageWallpaperInfo(int resourcesId) {
        return LiveWallpaperInfo.createImageWallpaperInfo(resourcesId, "", LiveWallpaperInfo.Source.SOURCE_ASSETS);
    }

    private LiveWallpaperInfo createVideoWallpaperInfo(String videoName) {
        return LiveWallpaperInfo.createVideoWallpaperInfo(videoName, LiveWallpaperInfo.Source.SOURCE_ASSETS);
    }

    private void choosePhoto() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //权限还没有授予，需要在这里写申请权限的代码
            // 第二个参数是一个字符串数组，里面是你需要申请的权限 可以设置申请多个权限
            // 最后一个参数是标志你这次申请的权限，该常量在onRequestPermissionsResult中使用到
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            startChoosePhotoActivity();
        }
    }

    private void startChoosePhotoActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        String dataType = "";
        if (mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
            dataType = "image/*";
        } else if (mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
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
            if (mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                startCropActivity(imageUri);
            } else if (mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
                LiveWallpaperInfo liveWallpaperInfo = LiveWallpaperInfo.
                        createVideoWallpaperInfo(path, LiveWallpaperInfo.Source.SOURCE_USER_ALBUM);
                WallpaperPreviewActivity.startWallpaperPreviewActivity(this, liveWallpaperInfo);
            }
        } else if (requestCode == PHOTO_REQUEST_CUT) {
            LiveWallpaperInfo liveWallpaperInfo =LiveWallpaperInfo.createImageWallpaperInfo(0,
                    uriTempFile.getPath(), LiveWallpaperInfo.Source.SOURCE_USER_ALBUM);
            WallpaperPreviewActivity.startWallpaperPreviewActivity(this, liveWallpaperInfo);
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
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
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
        // 裁剪框的比例
        intent.putExtra("aspectX", 405);
        intent.putExtra("aspectY", 720);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 405);
        intent.putExtra("outputY", 720);

        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        LiveWallpaperInfo liveWallpaperInfo =
                LiveWallpaperInfoManager.getInstance().getCurrentWallpaperInfo();
        if (liveWallpaperInfo != null && liveWallpaperInfo.mPath != null) {
            if (liveWallpaperInfo.mPath.contains(CROP_IMAGE_NAME)) {
                uriTempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "wallpaper.jpg");
            } else {
                uriTempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + CROP_IMAGE_NAME);
            }
        } else {
            uriTempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + CROP_IMAGE_NAME);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTempFile);
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
