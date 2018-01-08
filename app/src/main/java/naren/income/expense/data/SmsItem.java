package naren.income.expense.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by narensmac on 04/01/18.
 */

public class SmsItem implements Parcelable{

    private final String from;
    private final String data;
    private final Date date;

    public SmsItem(String from, String data){
        this.from = from;
        this.data = data;
        this.date = new Date();
    }

    public SmsItem(String from, String data, long timeInMillis){
        this.from = from;
        this.data = data;
        this.date = new Date(timeInMillis);
    }

    public String getFrom() {
        return from;
    }

    public String getData() {
        return data;
    }

    public Date getDate() {
        return date;
    }

    public static final Parcelable.Creator<SmsItem> CREATOR = new Creator<SmsItem>() {
        @Override
        public SmsItem createFromParcel(Parcel parcel) {
            SmsItem item = new SmsItem(parcel.readString(), parcel.readString(), parcel.readLong());
            return item;
        }

        @Override
        public SmsItem[] newArray(int i) {
            return new SmsItem[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(from);
        parcel.writeString(data);
        parcel.writeLong(date.getTime());
    }

    @Override
    public String toString() {
        return from+" : "+data+" : "+date;
    }
}
