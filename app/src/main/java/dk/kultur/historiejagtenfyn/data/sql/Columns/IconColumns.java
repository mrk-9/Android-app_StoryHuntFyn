package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.IconHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;

/**
 * Created by JustinasK on 2/21/2015.
 */
public interface IconColumns {

    public static final String COLUMN_AR_PIN_URL = IconHisContract.KEY_AR_PIN + ParseContract.URL;
    public static final String COLUMN_AR_PIN_INACTIVE_URL = IconHisContract.KEY_AR_PIN_INACTIVE + ParseContract.URL;
    public static final String COLUMN_AR_PIN_INACTIVE_RETINA_URL = IconHisContract.KEY_AR_PIN_INACTIVE_RETINA + ParseContract.URL;
    public static final String COLUMN_AR_PIN_RETINA_URL = IconHisContract.KEY_AR_PIN_RETINA + ParseContract.URL;
    public static final String COLUMN_ICON_URL = IconHisContract.KEY_ICON + ParseContract.URL;
    public static final String COLUMN_ICON_RETINA_URL = IconHisContract.KEY_ICON_RETINA + ParseContract.URL;
    public static final String COLUMN_PIN_URL = IconHisContract.KEY_PIN + ParseContract.URL;
    public static final String COLUMN_PIN_INACTIVE_URL = IconHisContract.KEY_PIN_INACTIVE + ParseContract.URL;
    public static final String COLUMN_PIN_INACTIVE_RETINA_URL = IconHisContract.KEY_PIN_INACTIVE_RETINA + ParseContract.URL;
    public static final String COLUMN_PIN_RETINA_URL = IconHisContract.KEY_PIN_RETINA + ParseContract.URL;
    public static final String COLUMN_NAME = IconHisContract.KEY_NAME;
    public static final String COLUMN_ICON_ID = IconHisContract.KEY_ICON_ID;
}
