package com.necer.ncalendar.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.necer.ncalendar.R;
import com.necer.ncalendar.listener.OnClickWeekViewListener;
import com.necer.ncalendar.utils.Attrs;
import com.necer.ncalendar.utils.Utils;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;


/**
 * Created by necer on 2017/8/25.
 * QQ群:127278900
 */

public class WeekView extends CalendarView {


    protected Map<String, Integer> focusMap;
    private OnClickWeekViewListener mOnClickWeekViewListener;
    private List<String> lunarList;
    private Rect focusRect;
    private Bitmap focusBitmap;
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (int i = 0; i < mRectList.size(); i++) {
                Rect rect = mRectList.get(i);
                if (rect.contains((int) e.getX(), (int) e.getY())) {
                    LocalDate selectDate = dates.get(i);
                    mOnClickWeekViewListener.onClickCurrentWeek(selectDate);
                    break;
                }
            }
            return true;
        }
    });

    public WeekView(Context context, LocalDate date, OnClickWeekViewListener onClickWeekViewListener) {
        super(context);

        this.mInitialDate = date;
        Utils.NCalendar weekCalendar2 = Utils.getWeekCalendar2(date, Attrs.firstDayOfWeek);

        dates = weekCalendar2.dateList;
        lunarList = weekCalendar2.lunarList;
        mOnClickWeekViewListener = onClickWeekViewListener;
        focusBitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.icon_focus)).getBitmap();
        focusRect = new Rect(0, 0, focusBitmap.getWidth(), focusBitmap.getHeight());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        //mHeight = getHeight();
        //为了与月日历保持一致，往上压缩一下,5倍的关系
        mHeight = (int) (getHeight() - Utils.dp2px(getContext(), 2));
        mRectList.clear();

        for (int i = 0; i < 7; i++) {
            Rect rect = new Rect(i * mWidth / 7, 0, i * mWidth / 7 + mWidth / 7, mHeight);
            mRectList.add(rect);
            LocalDate date = dates.get(i);
            Paint.FontMetricsInt fontMetrics = mSorlarPaint.getFontMetricsInt();
            int baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2 +2;

            if (Utils.isToday(date)) {
                if (date.equals(mSelectDate)) {
                    mSorlarPaint.setColor(mSelectCircleColor);
                    drawSelected(canvas, rect.centerX(), rect.centerY(), mCurrentDayColor, mSorlarPaint);
                    mSorlarPaint.setColor(Color.WHITE);
                }else{
                    mSorlarPaint.setColor(mCurrentDayColor);//当天颜色
                }
            } else if (mSelectDate != null && date.equals(mSelectDate)) {
                mSorlarPaint.setColor(mSelectCircleColor);
                drawSelected(canvas, rect.centerX(), rect.centerY(), mSelectCircleColor, mSorlarPaint);
                mSorlarPaint.setColor(Color.WHITE);
            } else {
                mSorlarPaint.setColor(mSolarTextColor);
                //绘制关注
                drawFocus(canvas, rect, date, baseline);
                //绘制圆点
                drawPoint(canvas, rect, date, baseline);
            }
            canvas.drawText(date.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
        }
    }

    private void drawSelected(Canvas canvas, int centerX, int centerY, int color, Paint paint) {
        int left, top, right, bottom;
        centerY += 7;
        int l = 40;
        top = centerY - l;
        bottom = centerY + l;
        left = centerX - l;
        right = centerX + l;

        //新建矩形r2
        RectF r2 = new RectF();
        r2.left = left;
        r2.right = right;
        r2.top = top;
        r2.bottom = bottom;
        Log.i("--------", centerY + ": " + bottom);
        Log.i("--------", r2.toString());
        //画出圆角矩形r2
        paint.setColor(color);
        canvas.drawRoundRect(r2, 10, 10, paint);


    }

    private void drawFocus(Canvas canvas, Rect rect, LocalDate date, int baseline) {
        String dayTime = date.toString("yyyy-MM-dd");
        Integer size = null;
        if (focusMap != null) {
            size = focusMap.get(dayTime);
        }
        if (size == null) {
            size = 0;
        } else if (size > 3) {
            size = 3;
        }
        size = 1;
        if (size > 0) {
            int left = rect.centerX() + rect.width() / 4 - 5;
            int top = baseline - getHeight() / 3 - 10;
            int right = left + focusRect.width();
            int bottom = top + focusRect.height();
            Rect desR = new Rect(left, top, right, bottom);
            mLunarPaint.setColor(mHolidayColor);
            canvas.drawBitmap(focusBitmap, focusRect, desR, mLunarPaint);
        }
    }

    private void drawLunar(Canvas canvas, Rect rect, int baseline, int i) {
        if (isShowLunar) {
            mLunarPaint.setColor(mLunarTextColor);
            String lunar = lunarList.get(i);
            canvas.drawText(lunar, rect.centerX(), baseline + getHeight() / 4, mLunarPaint);
        }
    }

    private void drawHolidays(Canvas canvas, Rect rect, LocalDate date, int baseline) {
        if (isShowHoliday) {
            if (holidayList.contains(date.toString())) {
                mLunarPaint.setColor(mHolidayColor);
                canvas.drawText("休", rect.centerX() + rect.width() / 4, baseline - getHeight() / 4, mLunarPaint);

            } else if (workdayList.contains(date.toString())) {
                mLunarPaint.setColor(mWorkdayColor);
                canvas.drawText("班", rect.centerX() + rect.width() / 4, baseline - getHeight() / 4, mLunarPaint);
            }
        }
    }

    public void drawPoint(Canvas canvas, Rect rect, LocalDate date, int baseline) {
        if (pointList != null && pointList.contains(date.toString())) {
            mLunarPaint.setColor(mPointColor);
            canvas.drawCircle(rect.centerX(), baseline + getHeight() / 3 -12, mPointSize, mLunarPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public boolean contains(LocalDate date) {
        return dates.contains(date);
    }

    public void setFocusMap(Map<String, Integer> focusMap) {
        this.focusMap = focusMap;
        invalidate();
    }

}
