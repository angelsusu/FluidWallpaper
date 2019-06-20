package com.cry.opengldemo5.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cry.opengldemo5.R;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        WallpaperManager wallpaperManager = WallpaperManager.getInstance(MainActivity.this);
//        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
//        ImageView imageView = findViewById(R.id.image);
//        imageView.setBackground(wallpaperDrawable);
    }

    private void initView() {
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperActivity.startWallpaperActivity(MainActivity.this,
                        LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE);
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperActivity.startWallpaperActivity(MainActivity.this,
                        LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO);
            }
        });
//        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, PhotoAlbumActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}
