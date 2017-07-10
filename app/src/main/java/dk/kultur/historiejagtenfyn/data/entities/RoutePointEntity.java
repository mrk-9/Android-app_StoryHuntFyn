package dk.kultur.historiejagtenfyn.data.entities;

import android.database.Cursor;

import static dk.kultur.historiejagtenfyn.data.sql.HisContract.*;

/**
 * Created by Lina on 2015.02.27
 */
public class RoutePointEntity {

    public static String [] COLUMNS = {
            RoutePointContentEntry.KEY_TEXT25, RoutePointContentEntry.KEY_TEXT50,
            RoutePointContentEntry.KEY_TEXT75, RoutePointContentEntry.KEY_TEXT100

    };

    private String objectId;
    private String text25;
    private String text50;
    private String text75;
    private String text100;
    private int poiCount;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getText25() {
        return text25;
    }

    public void setText25(String text25) {
        this.text25 = text25;
    }

    public String getText50() {
        return text50;
    }

    public void setText50(String text50) {
        this.text50 = text50;
    }

    public String getText75() {
        return text75;
    }

    public void setText75(String text75) {
        this.text75 = text75;
    }

    public String getText100() {
        return text100;
    }

    public void setText100(String text100) {
        this.text100 = text100;
    }

    public int getPoiCount() {
        return poiCount;
    }

    public void setPoiCount(int poiCount) {
        this.poiCount = poiCount;
    }

    public void setDataFromCursor(Cursor data) {
        this.text25 = data.getString(0);
        this.text50 = data.getString(1);
        this.text75 = data.getString(2);
        this.text100 = data.getString(3);

    }
}
