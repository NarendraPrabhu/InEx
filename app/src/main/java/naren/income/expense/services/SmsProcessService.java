package naren.income.expense.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import naren.income.expense.data.InEx;
import naren.income.expense.data.InExManager;
import naren.income.expense.data.SmsItem;

/**
 * Created by narensmac on 04/01/18.
 */

public class SmsProcessService extends IntentService {

    public static final String EXTRA_SMS = "sms_data";
    public static final String EXTRA_RECEIVER = "receiver";

    public static String EXTRA_STATE = "state";

    public static final String EXTRA_VALUE_NEW_SMS = "NEW_SMS";
    public static final String EXTRA_VALUE_PROCESS_SMS = "PROCESS_SMS";

    private InExManager mInExManager;

    public static abstract class OnProcessCompleteListener extends ResultReceiver{
        public OnProcessCompleteListener(Handler handler) {
            super(handler);
        }

        @Override
        final protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            onProcessComplete();
        }

        public abstract void onProcessComplete();
    }

    private ResultReceiver mListener = null;

    public SmsProcessService() {
        super("SMS_PROCESSOR");
        mInExManager = InExManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        switch (intent.getStringExtra(EXTRA_STATE)+"") {
            case EXTRA_VALUE_NEW_SMS: {
                List<SmsItem> items = (ArrayList)intent.getSerializableExtra(EXTRA_SMS);
                if (items == null) {
                    return;
                }
                parseSms(items);
                break;
            }
            case EXTRA_VALUE_PROCESS_SMS: {
                mListener = intent.getParcelableExtra(EXTRA_RECEIVER);
                processSmsDb();
                break;
            }
            default:
               //Do nothing
        }
    }

    private List<SmsItem> scanSmsDb(){
        List<SmsItem> items = new ArrayList<>();
        Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uriSms, new String[]{"_id", "address", "date", "body"},null,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
               do{
                  String from = cursor.getString(1);
                  long date = cursor.getLong(2);
                  String body = cursor.getString(3);
                   SmsItem item = new SmsItem(from, body, date);
                   items.add(item);
               }while (cursor.moveToNext());
            }
        }
        return items;
    }

    private void processSmsDb(){
        List<SmsItem> items = scanSmsDb();
        parseSms(items);
        if(mListener != null){
            mListener.send(0, null);
        }
    }

    private void parseSms(List<SmsItem> items){
        if(items == null){
            return;
        }
        for(SmsItem item : items) {
            InEx expense = SmsParser.parse(item.getData(), item.getDate().getTime());
            if (expense != null) {
                mInExManager.save(expense);
            }
        }
    }

    public static void start(Context context, ArrayList<SmsItem> items){
        Intent service = new Intent(context, SmsProcessService.class);
        service.putExtra(SmsProcessService.EXTRA_STATE, EXTRA_VALUE_NEW_SMS);
        if(items != null) {
            service.putExtra(SmsProcessService.EXTRA_SMS, items);
        }
        context.startService(service);
    }

    public static void start(Context context, OnProcessCompleteListener onProcessCompleteListener){
        Intent service = new Intent(context, SmsProcessService.class);
        service.putExtra(SmsProcessService.EXTRA_STATE, EXTRA_VALUE_PROCESS_SMS);
        service.putExtra(SmsProcessService.EXTRA_RECEIVER, onProcessCompleteListener);
        context.startService(service);
    }
}
