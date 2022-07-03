package com.example.assignment;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

// application level life cycle owner to toggle the background music service
public class App extends Application implements DefaultLifecycleObserver {
    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//        initiating the background music service and sound effect volume
        BackgroundMusicService.muteMusic(false);
        MainActivity.SFXMute = false;
        BackgroundMusicService.setMusicSource(R.raw.main_background_music);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        // continue to play bgm when the application is in the foreground
        // continue to play bgm when the application is in the foreground
        startService(new Intent(getApplicationContext(), BackgroundMusicService.class));
        Log.d(TAG, "App onStart: music " + BackgroundMusicService.getMusic());
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d("App:", "Stopping");
        // stop to play bgm when the application is in the background
        stopService(new Intent(getApplicationContext(), BackgroundMusicService.class));
    }
}
