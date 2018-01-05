package naren.income.expense.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import naren.income.expense.data.InEx;

/**
 * Created by narensmac on 04/01/18.
 */

public class SmsParser {

    private static final Pattern PATTERN_AMOUNT = Pattern.compile("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)");
    //private static final String PATTERN_MERCHANT = "(?i)(?:\\sat\\s|in\\*)([A-Za-z0-9]*\\s?-?\\s?[A-Za-z0-9]*\\s?-?\\.?)";
    private static final Pattern PATTEN_DIGITS_ALONE = Pattern.compile("-?[\\d,\\,,\\.]+");

    private static final String[][] KEYS_TO_CONTAIN = {
            {"spent", "spnt"},
            {"purchase made", "purchase has been made", "purchased", "purchase been made"},
            {"payment made", "paid", "payment has been made", "payment has made"},
            {"paid"},
            {"has been debited", "has debited"}
    };

    private static final String[][] KEYS_NOT_TO_CONTAIN = {
            {"ignore if", "ignore this if"},
            {"credited to your", "credited to ur"},
    };


    public static InEx parse(String body, long time){
        InEx expense = null;
        Matcher amountMatcher = PATTERN_AMOUNT.matcher(body);
        Float amount = null;
        if(amountMatcher.find()){
            try {
                String amountString = amountMatcher.group();
                Matcher m = PATTEN_DIGITS_ALONE.matcher(amountString);

                if (m.find()) {
                    if (m.groupCount() > 1) {
                        return expense;
                    }
                    amountString = m.group();
                    if (amountString.contains(",")) {
                        amountString = amountString.replace(",", "");
                    }
                    amount = Float.parseFloat(amountString);
                }
            } catch (RuntimeException rte){
                rte.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(amount == null){
            return expense;
        }

        body = body.toLowerCase();
        boolean isPresent = checkKeysToContain(body);
        if(isPresent){
            isPresent = !checkKeysNotToContain(body);
        }

        if(isPresent){
            expense = new InEx(body, amount, false, time);
        }

        return expense;
    }

    private static boolean checkKeysToContain(String body){
        for (String[] keySet: KEYS_TO_CONTAIN) {
            for(String key : keySet){
                if(body.contains(key)){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkKeysNotToContain(String body){
        for (String[] keySet: KEYS_NOT_TO_CONTAIN) {
            for(String key : keySet){
                if(body.contains(key)){
                    return true;
                }
            }
        }
        return false;
    }

}
