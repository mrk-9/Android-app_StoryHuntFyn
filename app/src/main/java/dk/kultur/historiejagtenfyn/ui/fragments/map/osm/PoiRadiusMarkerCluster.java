package dk.kultur.historiejagtenfyn.ui.fragments.map.osm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.entities.POIEntity;
//import dk.kultur.historiejagtenfyn.data.parse.contracts.POIHisContract;

/**
 * Created by Lina on 2015.02.19
 */
public class PoiRadiusMarkerCluster extends RadiusMarkerClusterer {

    private Context context;
    private Marker.OnMarkerClickListener markerClickListener;
    private Map<String, Marker> freeMarkers = new HashMap<>();

    public PoiRadiusMarkerCluster(Context ctx, Marker.OnMarkerClickListener markerClickListener) {
        super(ctx);
        context = ctx;
        this.markerClickListener = markerClickListener;
    }

    @Override public Marker buildClusterMarker(StaticCluster cluster, MapView mapView) {
        Marker m = new Marker(mapView);
        m.setPosition(cluster.getPosition());
        m.setInfoWindow(null);
        m.setAnchor(mAnchorU, mAnchorV);

        int activeCount = 0;
        for (int i=0; i<cluster.getSize(); i++) {
            Marker marker = cluster.getItem(i);
            POIEntity poi = (POIEntity) marker.getRelatedObject();
            if (poi.isActive()) {
                activeCount++;
            }
        }

        Drawable d = context.getResources().getDrawable(R.drawable.cluster_marker_pin_inactive);
        float markerRatio = (float)activeCount/(float)cluster.getSize();
        if (activeCount > 0) {
            if (markerRatio < 0.5f) {
                d = context.getResources().getDrawable(R.drawable.cluster_marker_pin1);
            } else if (markerRatio < 1.0f ) {
                d = context.getResources().getDrawable(R.drawable.cluster_marker_pin2);
            } else {
                d = context.getResources().getDrawable(R.drawable.cluster_marker_pin3);
            }
        }
        Bitmap clusterIcon = ((BitmapDrawable)d).getBitmap();
        Bitmap finalIcon = Bitmap.createBitmap(clusterIcon.getWidth(), clusterIcon.getHeight(), clusterIcon.getConfig());
        Canvas iconCanvas = new Canvas(finalIcon);


        iconCanvas.drawBitmap(clusterIcon, 0, 0, null);

        m.setIcon(new BitmapDrawable(mapView.getContext().getResources(), finalIcon));

        m.setOnMarkerClickListener(markerClickListener);
        return m;
    }


    public void destroy() {
        context = null;
        markerClickListener = null;
        mClusters = null;
        freeMarkers = null;
    }

    @Override public void renderer(ArrayList<StaticCluster> clusters, Canvas canvas, MapView mapView) {
        freeMarkers.clear();
        for (StaticCluster cluster : clusters) {
            if (cluster.getSize() == 1) {
                //cluster has only 1 marker => use it as it is:
                cluster.setMarker(cluster.getItem(0));
                Marker m = cluster.getItem(0);
                POIEntity poi = (POIEntity) m.getRelatedObject();
                freeMarkers.put(poi.getObjectId(), m);
            } else {
                //only draw 1 Marker at Cluster center, displaying number of Markers contained
                Marker m = buildClusterMarker(cluster, mapView);
                cluster.setMarker(m);
            }
        }

    }

    @Override protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        //if zoom has changed and mapView is now stable, rebuild clusters:
        int zoomLevel = mapView.getZoomLevel();
        if (zoomLevel != mLastZoomLevel && !mapView.isAnimating()){
            mClusters = clusterer(mapView);
            renderer(mClusters, canvas, mapView);
            mLastZoomLevel = zoomLevel;

        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(context.getResources().getColor(R.color.route_color));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1.0f);
        Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setColor(0x77bbbbbb);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setStrokeWidth(2.0f);
        final Projection pj = mapView.getProjection();
        for (Marker m : freeMarkers.values()) {
            POIEntity poi = (POIEntity) m.getRelatedObject();
            if (!poi.isActive()) {
                continue;
            }
            String parentId = poi.getParentPoiId();
            if (parentId != null) {
                Marker parentMarker = freeMarkers.get(parentId);
                if (parentMarker != null) {

                    Point poiPixels = new Point();
                    Point parentPixels = new Point();
                    pj.toPixels(m.getPosition(), poiPixels);
                    pj.toPixels(parentMarker.getPosition(), parentPixels);
                    canvas.drawLine(poiPixels.x, poiPixels.y, parentPixels.x, parentPixels.y, paint);
                    canvas.drawLine(poiPixels.x + 1, poiPixels.y + 1, parentPixels.x + 1, parentPixels.y + 1, shadowPaint);
                }
            }
        }

        for (StaticCluster cluster:mClusters){
            cluster.getMarker().draw(canvas, mapView, shadow);
        }

    }
}
