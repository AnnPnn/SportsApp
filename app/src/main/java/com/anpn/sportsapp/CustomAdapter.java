package com.anpn.sportsapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.anpn.sportsapp.todos.TodoTable;

/**
 * Адаптер для размещения элементов с записями в лист
 */
public class CustomAdapter extends SimpleCursorAdapter {
    private final int layout;
    private final LayoutInflater inflater;

    public CustomAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
        super(context, layout, c, from, to, flag);
        this.layout = layout;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        TextView title = view.findViewById(R.id.title);
        ImageView image = view.findViewById(R.id.imageWorkout);

        int Title_index = cursor.getColumnIndexOrThrow(TodoTable.COLUMN_SUMMARY);
        int Artist_index = cursor.getColumnIndexOrThrow(TodoTable.COLUMN_ICON);

        title.setText(cursor.getString(Title_index));
        image.setImageResource(Integer.parseInt(cursor.getString(Artist_index)));

    }

}
