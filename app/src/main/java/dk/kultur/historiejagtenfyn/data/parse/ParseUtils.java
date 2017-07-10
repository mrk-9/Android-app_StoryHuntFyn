package dk.kultur.historiejagtenfyn.data.parse;

import android.util.Log;
import bolts.Capture;
import bolts.Continuation;
import bolts.Task;
import com.parse.*;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RouteHisContract;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

/**
 * Parse methods with background tasks
 * Created by JustinasK on 1/30/2015.
 */
@SuppressWarnings("UnusedDeclaration")
public class ParseUtils {
    private static final String LOG_TAG = ParseUtils.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public Task<Integer> getIntAsync(ParseQuery<ParseObject> obj) {
        final Task<Integer>.TaskCompletionSource tcs = Task.create();
        obj.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    tcs.setResult(count);
                } else {
                    tcs.setError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<ParseObject> fetchAsync(ParseObject obj) {
        final Task<ParseObject>.TaskCompletionSource tcs = Task.create();
        obj.fetchInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    tcs.setResult(object);
                } else {
                    tcs.setError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<ParseObject> saveAsync(ParseObject obj) {
        final Task<ParseObject>.TaskCompletionSource tcs = Task.create();
        obj.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    tcs.setResult(null);
                } else {
                    tcs.setError(e);
                }
            }
        });
        return tcs.getTask();
    }

    public Task<List<ParseObject>> findAsync(ParseQuery<ParseObject> obj) {
        final Task<List<ParseObject>>.TaskCompletionSource tcs = Task.create();
        obj.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    tcs.setResult(list);
                } else {
                    tcs.setError(e);
                }
            }
        });
        return tcs.getTask();
    }


    public Task<Void> deleteAsync(ParseObject obj) {
        final Task<Void>.TaskCompletionSource tcs = Task.create();
        obj.deleteInBackground(new DeleteCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    tcs.setResult(null);
                } else {
                    tcs.setError(e);
                }
            }
        });
        return null;
    }

    public void testExamle2() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.whereNotEqualTo("published", true);
        query.findInBackground().onSuccessTask(new Continuation<List<ParseObject>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<ParseObject>> task) throws Exception {
                List<ParseObject> results = task.getResult();

                List<Task<Void>> saveTasks = new ArrayList<>();
                for (final ParseObject post : results) {
                    post.put("published", true);
                    saveTasks.add(post.saveInBackground());
                }
                return Task.whenAll(saveTasks);
            }
        }).onSuccess(new Continuation<Void, Void>() {
            @Override
            public Void then(final Task<Void> task) {
//                mPostStatusTextView.setText(String.format("All Posts Published!"));
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    public void testMainThread(Continuation<List<ParseObject>, Void> callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Answer");
        query.whereNotEqualTo("correct", true);
        query.findInBackground().onSuccess(callback, Task.UI_THREAD_EXECUTOR);
    }

    public void testMainThreadErrorHandling(Continuation<List<ParseObject>, Void> callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Answer");
        query.whereNotEqualTo("correct", true);
        query.findInBackground().continueWith(callback, Task.UI_THREAD_EXECUTOR);
    }

    public void testMainThreadError(Continuation<List<ParseObject>, Void> callback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Answer");
        query.whereNotEqualTo("correct", true);
        final Task<List<ParseObject>> inBackground = query.findInBackground();
        inBackground.onSuccessTask(new Continuation<List<ParseObject>, Task<List<ParseObject>>>() {
            @Override
            public Task<List<ParseObject>> then(Task<List<ParseObject>> listTask) throws Exception {
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }


    public void testContinueWith(ParseObject obj) {
        saveAsync(obj).continueWith(new Continuation<ParseObject, Void>() {
            public Void then(Task<ParseObject> task) throws Exception {
                if (task.isCancelled()) {
                    // the save was cancelled.
                } else if (task.isFaulted()) {
                    // the save failed.
                    Exception error = task.getError();
                } else {
                    // the object was saved successfully.
                    ParseObject object = task.getResult();
                }
                return null;
            }
        });
    }

    /**
     * Gets a String asynchronously.
     */
    public Task<String> getStringAsync(ParseQuery<ParseObject> obj) {
        // Let's suppose getIntAsync() returns a Task<Integer>.
        return getIntAsync(obj).continueWith(
                // This Continuation is a function which takes an Integer as input,
                // and provides a String as output. It must take an Integer because
                // that's what was returned from the previous Task.
                new Continuation<Integer, String>() {
                    // The Task getIntAsync() returned is passed to "then" for convenience.
                    public String then(Task<Integer> task) throws Exception {
                        Integer number = task.getResult();
                        return String.format("%d", Locale.US, number);
                    }
                }
        );
    }

    public void testSuccess(ParseObject obj) {
        saveAsync(obj).onSuccess(new Continuation<ParseObject, Void>() {
            public Void then(Task<ParseObject> task) throws Exception {
                // the object was saved successfully.
                return null;
            }
        });
    }

    public void testChaning() {

        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Student");
        query.orderByDescending("gpa");
        findAsync(query).onSuccessTask(new Continuation<List<ParseObject>, Task<ParseObject>>() {
            public Task<ParseObject> then(Task<List<ParseObject>> task) throws Exception {
                List<ParseObject> students = task.getResult();
                students.get(0).put("valedictorian", true);
                return saveAsync(students.get(0));
            }
        }).onSuccessTask(new Continuation<ParseObject, Task<List<ParseObject>>>() {
            public Task<List<ParseObject>> then(Task<ParseObject> task) throws Exception {
                ParseObject valedictorian = task.getResult();
                return findAsync(query);
            }
        }).onSuccessTask(new Continuation<List<ParseObject>, Task<ParseObject>>() {
            public Task<ParseObject> then(Task<List<ParseObject>> task) throws Exception {
                List<ParseObject> students = task.getResult();
                students.get(1).put("salutatorian", true);
                return saveAsync(students.get(1));
            }
        }).onSuccess(new Continuation<ParseObject, Void>() {
            public Void then(Task<ParseObject> task) throws Exception {
                // Everything is done!
                return null;
            }
        });
    }

    public void errorHandling() {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Student");
        query.orderByDescending("gpa");
        findAsync(query).onSuccessTask(new Continuation<List<ParseObject>, Task<ParseObject>>() {
            public Task<ParseObject> then(Task<List<ParseObject>> task) throws Exception {
                List<ParseObject> students = task.getResult();
                students.get(0).put("valedictorian", true);
                // Force this callback to fail.
                throw new RuntimeException("There was an error.");
            }
        }).onSuccessTask(new Continuation<ParseObject, Task<List<ParseObject>>>() {
            public Task<List<ParseObject>> then(Task<ParseObject> task) throws Exception {
                // Now this continuation will be skipped.
                ParseObject valedictorian = task.getResult();
                return findAsync(query);
            }
        }).continueWithTask(new Continuation<List<ParseObject>, Task<ParseObject>>() {
            public Task<ParseObject> then(Task<List<ParseObject>> task) throws Exception {
                if (task.isFaulted()) {
                    // This error handler WILL be called.
                    // The exception will be "There was an error."
                    // Let's handle the error by returning a new value.
                    // The task will be completed with null as its value.
                    return null;
                }

                // This will also be skipped.
                List<ParseObject> students = task.getResult();
                students.get(1).put("salutatorian", true);
                return saveAsync(students.get(1));
            }
        }).onSuccess(new Continuation<ParseObject, Void>() {
            public Void then(Task<ParseObject> task) throws Exception {
                // Everything is done! This gets called.
                // The task's result is null.
                return null;
            }
        });
    }

    public Task<String> succeedAsync() {
        // Java Generics syntax can be confusing sometimes. :)
        // This creates a TCS for a Task<String>.
        Task<String>.TaskCompletionSource successful = Task.create();
        successful.setResult("The good result.");
        return successful.getTask();
    }

    public Task<String> failAsync() {
        Task<String>.TaskCompletionSource failed = Task.create();
        failed.setError(new RuntimeException("An error message."));
        return failed.getTask();
    }

    public void testErrorHandlingShort() {
        Task<String> successful = Task.forResult("The good result.");
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });

        Task<String> failed = Task.forError(new RuntimeException("An error message."));
        failed.getResult();
    }


    public void testTaskInSeries() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comments");
        query.whereEqualTo("post", 123);

        findAsync(query).continueWithTask(new Continuation<List<ParseObject>, Task<Void>>() {
            public Task<Void> then(Task<List<ParseObject>> results) throws Exception {
                // Create a trivial completed task as a base case.
                Task<Void> task = Task.forResult(null);
                final List<ParseObject> resultsList = results.getResult();
                for (final ParseObject result : resultsList) {
                    // For each item, extend the task with a function to delete the item.
                    task = task.continueWithTask(new Continuation<Void, Task<Void>>() {
                        public Task<Void> then(Task<Void> ignored) throws Exception {
                            // Return a task that will be marked as completed when the delete is finished.
                            return deleteAsync(result);
                        }
                    });
                }
                return task;
            }


        }).continueWith(new Continuation<Void, Task<Void>>() {
            public Task<Void> then(Task<Void> ignored) throws Exception {
                // Every comment was deleted.
                return null;
            }
        });
    }

    public void testTaksInParalel() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comments");
        query.whereEqualTo("post", 123);

        findAsync(query).continueWithTask(new Continuation<List<ParseObject>, Task<Void>>() {
            public Task<Void> then(Task<List<ParseObject>> results) throws Exception {
                // Collect one task for each delete into an array.
                ArrayList<Task<Void>> tasks = new ArrayList<>();
                final List<ParseObject> resultList = results.getResult();
                for (ParseObject result : resultList) {
                    // Start this delete immediately and add its task to the list.
                    tasks.add(deleteAsync(result));
                }
                // Return a new task that will be marked as completed when all of the deletes are
                // finished.
                return Task.whenAll(tasks);
            }
        }).onSuccess(new Continuation<Void, Task<Void>>() {
            public Task<Void> then(Task<Void> ignored) throws Exception {
                // Every comment was deleted.
                return null;
            }
        });
    }

    public void captureVariable(ParseObject obj1, final ParseObject obj2, final ParseObject obj3, final ParseObject obj4) {
        // Capture a variable to be modified in the Task callbacks.
        final Capture<Integer> successfulSaveCount = new Capture<>(0);

        saveAsync(obj1).onSuccessTask(new Continuation<ParseObject, Task<ParseObject>>() {
            public Task<ParseObject> then(Task<ParseObject> obj1) throws Exception {
                successfulSaveCount.set(successfulSaveCount.get() + 1);
                return saveAsync(obj2);
            }
        }).onSuccessTask(new Continuation<ParseObject, Task<ParseObject>>() {
            public Task<ParseObject> then(Task<ParseObject> obj2) throws Exception {
                successfulSaveCount.set(successfulSaveCount.get() + 1);
                return saveAsync(obj3);
            }
        }).onSuccessTask(new Continuation<ParseObject, Task<ParseObject>>() {
            public Task<ParseObject> then(Task<ParseObject> obj3) throws Exception {
                successfulSaveCount.set(successfulSaveCount.get() + 1);
                return saveAsync(obj4);
            }
        }).onSuccess(new Continuation<ParseObject, Void>() {
            public Void then(Task<ParseObject> obj4) throws Exception {
                successfulSaveCount.set(successfulSaveCount.get() + 1);
                return null;
            }
        }).continueWith(new Continuation<Void, Integer>() {
            public Integer then(Task<Void> ignored) throws Exception {
                // successfulSaveCount now contains the number of saves that succeeded.
                return successfulSaveCount.get();
            }
        });
    }

    public void runTask(Callable<Task<?>> callable) {
        try {
            Task<?> task = callable.call();
            task.waitForCompletion();
            if (task.isFaulted()) {
                Exception error = task.getError();
                if (error instanceof RuntimeException) {
                    throw (RuntimeException) error;
                }
                throw new RuntimeException(error);
            } else if (task.isCancelled()) {
                throw new RuntimeException(new CancellationException());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void startTestTask3() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParserApiHis.KEY_ROUTE_COLLECTION);
        List<ParseObject> parseObjects1 = null;
        try {
            parseObjects1 = query.fromLocalDatastore().find();
            for (ParseObject allObject : parseObjects1) {
                final ParseRelation<ParseObject> relation = allObject.getRelation(RouteHisContract.KEY_POINT_OF_INTERESTS);
                final List<ParseObject> parseObjects = relation.getQuery().fromPin(ParserApiHis.KEY_ROUTE_COLLECTION + allObject.getObjectId()).find();
                for (ParseObject parseObject : parseObjects) {
                    Log.d(getLogTag(), parseObject.getString("name"));
                }
                Log.d(getLogTag(), allObject.getString("name"));
            }
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Uncaught exception", e);
        }
    }

    private void startTestTask2() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParserApiHis.KEY_ROUTE_COLLECTION);
        List<ParseObject> parseObjects1 = null;
        try {
            parseObjects1 = query.fromLocalDatastore().find();
            for (ParseObject allObject : parseObjects1) {

                ParseQuery<ParseObject> qury = ParseQuery.getQuery(ParserApiHis.KEY_POI_COLLECTION);
                final List<ParseObject> parseObjects2 = qury.fromPin(ParserApiHis.KEY_ROUTE_COLLECTION + allObject.getObjectId()).find();
                for (ParseObject parseObject : parseObjects2) {
                    Log.d(getLogTag(), parseObject.getString("name"));
                }
                Log.d(getLogTag(), allObject.getString("name"));
            }
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Uncaught exception", e);
        }
    }

    private void startTestTask() {
//        Task<Void> getActiveLanguage = Task.forResult(null);
        ParseQuery<RouteHisContract> query = ParseQuery.getQuery(ParserApiHis.KEY_ROUTE_COLLECTION);
        try {
            final List<RouteHisContract> parseObjects = query.fromLocalDatastore().find();
            for (RouteHisContract parseObject : parseObjects) {
                final ParseRelation<ParseObject> relation = parseObject.getRelation(RouteHisContract.KEY_POINT_OF_INTERESTS);
                final List<ParseObject> query1 = relation.getQuery().fromLocalDatastore().find();
                Log.d(getLogTag(), "size " + query1.size());
                for (ParseObject object : query1) {
                    Log.d(getLogTag(), object.getString("name"));
                }
                Log.d(getLogTag(), parseObject.getString("name"));
            }
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Uncaught exception", e);
        }
    }

}
