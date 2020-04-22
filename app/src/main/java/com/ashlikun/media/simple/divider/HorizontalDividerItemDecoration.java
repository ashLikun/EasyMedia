package com.ashlikun.media.simple.divider;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


/**
 * Created by yqritc on 2015/01/15.
 */
public class HorizontalDividerItemDecoration extends FlexibleDividerDecoration {

    private MarginProvider mMarginProvider;

    protected HorizontalDividerItemDecoration(Builder builder) {
        super(builder);
        mMarginProvider = builder.mMarginProvider;
    }

    @Override
    protected Rect getDividerBound(int position, RecyclerView parent, View child) {
        Rect bounds = new Rect(0, 0, 0, 0);
        int transitionX = (int) child.getTranslationX();
        int transitionY = (int) child.getTranslationY();
        int dividerSize = getDividerSize(position, parent);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        bounds.left = child.getLeft() - params.leftMargin + mMarginProvider.dividerLeftMargin(position, parent);
        bounds.right = child.getRight() + params.rightMargin + dividerSize - mMarginProvider.dividerRightMargin(position, parent);


        boolean isReverseLayout = isReverseLayout(parent);
        if (mDividerType == DividerType.DRAWABLE) {
            // set top and bottom position of divider
            if (isReverseLayout) {
                bounds.bottom = child.getTop() - params.topMargin + transitionY;
                bounds.top = bounds.bottom - dividerSize;
            } else {
                bounds.top = child.getBottom() + params.bottomMargin + transitionY;
                bounds.bottom = bounds.top + dividerSize;
            }
        } else {
            // set center point of divider
            int halfSize = dividerSize / 2;
            if (isReverseLayout) {
                bounds.top = child.getTop() - params.topMargin - halfSize + transitionY;
            } else {
                bounds.top = child.getBottom() + params.bottomMargin + halfSize + transitionY;
            }
            bounds.bottom = bounds.top;
        }

        if (mPositionInsideItem) {
            if (isReverseLayout) {
                bounds.top += dividerSize;
                bounds.bottom += dividerSize;
            } else {
                bounds.top -= dividerSize;
                bounds.bottom -= dividerSize;
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
        if (isLastRaw(parent, position, getSpanCount(parent), childCount))// 如果是最后一行，则不需要绘制底部
        {
            outRect.set(0, 0, 0, 0);
        } else if (isReverseLayout(parent)) {
            outRect.set(0, 0, getDividerSize(position, parent), 0);
        } else {
            outRect.set(0, 0, 0, getDividerSize(position, parent));
        }
    }

    private int getDividerSize(int position, RecyclerView parent) {
        if (mPaintProvider != null) {
            return (int) mPaintProvider.dividerPaint(position, parent).getStrokeWidth();
        } else if (mSizeProvider != null) {
            return mSizeProvider.dividerSize(position, parent);
        } else if (mDrawableProvider != null) {
            Drawable drawable = mDrawableProvider.drawableProvider(position, parent);
            return drawable.getIntrinsicHeight();
        }
        throw new RuntimeException("failed to get size");
    }

    /**
     * Interface for controlling divider margin
     */
    public interface MarginProvider {

        /**
         * Returns left margin of divider.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return left margin
         */
        int dividerLeftMargin(int position, RecyclerView parent);

        /**
         * Returns right margin of divider.
         *
         * @param position Divider position (or group index for GridLayoutManager)
         * @param parent   RecyclerView
         * @return right margin
         */
        int dividerRightMargin(int position, RecyclerView parent);
    }

    public static class Builder extends FlexibleDividerDecoration.Builder<Builder> {

        private MarginProvider mMarginProvider = new MarginProvider() {
            @Override
            public int dividerLeftMargin(int position, RecyclerView parent) {
                return 0;
            }

            @Override
            public int dividerRightMargin(int position, RecyclerView parent) {
                return 0;
            }
        };

        public Builder(Context context) {
            super(context);
        }

        public Builder margin(final int leftMargin, final int rightMargin) {
            return marginProvider(new MarginProvider() {
                @Override
                public int dividerLeftMargin(int position, RecyclerView parent) {
                    return leftMargin;
                }

                @Override
                public int dividerRightMargin(int position, RecyclerView parent) {
                    return rightMargin;
                }
            });
        }

        public Builder margin(int horizontalMargin) {
            return margin(horizontalMargin, horizontalMargin);
        }

        public Builder marginResId(@DimenRes int leftMarginId, @DimenRes int rightMarginId) {
            return margin(mResources.getDimensionPixelSize(leftMarginId),
                    mResources.getDimensionPixelSize(rightMarginId));
        }

        public Builder marginResId(@DimenRes int horizontalMarginId) {
            return marginResId(horizontalMarginId, horizontalMarginId);
        }

        public Builder marginProvider(MarginProvider provider) {
            mMarginProvider = provider;
            return this;
        }

        public RecyclerView.ItemDecoration build() {
            checkBuilderParams();
            return new HorizontalDividerItemDecoration(this);
        }
    }

    protected boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                                int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            childCount = childCount - childCount % spanCount;
            if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                return true;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                    .getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount)
                    return true;
            } else
            // StaggeredGridLayoutManager 且横向滚动
            {
                // 如果是最后一行，则不需要绘制底部
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}