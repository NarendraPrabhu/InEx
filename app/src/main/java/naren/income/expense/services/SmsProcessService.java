package naren.income.expense.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import java.util.Random;

import naren.income.expense.R;
import naren.income.expense.data.InEx;
import naren.income.expense.data.InExManager;
import naren.income.expense.data.SmsItem;

/**
 * Created by narensmac on 04/01/18.
 */

public class SmsProcessService extends IntentService {

    public static final String EXTRA_SMS = "sms_data";
    public static final String EXTRA_RECEIVER = "receiver";
    public static final String EXTRA_EXPENSE = "expense";
    private static final String EXTRA_ACTION = "action";
    private static final String EXTRA_NOTIFICATION_ID = "notification_id" ;

    public static String EXTRA_STATE = "state";

    public enum Action{
        DELETE,
        KEEP
    }


    public enum State{
        NEW_SMS,
        PROCESS_SMS,
        NEW_EXPENSE
    }

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
        switch ((State)intent.getSerializableExtra(EXTRA_STATE)) {
            case NEW_SMS: {
                List<SmsItem> items = (ArrayList)intent.getSerializableExtra(EXTRA_SMS);
                if (items == null) {
                    return;
                }
                parseSms(items, true);
                break;
            }
            case PROCESS_SMS: {
                mListener = intent.getParcelableExtra(EXTRA_RECEIVER);
                processSmsDb();
                break;
            }
            case NEW_EXPENSE:{
                InEx inEx = (InEx)intent.getParcelableExtra(EXTRA_EXPENSE);
                if(inEx == null){
                    return;
                }
                Action action = (Action)intent.getSerializableExtra(EXTRA_ACTION);
                if(inEx != null){
                    if(action == Action.DELETE){
                        InExManager.getInstance(this).delete(inEx.getId());
                    }
                }
                int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(notificationId);
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
        parseSms(items, false);
        if(mListener != null){
            mListener.send(0, null);
        }
    }

    private void parseSms(List<SmsItem> items, boolean isNew){
        if(items == null){
            return;
        }
        for(SmsItem item : items) {
            InEx expense = SmsParser.parse(item.getData(), item.getDate().getTime());
            if (expense != null) {
                if(mInExManager.save(expense) && isNew){
                    sendNotification(expense);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void sendNotification(InEx expense){
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.wallet);
        builder.setContentTitle(getString(R.string.notification_added_new_expense));
        builder.setContentText(String.format(getString(R.string.amount), expense.getAmount()+""));
        builder.setSubText(expense.getDescription());

        Random random = new Random();
        int notitificationId = random.nextInt();

        Intent deleteServiceIntent = new Intent(this, SmsProcessService.class);
        deleteServiceIntent.putExtra(EXTRA_STATE, State.NEW_EXPENSE);
        deleteServiceIntent.putExtra(EXTRA_ACTION, Action.DELETE);
        deleteServiceIntent.putExtra(EXTRA_NOTIFICATION_ID, notitificationId);
        deleteServiceIntent.putExtra(EXTRA_EXPENSE, expense);
        PendingIntent deleteServicePendingIntent = PendingIntent.getService(this, 0, deleteServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Action deleteAction = new Notification.Action.Builder(android.R.drawable.ic_menu_delete, getString(R.string.delete), deleteServicePendingIntent).build();

        Intent keepServiceIntent = new Intent(this, SmsProcessService.class);
        keepServiceIntent.putExtra(EXTRA_STATE, State.NEW_EXPENSE);
        keepServiceIntent.putExtra(EXTRA_ACTION, Action.KEEP);
        keepServiceIntent.putExtra(EXTRA_NOTIFICATION_ID, notitificationId);
        keepServiceIntent.putExtra(EXTRA_EXPENSE, expense);
        PendingIntent keepServicePendingIntent = PendingIntent.getService(this, 1, keepServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Action keepAction = new Notification.Action.Builder(android.R.drawable.ic_menu_save, getString(R.string.keep), keepServicePendingIntent).build();

        builder.setActions(deleteAction, keepAction);
        nm.notify(notitificationId, builder.build());
    }

    public static void start(Context context, ArrayList<SmsItem> items){
        Intent service = new Intent(context, SmsProcessService.class);
        service.putExtra(SmsProcessService.EXTRA_STATE, State.NEW_SMS);
        if(items != null) {
            service.putExtra(SmsProcessService.EXTRA_SMS, items);
        }
        context.startService(service);
    }

    public static void start(Context context, OnProcessCompleteListener onProcessCompleteListener){
        Intent service = new Intent(context, SmsProcessService.class);
        service.putExtra(SmsProcessService.EXTRA_STATE, State.PROCESS_SMS);
        service.putExtra(SmsProcessService.EXTRA_RECEIVER, onProcessCompleteListener);
        context.startService(service);
    }
}
