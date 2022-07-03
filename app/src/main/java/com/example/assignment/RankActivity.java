package com.example.assignment;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankActivity extends AppCompatActivity {
    private static final String DATAURL = "http://10.0.2.2/android/ranking_api.php";
    public static List<JSONObject> playerList;
    private RankDownloadTask task;
    private ListView gameRankListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        task = new RankDownloadTask();
        gameRankListView = findViewById(R.id.lv_game_ranking);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // start the rank download async task
        task.execute(DATAURL);
    }

    // rank download async task
    private class RankDownloadTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... values) {
            {

                StringBuilder result = new StringBuilder();
                try {
                    URL url = new URL(values[0]);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("GET");
                    con.connect();

                    InputStream inputStream = con.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    inputStream.close();
                    con.disconnect();
                    return result.toString();
                } catch (IOException e) {
                    Log.d(TAG, String.format("loadRanking: %s", e.getMessage()) );
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null)
                return;
            try {
                JSONArray playerArr = new JSONArray(s);
                playerList = new ArrayList<>();
                for (int i = 0; i < playerArr.length(); i++) {
                   playerList.add(playerArr.getJSONObject(i));
                }

                // sort(Merge) the player list array (ascending duration)
                Collections.sort(playerList, new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject o1, JSONObject o2) {
                        int o1Int = 0;
                        int o2Int = 0;
                        try {
                            o1Int = (int) o1.get("Duration");
                            o2Int = (int) o2.get("Duration");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return o1Int - o2Int;
                    }
                });
                RankAdapter recordAdapter = new RankAdapter(getApplicationContext(), RankActivity.playerList);
                gameRankListView.setAdapter(recordAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        overridePendingTransition(TransitionAnimation.CLOSEENTERING, TransitionAnimation.CLOSEEXITING);
        super.onDestroy();
    }
}