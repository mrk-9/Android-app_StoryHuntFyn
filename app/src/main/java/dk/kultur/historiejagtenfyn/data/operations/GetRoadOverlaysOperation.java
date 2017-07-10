package dk.kultur.historiejagtenfyn.data.operations;

import android.content.Context;
import android.util.Log;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.entities.POIConnection;
import dk.kultur.historiejagtenfyn.data.entities.POIEntity;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIConnectionHisContract;

/**
 * Created by Lina on 2015.02.20
 */
public class GetRoadOverlaysOperation extends AbsAsyncOperation<List<Polyline>> {

    private Context context;
    private List<POIConnection>poiConnectionList;

    public GetRoadOverlaysOperation(Context context, List<POIConnection>poiConnectionList) {
        this.context = context;
        this.poiConnectionList = poiConnectionList;
    }

    @Override
    protected List<Polyline> doInBackground(String... params) {
        List<Polyline> routeDrawingOverlays = null;
        final RoadManager roadManager = new MapQuestRoadManager(context.getString(R.string.map_quest_api_key));
        roadManager.addRequestOption("routeType=bicycle");
        final int color = context.getResources().getColor(R.color.route_color);
        final float width = 10.0f;

        for (final POIConnection con : poiConnectionList) {

            ArrayList<GeoPoint> pointList = new ArrayList<GeoPoint>();
            pointList.add(con.getSource().getPosition());
            pointList.add(con.getDestination().getPosition());
            try {
                Road road = roadManager.getRoad(pointList);

                Polyline roadOverlay = RoadManager.buildRoadOverlay(road, color, width, context);
                if (routeDrawingOverlays == null) {
                    routeDrawingOverlays = new ArrayList<>();
                }
                routeDrawingOverlays.add(roadOverlay);

            } catch (Exception e) {
                Log.e("GetRoadOverlayOperation", "get road exception", e);
            }
        }
        return routeDrawingOverlays;
    }
}
