package android.income.expense.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.income.expense.R;
import android.income.expense.data.DatabaseHelper;
import android.income.expense.data.Expense;
import android.income.expense.data.InEx;
import android.income.expense.data.Income;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by narensmac on 04/12/17.
 */

public class AddInExActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener{


    private TextView labelDescriptionTextView = null;
    private EditText descriptionEditText = null;
    private TextView labelAmountTextView = null;
    private EditText amountEditText = null;
    private Spinner inexOptionsSpinner = null;
    private Button dateButton = null;
    private Button saveButton = null;
    private DatePickerDialog datePickerDialog = null;

    private DateFormat DATE_FORMATTER = new SimpleDateFormat("dd - MM - yyyy");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inex);
        labelDescriptionTextView = ((TextView)findViewById(R.id.add_inex_label_description));
        descriptionEditText = ((EditText)findViewById(R.id.add_inex_description));
        labelAmountTextView = ((TextView)findViewById(R.id.add_inex_label_amount));
        amountEditText = ((EditText)findViewById(R.id.add_inex_amount));
        dateButton = ((Button)findViewById(R.id.add_inex_date));
        saveButton = ((Button)findViewById(R.id.add_inex_save));
        inexOptionsSpinner = ((Spinner)findViewById(R.id.add_inex_options));

        dateButton.setOnClickListener(this);
        Calendar c = Calendar.getInstance();
        setDate(c);
        saveButton.setOnClickListener(this);
        inexOptionsSpinner.setOnItemSelectedListener(this);
    }

    private void setDate(Calendar c){
        dateButton.setTag(c.getTime().getTime());
        dateButton.setText(DATE_FORMATTER.format(c.getTime()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_inex_date:{
                long time = (Long)dateButton.getTag();
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(time));
                if(datePickerDialog == null){
                    datePickerDialog = new DatePickerDialog(this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                    Calendar c = Calendar.getInstance();
                                    c.set(i,i1,i2);
                                    setDate(c);
                                }
                            },
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH));
                }
                datePickerDialog.show();
                break;
            }
            case R.id.add_inex_save:{
                long time = (Long)dateButton.getTag();
                String description = descriptionEditText.getText().toString().trim();
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(this, String.format(getString(R.string.warning_field_cannot_be_empty), labelDescriptionTextView.getText()), Toast.LENGTH_SHORT).show();
                    return;
                }
                String amountString = amountEditText.getText().toString().trim();
                if(TextUtils.isEmpty(amountString)){
                    Toast.makeText(this, String.format(getString(R.string.warning_field_cannot_be_empty), labelAmountTextView.getText()), Toast.LENGTH_SHORT).show();
                    return;
                }
                Float amount = Float.parseFloat(amountString);
                InEx inEx = null;
                if(inexOptionsSpinner.getSelectedItemPosition() == 0){
                    inEx = new Expense(description, amount, time);
                }else{
                    inEx = new Income(description, amount, time);
                }
                if(DatabaseHelper.getInstance(this).save(inEx)){
                    finish();
                }else{
                    Toast.makeText(this,"Failed", Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        labelDescriptionTextView.setText(i == 0 ? R.string.label_description_spent : R.string.label_description_earned);
    }
}
