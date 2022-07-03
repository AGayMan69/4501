package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

public class RecordActivity extends AppCompatActivity {
    private ListView gameRecordListView;
    private Button piechartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        gameRecordListView = findViewById(R.id.lv_game_record);
        piechartBtn = findViewById(R.id.btn_show_pie_chart);
        // open pie chart
        piechartBtn.setOnClickListener(v -> {
            Intent pieChartIntent = new Intent(getApplicationContext(), PieChartActivity.class);
            startActivity(pieChartIntent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadRecord();
    }

    private void loadRecord() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.assignment/tictactoeDB", null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.rawQuery("SELECT * FROM GamesLog",null);
        GameRecord.records = new GameRecord[cursor.getCount()];
        int counter = 0;
        while (cursor.moveToNext()) {
            @SuppressLint("Range") GameRecord record = new GameRecord(
                    cursor.getInt(cursor.getColumnIndex("gameID")),
                    cursor.getString(cursor.getColumnIndex("playDate")),
                    cursor.getString(cursor.getColumnIndex("playTime")),
                    cursor.getInt(cursor.getColumnIndex("duration")),
                    cursor.getString(cursor.getColumnIndex("winningStatus"))
            );
            // add records to individual
            GameRecord.records[counter++] = record;
        }
        PlayerRecordAdapter recordAdapter = new PlayerRecordAdapter(getApplicationContext(), GameRecord.records);
        // set list view
        gameRecordListView.setAdapter(recordAdapter);
    }
}