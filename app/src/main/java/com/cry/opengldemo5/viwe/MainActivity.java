package com.cry.opengldemo5.viwe;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.cry.opengldemo5.MyWallpaperService;
import com.cry.opengldemo5.R;
import com.cry.opengldemo5.wallpaper.WallpaperInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(MainActivity.this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        ImageView imageView = findViewById(R.id.image);
        imageView.setBackground(wallpaperDrawable);
    }

    private void initView() {
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWallpaper();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperActivity.startWallpaperActivity(MainActivity.this,
                        WallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE);
            }
        });
    }

    private void startWallpaper() {
        Intent intent = new Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, MyWallpaperService.class));
        startActivity(intent);
    }
}
