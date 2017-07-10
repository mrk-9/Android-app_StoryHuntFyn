package dk.kultur.historiejagtenfyn.ui.fragments.map.osm;

import static dk.kultur.historiejagtenfyn.data.sql.HisContract.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flurry.android.FlurryAgent;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.entities.POIConnection;
import dk.kultur.historiejagtenfyn.data.entities.POIEntity;
import dk.kultur.historiejagtenfyn.data.operations.AbsAsyncOperation;
import dk.kultur.historiejagtenfyn.data.operations.AsyncOperationListener;
import dk.kultur.historiejagtenfyn.data.operations.GetRoadOverlaysOperation;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.data.sql.ProviderMethods;
import dk.kultur.historiejagtenfyn.ui.Views.TabbView;
import dk.kultur.historiejagtenfyn.ui.dialogs.CustomPopupDialogFragment;
import dk.kultur.historiejagtenfyn.ui.dialogs.ErrorDialogFragment;
import dk.kultur.historiejagtenfyn.ui.fragments.map.MapPreferences;
import dk.kultur.historiejagtenfyn.ui.fragments.map.MapSettingsFragment;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

public class MapFragment extends Fragment {

    public static final int FRAGMENT_ID = 2;
    private static final int ROUTE_LOADER_ID = 1000;
    private static final int POI_LOADER_ID = 1001;
    private static final int POI_CONNECTION_LOADER_ID = 1002;

    public static final String EXTRA_ACTIVE_ROUTE_ID = "dk.kultur.historiejagtenfyn.ui.fragments.map.EXTRA_ACTIVE_ROUTE_ID";
    public static final int REQUEST_POI_CONTENT = 1;

    private static final String TAG = MapFragment.class.getSimpleName();

    public static final OnlineTileSourceBase MAPNIK = new XYTileSource("Mapnik",
            ResourceProxy.string.mapnik, 0, 19, 256, ".png", new String[]{
            "http://a.tile.openstreetmap.org/",
            "http://b.tile.openstreetmap.org/",
            "http://c.tile.openstreetmap.org/"});


    // ===========================================================
    // Fields
    // ===========================================================

    private MapView mMapView;
    private IGeoPoint mapCenter;
    private MyLocationOverlay mLocationOverlay;

    //overlays for drawing roads from one poi to another
    private List<Polyline> routeDrawingOverlays = new ArrayList<>();

    //private String activeRouteId;
    //key routeId - value POI
    private Map<String, List<POIEntity>> routePoiMap = new HashMap<>();

    //key is source poi, value is destination poi
    private List<POIConnection> poiConnectionList = new ArrayList<>();

    private ProgressBarOverlay progressBarOverlay;

    //private BoundingBoxE6 activeBoundBox = null;
    private PoiRadiusMarkerCluster poiMarkers = null;

    private static Handler mHandler = new Handler();
    //key route object id, value - icon bitmap
    private Map<String, Bitmap> activeRouteIconMap = new HashMap<>();
    private Map<String, Bitmap> inactiveRouteIconMap = new HashMap<>();
    //key route object id, value - icon id
    private Map<String, String>routeIconIdMap = new HashMap<>();


    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ResourceProxy mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
        RelativeLayout layout = new RelativeLayout(inflater.getContext());

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        MapTileProviderBasic tileProvider = new MapTileProviderBasic(inflater.getContext(), MAPNIK);

        mMapView = new MapView(inflater.getContext(), 256, mResourceProxy, tileProvider);
        // Setting the parameters on the TextView
        mMapView.setLayoutParams(lp);
        View imageView = new View(inflater.getContext());
        imageView.setBackgroundResource(R.drawable.map_clear);
        imageView.setLayoutParams(lp);
        View pageTextureView = new View(inflater.getContext());
        pageTextureView.setBackgroundResource(R.drawable.page_texture);
        imageView.setLayoutParams(lp);

        ImageView settingsView = new ImageView(inflater.getContext());
        settingsView.setImageResource(R.drawable.map_settings_button);
        RelativeLayout.LayoutParams settignsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        settignsParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        settignsParams.topMargin = getResources().getDimensionPixelSize(R.dimen.map_settings_top_margin);
        settingsView.setLayoutParams(settignsParams);
        settingsView.setOnClickListener(onSettingsClickListener);
        settingsView.setSoundEffectsEnabled(false);
        settingsView.setId(R.id.btnMapSettings);

