package dk.kultur.historiejagtenfyn;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RouteHisContract;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.POIEntry;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.RouteEntry;
import dk.kultur.historiejagtenfyn.data.sql.HisDataHelper;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Test DB
 * Created by JustinasK on 2/10/2015.
 */
public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = TestDb.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(HisDataHelper.DATABASE_NAME);
        SQLiteDatabase db = new HisDataHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        HisDataHelper dbHelper = new HisDataHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = db.insert(RouteEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                RouteEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = createPOIValues(locationRowId);

        long weatherRowId = db.insert(POIEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                POIEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        validateCursor(weatherCursor, weatherValues);

        dbHelper.close();
    }

    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(RouteHisContract.KEY_NAME, "North Pole");
        testValues.put(RouteHisContract.KEY_OBJECT_ID, "zxcxzcxzc");
        testValues.put(RouteHisContract.KEY_ICON, "icon");
        return testValues;
    }
    static ContentValues createPOIValues(long routeRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(POIHisContract.KEY_OBJECT_ID, "054065");
        weatherValues.put(POIHisContract.KEY_PARENT_ROUTE,  routeRowId);
        return weatherValues;
    }


    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());
        Set<Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
