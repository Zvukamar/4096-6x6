package a4096xedition.corp.axeleration.com.a4096_6x6_christmasxedition;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener { // TODO: text size after 4 digit number, undo score fix, code optimization, fix colors

    public static final int ROWS = 5;
    public static final int COLS = 5;
    private TextView[][] viewArr;
    private ViewGroup table;
    private TextView scoreView;
    private int score, lastUpdatedScore;
    private String[][] undoMatrix;
    private MediaPlayer moveSound, matchSound;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load an ad into the AdMob banner view.
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        matchSound = MediaPlayer.create(this, R.raw.match);
        moveSound = MediaPlayer.create(this, R.raw.move);
//        moveSound.start();
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();


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
        lastUpdatedScore = 0;

    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

            }

            @Override
            public void onAdClosed() {

            }
        });
        return interstitialAd;
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
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                viewArr[i][j].setText(undoMatrix[i][j]);
                setColor(i, j);
            }
        }
        score = lastUpdatedScore;
        scoreView.setText(String.valueOf(score));
    }

    private void restartGame() {
        loadInterstitial();
        TextView best = findViewById(R.id.bestScoreTxt);
        if (Integer.parseInt(best.getText().toString()) < score) {
            best.setText(String.valueOf(score));
            SharedPreferences.Editor edit = getSharedPreferences("Score", MODE_PRIVATE).edit();
            edit.putInt("high_score", score);
            edit.apply();
        }
        score = 0;
        lastUpdatedScore = 0;
        scoreView.setText("0");
        table.removeAllViews();
        boardInit();
        initFirstStart();
        undoMatrix = getNewStringArr(viewArr);
    }

    private void swipeDown() {
        new TaskDown().execute();
    }

    private void swipeUp() {
        new TaskUp().execute();
    }

    private void swipeLeft() {
        new TaskLeft().execute();
    }

    private void swipeRight() {
        new TaskRight().execute();
    }


    private String[][] getNewStringArr(TextView[][] viewArray) {
        String[][] temp = new String[COLS][ROWS];
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
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
        for (int i = 0; i < ROWS; i++) {
            layout = new LinearLayout(this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            layout.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < COLS; j++) {
                text = new TextView(this);
                text.setText("");
                text.setTextSize(30);
                text.setTypeface(Typeface.DEFAULT_BOLD);
                text.setGravity(Gravity.CENTER);
                params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                params.setMargins(15, 15, 15, 15);
                text.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.squareColor, null));
                text.setLayoutParams(params);
                layout.addView(text);
                viewArr[i][j] = text;
            }
            v.addView(layout);
        }
    }

    private void initFirstStart() {
        for (int i = 0; i < 2; i++) {
            addRandomSquare();
        }
    }

    private void addRandomSquare() {
        Random r = new Random();
        int row, col, num;
        do {
            row = r.nextInt(ROWS);
            col = r.nextInt(COLS);
        } while (isExists(row, col));
        if (r.nextInt(ROWS * COLS) % 2 == 0) {
            num = 2;
            viewArr[row][col].setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.square2, null));
        } else {
            num = 4;
            viewArr[row][col].setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.square4, null));
        }
        viewArr[row][col].setText(String.valueOf(num));
        viewArr[row][col].startAnimation(AnimationUtils.loadAnimation(this, R.anim.show_square));
    }

    private boolean isExists(int row, int col) {
        return !(viewArr[row][col].getText().toString().equals(""));
    }

    private void setColor(int row, int col) {
        int color;
        if (viewArr[row][col].getText().toString().equals("2")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square2, null);
        } else if (viewArr[row][col].getText().toString().equals("4")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square4, null);
        } else if (viewArr[row][col].getText().toString().equals("8")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square8, null);
        } else if (viewArr[row][col].getText().toString().equals("16")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square16, null);
        } else if (viewArr[row][col].getText().toString().equals("32")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square32, null);
        } else if (viewArr[row][col].getText().toString().equals("64")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square64, null);
        } else if (viewArr[row][col].getText().toString().equals("128")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square128, null);
        } else if (viewArr[row][col].getText().toString().equals("256")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square256, null);
        } else if (viewArr[row][col].getText().toString().equals("512")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square512, null);
        } else if (viewArr[row][col].getText().toString().equals("1024")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square128, null);
        } else if (viewArr[row][col].getText().toString().equals("2048")) {
            color = ResourcesCompat.getColor(getResources(), R.color.square2048, null);
        } else {
            color = ResourcesCompat.getColor(getResources(), R.color.squareColor, null);
        }
        viewArr[row][col].setBackgroundColor(color);
    }

    private boolean isTableFull() {
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (viewArr[i][j].getText().toString().equals("")) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean gameOver() {
        String origin, right, down;
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS - 1; j++) {
                origin = viewArr[i][j].getText().toString();
                right = viewArr[i][j + 1].getText().toString();
                if (i < COLS - 1) {
                    down = viewArr[i + 1][j].getText().toString();
                } else {
                    down = "";
                }
                if (origin.equals(down) || origin.equals(right)) {
                    return false;
                }
            }
        }
        showInterstitial();
        return true;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private boolean isEqual(String[][] newArr) {
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (!viewArr[i][j].getText().toString().equals(newArr[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    /* RIGHT SLIDE ASYNC TASK */
    private class TaskRight extends AsyncTask<Void, Integer, Void> {

        String[][] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            temp = getNewStringArr(viewArr);
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (int i = 0; i < COLS; i++) {
                for (int j = COLS - 1; j > 0; j--) {
                    for (int k = 0; k < COLS; k++) {
                        publishProgress(k, j);

                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (!isExists(values[0], values[1])) {
                viewArr[values[0]][values[1]].setText(viewArr[values[0]][values[1] - 1].getText().toString());
                viewArr[values[0]][values[1] - 1].setText("");
                setColor(values[0], values[1]);
                setColor(values[0], values[1] - 1);
            } else if (viewArr[values[0]][values[1]].getText().toString().equals(viewArr[values[0]][values[1] - 1].getText().toString())) {
                lastUpdatedScore = score;
                int number = Integer.parseInt(viewArr[values[0]][values[1] - 1].getText().toString()) * 2;
                score += number;
                scoreView.setText(String.valueOf(score));
                viewArr[values[0]][values[1]].setText(String.valueOf(number));
                viewArr[values[0]][values[1]].startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.colition));
                viewArr[values[0]][values[1] - 1].setText("");
                setColor(values[0], values[1]);
                setColor(values[0], values[1] - 1);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isEqual(temp)) {
                addRandomSquare();
                undoMatrix = temp;
            } else if (isTableFull()) {
                if (gameOver()) {
                    Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /* LEFT SLIDE ASYNC TASK */
    private class TaskLeft extends AsyncTask<Void, Integer, Void> {

        String[][] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            temp = getNewStringArr(viewArr);
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (int i = 0; i < COLS; i++) {
                for (int j = 0; j < COLS - 1; j++) {
                    for (int k = 0; k < COLS; k++) {
                        publishProgress(k, j);
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (!isExists(values[0], values[1])) {
                viewArr[values[0]][values[1]].setText(viewArr[values[0]][values[1] + 1].getText().toString());
                viewArr[values[0]][values[1] + 1].setText("");
                setColor(values[0], values[1]);
                setColor(values[0], values[1] + 1);
            } else if (viewArr[values[0]][values[1]].getText().toString().equals(viewArr[values[0]][values[1] + 1].getText().toString())) {
                lastUpdatedScore = score;
                int number = Integer.parseInt(viewArr[values[0]][values[1] + 1].getText().toString()) * 2;
                score += number;
                scoreView.setText(String.valueOf(score));
                viewArr[values[0]][values[1]].setText(String.valueOf(number));
                viewArr[values[0]][values[1]].startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.colition));
                viewArr[values[0]][values[1] + 1].setText("");
                setColor(values[0], values[1]);
                setColor(values[0], values[1] + 1);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isEqual(temp)) {
                addRandomSquare();
                undoMatrix = temp;
            } else if (isTableFull()) {
                if (gameOver()) {
                    Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /* UP SLIDE ASYNC TASK */
    private class TaskUp extends AsyncTask<Void, Integer, Void> {

        String[][] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            temp = getNewStringArr(viewArr);
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (int i = 0; i < COLS; i++) {
                for (int j = 0; j < ROWS - 1; j++) {
                    for (int k = 0; k < COLS; k++) {
                        publishProgress(j, k);
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (!isExists(values[0], values[1])) {
                viewArr[values[0]][values[1]].setText(viewArr[values[0] + 1][values[1]].getText().toString());
                viewArr[values[0] + 1][values[1]].setText("");
                setColor(values[0], values[1]);
                setColor(values[0] + 1, values[1]);
            } else if (viewArr[values[0]][values[1]].getText().toString().equals(viewArr[values[0] + 1][values[1]].getText().toString())) {
                lastUpdatedScore = score;
                int number = Integer.parseInt(viewArr[values[0] + 1][values[1]].getText().toString()) * 2;
                score += number;
                scoreView.setText(String.valueOf(score));
                viewArr[values[0]][values[1]].setText(String.valueOf(number));
                viewArr[values[0]][values[1]].startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.colition));
                viewArr[values[0] + 1][values[1]].setText("");
                setColor(values[0], values[1]);
                setColor(values[0] + 1, values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isEqual(temp)) {
                addRandomSquare();
                undoMatrix = temp;
            } else if (isTableFull()) {
                if (gameOver()) {
                    Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /* DOWN SLIDE ASYNC TASK */
    private class TaskDown extends AsyncTask<Void, Integer, Void> {

        String[][] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            temp = getNewStringArr(viewArr);
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (int i = 0; i < COLS; i++) {
                for (int j = ROWS - 1; j > 0; j--) {
                    for (int k = 0; k < COLS; k++) {
                        publishProgress(j, k);
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (!isExists(values[0], values[1])) {
                viewArr[values[0]][values[1]].setText(viewArr[values[0] - 1][values[1]].getText().toString());
                viewArr[values[0] - 1][values[1]].setText("");
                setColor(values[0], values[1]);
                setColor(values[0] - 1, values[1]);
            } else if (viewArr[values[0]][values[1]].getText().toString().equals(viewArr[values[0] - 1][values[1]].getText().toString())) {
                lastUpdatedScore = score;
                int number = Integer.parseInt(viewArr[values[0] - 1][values[1]].getText().toString()) * 2;
                score += number;
                scoreView.setText(String.valueOf(score));
                viewArr[values[0]][values[1]].setText(String.valueOf(number));
                viewArr[values[0]][values[1]].startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.colition));
                viewArr[values[0] - 1][values[1]].setText("");
                setColor(values[0], values[1]);
                setColor(values[0] - 1, values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isEqual(temp)) {
                addRandomSquare();
                undoMatrix = temp;
            } else if (isTableFull()) {
                if (gameOver()) {
                    Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
