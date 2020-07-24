package com.zf.card;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import kotlin.TypeCastException;

public class CardLayoutManager extends RecyclerView.LayoutManager {
    public CardLayoutManager() {

    }


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }


    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);


        for (int i = 0; i < getItemCount(); i++) {
            //这里就是从缓存里面取出
            View view = recycler.getViewForPosition(i);
            //对子View进行测量
            measureChild(view, 0, 0);
            //将View加入到RecyclerView中
            addView(view);

            android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                throw new TypeCastException("null cannot be cast to non-null type androidx.recyclerview.widget.RecyclerView.LayoutParams");
            }

            int left =(getItemCount() - 1 - i) * 15;
            int top = (int)((double)(view.getMeasuredHeight() * i) * 0.15);
            int right = view.getMeasuredWidth()  - (getItemCount() - 1 - i) * 15;
            int bottom = top + view.getMeasuredHeight();
            this.layoutDecorated(view, left, top, right, bottom);
        }
    }
}
