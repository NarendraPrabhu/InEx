package android.income.expense.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.income.expense.data.InEx;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by narensmac on 04/12/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    private static DatabaseHelper helper = null;

    public synchronized static DatabaseHelper getInstance(Context context){
        if(helper == null){
            helper = new DatabaseHelper(context);
        }
        return helper;
    }

    private DatabaseHelper(Context context) {
        super(context, InEx.class.getSimpleName(), null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_INEX_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if(i != i1) {
            sqLiteDatabase.execSQL(CREATE_INEX_TABLE);
        }
    }

    private static String CREATE_INEX_TABLE = "CREATE TABLE IF NOT EXISTS "+ InEx.class.getSimpleName() +
            "(" +
            InEx.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            InEx.COLUMN_AMOUNT + " REAL NOT NULL, " +
            InEx.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
            InEx.COLUMN_IS_INCOME + " INTEGER DEFAULT 0, " +
            InEx.COLUMN_DATE + " TEXT NOT NULL" +
            ");";

    public boolean save(InEx value){
        boolean returnValue = false;

        ContentValues cv = new ContentValues();
        cv.put(InEx.COLUMN_AMOUNT, value.getAmount());
        cv.put(InEx.COLUMN_DESCRIPTION, value.getDescription());
        cv.put(InEx.COLUMN_DATE, value.getTime());
        cv.put(InEx.COLUMN_IS_INCOME, value.isIncome() ? 1 : 0);

        if(value.getId() == -1){
            returnValue = (getWritableDatabase().insert(InEx.class.getSimpleName(), null, cv) != -1);
        }else{
            returnValue = (getWritableDatabase().update(InEx.class.getSimpleName(), cv, InEx.COLUMN_ID+"=?", new String[]{""+value.getId()}) > 0);
        }
        return returnValue;
    }

    public boolean delete(InEx value){
        return delete(value.getId());
    }

    public boolean delete(long id){
        return (getWritableDatabase().delete(InEx.class.getSimpleName(), InEx.COLUMN_ID+"=?",  new String[]{""+id}) > 0);
    }

    public InEx get(long id){
        InEx value = null;
        Cursor cursor = getReadableDatabase().query(InEx.class.getSimpleName(), null, InEx.COLUMN_ID+"=?", new String[]{""+id}, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()){
                value = InEx.fromCursor(cursor);
            }
            cursor = null;
        }
        return value;
    }

    private String format(int value){
        if(value < 10){
            return "0"+value;
        }
        return value+"";
    }

    public int getTotal(int day, int month, int year){
        int total = 0;
        String query = queryString(day, month, year);
        Cursor cursor = getReadableDatabase().query(InEx.class.getSimpleName(), new String[]{"SUM("+InEx.COLUMN_AMOUNT+")"}, query, null, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst() && cursor.getCount() == 1){
                total = cursor.getInt(0);
            }
            cursor = null;
        }
        return total;
    }

    public Cursor query(int day, int month, int year){
        String queryString = queryString(day, month, year);
        return getReadableDatabase().query(InEx.class.getSimpleName(), null, queryString, null, null, null, null);
    }

    private String queryString(int day, int month, int year){
        StringBuffer value = new StringBuffer();
        if(year != 0){
            value.append(year);
        }
        if(month != 0){
            if(!TextUtils.isEmpty(value)){
                value.append("/");
            }
            value.append(format(month));
        }
        if(day != 0){
            if(!TextUtils.isEmpty(value)){
                value.append("/");
            }
            value.append(format(day));
        }

        String queryString = null;
        if(!TextUtils.isEmpty(value)){
            queryString = InEx.COLUMN_DATE+" LIKE '%"+value.toString()+"%'";
        }

        return  queryString;
    }

    public List<InEx> get(int day, int month, int year){
        List<InEx> values = new ArrayList<>();

        String queryString = queryString(day, month, year);

        Cursor cursor = getReadableDatabase().query(InEx.class.getSimpleName(), null, queryString, null, null, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    values.add(InEx.fromCursor(cursor));
                }while (cursor.moveToNext());
            }
        }
        return values;
    }

}
