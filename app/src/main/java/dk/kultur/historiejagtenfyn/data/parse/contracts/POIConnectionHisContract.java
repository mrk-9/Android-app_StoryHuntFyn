package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.POIConnectionColumns;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;

/**
 * Point Of Interest Connection info
 * https://parse.com/apps/historiejagt-fyn/collections#class/PointOfInterestConnection
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_POI_CONNECTION_COLLECTION)
public class POIConnectionHisContract extends ParseContract {
    private static final String LOG_TAG = POIConnectionHisContract.class.getSimpleName();
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        fillValues(values);
        return values;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(POIConnectionColumns.COLUMN_SOURCE, getString(SOURCE));
        values.put(POIConnectionColumns.COLUMN_DESTINATION, getString(DESTINATION));
    }

    public POIHisContract getSource() {
        return (POIHisContract) get(SOURCE);
    }

    public POIHisContract getDestination() {
        return (POIHisContract) get(DESTINATION);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof POIConnectionHisContract) {
            POIConnectionHisContract poi = (POIConnectionHisContract) o;
            if (this.getObjectId().equals(poi.getObjectId())) {
                return true;
            }

        }
        return super.equals(o);
    }

    public static ContentValues getContentValues(String source, String destination) {
        ContentValues values = new ContentValues();
        values.put(POIConnectionColumns.COLUMN_SOURCE, source);
        values.put(POIConnectionColumns.COLUMN_DESTINATION, destination);
        return values;
    }
}
