package dk.kultur.historiejagtenfyn.data.models;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import bolts.Continuation;
import bolts.Task;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.util.List;

/**
 * Main parse functions
 * Created by JustinasK on 1/30/2015.
 */
public class ParseModel {
    private static final String LOG_TAG = ParseModel.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static void startSearchTaskBackground(ParseQuery<ParseObject> query, final Activity errorViewable, final DialogInterface.OnClickListener retryCallback, Continuation<List<ParseObject>, Void> callback) {
        query.findInBackground().continueWith(new Continuation<List<ParseObject>, List<ParseObject>>() {
            @Override
            public List<ParseObject> then(Task<List<ParseObject>> listTask) throws Exception {
                if (listTask.isFaulted()) showDialog(errorViewable, retryCallback);
                return listTask.getResult();
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(callback, Task.UI_THREAD_EXECUTOR);
    }

    private static void showDialog(Activity errorViewable, DialogInterface.OnClickListener retryCallback) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(errorViewable);
        builder1.setMessage("Error");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Retry", retryCallback);
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

}
