package dk.kultur.historiejagtenfyn.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beyondar.android.opengl.util.LowPassFilter;
import com.beyondar.android.sensor.BeyondarSensorManager;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.ui.Views.beyondar.ArCameraView;
import dk.kultur.historiejagtenfyn.ui.Views.beyondar.BeyondarGLSurfaceView;
import dk.kultur.historiejagtenfyn.ui.activities.HomeActivity;
import dk.kultur.historiejagtenfyn.ui.dialogs.CustomPopupDialogFragment;
import dk.kultur.historiejagtenfyn.ui.dialogs.LocationDisabledDialogFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.map.MapPreferences;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;


public class ARViewFragment extends AbsFragmentWithTabbar implements OnClickListener, OnTouchListener {

    public static final int FRAGMENT_ID = 3;

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 1;
    private static final long KEEP_ALIVE_TIME = 1000; // 1000 ms

    private ArCameraView mBeyondarCameraView;
    private BeyondarGLSurfaceView mBeyondarGLSurface;
    private RelativeLayout mMainLayout;

    private World mWorld;


    private float mLastScreenTouchX, mLastScreenTouchY;

    private ThreadPoolExecutor mThreadPool;
    private BlockingQueue<Runnable> mBlockingQueue;

    private SensorManager mSensorManager;

