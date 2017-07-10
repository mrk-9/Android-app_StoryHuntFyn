package dk.kultur.historiejagtenfyn.data.parse;

import android.util.Log;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds more than 1000 records of parse table
 * Created by JustinasK on 2/4/2015.
 */
public class FindAllCallback<T extends com.parse.ParseObject> implements FindCallback<T> {
    private static final String LOG_TAG = FindAllCallback.class.getSimpleName();

    private final List<T> allObjects = new ArrayList<>();
    private final ParseQuery<T> query;
    private final FindCallback<T> finalCallback;
    private int skip = 0;

    public FindAllCallback(ParseQuery<T> query, FindCallback<T> callback) {
        this.query = query;
        this.finalCallback = callback;
    }

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public void done(List<T> list, ParseException e) {
        if (e == null) {
            Log.d(getLogTag(), "We found part of PokeDex " + list.size());
            allObjects.addAll(list);
            if (list.size() == query.getLimit()) {
                skip = skip + query.getLimit();
                query.setSkip(skip);
                query.findInBackground(this);
            } else {
                finalCallback.done(allObjects, null);
                Log.d(getLogTag(), "We have a full PokeDex " + allObjects.size());
            }
        }
    }
}
