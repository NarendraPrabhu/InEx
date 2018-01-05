package naren.income.expense.ui.tasks;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import naren.income.expense.R;
import naren.income.expense.data.BackupData;
import naren.income.expense.data.InEx;
import naren.income.expense.data.InExManager;

/**
 * Created by narensmac on 04/01/18.
 */

public class ImportAndRestoreTask extends AsyncTask<Void, Void, Void> {

    private Uri mUri;
    private Context mContext;
    private ImportCompleteListener mImportCompleteListener;
    private boolean isImported = false;
    private NotificationManager nm;

    private static final int NOTIFICATION_ID = 0x100001;

    public interface ImportCompleteListener{
        void onImportComplete();
    }

    public ImportAndRestoreTask(Context context, Uri uri, ImportCompleteListener listener){
        mUri = uri;
        mContext = context;
        this.mImportCompleteListener = listener;
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPreExecute() {
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.drawable.wallet);
        builder.setProgress(0, 0, true);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setContentTitle(mContext.getString(R.string.import_and_restore));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setSubText(mContext.getString(R.string.notification_message_importing));
            nm.notify(NOTIFICATION_ID, builder.build());
        }else {
            nm.notify(NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            InputStream is = mContext.getContentResolver().openInputStream(mUri);
            String info = IOUtils.toString(is);
            if(info.contains(":")){
                String[] divideData = info.split(":");
                if(divideData[0].trim().startsWith(String.format(BackupRestoreConstants.FIRST_LINE, ""))){
                    info = divideData[1];
                    isImported = true;
                }else{
                    return null;
                }
            }
            byte[] bytes = Base64.decode(info, Base64.DEFAULT);
            String actualData = new String(bytes);
            BackupData backupData = BackupData.fromJson(actualData);
            List<InEx> items = backupData.items;
            InExManager manager = InExManager.getInstance(mContext);
            if(items != null){
                for(InEx inex : items){
                    try {
                        manager.save(inex);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        nm.cancel(NOTIFICATION_ID);
        if(!isImported){
            Toast.makeText(mContext, R.string.not_our_backup_file, Toast.LENGTH_SHORT).show();
        }
        if(mImportCompleteListener != null){
            mImportCompleteListener.onImportComplete();
        }
    }
}