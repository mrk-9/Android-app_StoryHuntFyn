package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RouteContentHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RouteHisContract;

/**
 * Route table columns
 * Created by JustinasK on 2/20/2015.
 */
public interface RouteColumns extends ParseColumns {

    public static final String COLUMN_AVATAR = RouteHisContract.KEY_AVATAR + ParseContract.POINTER_ID;
    public static final String COLUMN_CENTRED_COORDINATES_LAT = RouteHisContract.KEY_CENTER_COORDINATES + RouteHisContract.KEY_CENTER_COORDINATES_COLUMN_LAT;
    public static final String COLUMN_CENTRED_COORDINATES_LON = RouteHisContract.KEY_CENTER_COORDINATES + RouteHisContract.KEY_CENTER_COORDINATES_COLUMN_LON;
    public static final String COLUMN_ICON = RouteHisContract.KEY_ICON + ParseContract.POINTER_ID;
    public static final String COLUMN_NAME = RouteHisContract.KEY_NAME;


    /**
     * Created by JustinasK on 2/20/2015.
     */
    public interface RouteContentColumns extends ParseColumns {
        public static final String COLUMN_LANGUAGE = RouteContentHisContract.KEY_language + RouteContentHisContract.POINTER_ID;
        public static final String COLUMN_INFO = RouteContentHisContract.KEY_info;
        public static final String COLUMN_NAME = RouteContentHisContract.KEY_name;
    }
}
