package com.cry.opengldemo5.adapter;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.cry.opengldemo5.MyWallpaperService;
import com.cry.opengldemo5.R;
import com.cry.opengldemo5.wallpaper.WallpaperInfo;

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
                    if (mWallpaperInfo.mWallpaperType == WallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                        startImageWallpaperService();
                    }
                }
            });
        }

        private void updateView(WallpaperInfo itemData) {
            mWallpaperInfo = itemData;
            if (mWallpaperInfo.mWallpaperType == WallpaperInfo.WallpaperType.WALLPAPER_TYPE_IMAGE) {
                mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, itemData.mImgResId));
            } else {

            }
        }

        private void startImageWallpaperService() {
            Intent intent = new Intent(
                    WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(mContext, MyWallpaperService.class));
            mContext.startActivity(intent);
        }
    }
}
