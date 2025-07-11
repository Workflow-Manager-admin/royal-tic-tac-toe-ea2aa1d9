package com.example.androidfrontend;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private enum Player { KING, QUEEN }
    private Player[][] board = new Player[3][3];
    private Player currentPlayer = Player.KING;
    private int kingScore = 0;
    private int queenScore = 0;
    private int gamesPlayed = 0;
    private boolean gameActive = true;

    private GridLayout gridLayout;
    private TextView scoreboardText;
    private TextView statusText;
    private Button resetButton;

    private MediaPlayer moveSound, winSound, drawSound;

    // Icon resources for King (X) and Queen (O)
    private int kingDrawable = R.drawable.ic_king;   // You will need to add custom SVGs or PNGs to res/drawable/
    private int queenDrawable = R.drawable.ic_queen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		// UI references
        gridLayout = findViewById(R.id.gameGrid);
        scoreboardText = findViewById(R.id.scoreboard);
        statusText = findViewById(R.id.status);
        resetButton = findViewById(R.id.resetBtn);

        // Sound setup (to enable sounds, place move.mp3, win.mp3, and draw.mp3 in res/raw/)
        // If resources are missing, MediaPlayer will remain null.
        moveSound = null; // MediaPlayer.create(this, R.raw.move);
        winSound = null;  // MediaPlayer.create(this, R.raw.win);
        drawSound = null; // MediaPlayer.create(this, R.raw.draw);

        setupGame();
        resetButton.setOnClickListener(v -> restartGame());
    }

    // PUBLIC_INTERFACE
    /** Handles tap on a cell (ImageView in grid). */
    public void onCellClicked(View view) {
        if (!gameActive) return;

        ImageView cell = (ImageView) view;
        int tag = Integer.parseInt(view.getTag().toString());
        int row = tag / 3;
        int col = tag % 3;

        if (board[row][col] != null) return; // Already played

        // Show X or O (king/queen), animate in
        board[row][col] = currentPlayer;
        cell.setImageResource(currentPlayer == Player.KING ? kingDrawable : queenDrawable);
        cell.setAlpha(0f);
        cell.animate().alpha(1f).setDuration(200);

        if (moveSound != null) moveSound.start();

        if (checkForWin(currentPlayer)) {
            if (currentPlayer == Player.KING) {
                kingScore++;
                showEndGame("King (X) wins!");
            } else {
                queenScore++;
                showEndGame("Queen (O) wins!");
            }
            if (winSound != null) winSound.start();
            updateScoreboard();
            gameActive = false;
        } else if (isBoardFull()) {
            showEndGame("It's a draw!");
            if (drawSound != null) drawSound.start();
            gameActive = false;
        } else {
            togglePlayer();
            updateStatusText();
        }
    }

    private void showEndGame(String result) {
        statusText.setText(result);
        statusText.setTextColor(ContextCompat.getColor(this,
                result.contains("King") ? R.color.primary : result.contains("Queen") ? R.color.accent : R.color.secondary));
    }

    private void updateScoreboard() {
        gamesPlayed++;
        String scoreboard = "Score   King (X): " + kingScore + "   Queen (O): " + queenScore + "   |   Games: " + gamesPlayed;
        scoreboardText.setText(scoreboard);
    }

    private void updateStatusText() {
        statusText.setText(currentPlayer == Player.KING ? "King's (X) turn" : "Queen's (O) turn");
        statusText.setTextColor(ContextCompat.getColor(this, currentPlayer == Player.KING ? R.color.primary : R.color.accent));
    }

    private boolean isBoardFull() {
        for (Player[] row : board) for (Player cell : row) if (cell == null) return false;
        return true;
    }

    /** Switches current player */
    private void togglePlayer() {
        currentPlayer = (currentPlayer == Player.KING ? Player.QUEEN : Player.KING);
    }

    // PUBLIC_INTERFACE
    /** Resets the current session game board, retains the scoreboard. */
    private void restartGame() {
        for (int i = 0; i < 3 * 3; i++) {
            ImageView cell = (ImageView) gridLayout.getChildAt(i);
            cell.setImageDrawable(null);
        }
        setupGame();
    }

    private void setupGame() {
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) board[i][j] = null;
        currentPlayer = (gamesPlayed % 2 == 0) ? Player.KING : Player.QUEEN; // Alternate who starts each game
        gameActive = true;
        updateStatusText();
    }

    /** Checks win condition for given player. */
    private boolean checkForWin(Player player) {
        // rows, cols
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return true;
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) return true;
        }
        // diags
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return true;
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) return true;
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (moveSound != null) moveSound.release();
        if (winSound != null) winSound.release();
        if (drawSound != null) drawSound.release();
    }
}
