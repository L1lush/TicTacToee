package com.example.tictactoe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tictactoe.models.TicTacToeModel;
import com.google.android.material.appbar.MaterialToolbar;

/** Shared presentation layer for the original game and the later RTDB-prep copy. */
public abstract class BaseGameActivity extends AppCompatActivity {
    protected static final int[] BUTTON_IDS = {
            R.id.button00, R.id.button01, R.id.button02,
            R.id.button10, R.id.button11, R.id.button12,
            R.id.button20, R.id.button21, R.id.button22
    };

    protected TicTacToeModel model;
    protected TextView gameStatus;
    protected boolean gameFinished;

    protected abstract String gameModeLabel();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.game_toolbar);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(view -> finish());

        ((TextView) findViewById(R.id.game_mode)).setText(gameModeLabel());
        gameStatus = findViewById(R.id.game_status);
        model = new TicTacToeModel();
        findViewById(R.id.button_new_game).setOnClickListener(view -> resetBoard());
        updateStatus();
    }

    /** Called from each grid Button through android:onClick, exactly as in the course layout. */
    public void onCellClick(View view) {
        if (gameFinished) {
            return;
        }

        Button button = (Button) view;
        String[] position = button.getTag().toString().split(",");
        int row = Integer.parseInt(position[0]);
        int col = Integer.parseInt(position[1]);
        if (!model.isLegal(row, col)) {
            return;
        }

        String player = model.getCurrentPlayer();
        model.makeMove(row, col);
        updateButton(button, player);

        if (model.checkWin()) {
            gameFinished = true;
            gameStatus.setText("Player " + player + " wins!");
            highlightWinningLine();
        } else if (model.isTie()) {
            gameFinished = true;
            gameStatus.setText("It is a tie!");
        } else {
            model.changePlayer();
            updateStatus();
        }
    }

    protected void updateButton(Button button, String mark) {
        if (mark == null || mark.isEmpty()) {
            button.setText("");
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.cell_empty));
            return;
        }

        button.setText(mark);
        button.setTextColor(ContextCompat.getColor(this,
                "X".equals(mark) ? R.color.x_mark : R.color.o_mark));
        
        // Animate mark appearance
        button.setScaleX(0f);
        button.setScaleY(0f);
        button.setAlpha(0f);
        button.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    protected void highlightWinningLine() {
        int[][] winningLines = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Cols
                {0, 4, 8}, {2, 4, 6}             // Diagonals
        };

        for (int[] line : winningLines) {
            String c1 = model.getCell(line[0] / 3, line[0] % 3);
            String c2 = model.getCell(line[1] / 3, line[1] % 3);
            String c3 = model.getCell(line[2] / 3, line[2] % 3);

            if (!c1.isEmpty() && c1.equals(c2) && c1.equals(c3)) {
                for (int index : line) {
                    Button btn = findViewById(BUTTON_IDS[index]);
                    btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.win_highlight));
                    btn.animate().scaleX(1.1f).scaleY(1.1f).setDuration(500).start();
                }
                break;
            }
        }
    }

    protected void resetBoard() {
        model.resetGame();
        gameFinished = false;
        for (int id : BUTTON_IDS) {
            Button button = findViewById(id);
            button.setText("");
            button.setScaleX(1f);
            button.setScaleY(1f);
            button.setAlpha(1f);
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.cell_empty));
        }
        updateStatus();
    }

    protected void updateStatus() {
        gameStatus.setText("Player " + model.getCurrentPlayer() + "'s turn");
    }
}
