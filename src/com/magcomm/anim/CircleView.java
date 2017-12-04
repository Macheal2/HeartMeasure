package com.magcomm.anim;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class CircleView extends View {
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private float mMinSize;
    private float mMaxSize;
    private ScheduledExecutorService scheduledExecutor;
    private int mCurrent;
    private Paint mRotePaint;
    public final static int CIRCLE_NUM = 100;
    private int mCircleLength = 33;
    public final static int TIME = 10;
    private final static int PADDING = 80;
    float mSize;
    private int mRate=10;
    Context mContext;
    private boolean mIsRun;
    //private SweepGradient mSweepGradient;
    //private Matrix mMatrix = new Matrix();
    //private int mRotate;
    //private int[] mGradientColors = {Color.GREEN, Color.YELLOW, Color.RED};
    //private PathEffect effect;

    public CircleView(Context context) {
        super(context);
        initPaint(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context);
    }

    private void initPaint(Context context) {
        mContext = context;
        mPaint = new Paint();
        mRotePaint = new Paint();
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);
        mRotePaint.setStrokeWidth(4);
        mRotePaint.setAntiAlias(true);
        mRotePaint.setStyle(Paint.Style.FILL);
        mRotePaint.setStrokeJoin(Paint.Join.ROUND);
        mRotePaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.parseColor("#d0d0d0"));
        mRotePaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mRotePaint.setAntiAlias(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        int min = Math.min(mWidth, mHeight);
        mMaxSize = (float) min / 2 - 12f;
        mMinSize = mMaxSize - 30f;
        //mSweepGradient = new SweepGradient(mWidth/2, mHeight/2, mGradientColors, null);
       // effect = new DashPathEffect(new float[] { 8, 20, 8, 20}, 1);
       // mPaint.setShader(mSweepGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*mRotate+=3;
        mMatrix.setRotate(mRotate,mWidth/2,mHeight/2);
        mPaint.setPathEffect(effect);
        if(mRotate>=360){
            mRotate=0;
        }
        canvas.drawCircle(mWidth/2,mHeight/2,mMinSize,mPaint);
        */
        for (int i = 0; i < CIRCLE_NUM; i++) {
            int startX = (int) (mMinSize * Math.sin(2 * i * Math.PI / CIRCLE_NUM)) + mWidth / 2;
            int endX = (int) (mMaxSize * Math.sin(2 * i * Math.PI / CIRCLE_NUM)) + mWidth / 2;
            int startY = mHeight / 2 - (int) (mMinSize * Math.cos(2 * i * Math.PI / CIRCLE_NUM));
            int endY = mHeight / 2 - (int) (mMaxSize * Math.cos(2 * i * Math.PI / CIRCLE_NUM));
            if (i < mCurrent * 2 / TIME) {
                canvas.drawLine(startX, startY, endX, endY, mRotePaint);
            } else {
                canvas.drawLine(startX, startY, endX, endY, mPaint);
            }
        }
        if (mCurrent * 2 / TIME == CIRCLE_NUM) {
            setStop();
            return;
        }
        if (mIsRun) {
            if (mWidth - mSize - PADDING > PADDING) {
                int start=mWidth-PADDING-mCurrent*mRate;
                if(mWidth-PADDING-mCurrent*mRate<PADDING){
                    start=PADDING;
                }
                canvas.drawLine(start, mHeight / 2, mWidth - mSize - 80-4, mHeight / 2, mRotePaint);
            }
            if (mWidth - mSize + 360 - PADDING < PADDING) {
                canvas.drawLine(PADDING, mHeight / 2, mWidth - 80, mHeight / 2, mRotePaint);
            } else if (mWidth - mSize - PADDING + 360 < mWidth - PADDING) {
                canvas.drawLine(mWidth - mSize - PADDING + 360, mHeight / 2, mWidth - PADDING, mHeight / 2, mRotePaint);
            }
            if (mWidth - mSize - PADDING >= mWidth - PADDING || mWidth - mSize - PADDING + 360 <=mWidth - PADDING) {
                canvas.drawCircle(mWidth - PADDING, mHeight / 2, 10f, mRotePaint);
            }
            if(mCurrent*20>1000){
                for (int i = 0; i < 360; i+=2) {
                    if (mWidth - mSize + i >2 * PADDING && mWidth - mSize + i - PADDING <=mWidth - PADDING) {
                        if (mWidth - mSize + i - PADDING == mWidth - PADDING) {
                            canvas.drawCircle(mWidth - PADDING, 100 * (float) Math.sin(Math.PI * i / 180) + mHeight / 2, 10f, mRotePaint);
                        } else {
                            canvas.drawCircle(mWidth - mSize + i - PADDING, 100 * (float) Math.sin(Math.PI * i / 180) + mHeight / 2, 2f, mRotePaint);
                        }
                    }
                }
            }
            if(mCurrent*20>1000){
                mSize += mRate;
            }
            if (mWidth - mSize + 360 < 0) {
                mSize = 0;
            }
            mCurrent++;
        }
    }

    public void setStart() {
        if (scheduledExecutor != null) {
            return;
        }
        mIsRun = true;
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                postInvalidate();
            }
        }, 0, 20, TimeUnit.MILLISECONDS);
    }

    public void setStop() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdownNow();
            scheduledExecutor = null;
            mContext.sendBroadcast(new Intent("STOP_CIRCLE"));
            mIsRun = false;
            mCurrent=0;
            mSize=0;
        }
    }

}
