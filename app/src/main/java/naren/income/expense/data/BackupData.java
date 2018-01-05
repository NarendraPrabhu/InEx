package naren.income.expense.data;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by narensmac on 05/01/18.
 */

public class BackupData {

    @SerializedName("items")
    public List<InEx> items;

    public BackupData(List<InEx> items){
        this.items = items;
    }

    public static BackupData fromJson(String json){
        return new GsonBuilder().create().fromJson(json, BackupData.class);
    }

    public String toJson(){
        return new GsonBuilder().create().toJson(this);
    }
}
