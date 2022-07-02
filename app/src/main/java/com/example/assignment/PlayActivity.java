package com.example.assignment;

import static android.view.View.VISIBLE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import pl.droidsonroids.gif.GifImageView;

public class PlayActivity extends AppCompatActivity {
    public static boolean pauseGame = false;
    private Instant beginTime, nowTime;
    private Runnable timeUpdate;
    private Handler timeHandler;
    private TextView tvTime;
    private Dialog winDialog;
    private Dialog loseDialog;
    private Dialog drawDialog;
    private Dialog pauseDialog;
    private Intent playerIntent;
    private int player;
    private int cpu;
    private CardView playerCardX;
    private CardView playerCardO;
    private CardView playerCard;
    private CardView CPUCard;
    private SoundPool soundPool;
    private TicTacToe.GameResult gameResult;
    private TicTacToe gameSession;
    private boolean WaitForCpuInput = false;
    private int board_size;
    private GameHistory gamehist;
    private ImageButton rewind;
    private TextView tvRewind, tvRewindCounter;
    private int rewindCount;
    private boolean isRewinding;
    private int gameSize;
    private GifImageView rewindSam;
    // sound effect
    int readySFX,
            fightSFX,
            chooseSFX,
            gameSetSFX,
            victory1SFX,
            victory2SFX,
            defeat1SFX,
            defeat2SFX,
            draw1SFX,
            draw2SFX,
            draw3SFX,
            continueSFX,
            rewindSFX,
            rewindSAMSFX;
    ImageButton[][] boardBtns;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        BackgroundMusicService.changeMusic(R.raw.game_background_music, getApplicationContext());
        BackgroundMusicService.pause();

