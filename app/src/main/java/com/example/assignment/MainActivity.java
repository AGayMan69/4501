package com.example.assignment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.EnumMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    public enum buttonType {
        PLAY,
        RANK,
        RECORD,
        CLOSE
    }
    Map<buttonType, Button> buttonMap;
    private SoundPool soundPool;
    private int menu_chooseSFX, menu_playSFX;
    public static float soundEffectVolume;
    public static boolean SFXMute;
    private ImageButton musicVolumeBtn, sfxVolumeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Enum map for storing the 4 menu button
        buttonMap = new EnumMap(buttonType.class);

        Button playButton = findViewById(R.id.btn_Play);
        Button rankButton = findViewById(R.id.btn_Ranking);
        Button recordButton = findViewById(R.id.btn_Records);
        Button closeButton = findViewById(R.id.btn_Close);
        buttonMap.put(buttonType.PLAY, playButton);
        buttonMap.put(buttonType.RANK, rankButton);
        buttonMap.put(buttonType.RECORD, recordButton);
        buttonMap.put(buttonType.CLOSE, closeButton);
        Animation playVibrate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.icon_vibrate);
        playButton.startAnimation(playVibrate);
//        adding shadow to the menu buttons stored in the map
        for (Button btn: buttonMap.values()) {
            btn.setShadowLayer(4, 4, 4, Color.BLACK);
        }
//        start choose player activity when play button is clicked
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTicToe();
            }
        });

        // show ranking activity the ranking button is clicked
        rankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRanking();
            }
        });

        // show player record activity when the record is clicked
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecord();
            }
        });
        closeButton.setOnClickListener(v -> {
            // creating a dialog to prompt the user whether to close the application or not
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure??")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // close the application
                           finish();
                           System.exit(0);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        // close the dialog
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            builder.show();
        });

        musicVolumeBtn = findViewById(R.id.btn_music_volume);
        sfxVolumeBtn = findViewById(R.id.btn_sfx_volume);

        // toggle the music (muted/ not muted)
        musicVolumeBtn.setOnClickListener(v -> {
            toggleMusic();
        });

        // toggle the sound effect (muted/ not muted)
        sfxVolumeBtn.setOnClickListener(v -> {
            toggleSFX();
            changeSFXVolume();
        });

        changeSFXVolume();
        loadSound();
    }

    // toggle the background service volume
    private void toggleMusic() {
       BackgroundMusicService.muteMusic(!BackgroundMusicService.isMute());
       BackgroundMusicService.changeVolume();
       changeMusicIcon();
    }

    //        change the music icon to represent whether the music is muted or not
    private void changeMusicIcon() {
        if (BackgroundMusicService.isMute()) {
            musicVolumeBtn.setImageResource(R.drawable.music_mute);
        } else {
            musicVolumeBtn.setImageResource(R.drawable.music_unmute);
        }
    }

    private void toggleSFX() {
      SFXMute = !SFXMute;
      changeSFXIcon();
    }

    private void changeSFXIcon() {
        if (SFXMute) {
            sfxVolumeBtn.setImageResource(R.drawable.sfx_mute);
        } else {
            sfxVolumeBtn.setImageResource(R.drawable.sfx_unmute);
        }
    }

    // setting the sound effect volume for the sound pool
    private void changeSFXVolume() {
       if (SFXMute) {
           soundEffectVolume = 0;
       } else {
           soundEffectVolume = 0.5f;
       }
    }

    // loading the main menu button sound effect into the sound pool
    private void loadSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder().build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        menu_chooseSFX = soundPool.load(this, R.raw.main_menu_choose, 1);
        menu_playSFX = soundPool.load(this, R.raw.main_menu_play, 1);
    }

    public void playTicToe() {
        Intent playerIntent = new Intent(this, ChoosePlayerActivity.class);
        soundPool.play(menu_playSFX, soundEffectVolume, soundEffectVolume, 0,0, 1);
        startActivity(playerIntent);
        // enable fade in transition animation
        overridePendingTransition(TransitionAnimation.OPENENTERING, TransitionAnimation.OPENEXITING);
    }

    public void showRanking() {
        Intent rankIntent = new Intent(this, RankActivity.class);
        soundPool.play(menu_chooseSFX, soundEffectVolume, soundEffectVolume, 0,0, 1);
        startActivity(rankIntent);
        // enable fade in transition animation
        overridePendingTransition(TransitionAnimation.OPENENTERING, TransitionAnimation.OPENEXITING);
    }

    public void showRecord() {
        Intent recordIntent = new Intent(this, RecordActivity.class);
        soundPool.play(menu_chooseSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0,0, 1);
        startActivity(recordIntent);
        // enable fade in transition animation
        overridePendingTransition(TransitionAnimation.OPENENTERING, TransitionAnimation.OPENEXITING);
    }

    @Override
    protected void onStart() {
        super.onStart();
        changeMusicIcon();
        changeSFXIcon();
    }

}