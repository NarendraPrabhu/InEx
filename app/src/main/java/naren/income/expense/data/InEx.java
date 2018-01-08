package naren.income.expense.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by narensmac on 04/12/17.
 */

public class InEx implements Parcelable{

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

    public InEx(){

    }
    
    public InEx(String description, float amount, boolean isIncome){
        this.amount = amount;
        this.description = description;
        this.isIncome = isIncome;
        this.time = DATE_FORMATTER.format(new Date());
    }

    public InEx(String description, float amount, boolean isIncome, long time){
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
        InEx inx = new InEx(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DESCRIPTION)),
                    cursor.getFloat(cursor.getColumnIndex(InEx.COLUMN_AMOUNT)), cursor.getInt(cursor.getColumnIndex(COLUMN_IS_INCOME))==1);
        inx.id = cursor.getLong(cursor.getColumnIndex(InEx.COLUMN_ID));
        inx.time = cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DATE));
        return inx;
    }

    @Override
    public String toString() {
        return description+" : "+amount;
    }

    public String toJson(){
        return new GsonBuilder().create().toJson(this);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(description);
        parcel.writeFloat(amount);
        parcel.writeInt(isIncome ? 1 : 0);
        parcel.writeString(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InEx> CREATOR = new Creator<InEx>() {
        @Override
        public InEx createFromParcel(Parcel parcel) {
            InEx inEx = new InEx();
            inEx.id = parcel.readLong();
            inEx.description = parcel.readString();
            inEx.amount = parcel.readFloat();
            inEx.isIncome = parcel.readInt() == 1;
            inEx.time = parcel.readString();
            return inEx;
        }

        @Override
        public InEx[] newArray(int i) {
            return new InEx[i];
        }
    };
}
