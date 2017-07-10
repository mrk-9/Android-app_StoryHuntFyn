package dk.kultur.historiejagtenfyn.sync;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Scanner;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;


/**
 * Start up sync
 * Created by Lina on 2014.06.26.
 */
public class StartUpService extends Service {

    private static final String LOG_TAG = StartUpService.class.getSimpleName();

    public static final int LANGUAGE_COMPLETE = 1;
    public static final int PRIMARY_COMPLETE = 2;
    public static final int STARTUP_SYNC_COMPLETE = 3;
    public static final int SCANNER_SYNC_COMPLETE = 4;
    public static final int ALL_COMPLETE = 10;
    public static final int UPDATE_TEXT = 11;
    public static final int MSG_UNREGISTER_CLIENT = 8;
    public static final int MSG_REGISTER_CLIENT = 9;

    public static final String KEY_ACTIVE_LANGUAGE_ID = "his.key.active_language_key";

    private ExecutorService mExecutor;

    private AtomicBoolean working = new AtomicBoolean(false);

    /**
     * Keeps track of all current registered clients.
     */
    private ArrayList<Messenger> mClients = new ArrayList<>();
    public boolean mDataReady = false;

    private Scanner mScanner;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    // register client
                    Log.d(getLogTag(), "Activity registered in service");
                    mClients.add(msg.replyTo);
                    if (mDataReady) {
                        resumeActivityFlow();
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    // unregister client
                    Log.d(getLogTag(), "Activity UNREGISTERED from service");
                    mClients.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    });
    Scanner.SyncListener mSyncListener = new Scanner.SyncListener() {
        @Override
        public void onSyncStart() {
            Log.d("Moodstocks SDK", "Sync will start.");
        }

        @Override
        public void onSyncComplete() {
            scannerSyncFinished();
        }

        @Override
        public void onSyncFailed(MoodstocksError e) {
            Log.d("Moodstocks SDK", "Sync error #" + e.getErrorCode() + ": " + e.getMessage());
            scannerSyncFinished();
        }

        @Override
        public void onSyncProgress(int total, int current) {
            int percent = (int) ((float) current / (float) total * 100);
            Log.d("Moodstocks SDK", "Sync progressing: " + percent + "%");
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCANNER_SYNC_COMPLETE:
                    Log.i(getLogTag(), "scanner complete");
                    resumeActivityFlow();
                    break;
                case LANGUAGE_COMPLETE:
                    Log.i(getLogTag(), "language complete");
                    startScannerSync();
                    break;
                case PRIMARY_COMPLETE:
                    Log.i(getLogTag(), "primary complete");
                    startLanguageThread();
                    break;
                case STARTUP_SYNC_COMPLETE:
                    Log.i(getLogTag(), "sync complete");
                    startLanguageThread();
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(getLogTag(), "Service START_STICKY");
        /**
         * Unlock lock on ALL COMPLETED
         * Hacky
         */
        if (!working.get()){ //
            working.set(true);
            startLanguageThread();
        }
        Log.d(getLogTag(), "onStartCommand" + this.toString());
        return START_STICKY;
    }

    private void startScannerSync() {
        if (Scanner.isCompatible()) {
            try {
                mScanner = Scanner.get();
                String path = Scanner.pathFromFilesDir(getApplicationContext(), "scanner.db");
                mScanner.open(path, getString(R.string.moodstock_api_key), getString(R.string.moodstock_api_secret));
                mScanner.setSyncListener(mSyncListener);
                mScanner.sync();
            } catch (MoodstocksError moodstocksError) {
                moodstocksError.printStackTrace();
            }
        }
    }

    private void scannerSyncFinished() {
        try {
            mScanner.close();
            mScanner.destroy();
        } catch (MoodstocksError moodstocksError) {
            moodstocksError.printStackTrace();
        }
        Message msg = Message.obtain();
        msg.what = SCANNER_SYNC_COMPLETE;
        Log.i(getLogTag(), "send that scanner sync is complete");
        handler.sendMessage(msg);
    }

    private void startLanguageThread() {
        if (mExecutor == null) {
            mExecutor = Executors.newFixedThreadPool(1);
        }
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                UIUtils.cacheActiveLanguageFromProvider(StartUpService.this);
                Message msg = Message.obtain();
                msg.what = LANGUAGE_COMPLETE;
                Log.i(getLogTag(), "send that language check is complete");
                handler.sendMessage(msg);
            }
        };
        mExecutor.execute(worker);
    }

    /**
     * Sends message to all {@link #mClients} to finish Sync
     */
    private void resumeActivityFlow() {
        Log.i(getLogTag(), "resumeActivityFlow" + mClients.size());
        // resume activity
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message activityMessage = new Message();
                activityMessage.what = ALL_COMPLETE;
                Log.i(getLogTag(), "send that all is complete");
                mClients.get(i).send(activityMessage);
            } catch (RemoteException e) {
                Log.e(getLogTag(), "client dead");
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
        working.set(false);
    }

    /**
     * Sends message through registered {@link #mClients} to publish updates on the UI thread
     *
     * @param text object
     */
    private void updateUI(String text) {
        // resume activity
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message activityMessage = new Message();
                activityMessage.what = UPDATE_TEXT;
                activityMessage.obj = text;
                Log.i(getLogTag(), "send that ui should update with text" + text);
                mClients.get(i).send(activityMessage);
            } catch (RemoteException e) {
                Log.e(getLogTag(), "client dead");
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    public static String getLogTag() {
        return LOG_TAG;
    }
}