        ImageView locationView = new ImageView(inflater.getContext());
        locationView.setImageResource(R.drawable.user_location);
        RelativeLayout.LayoutParams locationParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        locationParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        locationParams.addRule(RelativeLayout.BELOW, R.id.btnMapSettings);
        locationParams.topMargin = getResources().getDimensionPixelSize(R.dimen.location_top_margin);
        locationView.setLayoutParams(locationParams);
        locationView.setOnClickListener(onLocationClickListener);
        locationView.setSoundEffectsEnabled(false);

        TabbView buttonsView = new TabbView(inflater.getContext());
        buttonsView.setActiveTabId(FRAGMENT_ID);
        buttonsView.setOnTabClickListener(onTabClickedListener);
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        buttonsView.setAdjustViewBounds(true);
        buttonsView.setLayoutParams(btnParams);

        // Adding the TextView to the RelativeLayout as a child
        layout.addView(mMapView);
        layout.addView(imageView);
        layout.addView(pageTextureView);
        layout.addView(settingsView);
        layout.addView(buttonsView);
        layout.addView(locationView);
        // layout.addView(progressBar);
        layout.setLayoutParams(lp);
        // Call this method to turn off hardware acceleration at the View level.
        // setHardwareAccelerationOff();
        return layout;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setHardwareAccelerationOff() {
        // Turn off hardware acceleration here, or in manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRemoving() && !isDetached() && getActivity() != null) {
                    final Context context = getActivity();
                    mapCenter = MapPreferences.getMapCenterOsm(context);
                    final int zoomLevel = MapPreferences.getZoomLevelInt(context);

                    mMapView.setBuiltInZoomControls(false);
                    mMapView.setMultiTouchControls(true);

                    mMapView.getController().setZoom(zoomLevel);
                    mMapView.getController().setCenter(mapCenter);

                    mMapView.setMinZoomLevel(4);


                    if (mLocationOverlay == null) {
                        mLocationOverlay = new MyLocationOverlay(context, mMapView);
                        boolean locSuccess = mLocationOverlay.enableMyLocation();
                        Log.d(TAG, "loc success " + locSuccess);
                        mMapView.getOverlays().add(mLocationOverlay);
                    }

                    getLoaderManager().initLoader(ROUTE_LOADER_ID, null, mLoaderCallback);
                }
            }
        }, 300);

    }

    @Override
    public void onPause() {
        Log.d(TAG, "on pause");
        if (mMapView != null) {

            mapCenter = mMapView.getMapCenter();
            MapPreferences.saveMapCenterOsm(getActivity(), mapCenter);
            MapPreferences.saveZoomLevelInt(getActivity(), mMapView.getZoomLevel());
        }
        if (mLocationOverlay != null) {
            mLocationOverlay.disableMyLocation();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocationOverlay != null) {
            mLocationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ondestroyt");
        super.onDestroy();
    }

    private void formPOIEntityMarkerList(List<POIEntity> poiList, RadiusMarkerClusterer markers, boolean allActive) {
        //List<Marker> markerList = new ArrayList<Marker>();
        String activeRouteId = MapPreferences.getCurrentRoute(getActivity());
        for (POIEntity poi : poiList) {

            String unlockPoiId = poi.getUnlockPoiId();
            if (unlockPoiId!= null && !MapPreferences.isPoiVisited(getActivity(), unlockPoiId)) {
                Log.d(TAG, "unlock poi is not visited yet " + unlockPoiId);
                continue;
            }

            //check if poi is already visited
            //and check if user is in map range
            if (!MapPreferences.isPoiVisited(getActivity(), poi.getObjectId())
                    && !checkMapRange(poi)) {
                Log.d(TAG, "poi is not in map range " + poi.getObjectId());
                continue;
            }
            GeoPoint point = new GeoPoint(poi.getPosition().getLatitude(), poi.getPosition().getLongitude());

            Marker poiMarker = new Marker(mMapView);
            //poiMarker.setTitle(poi.getName());
            poiMarker.setPosition(point);
            String routeObjectId = poi.getRouteId();
            Bitmap ab;
            if (allActive || (activeRouteId != null && activeRouteId.equals(poi.getRouteId()))) {

                ab = activeRouteIconMap.get(routeObjectId);
                poi.setActive(true);
                if (ab == null) {
                    try {
                        ab = BitmapFactory.decodeStream(FileUtils.getFileStream(getActivity(), routeIconIdMap.get(routeObjectId), FileUtils.ICON_PIN_ACTIVE_RETINA));
                        ab.setDensity(DisplayMetrics.DENSITY_XHIGH);
                        activeRouteIconMap.put(routeObjectId, ab);
                    } catch (IOException e){
                        Log.e(TAG, "exception " + e.getMessage(), e);
                    }

                }
            } else {
                ab = inactiveRouteIconMap.get(poi.getRouteId());
                poi.setActive(false);
                if (ab == null) {
                    try {
                        ab = BitmapFactory.decodeStream(FileUtils.getFileStream(getActivity(), routeIconIdMap.get(routeObjectId), FileUtils.ICON_PIN_INACTIVE_RETINA));
                        ab.setDensity(DisplayMetrics.DENSITY_XHIGH);
                        inactiveRouteIconMap.put(routeObjectId, ab);
                    } catch (IOException e){
                        Log.e(TAG, "exception " + e.getMessage(), e);
                    }

                }
            }

            if (ab != null) {
                if (poi.isParentPoi()) {
                    Bitmap bb = scaleUp(ab);
                    BitmapDrawable abd = new BitmapDrawable(getResources(), bb);
                    poiMarker.setIcon(abd);
                } else {
                    BitmapDrawable abd = new BitmapDrawable(getResources(), ab);

                    poiMarker.setIcon(abd);
                }
            }
            poiMarker.setRelatedObject(poi);
            // Add the init objects to the ArrayList overlayItemArray
            poiMarker.setOnMarkerClickListener(markerClickListener);
            //markerList.add(poiMarker);
            markers.add(poiMarker);
        }

    }

    public static Bitmap scaleUp(Bitmap realImage) {
        int width = Math.round((float) 1.4 * realImage.getWidth());
        int height = Math.round((float) 1.4 * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, false);
        return newBitmap;
    }


    //check if distance between user location and marker is less that poi map range
    private boolean checkMapRange(POIEntity point) {
        if (mLocationOverlay == null) {
            return true;
        }

        GeoPoint myLoc = mLocationOverlay.getMyLocation();
        //if location is not available we will draw all poi
        if (myLoc == null) {
            return true;
        }

        float[] results = new float[1];
        GeoPoint coord = point.getPosition();

        Location.distanceBetween(myLoc.getLatitude(), myLoc.getLongitude(),
                coord.getLatitude(), coord.getLongitude(), results);

        float distance = results[0];
        return (distance <= point.getMapRange());
    }

    //check if distance between user location and marker is less that poi click range
    private boolean checkClickRange(IGeoPoint markerPoint, POIEntity poi) {
        if (mLocationOverlay == null) {
            return true;
        }
        GeoPoint myLoc = mLocationOverlay.getMyLocation();
        if (myLoc == null) {
            return true;
        }
        float[] results = new float[1];

        Location.distanceBetween(myLoc.getLatitude(), myLoc.getLongitude(),
                markerPoint.getLatitude(), markerPoint.getLongitude(), results);
        float distance = results[0];
        return (distance <= poi.getClickRange());
    }

    //draw only active route and only if "draw route" is checked in map preferences
    private void loadRouteForDrawingOsm() {
        Log.d(TAG, "loadRouteForDrawingOsm ");
        mMapView.getOverlayManager().removeAll(routeDrawingOverlays);
        routeDrawingOverlays.clear();
        String activeRouteId = MapPreferences.getCurrentRoute(getActivity());
        if (activeRouteId == null || !MapPreferences.isShowRoutes(getActivity())) {
            return;
        }
        //List<String>activeRouteIdList = new ArrayList<>();
        List<POIEntity> activePOIList = routePoiMap.get(activeRouteId);//new ArrayList<>();
        List<POIConnection> connectionList = new ArrayList<>();
        for (POIConnection connection : poiConnectionList) {
            if (activePOIList.contains(connection.getSource()) &&
                    activePOIList.contains(connection.getDestination())) {
                connectionList.add(connection);
            }
        }
        Log.d(TAG, "conection list size is " + connectionList.size());
        //if we don't have roads to draw, simply return
        if (connectionList.size() == 0) {
            return;
        }
        //Log.d(TAG, "route list size " + activeRouteIdList.size());
        GetRoadOverlaysOperation op = new GetRoadOverlaysOperation(getActivity(), connectionList);
        op.addListener(new AsyncOperationListener() {
            @Override
            public void onOperationStarted(AbsAsyncOperation<?> operation) {
                UIUtils.showProgressBar(getActivity());
            }

            @Override
            public void onOperationFinished(AbsAsyncOperation<?> operation) {
                UIUtils.hideProgressBar(getActivity());
                GetRoadOverlaysOperation oper = (GetRoadOverlaysOperation) operation;
                Log.d(TAG, "oper rezult " + oper.getResult());

                if (operation.getResult() != null) {
                    routeDrawingOverlays = oper.getResult();
                    mMapView.getOverlayManager().addAll(routeDrawingOverlays);
                    //mMapView.getOverlayManager().add(allPOIOverlay);
                    Log.d(TAG, "poiMarkers " + poiMarkers);
                    if (poiMarkers != null) {
                        mMapView.getOverlayManager().remove(poiMarkers);
                        mMapView.getOverlayManager().add(poiMarkers);
                    }
                    if (progressBarOverlay != null) {
                        mMapView.getOverlayManager().remove(progressBarOverlay);
                        mMapView.getOverlayManager().add(progressBarOverlay);
                    }

                    mMapView.invalidate();
                }
                Log.d(TAG, "routeDrawing overlays " + routeDrawingOverlays);
            }
        });
        op.executeOperation();

    }

    //get bounding box for zooming to active route.
    private BoundingBoxE6 getBoundingBox(List<POIEntity> activePoiList) {
        int minLat = Integer.MAX_VALUE;
        int maxLat = Integer.MIN_VALUE;
        int minLong = Integer.MAX_VALUE;
        int maxLong = Integer.MIN_VALUE;
        for (POIEntity item : activePoiList) {
            IGeoPoint point = item.getPosition();
            if (point.getLatitudeE6() < minLat)
                minLat = point.getLatitudeE6();
            if (point.getLatitudeE6() > maxLat)
                maxLat = point.getLatitudeE6();
            if (point.getLongitudeE6() < minLong)
                minLong = point.getLongitudeE6();
            if (point.getLongitudeE6() > maxLong)
                maxLong = point.getLongitudeE6();
        }
        //add right margin (because of the book right margint)
        int rightMargin = Math.round((float)(maxLong - minLong)/6.0f);
        int leftMargin = Math.round((float)rightMargin/2);
        int topMargin = Math.round((float)(maxLat - minLat)/12.0f);
        BoundingBoxE6 boundingBox = new BoundingBoxE6(maxLat+topMargin, maxLong+rightMargin, minLat-topMargin, minLong - leftMargin);
        return boundingBox;
    }

    private Point pointFromGeoPoint(IGeoPoint gp, MapView vw) {

        Point rtnPoint = new Point();
        Projection projection = vw.getProjection();
        projection.toPixels(gp, rtnPoint);
        // Get the top left GeoPoint
        GeoPoint geoPointTopLeft = (GeoPoint) projection.fromPixels(0, 0);
        Point topLeftPoint = new Point();
        // Get the top left Point (includes osmdroid offsets)
        projection.toPixels(geoPointTopLeft, topLeftPoint);
        rtnPoint.x -= topLeftPoint.x; // remove offsets
        rtnPoint.y -= topLeftPoint.y;
        /*if (rtnPoint.x > vw.getWidth() || rtnPoint.y > vw.getHeight() ||
                rtnPoint.x < 0 || rtnPoint.y < 0) {
            return null; // gp must be off the screen
        }*/
        return rtnPoint;
    }



    /* ------ DATA LOADING METHODS ---- */

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            //values.put(AUTO_RANGE, info.getString(AUTO_RANGE));
            //values.put(NAME, info.getString(NAME));
            CursorLoader loader = null;
            switch (id) {
                case POI_LOADER_ID:
                    Log.d(TAG, "create POI_LOADER_ID");
                    loader = new CursorLoader(getActivity(), HisContract.POIFullJoinedEntry.CONTENT_URI, POIEntity.COLUMNS_FOR_MAP_MARKERS, null, null, null);
                    break;
                case ROUTE_LOADER_ID:
                    UIUtils.showProgressBar(getActivity());
                    Log.d(TAG, "create ROUTE_LOADER_ID");
                    String[] columns = new String[]{HisContract.RouteEntry.TABLE_NAME + ".objectId",
                            HisContract.IconEntry.TABLE_NAME + ".objectId"};

                    loader = new CursorLoader(getActivity(), RouteJoinedIconJoinedContentEntry.CONTENT_URI, columns, null, null, null);
                    break;
                case POI_CONNECTION_LOADER_ID:
                    Log.d(TAG, "create POI_CONNECTION_LOADER_ID");
                    String[] projection = {POIEntry.TABLE_NAME + ".objectId",
                            POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_COORDINATES_LAT,
                            POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_COORDINATES_LON,
                            POIDestinationEntry.TABLE_NAME + ".objectId",
                            POIDestinationEntry.TABLE_NAME + "." + POIDestinationEntry.COLUMN_COORDINATES_LAT,
                            POIDestinationEntry.TABLE_NAME + "." + POIDestinationEntry.COLUMN_COORDINATES_LON};
                    loader = ProviderMethods.getPoinWithDestinationEntry(getActivity(), projection, null);
                    break;
            }

            return loader;
        }

        @Override
        public void onLoadFinished(Loader loader, Cursor data) {
            Log.d(TAG, "on cursor load finished " + data.getCount());

            switch (loader.getId()) {
                case POI_LOADER_ID:
                    if (poiMarkers != null) {
                        mMapView.getOverlayManager().remove(poiMarkers);
                    }
                    UIUtils.hideProgressBar(getActivity());
                    while (data.moveToNext()) {
                        POIEntity poi = new POIEntity(data);
                        List<POIEntity> poiList = routePoiMap.get(poi.getRouteId());
                        if (poiList != null) {
                            poiList.add(poi);
                        }
                    }
                    Log.d(TAG, "POI_LOADER_ID finished");
                    String activeRouteId = MapPreferences.getCurrentRoute(getActivity());
                    if (activeRouteId != null) {
                        getLoaderManager().initLoader(POI_CONNECTION_LOADER_ID, null, this);
                    } else {
                        dataReady();
                        UIUtils.hideProgressBar(getActivity());
                    }
                    break;
                case ROUTE_LOADER_ID:
                    if (data.getCount() > 0) {
                        activeRouteIconMap.clear();
                        inactiveRouteIconMap.clear();
                    }
                    DisplayMetrics metrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    Log.d(TAG, "density is " + metrics.density);
                    Log.d(TAG, "densitydpi is " + metrics.densityDpi);
                    while (data.moveToNext()) {
                        String routeObjectId = data.getString(0);
                        String iconId = data.getString(1);
                        routeIconIdMap.put(routeObjectId, iconId);
                        try {

                            Bitmap ab = BitmapFactory.decodeStream(FileUtils.getFileStream(getActivity(), iconId, FileUtils.ICON_PIN_ACTIVE_RETINA));
                            ab.setDensity(DisplayMetrics.DENSITY_XHIGH);
                            activeRouteIconMap.put(routeObjectId, ab);
                            Bitmap ib = BitmapFactory.decodeStream(FileUtils.getFileStream(getActivity(), iconId, FileUtils.ICON_PIN_INACTIVE_RETINA));
                            ib.setDensity(DisplayMetrics.DENSITY_XHIGH);
                            inactiveRouteIconMap.put(routeObjectId, ib);
                        } catch (IOException e) {
                            Log.e(TAG, "exception " + e.getMessage(), e);
                        }
                        routePoiMap.put(routeObjectId, new ArrayList<POIEntity>());
                    }
                    Log.d(TAG, "ROUTE_LOADER_ID finished");
                    getLoaderManager().initLoader(POI_LOADER_ID, null, this);
                    break;
                case POI_CONNECTION_LOADER_ID:
                    while (data.moveToNext()) {
                        String sourceObjectId = data.getString(0);
                        double sourceLat = data.getDouble(1);
                        double sourceLon = data.getDouble(2);
                        String destObjectId = data.getString(3);
                        double destLat = data.getDouble(4);
                        double destLon = data.getDouble(5);
                        POIEntity source = new POIEntity();
                        source.setObjectId(sourceObjectId);
                        source.setPosition(new GeoPoint(sourceLat, sourceLon));
                        POIEntity dest = new POIEntity();
                        dest.setObjectId(destObjectId);
                        dest.setPosition(new GeoPoint(destLat, destLon));
                        poiConnectionList.add(new POIConnection(source, dest));

                    }
                    dataReady();
                    UIUtils.hideProgressBar(getActivity());
                    Log.d(TAG, "POI_CONNECTION_LOADER_ID finished");
                    break;
            }


        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };

    /* ------END OF DATA LOADING METHODS ---- */

    /**
     * when data load is finished, form marker lists, roads, point system progress bar
     */
    private void dataReady() {
        if (getActivity() == null) {
            return;
        }
       if (poiMarkers != null) {
            mMapView.getOverlayManager().remove(poiMarkers);
            poiMarkers.destroy();
        }

        poiMarkers = new PoiRadiusMarkerCluster(getActivity(), clusterMarkerClickListener);
        Drawable clusterIconD = getResources().getDrawable(R.drawable.cluster_marker_pin_inactive);
        Bitmap clusterIcon = ((BitmapDrawable) clusterIconD).getBitmap();
        poiMarkers.setIcon(clusterIcon);
        poiMarkers.setMaxClusteringZoomLevel(17);
        boolean allActive = true;

        String activeRouteId = MapPreferences.getCurrentRoute(getActivity());
        allActive = (activeRouteId == null);

        for (List<POIEntity> poiList : routePoiMap.values()) {
            formPOIEntityMarkerList(poiList, poiMarkers, allActive);
        }

        mMapView.getOverlayManager().add(poiMarkers);
        mMapView.getOverlayManager().remove(mLocationOverlay);
        mMapView.getOverlayManager().add(mLocationOverlay);
        Log.d(TAG, "po markers added " + poiMarkers.getItems().size());
        if (activeRouteId != null) {
            //set bounds
            List<POIEntity> activePoiList = routePoiMap.get(activeRouteId);
            BoundingBoxE6 boundingBox = getBoundingBox(activePoiList);
            mMapView.zoomToBoundingBox(boundingBox);
            Log.d(TAG, "active route bounding box " + boundingBox);
            //load route for drawing only if active route is selected
            loadRouteForDrawingOsm();
        }

        updateProgressBar();

        mMapView.invalidate();
    }

    private int getPoiCountForRoutePoint(String routePointId) {
        int count = 0;
        for (List<POIEntity> poiList : routePoiMap.values()) {
            for (POIEntity poi : poiList) {
                if (routePointId.equals(poi.getRoutePointId())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Redraw markers
     */
    public void reloadData() {
        dataReady();

    }

    /**
     * recalculate point system progress
     */
    public void updateProgressBar() {
        Log.d(TAG, "updateProgressBar");
        String routePointId = MapPreferences.getLastVisitedRoutePointId(getActivity());
        Set<String> visitedSet = MapPreferences.getVisitedPoisByRoutePointId(getActivity(), routePointId);
        Log.d(TAG, "route point id " + routePointId);
        Log.d(TAG, "visited set " + visitedSet);
        if (routePointId != null && visitedSet != null && visitedSet.size() > 0) {
            Log.d(TAG, "visited routes count " + visitedSet.size());
            int count = getPoiCountForRoutePoint(routePointId);
            Log.d(TAG, "poi count for routepoint " + count + "routepoint id " + routePointId);
            if (count > 0) {
                addProgressBar((float) visitedSet.size() / (float) count);
            }

        }
    }

    private void addProgressBar(float ratio) {

        String routePointId = MapPreferences.getLastVisitedRoutePointId(getActivity());
        Log.d(TAG, "progress bar overlay " + routePointId);
        if (progressBarOverlay == null) {
            //Set<String> visitedRoutes = MapPreferences.getVisitedPoisByRoutePointId(getActivity(), routePointId);
            progressBarOverlay = new ProgressBarOverlay(getActivity(), mMapView.getResourceProxy());
            mMapView.getOverlayManager().add(progressBarOverlay);

        }
        mMapView.getOverlayManager().remove(progressBarOverlay);
        progressBarOverlay.setRatio(ratio);
        mMapView.getOverlayManager().add(progressBarOverlay);
        mMapView.invalidate();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FlurryAgent.logEvent(getString(R.string.flurry_map_fragment), true);
    }

    @Override
    public void onDetach() {
        if (mMapView != null) {
            mMapView.getTileProvider().clearTileCache();
        }
        UIUtils.hideProgressBar(getActivity());
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_map_fragment));
    }

    /**
     * Disable view for 500ms, so it can not be clicked before the action is done.
     * @param view
     */
    public void disableView(final View view) {

        view.setEnabled(false);

        Runnable enableViewRunnable = new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        };
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(enableViewRunnable, 500);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.getTileProvider().clearTileCache();
            //System.gc();
        }
    }

    /** Listeners */

    private TabbView.OnTabClickedListener onTabClickedListener = new TabbView.OnTabClickedListener() {
        @Override
        public void onTabClicked(int id, boolean animate) {
            Log.d(TAG, "on tab clicked " + id);
            ((TabbView.OnTabClickedListener) getActivity()).onTabClicked(id, animate);
        }
    };

    /**
     * Map settings button click listener. Opens MapSettings fragment
     */
    private View.OnClickListener onSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            disableView(v);
            //settings button clicked open settings fragment
            //List<RouteHisContract> routeList = mModel.getRouteList();
            SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BOOK_PAGE);
            MapSettingsFragment fragment = MapSettingsFragment.newInstance();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.anim_grow_from_left, R.anim.anim_shrink_to_left, 0, R.anim.anim_shrink_to_left);
            fragmentTransaction.add(R.id.content, fragment, MapSettingsFragment.class.getSimpleName());
            fragmentTransaction.addToBackStack(MapSettingsFragment.class.getSimpleName());
            fragmentTransaction.commit();
        }
    };
    /**
     * Location button click listener. Checks if user location is available and if it isanimates to user location point
     * otherwise shows message.
     */
    private View.OnClickListener onLocationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            disableView(v);
            if (mLocationOverlay != null ) {
                GeoPoint myLoc = mLocationOverlay.getMyLocation();
                if (myLoc != null) {
                    mMapView.getController().animateTo(myLoc);
                    return;
                }
            }
            //if location is not available show error dialog
            ErrorDialogFragment newFragment = ErrorDialogFragment.newInstance(getString(R.string.no_gps_signal));
            newFragment.setCancelable(false);
            newFragment.show(getFragmentManager(), "dialog");

        }
    };

    /**
     * Cluster marker click listener. When clicked, map zooms in 1 level fixing the marker point.
     */
    private Marker.OnMarkerClickListener clusterMarkerClickListener = new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            marker.getPosition();
            Point markerPoint = pointFromGeoPoint(marker.getPosition(), mapView);
            if (markerPoint != null) {
                mapView.getController().zoomInFixing(markerPoint.x, markerPoint.y);
                mapView.invalidate();
            }
            return true;
        }
    };

    /**
     * Pin click listener. Shows popup dialog about the poi that is associated with selected marker.
     * If poi is not in click range, informs user about that
     */
    private Marker.OnMarkerClickListener markerClickListener = new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            final POIEntity poi = (POIEntity) marker.getRelatedObject();
            if (poi != null) {

                boolean inClickRange = checkClickRange(marker.getPosition(), poi);
                //if poi was once visited it should be always clickable
                if (MapPreferences.isPoiVisited(getActivity(), poi.getObjectId())) {
                    inClickRange = true;
                }

                CustomPopupDialogFragment newFragment = CustomPopupDialogFragment.newInstance(poi.getObjectId(), inClickRange);
                newFragment.setCancelable(false);
                newFragment.show(getFragmentManager(), "dialog");

            }
            return true;
        }
    };


}
