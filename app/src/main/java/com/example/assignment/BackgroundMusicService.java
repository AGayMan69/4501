package com.example.assignment;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


// a service that run media player for playing the background music
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
        // looping the bgm
        player.setLooping(true);
        changeVolume();
        player.start();
        if (PlayActivity.pauseGame)
            player.pause();
        return Service.START_STICKY;
    }

    // changing the music source for the media player
    public static void changeMusic(int music, Context context) {
        Log.d(TAG, "onCreate: changing music");
        BackgroundMusicService.music  = music;
        player.release();
        player = MediaPlayer.create(context, music);
        player.setLooping(true);
        changeVolume();
        player.start();
    }

    // pausing the media player / music
    public static void pause() {
        player.pause();
    }

    // resuming the media player / music
    public static void resume() {
        player.start();
    }

    // destroy the media player
    @Override
    public void onDestroy() {
        player.stop();
        player.release();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    // return the which of music is playing currently
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

    // mute if the muted option is selected on the main menu
    public static void changeVolume() {
        if (mute)
            player.setVolume(0, 0);
        else
            player.setVolume(0.5f, 0.5f);
    }
}