    private List<String> mGeoObjectsIds;

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    private static Handler mHandler = new Handler();
    private boolean worldInitialized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBlockingQueue = new LinkedBlockingQueue<Runnable>();
        mThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS, mBlockingQueue);
    }

    @Override
    protected int getFragmentId() {
        return FRAGMENT_ID;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ar_view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        FlurryAgent.logEvent(getString(R.string.flurry_ar_view_fragment), true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //init fonts
        TextView headerText = (TextView) view.findViewById(R.id.headerText);
        final Typeface typeFaceMarkerFelt = UIUtils.getTypeFaceMarkerFelt(getActivity());
        headerText.setTypeface(typeFaceMarkerFelt);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBeyondarCameraView = null;
        mBeyondarGLSurface = null;
        worldInitialized = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRemoving() && !isDetached() && getActivity() != null) {
                    connectToGoogleServices();
                    init(view);
                    startRenderingAR();

                }
            }
        }, 500);

        return view;
    }


    private void connectToGoogleServices() {

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(mGoogleConnectionCallbacks)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void initItems() {
        getLoaderManager().initLoader(1, null, mLoaderCallbacks);
    }

    private void initWorld(List<GeoObject> objects) {
        World world = new World(getActivity());

        //55.394944, 10.389435
        world.setGeoPosition(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        world.setDefaultImage("assets://pics/default_pin.png");
        //world.setGeoPosition(55.394944, 10.389435);
        for (GeoObject object : objects) {
            world.addBeyondarObject(object);
        }

        setWorld(world);
        worldInitialized = true;
    }

    private void init(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        params.topMargin = getStatusBarHeight();
        params.bottomMargin = getNavigationBarHeight();


        view.findViewById(R.id.loading_background).setVisibility(View.GONE);

        mMainLayout = (RelativeLayout) view.findViewById(R.id.ar_container);
        LowPassFilter.ALPHA = 0.05f;
        mBeyondarGLSurface = createBeyondarGLSurfaceView();
        mBeyondarGLSurface.setSensorDelay(SensorManager.SENSOR_DELAY_FASTEST);

        mBeyondarGLSurface.setOnTouchListener(this);

        mBeyondarCameraView = createCameraView();


        mMainLayout.addView(mBeyondarCameraView, params);
        mMainLayout.addView(mBeyondarGLSurface, params);

        mMainLayout.setClickable(true);
        mMainLayout.setOnClickListener(this);

        mBeyondarCameraView.startPreviewCamera();

        mBeyondarGLSurface.onResume();


    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected BeyondarGLSurfaceView createBeyondarGLSurfaceView() {
        return new BeyondarGLSurfaceView(getActivity());
    }

    protected ArCameraView createCameraView() {
        return new ArCameraView(getActivity());
    }

    private void checkIfSensorsAvailable() {
        PackageManager pm = getActivity().getPackageManager();
        boolean compass = pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
        boolean accelerometer = pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        if (!compass && !accelerometer) {
            throw new IllegalStateException(getClass().getName()
                    + " can not run without the compass and the acelerometer sensors.");
        } else if (!compass) {
            throw new IllegalStateException(getClass().getName() + " can not run without the compass sensor.");
        } else if (!accelerometer) {
            throw new IllegalStateException(getClass().getName()
                    + " can not run without the acelerometer sensor.");
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (mBeyondarCameraView != null) {
            mBeyondarCameraView.initCameraOrientation();
            mBeyondarCameraView.startPreviewCamera();
        }
        if (mBeyondarGLSurface != null) {
            mBeyondarGLSurface.onResume();
        }
        BeyondarSensorManager.resume(mSensorManager);
        if (mWorld != null) {
            mWorld.onResume();
        }

        if (((HomeActivity) getActivity()).mUserOpenedSettings && mLastLocation == null) {
            ((HomeActivity) getActivity()).mUserOpenedSettings = false;
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                getLocationFromGoogleServices();
            } else {
                connectToGoogleServices();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBeyondarCameraView != null) {
            mBeyondarCameraView.releaseCamera();
        }
        if (mBeyondarGLSurface != null) {
            mBeyondarGLSurface.onPause();
        }
        BeyondarSensorManager.pause(mSensorManager);
        if (mWorld != null) {
            mWorld.onPause();
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }
    }


    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        mLastScreenTouchX = event.getX();
        mLastScreenTouchY = event.getY();

        return false;
    }

    @Override
    public void onClick(View v) {
        if (worldInitialized && v == mMainLayout) {
            final float lastX = mLastScreenTouchX;
            final float lastY = mLastScreenTouchY;

            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<BeyondarObject> beyondarObjects = new ArrayList<BeyondarObject>();

                    mBeyondarGLSurface.getBeyondarObjectsOnScreenCoordinates(lastX, lastY, beyondarObjects);

                    if (beyondarObjects.size() == 0)
                        return;
                    mBeyondarGLSurface.post(new Runnable() {
                        @Override
                        public void run() {
                            onObjectClicked(beyondarObjects.get(0));

                        }
                    });
                }
            });
        }
    }


    /**
     * Set the {@link com.beyondar.android.world.World World} that contains all
     * the {@link com.beyondar.android.world.BeyondarObject BeyondarObject} that
     * will be displayed.
     *
     * @param world The {@link com.beyondar.android.world.World World} that holds
     *              the information of all the elements.
     * @throws IllegalStateException If the device do not have the required sensors available.
     */
    public void setWorld(World world) {
        try {
            checkIfSensorsAvailable();
        } catch (IllegalStateException e) {
            throw e;
        }
        mWorld = world;
        mBeyondarGLSurface.setDistanceFactor(15);
        mBeyondarGLSurface.setPushAwayDistance(10);
        mBeyondarGLSurface.setPullCloserDistance(1000);
        mBeyondarGLSurface.setMaxDistanceToRender(1000);
        mBeyondarGLSurface.setWorld(world);
    }


    /**
     * Enable the GLSurface to start rendering the AR world.
     */
    public void startRenderingAR() {
        mBeyondarGLSurface.setVisibility(View.VISIBLE);
    }


    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            //55.394944, 10.389435
            Uri uri = HisContract.POIJoinedRoute.buildByPositionAndRange((float) mLastLocation.getLatitude(), (float) mLastLocation.getLongitude(), 0.01f);
            //for testing
            //Uri uri = HisContract.POIJoinedRoute.buildByPositionAndRange((float)55.394944, (float) 10.389435, 0.01f);
            String[] keys = new String[]{
                    HisContract.POIEntry.COLUMN_COORDINATES_LAT,
                    HisContract.POIEntry.COLUMN_COORDINATES_LON,
                    HisContract.POIEntry.COLUMN_AR_RANGE,
                    HisContract.POIEntry.TABLE_NAME + ".objectId",
                    HisContract.RouteEntry.TABLE_NAME + ".objectId",
                    "icon_objectId"};

            return new CursorLoader(getActivity(), uri, keys, null, null, null);

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (mGeoObjectsIds != null) {
                mGeoObjectsIds.clear();
                mGeoObjectsIds = null;
            }
            mGeoObjectsIds = new ArrayList<String>();
            ArrayList geoObjects = new ArrayList<GeoObject>();
            int iterator = 0;
            while (data.moveToNext()) {
                GeoObject go = new GeoObject(iterator);
                go.setGeoPosition(data.getDouble(0), data.getDouble(1));
                go.setName("name");
                String routeId = data.getString(4);
                String currentRouteId = MapPreferences.getCurrentRoute(getActivity());

                String imageUri;
                if (currentRouteId == null || currentRouteId.equals(routeId)) {
                    imageUri = FileUtils.getIconPathForAR(data.getString(5), FileUtils.ICON_AR_ACTIVE_RETINA);
                } else {
                    imageUri = FileUtils.getIconPathForAR(data.getString(5), FileUtils.ICON_AR_INACTIVE_RETINA);
                }
                Log.d(getTag(), "image uri " + imageUri);
                go.setImageUri(imageUri);
                geoObjects.add(go);
                mGeoObjectsIds.add(data.getString(3));
                iterator++;
            }
           // generateTest(geoObjects);
            initWorld(geoObjects);


        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private void generateTest(ArrayList geoObjects) {

        int index = 1;
        for (double lat = 54.90; lat < 54.92; lat += 0.001) {
            for (double lon = 23.92; lon < 23.95; lon += 0.001) {
                GeoObject go = new GeoObject(index);
                go.setGeoPosition(lat, lon);
                go.setName("index" + index);
                go.setImageResource(R.drawable.cluster_marker_pin1);
                //go.setImageUri(FileUtils.getFileUrlForAR(getActivity(), "fXEzjUHldc", FileUtils.ICON_AR_ACTIVE_RETINA));
                geoObjects.add(go);
                mGeoObjectsIds.add(String.valueOf(index));
                index++;
            }
        }
    }


    private void onObjectClicked(BeyondarObject object) {
        CustomPopupDialogFragment newFragment = CustomPopupDialogFragment.newInstance(mGeoObjectsIds.get((int) object.getId()), true);
        newFragment.setCancelable(false);
        newFragment.show(getFragmentManager(), "dialog");

    }

    private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            getLocationFromGoogleServices();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
            initItems();
        }
    };


    private void getLocationFromGoogleServices() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            initItems();
        } else {
            if (isLocationEnabled()) {
                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, mLocationListener);
            } else {
                LocationDisabledDialogFragment newFragment = LocationDisabledDialogFragment.newInstance();
                newFragment.setCancelable(false);
                newFragment.show(getFragmentManager(), "dialog");
            }
        }
    }

    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_ar_view_fragment));
    }
}
