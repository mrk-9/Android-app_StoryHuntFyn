package dk.kultur.historiejagtenfyn.ui.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import dk.kultur.historiejagtenfyn.sync.HisSyncAdapter;
import dk.kultur.historiejagtenfyn.HistoriejagtenfynApplication;
import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.sync.StartUpService;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.ui.dialogs.ObbDownloadDialogFragment;
import dk.kultur.historiejagtenfyn.ui.dialogs.ProgressDialogFragment;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;

/**
 * Splash activity with two fragments
 */
public class SplashScreenActivity extends AbsActivityFullScreen implements
        SyncStatusObserver,
        DialogInterface.OnDismissListener {

    private static String LOG_TAG = SplashScreenActivity.class.getSimpleName();
    private ProgressDialogFragment progressFragment;
    private ObbDownloadDialogFragment obbFragment;

    private boolean mIsBound;
    private Messenger mService;

    private Object mContentProviderHandle;

    private SyncAdapterReceiver syncAdapterReceiver;

    public static final String ACTION_FILTER = "update.dialog.action";
    public static final String TYPE_MESSAGE = "Message";

    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StartUpService.ALL_COMPLETE:
                    Log.d("SplashScreenActivity", "Critical data ready" + msg.obj);
                    doUnbindService();
                    if (progressFragment != null) {
                        progressFragment.dismissAllowingStateLoss();
                        progressFragment = null;
                    }
                    checkMainExtension();
                    break;
                case StartUpService.UPDATE_TEXT:
                    Log.d("SplashScreenActivity", "update ready" + msg.obj);
                    if (progressFragment == null) {
                        progressFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag("PROGRESS");
                    }
                    if (progressFragment != null && progressFragment.getDialog() != null) {
                        progressFragment.setText((String) msg.obj);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    });

    /**
     * Only one dummy account for database sync adapter is used, detects adapter work status
     * @param which
     */
    @Override
    public void onStatusChanged(int which) {
        AccountManager accountManager = AccountManager.get(this);
        // get all accounts
        Account[] accounts = accountManager.getAccountsByType(null);

        if (accounts.length <= 0) {
            return;
        }

        //take first, cause empty account is used for all users
        updateRefresh(ContentResolver.isSyncActive(accounts[0], HisContract.CONTENT_AUTHORITY));
    }

    // Since onStatusChanged() is not called from the main thread
    // I need to update the ui in the ui-thread.
    private void updateRefresh(final boolean isSyncing) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (!isSyncing) {
                    /**
                     * Database sync by dummy account finished, start app components in service
                     */
                    Intent intent = new Intent(SplashScreenActivity.this, StartUpService.class);
                    Log.d(getLogTag(), "startService" + this.toString());
                    startService(intent);
                    doBindService();
                }
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (progressFragment != null) {
            progressFragment.dismissAllowingStateLoss();
            progressFragment = null;
        }
        if (obbFragment != null) {
            obbFragment.dismissAllowingStateLoss();
            obbFragment = null;
        }
        goWelcomeScreen();
    }

    public class SyncAdapterReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.hasExtra(TYPE_MESSAGE) && mMessenger != null) {
                Message message = new Message();
                message.what = StartUpService.UPDATE_TEXT;
                message.obj = intent.getStringExtra(TYPE_MESSAGE);
                try {
                    mMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("SplashScreenActivity", "on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        progressFragment = new ProgressDialogFragment();
        progressFragment.show(getSupportFragmentManager(), "PROGRESS");
    }


    /**
     * Connect the stub from our service on resume
     */
    @Override
    protected void onResume() {
        if (ContentResolver.isSyncActive(HisSyncAdapter.getSyncAccount(this), HisContract.CONTENT_AUTHORITY)) {
            mContentProviderHandle = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, this);

            syncAdapterReceiver = new SyncAdapterReceiver(); // Create the receiver
            registerReceiver(syncAdapterReceiver, new IntentFilter(ACTION_FILTER)); // Register receiver
        } else {
            Intent intent = new Intent(this, StartUpService.class);
            Log.d(getLogTag(), "startService" + this.toString());
            startService(intent);
            doBindService();
        }

        ((HistoriejagtenfynApplication) getApplication()).onActivityResumed();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((HistoriejagtenfynApplication) getApplication()).onActivityPaused();
        if (mContentProviderHandle != null) {
            ContentResolver.removeStatusChangeListener(mContentProviderHandle);
        }

        if (syncAdapterReceiver != null) {
            unregisterReceiver(syncAdapterReceiver);
            syncAdapterReceiver = null;
        }
        doUnbindService();
    }


    /**
     * Opens welcome screen
     */
    private void goWelcomeScreen() {
        startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
        finish();
        overridePendingTransition(0, R.anim.anim_shrink_to_left);
        SoundManager.getInstance(SplashScreenActivity.this).playSound(SoundManager.SOUND_BOOK_PAGE);
    }


    private void checkMainExtension() {
        // Check if expansion files are available before going any further
        if (!FileUtils.expansionFilesDelivered(this)) {
            obbFragment = (ObbDownloadDialogFragment) getSupportFragmentManager().findFragmentByTag("OBB_DOWNLOAD");
            if (obbFragment == null) {
                obbFragment = new ObbDownloadDialogFragment();
            }
            obbFragment.show(getSupportFragmentManager(), "OBB_DOWNLOAD");

            if (progressFragment != null) {
                progressFragment.dismissAllowingStateLoss();
                progressFragment = null;
            }
        } else {
            goWelcomeScreen();
        }
    }


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            Log.i("SplashScreenActivity", "onServiceConnected");
            mService = new Messenger(service);


            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        StartUpService.MSG_REGISTER_CLIENT);

                msg.replyTo = mMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };

    private void doBindService() {
        Log.d("SplashScreenActivity", "bound service");
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(SplashScreenActivity.this,
                StartUpService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        Log.d("SplashScreenActivity", "unbound service");
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            StartUpService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    public static String getLogTag() {
        return LOG_TAG;
    }
}
