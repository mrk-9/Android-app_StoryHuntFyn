package dk.kultur.historiejagtenfyn.data.entities;

import static dk.kultur.historiejagtenfyn.data.sql.HisContract.*;

import android.database.Cursor;

import org.osmdroid.util.GeoPoint;

import dk.kultur.historiejagtenfyn.data.parse.contracts.POIHisContract;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;

public class POIEntity {

    public static final String[] COLUMNS_FOR_MAP_MARKERS = new String[]{
            POIHisContract.COORDINATES + POIHisContract.COORDINATES_LAT,
            POIHisContract.COORDINATES + POIHisContract.COORDINATES_LON,
            POIHisContract.MAP_RANGE,
            POIHisContract.CLICK_RANGE,
            POIEntry.TABLE_NAME + ".objectId",
            RouteEntry.TABLE_NAME + ".objectId",
            JoinPOIsRoutePointEntry.TABLE_NAME + "." + HisContract.JoinPOIsRoutePointEntry.COLUMN_OWNER,
            POIEntry.COLUMN_UNLOCK_POI,
            POIEntry.COLUMN_PARENT_POI,
            POIEntry.COLUMN_PARENT_POINT};

    public static final String[] COLUMNS_FOR_POI_CONTENT = new String[]{
            POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_AUDIO,
            POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_VIDEO_URL,
            POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_IMAGE,
            POIContentEntry.TABLE_NAME + "." + POIContentEntry.COLUMN_NAME,
            POIContentEntry.TABLE_NAME + "." + POIContentEntry.COLUMN_INFO,
            POIContentEntry.TABLE_NAME + "." + POIContentEntry.COLUMN_IMAGE_TITLE,
            POIContentEntry.TABLE_NAME + "." + POIContentEntry.COLUMN_VIDEO_TITLE,
            POIEntry.TABLE_NAME + ".objectId",
            POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_AVATAR,
            POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_QUIZ,
            POIContentEntry.TABLE_NAME + "." + POIContentEntry.COLUMN_FACTS};

    private GeoPoint position;
    private int mapRange;
    private int clickRange;
    private String objectId;
    private String routeId;
    private String routePointId;
    private boolean active;

    private String audioUrl;
    private String videoUrl;
    private String imageUrl;
    private String name;
    private String info;
    private String imageTitle;
    private String videoTitle;
    private String avatarObjectId;

    private String unlockPoiId;
    private String parentPoiId;
    private int parentPoint; //indicates if this poi is parent

    public static POIEntity newInstanceForContent(Cursor data) {
        POIEntity poi = new POIEntity();
        poi.audioUrl = data.getString(0);
        poi.videoUrl = data.getString(1);
        poi.imageUrl = data.getString(2);
        poi.name = data.getString(3);
        poi.info = data.getString(4);
        poi.imageTitle = data.getString(5);
        poi.videoTitle = data.getString(6);
        poi.objectId = data.getString(7);
        poi.avatarObjectId = data.getString(8);
        return poi;

    }


    public POIEntity() {

    }

    public POIEntity(Cursor data) {
        position = new GeoPoint(data.getDouble(0), data.getDouble(1));
        mapRange = data.getInt(2);
        clickRange = data.getInt(3);
        objectId = data.getString(4);
        routeId = data.getString(5);
        routePointId = data.getString(6);
        unlockPoiId = data.getString(7);
        parentPoiId = data.getString(8);
        parentPoint = data.getInt(9);

    }

    public GeoPoint getPosition() {
        return position;
    }

    public void setPosition(GeoPoint position) {
        this.position = position;
    }

    public int getMapRange() {
        return mapRange;
    }

    public void setMapRange(int mapRange) {
        this.mapRange = mapRange;
    }

    public int getClickRange() {
        return clickRange;
    }

    public void setClickRange(int clickRange) {
        this.clickRange = clickRange;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRoutePointId() {
        return routePointId;
    }

    public void setRoutePointId(String routePointId) {
        this.routePointId = routePointId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getAvatarObjectId() {
        return avatarObjectId;
    }

    public void setAvatarObjectId(String avatarObjectId) {
        this.avatarObjectId = avatarObjectId;
    }

    public String getUnlockPoiId() {
        return unlockPoiId;
    }

    public void setUnlockPoiId(String unlockPoiId) {
        this.unlockPoiId = unlockPoiId;
    }

    public String getParentPoiId() {
        return parentPoiId;
    }

    public void setParentPoiId(String parentPoiId) {
        this.parentPoiId = parentPoiId;
    }

    public boolean isParentPoi() {
        return (parentPoint == 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof POIEntity) {
            POIEntity other = (POIEntity) o;
            if (objectId != null && objectId.equals(other.objectId)) {
                return true;
            }
        }

        return false;
    }
}
