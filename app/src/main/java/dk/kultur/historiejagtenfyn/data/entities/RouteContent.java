package dk.kultur.historiejagtenfyn.data.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

/**
 * Created by Lina on 2014.07.01.
 */
@DatabaseTable(tableName = RouteContent.TABLE_NAME)
public class RouteContent extends AbsEntity {
    public static final String TABLE_NAME = "routecontent";
    public static final String COLUMN_INFO = "info";
    public static final String COLUMN_NAME = "name";

    // relations
    public static final String COLUMN_LANGUAGE_ID = "language_id";
    public static final String COLUMN_ROUTE_ID = "route_id";

    @DatabaseField(columnName = COLUMN_NAME)
    private String mName;
    @DatabaseField(columnName = COLUMN_INFO)
    private String mInfo; //route content

    @DatabaseField(columnName = COLUMN_LANGUAGE_ID)
    private String mLanguageId;
    @DatabaseField(columnName = COLUMN_ROUTE_ID)
    private String mRouteId;

    public RouteContent() {

    }

    public RouteContent(ParseObject routeContent) {
        super(routeContent);
        mName = routeContent.getString("name");
        mInfo = routeContent.getString("info");

    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String mInfo) {
        this.mInfo = mInfo;
    }

    public String getLanguageId() {
        return mLanguageId;
    }

    public void setLanguageId(String languageId) {
        this.mLanguageId = languageId;
    }

    public String getRouteId() {
        return mRouteId;
    }

    public void setRouteId(String routeId) {
        this.mRouteId = routeId;
    }
}
