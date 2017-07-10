package dk.kultur.historiejagtenfyn.data.entities;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

/**
 * Created by Lina on 2014.06.26.
 */
@DatabaseTable(tableName = Route.TABLE_NAME)
public class Route extends AbsEntity {
    public static final String TABLE_NAME = "route";
    public static final String COLUMN_NAME = "name";
    // icon
    public static final String COLUMN_ICON_OBJECTID = "icon_object_id";
    public static final String COLUMN_ICONID = "icon_id";
    // avatar
    public static final String COLUMN_AVATAR_OBJECTID = "avatar_object_id";
    public static final String COLUMN_AVATAR_NAME = "avatar_name";

    @DatabaseField(columnName = COLUMN_NAME)
    private String mName;
    @DatabaseField(columnName = COLUMN_LATITUDE)
    private double mLatitude;
    @DatabaseField(columnName = COLUMN_LONGITUDE)
    private double mLongitude;
    @DatabaseField(columnName = COLUMN_ICON_OBJECTID)
    private String mIconObjectId;
    @DatabaseField(columnName = COLUMN_AVATAR_OBJECTID)
    private String mAvatarObjectId;
    @DatabaseField(columnName = COLUMN_ICONID)
    //drawable name of icon (for getting icon from resources)
    private String iconId;

    @DatabaseField(columnName = COLUMN_AVATAR_NAME)
    //avatar name (for getting avatar image)
    private String mAvatarName;


    private LatLng mLocation; //center coordinates

    private RouteContent mContent;


    public Route() {
        super();
    }

    public Route(ParseObject entity) {
        super(entity);
        mName = entity.getString("name");
        if(entity.getParseGeoPoint("centerCoordinates") != null) {
            mLatitude = entity.getParseGeoPoint("centerCoordinates").getLatitude();
            mLongitude = entity.getParseGeoPoint("centerCoordinates").getLongitude();
            mLocation = new LatLng(mLatitude, mLongitude);
        }

    }

    public void setAvatar(ParseObject avatar) {
        mAvatarName = avatar.getString("avatar");
        mAvatarObjectId = avatar.getObjectId();
    }

    public void setIcon(ParseObject icon) {
        iconId = icon.getString("iconId");
        mIconObjectId = icon.getObjectId();

    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public void setLocation(LatLng location) {
        this.mLocation = location;
    }

    public String getAvatarObjectId() {
        return mAvatarObjectId;
    }

    public void setAvatarObjectId(String mAvatarObjectId) {
        this.mAvatarObjectId = mAvatarObjectId;
    }

    public String getIconObjectId() {
        return mIconObjectId;
    }

    public void setIconObjectId(String mIconObjectId) {
        this.mIconObjectId = mIconObjectId;
    }

    public RouteContent getContent() {
        return mContent;
    }

    public void setContent(RouteContent mContent) {
        this.mContent = mContent;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    @Override
    public String toString() {
        return mName;
    }
}
