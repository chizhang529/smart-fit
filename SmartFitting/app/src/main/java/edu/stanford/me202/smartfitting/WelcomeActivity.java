package edu.stanford.me202.smartfitting;

import android.graphics.Typeface;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.dd.CircularProgressButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends AppCompatActivity {
    private final static String TAG = WelcomeActivity.class.getSimpleName();
    private int num = 1;

    @BindView(R.id.welcometext)
    TextView welcomeText;
    @BindView(R.id.scanButton)
    CircularProgressButton scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        // display welcome text
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/gardenfont.ttf");
        welcomeText.setTypeface(font);

        scanButton.setIndeterminateProgressMode(true);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scanButton.getProgress() == 0) {
                    scanButton.setProgress(50);
                } else if (scanButton.getProgress() == 50) {
                    scanButton.setProgress(100);
                }
            }
        });
    }
}
