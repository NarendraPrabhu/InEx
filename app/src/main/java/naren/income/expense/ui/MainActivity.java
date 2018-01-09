package naren.income.expense.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import naren.income.expense.R;
import naren.income.expense.data.InEx;
import naren.income.expense.data.InExManager;
import naren.income.expense.receivers.SmsReceiver;
import naren.income.expense.services.SmsProcessService;
import naren.income.expense.ui.tasks.BackupAndExportTask;
import naren.income.expense.ui.tasks.ImportAndRestoreTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DateMonthYearPicker.OnDateChangeListener{

    private static final int REQUEST_CODE_IMPORT_FILE = 0x101;
    private ListView mListView;
    private ContentsAdapter mListAdapter;
    private InExManager mInExManager;
    private DateMonthYearPicker mDateMonthYearPicker;
    private TextView mTotalTextView;
    private String amountFormat;

    private final int RC_HANDLE_SMS_PERMISSION = 0x001;

    private ComponentName smsComponentName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInExManager = InExManager.getInstance(this);
        amountFormat = getResources().getString(R.string.amount);

        smsComponentName = new ComponentName(getBaseContext().getPackageName(), SmsReceiver.class.getName());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.fab).setOnClickListener(this);

        mListView = findViewById(R.id.main_list);
        addEmptyHeaderAndFooter();
        mDateMonthYearPicker = findViewById(R.id.main_date_month_year_picker);
        mDateMonthYearPicker.setOnDateChangeListener(this);
        mTotalTextView = findViewById(R.id.main_total_amount);

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            checkSmsComponent();
        }else{
            requestSmsPermission();
        }
    }

    private void checkSmsComponent(){
        PackageManager pm = getPackageManager();
        if(pm.getComponentEnabledSetting(smsComponentName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
            pm.setComponentEnabledSetting(smsComponentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            SmsProcessService.start(this, new SmsProcessService.OnProcessCompleteListener(new Handler()){

                @Override
                public void onProcessComplete() {
                    if(mListAdapter != null){
                        mListAdapter.notifyDataSetChanged();
                        mListAdapter.notifyDataSetInvalidated();
                    }
                }
            });
        }
    }

    private void requestSmsPermission() {

        final String[] permissions = new String[]{
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        };

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_SMS_PERMISSION);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_SMS_PERMISSION);
            }
        };

        Snackbar.make(mListView, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
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
        if (item.getItemId() == R.id.action_backup) {
            new BackupAndExportTask(this).execute();
            return true;
        }
        if (item.getItemId() == R.id.action_restore) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_IMPORT_FILE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ContentsAdapter extends CursorAdapter{

        private SimpleDateFormat readableFormatter = new SimpleDateFormat("MMM dd, yyyy");

        public ContentsAdapter(Cursor c) {
            super(MainActivity.this, c, true);
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
            String dateString = cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DATE));
            try {
                Date date = InEx.DATE_FORMATTER.parse(dateString);
                dateString = readableFormatter.format(date);
            }catch (ParseException pe){
                pe.printStackTrace();
            }
            ((TextView)view.findViewById(R.id.inex_item_date)).setText(dateString);
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

    private void addEmptyHeaderAndFooter(){
        mListView.addHeaderView(getEmptyView());
        mListView.addFooterView(getEmptyView());
    }

    private View getEmptyView(){
        View v = new View(this);
        int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        v.setMinimumHeight(height);
        v.setBackgroundColor(Color.TRANSPARENT);
        return v;
    }

    private void setTotal(){
        int total = mInExManager.getTotal(mDateMonthYearPicker.getCurrentDay(), mDateMonthYearPicker.getCurrentMonth(), mDateMonthYearPicker.getCurrentYear());
        String totalAmount = String.format(amountFormat, ""+total);
        mTotalTextView.setText(String.format(getResources().getString(R.string.total), totalAmount));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_IMPORT_FILE && resultCode == RESULT_OK){
            Uri uri = data.getData();
            ImportAndRestoreTask iart = new ImportAndRestoreTask(this, uri, new ImportAndRestoreTask.ImportCompleteListener() {
                @Override
                public void onImportComplete() {
                    refresh();
                }
            });
            iart.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != RC_HANDLE_SMS_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if(grantResults != null) {
            if (grantResults.length == 0) {
                Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();
            } else {
                int state = (grantResults[0] == PackageManager.PERMISSION_GRANTED)?PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                PackageManager pm = getPackageManager();
                pm.setComponentEnabledSetting(smsComponentName, state, PackageManager.DONT_KILL_APP);

                if(grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    SmsProcessService.start(this, new SmsProcessService.OnProcessCompleteListener(new Handler()){

                        @Override
                        public void onProcessComplete() {
                            refresh();
                        }
                    });
                }
            }
        }
    }
}