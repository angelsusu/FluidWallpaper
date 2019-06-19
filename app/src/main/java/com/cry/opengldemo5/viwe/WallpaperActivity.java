package com.cry.opengldemo5.viwe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cry.opengldemo5.R;
import com.cry.opengldemo5.adapter.GridSpacingItemDecoration;
import com.cry.opengldemo5.adapter.VarietyTypeRecyclerViewAdapter;
import com.cry.opengldemo5.adapter.WallpaperViewType;
import com.cry.opengldemo5.wallpaper.WallpaperInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xieguohua on 2019/6/17.
 */
public class WallpaperActivity extends AppCompatActivity {

    public static final String WALLPAPER_TYPE = "wallpaperType";
    private RecyclerView mRecyclerView;
    private VarietyTypeRecyclerViewAdapter mAdapter;
    private WallpaperViewType mWallpaperViewType;
    private int mWallpaperType;
    private List<WallpaperInfo> mWallpaperInfoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        mWallpaperType = getIntent().getIntExtra(WALLPAPER_TYPE, 0);
        initData();
        initRecyclerView();
    }

    private void initData() {
        if (mWallpaperType == WallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
            mWallpaperInfoList.add(createImageWallpaperInfo(getBitmap(R.drawable.test_wallpaper_six)));
            mWallpaperInfoList.add(createImageWallpaperInfo(getBitmap(R.drawable.test_wallpaper_one)));
            mWallpaperInfoList.add(createImageWallpaperInfo(getBitmap(R.drawable.test_wallpaper_two)));
            mWallpaperInfoList.add(createImageWallpaperInfo(getBitmap(R.drawable.test_wallpaper_three)));
            mWallpaperInfoList.add(createImageWallpaperInfo(getBitmap(R.drawable.test_wallpaper_four)));
            mWallpaperInfoList.add(createImageWallpaperInfo(getBitmap(R.drawable.test_wallpaper_five)));
        } else if (mWallpaperType == WallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
            mWallpaperInfoList.add(createVideoWallpaperInfo("video/video1.mp4"));
            mWallpaperInfoList.add(createVideoWallpaperInfo("video/video2.mp4"));
            mWallpaperInfoList.add(createVideoWallpaperInfo("video/video3.mp4"));
            mWallpaperInfoList.add(createVideoWallpaperInfo("video/video4.mp4"));
            mWallpaperInfoList.add(createVideoWallpaperInfo("video/video5.mp4"));
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
        for (WallpaperInfo item : mWallpaperInfoList) {
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

    private WallpaperInfo createImageWallpaperInfo(Bitmap bitmap) {
        return WallpaperInfo.createImageWallpaperInfo(bitmap);
    }

    private WallpaperInfo createVideoWallpaperInfo(String videoName) {
        return WallpaperInfo.createVideoWallpaperInfo(videoName, WallpaperInfo.VideoSource.VIDEOSOURCE_ASSETS);
    }

    private Bitmap getBitmap(int imgResId) {
        return BitmapFactory.decodeResource(getResources(), imgResId);
    }
}
