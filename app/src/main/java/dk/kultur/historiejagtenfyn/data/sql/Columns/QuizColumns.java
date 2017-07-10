package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.QuizContentHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.QuizHisContract;

/**
 * Created by JustinasK on 2/22/2015.
 */
public interface QuizColumns {

    String COLUMN_NAME = QuizHisContract.KEY_NAME;

    public interface QuizContentColumns {

        String COLUMN_HEADER = QuizContentHisContract.KEY_HEADER;
        String COLUMN_LANGUAGE_ID = QuizContentHisContract.KEY_POINTER_LANGUAGE + ParseContract.POINTER_ID;
    }

}
