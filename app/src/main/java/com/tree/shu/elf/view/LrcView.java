package com.tree.shu.elf.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.SeekBar;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 歌词
 * Created by wcy on 2015/11/9.
 */
public class LrcView extends View {
    private static final long ADJUST_DURATION = 100;
    private static final long TIMELINE_KEEP_TIME = 4 * DateUtils.SECOND_IN_MILLIS;

    private List<LrcEntry> mLrcEntryList = new ArrayList<>();
    private TextPaint mLrcPaint = new TextPaint();
    private TextPaint mTimePaint = new TextPaint();
    private Paint.FontMetrics mTimeFontMetrics;
    private Drawable mPlayDrawable;
    private float mDividerHeight;
    private long mAnimationDuration;
    private int mNormalTextColor;
    private int mCurrentTextColor;
    private int mTimelineTextColor;
    private int mTimelineColor;
    private int mTimeTextColor;
    private int mDrawableWidth;
    private int mTimeTextWidth;
    private String mDefaultLabel;
    private float mLrcPadding;
    private ValueAnimator mAnimator;
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private float mOffset;
    private int mCurrentLine;
    private Object mFlag;
    private boolean isTouching;
    private boolean isFling;


    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mDrawableWidth = 10;
        mTimeTextWidth = 10;

        mLrcPaint.setAntiAlias(true);
        mLrcPaint.setTextSize(14);
        mLrcPaint.setTextAlign(Paint.Align.LEFT);
        mTimePaint.setAntiAlias(true);
        mTimePaint.setTextSize(14);
        mTimePaint.setTextAlign(Paint.Align.CENTER);
        //noinspection SuspiciousNameCombination
        mTimePaint.setStrokeWidth(10);
        mTimePaint.setStrokeCap(Paint.Cap.ROUND);
        mTimeFontMetrics = mTimePaint.getFontMetrics();

