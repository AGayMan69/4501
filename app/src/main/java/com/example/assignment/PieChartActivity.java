package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class PieChartActivity extends AppCompatActivity {

    private Panel piechart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        piechart = new Panel(this);
        setContentView(piechart);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private class Panel extends View {
        int winCount, loseCount, drawCount, totalGame;
        public Panel(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            loadData(canvas);
        }

        private void loadData(Canvas c) {
            SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.example.assignment/tictactoeDB"
                    , null
            ,SQLiteDatabase.OPEN_READONLY);
            winCount = (int) DatabaseUtils.queryNumEntries(db, "GamesLog", "winningStatus=?", new String[] {"WIN"});
            loseCount = (int) DatabaseUtils.queryNumEntries(db, "GamesLog", "winningStatus=?", new String[] {"LOSE"});
            drawCount = (int) DatabaseUtils.queryNumEntries(db, "GamesLog", "winningStatus=?", new String[] {"DRAW"});
            totalGame = winCount + loseCount + drawCount;
            float winDegree = (winCount / (float) totalGame) * 360;
            float loseDegree = (loseCount / (float) totalGame) * 360;
            float drawDegree = (drawCount/ (float) totalGame) * 360;
            int rectX = (getWidth() - getHeight() / 2)/2;
            int rectY = getHeight() / 4;
            int lblRectX = getWidth() - 250;
            int lblRectY = getHeight() - 250;
            int lblRectSize = 50;
            RectF rec = new RectF(
                    rectX,
                    rectY,
                    getWidth() - rectX,
                    getHeight() - rectY
                    );
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            c.drawColor(Color.parseColor("#232634"));
            paint.setTextSize(40);

            // drawing winning part
            paint.setColor(Color.parseColor("#a6d189"));
            c.drawArc(rec, 0, winDegree, true, paint);
            c.drawRect(lblRectX, lblRectY, lblRectX + lblRectSize, lblRectY + lblRectSize, paint);
            paint.setColor(Color.WHITE);
            c.drawText("WIN", lblRectX + lblRectSize + 35, lblRectY + lblRectSize - 10, paint);


            lblRectY += lblRectSize + 20;


            // drawing losing part
            paint.setColor(Color.parseColor("#e78284"));
            c.drawArc(rec, winDegree, loseDegree, true, paint);
            c.drawRect(lblRectX, lblRectY, lblRectX + lblRectSize, lblRectY + lblRectSize, paint);
            paint.setColor(Color.WHITE);
            c.drawText("LOSE", lblRectX + lblRectSize + 35, lblRectY + lblRectSize - 10, paint);

            lblRectY += lblRectSize + 20;


            // drawing draw part
            paint.setColor(Color.parseColor("#ef9f76"));
            c.drawArc(rec, winDegree+loseDegree, drawDegree, true, paint);
            c.drawRect(lblRectX, lblRectY, lblRectX + lblRectSize, lblRectY + lblRectSize, paint);
            paint.setColor(Color.WHITE);
            c.drawText("DRAW", lblRectX + lblRectSize + 35, lblRectY + lblRectSize - 10, paint);



            paint.setTextSize(80);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            paint.setColor(Color.parseColor("#74c7ec"));
            c.drawText("PLAYER RECORD", (int) (c.getWidth()/2), 150, paint);
        }
    }


}