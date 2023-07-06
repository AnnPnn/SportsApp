package com.anpn.sportsapp.todos;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;

import com.anpn.sportsapp.CustomAdapter;
import com.anpn.sportsapp.R;
import com.anpn.sportsapp.StopwatchActivity;
import com.anpn.sportsapp.todos.contentprovider.MyTodoContentProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Экран со списком добавленных тренировок
 */
public class TodoActivity extends ListActivity implements


        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int DELETE_ID = Menu.FIRST + 1;
    private CustomAdapter adapter;

    /** Called when the activity is first created. */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        this.getListView().setDividerHeight(2);
        fillData();
        registerForContextMenu(getListView());


        registerForContextMenu(getListView());

        Button todo_main_button = findViewById(R.id.todo_main_button);
        todo_main_button.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), EditActivity.class)));


        //панель нижней навигации
        BottomNavigationView btNavView = findViewById(R.id.btNavView);

        //слушатель нажатий кнопок нижнего меню

        btNavView.setSelectedItemId(R.id.workoutPlan);
        btNavView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.stopwatch:
                    startActivity(new Intent(getApplicationContext(), StopwatchActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.workoutPlan:
                    return true;
            }
            return false;

        });

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == DELETE_ID) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                    .getMenuInfo();
            Uri uri = Uri.parse(MyTodoContentProvider.CONTENT_URI + "/"
                    + info.id);
            getContentResolver().delete(uri, null, null);
            fillData();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, EditActivity.class);
        Uri todoUri = Uri.parse(MyTodoContentProvider.CONTENT_URI + "/" + id);
        i.putExtra(MyTodoContentProvider.CONTENT_ITEM_TYPE, todoUri);

        startActivity(i);
    }



    private void fillData() {

        String[] picture = new String[]{TodoTable.COLUMN_ICON};
        int[] to = new int[] { R.id.title };

        getLoaderManager().initLoader(0, null, this);

        adapter = new CustomAdapter(this, R.layout.list_row,
                null, picture, to, 0);

        setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "delete");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { TodoTable.COLUMN_ID, TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_ICON };
        return new CursorLoader(this,
                MyTodoContentProvider.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
