package dk.kultur.historiejagtenfyn.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.geofence.GeofenceLocationService;
import dk.kultur.historiejagtenfyn.geofence.GeofenceTransitionsIntentService;
import dk.kultur.historiejagtenfyn.ui.Views.TabbView;
import dk.kultur.historiejagtenfyn.ui.fragments.ARViewFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.RouteDetailsFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.RouteDetailsFragment.GoBackFromDetailsListener;
import dk.kultur.historiejagtenfyn.ui.fragments.RouteDetailsFragment.MapRouteSelectedListener;
import dk.kultur.historiejagtenfyn.ui.fragments.RouteListFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.ScanFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.map.MapPreferences;
import dk.kultur.historiejagtenfyn.ui.fragments.map.osm.MapFragment;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;

public class HomeActivity extends AbsActivityFullScreen implements TabbView.OnTabClickedListener,
        RouteListFragment.RouteSelectedListener,
        MapRouteSelectedListener,
        GoBackFromDetailsListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    public static final String ARG_POI_ID = "dk.kultur.historiejagtenfyn.ui.activities.ARG_POI_ID";

    private int mActiveTab = 0;

    public boolean mUserOpenedSettings = false;

    private BroadcastReceiver broadcastGeofenceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "#onReceive");

            String poiId = intent.getStringExtra(ARG_POI_ID);
            Log.d(TAG, "entered poi id " + poiId);
            startPoiContentActivity(poiId);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "on create");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        onTabClicked(1, false);

        Intent service = new Intent(this, GeofenceLocationService.class);
        startService(service);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastGeofenceReceiver,
                new IntentFilter(GeofenceTransitionsIntentService.POI_GEOFENCE_ENTERED));

        if (getIntent() != null) {
            String poiId = getIntent().getStringExtra(ARG_POI_ID);
            //if poiId is not null that means we got here form geofence
            if (poiId != null) {
                onTabClicked(MapFragment.FRAGMENT_ID, false);
                startPoiContentActivity(poiId);

            }
        }

    }

    /**
     * we got here from geofence
     * @param poiId
     */
    private void startPoiContentActivity(String poiId) {

        Intent intent = new Intent(this, POIDetailsActivity.class);
        intent.putExtra(POIDetailsActivity.EXTRA_POI_ID, poiId);
        startActivityForResult(intent, MapFragment.REQUEST_POI_CONTENT);

    }

    @Override
    public void onTabClicked(int id, boolean animate) {


        if (mActiveTab == id) {
            return;
        }
        mActiveTab = id;
        if (id < 1) {
            return;
        }
        Fragment fragment = null;
        switch (id) {
            case RouteListFragment.FRAGMENT_ID:
                fragment = new RouteListFragment();
                MapPreferences.saveCurrentRoute(this, null);
                break;
            case MapFragment.FRAGMENT_ID:
                fragment = MapFragment.newInstance();
                break;
            case ARViewFragment.FRAGMENT_ID:
                fragment = new ARViewFragment();
                break;
            case ScanFragment.FRAGMENT_ID:
                fragment = new ScanFragment();
                break;
        }
        if (fragment != null) {
            openFragment(fragment, true);
        }
    }

    private void openFragment(Fragment fragment, boolean animate) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


            if (animate) {
                transaction.setCustomAnimations(0, R.anim.anim_shrink_to_left);
            }
            transaction.replace(R.id.content, fragment, fragment.getClass().getSimpleName());
            transaction.commitAllowingStateLoss();

            SoundManager.getInstance(this).playSound(SoundManager.SOUND_BOOK_PAGE);
        }
    }


    @Override
    public void onBackPressed() {

        Fragment f = getSupportFragmentManager().findFragmentByTag(MapFragment.class.getSimpleName());
        if (f != null && f instanceof MapFragment) {
            MapFragment fragment = (MapFragment) f;
            fragment.reloadData();
        }

        super.onBackPressed();

        SoundManager.getInstance(this).playSound(SoundManager.SOUND_BOOK_PAGE);
    }

    @Override
    public void onRouteSelected(String routeId, String routeName) {
        MapPreferences.saveCurrentRoute(this, routeId);
        RouteDetailsFragment fragment = RouteDetailsFragment.getInstance(routeId, routeName);
        openFragment(fragment, true);
    }

    @Override
    public void onGoBackFromDetails() {
        RouteListFragment fragment = new RouteListFragment();
        openFragment(fragment, true);
    }

    @Override
    public void onMapRouteSelected(String routeId) {
        MapPreferences.saveCurrentRoute(this, routeId);
        MapFragment fragment = MapFragment.newInstance();
        mActiveTab = MapFragment.FRAGMENT_ID;
        openFragment(fragment, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Home activity stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastGeofenceReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "on activity result " + requestCode);

        if (requestCode == MapFragment.REQUEST_POI_CONTENT) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(MapFragment.class.getSimpleName());
            if (f != null && f instanceof MapFragment) {
                MapFragment fragment = (MapFragment) f;
                fragment.reloadData();
            }
        }
    }
}