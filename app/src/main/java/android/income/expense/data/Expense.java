package android.income.expense.data;

import android.database.Cursor;

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

}
