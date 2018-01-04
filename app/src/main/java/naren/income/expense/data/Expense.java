package naren.income.expense.data;

import com.google.gson.GsonBuilder;

/**
 * Created by narensmac on 04/12/17.
 */

public class Expense extends InEx {

    public Expense(String description, float amount, long time) {
        super(description, amount, false, time);
    }

    public Expense(String description, float amount) {
        super(description, amount, false);
    }

    public static InEx fromJson(String json){
        return new GsonBuilder().create().fromJson(json, InEx.class);
    }

}
