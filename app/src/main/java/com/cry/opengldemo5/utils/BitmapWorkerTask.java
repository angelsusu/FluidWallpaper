package com.cry.opengldemo5.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.cry.opengldemo5.view.MainApplication;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfo;
import com.cry.opengldemo5.wallpaper.LiveWallpaperInfoManager;

/**
 * Created by xieguohua on 2019/6/26.
 */
public class BitmapWorkerTask extends AsyncTask<Void, Integer, Bitmap> {

    private BitmapWorkerListener mBitmapWorkerListener;

    public BitmapWorkerTask(BitmapWorkerListener listener) {
        super();
        mBitmapWorkerListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (mBitmapWorkerListener != null && bitmap != null) {
            mBitmapWorkerListener.onSuccess(bitmap);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        LiveWallpaperInfo wallpaperInfo = LiveWallpaperInfoManager.getInstance().getCurrentWallpaperInfo();
        if (wallpaperInfo == null) {
            return null;
        }

        Bitmap bitmap = getBitmap(wallpaperInfo);
        Log.d("data", "bitmapSize: " + bitmap.getByteCount());
        String text = wallpaperInfo.mWallpaperText;
        if (!TextUtils.isEmpty(text)) {
            android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
            // set default bitmap config if none
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            bitmap = bitmap.copy(bitmapConfig, true);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(255, 255, 255));
            paint.setTextSize(90);
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            int x = (bitmap.getWidth() - bounds.width()) / 2;
            int y = (bitmap.getHeight() + bounds.height()) / 2;
            canvas.drawText(text, x, y, paint);
        }
        return bitmap;
    }

    private Bitmap getBitmap(LiveWallpaperInfo wallpaperInfo) {
        Bitmap bitmap;
        Resources resources = MainApplication.INSTANCE.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (wallpaperInfo.mSource == LiveWallpaperInfo.Source.SOURCE_ASSETS) {
            BitmapFactory.decodeResource(resources, wallpaperInfo.mResourcesId, options);
            options.inSampleSize = calSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeResource(resources, wallpaperInfo.mResourcesId, options);
        } else {
            BitmapFactory.decodeFile(wallpaperInfo.mPath, options);
            options.inSampleSize = calSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(wallpaperInfo.mPath, options);
        }
        return bitmap;
    }

    private int calSampleSize(BitmapFactory.Options options, int width, int height) {
        int rawWidth = options.outWidth;
        int rawHeight = options.outHeight;
        int inSampleSize = 1;
        if (rawHeight > height || rawWidth > width) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > height && (halfWidth / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize <= 0 ? 1 : inSampleSize;
    }

    public interface BitmapWorkerListener {
        void onSuccess(Bitmap bitmap);
    }
}
