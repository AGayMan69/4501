package com.example.assignment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

// list view adapter to generate view
public class RankAdapter extends ArrayAdapter<JSONObject> {

    public RankAdapter(Context applicationContext, List<JSONObject> records) {
        super(applicationContext, 0, records);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        JSONObject rankRecord = RankActivity.playerList.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.gamerank, parent, false);
        }
        TextView playerName = convertView.findViewById(R.id.tv_player_name);
        TextView playerDuration = convertView.findViewById(R.id.tv_duration);
        try {
            playerName.setText(rankRecord.getString("Name"));
            String duration = rankRecord.getInt("Duration") + " sec";
            playerDuration.setText(duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}
