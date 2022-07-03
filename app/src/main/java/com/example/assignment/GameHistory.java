package com.example.assignment;

import android.util.Log;

import java.util.Stack;

// care taker for storing game history
public class GameHistory {
    private static final String TAG = "TTT Caretaker";
    private Stack<TicTacToe.TicTacToeMemento> gamehist = new Stack<>();

    public void saveGame(TicTacToe game) {
        gamehist.push(game.saveGame());
    }

    public void restoreGame(TicTacToe game) {
        if (!gamehist.isEmpty()) {
            game.restoreGame(gamehist.pop());
        } else {
            Log.d(TAG, "restoreGame: Already the oldest state of the Game");
        }
    }

    public void popGame() {
        gamehist.pop();
    }
}
