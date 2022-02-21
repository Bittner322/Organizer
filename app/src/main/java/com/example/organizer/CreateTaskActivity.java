package com.example.organizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class CreateTaskActivity extends AppCompatActivity {

    static EditText titleEt;
    static EditText descriptionEt;
    TextView dateTv;
    TextView timeTv;
    DatePicker datePicker;
    TimePicker timePicker;
    Button buttonAdd;
    Button buttonBack;

    SQLiteDatabase db;
    ContentValues cv;

    Boolean rowExists;
    int task_number;

    Calendar calendar;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        dateTv = findViewById(R.id.dateTv);
        timeTv = findViewById(R.id.timeTv);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonBack = findViewById(R.id.buttonBack);

        db = getBaseContext().openOrCreateDatabase("tasks.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (_ID INTEGER PRIMARY KEY, TASK_TEXT TEXT)");

        Bundle arguments = getIntent().getExtras();

        if(arguments != null) {
            titleEt.setText(arguments.getString("title"));
            descriptionEt.setText(arguments.getString("description"));

            buttonAdd.setText("Сохранить");

            db = getBaseContext().openOrCreateDatabase("tasks.db", MODE_PRIVATE, null);

            db.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (_ID INTEGER PRIMARY KEY, TASK_TEXT TEXT)");

            Cursor query = db.rawQuery("SELECT * FROM tasks_new;", null);

            int minute = timePicker.getMinute();
            int hour = timePicker.getHour();

            String timeFromTimePicker = String.valueOf(hour) + ":" + String.valueOf(minute);

            String editedTaskText = titleEt.getText().toString() + "\n" + descriptionEt.getText().toString() + "\n" + getDateFromDatePicker(datePicker).toString() + "\n" + timeFromTimePicker.toString() + "\n";

            int id = arguments.getInt("taskId");

            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        db.execSQL("UPDATE tasks_new SET TASK_TEXT = "+editedTaskText+" WHERE _ID = "+id+"");
                    }
                    catch (Exception e1) {
                        Toast.makeText(getApplicationContext(), e1.toString(), Toast.LENGTH_LONG);
                    }
                }
            });
        }

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
                createNotification();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(CreateTaskActivity.this, MainActivity.class);
                startActivity(backIntent);
                finish();
            }
        });
    }

    public void createTask() {
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        dateTv = findViewById(R.id.dateTv);
        timeTv = findViewById(R.id.timeTv);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonBack = findViewById(R.id.buttonBack);

        if(titleEt.length() == 0 || descriptionEt.length() == 0) {
            Toast.makeText(getApplicationContext(), "Вы вписали не все данные!", Toast.LENGTH_LONG).show();
        }
        else {
            String textFromTitleEt = titleEt.getText().toString();
            String textFromDescriptionEt = descriptionEt.getText().toString();

            int minute = timePicker.getMinute();
            int hour = timePicker.getHour();

            String timeFromTimePicker = String.valueOf(hour) + ":" + String.valueOf(minute);

            TextView taskTv = new TextView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(20,20,20, 0);
            taskTv.setLayoutParams(params);
            taskTv.setBackgroundColor(R.drawable.fields);
            taskTv.setPadding(10,10,10,10);

            taskTv.append(textFromTitleEt + "\n");
            taskTv.append(textFromDescriptionEt + "\n");
            taskTv.append(getDateFromDatePicker(datePicker) + "\n");
            taskTv.append(timeFromTimePicker);

            db = getBaseContext().openOrCreateDatabase("tasks.db", MODE_PRIVATE, null);

            db.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (_ID INTEGER PRIMARY KEY, TASK_TEXT TEXT)");

            Cursor query = db.rawQuery("SELECT * FROM tasks_new;", null);

            cv = new ContentValues();

            query.moveToLast();

            task_number = query.getInt(0);

            cv.put("_ID", task_number + 1);
            cv.put("TASK_TEXT", taskTv.getText().toString());

            db.insert("tasks_new",null, cv);

            Intent addIntent = new Intent(CreateTaskActivity.this, MainActivity.class);
            startActivity(addIntent);

            finish();
        }
    }

    public void createNotification() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent notifyIntent = new Intent(CreateTaskActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notifyIntent, 0);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static String getDateFromDatePicker (DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        return (day + "." + month + "." + year).toString();
    }
}