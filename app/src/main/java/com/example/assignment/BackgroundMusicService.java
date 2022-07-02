package com.example.assignment;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class BackgroundMusicService extends Service {
    private static final String TAG = "Music";
    private static int music = 0;
    private static MediaPlayer player;
    private static boolean mute;

    public static void setMusicSource(int source){
        music = source;
    }

    public IBinder onBind(Intent arg){
       return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, music);
        player.setLooping(true);
    }

    public int onStartCommand(Intent intent, int flag, int startID){
        player.setLooping(true);
        changeVolume();
        player.start();
        if (PlayActivity.pauseGame)
            player.pause();
        return Service.START_STICKY;
    }

    public static void changeMusic(int music, Context context) {
        Log.d(TAG, "onCreate: changing music");
        BackgroundMusicService.music  = music;
        player.release();
        player = MediaPlayer.create(context, music);
        player.setLooping(true);
        changeVolume();
        player.start();
    }

    public static void pause() {
        player.pause();
    }

    public static void resume() {
        player.start();
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public static String getMusic() {
       if (music == R.raw.main_background_music)
           return "main";
       else
           return "game";
    }

    public static void muteMusic(boolean mute) {
        BackgroundMusicService.mute = mute;
    }

    public static boolean isMute() {
       return mute;
    }

    public static void changeVolume() {
        if (mute)
            player.setVolume(0, 0);
        else
            player.setVolume(0.5f, 0.5f);
    }
}
