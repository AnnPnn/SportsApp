package com.anpn.sportsapp.todos;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.anpn.sportsapp.R;
import com.anpn.sportsapp.todos.contentprovider.MyTodoContentProvider;

/**
 * Экран для создания элемента списка тренировок
 */
public class EditActivity extends Activity {
    private EditText mTitleText;
    private EditText mBodyText;
    private Spinner mCategory;
    int icon;

    private Uri todoUri;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit);

        mCategory = findViewById(R.id.category);
        mTitleText = findViewById(R.id.todo_edit_summary);
        mBodyText = findViewById(R.id.todo_edit_description);
        Button confirmButton = findViewById(R.id.todo_edit_button);

        Bundle extras = getIntent().getExtras();

        todoUri = (bundle == null) ? null : (Uri) bundle
                .getParcelable(MyTodoContentProvider.CONTENT_ITEM_TYPE);

        if (extras != null) {
            todoUri = extras
                    .getParcelable(MyTodoContentProvider.CONTENT_ITEM_TYPE);

            fillData(todoUri);
        }

        confirmButton.setOnClickListener(view -> {
            if (TextUtils.isEmpty(mTitleText.getText().toString())) {
                makeToast();
            } else {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void fillData(Uri uri) {
        String[] projection = { TodoTable.COLUMN_SUMMARY,
                TodoTable.COLUMN_DESCRIPTION, TodoTable.COLUMN_CATEGORY };
        Cursor cursor = getContentResolver().query(uri, projection, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            String category = cursor.getString(cursor
                    .getColumnIndexOrThrow(TodoTable.COLUMN_CATEGORY));

            for (int i = 0; i < mCategory.getCount(); i++) {

                String s = (String) mCategory.getItemAtPosition(i);
                if (s.equalsIgnoreCase(category)) {
                    mCategory.setSelection(i);
                }
            }

            mTitleText.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(TodoTable.COLUMN_SUMMARY)));
            mBodyText.setText(cursor.getString(cursor
                    .getColumnIndexOrThrow(TodoTable.COLUMN_DESCRIPTION)));

            cursor.close();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putParcelable(MyTodoContentProvider.CONTENT_ITEM_TYPE, todoUri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    private void saveState() {
        String category = (String) mCategory.getSelectedItem();
        String summary = mTitleText.getText().toString();
        String description = mBodyText.getText().toString();
        icon = R.drawable.workout_p;

        switch (category) {
            case "run":
                icon = R.drawable.runner;
                break;
            case "ellipsoid":
                icon = R.drawable.ellipsoid;
                break;
            case "swimming":
                icon = R.drawable.swimming;
                break;
            case "walking":
                icon = R.drawable.walking;
                break;
            case "yoga":
                icon = R.drawable.meditation;
                break;
            case "dancing":
                icon = R.drawable.dance;
                break;
            case "pilates":
                icon = R.drawable.fitness;
                break;
        }

        if (description.length() == 0 && summary.length() == 0) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(TodoTable.COLUMN_CATEGORY, category);
        values.put(TodoTable.COLUMN_SUMMARY, summary);
        values.put(TodoTable.COLUMN_DESCRIPTION, description);
        values.put(TodoTable.COLUMN_ICON, icon);

        if (todoUri == null) {
            todoUri = getContentResolver().insert(
                    MyTodoContentProvider.CONTENT_URI, values);
        } else {
            getContentResolver().update(todoUri, values, null, null);
        }
    }

    private void makeToast() {
        Toast.makeText(EditActivity.this, "Please fill in the title field",
                Toast.LENGTH_LONG).show();
    }
}
