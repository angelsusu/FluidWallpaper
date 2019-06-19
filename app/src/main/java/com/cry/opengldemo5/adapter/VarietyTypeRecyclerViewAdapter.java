package com.cry.opengldemo5.adapter;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liujinwei on 2018/7/17.
 */

public class VarietyTypeRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final String TAG = getClass().getSimpleName();

    private List<ItemViewDataWrapper> mItemDataList;
    private Context mContext;
    private ItemViewTypeManager mTypeHelperManager = null;

    public static class ItemViewDataWrapper<TData> {
        private RecyclerItemViewType mItemViewType;
        private TData mOriginData;

        public ItemViewDataWrapper(@NonNull TData data, @NonNull RecyclerItemViewType<TData> viewType) {
            mOriginData = data;
            this.mItemViewType = viewType;
        }

        public RecyclerItemViewType getItemViewType() {
            return mItemViewType;
        }

        public void setItemViewType(RecyclerItemViewType itemViewType) {
            this.mItemViewType = itemViewType;
        }

        public TData getOriginData() {
            return mOriginData;
        }

        public static<TData> List< ItemViewDataWrapper > createDataViewItemDataList(
            List<TData> dataList, @NonNull RecyclerItemViewType<TData> viewType) {

            if (null == dataList || dataList.size() == 0) {
                return null;
            }

            List< ItemViewDataWrapper > itemDataList = new ArrayList<>();
            for (TData item : dataList) {
                ItemViewDataWrapper itemData = new ItemViewDataWrapper<>(item, viewType);
                itemDataList.add(itemData);
            }

            return itemDataList;
        }
    }

    private static class ItemViewTypeManager {
        private List<RecyclerItemViewType> mTypeList = new ArrayList<>();
        public RecyclerItemViewType getViewTypeAt(int index) {
            return mTypeList.get(index);
        }

        public int getIndexOfType(RecyclerItemViewType viewType) {
            return mTypeList.indexOf(viewType);
        }

        public void addViewType(RecyclerItemViewType viewType) {
            if (-1 != mTypeList.indexOf(viewType)) {
                return;
            }

            mTypeList.add(viewType);
        }

        public boolean removeViewType(RecyclerItemViewType viewType) {
            return mTypeList.remove(viewType);
        }
    }

    public static abstract class RecyclerItemViewType<TData> {
        protected RecyclerView.Adapter  mRecyclerAdapter;
        protected int mLayoutId;
        protected Context mContext;

        protected Context getContext() {
            return mContext;
        }

        public RecyclerItemViewType(@NonNull Context context, int layoutId) {
            mContext = context;
            mLayoutId = layoutId;
        }

        protected abstract ViewHolder createViewHolder(View view);

        public void updateData(RecyclerView.Adapter adapter,
                               ViewHolder viewHolder, ItemViewDataWrapper<TData> itemData) {
            mRecyclerAdapter = adapter;
            updateData(viewHolder, itemData.getOriginData());
        }

        public abstract void updateData(ViewHolder viewHolder, TData itemData);

        public ViewHolder createViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(mContext).inflate(mLayoutId, parent,false);
            return createViewHolder(view);
        }
    }

    public VarietyTypeRecyclerViewAdapter(Context context) {
        mItemDataList = new ArrayList<>();
        mContext = context;
        mTypeHelperManager = new ItemViewTypeManager();
    }

    public void addViewType(RecyclerItemViewType viewType) {
        mTypeHelperManager.addViewType(viewType);
    }

    public boolean removeViewType(RecyclerItemViewType viewType) {
        return mTypeHelperManager.removeViewType(viewType);
    }

    public void setListData(@NonNull List<ItemViewDataWrapper> dataList) {
        if (null == dataList) {
            return;
        }

        mItemDataList = dataList;
    }

    public void clearData() {
        if (null == mItemDataList) {
            return;
        }
        mItemDataList.clear();
    }

    public List<ItemViewDataWrapper> getListData() {
        return mItemDataList;
    }

    @MainThread
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        RecyclerItemViewType viewType = mTypeHelperManager.getViewTypeAt(position);
        return viewType.createViewHolder(viewGroup);
    }

    @MainThread
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ItemViewDataWrapper itemData = mItemDataList.get(position);
        RecyclerItemViewType typeHelper = itemData.getItemViewType();
        typeHelper.updateData(this, viewHolder, itemData);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            ItemViewDataWrapper data = mItemDataList.get(position);
            int type = mTypeHelperManager.getIndexOfType(data.getItemViewType());

            if (type == -1) {
                Log.e(TAG, "exception error : no view type --- " +
                    data.mOriginData.getClass().toString());
            }

            return type;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mItemDataList.size();
    }

    public ItemViewDataWrapper getItem(int pos) {
        if (mItemDataList == null) {
            return null;
        }
        return mItemDataList.get(pos);
    }

    public void addListData(@NonNull List<ItemViewDataWrapper> dataList, boolean isBottom) {
        int pos = 0;
        if (isBottom) {
            pos = mItemDataList.size();
        }
        mItemDataList.addAll(pos, dataList);
    }

    public void setItemData(int position, ItemViewDataWrapper dataWrapper) {
        if (dataWrapper == null) {
            return;
        }

        if (mItemDataList.size() <= position) {
            mItemDataList.add(dataWrapper);
        }

        mItemDataList.set(position, dataWrapper);
    }
}
