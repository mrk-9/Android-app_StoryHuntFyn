package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RoutePointContentHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RoutePointHisContract;

/**
 * Created by JustinasK on 2/22/2015.
 */
public interface RoutePointColumns {

    String COLUMNS_ROUTE = RoutePointHisContract.KEY_POINTER_ROUTE + ParseContract.POINTER_ID;
    String COLUMNS_NAME = RoutePointHisContract.KEY_NAME;

    /**
     * Created by JustinasK on 2/22/2015.
     */
    public interface RoutePointContentColumns {
        String KEY_LANGUAGE = RoutePointContentHisContract.KEY_POINTER_LANGUAGE + ParseContract.POINTER_ID;
        String KEY_TEXT100 = RoutePointContentHisContract.KEY_TEXT100;
        String KEY_TEXT25 = RoutePointContentHisContract.KEY_TEXT25;
        String KEY_TEXT50 = RoutePointContentHisContract.KEY_TEXT50;
        String KEY_TEXT75 = RoutePointContentHisContract.KEY_TEXT75;
    }
}
