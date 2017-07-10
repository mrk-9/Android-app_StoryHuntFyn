package dk.kultur.historiejagtenfyn;

import android.test.InstrumentationTestCase;
import bolts.Continuation;
import bolts.Task;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import dk.kultur.historiejagtenfyn.data.parse.FindAllCallback;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

/**
 * Test case for bolds api
 * Created by JustinasK on 11/24/2014.
 */
public class BoltsTest extends InstrumentationTestCase {
    public void testMountainViewThirdDay() throws JSONException {

    }

    public void testFremontLastDay() throws JSONException {
    }

    public void testSynchronousContinuation() {
        final Task<Integer> complete = Task.forResult(5);
        complete.continueWith(new Continuation<Integer, Void>() {
            public Void then(Task<Integer> task) {
                assertEquals(complete, task);
                assertTrue(task.isCompleted());
                assertEquals(5, task.getResult().intValue());
                assertFalse(task.isFaulted());
                assertFalse(task.isCancelled());
                return null;
            }
        });
    }


    public void testFindAllBackground() {
        final ParseQuery<ParseObject> parseQuery = new ParseQuery<>(ParserApiHis.KEY_LANGUAGE_COLLECTION);
        parseQuery.setLimit(2);
        parseQuery.findInBackground(new FindAllCallback<>(parseQuery, new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                final int count;
                try {
                    count = parseQuery.count();
                } catch (ParseException e1) {
                    throw new RuntimeException(e);
                }
                assertEquals(list.size(), count);
            }
        }));
    }

}