        playerIntent = getIntent();
        if (playerIntent == null) {
            finish();
        }
        if (playerIntent.getIntExtra("mode", 3) == 5) {
            setContentView(R.layout.activity_play_five);
        } else
            setContentView(R.layout.activity_play);
        gamehist = new GameHistory();
        initialSound();
        initiateDialog();
        initialCard();
        initialBoardUI();
        startTimer();
        initialRewind();
        getReady();
    }

    private void initialRewind() {
        rewind = findViewById(R.id.btn_rewind);
        tvRewind = findViewById(R.id.tv_rewind);
        tvRewindCounter = findViewById(R.id.tv_rewindCount);
        rewind.setOnClickListener(v -> {
            rewindTime();
        });
        rewind.setVisibility(View.INVISIBLE);
        if (board_size == 5) {
            rewindCount = 5;
        } else {
            rewindCount = 2;
        }
        rewindSam = findViewById(R.id.gif_sam);
        isRewinding = false;
    }

    private void rewindTime() {
        if (!WaitForCpuInput && !isRewinding) {
            isRewinding = true;
            RelativeLayout overlay = findViewById(R.id.ready_overlay);
            overlay.setElevation(1 * getResources().getDisplayMetrics().density);
            Animation rewindAnimation = AnimationUtils.loadAnimation(this, R.anim.ready);
            rewindAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    tvRewind.setVisibility(VISIBLE);
                    rewindSam.setVisibility(VISIBLE);
                    soundPool.play(rewindSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    rewindCount--;
                    tvRewindCounter.setText(String.format("%d rewind left", rewindCount));
                    tvRewindCounter.setVisibility(VISIBLE);
                    Handler animateHandler = new Handler(Looper.getMainLooper());
                    animateHandler.postDelayed(() -> {
                        tvRewind.setVisibility(View.INVISIBLE);
                        tvRewindCounter.setVisibility(View.INVISIBLE);
                        rewindBoardUI();
                        overlay.setElevation(-1 * getResources().getDisplayMetrics().density);
                        gamehist.restoreGame(gameSession);
                        if (gameSession.getTurn() < 2 || rewindCount <= 0)
                            rewind.setVisibility(View.INVISIBLE);
                        rewindSam.setVisibility(View.GONE);
                        timeHandler.post(timeUpdate);
                        isRewinding = false;
                    }, 3000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            Handler samSmithHandler = new Handler(Looper.getMainLooper());
            timeHandler.removeCallbacksAndMessages(null);
            soundPool.play(rewindSAMSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
            samSmithHandler.postDelayed(() -> {
                tvRewind.startAnimation(rewindAnimation);
            }, 3000);
        }
    }

    private void rewindBoardUI() {
        TicTacToe.Move[] lastMoves = gameSession.getLastMove();
        for (int i = 0; i < lastMoves.length; i++) {
            boardBtns[lastMoves[i].getRow()][lastMoves[i].getCol()].setImageResource(0);
        }
    }

    private void startTimer() {
        tvTime = findViewById(R.id.tv_time);
        timeHandler = new Handler();
        beginTime = Instant.now();
        nowTime = beginTime.minusSeconds(1);

        timeUpdate = new Runnable() {
            @Override
            public void run() {
                nowTime = nowTime.plusSeconds(1);
                Duration timeDuration = Duration.between(beginTime, nowTime);
                tvTime.setText(String.format("%02d : %02d", timeDuration.toMinutes(), timeDuration.getSeconds()));
                timeHandler.postDelayed(timeUpdate, 1000);
            }
        };
        timeHandler.post(timeUpdate);
    }

    private void initialBoardUI() {
        board_size = playerIntent.getIntExtra("mode", 3);
        String idString;
        boardBtns = new ImageButton[board_size][board_size];
        for (int i = 0; i < board_size; i++) {
            for (int j = 0; j < board_size; j++) {
                final int row = i;
                final int col = j;
                idString = String.format("board_btn_%d", (row * board_size + col + 1));
                boardBtns[row][col] = (ImageButton) findViewById(getResources().getIdentifier(idString, "id", getPackageName()));
                final ImageButton btn = boardBtns[row][col];
                btn.setBackground(getDrawable(R.drawable.tic_button_effect));
                btn.setImageResource(0);
                btn.setOnClickListener(v -> {
                    clickBoard(btn, player, row, col);
                });
            }
        }
        for (ImageButton[] boardBtnRow : boardBtns) {
            for (ImageButton boardBtn : boardBtnRow) {
                boardBtn.setClickable(false);
            }
        }
    }

    private void clickBoard(ImageButton btn, int player, int row, int col) {
        if (WaitForCpuInput)
            return;

        String debug = String.format("Click row: %d col: %d", row, col);
        Log.d(TAG, debug);
        int src = (player == 1) ? R.drawable.player_o_card : R.drawable.player_x_card;
        try {
            gamehist.saveGame(gameSession);
            gameResult = gameSession.addMove(player, row, col);
            btn.setImageResource(src);
            soundPool.play(chooseSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
            gamehist.popGame();
            return;
        }
        // game is decide
        if (gameResult.getWinner() != 0) {
            gameEnd();
        } else {
            // start cpu turn
            WaitForCpuInput = true;
            cleanCard(playerCard);
            lightUpCard(CPUCard, cpu);
            int cpuSrc = (cpu == 1) ? R.drawable.player_o_card : R.drawable.player_x_card;
            Handler btnAnimHandler = new Handler(Looper.getMainLooper());
            btnAnimHandler.postDelayed(() -> {
                while (true) {
                    try {
                        // generate random number
                        int cpuRow = ThreadLocalRandom.current().nextInt(0, gameSession.getSize());
                        int cpuCol = ThreadLocalRandom.current().nextInt(0, gameSession.getSize());

                        gameResult = gameSession.addMove(cpu, cpuRow, cpuCol);
                        boardBtns[cpuRow][cpuCol].setImageResource(cpuSrc);
                        soundPool.play(chooseSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
                        Log.d(TAG, String.format("Generate cpu coordinate row: %d col: %d", cpuRow, cpuCol));
                        if (gameResult.getWinner() != 0) {
                            gameEnd();
                            break;
                        }
                        WaitForCpuInput = false;
                        cleanCard(CPUCard);
                        lightUpCard(playerCard, player);
                        if (gameSession.getTurn() == 2 && rewindCount > 0)
                            rewind.setVisibility(VISIBLE);
                        break;
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
                btnAnimHandler.removeCallbacksAndMessages(null);
            }, 700);

        }
    }

    private void gameEnd() {
        BackgroundMusicService.pause();
        rewind.setClickable(false);
        if (gameResult.getWinner() != 2) {
            int winplayer = gameResult.getWinner();
            showWinningMove();
            Handler btnAnimHandler = new Handler(Looper.getMainLooper());
            btnAnimHandler.postDelayed(() -> {
                if (winplayer == player) {
                    openWinDialog();
                } else {
                    openLoseDialog();
                }
                btnAnimHandler.removeCallbacksAndMessages(null);
            }, 4000);
        } else {
            openDrawDialog();
        }
        saveGameDB();
    }

    private void saveGameDB() {
        SQLiteDatabase db;
        try {
            db = SQLiteDatabase.openDatabase(
                    "data/data/com.example.assignment/tictactoeDB",
                    null,
                    SQLiteDatabase.CREATE_IF_NECESSARY);
            db.execSQL("CREATE TABLE IF NOT EXISTS" +
                    " GamesLog(gameID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "playDate TEXT," +
                    "playTime TEXT," +
                    "duration INTEGER," +
                    "winningStatus TEXT)");
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            long duration = Duration.between(beginTime, nowTime).getSeconds();
            String winstat;
            int winner = gameResult.getWinner();
            if (winner == 2) {
                winstat = "DRAW";
            } else if (winner == player) {
                winstat = "WIN";
            } else {
                winstat = "LOSE";
            }
            String sql = String.format(Locale.getDefault(), "INSERT INTO GamesLog(" +
                            "playDate, playTime, duration, winningStatus) values" +
                            "('%s', '%s', '%d', '%s')",
                    dateFormat.format(currentDate),
                    timeFormat.format(currentDate),
                    duration,
                    winstat);
            Log.d(TAG, String.format("Saving... %s", sql));
            db.execSQL(sql);
            db.close();
        } catch (SQLException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void showWinningMove() {
        ImageButton[] winBtns = new ImageButton[gameSession.getSize()];
        TicTacToe.WinType type = gameResult.getType();
        int size = gameSession.getSize();
        if (type == TicTacToe.WinType.ROW_WIN) {
            int row = gameResult.getIndex();
            for (int col = 0; col < size; col++) {
                winBtns[col] = boardBtns[row][col];
            }
        } else if (type == TicTacToe.WinType.COL_WIN) {
            int col = gameResult.getIndex();
            for (int row = 0; row < size; row++) {
                winBtns[row] = boardBtns[row][col];
            }
        } else if (type == TicTacToe.WinType.DIAGN_WIN) {
            for (int i = 0; i < size; i++) {
                winBtns[i] = boardBtns[i][i];
            }
        } else if (type == TicTacToe.WinType.DIAGP_WIN) {
            for (int col = 0; col < size; col++) {
                winBtns[col] = boardBtns[size - 1 - col][col];
            }
        } else {
            return;
        }
        soundPool.play(gameSetSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
        for (int i = 0; i < winBtns.length; i++) {
            winBtns[i].setBackground(
                    (gameResult.getWinner() == 1)
                            ? getDrawable(R.drawable.tic_button_change_o)
                            : getDrawable(R.drawable.tic_button_change_x)
            );
            AnimationDrawable animation = (AnimationDrawable) winBtns[i].getBackground();
            animation.setEnterFadeDuration(getResources().getInteger(R.integer.tic_button_change_delay) / 2);
            animation.start();
        }
    }

    private void initialSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder().build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        readySFX = soundPool.load(this, R.raw.ready, 1);
        fightSFX = soundPool.load(this, R.raw.fight, 1);
        chooseSFX = soundPool.load(this, R.raw.tictactoe_choose, 1);
        gameSetSFX = soundPool.load(this, R.raw.game_set, 1);
        victory1SFX = soundPool.load(this, R.raw.tictactoe_you_win, 1);
        victory2SFX = soundPool.load(this, R.raw.tictactoe_you_win_2, 1);
        defeat1SFX = soundPool.load(this, R.raw.tictactoe_you_lose, 1);
        defeat2SFX = soundPool.load(this, R.raw.tictactoe_you_lose_2, 1);
        draw1SFX = soundPool.load(this, R.raw.kekw, 1);
        draw2SFX = soundPool.load(this, R.raw.bruh_slow, 1);
        draw3SFX = soundPool.load(this, R.raw.bruh_song, 1);
        continueSFX = soundPool.load(this, R.raw.tictactoe_continue, 1);
        rewindSFX = soundPool.load(this, R.raw.rewind, 1);
        rewindSAMSFX = soundPool.load(this, R.raw.rewind_sam_smith, 1);
    }

    private void getReady() {
        timeHandler.removeCallbacks(timeUpdate);

        TextView tvReady = findViewById(R.id.ready);
        TextView tvFight = findViewById(R.id.fight);
        Animation readyAnim = AnimationUtils.loadAnimation(this, R.anim.ready);
        Animation fightAnim = AnimationUtils.loadAnimation(this, R.anim.fight);
        Animation fightVibrate = AnimationUtils.loadAnimation(this, R.anim.fightvibrate);
        readyAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                soundPool.play(readySFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
                RelativeLayout overlay = findViewById(R.id.ready_overlay);
                overlay.setElevation(1 * getResources().getDisplayMetrics().density);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Handler animateHandler = new Handler(Looper.getMainLooper());
                animateHandler.postDelayed(() -> {
                    tvReady.setVisibility(View.GONE);
                    tvFight.setVisibility(View.VISIBLE);
                    tvFight.startAnimation(fightAnim);
                    soundPool.play(fightSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
                    animateHandler.removeCallbacksAndMessages(null);
                }, 1300);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fightAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvFight.startAnimation(fightVibrate);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fightVibrate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Handler animHandler = new Handler(Looper.getMainLooper());
                animHandler.postDelayed(() -> {
                    tvFight.setVisibility(View.GONE);
                    BackgroundMusicService.resume();
                    startGame();
                    animHandler.removeCallbacksAndMessages(null);
                }, 1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        tvReady.startAnimation(readyAnim);
    }

    private void startGame() {
        for (ImageButton[] boardBtnRow : boardBtns) {
            for (ImageButton boardBtn : boardBtnRow) {
                boardBtn.setClickable(true);
            }
        }
        RelativeLayout overlay = findViewById(R.id.ready_overlay);
        overlay.setElevation(-1 * getResources().getDisplayMetrics().density);
        cpu = (player == -1) ? 1 : -1;
        gameSession = new TicTacToe(board_size);
        timeHandler.post(timeUpdate);
    }

    private void initialCard() {
        player = playerIntent.getIntExtra("player", 1);
        if (player == -1) {
            playerCard = playerCardX;
            CPUCard = playerCardO;
            cleanCard(playerCardO);
            lightUpCard(playerCardX, player);
            ((TextView) findViewById(R.id.tv_card_x)).setText(R.string.play_text_player);
            ((TextView) findViewById(R.id.tv_card_o)).setText(R.string.play_text_cpu);
        } else {
            playerCard = playerCardO;
            CPUCard = playerCardX;
            cleanCard(playerCardO);
            cleanCard(playerCardX);
            lightUpCard(playerCardO, player);
            ((TextView) findViewById(R.id.tv_card_o)).setText(R.string.play_text_player);
            ((TextView) findViewById(R.id.tv_card_x)).setText(R.string.play_text_cpu);
        }
    }

    private void lightUpCard(CardView card, int i) {
        card.setCardElevation(getResources().getDimension(R.dimen.choose_elevation));
        int color = (i == 1) ? R.color.glow_clr : R.color.glow_clr_red;
        card.setOutlineAmbientShadowColor(getColor(color));
        card.setOutlineSpotShadowColor(getColor(color));
    }

    private void cleanCard(CardView card) {
        playerCardO.setCardElevation(getResources().getDimension(R.dimen.reset_elevation));
        card.setOutlineAmbientShadowColor(getColor(R.color.black));
        card.setOutlineSpotShadowColor(getColor(R.color.black));
    }

    private void initiateDialog() {
        playerCardO = findViewById(R.id.player_o);
        playerCardX = findViewById(R.id.player_x);
        winDialog = new Dialog(this, R.style.GameDialogAnimation);
        winDialog.setContentView(R.layout.dialog_win);
        winDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getColor(R.color.transparent_black)));
        Button winOK = winDialog.findViewById(R.id.btn_win_ok);
        winOK.setOnClickListener(v -> {
            soundPool.play(continueSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);

            Handler dialogHandler = new Handler(Looper.getMainLooper());
            dialogHandler.postDelayed(() -> {
                winDialog.dismiss();
                restartedGame();
                dialogHandler.removeCallbacksAndMessages(null);
            }, 1000);
        });

        loseDialog = new Dialog(this, R.style.GameDialogAnimation);
        loseDialog.setContentView(R.layout.dialog_lose);
        loseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getColor(R.color.transparent_black)));
        Button loseOK = loseDialog.findViewById(R.id.btn_win_ok);
        loseOK.setOnClickListener(v -> {
            soundPool.play(continueSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);

            Handler dialogHandler = new Handler(Looper.getMainLooper());
            dialogHandler.postDelayed(() -> {
                loseDialog.dismiss();
                restartedGame();
                dialogHandler.removeCallbacksAndMessages(null);
            }, 1000);
        });

        drawDialog = new Dialog(this, R.style.GameDialogAnimation);
        drawDialog.setContentView(R.layout.dialog_draw);
        drawDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getColor(R.color.transparent_black)));
        Button drawOK = drawDialog.findViewById(R.id.btn_win_ok);
        drawOK.setOnClickListener(v -> {
            soundPool.play(continueSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);

            Handler dialogHandler = new Handler(Looper.getMainLooper());
            dialogHandler.postDelayed(() -> {
                drawDialog.dismiss();
                restartedGame();
                dialogHandler.removeCallbacksAndMessages(null);
            }, 1000);
        });

        pauseDialog = new Dialog(this, R.style.GameDialogAnimation);
        pauseDialog.setContentView(R.layout.dialog_pause);
        pauseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getColor(R.color.transparent_black)));
        Button resumeBtn = pauseDialog.findViewById(R.id.btn_resume);
        Button menuBtn = pauseDialog.findViewById(R.id.btn_main_menu);
        resumeBtn.setOnClickListener(v -> {
            soundPool.play(continueSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);

            Handler dialogHandler = new Handler(Looper.getMainLooper());
            dialogHandler.postDelayed(() -> {
                pauseDialog.dismiss();
                timeHandler.post(timeUpdate);
                pauseGame = false;
                BackgroundMusicService.resume();
                dialogHandler.removeCallbacksAndMessages(null);
            }, 1000);
        });
        menuBtn.setOnClickListener(v -> {
            soundPool.play(continueSFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);

            Handler dialogHandler = new Handler(Looper.getMainLooper());
            dialogHandler.postDelayed(() -> {
                pauseDialog.dismiss();
                pauseGame = false;
                Intent mainintent = new Intent(this, MainActivity.class);
                mainintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                BackgroundMusicService.changeMusic(R.raw.main_background_music, getApplicationContext());
                startActivity(mainintent);
                dialogHandler.removeCallbacksAndMessages(null);
            }, 1000);
        });
    }

    private void openDrawDialog() {
        drawDialog.show();
        soundPool.play(draw1SFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
        soundPool.play(draw2SFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
        soundPool.play(draw3SFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
    }

    private void openLoseDialog() {
        loseDialog.show();
        soundPool.play(defeat1SFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
        soundPool.play(defeat2SFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
    }

    private void openWinDialog() {
        winDialog.show();
        soundPool.play(victory1SFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
        soundPool.play(victory2SFX, MainActivity.soundEffectVolume, MainActivity.soundEffectVolume, 0, 0, 1);
    }

    private void restartedGame() {
        soundPool.release();
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        pauseGame = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pauseGame) {
            pauseDialog.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timeHandler.removeCallbacks(timeUpdate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacks(timeUpdate);
    }
}