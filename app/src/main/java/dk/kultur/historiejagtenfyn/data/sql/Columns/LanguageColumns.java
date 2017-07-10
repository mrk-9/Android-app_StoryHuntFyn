package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.LanguageHisContract;

/**
 * Created by JustinasK on 2/22/2015.
 */
public interface LanguageColumns extends ParseColumns {
    public static final String COLUMN_ACTIVE = LanguageHisContract.KEY_ACTIVE;
    public static final String COLUMN_CODE = LanguageHisContract.KEY_CODE;
    public static final String COLUMN_LANGUAGE = LanguageHisContract.KEY_LANGUAGE;
    public static final String COLUMN_PRIORITY = LanguageHisContract.KEY_PRIORITY;
}
