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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView mListView;
    private ContentsAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.main_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.fab).setOnClickListener(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        Cursor cursor = dbHelper.query(-1, -1, -1);
        if(cursor != null){
            mListAdapter = new ContentsAdapter(cursor);
            mListView.setAdapter(mListAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh(){
        if(mListAdapter != null){
            Cursor cursor = DatabaseHelper.getInstance(this).query(-1, -1, -1);
            mListAdapter.swapCursor(cursor);
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
            v.findViewById(R.id.inex_item_delete).setOnClickListener(MainActivity.this);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            view.findViewById(R.id.inex_item_delete).setTag(cursor.getLong(cursor.getColumnIndex(InEx.COLUMN_ID)));
            ((TextView)view.findViewById(R.id.inex_item_amount)).setText(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_AMOUNT)));
            ((TextView)view.findViewById(R.id.inex_item_description)).setText(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DESCRIPTION)));
            ((TextView)view.findViewById(R.id.inex_item_date)).setText(cursor.getString(cursor.getColumnIndex(InEx.COLUMN_DATE)));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.inex_item_delete:
                long id = (Long)view.getTag();
                if(DatabaseHelper.getInstance(this).delete(id)){
                    refresh();
                }
                break;
            case R.id.fab:
                startActivity(new Intent(MainActivity.this, AddInExActivity.class));
                break;
        }
    }
}
