package naren.income.expense.data;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by narensmac on 04/12/17.
 */

public abstract class InEx {

    public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd");

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IS_INCOME = "is_income";
    public static final String COLUMN_DATE = "date";
    
    
    @SerializedName(COLUMN_ID)
    private long id = -1;

    @SerializedName(COLUMN_DESCRIPTION)
    private String description;

    @SerializedName(COLUMN_AMOUNT)
    private float amount;

    @SerializedName(COLUMN_IS_INCOME)
    private boolean isIncome;

    @SerializedName(COLUMN_DATE)
    private String time;
    
    protected InEx(String description, float amount, boolean isIncome){
        this.amount = amount;
        this.description = description;
        this.isIncome = isIncome;
        this.time = DATE_FORMATTER.format(new Date());
    }

    protected InEx(String description, float amount, boolean isIncome, long time){
        this.amount = amount;
        this.description = description;
        this.isIncome = isIncome;
        this.time = DATE_FORMATTER.format(time);
    }

    public long getId() {
        return id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public static InEx fromCursor(final Cursor cursor){
        InEx inx = null;
        if(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_INCOME))==1){
            inx = new Income(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DESCRIPTION)),
                    cursor.getFloat(cursor.getColumnIndex(InEx.COLUMN_AMOUNT)));
        }else {
           inx = new Expense(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DESCRIPTION)),
                    cursor.getFloat(cursor.getColumnIndex(InEx.COLUMN_AMOUNT)));
        }
        inx.id = cursor.getLong(cursor.getColumnIndex(InEx.COLUMN_ID));
        inx.time = cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DATE));
        return inx;
    }
}
