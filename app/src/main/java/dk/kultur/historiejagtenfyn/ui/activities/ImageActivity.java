package dk.kultur.historiejagtenfyn.ui.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.flurry.android.FlurryAgent;

import java.io.IOException;

import dk.kultur.historiejagtenfyn.HistoriejagtenfynApplication;
import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;

public class ImageActivity extends Activity {

    public static final String EXTRA_IMAGE_PATH = "EXTRA_IMAGE_PATH";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        ImageView imgView = (ImageView) findViewById(R.id.image);
        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);

        try {
            Bitmap b1 = BitmapFactory.decodeStream(FileUtils.getFileStream(this, imagePath));
            BitmapDrawable bd = new BitmapDrawable(getResources(), b1);
            imgView.setImageDrawable(bd);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        ((HistoriejagtenfynApplication)getApplication()).onActivityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((HistoriejagtenfynApplication)getApplication()).onActivityPaused();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("ImageActivity", "onWindowFocusChanged " + hasFocus);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        } else if (Build.VERSION.SDK_INT >= 19){

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );

        } else {

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            );

        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, getResources().getString(R.string.flurry_api_key));
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }
}
