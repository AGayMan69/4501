package com.example.assignment;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

public class ChoosePlayerActivity extends AppCompatActivity {
    private TextView confirmBtn, hyperBtn;
    private CardView cardO;
    private CardView cardX;
    private CardView lastSelect;
    private int player;
    private Animation checkAni;
    private boolean selected;
    private Animation confirmFadeAnim, hyperVibrateAnim;
    private SoundPool soundPool;
    private int selectColor;
    private Animation confirmAni;
    private long lastPlayClickTime = 0;

    private int
            chooseSFX,
            confirmSFX,
            anounceSFX,
            hyperShowSFX,
            hyperConfirmSFX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_player);
        TextView hintText = findViewById(R.id.tv_select);
        confirmBtn = findViewById(R.id.btn_confirm);
        hyperBtn = findViewById(R.id.btn_hyper);
        cardO = findViewById(R.id.player_o);
        cardX = findViewById(R.id.player_x);
        selected = false;
        confirmBtn.setVisibility(View.INVISIBLE);
        hyperBtn.setVisibility(View.INVISIBLE);

        loadSound();
        Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.flashing);
        hintText.startAnimation(animation);
        confirmBtn.setOnClickListener(v -> {
            startGame(3);
            confirmBtn.startAnimation(confirmAni);
        });
        hyperBtn.setOnClickListener(v -> {
            startGame(5);
            hyperBtn.startAnimation(confirmAni);
        });
        cardO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPlayer(cardO);
            }
        });
        cardX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPlayer(cardX);
            }
        });
        getAnimation();
        MediaPlayer player = MediaPlayer.create(this, R.raw.choose_announce);
        if (!MainActivity.SFXMute)
            player.start();
//        gameLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            BackgroundMusicService.changeMusic(R.raw.main_background_music, getApplicationContext());
//        });
    }

    private void loadSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder().build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        chooseSFX = soundPool.load(this, R.raw.choose_player, 1);
        confirmSFX = soundPool.load(this, R.raw.choose_confirm, 1);
        anounceSFX = soundPool.load(this, R.raw.choose_announce, 1);
        hyperShowSFX = soundPool.load(this, R.raw.hyper_show, 1);
        hyperConfirmSFX = soundPool.load(this, R.raw.hyper, 1);
        soundPool.play(anounceSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
    }

    private void getAnimation() {
        checkAni = new ScaleAnimation(
                1f, 1.1f,
                1f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        checkAni.setFillAfter(true);
        checkAni.setDuration(250);
        checkAni.setInterpolator(getBaseContext(), android.R.anim.bounce_interpolator);
        checkAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                lastSelect.setCardElevation(getResources().getDimension(R.dimen.choose_elevation));
                lastSelect.setOutlineSpotShadowColor(getColor(selectColor));
                lastSelect.setOutlineAmbientShadowColor(getColor(selectColor));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        confirmFadeAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fade_in_slide_in);
        hyperVibrateAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fightvibrate);
    }

    private void startGame(int size) {
        if ((SystemClock.elapsedRealtime() - lastPlayClickTime) < 3000) {
            return;
        }
        lastPlayClickTime = SystemClock.elapsedRealtime();
        confirmAni = AnimationUtils.loadAnimation(getBaseContext(), R.anim.zoom_out);
        confirmAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent gameIntent = new Intent(getBaseContext(), PlayActivity.class);
                gameIntent.putExtra("mode", size);
                gameIntent.putExtra("player", player);
                ActivityOptionsCompat options;
                if (player == 1) {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            ChoosePlayerActivity.this, cardO, ViewCompat.getTransitionName(cardO)
                    );
                } else {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            ChoosePlayerActivity.this, cardX, ViewCompat.getTransitionName(cardX)
                    );
                }
                confirmBtn.setClickable(true);
                hyperBtn.setClickable(true);
                startActivityForResult(gameIntent, 666, options.toBundle());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (size == 3) {
            soundPool.play(confirmSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
        } else {
            soundPool.play(hyperConfirmSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
        }

    }

    private void selectPlayer(CardView card) {
        if (!selected) {
            selected = true;
            confirmBtn.setVisibility(View.VISIBLE);
            confirmBtn.startAnimation(confirmFadeAnim);
            Handler animateHandler = new Handler(Looper.getMainLooper());
            animateHandler.postDelayed(() -> {
                hyperBtn.setVisibility(View.VISIBLE);
                hyperBtn.startAnimation(hyperVibrateAnim);
                soundPool.play(hyperShowSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
            }, 700);
        }
        if (card != lastSelect) {
            if (card == cardO) {
                cardX.clearAnimation();
                clearStyle(cardX);
                Log.d(TAG, "selectPlayer: setting to player O");
                player = 1;
            } else {
                cardO.clearAnimation();
                clearStyle(cardO);
                Log.d(TAG, "selectPlayer: setting to player X");
                player = -1;
            }
            Log.d(TAG, "selectPlayer: Starting Animation");
            lastSelect = card;
            selectColor = (player == 1) ? R.color.glow_clr : R.color.glow_clr_red;
            soundPool.play(chooseSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
            Handler animateHandler = new Handler(Looper.getMainLooper());
            animateHandler.postDelayed(() -> {
                card.startAnimation(checkAni);
            }, 300);
        }
    }

    private void clearStyle(CardView card) {
        card.setCardElevation(getResources().getDimension(R.dimen.reset_elevation));
        card.setOutlineAmbientShadowColor(getColor(R.color.black));
        card.setOutlineSpotShadowColor(getColor(R.color.black));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: returned");
        confirmBtn.setEnabled(true);
        hyperBtn.setEnabled(true);
        BackgroundMusicService.changeMusic(R.raw.main_background_music, getBaseContext());
        soundPool.play(anounceSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
    }
}