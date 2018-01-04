package naren.income.expense.data;

import com.google.gson.GsonBuilder;

/**
 * Created by narensmac on 04/12/17.
 */

public class Income extends InEx {

    public Income(String description, float amount, long time) {
        super(description, -amount, true, time);
    }

    public Income(String description, float amount) {
        super(description, amount, true);
    }

    public static InEx fromJson(String json){
        return new GsonBuilder().create().fromJson(json, InEx.class);
    }


}
