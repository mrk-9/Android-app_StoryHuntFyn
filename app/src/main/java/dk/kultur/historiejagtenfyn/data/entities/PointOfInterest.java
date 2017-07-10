package dk.kultur.historiejagtenfyn.data.entities;

import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

/**
 * Created by Lina on 2014.06.27.
 */
@DatabaseTable(tableName = PointOfInterest.TABLE_NAME)
public class PointOfInterest extends AbsEntity {

    public static final String TABLE_NAME = "point_of_interest";
    public static final String COLUMN_VIDEO_URL = "video_url";
    public static final String COLUMN_AR_RANGE = "ar_range";
    public static final String COLUMN_AUTO_RANGE = "auto_range";
    public static final String COLUMN_CLICK_RANGE = "click_range";
    public static final String COLUMN_MAP_RANGE = "map_range";

    // relations
    public static final String COLUMN_AVATAR_OBJECTID = "avatar_objectid";
    public static final String COLUMN_ROUTE_ID = "route_id";
    public static final String COLUMN_DESTINATION_ID = "destination_poi_id";
    public static final String COLUMN_PARENT_ID = "parent_id";
    public static final String COLUMN_UNLOCK_ID = "unlock_id";
    public static final String COLUMN_IMAGE_ID = "image_id";
    public static final String COLUMN_QUIZ_ID = "quiz_id";


    @DatabaseField(columnName = COLUMN_PARENT_ID)
    private String mParentObjectId;
    @DatabaseField(columnName = COLUMN_MAP_RANGE)
    private double mMapRange;
    @DatabaseField(columnName = COLUMN_ROUTE_ID)
    private String mPoiRouteId;
    @DatabaseField(columnName = COLUMN_AUTO_RANGE)
    private long mAutoRange;
    @DatabaseField(columnName = COLUMN_CLICK_RANGE)
    private long mClickRange;
    @DatabaseField(columnName = COLUMN_DESTINATION_ID)
    private String mDestinationId;
    @DatabaseField(columnName = COLUMN_IMAGE_ID)
    private String mImageId;
    @DatabaseField(columnName = COLUMN_LATITUDE)
    private double latitude;
    @DatabaseField(columnName = COLUMN_LONGITUDE)
    private double longitude;

    private PointOfInterest mDestination;

    private LatLng mLocation;

    private long mLastTimestamp;


    public PointOfInterest(ParseObject entity) {
        super(entity);
    }
}
