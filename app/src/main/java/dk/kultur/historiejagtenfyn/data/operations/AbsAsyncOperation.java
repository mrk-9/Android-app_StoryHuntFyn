package dk.kultur.historiejagtenfyn.data.operations;

import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsAsyncOperation<T> extends AsyncTask<String, Void, T> implements IAsyncOperation {

    private List<AsyncOperationListener> mListeners = new ArrayList<AsyncOperationListener>();

    // operation result
    private T mResult;

    /**
     *
     */
    public AbsAsyncOperation() {
    }

    /**
     *
     */
    public T getResult() {
        return mResult;
    }

    /**
     *
     */
    @Override
    public void addListener(AsyncOperationListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     *
     */
    @Override
    public void removeListener(AsyncOperationListener listener) {
        mListeners.remove(listener);
    }

    /**
     *
     */
    @Override
    public void cancelOperation() {
        mListeners.clear();
        cancel(true);
    }

    /**
     *
     */
    @Override
    public void onPreExecute() {
        for (AsyncOperationListener listener : mListeners) {
            listener.onOperationStarted(this);
        }
    }

    /**
     *
     */
    @Override
    public void onPostExecute(T result) {
        mResult = result;
        for (AsyncOperationListener listener : mListeners) {
            listener.onOperationFinished(this);
        }
        mListeners.clear();
    }

    /**
     *
     */
    @Override
    public void executeOperation() {
        executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

}
