package com.ashlikun.media.simple.divider;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


/**
 * Created by yqritc on 2015/01/15.
 */
public class VerticalDividerItemDecoration extends FlexibleDividerDecoration {

    private MarginProvider mMarginProvider;

    protected VerticalDividerItemDecoration(Builder builder) {
        super(builder);
        mShowLastDivider = true;
        mMarginProvider = builder.mMarginProvider;
    }

    @Override
    protected Rect getDividerBound(int position, RecyclerView parent, View child) {

        Rect bounds = new Rect(0, 0, 0, 0);
        int transitionX = (int) ViewCompat.getTranslationX(child);
        int transitionY = (int) ViewCompat.getTranslationY(child);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        bounds.top = child.getTop() - params.topMargin + mMarginProvider.dividerTopMargin(position, parent);
        bounds.bottom = child.getBottom() + params.bottomMargin - mMarginProvider.dividerBottomMargin(position, parent);


        int dividerSize = getDividerSize(position, parent);
        boolean isReverseLayout = isReverseLayout(parent);
        if (mDividerType == DividerType.DRAWABLE) {
            // set left and right position of divider
            if (isReverseLayout) {
                bounds.right = child.getLeft() - params.leftMargin + transitionX;
                bounds.left = bounds.right - dividerSize;
            } else {
                bounds.left = child.getRight() + params.rightMargin + transitionX;
                bounds.right = bounds.left + dividerSize;
            }
        } else {
            // set center point of divider
            int halfSize = dividerSize / 2;
            if (isReverseLayout) {
                bounds.left = child.getLeft() - params.leftMargin - halfSize + transitionX;
            } else {
                bounds.left = child.getRight() + params.rightMargin + halfSize + transitionX;
            }
            bounds.right = bounds.left;
        }

        if (mPositionInsideItem) {
            if (isReverseLayout) {
                bounds.left += dividerSize;
                bounds.right += dividerSize;
            } else {
                bounds.left -= dividerSize;
                bounds.right -= dividerSize;
            }
        }

        return bounds;
    }


    @Override
    protected void setItemOffsets(Rect outRect, int position, int childCount, RecyclerView parent) {
        if (mPositionInsideItem) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        int spanCount = getSpanCount(parent);//多少列
        if (spanCount <= 0) {
            outRect.set(0, 0, getDividerSize(position, parent), 0);
            return;
        }
        int spanIndex = getIndexColum(parent, position, spanCount, childCount);//当前第几列
        int dividerAllSize = 0;//总共大小
        for (int i = 0; i < spanCount - 1; i++) {
            int startPoi = position - spanIndex + 1;//当前列开始的第一个
            dividerAllSize += getDividerSize(startPoi + i, parent);
        }
        int itemDivSize = Math.round(dividerAllSize / (spanCount * 1f));//每个item应该大小
        if (spanIndex == spanCount)// 如果是最后一列，则不需要绘制右边
        {
            outRect.set(itemDivSize, 0, 0, 0);
            return;
        } else if (spanIndex == 1) {//第一列不绘制左边
            outRect.set(0, 0, itemDivSize, 0);
        } else {//中间的左右都绘制
            outRect.set(Math.round(itemDivSize / 2f), 0, Math.round(itemDivSize / 2f), 0);
        }


    }


    private int getDividerSize(int position, RecyclerView parent) {
        if (mPaintProvider != null) {
            return (int) mPaintProvider.dividerPaint(position, parent).getStrokeWidth();
        } else if (mSizeProvider != null) {
            return mSizeProvider.dividerSize(position, parent);
        } else if (mDrawableProvider != null) {
            Drawable drawable = mDrawableProvider.drawableProvider(position, parent);
            return drawable.getIntrinsicWidth();
        }
        throw new RuntimeException("failed to get size");
    }

    /**
     * Interface for controlling divider margin
     */
    public interface MarginProvider {

        /**
         * Returns top margin of divider.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return top margin
         */
        int dividerTopMargin(int position, RecyclerView parent);

        /**
         * Returns bottom margin of divider.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return bottom margin
         */
        int dividerBottomMargin(int position, RecyclerView parent);
    }

    public static class Builder extends FlexibleDividerDecoration.Builder<Builder> {

        private MarginProvider mMarginProvider = new MarginProvider() {
            @Override
            public int dividerTopMargin(int position, RecyclerView parent) {
                return 0;
            }

            @Override
            public int dividerBottomMargin(int position, RecyclerView parent) {
                return 0;
            }
        };

        public Builder(Context context) {
            super(context);
        }

        public Builder margin(final int topMargin, final int bottomMargin) {
            return marginProvider(new MarginProvider() {
                @Override
                public int dividerTopMargin(int position, RecyclerView parent) {
                    return topMargin;
                }

                @Override
                public int dividerBottomMargin(int position, RecyclerView parent) {
                    return bottomMargin;
                }
            });
        }

        public Builder margin(int verticalMargin) {
            return margin(verticalMargin, verticalMargin);
        }

        public Builder marginResId(@DimenRes int topMarginId, @DimenRes int bottomMarginId) {
            return margin(mResources.getDimensionPixelSize(topMarginId),
                    mResources.getDimensionPixelSize(bottomMarginId));
        }

        public Builder marginResId(@DimenRes int verticalMarginId) {
            return marginResId(verticalMarginId, verticalMarginId);
        }

        public Builder marginProvider(MarginProvider provider) {
            mMarginProvider = provider;
            return this;
        }

        public VerticalDividerItemDecoration build() {
            checkBuilderParams();
            return new VerticalDividerItemDecoration(this);
        }
    }

    //当前是第几列
    protected int getIndexColum(RecyclerView parent, int pos, int spanCount,
                                int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int posSpan = (pos + 1) % spanCount;
        if (layoutManager instanceof GridLayoutManager) {

            return posSpan == 0 ? spanCount : posSpan;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                return posSpan == 0 ? spanCount : posSpan;
            } else {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                    return spanCount;
            }
        }
        return spanCount;
    }
}