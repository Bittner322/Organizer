package com.example.organizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    Button buttonSaveChanges;
    EditText titleEt;
    EditText descriptionEt;
    TextView dateTv;
    TextView timeTv;
    DatePicker datePicker;
    TimePicker timePicker;
    Button buttonBack;

    SQLiteDatabase db;
    ContentValues cv;

    Boolean rowExists;
    int task_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        dateTv = findViewById(R.id.dateTv);
        timeTv = findViewById(R.id.timeTv);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        buttonBack = findViewById(R.id.buttonBack);

        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);

        Bundle arguments = getIntent().getExtras();

        if(arguments != null) {
            titleEt.setText(arguments.getString("title"));
            descriptionEt.setText(arguments.getString("description"));

            db = getBaseContext().openOrCreateDatabase("tasks.db", MODE_PRIVATE, null);

            db.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (_ID INTEGER PRIMARY KEY, TASK_TEXT TEXT)");

            Cursor query = db.rawQuery("SELECT * FROM tasks_new;", null);

            int minute = timePicker.getMinute();
            int hour = timePicker.getHour();

            String timeFromTimePicker = String.valueOf(hour) + ":" + String.valueOf(minute);


            buttonSaveChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String editedTaskText = titleEt.getText().toString() + "\n" + descriptionEt.getText().toString() + "\n" + getDateFromDatePicker(datePicker).toString() + "\n" + timeFromTimePicker.toString();
                        cv = new ContentValues();
                        int id = arguments.getInt("taskId");
                        cv.put("TASK_TEXT", editedTaskText);
                        db.update("tasks_new", cv, "_ID = ?", new String[] { String.valueOf(id) });
                        finish();
                    }
                    catch (Exception e1) {
                        Toast.makeText(getApplicationContext(), e1.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }
    }

    public static String getDateFromDatePicker (DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        return (day + "." + month + "." + year).toString();
    }
}