        mScroller = new Scroller(getContext());
    }

    public void setNormalColor(int normalColor) {
        mNormalTextColor = normalColor;
        postInvalidate();
    }

    public void setCurrentColor(int currentColor) {
        mCurrentTextColor = currentColor;
        postInvalidate();
    }

    public void setTimelineTextColor(int timelineTextColor) {
        mTimelineTextColor = timelineTextColor;
        postInvalidate();
    }

    public void setTimelineColor(int timelineColor) {
        mTimelineColor = timelineColor;
        postInvalidate();
    }

    public void setTimeTextColor(int timeTextColor) {
        mTimeTextColor = timeTextColor;
        postInvalidate();
    }


    /**
     * 设置歌词为空时屏幕中央显示的文字，如“暂无歌词”
     */
    public void setLabel(final String label) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                mDefaultLabel = label;
                invalidate();
            }
        });
    }

    /**
     * 加载歌词文件
     *
     * @param lrcFile 歌词文件
     */
    public void loadLrc(final File lrcFile) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                reset();
                setFlag(lrcFile);
                new AsyncTask<File, Integer, List<LrcEntry>>() {
                    @Override
                    protected List<LrcEntry> doInBackground(File... params) {
                        return LrcEntry.parseLrc(params[0]);
                    }

                    @Override
                    protected void onPostExecute(List<LrcEntry> lrcEntries) {
                        if (getFlag() == lrcFile) {
                            onLrcLoaded(lrcEntries);
                            setFlag(null);
                        }
                    }
                }.execute(lrcFile);
            }
        });
    }

    /**
     * 加载歌词文件
     *
     * @param lrcText 歌词文本
     */
    public void loadLrc(final String lrcText) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                reset();

                setFlag(lrcText);
                new AsyncTask<String, Integer, List<LrcEntry>>() {
                    @Override
                    protected List<LrcEntry> doInBackground(String... params) {
                        return LrcEntry.parseLrc(params[0]);
                    }

                    @Override
                    protected void onPostExecute(List<LrcEntry> lrcEntries) {
                        if (getFlag() == lrcText) {
                            onLrcLoaded(lrcEntries);
                            setFlag(null);
                        }
                    }
                }.execute(lrcText);
            }
        });
    }

    /**
     * 歌词是否有效
     *
     * @return true，如果歌词有效，否则false
     */
    public boolean hasLrc() {
        return !mLrcEntryList.isEmpty();
    }

    /**
     * 刷新歌词
     *
     * @param time 当前播放时间
     */
    public void updateTime(final long time) {
        runOnUi(() -> {
            if (!hasLrc()) {
                return;
            }
            int line = findShowLine(time);
            if (line != mCurrentLine) {
                mCurrentLine = line;
                scrollTo(line);
            }
        });
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerY = getHeight() / 2;

        // 无歌词文件
        if (!hasLrc()) {
            return;
        }

        int centerLine = getCenterLine();


        canvas.translate(0, mOffset);

        float y = 0;
        for (int i = 0; i < mLrcEntryList.size(); i++) {
            if (i > 0) {
                y += (mLrcEntryList.get(i - 1).getHeight() + mLrcEntryList.get(i).getHeight()) / 2 + mDividerHeight;
            }
            if (i == mCurrentLine) {
                mLrcPaint.setColor(mCurrentTextColor);
            } else if (i == centerLine) {
                mLrcPaint.setColor(mTimelineTextColor);
            } else {
                mLrcPaint.setColor(mNormalTextColor);
            }
            drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), y);
        }
    }

    /**
     * 画一行歌词
     *
     * @param y 歌词中心 Y 坐标
     */
    private void drawText(Canvas canvas, StaticLayout staticLayout, float y) {
        canvas.save();
        canvas.translate(mLrcPadding, y - staticLayout.getHeight() / 2);
        staticLayout.draw(canvas);
        canvas.restore();
    }


    private Runnable hideTimelineRunnable = new Runnable() {
        @Override
        public void run() {
            if (hasLrc()) {
                scrollTo(mCurrentLine);
            }
        }
    };



    private void onLrcLoaded(List<LrcEntry> entryList) {
        if (entryList != null && !entryList.isEmpty()) {
            mLrcEntryList.addAll(entryList);
        }

        initEntryList();
        invalidate();
    }

    private void initEntryList() {
        if (!hasLrc() || getWidth() == 0) {
            return;
        }

        Collections.sort(mLrcEntryList);

        for (LrcEntry lrcEntry : mLrcEntryList) {
            lrcEntry.init(mLrcPaint, (int) getLrcWidth());
        }

        mOffset = getHeight() / 2;
    }

    private void reset() {
        endAnimation();
        mScroller.forceFinished(true);
        isTouching = false;
        isFling = false;
        removeCallbacks(hideTimelineRunnable);
        mLrcEntryList.clear();
        mOffset = 0;
        mCurrentLine = 0;
        invalidate();
    }

    /**
     * 滚动到某一行
     */
    private void scrollTo(int line) {
        scrollTo(line, mAnimationDuration);
    }


    private void scrollTo(int line, long duration) {
        float offset = getOffset(line);
        endAnimation();

        mAnimator = ValueAnimator.ofFloat(mOffset, offset);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();
    }

    private void endAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.end();
        }
    }

    /**
     * 二分法查找当前时间应该显示的行数（最后一个 <= time 的行数）
     */
    private int findShowLine(long time) {
        int left = 0;
        int right = mLrcEntryList.size();
        while (left <= right) {
            int middle = (left + right) / 2;
            long middleTime = mLrcEntryList.get(middle).getTime();

            if (time < middleTime) {
                right = middle - 1;
            } else {
                if (middle + 1 >= mLrcEntryList.size() || time < mLrcEntryList.get(middle + 1).getTime()) {
                    return middle;
                }

                left = middle + 1;
            }
        }
        return 0;
    }

    private int getCenterLine() {
        int centerLine = 0;
        float minDistance = Float.MAX_VALUE;
        for (int i = 0; i < mLrcEntryList.size(); i++) {
            if (Math.abs(mOffset - getOffset(i)) < minDistance) {
                minDistance = Math.abs(mOffset - getOffset(i));
                centerLine = i;
            }
        }
        return centerLine;
    }

    private float getOffset(int line) {
        if (mLrcEntryList.get(line).getOffset() == Float.MIN_VALUE) {
            float offset = getHeight() / 2;
            for (int i = 1; i <= line; i++) {
                offset -= (mLrcEntryList.get(i - 1).getHeight() + mLrcEntryList.get(i).getHeight()) / 2 + mDividerHeight;
            }
            mLrcEntryList.get(line).setOffset(offset);
        }

        return mLrcEntryList.get(line).getOffset();
    }

    private float getLrcWidth() {
        return getWidth() - mLrcPadding * 2;
    }

    private void runOnUi(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            post(r);
        }
    }

    private Object getFlag() {
        return mFlag;
    }

    private void setFlag(Object flag) {
        this.mFlag = flag;
    }
}
