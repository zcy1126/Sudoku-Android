package android.hfad.com.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MainMenuActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        /* hide the status bar */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Typeface defaultFont = Typeface.createFromAsset(getAssets(), AppConstant.defaultFontName);

        Button btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setTypeface(defaultFont);

        Button btnLadderboard = findViewById(R.id.btnLadderBoard);
        btnLadderboard.setTypeface(defaultFont);

    }

    public void onClickLadderboard(View view) {
//        Intent intent = new Intent(this, GameActivity.class);
//        startActivity(intent);
    }

    public void onClickPlay(View view) {
        Intent intent = new Intent(this, SelectDifficultyActivity.class);
        startActivity(intent);
    }
}