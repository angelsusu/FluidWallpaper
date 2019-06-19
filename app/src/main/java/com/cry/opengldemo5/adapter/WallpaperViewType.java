package com.cry.opengldemo5.adapter;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.cry.opengldemo5.MyWallpaperService;
import com.cry.opengldemo5.R;
import com.cry.opengldemo5.VideoWallpaperService;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfoManager;

import java.io.IOException;

/**
 * Created by xieguohua on 2019/6/19.
 */
public class WallpaperViewType extends VarietyTypeRecyclerViewAdapter.RecyclerItemViewType<LiveWallpaperInfo> {

    public WallpaperViewType(Context context) {
        super(context, R.layout.item_wallpaper);
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void updateData(RecyclerView.ViewHolder viewHolder, LiveWallpaperInfo itemData) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        itemViewHolder.updateView(itemData);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private LiveWallpaperInfo mLiveWallpaperInfo;

        private ItemViewHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.image);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LiveWallpaperInfoManager.getInstance().setCurrentWallpaperInfo(mLiveWallpaperInfo);
                    if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                        MyWallpaperService.startWallpaper(mContext);
                    } else {
                        VideoWallpaperService.startWallpaper(mContext);
                    }
                }
            });
        }

        private void updateView(LiveWallpaperInfo itemData) {
            mLiveWallpaperInfo = itemData;
            if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                mImageView.setImageBitmap(itemData.mImgBitmap);
            } else if (mLiveWallpaperInfo.mWallpaperType == LiveWallpaperInfo.WallpaperType.WALLPAPER_TYPE_VIDEO) {
                if (itemData.mVideoSource == LiveWallpaperInfo.VideoSource.VIDEOSOURCE_ASSETS) {
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
