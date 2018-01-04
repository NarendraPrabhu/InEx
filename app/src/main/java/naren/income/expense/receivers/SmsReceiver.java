package naren.income.expense.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import java.util.ArrayList;

import naren.income.expense.data.SmsItem;
import naren.income.expense.services.SmsProcessService;

/**
 * Created by narensmac on 04/01/18.
 */

public class SmsReceiver  extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msgFrom = null;
            if (bundle != null){
                try{
                    ArrayList<SmsItem> items = new ArrayList<>();
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    String format = bundle.getString("format");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i < msgs.length; i++){
                        if(TextUtils.isEmpty(format)) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }else{
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        }
                        msgFrom = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        items.add(new SmsItem(msgFrom, msgBody));
                    }
                    SmsProcessService.start(context, items);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
