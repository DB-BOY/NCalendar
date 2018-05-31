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
import com.necer.ncalendar.listener.OnClickMonthViewListener;
import com.necer.ncalendar.utils.Attrs;
import com.necer.ncalendar.utils.Utils;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;


/**
 * Created by necer on 2017/8/25.
 * QQ群:127278900
 */

public class MonthView extends CalendarView {

    protected Map<String, Integer> focusMap;
    private List<String> lunarList;
    private int mRowNum;
    private OnClickMonthViewListener mOnClickMonthViewListener;
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
                    if (Utils.isLastMonth(selectDate, mInitialDate)) {
                        mOnClickMonthViewListener.onClickLastMonth(selectDate);
                    } else if (Utils.isNextMonth(selectDate, mInitialDate)) {
                        mOnClickMonthViewListener.onClickNextMonth(selectDate);
                    } else {
                        mOnClickMonthViewListener.onClickCurrentMonth(selectDate);
                    }
                    break;
                }
            }
            return true;
        }
    });

    private Rect focusRect;
    private Bitmap focusBitmap;

    //    private Rect todayRect;
    //    private Rect selectedRect;
    //    private Bitmap todayBitmap;
    //    private Bitmap selectedBitmap;
    public MonthView(Context context, LocalDate date, OnClickMonthViewListener onClickMonthViewListener) {
        super(context);
        this.mInitialDate = date;

        //0周日，1周一
        Utils.NCalendar nCalendar2 = Utils.getMonthCalendar2(date, Attrs.firstDayOfWeek);
        mOnClickMonthViewListener = onClickMonthViewListener;

        lunarList = nCalendar2.lunarList;
        dates = nCalendar2.dateList;

        mRowNum = dates.size() / 7;
        focusBitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.icon_focus)).getBitmap();
        focusRect = new Rect(0, 0, focusBitmap.getWidth(), focusBitmap.getHeight());

    }

    private int getCenterPoint(Rect rect) {
        return mRowNum == 5 ? rect.centerY() - 20 : (rect.centerY() + (mHeight / 5 - mHeight / 6) / 2) - 20;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        //绘制高度
        mHeight = getDrawHeight();
        mRectList.clear();
        Rect rect;
        int baseline;
        LocalDate date;
        for (int i = 0; i < mRowNum; i++) {
            for (int j = 0; j < 7; j++) {
                rect = new Rect((int) (j * mWidth / 7.0f), i * mHeight / mRowNum, (int) (j * mWidth / 7.0f + mWidth / 7.0f), i * mHeight / mRowNum + mHeight / mRowNum);
                mRectList.add(rect);
                /*
                 * centerY 与baseline对应
                 * baseline减小,centerY增加
                 * -20      +5
                 * -15      +10
                 * -10      +15
                 * -5       +20
                 * 0        +25
                 *
                 */

                int centerY = getCenterPoint(rect) + 25;
                date = dates.get(i * 7 + j);
                Paint.FontMetricsInt fontMetrics = mSorlarPaint.getFontMetricsInt();
                //让6行的第一行和5行的第一行在同一直线上，处理选中第一行的滑动
                if (mRowNum == 5) {
                    baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                } else {
                    baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2 + (mHeight / 5 - mHeight / 6) / 2;
                }
                //当月和上下月的颜色不同
                if (Utils.isEqualsMonth(date, mInitialDate)) {
                    //当天和选中的日期不绘制农历
                    if (Utils.isToday(date)) {
                        if (date.equals(mSelectDate)) {
                            mSorlarPaint.setColor(mSelectCircleColor);
                            drawSelected(canvas, rect.centerX(), centerY, mCurrentDayColor, mSorlarPaint);
                            mSorlarPaint.setColor(Color.WHITE);//选中变白
                        } else {
                            mSorlarPaint.setColor(mCurrentDayColor);//当天颜色
                        }
                    } else if (mSelectDate != null && date.equals(mSelectDate)) {
                        mSorlarPaint.setColor(mSelectCircleColor);
                        drawSelected(canvas, rect.centerX(), centerY, mSelectCircleColor, mSorlarPaint);
                        mSorlarPaint.setColor(Color.WHITE);
                    } else {
                        mSorlarPaint.setColor(mSolarTextColor);
                        //                        //绘制圆点
                        drawPoint(canvas, rect, date, baseline);
                        drawFocus(canvas, rect, date, baseline);
                    }
                    canvas.drawText(date.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                } else {
                    mSorlarPaint.setColor(mHintColor);
                    canvas.drawText(date.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                    //绘制关注
                    drawFocus(canvas, rect, date, baseline);
                    //绘制圆点
                    drawPoint(canvas, rect, date, baseline);
                }
            }
        }
    }

    private void drawSelected(Canvas canvas, int centerX, int centerY, int colorId, Paint paint) {
        //        canvas.drawCircle(rect.centerX(), baseline + getMonthHeight() / 20, mPointSize, mLunarPaint);
        int left, top, right, bottom;
        int l = 40;
        left = centerX -l;
        right = centerX +l;
        top  = centerY -l;
        bottom = centerY+l;
        
        //新建矩形r2
        RectF r2 = new RectF();
        r2.left = left;
        r2.right = right;
        r2.top = top;
        r2.bottom = bottom;
        Log.i("--------", centerY+": "+bottom);
        Log.i("--------", r2.toString());
        //画出圆角矩形r2
        paint.setColor(colorId);
        canvas.drawRoundRect(r2, 10, 10, paint);


    }

    /**
     * 月日历高度
     *
     * @return
     */
    public int getMonthHeight() {
        return Attrs.monthCalendarHeight;
    }

    /**
     * 月日历的绘制高度，
     * 为了月日历6行时，绘制农历不至于太靠下，绘制区域网上压缩一下
     *
     * @return
     */
    public int getDrawHeight() {
        return (int) (getMonthHeight() - Utils.dp2px(getContext(), 3));
    }

    private void drawLunar(Canvas canvas, Rect rect, int baseline, int color, int i, int j) {
        if (isShowLunar) {
            mLunarPaint.setColor(color);
            String lunar = lunarList.get(i * 7 + j);
            canvas.drawText(lunar, rect.centerX(), baseline + getMonthHeight() / 20, mLunarPaint);
        }
    }

    private void drawHolidays(Canvas canvas, Rect rect, LocalDate date, int baseline) {
        if (isShowHoliday) {
            if (holidayList.contains(date.toString())) {
                mLunarPaint.setColor(mHolidayColor);
                canvas.drawText("休", rect.centerX() + rect.width() / 4, baseline - getMonthHeight() / 20, mLunarPaint);

            } else if (workdayList.contains(date.toString())) {
                mLunarPaint.setColor(mWorkdayColor);
                canvas.drawText("班", rect.centerX() + rect.width() / 4, baseline - getMonthHeight() / 20, mLunarPaint);
            }
        }
    }

    //绘制圆点
    public void drawPoint(Canvas canvas, Rect rect, LocalDate date, int baseline) {
        if (pointList != null && pointList.contains(date.toString())) {
            mLunarPaint.setColor(mPointColor);
            canvas.drawCircle(rect.centerX(), baseline + getMonthHeight() / 20-2, mPointSize, mLunarPaint);
        }
    }

    /**
     * 绘制关注的点
     *
     * @param canvas
     * @param rect
     * @param date
     * @param baseline
     */
    public void drawFocus(Canvas canvas, Rect rect, LocalDate date, int baseline) {
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
            int left = rect.centerX() + rect.width() / 4 -5;
            int top = baseline - getMonthHeight() / 20 -20;
            int right = left + focusRect.width();
            int bottom = top + focusRect.height();
            Rect desR = new Rect(left, top, right, bottom);
            mLunarPaint.setColor(mHolidayColor);
            canvas.drawBitmap(focusBitmap, focusRect, desR, mLunarPaint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public int getRowNum() {
        return mRowNum;
    }

    public int getSelectRowIndex() {
        if (mSelectDate == null) {
            return 0;
        }
        int indexOf = dates.indexOf(mSelectDate);
        return indexOf / 7;
    }

    public void setFocusMap(Map<String, Integer> focusMap) {
        this.focusMap = focusMap;
        invalidate();
    }

}
