package android.income.expense.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.income.expense.R;
import android.income.expense.data.InEx;
import android.income.expense.data.InExManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DateMonthYearPicker.OnDateChangeListener{

    private ListView mListView;
    private ContentsAdapter mListAdapter;
    private InExManager mInExManager;
    private DateMonthYearPicker mDateMonthYearPicker;
    private TextView mTotalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInExManager = InExManager.getInstance(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.fab).setOnClickListener(this);

        mListView = (ListView) findViewById(R.id.main_list);
        mDateMonthYearPicker = findViewById(R.id.main_date_month_year_picker);
        mDateMonthYearPicker.setOnDateChangeListener(this);
        mTotalTextView = findViewById(R.id.main_total_amount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh(){
        Cursor cursor = mInExManager.query(mDateMonthYearPicker.getCurrentDay(), mDateMonthYearPicker.getCurrentMonth(), mDateMonthYearPicker.getCurrentYear());
        if(cursor == null){
            return;
        }
        if(mListAdapter == null){
            mListAdapter = new ContentsAdapter(cursor);
            mListView.setAdapter(mListAdapter);
        }else{
            mListAdapter.swapCursor(cursor);
        }
        setTotal();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ContentsAdapter extends CursorAdapter{

        private int itemPadding = 6;
        private String amountFormat;
        public ContentsAdapter(Cursor c) {
            super(MainActivity.this, c, true);
            amountFormat = getResources().getString(R.string.amount);
            itemPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_inex, viewGroup, false);
            v.findViewById(R.id.inex_item_delete).setOnClickListener(MainActivity.this);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            view.findViewById(R.id.inex_item_delete).setTag(cursor.getLong(cursor.getColumnIndex(InEx.COLUMN_ID)));
            String amountValue = String.format(amountFormat, cursor.getString(cursor.getColumnIndex(InEx.COLUMN_AMOUNT)));
            ((TextView)view.findViewById(R.id.inex_item_amount)).setText(amountValue);
            ((TextView)view.findViewById(R.id.inex_item_description)).setText(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DESCRIPTION)));
            ((TextView)view.findViewById(R.id.inex_item_date)).setText(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DATE)));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.inex_item_delete:
                long id = (Long)view.getTag();
                if(mInExManager.delete(id)){
                    refresh();
                }
                break;
            case R.id.fab:
                startActivity(new Intent(MainActivity.this, AddInExActivity.class));
                break;
        }
    }

    @Override
    public void onDateChange(int day, int month, int year) {
        Cursor cursor = mInExManager.query(day, month, year);
        if(cursor != null) {
            mListAdapter.swapCursor(cursor);
        }
        setTotal();
    }

    private void setTotal(){
        int total = mInExManager.getTotal(mDateMonthYearPicker.getCurrentDay(), mDateMonthYearPicker.getCurrentMonth(), mDateMonthYearPicker.getCurrentYear());
        mTotalTextView.setText(String.format(getResources().getString(R.string.total), ""+total));
    }

}
