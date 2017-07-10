package dk.kultur.historiejagtenfyn;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.parse.ParseException;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import dk.kultur.historiejagtenfyn.data.DataParser;

/**
 * Created by Lina on 2014.06.26.
 */
public class SyncService extends Service {

    public static final int LANGUAGE_COMPLETE = 1;
    public static final int PRIMARY_COMPLETE = 2;
    public static final int ALL_COMPLETE = 10;
    public static final int MSG_UNREGISTER_CLIENT = 8;
    public static final int MSG_REGISTER_CLIENT = 9;

    private ExecutorService mExecutor;
    private AtomicInteger mExecutorCount = new AtomicInteger();

    /** Keeps track of all current registered clients. */
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    public boolean mDataReady = false;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    // register client
                    Log.d("SyncService", "Activity registered in service");
                    mClients.add(msg.replyTo);
                    if (mDataReady) {
                        resumeActivityFlow();
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    // unregister client
                    Log.d("SyncService", "Activity UNREGISTERED from service");
                    mClients.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);

            }
        }
    });



    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case LANGUAGE_COMPLETE:
                    Log.i("Syncservice", "language complete");
                    startPrimaryContentThreads();
                    break;
                case PRIMARY_COMPLETE:
                    Log.i("Syncservice", "primary complete");
                    resumeActivityFlow();
                    break;

            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SyncService", "Service START_STICKY");

        startInfoThread();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private void startInfoThread() {
        if(mExecutor == null) {
            mExecutor = Executors.newFixedThreadPool(1);
        }

        Runnable worker2 = new Runnable() {
            @Override
            public void run() {
                try {
                    DataParser.fetchInfos(getBaseContext());
                    Message msg = Message.obtain();
                    msg.what = LANGUAGE_COMPLETE;
                    Log.i("SyncService", "send that language is complete");
                    handler.sendMessage(msg);
                } catch (ParseException e) {
                    Log.e("SyncService", "worker2 error: " + e.toString());
                    resumeActivityFlow();
                }
            }
        };

        mExecutor.execute(worker2);

    }

    private void startPrimaryContentThreads() {
        if(mExecutor == null) {
            mExecutor = Executors.newFixedThreadPool(1);
        }

        Runnable worker2 = new Runnable() {
            @Override
            public void run() {
                try {
                    DataParser.fetchRoutes(getBaseContext());
                    Message msg = Message.obtain();
                    msg.what = PRIMARY_COMPLETE;
                    Log.i("SyncService", "send that primary is complete");
                    handler.sendMessage(msg);
                } catch (ParseException e) {
                    Log.e("SyncService", "worker2 error: " + e.toString());
                }
            }
        };

        mExecutor.execute(worker2);

    }

    private void resumeActivityFlow() {
        // resume activity
        for (int i = mClients.size()-1; i >= 0; i--) {
            try {
                Message activityMessage = new Message();
                activityMessage.what = ALL_COMPLETE;
                Log.i("SyncService", "send that all is complete");
                mClients.get(i).send(activityMessage);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
}
