package com.example.assignment;


import android.util.Log;

public class TicTacToe {
    private static final String TAG = "Tic Tac Toe";

    private int[][] board;
    private final int size;
    private int[] rowScore, colScore;
    // default to 0
    private int diagNScore, diagPScore;
    private int turn;
    private GameResult result;
    private int moveCount;
    private Move[] lastMove;

    public enum WinType {
        NOT_DECIDE,
        ROW_WIN,
        COL_WIN,
        DIAGN_WIN,
        DIAGP_WIN
    }
    public class GameResult {
        private int winner;
        private WinType type;
        private int index;

        public GameResult() {
            winner = 0;
            type = WinType.NOT_DECIDE;
            index = -1;
        }

        public GameResult(int winner) {
           this.winner = winner;
           type = WinType.NOT_DECIDE;
           index = -1;
        }

        public GameResult(int winner, WinType type) {
            this.winner = winner;
            this.type = type;
            index = -1;
        }

        public GameResult(int winner, WinType type, int index) {
            this.winner = winner;
            this.type = type;
            this.index = index;
        }

        public int getWinner() {
//            Log.d(TAG, String.format("getWinner: Winner is %d", winner) );
            return winner;
        }

        public WinType getType() {
            return type;
        }

        public int getIndex() {
            return index;
        }
    }
    public TicTacToe() {
        this(3);
    }

    public TicTacToe(int size) {
        this.size = size;
        this.board = new int[this.size][this.size];
        rowScore = new int[this.size];
        colScore = new int[this.size];
        result = new GameResult();
        moveCount = 0;
        lastMove = new Move[2];
    }

    public GameResult addMove(int player, int row, int col) throws IllegalArgumentException {
        if (!(player == -1 || player == 1)) {
            throw new IllegalArgumentException("Invalid Player");
        }
        if (col < 0 || row < 0 || col >= size || row >= size) {
            throw new IllegalArgumentException("Exceed Board Boundary");
        }
        if (board[row][col] != 0){
           throw new IllegalArgumentException("Already Occupied");
        }
        if (result.getWinner() != 0) {
            throw new IllegalArgumentException("Game is Completed");
        }
        board[row][col] = player;
        rowScore[row] += player;
        colScore[col] += player;
        // add negative diagonal score
        if (col == row) {
           diagNScore += player;
        }
        if (row == size - 1 - col) {
            diagPScore += player;
        }

        // check winner
        turn++;
        Log.d(TAG, String.format("addMove: rowScore[%d]: %d colScore[%d]: %d diagPScore: %d diagNScore: %d",
                row, rowScore[row], col, colScore[col], diagPScore, diagNScore));
        if (Math.abs(rowScore[row])  == size) {
            result = new GameResult(player, WinType.ROW_WIN, row);
        }
        else if (Math.abs(colScore[col])  == size)
        {
            result = new GameResult(player, WinType.COL_WIN, col);
        }
        else if (Math.abs(diagPScore)  == size)
        {
            result = new GameResult(player, WinType.DIAGP_WIN);
        }
        else if (Math.abs(diagNScore)  == size)
        {
            result = new GameResult(player, WinType.DIAGN_WIN);
        }
        else if (turn == size * size)
        {
           result = new GameResult(2);
        }
        Log.d(TAG, String.format("addMove: Turn %d", turn) );
        moveCount = ++moveCount % 2;
        lastMove[moveCount]  = new Move(player, col, row);
        return getResult();
    }

    public GameResult getResult() {
        return result;
    }

    public int[][] getBoard() {
        int[][] cloneBoard = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
           cloneBoard[i] = board[i].clone();
        }
        return cloneBoard;
    }

    public int[] getRowScore() {
        return rowScore.clone();
    }

    public int[] getColScore() {
        return colScore.clone();
    }

    public int getDiagNScore() {
        return diagNScore;
    }

    public int getDiagPScore() {
        return diagPScore;
    }

    public int getTurn() {
        return turn;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public Move[] getLastMove() {
        return lastMove.clone();
    }

    public int getSize() {
        return size;
    }

    public TicTacToeMemento saveGame(){
        return new TicTacToeMemento(
               getBoard(),
               getRowScore(),
               getColScore(),
               getDiagNScore(),
               getDiagPScore(),
               getTurn(),
               getMoveCount(),
               getLastMove()
        );
    }

    public void restoreGame(TicTacToeMemento memento){
        board = memento.getBoard();
        rowScore = memento.getRowScore();
        colScore = memento.getColScore();
        diagNScore = memento.getDiagNScore();
        diagPScore = memento.getDiagPScore();
        turn = memento.getTurn();
        moveCount = memento.moveCount;
        lastMove = memento.getLastMove();
    }

    static class TicTacToeMemento {
        int[][] board;
        int[] rowScore;
        int[] colScore;
        int diagNScore;
        int diagPScore;
        int turn;
        int moveCount;
        Move[] lastMove;

        public TicTacToeMemento(int[][] board, int[] rowScore, int[] colScore, int diagNScore, int diagPScore, int turn, int moveCount, Move[] lastMove) {
            this.board = board;
            this.rowScore = rowScore;
            this.colScore = colScore;
            this.diagNScore = diagNScore;
            this.diagPScore = diagPScore;
            this.turn = turn;
            this.moveCount = moveCount;
            this.lastMove = lastMove;
        }

        public int[][] getBoard() {
            return board;
        }

        public int[] getRowScore() {
            return rowScore;
        }

        public int[] getColScore() {
            return colScore;
        }

        public int getDiagNScore() {
            return diagNScore;
        }

        public int getDiagPScore() {
            return diagPScore;
        }

        public int getTurn() {
            return turn;
        }

        public int getMoveCount() {
            return moveCount;
        }

        public Move[] getLastMove() {
            return lastMove;
        }
    }
    public class Move {
        private int player;
        private int col;
        private int row;

        public Move(int player, int col, int row) {
            this.player = player;
            this.col = col;
            this.row = row;
        }

        public int getPlayer() {
            return player;
        }

        public int getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }
    }
}
