package com.cry.opengldemo5.adapter;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.cry.opengldemo5.MyWallpaperService;
import com.cry.opengldemo5.R;
import com.cry.opengldemo5.VideoWallpaperService;
import com.cry.opengldemo5.wallpaper.WallpaperInfo;
import com.cry.opengldemo5.wallpaper.WallpaperInfoManager;

import java.io.IOException;

/**
 * Created by xieguohua on 2019/6/19.
 */
public class WallpaperViewType extends VarietyTypeRecyclerViewAdapter.RecyclerItemViewType<WallpaperInfo> {

    public WallpaperViewType(Context context) {
        super(context, R.layout.item_wallpaper);
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void updateData(RecyclerView.ViewHolder viewHolder, WallpaperInfo itemData) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        itemViewHolder.updateView(itemData);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private WallpaperInfo mWallpaperInfo;

        private ItemViewHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.image);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WallpaperInfoManager.getInstance().setCurrentWallpaperInfo(mWallpaperInfo);
                    if (mWallpaperInfo.mWallpaperType == WallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                        MyWallpaperService.startWallpaper(mContext);
                    } else {
                        VideoWallpaperService.startWallpaper(mContext);
                    }
                }
            });
        }

        private void updateView(WallpaperInfo itemData) {
            mWallpaperInfo = itemData;
            if (mWallpaperInfo.mWallpaperType == WallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                mImageView.setImageBitmap(itemData.mImgBitmap);
            } else if (mWallpaperInfo.mWallpaperType == WallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
                if (itemData.mVideoSource == WallpaperInfo.VideoSource.VIDEOSOURCE_ASSETS) {
                    mImageView.setImageBitmap(getAssetsImage(itemData.mVideoPath));
                }
            }
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
    }
}
