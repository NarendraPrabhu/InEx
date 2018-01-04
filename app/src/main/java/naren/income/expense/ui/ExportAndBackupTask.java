package naren.income.expense.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Base64;

import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import naren.income.expense.data.InEx;
import naren.income.expense.data.InExManager;

/**
 * Created by narensmac on 04/01/18.
 */

public class ExportAndBackupTask extends AsyncTask<Void, Void, Void>{

    public static final String AUTHORITY = "naren.income.expense";
    private Context mContext;
    private File backupFile = null;

    public ExportAndBackupTask(Context context){
        mContext = context;
        File folder = new File(mContext.getFilesDir(), "backup");
        if(!folder.exists()){
            folder.mkdir();
        }
        backupFile = new File(folder, "backup.inex");
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
            JSONArray array = new JSONArray();
            for(InEx item : items){
                array.put(item.toJson());
            }
            byte[] encoded = Base64.encode(array.toString().getBytes(), Base64.DEFAULT);
            fos.write(encoded);
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
        if(backupFile != null && backupFile.exists()){
            Intent intent = new Intent(Intent.ACTION_SEND);
            Uri uri = FileProvider.getUriForFile(mContext, AUTHORITY, backupFile);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager pm = mContext.getPackageManager();
            List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
            if(info != null && info.size() > 0) {
                mContext.startService(Intent.createChooser(intent, "Share with"));
            }
        }
    }
}
