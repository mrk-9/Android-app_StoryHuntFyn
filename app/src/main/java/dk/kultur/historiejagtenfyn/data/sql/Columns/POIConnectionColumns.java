package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.POIConnectionHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;

/**
 * Created by JustinasK on 2/22/2015.
 */
public interface POIConnectionColumns {

    public static final String COLUMN_SOURCE = POIConnectionHisContract.SOURCE + ParseContract.POINTER_ID;
    public static final String COLUMN_DESTINATION = POIConnectionHisContract.DESTINATION + ParseContract.POINTER_ID;

}
