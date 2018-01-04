package naren.income.expense.data;

import android.content.Context;
import android.database.Cursor;

import java.util.List;

import naren.income.expense.data.db.DatabaseHelper;

/**
 * Created by narensmac on 07/12/17.
 */

public class InExManager{

    private static InExManager manager = null;
    private DatabaseHelper mDatabaseHelper = null;

    private InExManager(Context context){
        mDatabaseHelper = DatabaseHelper.getInstance(context);
    }

    public synchronized static InExManager getInstance(Context context){
        if (manager == null) {
            manager = new InExManager(context);
        }
        return manager;
    }

    public boolean save(InEx item){
        return mDatabaseHelper.save(item);
    }

    public boolean delete(long id){
        return mDatabaseHelper.delete(id);
    }

    public Cursor query(int day, int month, int year){
        return mDatabaseHelper.query(day, month, year);
    }

    public int getTotal(int day, int month, int year){
        return mDatabaseHelper.getTotal(day, month, year);
    }

    public List<InEx> getAllItems(){
        return mDatabaseHelper.getAllItems();
    }

}
