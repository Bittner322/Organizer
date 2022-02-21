package com.example.organizer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;

    // Идентификатор канала
    private static String CHANNEL_ID = "Cat channel";

    static LinearLayout mainLayout;

    Button button;
    ListView lv;

    SQLiteDatabase db;

    ContentValues cv;

    ArrayList<String> taskList;
    ArrayAdapter<String> adapter;

    public boolean dbHasRecord() {
        Cursor query = db.rawQuery("SELECT * FROM tasks_new;", null);
        if (query != null && query.getCount() > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        lv = findViewById(R.id.lv);


        db = getBaseContext().openOrCreateDatabase("tasks.db", MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (_ID INTEGER PRIMARY KEY, TASK_TEXT TEXT)");

        cv = new ContentValues();

        Cursor query = db.rawQuery("SELECT * FROM tasks_new;", null);

        button = findViewById(R.id.button);
        mainLayout = findViewById(R.id.mainLayout);

        if(!dbHasRecord()) {
            cv.put("_ID", 0);
            cv.put("TASK_TEXT", "");
            db.insert("tasks_new", null, cv);
            cv.clear();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
                startActivity(intent);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                builder.setPositiveButton("УДАЛИТЬ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.execSQL("DELETE FROM tasks_new WHERE _ID = (SELECT _ID FROM tasks_new WHERE TASK_TEXT = '"+lv.getItemAtPosition(position).toString()+"')");
                        taskList.remove(position);
                        adapter.notifyDataSetChanged();

                    }
                });

                builder.setNeutralButton("РЕДАКТИРОВАТЬ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent editIntent = new Intent(MainActivity.this, EditTaskActivity.class);
                        Cursor taskIdCursor = db.rawQuery("SELECT _ID FROM tasks_new WHERE TASK_TEXT = ?;", new String[] {lv.getItemAtPosition(position).toString()});
                        taskIdCursor.moveToFirst();
                        int taskId = taskIdCursor.getInt(0);
                        editIntent.putExtra("taskId", taskId);
                        startActivity(editIntent);
                    }
                });

                builder.setNegativeButton("НАЗАД", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        taskList = new ArrayList<String>();

        db = getBaseContext().openOrCreateDatabase("tasks.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (_ID INTEGER PRIMARY KEY, TASK_TEXT TEXT)");

        Cursor query = db.rawQuery("SELECT * FROM tasks_new;", null);

        query.moveToFirst();

        while(query.moveToNext()) {
            String taskText = query.getString(1);
            taskList.add(taskText);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, taskList);
            lv.setAdapter(adapter);
        }
    }
}
