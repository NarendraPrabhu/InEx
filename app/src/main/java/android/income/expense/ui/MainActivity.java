package android.income.expense.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.income.expense.R;
import android.income.expense.data.DatabaseHelper;
import android.income.expense.data.InEx;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private ContentsAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.main_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddInExActivity.class));
            }
        });
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        Calendar c= Calendar.getInstance();
        Cursor cursor = dbHelper.query(c.get(Calendar.DAY_OF_MONTH),c.get(Calendar.MONTH),c.get(Calendar.YEAR));
        if(cursor != null){
            mListAdapter = new ContentsAdapter(cursor);
            mListView.setAdapter(mListAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mListAdapter != null){
            Calendar c = Calendar.getInstance();
            Cursor cursor = DatabaseHelper.getInstance(this).query(-1,-1,c.get(Calendar.YEAR));
            //Cursor cursor = DatabaseHelper.getInstance(this).query(-1,-1,-1);
            if(cursor != null) {
                mListAdapter.changeCursor(cursor);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ContentsAdapter extends CursorAdapter{

        public ContentsAdapter(Cursor c) {
            super(MainActivity.this, c, true);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_inex, viewGroup, false);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView)view.findViewById(R.id.inex_item_amount)).setText(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_AMOUNT)));
            ((TextView)view.findViewById(R.id.inex_item_description)).setText(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DESCRIPTION)));
            ((TextView)view.findViewById(R.id.inex_item_date)).setText(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DATE)));
        }
    }
}
