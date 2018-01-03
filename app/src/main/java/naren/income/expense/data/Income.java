package naren.income.expense.data;

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

}
