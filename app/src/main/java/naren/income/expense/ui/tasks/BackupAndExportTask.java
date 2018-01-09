package naren.income.expense.ui.tasks;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import naren.income.expense.R;
import naren.income.expense.data.BackupData;
import naren.income.expense.data.InEx;
import naren.income.expense.data.InExManager;

/**
 * Created by narensmac on 04/01/18.
 */

public class BackupAndExportTask extends AsyncTask<Void, Void, Void>{

    private static final String AUTHORITY = "naren.income.expense";

    private Context mContext;
    private File backupFile = null;
    private NotificationManager nm;

    private static final int NOTIFICATION_ID = 0x100001;

    public BackupAndExportTask(Context context){
        mContext = context;
        File folder = mContext.getFilesDir();
        backupFile = new File(folder, "backup.inex");
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
        builder.setContentTitle(mContext.getString(R.string.backup_and_export));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setSubText(mContext.getString(R.string.notification_message_exporting));
        }
        nm.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if(backupFile.exists()){
                backupFile.delete();
                backupFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(backupFile);
            InExManager manager = InExManager.getInstance(mContext);
            List<InEx> items = manager.getAllItems();
            BackupData data = new BackupData(items);
            String encoded = new String(Base64.encode(data.toJson().getBytes(), Base64.DEFAULT));
            String header = String.format(BackupRestoreConstants.FIRST_LINE, ""+System.currentTimeMillis());
            fos.write(String.format("%s:%s", header,encoded).getBytes());
            fos.flush();
            fos.close();
        }catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        nm.cancel(NOTIFICATION_ID);
        if(backupFile != null && backupFile.exists()){
            Intent intent = new Intent(Intent.ACTION_SEND);
            Uri uri = FileProvider.getUriForFile(mContext, AUTHORITY, backupFile);
            intent.setDataAndType(uri, "application/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            List<ResolveInfo> resolveInfos = mContext.getPackageManager().queryIntentActivities(intent, 0);
            if(resolveInfos == null || resolveInfos.size() == 0){
                Toast.makeText(mContext, "Nothing to handle", Toast.LENGTH_SHORT).show();
            }else{
                for(ResolveInfo info : resolveInfos){
                    mContext.grantUriPermission(info.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                mContext.startActivity(intent);
            }
        }
    }
}
