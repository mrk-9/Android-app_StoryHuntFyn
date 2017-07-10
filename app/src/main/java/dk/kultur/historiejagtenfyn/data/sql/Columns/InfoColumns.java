package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.InfoHisContract;

/**
 * Created by JustinasK on 2/22/2015.
 */
public interface InfoColumns {

    String COLUMN_LANGUAGE_ID = InfoHisContract.KEY_LANGUAGE + InfoHisContract.POINTER_ID;
    String COLUMN_TITLE = InfoHisContract.KEY_TITLE;
    String COLUMN_TEXT = InfoHisContract.KEY_TEXT;
}
