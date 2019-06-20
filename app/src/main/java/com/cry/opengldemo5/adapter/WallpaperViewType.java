package com.cry.opengldemo5.adapter;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.cry.opengldemo5.R;
import com.cry.opengldemo5.view.WallpaperPreviewActivity;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;

import java.io.IOException;

/**
 * Created by xieguohua on 2019/6/19.
 */
public class WallpaperViewType extends VarietyTypeRecyclerViewAdapter.RecyclerItemViewType<LiveWallpaperInfo> {

    ItemViewHolder mItemViewHolder;

    public WallpaperViewType(Context context) {
        super(context, R.layout.item_wallpaper);
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void updateData(RecyclerView.ViewHolder viewHolder, LiveWallpaperInfo itemData) {
        mItemViewHolder = (ItemViewHolder) viewHolder;
        mItemViewHolder.updateView(itemData);
    }

    public void onDestroy() {
        mItemViewHolder.onDestroy();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private SurfaceView mSurfaceView;
        private LiveWallpaperInfo mLiveWallpaperInfo;
        private MediaPlayer mPlayer = new MediaPlayer();

        private ItemViewHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.wallpaper_image);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WallpaperPreviewActivity.startWallpaperPreviewActivity(mContext, mLiveWallpaperInfo);
                }
            });
            mSurfaceView = view.findViewById(R.id.surface_view);
            mSurfaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WallpaperPreviewActivity.startWallpaperPreviewActivity(mContext, mLiveWallpaperInfo);
                }
            });
        }

        private void updateView(LiveWallpaperInfo itemData) {
            mLiveWallpaperInfo = itemData;
            if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(getBitmap(mLiveWallpaperInfo.mResourcesId));
            } else if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
                mSurfaceView.setVisibility(View.VISIBLE);
                //mImageView.setImageBitmap(getAssetsImage(itemData.mPath));
                mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        mPlayer.setSurface(holder.getSurface());
                        try {
                            AssetManager aManager = mContext.getAssets();AssetFileDescriptor fileDescriptor = aManager.openFd(mLiveWallpaperInfo.mPath);
                            mPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                            //循环播放我们的视频
                            mPlayer.setLooping(true);
                            mPlayer.setVolume(0, 0);
                            mPlayer.prepare();
                            mPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {

                    }
                });
            }
        }

        private Bitmap getBitmap(int id) {
            return BitmapFactory.decodeResource(mContext.getResources(), id);
        }

        private Bitmap getAssetsImage(String fileName) {
            Bitmap bitmap = null;
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                AssetFileDescriptor afd = mContext.getAssets().openFd(fileName);
                mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                bitmap = mmr.getFrameAtTime();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        private void onDestroy() {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
