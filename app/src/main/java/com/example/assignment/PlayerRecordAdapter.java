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

import java.util.Objects;

// player record list view adapter
public class PlayerRecordAdapter extends ArrayAdapter<GameRecord> {

    public PlayerRecordAdapter(Context applicationContext, GameRecord[] records) {
        super(applicationContext, 0, records);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GameRecord gameRecord = GameRecord.records[position];
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.gamerecord, parent, false);
        }
//        load the player record into the list view record
        TextView playTime = convertView.findViewById(R.id.tv_player_name);
        TextView playDate = convertView.findViewById(R.id.tv_player_head);
        TextView duration = convertView.findViewById(R.id.tv_duration);
        TextView status = convertView.findViewById(R.id.tv_status);
        playTime.setText(gameRecord.getTime());
        playDate.setText(gameRecord.getDate());
        String durationText = gameRecord.getDuration() + " sec";
        duration.setText(durationText);
        String result = gameRecord.getStatus();
        status.setText(result);

        // change color accord to the game result
        if (Objects.equals(result, "WIN")) {
            status.setTextColor(Color.parseColor("#a6d189"));
        } else if (Objects.equals(result, "LOSE")) {
            status.setTextColor(Color.parseColor("#e78284"));
        } else {
            status.setTextColor(ContextCompat.getColor(getContext(), R.color.record_status_clr));
        }
        return convertView;
    }
}
