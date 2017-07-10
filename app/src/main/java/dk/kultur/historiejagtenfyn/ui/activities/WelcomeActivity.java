package dk.kultur.historiejagtenfyn.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.fragments.WelcomeFragment;


/**
 * Activity with introduction text
 */
public class WelcomeActivity extends AbsActivityFullScreen {

    private static final String LOG_TAG = WelcomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(WelcomeFragment.getLogTag());
        if (fragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.content, new WelcomeFragment(), WelcomeFragment.getLogTag());
            ft.commit();
        }
    }

    public String getLogTag() {
        return LOG_TAG;
    }
}
