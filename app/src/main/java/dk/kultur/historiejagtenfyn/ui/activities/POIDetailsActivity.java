package dk.kultur.historiejagtenfyn.ui.activities;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.entities.POIEntity;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.data.sql.ProviderMethods;
import dk.kultur.historiejagtenfyn.ui.Views.SideTabView;
import dk.kultur.historiejagtenfyn.ui.fragments.map.POIContentFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.map.POIPracticalFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.map.POIQuizFragment;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;

public class POIDetailsActivity extends AbsActivityFullScreen implements SideTabView.OnTabClickedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    public static final String EXTRA_POI_ID = "extra_poi_id";

    private List<Fragment> fragmentsList = new ArrayList<Fragment>();
    private int mActiveTab = 0;
    private String mPoiId;
    private boolean checkRoutePoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPoiId = getIntent().getStringExtra(EXTRA_POI_ID);
        if (mPoiId == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_poi_details);
        fragmentsList.add(POIContentFragment.newInstance(mPoiId, 1, false, 1));
        openFragment(fragmentsList.get(0), false);


    }

    public void addPracticalFragment(int tabCount, boolean isQuiz, String content) {
        fragmentsList.add(POIPracticalFragment.newInstance(content, tabCount, isQuiz, 2, mPoiId));
    }

    public void addQuizFragment(int tabCount) {
        fragmentsList.add(POIQuizFragment.newInstance(mPoiId, tabCount, tabCount));
    }
    @Override
    public void onTabClicked(int id, boolean animated) {
        Log.d(TAG, "onTabClicked " + id);
        Log.d(TAG, "onTabClicked active tab" + mActiveTab);
        if (mActiveTab == id) {
            return;
        }
        mActiveTab = id;
        if (id < 1) {
            return;
        }
        Fragment fragment = fragmentsList.get(id - 1);
        openFragment(fragment, animated);
    }

    private void openFragment(Fragment fragment, boolean animated) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(0, R.anim.anim_shrink_to_left);

            if (animated) {
                SoundManager.getInstance(this).playSound(SoundManager.SOUND_BOOK_PAGE);
            }
            transaction.replace(R.id.content, fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.anim_shrink_to_left);
        SoundManager.getInstance(this).playSound(SoundManager.SOUND_BOOK_PAGE);

    }

}
