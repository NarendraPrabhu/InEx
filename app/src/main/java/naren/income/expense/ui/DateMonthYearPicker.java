package naren.income.expense.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import naren.income.expense.R;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by narensmac on 06/12/17.
 */

public final class DateMonthYearPicker extends LinearLayout implements AdapterView.OnItemSelectedListener{

    private int currentDay = 0;
    private int currentMonth = 0;
    private int currentYear = 0;

    private Spinner daysSpinner = null;
    private Spinner monthsSpinner = null;
    private Spinner yearSpinner = null;

    private DaysAdapter daysAdapter = null;
    private MonthsAdapter monthsAdapter = null;
    private YearsAdapter yearsAdapter = null;

    private OnDateChangeListener mOnDateChangeListener = null;

    private int textSize = 13;
    private int textColor = android.R.color.black;
    private int typeface = Typeface.NORMAL;
    private int textViewResourceId = -1;
    int minimumHeight = 40;

    public interface OnDateChangeListener{
        void onDateChange(int day, int month, int year);
    }

    public DateMonthYearPicker(Context context) {
        super(context);
        init();
    }

    public DateMonthYearPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DateMonthYearPicker);
        textViewResourceId = a.getResourceId(R.styleable.DateMonthYearPicker_textViewResource, -1);
        a.recycle();
        init();
    }

    public DateMonthYearPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DateMonthYearPicker);
        textViewResourceId = a.getResourceId(R.styleable.DateMonthYearPicker_textViewResource, -1);
        a.recycle();
        init();
    }

    public DateMonthYearPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setOnDateChangeListener(OnDateChangeListener mOnDateChangeListener) {
        this.mOnDateChangeListener = mOnDateChangeListener;
    }

    private void init(){
        minimumHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, minimumHeight, getContext().getResources().getDisplayMetrics());

        Calendar c = Calendar.getInstance();
        currentDay = c.get(Calendar.DAY_OF_MONTH);
        currentMonth = c.get(Calendar.MONTH)+1;
        currentYear = c.get(Calendar.YEAR);

        daysSpinner = getSpinner();
        monthsSpinner = getSpinner();
        yearSpinner = getSpinner();

        daysAdapter = new DaysAdapter();
        monthsAdapter = new MonthsAdapter();
        yearsAdapter = new YearsAdapter();

        setOrientation(HORIZONTAL);
        setWeightSum(.9f);
        addView(daysSpinner, getParams(0));
        addView(monthsSpinner, getParams(1));
        addView(yearSpinner, getParams(2));

        daysSpinner.setAdapter(daysAdapter);
        monthsSpinner.setAdapter(monthsAdapter);
        yearSpinner.setAdapter(yearsAdapter);

        daysSpinner.setSelection(currentDay);
        monthsSpinner.setSelection(currentMonth);
        yearSpinner.setSelection(currentYear-1970);
    }

    private Spinner getSpinner(){
        Spinner spinner = new Spinner(getContext());
        spinner.setPopupBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dk_gray)));
        spinner.setOnItemSelectedListener(this);
        return spinner;
    }

    private LayoutParams getParams(int i){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = .3f;
        lp.gravity = Gravity.CENTER;
        return lp;
    }

    private class DaysAdapter extends BaseAdapter{

        private int count = 31;

        public void notifyDataSetChanged(int count) {
            super.notifyDataSetChanged();
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int i) {
            return (Integer)(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null){
                view = createTextView(getContext());
            }
            ((TextView)view).setText(i == 0 ? "All" : (i)+"");
            return view;
        }
    }

    private class MonthsAdapter extends BaseAdapter{

        private String[] months = {"All", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        @Override
        public int getCount() {
            return months.length;
        }

        @Override
        public Object getItem(int i) {
            return (Integer)(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null){
                view = createTextView(getContext());
            }
            ((TextView)view).setText(months[i]);
            return view;
        }
    }

    private class YearsAdapter extends BaseAdapter{

        private int INIT_YEAR = 1970;
        private int END_YEAR = 2099;

        @Override
        public int getCount() {
            return END_YEAR-INIT_YEAR;
        }

        @Override
        public Object getItem(int i) {
            return (Integer)(INIT_YEAR+i);

        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null){
                view = createTextView(getContext());
            }
            ((TextView)view).setText(""+(INIT_YEAR+i));
            return view;
        }
    }

    private TextView createTextView(Context context){
        TextView view = null;
        if(textViewResourceId == -1) {
            view = new TextView(getContext());
            view.setGravity(Gravity.CENTER);
            view.setTextSize(textSize);
            view.setTypeface(null, typeface);
            view.setMinimumHeight(minimumHeight);
            view.setTextColor(getContext().getResources().getColor(textColor));
            Spinner.LayoutParams lp = new Spinner.LayoutParams(Spinner.LayoutParams.MATCH_PARENT, Spinner.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lp);
        }else{
            view = (TextView) LayoutInflater.from(context).inflate(textViewResourceId, null);
        }
        return view;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getAdapter() instanceof DaysAdapter){
            currentDay = (Integer) ((DaysAdapter)adapterView.getAdapter()).getItem(i);
        }

        if(adapterView.getAdapter() instanceof MonthsAdapter){
            int month = (Integer)((MonthsAdapter)adapterView.getAdapter()).getItem(i);
            if(month != currentMonth){
                currentDay = 0;
            }
            currentMonth = month;
        }

        if(adapterView.getAdapter() instanceof YearsAdapter){
            currentYear = (Integer)((YearsAdapter)adapterView.getAdapter()).getItem(i);
        }

        if(mOnDateChangeListener != null){
            mOnDateChangeListener.onDateChange(currentDay, currentMonth, currentYear);
        }

        int daysCount = 30;
        switch (currentMonth){
            case 0:
                currentDay = 0;
                break;
            case 2:
                daysCount = checkIsLeapYear(currentYear) ? 29 : 28;
                break;
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysCount = 31;
                break;
        }
        daysAdapter.notifyDataSetChanged(daysCount+1);
        daysSpinner.invalidate();
        daysSpinner.setSelection(currentDay);
    }

    private static boolean checkIsLeapYear(int year)
    {
        if (year % 400 == 0)
            return true;
        if (year % 100 == 0)
            return false;
        if (year % 4 == 0)
            return true;
        return false;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    public int getCurrentYear() {
        return currentYear;
    }
}
