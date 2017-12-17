package a4096xedition.corp.axeleration.com.a4096_6x6_christmasxedition;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int ROWS = 4;
    public static final int COLS = 4;
    private TextView[][] viewArr;
    private ViewGroup table;
    private TextView scoreView;
    private int score;
    private String[][] undoMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("Score", MODE_PRIVATE);
        int high_score = prefs.getInt("high_score", 0);
        TextView bestScore = findViewById(R.id.bestScoreTxt);
        bestScore.setText(String.valueOf(high_score));
        viewArr = new TextView[ROWS][COLS];
        Button restartBtn = findViewById(R.id.restartBtn);
        Button undoBtn = findViewById(R.id.undoBtn);
        table = findViewById(R.id.gameBoardLayout);
        int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
        table.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, widthPixels));
        scoreView = findViewById(R.id.scoreTxt);
        table.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {

            @Override
            public void onSwipeRight() {
                swipeRight();
            }

            @Override
            public void onSwipeLeft() {
                swipeLeft();
            }

            @Override
            public void onSwipeTop() {
                swipeUp();
            }

            @Override
            public void onSwipeBottom() {
                swipeDown();
            }
        });

        restartBtn.setOnClickListener(this);
        undoBtn.setOnClickListener(this);

        boardInit();
        initFirstStart();
        score = 0;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restartBtn:
                restartGame();
                break;
            case R.id.undoBtn:
                undo();
                break;
        }
    }

    private void undo() {
        for(int i = 0; i < COLS; i++) {
            for(int j = 0; j < ROWS; j++) {
                viewArr[i][j].setText(undoMatrix[i][j]);
                setColor(i,j);
            }
        }
    }

    private void restartGame() {
        TextView best = findViewById(R.id.bestScoreTxt);
        if(Integer.parseInt(best.getText().toString()) < score) {
            best.setText(String.valueOf(score));
            SharedPreferences.Editor edit = getSharedPreferences("Score", MODE_PRIVATE).edit();
            edit.putInt("high_score", score);
            edit.apply();
        }
        score = 0;
        scoreView.setText("0");
        table.removeAllViews();
        boardInit();
        initFirstStart();
    }

    private void swipeDown() {
        String temp[][] = getNewStringArr(viewArr);
        for(int i = 0; i < ROWS; i++) {
            for(int j = ROWS - 1; j >= 0; j--) {
                if(isExists(j,i)) {
                    for(int k = j + 1; k < ROWS; k++){
                        if(!isExists(k,i)) {
                            viewArr[k][i].setText(viewArr[k-1][i].getText().toString());
                            viewArr[k-1][i].setText("");
                            setColor(k,i);
                            setColor(k-1,i);
                        } else if(viewArr[k][i].getText().toString().equals(viewArr[k-1][i].getText().toString())) {
                            int number = Integer.parseInt(viewArr[k-1][i].getText().toString()) * 2;
                            score += number;
                            scoreView.setText(String.valueOf(score));
                            viewArr[k][i].setText(String.valueOf(number));
                            viewArr[k-1][i].setText("");
                            setColor(k,i);
                            setColor(k-1,i);
                            break;
                        }
                    }
                }
            }
        }
        if(!isEqual(temp)) {
            addRandomSquare();
            undoMatrix = temp;
        } else if(isTableFull()) {
            if(gameOver()) {
                Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void swipeUp() {
        String temp[][] = getNewStringArr(viewArr);
        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < ROWS; j++) {
                if(isExists(j,i)) {
                    for(int k = j - 1; k >= 0; k--){
                        if(!isExists(k,i)) {
                            viewArr[k][i].setText(viewArr[k + 1][i].getText().toString());
                            viewArr[k + 1][i].setText("");
                            setColor(k, i);
                            setColor(k + 1, i);
                        } else if(viewArr[k][i].getText().toString().equals(viewArr[k+1][i].getText().toString())) {
                            int number = Integer.parseInt(viewArr[k+1][i].getText().toString()) * 2;
                            score += number;
                            scoreView.setText(String.valueOf(score));
                            viewArr[k][i].setText(String.valueOf(number));
                            viewArr[k+1][i].setText("");
                            setColor(k,i);
                            setColor(k+1,i);
                            break;
                        }
                    }
                }
            }
        }
        if(!isEqual(temp)) {
            addRandomSquare();
            undoMatrix = temp;
        } else if(isTableFull()) {
            if(gameOver()) {
                Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void swipeLeft() {
        String temp[][] = getNewStringArr(viewArr);
        for(int i = 0; i < COLS; i++) {
            for(int j = 0; j < COLS; j++) {
                if(isExists(i,j)) {
                    for(int k = j - 1; k >= 0; k--){
                        if(!isExists(i,k)) {
                            viewArr[i][k].setText(viewArr[i][k + 1].getText().toString());
                            viewArr[i][k + 1].setText("");
                            setColor(i, k);
                            setColor(i, k + 1);
                        } else if(viewArr[i][k].getText().toString().equals(viewArr[i][k+1].getText().toString())) {
                            int number = Integer.parseInt(viewArr[i][k+1].getText().toString()) * 2;
                            score += number;
                            scoreView.setText(String.valueOf(score));
                            viewArr[i][k].setText(String.valueOf(number));
                            viewArr[i][k+1].setText("");
                            setColor(i,k);
                            setColor(i,k+1);
                            break;
                        }
                    }
                }
            }
        }
        if(!isEqual(temp)) {
            addRandomSquare();
            undoMatrix = temp;
        } else if(isTableFull()) {
            if(gameOver()) {
                Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void swipeRight() {
        String temp[][] = getNewStringArr(viewArr);
        for(int i = 0; i < COLS; i++) {
            for(int j = COLS - 1; j >= 0; j--) {
                if(isExists(i,j)) {
                    for(int k = j + 1; k < COLS; k++){
                        if(!isExists(i,k)) {
                            viewArr[i][k].setText(viewArr[i][k - 1].getText().toString());
                            viewArr[i][k - 1].setText("");
                            setColor(i, k);
                            setColor(i, k - 1);
                        } else if(viewArr[i][k].getText().toString().equals(viewArr[i][k-1].getText().toString())) {
                            int number = Integer.parseInt(viewArr[i][k-1].getText().toString()) * 2;
                            score += number;
                            scoreView.setText(String.valueOf(score));
                            viewArr[i][k].setText(String.valueOf(number));
                            viewArr[i][k-1].setText("");
                            setColor(i,k);
                            setColor(i,k-1);
                            break;
                        }
                    }
                }
            }
        }
        if(!isEqual(temp)) {
            addRandomSquare();
            undoMatrix = temp;
        } else if(isTableFull()) {
            if(gameOver()) {
                Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String[][] getNewStringArr(TextView[][] viewArray) {
        String [][]temp = new String[COLS][ROWS];
        for(int i = 0; i < COLS; i++) {
            for(int j = 0; j < ROWS; j++) {
                temp[i][j] = viewArray[i][j].getText().toString();
            }
        }
        return temp;
    }

    private void boardInit() {
        LinearLayout v = findViewById(R.id.gameBoardLayout);
        LinearLayout layout;
        TextView text;
        LinearLayout.LayoutParams params;
        for(int i = 0; i < ROWS; i++) {
            layout = new LinearLayout(this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            layout.setOrientation(LinearLayout.HORIZONTAL);
            for(int j = 0; j < COLS; j++) {
                text = new TextView(this);
                text.setText("");
                text.setTextSize(35);
                text.setTypeface(Typeface.DEFAULT_BOLD);
                text.setGravity(Gravity.CENTER);
                params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1f);
                params.setMargins(7,7,7,7);
                text.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.squareColor, null));
                text.setLayoutParams(params);
                layout.addView(text);
                viewArr[i][j] = text;
            }
            v.addView(layout);
        }
    }

    private void initFirstStart() {
        for(int i = 0; i < 2; i++) {
            addRandomSquare();
        }
    }

    private void addRandomSquare() {
        Random r = new Random();
        int row, col, num;
        do {
            row = r.nextInt(ROWS);
            col = r.nextInt(COLS);
        } while(isExists(row, col));
        if(r.nextInt(ROWS*COLS) % 2 == 0) {
            num = 2;
            viewArr[row][col].setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.square2, null));
        } else {
            num = 4;
            viewArr[row][col].setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.square4, null));
        }
        viewArr[row][col].setText(String.valueOf(num));
    }

    private boolean isExists(int row, int col) {
        return !(viewArr[row][col].getText().toString().equals(""));
    }

    private void setColor(int row, int col) {
        int color;
        if(viewArr[row][col].getText().toString().equals("2")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square2, null);
        } else if(viewArr[row][col].getText().toString().equals("4")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square4, null);
        } else if(viewArr[row][col].getText().toString().equals("8")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square8, null);
        } else if(viewArr[row][col].getText().toString().equals("16")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square16, null);
        } else if(viewArr[row][col].getText().toString().equals("32")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square32, null);
        } else if(viewArr[row][col].getText().toString().equals("64")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square64, null);
        } else if(viewArr[row][col].getText().toString().equals("128")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square128, null);
        } else if(viewArr[row][col].getText().toString().equals("256")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square256, null);
        } else if(viewArr[row][col].getText().toString().equals("512")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square512, null);
        } else if(viewArr[row][col].getText().toString().equals("1024")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square128, null);
        } else if(viewArr[row][col].getText().toString().equals("2048")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square2048, null);
        } else {
            color = ResourcesCompat.getColor(getResources(), R.color.squareColor, null);
        }
        viewArr[row][col].setBackgroundColor(color);
    }

    private boolean isTableFull() {
        for(int i = 0; i < COLS; i++) {
            for(int j = 0; j < ROWS; j++) {
                if(viewArr[i][j].getText().toString().equals("")) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean gameOver() {
        String origin, right, down;
        for(int i = 0; i < COLS; i++) {
            for(int j = 0; j < ROWS - 1; j++) {
                origin = viewArr[i][j].getText().toString();
                right = viewArr[i][j+1].getText().toString();
                if(i < COLS- 1) {
                    down = viewArr[i+1][j].getText().toString();
                } else {
                    down = "";
                }
                if(origin.equals(down) || origin.equals(right)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isEqual(String[][] newArr) {
        for(int i = 0; i < COLS; i++) {
            for(int j = 0; j < ROWS; j++) {
                if(!viewArr[i][j].getText().toString().equals(newArr[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }
}
