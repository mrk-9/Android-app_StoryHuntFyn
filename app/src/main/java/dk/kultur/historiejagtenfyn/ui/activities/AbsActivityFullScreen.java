package dk.kultur.historiejagtenfyn.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.support.v4.app.FragmentActivity;
import com.flurry.android.FlurryAgent;

import dk.kultur.historiejagtenfyn.HistoriejagtenfynApplication;
import dk.kultur.historiejagtenfyn.R;

/**
 * Main fragment
 * Created by JustinasK on 12/11/2014.
 */
public abstract class AbsActivityFullScreen extends FragmentActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("Activity", "onWindowFocusChanged " + hasFocus);
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
    protected void onResume() {
        super.onResume();
        ((HistoriejagtenfynApplication) getApplication()).onActivityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((HistoriejagtenfynApplication) getApplication()).onActivityPaused();
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
