package com.magcomm.anim;

/**
 * Created by hucheng on 2017/11/30.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.magcomm.heartmeasure.R;

/**
 * Created by wuhaojie on 2016/7/19 20:39.
 */
public class MultiScrollNumber extends LinearLayout {
    private Context mContext;
    private List<Integer> mTargetNumbers = new ArrayList<Integer>();
    private List<Integer> mPrimaryNumbers = new ArrayList<Integer>();
    private List<ScrollNumber> mScrollNumbers = new ArrayList<ScrollNumber>();
    private int mTextSize = 130;

    private int[] mTextColors = new int[]{R.color.green01};
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private String mFontFileName;
    private int mVelocity = 15;

    public MultiScrollNumber(Context context) {
        this(context, null);
    }

    public MultiScrollNumber(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiScrollNumber(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.MultiScrollNumber);
        int primaryNumber = typedArray.getInteger(R.styleable.MultiScrollNumber_primary_number, 0);
        int targetNumber = typedArray.getInteger(R.styleable.MultiScrollNumber_target_number, 0);
        int numberSize = typedArray.getInteger(R.styleable.MultiScrollNumber_number_size, 130);

        setNumber(primaryNumber, targetNumber);
        setTextSize(numberSize);

        typedArray.recycle();

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

    }

    public void setNumber(double num) {
        if (num < 0) throw new IllegalArgumentException("number value should >= 0");
        resetView();

        String str = String.valueOf(num);
        char[] charArray = str.toCharArray();
        for (int i = charArray.length - 1; i >= 0; i--) {
            if (Character.isDigit(charArray[i])) {
                mTargetNumbers.add(charArray[i] - '0');
            } else {
                mTargetNumbers.add(-1);
            }
        }

        for (int i = mTargetNumbers.size() - 1; i >= 0; i--) {
            if (mTargetNumbers.get(i) != -1) {
                ScrollNumber scrollNumber = new ScrollNumber(mContext);
                scrollNumber.setTextColor(Color.WHITE);
                scrollNumber.setVelocity(mVelocity);
                scrollNumber.setTextSize(mTextSize);
                scrollNumber.setInterpolator(mInterpolator);
                if (!TextUtils.isEmpty(mFontFileName))
                    scrollNumber.setTextFont(mFontFileName);
                scrollNumber.setNumber(0, mTargetNumbers.get(i), i * 10);
                mScrollNumbers.add(scrollNumber);
                addView(scrollNumber);

            } else {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                TextView point = new TextView(mContext);
                point.setText(" . ");
                point.setGravity(Gravity.BOTTOM);
                point.setTextColor(Color.WHITE);
                point.setTextSize(mTextSize / 3);
                addView(point, params);
            }
        }
    }

    public void setNumber(int val) {
        resetView();

        int number = val;
        if(number==0){
        	mTargetNumbers.add(0);
        }
        while (number > 0) {
            int i = number % 10;
            mTargetNumbers.add(i);
            number /= 10;
        }

        for (int i = mTargetNumbers.size() - 1; i >= 0; i--) {
            ScrollNumber scrollNumber = new ScrollNumber(mContext);
            scrollNumber.setTextColor(Color.WHITE);
            scrollNumber.setVelocity(mVelocity);
            scrollNumber.setTextSize(mTextSize);
            scrollNumber.setInterpolator(mInterpolator);
            if (!TextUtils.isEmpty(mFontFileName))
                scrollNumber.setTextFont(mFontFileName);
            scrollNumber.setNumber(0, mTargetNumbers.get(i), i * 10);
            mScrollNumbers.add(scrollNumber);
            addView(scrollNumber);
        }
        
    }

    private void resetView() {
        mTargetNumbers.clear();
        mScrollNumbers.clear();
        removeAllViews();
    }


    public void setNumber(int from, int to) {
        if (to < from)
            throw new UnsupportedOperationException("'to' value must > 'from' value");

        resetView();
        // operate to
        int number = to, count = 0;
        while (number > 0) {
            int i = number % 10;
            mTargetNumbers.add(i);
            number /= 10;
            count++;
        }
        // operate from
        number = from;
        while (count > 0) {
            int i = number % 10;
            mPrimaryNumbers.add(i);
            number /= 10;
            count--;
        }

        for (int i = mTargetNumbers.size() - 1; i >= 0; i--) {
            ScrollNumber scrollNumber = new ScrollNumber(mContext);
            scrollNumber.setTextColor(Color.WHITE);
            scrollNumber.setTextSize(mTextSize);
            if (!TextUtils.isEmpty(mFontFileName))
                scrollNumber.setTextFont(mFontFileName);
            scrollNumber.setNumber(mPrimaryNumbers.get(i), mTargetNumbers.get(i), i * 10);
            mScrollNumbers.add(scrollNumber);
            addView(scrollNumber);
        }

    }

    public void setTextColors(int[] textColors) {
        if (textColors == null || textColors.length == 0)
            throw new IllegalArgumentException("color array couldn't be empty!");
        mTextColors = textColors;
        for (int i = mScrollNumbers.size() - 1; i >= 0; i--) {
            ScrollNumber scrollNumber = mScrollNumbers.get(i);
            scrollNumber.setTextColor(Color.WHITE);
        }
    }


    public void setTextSize(int textSize) {
        if (textSize <= 0) throw new IllegalArgumentException("text size must > 0!");
        mTextSize = textSize;
        for (ScrollNumber s : mScrollNumbers) {
            s.setTextSize(textSize);
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null)
            throw new IllegalArgumentException("interpolator couldn't be null");
        mInterpolator = interpolator;
        for (ScrollNumber s : mScrollNumbers) {
            s.setInterpolator(interpolator);
        }
    }

    public void setTextFont(String fileName) {
        if (TextUtils.isEmpty(fileName)) throw new IllegalArgumentException("file name is null");
        mFontFileName = fileName;
        for (ScrollNumber s : mScrollNumbers) {
            s.setTextFont(fileName);
        }
    }

    public void setScrollVelocity(int velocity) {
        mVelocity = velocity;
        for (ScrollNumber s : mScrollNumbers) {
            s.setVelocity(velocity);
        }
    }


    private int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    private int sp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                dpVal, getResources().getDisplayMetrics());
    }

}
