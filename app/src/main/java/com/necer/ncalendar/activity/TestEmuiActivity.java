package com.necer.ncalendar.activity;

import android.widget.TextView;

import com.necer.calendar.EmuiCalendar;
import com.necer.entity.NDate;
import com.necer.listener.OnCalendarChangedListener;
import com.necer.ncalendar.R;
import com.necer.painter.CalendarPainter;
import com.necer.painter.InnerPainter;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.List;


/**
 * Created by necer on 2018/11/12.
 */
public class TestEmuiActivity extends BaseActivity {


    TextView tv_lunar;
    TextView tv_date;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_emui;
    }

    @Override
    protected void onCreatee() {

        tv_lunar = findViewById(R.id.tv_lunar);
        tv_date = findViewById(R.id.tv_date);

        List<String> pointList = Arrays.asList("2018-10-01", "2018-11-19", "2018-11-20", "2018-05-23", "2019-01-01", "2018-12-23");


        EmuiCalendar emuiCalendar = findViewById(R.id.emuiCalendar);
        InnerPainter innerPainter = (InnerPainter) emuiCalendar.getCalendarPainter();

        innerPainter.setPointList(pointList);
        emuiCalendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarDateChanged(NDate date,boolean isClick) {
                tv_date.setText(date.localDate.getYear() + "年" + date.localDate.getMonthOfYear() + "月");
                int days = Days.daysBetween(new LocalDate(), date.localDate).getDays();
                String string ;
                if (days == 0) {
                    string = "今天";
                } else if (days > 0) {
                    string = days + "天后";
                } else {
                    string = -days + "天前";
                }
                tv_lunar.setText(string + " 农历" + date.lunar.lunarYearStr + "年 " + date.lunar.lunarMonthStr + date.lunar.lunarDayStr);
            }

            @Override
            public void onCalendarStateChanged(boolean isMonthSate) {

            }
        });
    }
}
